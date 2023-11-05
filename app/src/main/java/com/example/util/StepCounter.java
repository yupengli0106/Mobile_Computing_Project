package com.example.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class StepCounter implements SensorEventListener {
    private int sOffsetStep;
    private int sCurrStep;
    private String mTodayDate;
    private boolean mIsCleanStep;
    private boolean mIsCounterStepReset = true;
    private Context mContext;
    private boolean mIsSeparate;
    private boolean mIsBoot;
    private final DatabaseReference usersRef;
    private final DatabaseReference myDatabase;
    private static final String URL = "https://mobile-computing-ef31f-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private final FirebaseAuth mAuth;

    public StepCounter(Context context, boolean separate, boolean boot) {
        mContext = context;
        mIsSeparate = separate;
        mIsBoot = boot;
        sCurrStep = (int) StepSPHelper.getCurrentStep(mContext);
        mIsCleanStep = StepSPHelper.getCleanStep(mContext);
        mTodayDate = StepSPHelper.getStepToday(mContext);
        sOffsetStep = (int) StepSPHelper.getStepOffset(mContext);
        myDatabase = FirebaseDatabase.getInstance(URL).getReference();
        usersRef = myDatabase.child("users");
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int counterStep = (int) event.values[0];
            if (mIsCleanStep) {
                cleanStep(counterStep);
            }
            sCurrStep = counterStep - sOffsetStep;
            if (sCurrStep < 0) {
                cleanStep(counterStep);
            }
            StepSPHelper.setCurrentStep(mContext, sCurrStep);
            StepSPHelper.setElapsedRealTime(mContext, SystemClock.elapsedRealtime());
            StepSPHelper.setLastSensorStep(mContext, counterStep);
            Log.e("TAG", "onSensorChanged: " + sCurrStep);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            // get current user id
            String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setStep(sCurrStep + "");
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        usersRef.child(userId).setValue(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("TAG", "Get username failed: " + databaseError.getMessage());
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void cleanStep(int counterStep) {
        sCurrStep = 0;
        sOffsetStep = counterStep;
        StepSPHelper.setStepOffset(mContext, sOffsetStep);
        mIsCleanStep = false;
        StepSPHelper.setCleanStep(mContext, false);
    }


}
