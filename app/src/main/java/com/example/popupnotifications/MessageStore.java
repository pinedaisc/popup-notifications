package com.example.popupnotifications;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Almacenamiento simple del mensaje del popup usando SharedPreferences.
 */
public final class MessageStore {

    private static final String PREFS_NAME = "popup_prefs";
    private static final String KEY_MESSAGE = "popup_message";

    private MessageStore() {
        // utilidad estática
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Guarda el mensaje que se mostrará en los popups. */
    public static void saveMessage(Context context, String message) {
        prefs(context).edit().putString(KEY_MESSAGE, message).apply();
    }

    /** Devuelve el mensaje guardado o una cadena vacía si no hay ninguno. */
    public static String getMessage(Context context) {
        return prefs(context).getString(KEY_MESSAGE, "");
    }
}
