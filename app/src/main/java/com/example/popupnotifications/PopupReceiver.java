package com.example.popupnotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Recibe la alarma programada, muestra la notificación con el mensaje guardado
 * y reprograma la siguiente con un nuevo retraso aleatorio.
 *
 * También se dispara al reiniciar el dispositivo (BOOT_COMPLETED) para volver a
 * programar la alarma, ya que las alarmas no sobreviven a un reinicio.
 */
public class PopupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Tras reiniciar: si hay un mensaje configurado, reprogramar.
            String message = MessageStore.getMessage(context);
            if (message != null && !message.trim().isEmpty()) {
                PopupScheduler.scheduleNext(context);
            }
            return;
        }

        // Disparo normal de la alarma.
        String message = MessageStore.getMessage(context);
        if (message != null && !message.trim().isEmpty()) {
            NotificationHelper.showNotification(context, message);
        }

        // Reprogramar el siguiente popup con un nuevo retraso aleatorio.
        PopupScheduler.scheduleNext(context);
    }
}
