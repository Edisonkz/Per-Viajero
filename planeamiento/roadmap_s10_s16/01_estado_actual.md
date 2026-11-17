# Estado Actual del Proyecto — Cierre Semana 10 (`APF2`)

**Proyecto:** `PerúViajero` (`pe.edu.utp.perviajero`)  
**Rama / Commit auditado:** `main` (`771fec5`)  
**Documento maestro:** [README.md](README.md)

---

## 1. Verificación Técnica del Repositorio

| Aspecto | Estado Verificado | Detalle Observado |
| :--- | :--- | :--- |
| **Compilación Debug** | ✅ Pasa (`BUILD SUCCESSFUL`) | `.\gradlew.bat assembleDebug` compila correctamente configurando `ANDROID_HOME` (el repositorio no incluye `local.properties` al estar en `.gitignore`). |
| **Tests Unitarios** | ⚠️ Solo plantilla | `.\gradlew.bat testDebugUnitTest` pasa, pero `ExampleUnitTest.kt` únicamente ejecuta `assertEquals(4, 2 + 2)`. |
| **Warnings de Compilación** | ⚠️ 2 warnings | `ItinerarioActivity.kt` (líneas `197` y `376`) usa `ChipGroup.setOnCheckedChangeListener` (deprecado). |
| **API REST Pública** | ✅ Activa (HTTP `200`) | `https://raw.githubusercontent.com/Edisonkz/Per-Viajero/main/destinos.json` responde `200 OK` con los 12 destinos definidos en `destinos.json`. |
| **Configuración SDK** | ✅ Moderna | `app/build.gradle.kts`: `compileSdk = 37`, `targetSdk = 37`, `minSdk = 24`, `JavaVersion.VERSION_11`, AGP `9.4.1`, Material 3 (`1.14.0`). |

---

## 2. Inventario de Lo Que Ya Funciona (Semanas 1 a 10)

### 2.1 Navegación y Pantallas (Unidad 1 — APF1)
* **`MainActivity.kt`:** `Toolbar` + `BottomNavigationView` de 5 pestañas (`Destinos`, `Guía Perú`, `Reservas / Agencia`, `Favoritos`, `Perfil`) y redirección inicial a `OnboardingActivity` en primer arranque.
* **Activities secundarias:** `OnboardingActivity`, `DetalleDestinoActivity`, `RutaDestinoActivity`, `ReservaTourActivity`, `ItinerarioActivity`, `LoginActivity`.
* **Intents implícitos:** Apertura de mapa (`geo:lat,lng` con fallback web en `RutaDestinoActivity` y `geo:0,0?q=...` en vouchers), llamada telefónica a IPERÚ (`ACTION_DIAL` en `GuiaViajeFragment`) y compartir texto (`ACTION_SEND`) para vouchers e itinerarios.

### 2.2 Red, Estado Dinámico y Persistencia (Unidad 2 — APF2)
* **Capa REST (`network/`):** `ApiClient.kt` (Retrofit 2 + OkHttp 4 + Gson + interceptor `isOnline()` con `ConnectivityManager`) y `TourismApiService.kt` (`@GET("destinos.json")`).
* **3 Estados UI (`DestinosFragment.kt`):** Manejo de `CARGANDO` (`ProgressBar`), `EXITO` (`RecyclerView`) y `ERROR` (vista de error con botón *Reintentar*), más caché local en `SharedPreferences`.
* **Estado Dinámico sin recrear Activity:**
  * Buscador en tiempo real con `TextWatcher` en `DestinosFragment`.
  * Cotizador multivariable en vivo (fecha, pasajeros 1–10, movilidad, hotel, guía) y pasarela simulada en `ReservaTourActivity`.
  * Planificador con vista por Días (1–7), `CalendarView` y barra de progreso en `ItinerarioActivity`.
* **Persistencia (`PreferencesHelper.kt`):** Almacenamiento en `SharedPreferences` (`peruviajero_prefs`) de sesión/rol (`CLIENTE`/`ADMIN`), favoritos, última búsqueda, caché de destinos, reservas, datos fiscales de empresa, actividades de itinerario, `first_run` y modo oscuro (`AppCompatDelegate`).

### 2.3 Base Parcial Existente de Unidad 3
* **`POST_NOTIFICATIONS` y `NotificationHelper.kt`:** Ya existe creación de canal (`canal_reservas_peruviajero`), solicitud básica de permiso en `MainActivity.onCreate()` para Android 13+ y 2 notificaciones locales inmediatas (confirmación de reserva y cambio de estado desde `AdminDashboardFragment`).

---

## 3. Brechas Frente al Sílabo (Semanas 10 a 16)

| Requisito del Sílabo (Semanas 11–16) | Estado Actual | Qué Falta Implementar |
| :--- | :---: | :--- |
| **S11:** Acceso a la cámara y captura de fotos | ❌ No implementado | Integrar cámara nativa (`TakePicture` / CameraX + `FileProvider`) y persistir fotos dentro de la app. |
| **S12:** Acceso a sensores (GPS, acelerómetro) | ❌ No implementado | `RutaDestinoActivity` solo muestra coordenadas estáticas del JSON; no lee GPS real (`FusedLocationProviderClient`) ni usa `SensorManager` (acelerómetro). |
| **S13:** Solicitud de permisos en tiempo de ejecución | ⚠️ Parcial | Solo solicita `POST_NOTIFICATIONS` de forma directa en `MainActivity` sin flujo de *rationale*, denegación permanente ni gestión para `CAMERA` / `LOCATION`. |
| **S14:** Notificaciones Push y Locales | ⚠️ Parcial | Solo hay notificaciones locales inmediatas; faltan notificaciones locales programadas (`WorkManager`) y notificaciones **Push (FCM)**. |
| **S15:** Rendimiento, optimización, errores y logging | ⚠️ Básico | Faltan `DiffUtil`/`ListAdapter`, logging estructurado, manejador global de excepciones, manejo diferenciado de errores HTTP y pruebas unitarias reales. |
| **S16:** Despliegue, Play Store y APK Android | ⚠️ Parcial | `release.optimization.enable = true` está activado, pero `app/src/main/keepRules/rules.keep` solo tiene comentarios (riesgo de fallo de Gson/Retrofit en release), no hay `signingConfigs` ni guía/checklist de Play Store. |

---

## 4. Deuda Técnica y Mejoras Detectadas en el Código Actual

1. **`FavoritosFragment.kt` sin respaldo offline:** `cargarDatos()` llama a la API y en `onFailure` no consulta `PreferencesHelper.obtenerDestinosEnCache()`, mostrando lista vacía sin internet aunque haya caché.
2. **`DestinosFragment.kt` y `KEY_LAST_SEARCH`:** Guarda el término buscado en `SharedPreferences`, pero no lo restaura en `etBuscar` al abrir nuevamente el fragmento.
3. **Comportamiento del Estado de Error en `DestinosFragment.kt`:** `manejarFalloDeConexion()` prioriza la caché local antes que `EstadoUi.ERROR`, por lo que en sustentación solo se ve la pantalla de error si la app nunca cargó el catálogo o se limpian datos. Conviene mejorar el feedback visual (banner offline + opción explícita de recarga/error si no hay datos).
4. **Duplicación en Vouchers:** `mostrarDetalleVoucher()` está duplicado casi de forma idéntica en `MisReservasFragment.kt` y `AdminDashboardFragment.kt`; conviene extraer un helper común (`VoucherDialogHelper`).
5. **Autenticación local (`LoginActivity.kt`):** Permite ingresar cualquier email/contraseña sin validar registros previos ni persistir una lista de usuarios registrados.
6. **Deprecaciones y limpieza de Manifest:**
   * Reemplazar `setOnCheckedChangeListener` deprecado en `ItinerarioActivity.kt` por `setOnCheckedStateChangeListener`.
   * Revisar `android:usesCleartextTraffic="true"` en `AndroidManifest.xml` (la API pública ya usa HTTPS).
7. **Desfase menor en documentación:**
   * `README.md` raíz aún menciona `MockData.kt` (ya eliminado) y `GET /v1/destinos` (el endpoint real es `GET destinos.json`).
   * `planeamiento/04_plan_desarrollo_sdd_tareas.md` cita archivos `.java` cuando todo el proyecto es Kotlin (`.kt`).
   * `planeamiento/README.md` contiene rutas locales absolutas antiguas (`Proyecto APP/planeamiento/...`).
