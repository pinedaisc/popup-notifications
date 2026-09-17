package com.example.popupnotifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Random;

/**
 * Programa el envío de notificaciones popup en tiempos aleatorios usando
 * AlarmManager, que es fiable incluso con la app cerrada o el dispositivo en
 * modo Doze (a diferencia de WorkManager, más sujeto a retrasos del sistema).
 *
 * Cada alarma dispara PopupReceiver, que muestra la notificación y vuelve a
 * llamar a scheduleNext(), generando así intervalos variables.
 */
public final class PopupScheduler {

    private static final int REQUEST_CODE = 1001;

    // Rango del retraso aleatorio entre notificaciones (en minutos).
    // Ajusta MIN/MAX para pruebas (p. ej. usar segundos cambiando el cálculo).
    private static final int MIN_MINUTES = 15;
    private static final int MAX_MINUTES = 60;

    private static final Random RANDOM = new Random();

    private PopupScheduler() {
    }

    /** Programa la próxima notificación con un retraso aleatorio. */
    public static void scheduleNext(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        long delayMinutes = MIN_MINUTES + RANDOM.nextInt(MAX_MINUTES - MIN_MINUTES + 1);
        long triggerAt = System.currentTimeMillis() + delayMinutes * 60_000L;

        PendingIntent pendingIntent = buildPendingIntent(context);

        // Usar alarma exacta si el sistema lo permite; si no, inexacta.
        if (canScheduleExact(alarmManager)) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            // Fallback: alarma inexacta que igualmente atraviesa Doze.
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    /** Cancela cualquier notificación programada. */
    public static void cancel(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(buildPendingIntent(context));
        }
    }

    private static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, PopupReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags);
    }

    private static boolean canScheduleExact(AlarmManager alarmManager) {
        // En Android 12+ (S) las alarmas exactas requieren permiso especial.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
}
