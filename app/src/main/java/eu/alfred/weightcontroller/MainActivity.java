package eu.alfred.weightcontroller;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import eu.alfred.api.PersonalAssistant;
import eu.alfred.api.PersonalAssistantConnection;
import eu.alfred.api.sensors.SAFFacade;
import eu.alfred.api.sensors.responses.SensorDataResponse;
import eu.alfred.api.speech.Cade;
import eu.alfred.api.speech.responses.CadeResponse;
import eu.alfred.api.storage.CloudStorage;
import eu.alfred.api.storage.responses.BucketResponse;


public class MainActivity extends ActionBarActivity {

    private PersonalAssistant personalAssistant;
    private CloudStorage cloudStorage;
    private SAFFacade safFacade;
    private Cade cade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        cloudStorage = null;
        safFacade = null;
        //personalAssistant = new PersonalAssistant(this);
        cade = null;

        personalAssistant = new PersonalAssistant(this);

        /*cloudStorage = new CloudStorage(personalAssistant.getMessenger());
        safFacade = new SAFFacade(personalAssistant.getMessenger());
        cade = new Cade(personalAssistant.getMessenger());*/
        sendNotification("Connected to AlfredService");

        personalAssistant.setOnPersonalAssistantConnectionListener(new PersonalAssistantConnection() {
            @Override
            public void OnConnected() {
                // Do some stuff
                cade = new Cade(personalAssistant.getMessenger());
            }

            @Override
            public void OnDisconnected() {
                // Do some cleanup stuff
            }
        });

        personalAssistant.Init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBtnSaveClick(View view) {
        EditText nameText = (EditText)this.findViewById(R.id.nameText);
        saveText(nameText.getText().toString());
    }

    public void onBtnReadClick(View view){
        readText();
    }

    private void saveText(String text) {

        //cade.InitiateSpeechRecognition();

        /*cade.GetCadeBackendUrl(new CadeResponse() {
            @Override
            public void OnSuccess(JSONObject jsonObject) {
                //Not used
            }

            @Override
            public void OnSuccess(JSONArray jsonArray) {
                //Not used
            }

            @Override
            public void OnSuccess(String s) {
                String data = s;
            }

            @Override
            public void OnError(Exception e) {

            }
        });*/

       // AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
       // setText(String.valueOf(manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)));

        //setText("Started");
        /*safFacade.GetLiveData("/shirt/tmp", new SensorDataResponse() {
            @Override
            public void OnError(Exception e) {
                setText(e.toString());
            }

            @Override
            public void OnSuccess(Byte[] jsonObject) {
                if (jsonObject != null) {
                    setText(Arrays.toString(jsonObject));
                }
            }
        });
*/

        //safFacade.GetLiveData("/shirt/temp";

        /*

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", text);
            obj.put("key", "HelloAlfredName");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject queryObject = new JSONObject();
        try {
            queryObject.put("key", "HelloAlfredName");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            cloudStorage.deleteJsonObject("HelloAlfredStructuredBucket", queryObject, new BucketResponse() {
                @Override
                public void OnError(Exception e) {
                    setText(e.toString());
                }

                @Override
                public void OnSuccess(JSONObject jsonObject) {

                }

                @Override
                public void OnSuccess(JSONArray jsonArray) {

                }
            });
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }


        cloudStorage.saveJsonObject("HelloAlfredStructuredBucket", obj, new BucketResponse() {
            @Override
            public void OnError(Exception e) {
                setText(e.toString());
            }

            @Override
            public void OnSuccess(JSONObject jsonObject) {

            }

            @Override
            public void OnSuccess(JSONArray jsonArray) {

            }
        });
        */

    }

    private void readText(){
        JSONObject queryObject = new JSONObject();
        try {
            queryObject.put("key", "HelloAlfredName");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        cloudStorage.readJsonArray("HelloAlfredStructuredBucket", queryObject, new BucketResponse() {
            @Override
            public void OnError(Exception e) {
                setText(e.toString());
            }

            @Override
            public void OnSuccess(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        setText(jsonObject.getString("response"));
                    } catch (JSONException e) {
                        setText(e.toString());
                    }
                }
            }

            @Override
            public void OnSuccess(JSONArray jsonArray) {
                setText(jsonArray.toString());
            }
        });
    }

    private void setText(String text){

        TextView nameText = (TextView) this.findViewById(R.id.resultLabel);
        nameText.setText(text);
    }

    //region NotificationHelper
    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.BLUE)
                .setContentTitle(notificationDetails)
                .setContentText("Notification")
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
    //endregion

}
