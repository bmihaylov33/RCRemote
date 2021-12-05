package com.tu_sofia.rcremote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class AccelerometerActivity extends AppCompatActivity {

    TextView state_text;
    TextView distance_text;
    Button normal_mode_bt;
    Button light_bt;

    ImageView battery;
    ImageView voice_bt;

    private SensorManager sensorManager;
    boolean isClicked = true; //shows if bluetooth button is clicked

    private Context context;

    final static String MY_ACTION = "ACCELEROMETER_ACTIVITY_ACTION";

    BluetoothConnection service;

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        context = getApplicationContext();

        state_text     = (TextView) findViewById(R.id.state_text);
        distance_text  = (TextView) findViewById(R.id.distance_text);
        normal_mode_bt = (Button) findViewById(R.id.normal_mode_bt);
        light_bt       = (Button) findViewById(R.id.light_bt);
        voice_bt       = (ImageView) findViewById(R.id.voice_bt);

        light_bt.setBackgroundResource(R.drawable.lightbulb);

        light_bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isClicked) {
                    v.setBackgroundResource(R.drawable.lightbulb_on);
                    turnShortLightOn();
                    msg("Short lights on");
                    Log.d("Lights", "on");

                } else {
                    v.setBackgroundResource(R.drawable.lightbulb);
                    turnLightOff();
                    msg("Lights off");
                }

                isClicked = !isClicked; //reverse
            }
        });

        light_bt.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                // change your light_bt background

                if (isClicked) {
                    v.setBackgroundResource(R.drawable.lightbulb_on);
                    turnLongLightOn();
                    msg("Long lights on");
                    Log.d("Lights", "on");
                }

                isClicked = !isClicked; //reverse
                return true;
            }
        });

        normal_mode_bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                motorStop();
                disableAccelerometerListening();
                startActivity(intent);
                msg("Arrow keys mode");
            }
        });

        battery = (ImageView) findViewById(R.id.battery_level);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra("en-US", Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                state_text.setText("Listening...");
            }

            @Override
            public void onBeginningOfSpeech() {
                state_text.setText("");
                state_text.setText("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                voice_bt.setImageResource(R.drawable.ic_mic_black);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String[] commands = {"forward", "backward", "left", "right", "stop", "lights on", "lights off"};
                List<String> list = Arrays.asList(commands);

                if(list.contains(data.get(0).toLowerCase())) {
                    switch(data.get(0).toLowerCase()) {
                        case "forward":
                            motorForward();
                            break;
                        case "backward":
                            motorBack();
                            break;
                        case "left":
                            motorLeft();
                            break;
                        case "right":
                            motorRight();
                            break;
                        case "stop":
                            motorStop();
                            break;
                        case "lights":
                            if ((data.get(0).toLowerCase()).contains("on")) {
                                turnLongLightOn();
                            } else if ((data.get(0).toLowerCase()).contains("off")) {
                                turnLightOff();
                            }
                            break;
                        default:
                            state_text.setText("try again...");
                    }
                    state_text.setText(data.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        voice_bt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    voice_bt.setImageResource(R.drawable.ic_mic_black);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });

        //Enable the button
        enableAccelerometerListening();
    }

    @Override
    protected void onStart() {
        //Register BroadcastReceiver to receive event from our service
        AccelerometerActivity.MyReceiver myReceiver = new AccelerometerActivity.MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothConnection.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        super.onStart();
    };

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            for (String key : arg1.getExtras().keySet()) {

                Log.v("DEBUG key", (String) arg1.getExtras().get(key));

                switch (key) {
                    case "distance":
                        displayDistance((String) arg1.getExtras().get(key));
                        break;
                    case "battery":
                        setBatteryState(Float.parseFloat((String) arg1.getExtras().get(key)));
                        break;

                }
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void motorRight() {
        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "R");
        sendBroadcast(intent);
    }

    public void motorLeft() {
        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "L");
        sendBroadcast(intent);
    }

    public void motorForward() {
        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "F");
        sendBroadcast(intent);
    }

    public void motorBack() {
        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "B");
        sendBroadcast(intent);
    }

    public void motorStop() {
        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "Q");
        sendBroadcast(intent);
    }

    public void turnShortLightOn() {
        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "o");
        sendBroadcast(intent);
    }

    public void turnLongLightOn() {
        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "O");
        sendBroadcast(intent);
    }

    public void turnLightOff() {
        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "s");
        sendBroadcast(intent);
    }

    private void enableAccelerometerListening() {
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 1000000);
    }

    private void disableAccelerometerListening() {
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(sensorEventListener);
    }

    public void displayDistance(String distance) {
        distance_text.setText(distance + " cm");
    }

    public void setBatteryState(Float percentage) {
        if(percentage > 80) {
            battery.setImageResource(R.drawable.battery_full);
        } else if(percentage <= 80 & percentage > 60) {
            battery.setImageResource(R.drawable.battery_80);
        } else if(percentage <= 60 & percentage > 40) {
            battery.setImageResource(R.drawable.battery_60);
        } else if(percentage <= 40 & percentage > 20) {
            battery.setImageResource(R.drawable.battery_40);
        } else if(percentage <= 20) {
            battery.setImageResource(R.drawable.battery_20);
            Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Battery Low!!", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else if(percentage < 5) {
            battery.setImageResource(R.drawable.battery_outline);
            Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Change batteries!", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d("accuracy", String.valueOf(event.accuracy));
            }

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];


            if(x > 4) {
                state_text.setText("BACKWARD...");
                motorBack();
            }
            else if(x < -4) {
                state_text.setText("FORWARD...");
                motorForward();
            }
            if(y > 4) {
                state_text.setText("RIGHT...");
                motorRight();
            }
            else if(y < -4) {
                state_text.setText("LEFT...");
                motorLeft();
            }
            else if(x > -4 && x < 4) {
                state_text.setText("STAY");
                motorStop();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//            if (sensor == mValuen) {
//                switch (accuracy) {
//                    case 0:
//                        System.out.println("Unreliable");
//                        con=0;
//                        break;
//                    case 1:
//                        System.out.println("Low Accuracy");
//                        con=0;
//                        break;
//                    case 2:
//                        System.out.println("Medium Accuracy");
//                        con=1;
//
//                        break;
//                    case 3:
//                        System.out.println("High Accuracy");
//                        con=1;
//                        break;
//                }
//            }
        }
    };
}

