package com.example.popupnotifications;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Almacenamiento de la ubicación objetivo (latitud/longitud) para el geofencing,
 * usando SharedPreferences.
 */
public final class LocationStore {

    private static final String PREFS_NAME = "popup_prefs";
    private static final String KEY_LAT = "target_lat";
    private static final String KEY_LON = "target_lon";
    private static final String KEY_SET = "target_set";

    private LocationStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Guarda la ubicación objetivo elegida en el mapa. */
    public static void saveTarget(Context context, double lat, double lon) {
        prefs(context).edit()
                .putLong(KEY_LAT, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LON, Double.doubleToRawLongBits(lon))
                .putBoolean(KEY_SET, true)
                .apply();
    }

    /** Indica si ya hay una ubicación objetivo configurada. */
    public static boolean hasTarget(Context context) {
        return prefs(context).getBoolean(KEY_SET, false);
    }

    /** Devuelve la latitud objetivo (0 si no hay). */
    public static double getLat(Context context) {
        return Double.longBitsToDouble(prefs(context).getLong(KEY_LAT, 0L));
    }

    /** Devuelve la longitud objetivo (0 si no hay). */
    public static double getLon(Context context) {
        return Double.longBitsToDouble(prefs(context).getLong(KEY_LON, 0L));
    }

    /** Borra la ubicación objetivo. */
    public static void clear(Context context) {
        prefs(context).edit()
                .remove(KEY_LAT)
                .remove(KEY_LON)
                .putBoolean(KEY_SET, false)
                .apply();
    }
}
