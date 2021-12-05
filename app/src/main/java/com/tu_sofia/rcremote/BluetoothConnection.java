package com.tu_sofia.rcremote;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnection extends Service {

    Handler bluetoothIn;

    final static String MY_ACTION = "BLUETOOTH_ACTION";

    final int handlerState = 0;

    private BluetoothAdapter btAdapter = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectingThread mConnectingThread;
    public ConnectedThread mConnectedThread;

    private boolean stopThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    @Override
    public void onCreate() {
        super.onCreate();
        stopThread = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
//        bluetoothIn = ((MainActivity) getApplication()).getHandler();
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BluetoothConnection getService() {
            return BluetoothConnection.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        for (String key : intent.getExtras().keySet()) {

            Log.v("DEBUG", (String) intent.getExtras().get(key));

            switch (key) {
                case "controller":
                    if (mConnectedThread.isAlive()) {
                        mConnectedThread.write((String) intent.getExtras().get(key));
                    } else {
                        Log.v("IN", (String) intent.getExtras().get(key));
                    }
                    break;
                case "device_address":
                    address = (String) intent.getExtras().get(key);
                    break;
                default:
                    Log.v("DEBUG", (String) intent.getExtras().get(key));
            }
        }

        bluetoothIn = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String distance;
                String percentage;
                if (msg.what == handlerState) {                                             //if message is what we want
                    String readMessage = (String) msg.obj;                                  // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                        // determine the end-of-line
                    if (endOfLineIndex > 0) {                                               // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string

                        int dataLength = dataInPrint.length();                              //get length of data received
                            percentage = dataInPrint;
                            percentage = percentage.substring(recDataString.indexOf("#")+1, recDataString.indexOf("/") - 1);

                            distance = dataInPrint;
                            distance = distance.substring(recDataString.indexOf("/")+1, dataLength);

                            Intent intent = new Intent();
                            intent.setAction(MY_ACTION);
                            intent.putExtra("battery", percentage);
                            intent.putExtra("distance", distance);
                            sendBroadcast(intent);

                            dataInPrint = " ";
                        recDataString.delete(0, recDataString.length());                    //clear all string data
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();                                   // get Bluetooth adapter
        checkBTState();

        return super.onStartCommand(intent, flags, startId);
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            for (String key : arg1.getExtras().keySet()) {

                Log.v("DEBUG", (String) arg1.getExtras().get(key));

                switch (key) {
                    case "controller":
                        mConnectedThread.write((String) arg1.getExtras().get(key));
                        break;
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bluetoothIn.removeCallbacksAndMessages(null);
        stopThread = true;
        if (mConnectedThread != null) {
            mConnectedThread.closeStreams();
        }
        if (mConnectingThread != null) {
            mConnectingThread.closeSocket();
        }
        Log.d("SERVICE", "onDestroy");
    }

    private void checkBTState() {

        if(btAdapter == null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            stopSelf();
        } else {
            if (btAdapter.isEnabled()) {
                try {
                    //create device and set the MAC address
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);

                    mConnectingThread = new ConnectingThread(device);
                    mConnectingThread.start();
                } catch (IllegalArgumentException e) {
                    Log.d("DEBUG BT", "PROBLEM WITH MAC ADDRESS : " + e.toString());
                    Log.d("BT SEVICE", "ILLEGAL MAC ADDRESS, STOPPING SERVICE");
                    stopSelf();
                }
            } else {
                Log.d("BT SERVICE", "BLUETOOTH NOT ON, STOPPING SERVICE");
                stopSelf();
            }
        }
    }

    // New Class for Connecting Thread
    private class ConnectingThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectingThread(BluetoothDevice device) {
            Log.d("DEBUG BT", "IN CONNECTING THREAD");
            mmDevice = device;
            BluetoothSocket temp = null;
            try {
                temp = mmDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
                Log.d("DEBUG BT", "SOCKET CREATED : " + temp.toString());
            } catch (IOException e) {
                Log.d("DEBUG BT", "SOCKET CREATION FAILED :" + e.toString());
                Log.d("BT SERVICE", "SOCKET CREATION FAILED, STOPPING SERVICE");
                stopSelf();
            }
            mmSocket = temp;
        }

        @Override
        public void run() {
            super.run();
            Log.d("DEBUG BT", "IN CONNECTING THREAD RUN");
            // Establish the Bluetooth socket connection.
            // Cancelling discovery as it may slow down connection
            btAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.d("DEBUG BT", "BT SOCKET CONNECTED");
                mConnectedThread = new ConnectedThread(mmSocket);
                mConnectedThread.start();
                Log.d("DEBUG BT", "CONNECTED THREAD STARTED");
                //I send a character when resuming.beginning transmission to check device is connected
                //If it is not an exception will be thrown in the write method and finish() will be called
                mConnectedThread.write("c");

                BluetoothConnection.MyReceiver myReceiver = new BluetoothConnection.MyReceiver();
                BluetoothConnection.MyReceiver myReceiver1 = new BluetoothConnection.MyReceiver();
                IntentFilter intentFilter = new IntentFilter();
                IntentFilter intentFilter1 = new IntentFilter();
                intentFilter.addAction(MainActivity.MY_ACTION);
                intentFilter1.addAction(AccelerometerActivity.MY_ACTION);
                registerReceiver(myReceiver, intentFilter);
                registerReceiver(myReceiver1, intentFilter1);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (IOException e) {
                try {
                    Log.d("DEBUG BT", "SOCKET CONNECTION FAILED : " + e.toString());
                    Log.d("BT SERVICE", "SOCKET CONNECTION FAILED, STOPPING SERVICE");
                    mmSocket.close();
                    stopSelf();
                } catch (IOException e2) {
                    Log.d("DEBUG BT", "SOCKET CLOSING FAILED :" + e2.toString());
                    Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                    stopSelf();
                    //insert code to deal with this
                }
            } catch (IllegalStateException e) {
                Log.d("DEBUG BT", "CONNECTED THREAD START FAILED : " + e.toString());
                Log.d("BT SERVICE", "CONNECTED THREAD START FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }

        public void closeSocket() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                mmSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT", e2.toString());
                Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }
    }

    public class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	                                        //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();     // Send the obtained bytes to the UI Activity via handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] msgBuffer = input.getBytes();                                                        //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                                                       //write bytes over BT connection via output stream
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                stopSelf();
            }
        }

        public void closeStreams() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                mmInStream.close();
                mmOutStream.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT", e2.toString());
                Log.d("BT SERVICE", "STREAM CLOSING FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }
    }
}
