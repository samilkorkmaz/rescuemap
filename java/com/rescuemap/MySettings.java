package com.rescuemap;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sam on 5.02.2017.
 */

public class MySettings {
    private static final String CHILD_7_YEARS_OLD_SPEED_KMPH_STR = "CHILD_7_YEARS_OLD_SPEED_KMPH_STR";
    private static final String CHILD_15_YEARS_OLD_SPEED_KMPH_STR = "CHILD_15_YEARS_OLD_SPEED_KMPH_STR";
    private static final String ADULT_30_YEARS_OLD_SPEED_KMPH_STR = "ADULT_30_YEARS_OLD_SPEED_KMPH_STR";
    private static final float CHILD_7_YEARS_OLD_DEFAULT_SPEED_KMPH = 3f;
    private static final float CHILD_7_YEARS_OLD_CONST_RADIUS_M = 7e3f;
    private static final float CHILD_15_YEARS_OLD_DEFAULT_SPEED_KMPH = 6f;
    private static final float CHILD_15_YEARS_OLD_CONST_RADIUS_M = 6.55e3f;
    private static final float ADULT_30_YEARS_OLD_DEFAULT_SPEED_KMPH = 9f;
    private static final float ADULT_30_YEARS_OLD_CONST_RADIUS_M = 19e3f;
    private static SharedPreferences settings;

    public static void init(Context context) {
        settings = context.getSharedPreferences("RESCUE_MAP_SETTINGS", Context.MODE_PRIVATE);
    }

    public static void restoreDefaults() {
        settings.edit().putFloat(CHILD_7_YEARS_OLD_SPEED_KMPH_STR, CHILD_7_YEARS_OLD_DEFAULT_SPEED_KMPH).commit();
        settings.edit().putFloat(CHILD_15_YEARS_OLD_SPEED_KMPH_STR, CHILD_15_YEARS_OLD_DEFAULT_SPEED_KMPH).commit();
        settings.edit().putFloat(ADULT_30_YEARS_OLD_SPEED_KMPH_STR, ADULT_30_YEARS_OLD_DEFAULT_SPEED_KMPH).commit();
    }

    public static float getChild7YearsOldSpeedKmph() {
        return settings.getFloat(CHILD_7_YEARS_OLD_SPEED_KMPH_STR, CHILD_7_YEARS_OLD_DEFAULT_SPEED_KMPH);
    }

    public static float getChild7YearsOldConstRadiusM() {
        return CHILD_7_YEARS_OLD_CONST_RADIUS_M;
    }

    public static float getChild7YearsOldDefaultSpeedKmph() {
        return CHILD_7_YEARS_OLD_DEFAULT_SPEED_KMPH;
    }

    public static void setChild7YearsOldSpeedKmph(float child7YearsOldSpeed_kmph) {
        settings.edit().putFloat(CHILD_7_YEARS_OLD_SPEED_KMPH_STR, child7YearsOldSpeed_kmph).commit();
    }

    public static float getChild15YearsOldSpeedKmph() {
        return settings.getFloat(CHILD_15_YEARS_OLD_SPEED_KMPH_STR, CHILD_15_YEARS_OLD_DEFAULT_SPEED_KMPH);
    }

    public static float getChild15YearsOldDefaultSpeedKmph() {
        return CHILD_15_YEARS_OLD_DEFAULT_SPEED_KMPH;
    }

    public static float getChild15YearsOldConstRadiusM() {
        return CHILD_15_YEARS_OLD_CONST_RADIUS_M;
    }

    public static void setChild15YearsOldSpeedKmph(float child15YearsOldSpeed_kmph) {
        settings.edit().putFloat(CHILD_15_YEARS_OLD_SPEED_KMPH_STR, child15YearsOldSpeed_kmph).commit();
    }

    public static float getAdult30YearsOldSpeedKmph() {
        return settings.getFloat(ADULT_30_YEARS_OLD_SPEED_KMPH_STR, ADULT_30_YEARS_OLD_DEFAULT_SPEED_KMPH);
    }

    public static float getAdult30YearsOldDefaultSpeedKmph() {
        return ADULT_30_YEARS_OLD_DEFAULT_SPEED_KMPH;
    }

    public static float getAdult30YearsOldConstRadiusM() {
        return ADULT_30_YEARS_OLD_CONST_RADIUS_M;
    }

    public static void setAdult30YearsOldSpeedKmph(float adult30YearsOldSpeed_kmph) {
        settings.edit().putFloat(ADULT_30_YEARS_OLD_SPEED_KMPH_STR, adult30YearsOldSpeed_kmph).commit();
    }
}
