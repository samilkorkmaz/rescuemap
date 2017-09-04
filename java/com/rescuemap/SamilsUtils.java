package com.rescuemap;

/**
 * Created by sam on 4.9.2017.
 */

public class SamilsUtils {

    public static boolean isEqual(double val1, double val2, double tol) {
        return Math.abs(val1-val2) <= tol;
    }
}
