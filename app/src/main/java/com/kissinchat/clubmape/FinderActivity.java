package com.kissinchat.clubmape;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class FinderActivity extends AppCompatActivity implements SensorEventListener {

    // Visuals
    private View bottle;
    private TextView text;

    // Compass
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float currentAzimuth = 0;
    private ArrayList<Float> azimuthCache = new ArrayList<Float>();
    private int azimuthCacheMaxSize = 20;

    // Bottle related
    private float currentBottleRotation = 0;
    private int bottleUpdateRate = 200;
    private boolean updatingBottle = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_finder);

        // Visuals
        bottle = findViewById(R.id.bottleView);
        text = (TextView) findViewById(R.id.distanceText);

        // Compass stuff
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = (Sensor) mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = (Sensor) mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

        updatingBottle = true;
        final Handler h = new Handler();
        final int delay = bottleUpdateRate; //milliseconds
        h.postDelayed(new Runnable() {
            public void run() {
                spinBottle(-currentAzimuth);
                if (updatingBottle) {
                    h.postDelayed(this, delay);
                }
            }
        }, delay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
        updatingBottle = false;
    }

    private void spinBottle(float to) {

        // Update current bottle rotation
        float from = currentBottleRotation;
        currentBottleRotation = to;

        // Find shortest angle
        float optimalTo = optimizeTo(from, to);

        RotateAnimation rotate = new RotateAnimation(from, optimalTo, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(bottleUpdateRate);
        rotate.setRepeatCount(0);
        rotate.setFillAfter(true);
        bottle.startAnimation(rotate);

        text.setText(String.valueOf(to));
    }

    private float optimizeTo(float from, float to) {
        float fromR = (float) Math.toRadians(from);
        float toR = (float) Math.toRadians(to);
        return from + (float) Math.toDegrees( Math.atan2(Math.sin(toR - fromR), Math.cos(toR - fromR)) );
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Get compass information
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
//            currentAzimuth = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            azimuthCache.add( (float)(Math.toDegrees(azimuthInRadians)+360)%360 );
        }

        int azimuthCacheLength = azimuthCache.size();
        if ( azimuthCacheLength >= azimuthCacheMaxSize) {
            float averageAzimuth = 0;

            for (int i = 0; i < azimuthCacheLength; i++) {
                averageAzimuth += azimuthCache.get(i);
            }

            averageAzimuth /= azimuthCacheLength;
            azimuthCache.clear();

            currentAzimuth = averageAzimuth;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}
