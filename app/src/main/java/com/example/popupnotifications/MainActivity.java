package com.example.popupnotifications;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.popupnotifications.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Precargar el mensaje guardado previamente.
        binding.messageEditText.setText(MessageStore.getMessage(this));

        // Asegurar el canal de notificaciones.
        NotificationHelper.createChannel(this);

        // Pedir permiso de notificaciones en Android 13+.
        requestNotificationPermissionIfNeeded();

        binding.saveButton.setOnClickListener(v -> onSave());
        binding.testButton.setOnClickListener(v -> onTest());
    }

    private void onSave() {
        String message = currentMessage();

        if (TextUtils.isEmpty(message)) {
            binding.messageInputLayout.setError(getString(R.string.empty_message));
            return;
        }
        binding.messageInputLayout.setError(null);

        MessageStore.saveMessage(this, message);

        // Pedir permiso de alarmas exactas si el sistema lo requiere (Android 12+).
        ensureExactAlarmPermission();

        // (Re)iniciar la programación de popups aleatorios.
        PopupScheduler.scheduleNext(this);

        Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show();
    }

    /** Dispara una notificación inmediata para probar. */
    private void onTest() {
        String message = currentMessage();
        if (TextUtils.isEmpty(message)) {
            binding.messageInputLayout.setError(getString(R.string.test_empty));
            return;
        }
        binding.messageInputLayout.setError(null);

        boolean shown = NotificationHelper.showNotification(this, message);
        if (!shown) {
            // No se pudo mostrar: falta el permiso de notificaciones (Android 13+).
            requestNotificationPermissionIfNeeded();
            Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_LONG).show();
        }
    }

    private String currentMessage() {
        return binding.messageEditText.getText() != null
                ? binding.messageEditText.getText().toString().trim()
                : "";
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    /**
     * En Android 12 (S) las alarmas exactas requieren que el usuario conceda el
     * permiso "Alarmas y recordatorios". Si no está concedido, abrimos la
     * pantalla de ajustes correspondiente. En Android 13+ con USE_EXACT_ALARM
     * se concede automáticamente y no hace falta.
     */
    private void ensureExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                try {
                    startActivity(intent);
                } catch (Exception ignored) {
                    // Si no se puede abrir, se usará una alarma inexacta como fallback.
                }
            }
        }
    }
}
