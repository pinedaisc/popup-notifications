package com.example.popupnotifications;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

/**
 * Utilidad para crear el canal de notificaciones y publicar notificaciones.
 */
public final class NotificationHelper {

    public static final String CHANNEL_ID = "popup_channel";
    public static final String GEOFENCE_CHANNEL_ID = "geofence_channel";

    /** ID fijo para la notificación persistente del foreground service. */
    public static final int SERVICE_NOTIFICATION_ID = 42;

    private static final Random RANDOM = new Random();

    private NotificationHelper() {
    }

    /** Crea el canal de notificaciones (requerido desde Android 8). */
    public static void createChannel(Context context) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.notification_channel_desc));
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }
        if (manager.getNotificationChannel(GEOFENCE_CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(
                    GEOFENCE_CHANNEL_ID,
                    context.getString(R.string.geofence_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.geofence_channel_desc));
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Muestra una notificación popup con el mensaje dado.
     * Devuelve true si se publicó, false si faltaba el permiso (Android 13+).
     */
    public static boolean showNotification(Context context, String message) {
        createChannel(context);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true);

        // En Android 13+ se requiere permiso en tiempo de ejecución.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        int notificationId = RANDOM.nextInt(Integer.MAX_VALUE);
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        return true;
    }

    /**
     * Notificación que se muestra al entrar en el rango de la ubicación objetivo.
     * Devuelve true si se publicó, false si faltaba el permiso (Android 13+).
     */
    public static boolean showGeofenceNotification(Context context) {
        createChannel(context);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, GEOFENCE_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(context.getString(R.string.geofence_notification_title))
                        .setContentText(context.getString(R.string.geofence_notification_text))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        int notificationId = RANDOM.nextInt(Integer.MAX_VALUE);
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        return true;
    }

    /** Construye la notificación persistente requerida por el foreground service. */
    public static Notification buildServiceNotification(Context context) {
        createChannel(context);
        return new NotificationCompat.Builder(context, GEOFENCE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.geofence_service_title))
                .setContentText(context.getString(R.string.geofence_service_text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
}
