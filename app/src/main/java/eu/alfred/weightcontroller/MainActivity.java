package eu.alfred.weightcontroller;

import android.app.Activity;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import eu.alfred.ui.CircleButton;

public class MainActivity extends eu.alfred.ui.AppActivity {
    private XYPlot plot;
    private static boolean authInProgress = false;
    private GoogleApiClient mClient = null;
    public static final String TAG = "WeightController";

    public void addData(List<Weight> weights) {
        Number[] seriesNumbers = new Number[weights.size()];
        int i = 0;
        for (Weight weight : weights) {
            seriesNumbers[i] = weight.weight;
            ++i;
        }

        XYSeries series = new SimpleXYSeries(Arrays.asList(seriesNumbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Weights");

        LineAndPointFormatter seriesFormat = new LineAndPointFormatter();
        seriesFormat.setPointLabelFormatter(new PointLabelFormatter());
        seriesFormat.configure(getApplicationContext(), R.xml.line_point_formatter_with_labels);

        if (seriesNumbers.length > 2) {
            seriesFormat.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
        }

        plot.addSeries(series, seriesFormat);

        plot.redraw();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleButton = (CircleButton)findViewById(R.id.voiceControlBtn);
        circleButton.setOnTouchListener(new CircleTouchListener());

        plot = (XYPlot)findViewById(R.id.plot);
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);

        Log.i(TAG, "Connecting");
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean("auth_state_pending");
        }

        final MainActivity self = this;
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                DataReader.read(self, mClient);
                            }
                            @Override
                            public void onConnectionSuspended(int i) {
                                Log.i(MainActivity.TAG, "Suspended");
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(MainActivity.TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(MainActivity.TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(MainActivity.TAG, "Google Play services connection failed. Cause: " + result.toString());
                    }
                })
                .build();
    }

    @Override
    public void performAction(String command, Map<String, String> map) {
        Log.i("weightcontroller", "Perform action " + command);
    }

    @Override
    public void performEntityRecognizer(String bla, Map<String, String> map) {

    }

    @Override
    public void performWhQuery(String bla, Map<String, String> map) {

    }

    @Override
    public void performValidity(String bla, Map<String, String> map) {

    }
}
