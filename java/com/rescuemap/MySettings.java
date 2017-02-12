package com.rescuemap;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sam on 5.02.2017.
 */

public class MySettings {
    private static final String CHILD_7_YEARS_OLD_SPEED_MPS_STR = "CHILD_7_YEARS_OLD_SPEED_MPS_STR";
    private static final String CHILD_15_YEARS_OLD_SPEED_MPS_STR = "CHILD_15_YEARS_OLD_SPEED_MPS_STR";
    private static final String ADULT_30_YEARS_OLD_SPEED_MPS_STR = "ADULT_30_YEARS_OLD_SPEED_MPS_STR";
    private static final float CHILD_7_YEARS_OLD_DEFAULT_SPEED_MPS = 1f;
    private static final float CHILD_15_YEARS_OLD_DEFAULT_SPEED_MPS = 2f;
    private static final float ADULT_30_YEARS_OLD_DEFAULT_SPEED_MPS = 3f;
    private static SharedPreferences settings;

    public static void init(Context context) {
        settings = context.getSharedPreferences("RESCUE_MAP_SETTINGS", Context.MODE_PRIVATE);
    }

    public static void restoreDefaults() {
        settings.edit().putFloat(CHILD_7_YEARS_OLD_SPEED_MPS_STR, CHILD_7_YEARS_OLD_DEFAULT_SPEED_MPS).commit();
        settings.edit().putFloat(CHILD_15_YEARS_OLD_SPEED_MPS_STR, CHILD_15_YEARS_OLD_DEFAULT_SPEED_MPS).commit();
        settings.edit().putFloat(ADULT_30_YEARS_OLD_SPEED_MPS_STR, ADULT_30_YEARS_OLD_DEFAULT_SPEED_MPS).commit();
    }

    public static float getChild7YearsOldSpeedMps() {
        return settings.getFloat(CHILD_7_YEARS_OLD_SPEED_MPS_STR, CHILD_7_YEARS_OLD_DEFAULT_SPEED_MPS);
    }

    public static float getChild7YearsOldDefaultSpeedMps() {
        return CHILD_7_YEARS_OLD_DEFAULT_SPEED_MPS;
    }

    public static void setChild7YearsOldSpeedMps(float child7YearsOldSpeed_mps) {
        settings.edit().putFloat(CHILD_7_YEARS_OLD_SPEED_MPS_STR, child7YearsOldSpeed_mps).commit();
    }

    public static float getChild15YearsOldSpeedMps() {
        return settings.getFloat(CHILD_15_YEARS_OLD_SPEED_MPS_STR, CHILD_15_YEARS_OLD_DEFAULT_SPEED_MPS);
    }

    public static float getChild15YearsOldDefaultSpeedMps() {
        return CHILD_15_YEARS_OLD_DEFAULT_SPEED_MPS;
    }

    public static void setChild15YearsOldSpeedMps(float child15YearsOldSpeed_mps) {
        settings.edit().putFloat(CHILD_15_YEARS_OLD_SPEED_MPS_STR, child15YearsOldSpeed_mps).commit();
    }

    public static float getAdult30YearsOldSpeedMps() {
        return settings.getFloat(ADULT_30_YEARS_OLD_SPEED_MPS_STR, ADULT_30_YEARS_OLD_DEFAULT_SPEED_MPS);
    }

    public static float getAdult30YearsOldDefaultSpeedMps() {
        return ADULT_30_YEARS_OLD_DEFAULT_SPEED_MPS;
    }

    public static void setAdult30YearsOldSpeedMps(float adult30YearsOldSpeed_mps) {
        settings.edit().putFloat(ADULT_30_YEARS_OLD_SPEED_MPS_STR, adult30YearsOldSpeed_mps).commit();
    }
}
