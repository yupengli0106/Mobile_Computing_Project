package com.example.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import java.util.Calendar;
import java.util.TimeZone;

public class StepService extends Service {

    private static final int SAMPLING_PERIOD_US = SensorManager.SENSOR_DELAY_FASTEST;
    public static final String INTENT_ALARM_0_SEPARATE = "intent_alarm_0_separate";
    public static final String INTENT_BOOT_COMPLETED = "intent_boot_completed";
    private SensorManager mSensorManager;
    private StepCounter mStepCounter;
    private boolean mIsSeparate = false;
    private boolean mIsBoot = false;
    private int alarmCount;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification build = NotificationHelper.getInstance().createSystem()
                    .setOngoing(true)
                    .setTicker("Calculating Steps")
                    .setContentText("Calculating Steps")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .build();
            startForeground(1, build);
        }
        if (null != intent) {
            mIsSeparate = intent.getBooleanExtra(INTENT_ALARM_0_SEPARATE, false);
            mIsBoot = intent.getBooleanExtra(INTENT_BOOT_COMPLETED, false);
        }
        startStepDetector();
        return START_STICKY;
    }

    private void startStepDetector() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && getStepCounter()) {
            addStepCounterListener();
        } else {
            StepSPHelper.setSupportStep(this, false);
        }
    }


    private boolean getStepCounter() {
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        boolean isHaveStepCounter = getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER);
        return null != countSensor && isHaveStepCounter;
    }

    private void addStepCounterListener() {
        StepSPHelper.setSupportStep(this, true);
        if (null != mStepCounter) {
            mStepCounter.setZeroAndBoot(mIsSeparate, mIsBoot);
            return;
        }
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepCounter = new StepCounter(getApplicationContext(), mIsSeparate, mIsBoot);
        mSensorManager.registerListener(mStepCounter, countSensor, SAMPLING_PERIOD_US);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
