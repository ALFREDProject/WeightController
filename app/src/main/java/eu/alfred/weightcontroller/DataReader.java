package eu.alfred.weightcontroller;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataReader implements Runnable {
    private MainActivity activity;
    private GoogleApiClient mClient;

    private DataReader(MainActivity activity, GoogleApiClient mClient) {
        this.activity = activity;
        this.mClient = mClient;
    }

    public void run() {
        Log.i(MainActivity.TAG, "Connected");

        DataReadRequest readRequest = queryFitnessData();
        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);

        final List<Weight> weights = new ArrayList<>();

        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(MainActivity.TAG, "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet, weights);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(MainActivity.TAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet, weights);
            }
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.addData(weights);
            }
        });
    }

    public static void read(MainActivity activity, GoogleApiClient mClient) {
        new Thread(new DataReader(activity, mClient)).start();
    }

    public static DataReadRequest queryFitnessData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.i(MainActivity.TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(MainActivity.TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        return readRequest;
    }

    private static void dumpDataSet(DataSet dataSet, List<Weight> weights) {
        Log.i(MainActivity.TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Weight weight = new Weight(0, 0);
            Log.i(MainActivity.TAG, "Data point:");
            Log.i(MainActivity.TAG, "\tType: " + dp.getDataType().getName());
            Log.i(MainActivity.TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(MainActivity.TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            weight.time = dp.getEndTime(TimeUnit.MILLISECONDS);
            for(Field field : dp.getDataType().getFields()) {
                Log.i(MainActivity.TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                if (field.getName().equals("weight")) {
                    weight.weight = dp.getValue(field).asFloat();
                }
            }
            weights.add(weight);
        }
    }
}
