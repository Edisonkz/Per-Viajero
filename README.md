# PerúViajero — Guía Turística Nacional
**Curso:** Desarrollo de Aplicaciones Móviles  
**Avance:** Proyecto Final 2 (APF2) — Semana 10 (Continuidad APF1 Semana 5)  
**Institución:** Universidad Tecnológica del Perú (UTP)  

---

## 1. Datos Generales del Proyecto

| Campo | Detalle |
| :--- | :--- |
| **Nombre de la aplicación** | PerúViajero |
| **Integrantes del grupo** | *(Completar con los integrantes)* |
| **Package name** | `pe.edu.utp.peruviajero` |
| **API pública consumida** | API REST de Destinos Turísticos del Perú |
| **Endpoint(s) utilizados** | `GET /destinos` (Lista de 12 destinos con fotos HD, rating y categorías) |

---

## 2. Equivalencias que Demuestra este Avance (Sílabo Oficial)

| Concepto (React Native — Sílabo) | Implementación Nativa Exigida (Android) | Implementación en PerúViajero |
| :--- | :--- | :--- |
| **Hooks `useState` / `useEffect`** | Variables de instancia + refresco manual de UI, o ciclo de vida (`onCreate`, `onResume`) | `DestinosFragment` gestiona la lista en memoria y actualiza el `RecyclerView` mediante `DestinoAdapter.filter()` en tiempo real. |
| **`fetch` / `axios`** | Consumo de API REST pública usando Retrofit, OkHttp o HttpURLConnection | **Retrofit 2** con **GsonConverterFactory** y llamadas asíncronas `enqueue()`. |
| **`AsyncStorage`** | Persistencia local simple con `SharedPreferences` | `PreferencesHelper` almacena el conjunto de IDs de destinos favoritos (`Set<String>`) y preferencias de usuario. |
| **Integración de temas** | `DayNight` theme de Material Components o selector manual guardado en `SharedPreferences` | Switch de Modo Oscuro en `AjustesFragment` con `AppCompatDelegate.setDefaultNightMode()` persistido en `SharedPreferences`. |

---

## 3. Requisitos Funcionales Cumplidos

### Continuidad con APF1 (Semana 5)
- [x] **Navegación fluida:** Menú inferior `BottomNavigationView` con fragments:
  1. **Destinos:** Catálogo completo desde la API.
  2. **Favoritos:** Filtro local de destinos guardados.
  3. **Ajustes:** Configuración del tema y datos de la app.
- [x] **Navegación hacia detalle:** Apertura de `DetalleDestinoActivity` vía `Intent` explícito con `Bundle`.

### Novedades de APF2 (Semana 10)
- [x] **Consumo de API REST pública en RecyclerView:** Catálogo de 12 destinos con imágenes cargadas asíncronamente vía Glide.
- [x] **Manejo visible de los 3 estados:**
  - ⏳ **Cargando:** `ProgressBar` centrado mientras consulta la red.
  - ✅ **Éxito:** `RecyclerView` visible con todas las tarjetas.
  - ❌ **Error:** Vista con mensaje de error y botón **"Reintentar"** (simulable desactivando internet en el emulador).
- [x] **Estado dinámico en pantalla:** Buscador en tiempo real que filtra por nombre y departamento sin recrear la pantalla.
- [x] **Persistencia con SharedPreferences:** Favoritos y modo oscuro que sobreviven al reinicio de la aplicación.
- [x] **Tema Claro / Oscuro (DayNight):** Soporte visual completo para ambos temas.

---

## 4. Estructura del Código Fuente

```text
pe.edu.utp.peruviajero/
├── ui/
│   ├── MainActivity.kt / .java
│   ├── DestinosFragment.kt / .java
│   ├── FavoritosFragment.kt / .java
│   ├── AjustesFragment.kt / .java
│   └── DetalleDestinoActivity.kt / .java
├── data/
│   ├── model/
│   │   └── Destino.kt / .java
│   └── local/
│       └── PreferencesHelper.kt / .java
├── network/
│   ├── ApiClient.kt / .java
│   └── TourismApiService.kt / .java
└── adapter/
    └── DestinoAdapter.kt / .java
```

---

## 5. Preguntas de Reflexión para la Sustentación

1. **¿Qué parte de este avance hubiera sido más directa con fetch/axios y useState en React Native, y por qué en Android nativo toma más líneas de código?**  
   *Respuesta:* En React Native, un hook `useState` y una llamada `fetch` dentro de `useEffect` actualizan reactivamente el componente con pocas líneas. En Android nativo, se requiere orquestar permisos en el `AndroidManifest`, configurar interfaces de Retrofit, POJOs, un `RecyclerView.Adapter` con patrón `ViewHolder` y gestionar los hilos mediante callbacks (`enqueue`) entre el background thread y el UI thread.

2. **¿Qué diferencia encontraron entre guardar datos con SharedPreferences y lo que AsyncStorage haría en React Native?**  
   *Respuesta:* `SharedPreferences` maneja tipos primitivos nativos (`boolean`, `int`, `float`, `String`, `Set<String>`) con operaciones síncronas/asíncronas (`commit`/`apply`). En cambio, `AsyncStorage` en React Native opera asíncronamente con promesas y solo soporta cadenas de texto (`String`), obligando a serializar y deserializar con `JSON.stringify` y `JSON.parse`.

