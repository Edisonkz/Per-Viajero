# Funcionalidades y Catálogo Completo de Pantallas a Implementar (Semanas 10 a 16)

**Proyecto:** `PerúViajero` (`pe.edu.utp.perviajero`)  
**Documento maestro:** [README.md](README.md) • **Línea base:** [01_estado_actual.md](01_estado_actual.md)

---

## 1. Consolidación y Mejora del Avance Actual — Semana 10 (Post-APF2)

Antes de construir los nuevos módulos de hardware y despliegue, se corrige la deuda técnica existente:

1. **Soporte Offline en `FavoritosFragment.kt`:** Usar `obtenerDestinosEnCache()` como respaldo si falla la llamada HTTP.
2. **Restauración de Última Búsqueda en `DestinosFragment.kt`:** Recuperar `getLastSearch()` al iniciar la vista y aplicarlo automáticamente al buscador.
3. **Helper Compartido de Vouchers (`VoucherDialogHelper.kt`):** Extraer la lógica duplicada de `mostrarDetalleVoucher()` y `compartirBoleto()` entre `MisReservasFragment` y `AdminDashboardFragment`.
4. **Corrección de Deprecaciones y Documentación:**
   * Cambiar `ChipGroup.setOnCheckedChangeListener` por `setOnCheckedStateChangeListener` en `ItinerarioActivity.kt`.
   * Actualizar `README.md` raíz (eliminar `MockData.kt` y `/v1/destinos`) y corregir rutas en `planeamiento/README.md`.

---

## 2. Nuevas Funcionalidades Curriculares (Semanas 11 a 16)

### F-S11: Red Social "Comunidad de Viajeros" con Cámara Real (Semana 11)
Apartado social orientado a turistas reales donde comparten fotografías y reseñas de los lugares que visitaron en el Perú, filtrados por **Región (Departamento)** y por **Destino específico**.

* **Publicación de Experiencias (`PublicarExperienciaActivity.kt`):**
  * Selección obligatoria de **Región/Departamento** (`Cusco`, `Arequipa`, `Ica`, `Puno`, `Amazonas`, `Áncash`, `Piura`, `Loreto`, `La Libertad`, `Lima`) y **Destino visitado** (carga automática desde los 12 destinos oficiales; si se entra desde `DetalleDestinoActivity`, llega preseleccionado).
  * **Captura de fotografía con la Cámara del dispositivo** mediante `ActivityResultContracts.TakePicture` + `FileProvider` (con opción secundaria de galería `PickVisualMedia`).
  * Calificación con estrellas (`RatingBar`), título/consejo práctico para otros viajeros (ej. mejor hora, clima, costo local) y fecha de visita.
* **Feed Social Filtrable (`ComunidadActivity.kt` / `PublicacionAdapter.kt`):**
  * Filtros dinámicos en pantalla por **Región** (chips horizontales) y por **Destino seleccionado**.
  * Tarjetas tipo red social con autor, rol/insignia de viajero, región + destino, fotografía en HD cargada con Glide, texto de reseña, calificación, contador de **"Me gusta / Útil"** persistente, comentarios rápidos y botón para ver el destino o ir a reservarlo.
  * Seed inicial realista con publicaciones por región para que el feed nunca luzca vacío al abrir por primera vez.
* **Foto de Perfil del Usuario (`AjustesFragment.kt`):**
  * Posibilidad de capturar o actualizar la foto del perfil del turista/administrador usando la cámara y guardarla localmente.
* **Equivalencia React Native (Sílabo):** `expo-camera` / `expo-image-picker` (`launchCameraAsync`).

---

### F-S12: Mapa Interactivo con Ruta GPS y Brújula Turística Offline (Semana 12)
Uso real de los sensores de ubicación e inerciales/magnéticos del dispositivo al elegir un destino turístico.

#### A. Mapa Interactivo con Trazado de Ruta desde GPS (`MapaRutaActivity.kt` + mejora de `RutaDestinoActivity.kt`)
* **Ubicación real del turista (`LocationHelper.kt`):** Obtenida mediante `FusedLocationProviderClient` (`play-services-location`).
* **Mapa embebido dentro de la app (`OSMDroid` / OpenStreetMap):**
  * No requiere API Key de facturación y soporta caché de mapa.
  * Dibuja un marcador de **Tu ubicación actual (GPS)** y un marcador del **Destino turístico** (`latitud`, `longitud` de `destinos.json`).
  * **Trazado de Ruta (`Polyline`):**
    * Con conexión a internet: consulta la ruta vial real entre `[latOrigen, lngOrigen]` y `[latDestino, lngDestino]` vía servicio público **OSRM** (`router.project-osrm.org`) y traza la polilínea exacta sobre carreteras en el mapa.
    * Sin conexión o en rutas sin vía terrestre (ej. Iquitos / Machu Picchu tramo final): traza automáticamente la línea geodésica directa entre ambos puntos e informa la distancia en línea recta.
  * **Panel de telemetría de ruta:** Distancia exacta en km (fórmula de Haversine / longitud de ruta), tiempo estimado referencial, botón *"Centrar en mi ruta"*, acceso directo **"Abrir Brújula Offline"** y opción secundaria **"Abrir navegación externa en Google Maps"**.

#### B. Brújula Turística Offline (`BrujulaActivity.kt` + `CompassView.kt`)
* **Funcionamiento 100% sin internet (Modo Offline real):**
  * Lee los sensores físicos mediante `SensorManager`: **`Sensor.TYPE_ACCELEROMETER`** + **`Sensor.TYPE_MAGNETIC_FIELD`** (usando `SensorManager.getRotationMatrix()` + `SensorManager.getOrientation()`, con filtro paso-bajo para suavizar la aguja; o `TYPE_ROTATION_VECTOR` si el dispositivo lo soporta).
  * Libera los sensores en `onPause()` para no drenar batería.
* **Funciones en pantalla:**
  * **Rosa de los vientos animada (`CompassView`):** Gira suavemente en tiempo real apuntando al **Norte magnético**.
  * Lectura digital en grados (`0° – 360°`) y punto cardinal (`N`, `NE`, `E`, `SE`, `S`, `SO`, `O`, `NO`).
  * **Indicador de Rumbo hacia el Destino (*Bearing*):** Si se abre desde un destino (o seleccionando uno de la caché offline), calcula el azimut hacia el atractivo (`Location.bearingTo`) respecto a la última ubicación GPS guardada y muestra una **aguja secundaria direccional** con el texto *"Rumbo a [Destino]: X° — Gira a la izquierda/derecha"*, útil en trekking, montaña o selva sin cobertura.
  * Indicador de precisión del sensor magnético + tarjeta de estado **"Modo Offline Activo — Sensores locales"** + datos técnicos colapsables (valores de acelerómetro, magnetómetro y última coordenada conocida).
* **Equivalencia React Native (Sílabo):** `expo-location` + `expo-sensors` (`Accelerometer` / `Magnetometer`).

---

### F-S13: Solicitud de Permisos en Tiempo de Ejecución (Semana 13)
* **Gestor Centralizado (`PermissionHelper.kt`):**
  * Administra con `ActivityResultContracts.RequestPermission` y `RequestMultiplePermissions` los permisos:
    * `android.permission.CAMERA` (Publicaciones en Comunidad y foto de perfil).
    * `android.permission.ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION` (Mapa con ruta GPS y rumbo en brújula).
    * `android.permission.POST_NOTIFICATIONS` en Android 13+ / API 33+ (Alertas de reservas, recordatorios y push).
  * **Ciclo completo de UX:**
    1. Verificación previa (`ContextCompat.checkSelfPermission`).
    2. Diálogo explicativo (*rationale*) cuando `shouldShowRequestPermissionRationale` es verdadero.
    3. Detección de denegación permanente (*"No volver a preguntar"*) con botón directo a **Ajustes del Sistema** (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`).
* **Panel de Estado de Permisos en `AjustesFragment.kt`:** Muestra si Cámara, Ubicación y Notificaciones están concedidos o pendientes, permitiendo habilitarlos desde un mismo lugar.
* **Equivalencia React Native (Sílabo):** `PermissionsAndroid` / `Expo Permissions` y configuración equivalente en `Info.plist` (iOS).

---

### F-S14: Notificaciones Locales Programadas y Push (FCM) (Semana 14)
* **Notificaciones Locales Programadas (`RecordatorioViajeWorker.kt` con `WorkManager`):**
  * Al confirmar una reserva en `ReservaTourActivity` o agregar una actividad con fecha en `ItinerarioActivity`, programa un recordatorio automático en segundo plano (persistente aunque la app se cierre).
  * Incluye botón de demostración en Ajustes (*"Simular recordatorio de viaje en 10s"*) para verificación en clase.
* **Notificaciones Push Remotas (`PeruViajeroMessagingService.kt` con Firebase Cloud Messaging):**
  * Recepción de mensajes push en primer plano (`onMessageReceived`) y segundo plano (alertas de clima/rutas, cambio de estado de reserva o promociones por región).
  * Al tocar la notificación push, redirige con `PendingIntent` a `MainActivity` o al destino/reserva correspondiente.
  * Visualización/copiado del **Token FCM** en Ajustes para pruebas desde Firebase Console.
* **Equivalencia React Native (Sílabo):** `expo-notifications` (`scheduleNotificationAsync` / push tokens) y `@react-native-firebase/messaging`.

---

### F-S15: Rendimiento, Optimización, Manejo de Errores y Logging (Semana 15 — `APF3`)
* **Rendimiento de UI y Red:**
  * Migrar `DestinoAdapter`, `ReservaAdapter`, `ActividadItinerarioAdapter` y `PublicacionAdapter` a **`ListAdapter` + `DiffUtil.ItemCallback`** (eliminando `notifyDataSetChanged()`).
  * Configurar caché de disco en `OkHttpClient` (10 MB) + `HttpLoggingInterceptor` activo solo en `BuildConfig.DEBUG`.
  * Optimizar carga de imágenes en `Glide` (caché automática y recorte eficiente).
* **Manejo de Errores y Logging:**
  * **`PeruViajeroApp.kt` (`Application`):** Inicialización global y registro de `Thread.UncaughtExceptionHandler` que guarda el stacktrace en registro local y redirige a `ErrorFatalActivity` en vez de cerrarse abruptamente.
  * **`AppLogger.kt`:** Logger centralizado (`DEBUG`, `INFO`, `WARN`, `ERROR`) que escribe en Logcat (solo debug) y mantiene un buffer circular persistido en `SharedPreferences`.
  * **Visor de Diagnóstico (`DiagnosticoLogsActivity.kt`):** Pantalla técnica accesible desde Ajustes/Admin para inspeccionar logs recientes, estado de caché de red, token FCM y errores capturados.
  * **Errores HTTP tipados:** Mensajes claros diferenciando sin red, timeout (`SocketTimeoutException`), error de servidor (`5xx`) y formato inválido.
* **Pruebas Unitarias Reales:** Suites en `app/src/test/` probando cálculo de cotización e IGV, filtro de destinos, cálculo de distancia/rumbo GPS (Haversine/bearing) y filtros de publicaciones por región.

---

### F-S16: Despliegue, R8, Firma Digital y APK/AAB Release (Semana 16)
* **Reglas de Ofuscación R8 (`app/src/main/keepRules/rules.keep`):** Reglas `-keep` para modelos Gson (`pe.edu.utp.perviajero.data.model.**`), Retrofit, OkHttp y OSMDroid.
* **Seguridad de Tráfico:** Desactivar `android:usesCleartextTraffic="true"` en `AndroidManifest.xml` (uso estricto de HTTPS).
* **Firma y Artefactos:** Configuración segura con `keystore.properties` (ignorado en `.gitignore`), incremento de `versionCode` y `versionName`, generación y prueba real de **`app-release.apk`** (`assembleRelease`) y **`app-release.aab`** (`bundleRelease`).
* **Documentación de Publicación (`docs/checklist_publicacion_play_store.md`):** Ficha de Play Console, declaración de permisos (`CAMERA`, `ACCESS_FINE_LOCATION`, `POST_NOTIFICATIONS`) y borrador de Política de Privacidad.

---

## 3. Catálogo Completo de Pantallas y Apartados de la App (Nuevos vs. Actualizados)

### 3.1 Pantallas y Vistas Nuevas

| # | Pantalla / Archivo | Tipo | Layout XML | Acceso / Navegación | Qué Hace y Componentes Clave |
| :-: | :--- | :--- | :--- | :--- | :--- |
| **1** | **`ComunidadActivity.kt`** | `Activity` (Feed Social) | `activity_comunidad.xml` + `item_publicacion.xml` | Botón/banner *"Comunidad de Viajeros"* en `DestinosFragment`, botón en `DetalleDestinoActivity` y acceso en Toolbar de `MainActivity` | Red social de turistas. Muestra publicaciones con foto, autor, región, destino, rating, likes y comentarios. Incluye `ChipGroup` de filtro por **Región** (Cusco, Arequipa, Ica, Puno, etc.) + filtro por **Destino**, estado vacío y FAB **"Publicar experiencia"**. |
| **2** | **`PublicarExperienciaActivity.kt`** | `Activity` (Formulario) | `activity_publicar_experiencia.xml` | FAB `+` en `ComunidadActivity` o botón *"Publicar mi visita"* desde `DetalleDestinoActivity` | Formulario para crear una publicación: selector de **Región** y **Destino visitado**, botón **Tomar Foto con Cámara** (`TakePicture` + `FileProvider`) o Galería, vista previa de foto, `RatingBar`, campo de reseña/consejos y botón Publicar. |
| **3** | **`MapaRutaActivity.kt`** | `Activity` (Mapa GPS) | `activity_mapa_ruta.xml` | Botón *"Trazar Ruta en Mapa GPS"* en `DetalleDestinoActivity` y en `RutaDestinoActivity` | Mapa interactivo embebido (`MapView` de OSMDroid). Obtiene ubicación real con GPS (`FusedLocationProviderClient`), coloca pines de Origen y Destino, **traza la polilínea de ruta** (OSRM vial online / geodésica offline), muestra distancia en km, tiempo estimado, botón *"Abrir Brújula Offline"* y botón *"Abrir en Google Maps"*. |
| **4** | **`BrujulaActivity.kt`** | `Activity` (Sensores Offline) | `activity_brujula.xml` (+ `CompassView.kt`) | Botón *"Brújula Offline"* desde `MapaRutaActivity`, `RutaDestinoActivity` y tarjeta en `GuiaViajeFragment` | Brújula 100% offline con `SensorManager` (Acelerómetro + Magnetómetro). Rosa de los vientos rotatoria, grados `0°–360°`, punto cardinal, aguja secundaria de **rumbo hacia el destino (*bearing*)**, selector de destino offline, indicador de precisión magnética y panel técnico colapsable de ejes `X, Y, Z`. |
| **5** | **`DiagnosticoLogsActivity.kt`** | `Activity` (Técnica/Admin) | `activity_diagnostico_logs.xml` | Opción *"Diagnóstico, Logs y Push"* en `AjustesFragment` (sección técnica/admin) | Muestra registros de `AppLogger`, errores capturados, tamaño de caché de red, token FCM para pruebas push, botón *"Simular recordatorio (10s)"* y botón para limpiar/exportar logs. |
| **6** | **`ErrorFatalActivity.kt`** | `Activity` (Resiliencia) | `activity_error_fatal.xml` | Lanzada automáticamente por `UncaughtExceptionHandler` en `PeruViajeroApp` | Pantalla amigable de recuperación ante fallos inesperados: explica que ocurrió un problema, guarda el detalle técnico en `AppLogger` y permite reiniciar la app limpiamente sin pantalla congelada. |

### 3.2 Modales / Diálogos Nuevos

| # | Diálogo / Componente | Ubicación de Lanzamiento | Propósito |
| :-: | :--- | :--- | :--- |
| **D1** | **Diálogo de Comentarios / Detalle de Publicación** | Al tocar una tarjeta o botón de comentarios en `ComunidadActivity` | Permite leer la reseña completa, ampliar la fotografía tomada por el viajero y añadir comentarios locales. |
| **D2** | **Diálogo Educativo de Permisos (*Rationale* y *Ir a Ajustes*)** | `PermissionHelper.kt` (desde Cámara, GPS o Notificaciones) | Explica al turista para qué se usa la cámara o el GPS antes de pedir el permiso, o abre Ajustes del sistema si fue denegado permanentemente. |
| **D3** | **Diálogo Unificado de Voucher (`VoucherDialogHelper.kt`)** | `MisReservasFragment` y `AdminDashboardFragment` | Reemplaza la lógica duplicada actual manteniendo ticket, QR/código, mapa de terminal y compartir por WhatsApp. |

### 3.3 Pantallas y Apartados Existentes que se Mejoran

| # | Pantalla Existente | Mejoras que Recibe (S10 a S16) |
| :-: | :--- | :--- |
| **M1** | **`MainActivity.kt`** | Acceso rápido en Toolbar hacia `ComunidadActivity`, delegación de `POST_NOTIFICATIONS` a `PermissionHelper` y soporte de deep-link desde notificaciones Push/WorkManager. |
| **M2** | **`DestinosFragment.kt`** | Tarjeta/acceso directo a **"Comunidad por Región"**, restauración de `KEY_LAST_SEARCH`, banner de modo offline, manejo tipado de errores HTTP y `ListAdapter` + `DiffUtil`. |
| **M3** | **`DetalleDestinoActivity.kt`** | Dos accesos nuevos directos: **"Trazar Ruta GPS en Mapa"** (abre `MapaRutaActivity` con el destino) y **"Experiencias de Viajeros"** (muestra conteo de reseñas de ese destino y abre `ComunidadActivity` filtrada o `PublicarExperienciaActivity`). |
| **M4** | **`RutaDestinoActivity.kt`** | Añade tarjeta de **Distancia GPS en tiempo real**, botón principal **"Ver Mapa y Ruta Trazada"** (`MapaRutaActivity`) y botón **"Brújula Offline hacia este Destino"** (`BrujulaActivity`). |
| **M5** | **`GuiaViajeFragment.kt`** | Agrega tarjeta de **"Herramientas Offline de Campo: Brújula Turística"** para abrir `BrujulaActivity` sin tener que entrar primero a un destino, y acceso a `ComunidadActivity`. |
| **M6** | **`FavoritosFragment.kt`** | Respaldo con `obtenerDestinosEnCache()` cuando no hay internet para que favoritos funcione offline. |
| **M7** | **`ReservaTourActivity.kt` & `ItinerarioActivity.kt`** | Programación automática de recordatorios locales vía `WorkManager` (`RecordatorioViajeWorker`), corrección de warnings de `ChipGroup` y `DiffUtil` en actividades. |
| **M8** | **`AjustesFragment.kt`** | Foto de perfil editable con **Cámara**, panel de **Estado de Permisos** (Cámara, GPS, Notificaciones), botón de prueba de recordatorio `WorkManager`, estado de token **Push FCM** y acceso a `DiagnosticoLogsActivity`. |

---

## 4. Resumen de Cobertura Curricular (Sustentación)

| Concepto del Sílabo (React Native / Expo) | Implementación en PerúViajero (Android Nativo Kotlin) | Pantalla / Clase Principal |
| :--- | :--- | :--- |
| **Cámara (`expo-camera` / `image-picker`)** | `ActivityResultContracts.TakePicture` + `FileProvider` | `PublicarExperienciaActivity`, `AjustesFragment` |
| **GPS (`expo-location`)** | `FusedLocationProviderClient` + `OSMDroid` + Polilínea | `LocationHelper`, `MapaRutaActivity`, `RutaDestinoActivity` |
| **Sensores (`expo-sensors` Accelerometer/Magnetometer)** | `SensorManager` (`TYPE_ACCELEROMETER` + `TYPE_MAGNETIC_FIELD`) | `BrujulaActivity`, `CompassView` |
| **Permisos (`PermissionsAndroid` / iOS `Info.plist`)** | `ActivityResultContracts.RequestPermission` + *Rationale* + Ajustes | `PermissionHelper`, `AjustesFragment` |
| **Notificaciones Locales y Push (`expo-notifications`)** | `NotificationCompat` + `WorkManager` + `FirebaseMessagingService` | `NotificationHelper`, `RecordatorioViajeWorker`, `PeruViajeroMessagingService` |
| **Rendimiento, Errores y Logging** | `ListAdapter`/`DiffUtil`, `OkHttp Cache`, `AppLogger`, `UncaughtExceptionHandler` | `PeruViajeroApp`, `AppLogger`, `DiagnosticoLogsActivity`, `ErrorFatalActivity` |
| **Despliegue y APK (`Expo build` / Play Store)** | `rules.keep` (R8), `signingConfigs`, `assembleRelease`, `bundleRelease` | `app/build.gradle.kts`, `rules.keep`, `docs/checklist_publicacion_play_store.md` |
