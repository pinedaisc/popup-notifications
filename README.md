# Popup Notifications

Aplicación Android sencilla creada como **experimento** para probar el comportamiento de los mensajes push / popup (notificaciones locales) en dispositivos reales.

El objetivo no es un producto terminado, sino un banco de pruebas para observar:

- Cómo aparecen las notificaciones tipo *heads-up* (popup) en distintos dispositivos.
- Si las notificaciones se entregan de forma fiable cuando la app está en **segundo plano** o cerrada.
- Cómo afectan el modo Doze, la optimización de batería y los permisos de alarmas exactas a la entrega.
- El flujo de permisos de notificación en distintas versiones de Android.

## Qué hace

- Una sola pantalla con un formulario: un campo de texto para el mensaje y un botón **Guardar**.
- Un botón **Probar notificación ahora** que dispara una notificación inmediata con el texto actual, para verificación rápida.
- Tras guardar, la app programa notificaciones popup en **tiempos aleatorios** (por defecto entre 15 y 60 minutos), que se repiten indefinidamente reprogramándose solas.

## Cómo funciona

| Componente | Responsabilidad |
|---|---|
| `MainActivity` | UI del formulario, botón de prueba y solicitud de permisos. |
| `MessageStore` | Guarda el mensaje en `SharedPreferences`. |
| `NotificationHelper` | Crea el canal de notificaciones y publica la notificación (BigTextStyle, prioridad alta). |
| `PopupScheduler` | Programa la próxima notificación con `AlarmManager` y un retraso aleatorio. |
| `PopupReceiver` | `BroadcastReceiver` que recibe la alarma, muestra la notificación y reprograma la siguiente. También reprograma tras reiniciar el dispositivo. |

La entrega en segundo plano usa **`AlarmManager`** con `setExactAndAllowWhileIdle`, que dispara a la hora prevista incluso con la app cerrada o el dispositivo en Doze. Cada disparo reprograma el siguiente, generando intervalos variables.

## Requisitos

- Android 12 (API 31) o superior — `minSdk 31`.
- Android SDK instalado (plataforma `android-36`, build-tools `36.0.0`).
- JDK 17.

## Compilar

```bash
./gradlew :app:assembleDebug
```

El APK de debug queda en:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Instalar

Con un dispositivo conectado por USB y depuración activada:

```bash
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

O copia el `app-debug.apk` al teléfono e instálalo manualmente (deberás permitir la instalación de "fuentes desconocidas").

## Notas para que las notificaciones lleguen

Como es un experimento sobre fiabilidad de entrega, ten en cuenta:

- **Alarmas exactas (Android 12):** al guardar, la app abre los ajustes de *Alarmas y recordatorios* si el permiso no está concedido. Actívalo para máxima precisión. Sin él, se usa una alarma inexacta como respaldo.
- **Permiso de notificaciones (Android 13+):** la app lo solicita en tiempo de ejecución. En Android 12 no es necesario.
- **Optimización de batería:** algunos fabricantes (Xiaomi/MIUI, Samsung, Huawei, Oppo) restringen apps en segundo plano. Si las notificaciones programadas no llegan pero el botón de prueba sí, ve a *Ajustes → Apps → Popup Notifications → Batería* y elige **Sin restricciones**.

## Ajustar el intervalo

El rango de tiempo aleatorio se define en `PopupScheduler.java`:

```java
private static final int MIN_MINUTES = 15;
private static final int MAX_MINUTES = 60;
```

Cámbialo para hacer pruebas más rápidas.

---

> Proyecto experimental con fines de aprendizaje sobre notificaciones locales en Android. `applicationId`: `com.example.popupnotifications`.
