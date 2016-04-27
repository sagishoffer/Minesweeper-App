package com.example.sagi.mines;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MovementService extends Service implements SensorEventListener {

    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_ADD_RANDOM_MINE = 3;
    static final int MSG_REGISTER_SENSOR_LISTENER = 4;
    static final int MSG_UNREGISTER_SENSOR_LISTENER = 5;

    /** Target we publish for clients to send messages to IncomingHandler. */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /** Keeps track of all current registered clients. */
    Messenger mClients;

    private SensorManager sManager;
    private boolean isSensorManagerRegister = false;
    private int rotateDelta = 25;
    private float interval = 5; // seconds
    private long lastChange;

    private float originalRotateVector[];
    private float tempRotateVector[];

    public MovementService() {
    }


    /** Handler of incoming messages from clients */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    registerClient(msg);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients = null;
                    break;
                case MSG_REGISTER_SENSOR_LISTENER:
                    registerSensorListener();
                    break;
                case MSG_UNREGISTER_SENSOR_LISTENER:
                    unRegisterSensorListener();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void registerClient(Message msg) {
        mClients = msg.replyTo;

        if(sManager == null) {
            Log.i("SensorService", "Init Manager");
            sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }
    }

    private void registerSensorListener() {
        if(!isSensorManagerRegister) {
            Log.i("SensorService", "registerSensorListener");
            sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
            isSensorManagerRegister = true;
        }
    }

    private void unRegisterSensorListener(){
        if(isSensorManagerRegister) {
            Log.i("SensorService", "unRegisterSensorListener");
            sManager.unregisterListener(this);
            isSensorManagerRegister = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        long currentTime = System.currentTimeMillis();

        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            return;
        }

        Log.i("SensorService", "onSensorChanged");

        if(originalRotateVector == null) {
            lastChange = currentTime;
            originalRotateVector = event.values.clone();
            Log.i("SensorService", "rotateVector.length " + originalRotateVector.length);
        }

        if(currentTime - lastChange > interval * 1000) {
//            Log.i("SensorService", "event.values[0] " + event.values[0]);
//            Log.i("SensorService", "event.values[1] " + event.values[1]);
//            Log.i("SensorService", "event.values[2] " + event.values[2]);
            if (isDeltaChange(originalRotateVector, event.values)) {
                try {
                    Message msg = Message.obtain(null, MovementService.MSG_ADD_RANDOM_MINE);
                    msg.replyTo = mMessenger;
                    mClients.send(msg);
                } catch (RemoteException e) {

                }
//            //else it will output the Roll, Pitch and Yawn values
//            Log.i("Sensor", "Orientation X (Roll) :" + Float.toString(event.values[2]) + "\n" +
//                    "Orientation Y (Pitch) :" + Float.toString(event.values[1]) + "\n" +
//                    "Orientation Z (Yaw) :" + Float.toString(event.values[0]));
            }

            lastChange = currentTime;
        }
    }

    private boolean isDeltaChange(float oldRotateVector[], float newRotateVector[]) {

        for(int i=0; i<newRotateVector.length; i++) {
            Log.i("isDeltaChange", "rotateVector[i] = " + oldRotateVector[i]);
            Log.i("isDeltaChange", "newRotateVector[i] = " + newRotateVector[i]);
            Log.i("isDeltaChange", "Math.abs(newRotateVector[i] - rotateVector[i]) = " + Math.abs(newRotateVector[i] - oldRotateVector[i]));

            if(Math.abs(newRotateVector[i] - oldRotateVector[i]) > rotateDelta) {
                Log.i("isDeltaChange", "true");
                return true;
            }
        }

        Log.i("isDeltaChange", "false");
        return false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
