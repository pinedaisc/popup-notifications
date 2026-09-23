package com.example.popupnotifications;

import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.popupnotifications.databinding.ActivityMapBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.Locale;

/**
 * Vista de mapa (OpenStreetMap vía osmdroid) para seleccionar una coordenada.
 *
 * El pin está fijo en el centro de la pantalla; el usuario desplaza el mapa y
 * el centro del mapa es la ubicación elegida. Al confirmar, se guarda en
 * LocationStore y se cierra devolviendo RESULT_OK.
 */
public class MapActivity extends AppCompatActivity {

    private ActivityMapBinding binding;
    private MapView map;

    // Ubicación por defecto si no hay una guardada (Ciudad de México).
    private static final double DEFAULT_LAT = 19.4326;
    private static final double DEFAULT_LON = -99.1332;
    private static final double DEFAULT_ZOOM = 17.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // osmdroid requiere configuración de user-agent antes de inflar el mapa.
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        map = binding.mapView;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Centrar en la ubicación guardada o en la de por defecto.
        double lat = LocationStore.hasTarget(this) ? LocationStore.getLat(this) : DEFAULT_LAT;
        double lon = LocationStore.hasTarget(this) ? LocationStore.getLon(this) : DEFAULT_LON;

        map.getController().setZoom(DEFAULT_ZOOM);
        map.getController().setCenter(new GeoPoint(lat, lon));
        updateCoordsLabel(lat, lon);

        // Actualizar la etiqueta de coordenadas cuando el usuario mueve el mapa.
        map.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                GeoPoint center = (GeoPoint) map.getMapCenter();
                updateCoordsLabel(center.getLatitude(), center.getLongitude());
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        });

        binding.confirmButton.setOnClickListener(v -> onConfirm());
    }

    private void onConfirm() {
        GeoPoint center = (GeoPoint) map.getMapCenter();
        LocationStore.saveTarget(this, center.getLatitude(), center.getLongitude());
        setResult(RESULT_OK);
        finish();
    }

    private void updateCoordsLabel(double lat, double lon) {
        binding.coordsLabel.setText(
                String.format(Locale.US, "%s\nLat: %.6f\nLon: %.6f",
                        getString(R.string.map_hint), lat, lon));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }
}
