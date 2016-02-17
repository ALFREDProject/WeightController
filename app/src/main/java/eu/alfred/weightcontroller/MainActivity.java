package eu.alfred.weightcontroller;

import android.os.Bundle;
import android.util.Log;

import java.util.Map;

import eu.alfred.ui.CircleButton;

public class MainActivity extends eu.alfred.ui.AppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleButton = (CircleButton)findViewById(R.id.voiceControlBtn);
        circleButton.setOnTouchListener(new CircleTouchListener());
    }

    @Override
    public void performAction(String command, Map<String, String> map) {
        Log.i("weightcontroller", "Perform action " + command);
    }
}
