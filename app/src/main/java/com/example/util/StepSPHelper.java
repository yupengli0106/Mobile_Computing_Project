package com.example.util;
import android.content.Context;

public class StepSPHelper {

    private static final String LAST_SENSOR_TIME = "last_sensor_time";
    private static final String STEP_OFFSET = "step_offset";
    private static final String STEP_TODAY = "step_today";
    private static final String CLEAN_STEP = "clean_step";
    private static final String CURR_STEP = "curr_step";
    private static final String SHUTDOWN = "shutdown";
    private static final String ELAPSED_REAL_TIME = "elapsed_real_time";
    private static final String IS_SUPPORT_STEP = "is_support_step";

    protected static void setLastSensorStep(Context context, float lastSensorStep) {
        StepSharedPreferencesUtil.setParam(context, LAST_SENSOR_TIME, lastSensorStep);
    }

    protected static float getLastSensorStep(Context context) {
        return (float) StepSharedPreferencesUtil.getParam(context, LAST_SENSOR_TIME, 0.0f);
    }

    protected static void setStepOffset(Context context, float stepOffset) {
        StepSharedPreferencesUtil.setParam(context, STEP_OFFSET, stepOffset);
    }

    protected static float getStepOffset(Context context) {
        return (float) StepSharedPreferencesUtil.getParam(context, STEP_OFFSET, 0.0f);
    }


    protected static void setStepToday(Context context, String stepToday) {
        StepSharedPreferencesUtil.setParam(context, STEP_TODAY, stepToday);
    }

    protected static String getStepToday(Context context) {
        return (String) StepSharedPreferencesUtil.getParam(context, STEP_TODAY, "");
    }


    protected static void setCleanStep(Context context, boolean cleanStep) {
        StepSharedPreferencesUtil.setParam(context, CLEAN_STEP, cleanStep);
    }


    protected static boolean getCleanStep(Context context) {
        return (boolean) StepSharedPreferencesUtil.getParam(context, CLEAN_STEP, true);
    }


    // Save current step
    protected static void setCurrentStep(Context context, float currStep) {
        StepSharedPreferencesUtil.setParam(context, CURR_STEP, currStep);
    }

    // Return current step
    protected static float getCurrentStep(Context context) {
        return (float) StepSharedPreferencesUtil.getParam(context, CURR_STEP, 0.0f);
    }


    // Checks the elapsed real-time since the device was booted
    protected static void setElapsedRealTime(Context context, long elapsedRealTime) {
        StepSharedPreferencesUtil.setParam(context, ELAPSED_REAL_TIME, elapsedRealTime);
    }

    // Returns the elapsed real-time since the device was booted
    protected static long getElapsedRealTime(Context context) {
        return (long) StepSharedPreferencesUtil.getParam(context, ELAPSED_REAL_TIME, 0L);
    }

    // Check if it is supported for count step
    protected static void setSupportStep(Context context, boolean isSupportStep) {
        StepSharedPreferencesUtil.setParam(context, IS_SUPPORT_STEP, isSupportStep);
    }

    // Check if it is supported for count step
    protected static boolean getSupportStep(Context context) {
        return (boolean) StepSharedPreferencesUtil.getParam(context, IS_SUPPORT_STEP, false);
    }

}
