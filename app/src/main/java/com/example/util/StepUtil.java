package com.example.util;
import android.content.Context;

public class StepUtil {

    // Checks if the step sensor is available on the device
    public static boolean isSupportStep(Context context) {
        return StepSPHelper.getSupportStep(context);
    }

    // Checks if the step sensor is available on the device
    public static int getTodayStep(Context context) {
        return (int) StepSPHelper.getCurrentStep(context);
    }

}
