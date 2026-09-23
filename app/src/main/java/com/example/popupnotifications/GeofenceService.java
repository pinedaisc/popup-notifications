package com.example.popupnotifications;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

/**
 * Servicio en primer plano (foreground service) que monitorea la ubicación del
 * dispositivo y, cuando entra dentro del radio configurado alrededor de la
 * ubicación objetivo, dispara una notificación.
 *
 * Usa LocationManager nativo (sin Google Play Services). Aplica histéresis para
 * notificar solo al ENTRAR desde fuera, evitando repetir la notificación
 * mientras el dispositivo permanece dentro de la zona.
 */
public class GeofenceService extends Service implements LocationListener {

    /** Radio mínimo sugerido de detección, en metros (el GPS tiene ~5-20 m de error). */
    public static final float RADIUS_METERS = 20f;

    // Frecuencia de actualización de ubicación.
    private static final long MIN_TIME_MS = 5_000L;
    private static final float MIN_DISTANCE_M = 0f;

    private LocationManager locationManager;
    private boolean insideZone = false;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Arrancar como foreground service con su notificación persistente.
        startForeground(
                NotificationHelper.SERVICE_NOTIFICATION_ID,
                NotificationHelper.buildServiceNotification(this));

        startLocationUpdates();
        return START_STICKY;
    }

    private void startLocationUpdates() {
        if (locationManager == null) {
            stopSelf();
            return;
        }
        if (!hasLocationPermission()) {
            stopSelf();
            return;
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, MIN_TIME_MS, MIN_DISTANCE_M, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, MIN_TIME_MS, MIN_DISTANCE_M, this);
            }
        } catch (SecurityException e) {
            stopSelf();
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (!LocationStore.hasTarget(this)) {
            return;
        }

        Location target = new Location("target");
        target.setLatitude(LocationStore.getLat(this));
        target.setLongitude(LocationStore.getLon(this));

        float distance = location.distanceTo(target);
        boolean nowInside = distance <= RADIUS_METERS;

        // Histéresis: notificar solo en la transición de fuera -> dentro.
        if (nowInside && !insideZone) {
            NotificationHelper.showGeofenceNotification(this);
        }
        insideZone = nowInside;
    }

    // Callbacks heredados requeridos en versiones antiguas; no se usan.
    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException ignored) {
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Inicia el servicio de monitoreo. */
    public static void start(Context context) {
        Intent intent = new Intent(context, GeofenceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    /** Detiene el servicio de monitoreo. */
    public static void stop(Context context) {
        context.stopService(new Intent(context, GeofenceService.class));
    }
}
