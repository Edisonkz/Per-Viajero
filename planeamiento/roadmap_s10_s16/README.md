# Plan de Mejora e Implementación — Semanas 10 a 16 (`PerúViajero`)

**Curso:** Desarrollo de Aplicaciones Móviles (`100000ST91` — 2026 Ciclo 2 Agosto, UTP)  
**Proyecto:** `PerúViajero` (`pe.edu.utp.perviajero`)  
**Alcance del Roadmap:** Cierre Unidad 2 (Semana 10 / `APF2`) → Unidad 3 completa (Semanas 11–15 / `APF3`) → Inicio Unidad 4 (Semana 16 / Despliegue APK)

---

## 1. Propósito de esta Carpeta

Este directorio define la hoja de ruta técnica para evolucionar **PerúViajero** desde su estado actual de **Avance de Proyecto Final 2 (APF2 — Semana 10)** hacia una plataforma turística completa que integra funcionalidades de alto valor real —**Comunidad Social de Viajeros por Región/Destino**, **Mapa Interactivo con Trazado de Ruta GPS** y **Brújula Turística Offline**— cubriendo toda la **Unidad 3 (APF3 — Semana 15)** y el **Despliegue APK Release (Unidad 4 — Semana 16)** del sílabo oficial.

> **Nota de enfoque tecnológico:** El sílabo plantea los conceptos usando ejemplos de React Native / Expo, mientras que **PerúViajero** está implementado en **Android Nativo con Kotlin**. Cada módulo usa la API nativa correspondiente de Android y documenta su equivalencia para la sustentación.

---

## 2. Documentos del Plan

| Archivo | Propósito | Contenido Principal |
| :--- | :--- | :--- |
| **[01_estado_actual.md](01_estado_actual.md)** | **Estado Actual del Proyecto** | Diagnóstico técnico real al cierre de la Semana 10 (compilación, tests, endpoint REST, módulos existentes, brechas frente al sílabo y deuda técnica). |
| **[02_funcionalidades_a_implementar.md](02_funcionalidades_a_implementar.md)** | **Funcionalidades y Pantallas a Implementar** | Especificación completa de mejoras de S10 y nuevos apartados de S11–S16 (**Comunidad Social de Viajeros**, **Mapa GPS con Trazado de Ruta**, **Brújula Offline**, **Permisos Runtime**, **Push/Locales**, **Optimización/Logging** y **Release APK**), incluyendo el **catálogo detallado de pantallas nuevas y apartados modificados**. |
| **[03_plan_implementacion.md](03_plan_implementacion.md)** | **Plan de Implementación** | Cronograma semana a semana (Semanas 10 a 16), tareas técnicas, librerías Gradle, permisos de `AndroidManifest.xml`, riesgos y criterios de aceptación (*Definition of Done*). |

---

## 3. Mapa General de Evolución (Semanas 10 a 16)

| Semana | Unidad / Evaluación | Tema del Sílabo | Implementación en PerúViajero |
| :---: | :--- | :--- | :--- |
| **10** | Unidad 2 • **`APF2` (20%)** | Integración de hooks/estado, REST API, almacenamiento local y temas | Consolidación APF2: caché offline en `FavoritosFragment`, restauración de búsqueda, refactor de voucher (`VoucherDialogHelper`), corrección de deprecaciones y sincronización de documentación. |
| **11** | Unidad 3 | Acceso a la cámara del dispositivo y captura de fotos | **Red Social / Comunidad de Viajeros (`ComunidadActivity` + `PublicarExperienciaActivity`):** los turistas publican reseñas con fotos reales de Cámara o galería vinculadas a la **Región** y **Destino** visitado, además de foto de perfil en Ajustes. |
| **12** | Unidad 3 | Acceso a sensores del dispositivo (GPS, acelerómetro, etc.) | **1) Mapa Interactivo con Trazado de Ruta GPS (`MapaRutaActivity` + `RutaDestinoActivity`)** desde la ubicación real del usuario hasta el destino (`OSMDroid` + polilínea de ruta).<br>**2) Brújula Turística Offline (`BrujulaActivity`)** con `SensorManager` (Acelerómetro + Magnetómetro) mostrando Norte magnético, grados y **rumbo (*bearing*) directo al destino** sin internet. |
| **13** | Unidad 3 | Solicitud de permisos en tiempo de ejecución (Android / iOS) | **`PermissionHelper` centralizado** con flujo completo (*concedido*, *rationale explicativo*, *denegado permanente → Ajustes*) para Cámara, Ubicación GPS y Notificaciones, más panel de estado en Ajustes. |
| **14** | Unidad 3 | Manejo de notificaciones push y notificaciones locales | **Notificaciones locales programadas** con `WorkManager` (recordatorios de salida de tours e itinerario) + **Notificaciones Push con Firebase Cloud Messaging (FCM)** para alertas y promociones regionales. |
| **15** | Unidad 3 • **`APF3` (30%)** | Rendimiento, optimización, manejo de errores y logging | `DiffUtil`/`ListAdapter` en adaptadores, `AppLogger` + `DiagnosticoLogsActivity`, `UncaughtExceptionHandler` + `ErrorFatalActivity`, caché HTTP en OkHttp, manejo tipado de errores y **pruebas unitarias reales**. |
| **16** | Unidad 4 • *(Base `PROY` S18)* | Proceso de publicación en Play Store y generación de APK Android | Reglas R8/ProGuard para Gson/Retrofit/OSMDroid en `rules.keep`, firma digital (`signingConfigs` + keystore), generación de **APK Release** y **AAB**, política de privacidad y checklist de Google Play Console. |
