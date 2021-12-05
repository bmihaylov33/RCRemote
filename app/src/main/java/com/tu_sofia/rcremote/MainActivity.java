package com.tu_sofia.rcremote;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView distance_text;
    TextView state_text;

    Button left_bt;
    Button right_bt;
    Button up_bt;
    Button down_bt;
    Button accelerometer_bt;
    Button light_bt;

    ImageView voice_bt;
    ImageView battery;

    boolean isClicked = false;
    boolean clicked = true;
    boolean arrow_clicked = true;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbind;

    final static String MY_ACTION = "MAIN_ACTIVITY_ACTION";

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;

    // To invoke the bound service, first make sure that this value
    // is not null.
    private BluetoothConnection mBoundService;

    private static Context context;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((BluetoothConnection.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };

    void doBindService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).

        if (bindService(new Intent(this, BluetoothConnection.class),
                mConnection, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true;
        } else {
            Log.e("MY_APP_TAG", "Error: The requested service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    void doUnbindService() {
        if (mShouldUnbind) {
            // Release information about the service's state.
            unbindService(mConnection);
            mShouldUnbind = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = getApplicationContext();

        initializeButtons();
    }

    @Override
    protected void onStart() {
        //Register BroadcastReceiver to receive event from our service
        MyReceiver myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothConnection.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        super.onStart();
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

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

    private void initializeButtons() {

        state_text       = (TextView) findViewById(R.id.state_text);
        distance_text    = (TextView) findViewById(R.id.distance_text);
        battery          = (ImageView) findViewById(R.id.battery_level);
        voice_bt         = (ImageView) findViewById(R.id.voice_bt);
        right_bt         = (Button) findViewById(R.id.right_arrow_bt);
        left_bt          = (Button) findViewById(R.id.left_arrow_bt);
        up_bt            = (Button) findViewById(R.id.up_arrow_bt);
        down_bt          = (Button) findViewById(R.id.down_arrow_bt);
        light_bt         = (Button) findViewById(R.id.light_bt);
        accelerometer_bt = (Button) findViewById(R.id.accelerometer_bt);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
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

        accelerometer_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), AccelerometerActivity.class);
                startActivity(intent);
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

        right_bt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int eventAction = event.getAction();
                switch (eventAction) {
                    case MotionEvent.ACTION_DOWN:
                        view.setBackgroundResource(R.drawable.arrow_right_clicked);
                        motorRight();
                        state_text.setText("RIGHT...");
                        Log.d("motors", "right");
                        return true;
                    case MotionEvent.ACTION_UP:
                        view.setBackgroundResource(R.drawable.arrow_right);
                        motorStop();
                        state_text.setText("STAY");
                        break;
                }
                return false;
            }
        });

        left_bt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int eventAction = event.getAction();
                switch (eventAction) {
                    case MotionEvent.ACTION_DOWN:
                        view.setBackgroundResource(R.drawable.arrow_left_clicked);
                        motorLeft();
                        state_text.setText("LEFT...");
                        Log.d("motors", "left");
                        return true;
                    case MotionEvent.ACTION_UP:
                        view.setBackgroundResource(R.drawable.arrow_left);
                        motorStop();
                        state_text.setText("STAY");
                        break;
                }
                return false;
            }
        });

        up_bt.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int eventAction = event.getAction();
                switch (eventAction) {
                    case MotionEvent.ACTION_DOWN:
                        view.setBackgroundResource(R.drawable.arrow_up_clicked);
                        motorForward();
                        state_text.setText("FORWARD...");
                        Log.d("motors", "forward");
                        return true;
                    case MotionEvent.ACTION_UP:
                        view.setBackgroundResource(R.drawable.arrow_up);
                        motorStop();
                        state_text.setText("STAY");
                        break;
                }
//                        if (clicked) {
//                    view.setBackgroundResource(R.drawable.arrow_up_clicked);
//                    motorForward();
//                    state_text.setText("FORWARD...");
//                    Log.d("motors", "forward");
//
//                } else {
//                    view.setBackgroundResource(R.drawable.arrow_up);
//                    motorStop();
//                    state_text.setText("STAY");
//                }
//                clicked = !clicked; //reverse
                return false;
            }
        });

        down_bt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int eventAction = event.getAction();
                switch (eventAction) {
                    case MotionEvent.ACTION_DOWN:
                        view.setBackgroundResource(R.drawable.arrow_down_clicked);
                        motorBack();
                        state_text.setText("BACKWARD...");
                        Log.d("motors", "backward");
                        return true;
                    case MotionEvent.ACTION_UP:
                        view.setBackgroundResource(R.drawable.arrow_down);
                        motorStop();
                        state_text.setText("STAY");
                        break;
                }
                return false;
            }
        });

        light_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClicked) {
                    view.setBackgroundResource(R.drawable.lightbulb_on);
                    turnShortLightOn();
                    msg("Short lights on");
                    Log.d("Lights", "on");

                } else {
                    view.setBackgroundResource(R.drawable.lightbulb);
                    turnLightOff();
                    msg("Lights off");
                }
                isClicked = !isClicked; //reverse
            }
        });

        light_bt.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {

                if (isClicked) {
                    view.setBackgroundResource(R.drawable.lightbulb_on);
                    turnLongLightOn();
                    msg("Long lights on");
                    Log.d("Lights", "long on");
                }
                isClicked = !isClicked; //reverse
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        alertMsg();
    }

    @Override
    protected void onStop() {
        super.onStop();

        doUnbindService();
    };

    public void alertMsg() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Press back again to exit.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void motorRight() {
//        Intent intent = new Intent(MainActivity.this, BluetoothConnection.class);
//        intent.putExtra("controller", "R");
//        this.startService(intent);

        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "R");
        sendBroadcast(intent);

//        mBoundService.mConnectedThread.write("R");
    }

    public void motorLeft() {
//        Intent intent = new Intent(MainActivity.this, BluetoothConnection.class);
//        intent.putExtra("controller", "L");
//        this.startService(intent);

        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "L");
        sendBroadcast(intent);

//        mBoundService.mConnectedThread.write("L");
    }

    public void motorForward() {
//        Intent intent = new Intent(MainActivity.this, BluetoothConnection.class);
//        intent.putExtra("controller", "F");
//        this.startService(intent);

        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "F");
        sendBroadcast(intent);

//        mBoundService.mConnectedThread.write("F");
    }

    public void motorBack() {
//        Intent intent = new Intent(MainActivity.this, BluetoothConnection.class);
//        intent.putExtra("controller", "B");
//        this.startService(intent);

        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "B");
        sendBroadcast(intent);

//        mBoundService.mConnectedThread.write("B");
    }

    public void motorStop() {
//        Intent intent = new Intent(MainActivity.this, BluetoothConnection.class);
//        intent.putExtra("controller", "Q");
//        this.startService(intent);

        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        intent.putExtra("controller", "Q");
        sendBroadcast(intent);

//        mBoundService.mConnectedThread.write("Q");
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

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
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
}