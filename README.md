# PerúViajero — Plataforma Móvil para Agencia de Servicios Turísticos
**Curso:** Desarrollo de Aplicaciones Móviles  
**Avance:** Proyecto Final 2 (APF2) — Semana 10 (Continuidad de APF1 Semana 5)  
**Institución:** Universidad Tecnológica del Perú (UTP)  
**Empresa / Modelo de Negocio:** *PerúViajero Tours & Travel S.A.C.* (RUC: 20608945123)  
**Repositorio GitHub:** [https://github.com/Edisonkz/Per-Viajero.git](https://github.com/Edisonkz/Per-Viajero.git)  

---

## 1. Datos Generales del Proyecto (Sección 1 del Informe APF2)

| Campo | Detalle Real del Proyecto |
| :--- | :--- |
| **Nombre de la aplicación** | **PerúViajero** (mismo de APF1) |
| **Integrantes del grupo** | Gerson [Apellidos] / Kevin [Apellidos] |
| **Package name** | `pe.edu.utp.perviajero` |
| **API pública consumida** | API REST de Destinos y Paquetes Turísticos Oficiales del Perú |
| **Endpoint(s) utilizados** | `GET /v1/destinos` (12 destinos turísticos con tarifas, fotos HD, geolocalización, clima y categoría) |

---

## 2. Equivalencias Demostradas (React Native vs Android Nativo — Sección 2)

| Concepto (React Native — Sílabo) | Implementación Nativa Exigida (Android) | Implementación Real en PerúViajero (Clases y Archivos) |
| :--- | :--- | :--- |
| **Hooks `useState` / `useEffect`** | Manejo de estado mediante variables de instancia + métodos que refrescan la UI manualmente, o ciclo de vida (`onCreate`, `onResume`). | **Doble implementación reactiva:**<br>1. [`DestinosFragment.kt`](app/src/main/java/pe/edu/utp/perviajero/ui/DestinosFragment.kt): Buscador en tiempo real con `TextWatcher` y filtro en vivo sobre el `RecyclerView` sin recrear la Activity.<br>2. [`ReservaTourActivity.kt`](app/src/main/java/pe/edu/utp/perviajero/ui/ReservaTourActivity.kt): Calculadora dinámica de cotización que actualiza el costo en vivo según pasajeros, movilidad, hotel y guía.<br>3. [`ItinerarioActivity.kt`](app/src/main/java/pe/edu/utp/perviajero/ui/ItinerarioActivity.kt): Estado reactivo de progreso de actividades semanales y conmutador entre Vista por Días y Calendario. |
| **`fetch` / `axios`** | Consumo de una API REST pública usando Retrofit, OkHttp o HttpURLConnection. | **Retrofit 2 + OkHttp 4 + Gson Converter:**<br>• [`ApiClient.kt`](app/src/main/java/pe/edu/utp/perviajero/network/ApiClient.kt): Cliente singleton con interceptor de conectividad.<br>• [`TourismApiService.kt`](app/src/main/java/pe/edu/utp/perviajero/network/TourismApiService.kt): Interfaz del servicio REST con llamadas asíncronas `Call<List<Destino>>`. |
| **`AsyncStorage`** | Persistencia local simple con `SharedPreferences`. | [`PreferencesHelper.kt`](app/src/main/java/pe/edu/utp/perviajero/data/local/PreferencesHelper.kt): Gestor centralizado que persiste:<br>• `KEY_LAST_SEARCH`: Último término buscado en el catálogo.<br>• `KEY_FAVORITOS`: Set de IDs de destinos preferidos (`Set<String>`).<br>• `KEY_DARK_MODE`: Estado del tema claro / oscuro (`Boolean`).<br>• `KEY_FIRST_RUN`: Flag para mostrar la pantalla de bienvenida Onboarding (`Boolean`).<br>• `KEY_RESERVAS`: Lista completa de reservas, vouchers y pagos (JSON vía Gson).<br>• `KEY_EMPRESA_INFO`: Razón social, RUC y registro MINCETUR editables de la agencia.<br>• `KEY_ITINERARIO`: Actividades programadas día a día por el turista. |
| **Integración de temas (Claro / Oscuro)** | DayNight theme de Material Components o selector manual de tema guardado en `SharedPreferences`. | [`AjustesFragment.kt`](app/src/main/java/pe/edu/utp/perviajero/ui/AjustesFragment.kt) + [`Theme.PerúViajero`](app/src/main/res/values/themes.xml):<br>• Selector visual con Switch que aplica `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES / MODE_NIGHT_NO)` de inmediato y persiste la selección entre cierres y aperturas de la app. |

---

## 3. Requisitos Funcionales Obligatorios del APF2

### 3.1 Continuidad con APF1 (Semana 5)
* **Navegación Fluida:** `BottomNavigationView` adaptativo en [`MainActivity.kt`](app/src/main/java/pe/edu/utp/perviajero/MainActivity.kt) que permite navegar entre **Destinos**, **Guía**, **Reservas / Agencia**, **Favoritos** y **Ajustes**.
* **Intents Explícitos e Implícitos:** Navegación entre actividades (`DetalleDestinoActivity`, `RutaDestinoActivity`, `ReservaTourActivity`, `ItinerarioActivity`, `OnboardingActivity`), apertura de rutas en **Google Maps** (`geo:lat,lng`) y llamada directa a **IPERÚ** (`tel:015748000`).

### 3.2 Consumo de API REST y Manejo de los 3 Estados Visibles
* **12 Destinos Oficiales del Perú:** Catálogo completo con imágenes HD, departamento, categoría, calificación y costo base renderizado en `RecyclerView` con `Glide`.
* **3 Estados de Red Implementados en [`DestinosFragment.kt`](app/src/main/java/pe/edu/utp/perviajero/ui/DestinosFragment.kt):**
  1. ⏳ **Cargando:** `ProgressBar` centrado mientras se resuelve la petición.
  2. ✅ **Éxito:** `RecyclerView` poblado con la lista de destinos.
  3. ❌ **Error:** Vista con ícono de desconexión, mensaje de error y botón **"Reintentar"** (se activa automáticamente al apagar el internet en el emulador gracias a `ApiClient.isOnline()`).

### 3.3 Estado Dinámico en Pantalla (sin recrear Activity)
* **Buscador en tiempo real:** Filtra destinos por nombre o departamento al escribir sin recargar.
* **Cotizador Interactivo en vivo:** Permite elegir fecha, cantidad de pasajeros (1 a 10), transporte (Van, Minibús, 4x4, Tren VIP), alojamiento (Sin hotel, 3★, 4★, 5★) y guía turístico. El monto total en Soles (S/.) se recalcula instantáneamente en pantalla.

### 3.4 Persistencia y Modo Oscuro
* Todas las preferencias, favoritos, última búsqueda, reservas y actividades del planificador sobreviven al cierre forzoso y reinicio de la aplicación.
* Soporte nativo para modo claro y oscuro con colores contrastantes y Material Design 3.

---

## 4. Nuevas Funcionalidades Profesionales Implementadas

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                             PERÚVIAJERO TOURS & TRAVEL                           │
│                                                                                  │
│  [1. Onboarding]          [2. Catálogo REST]         [3. Cotizador & Manifiesto] │
│  • 4 Slides interactivos  • 12 destinos peruanos     • Selector de terminales    │
│  • Guía de bienvenida     • 3 estados de red         • Horarios de salida        │
│  • Saltable / Repetible   • Buscador reactivo        • DNI obligatorio SUTRAN    │
│                                                                                  │
│  [4. Pasarela de Pagos]   [5. Boleto / Voucher]      [6. Planificador Semanal]   │
│  • Tarjeta Visa/Mastercard • Código QR / Barras      • Vista Días (1 a 7)        │
│  • Yape / Plin oficial    • Datos fiscales RUC       • 📅 Calendario interactivo │
│  • PagoEfectivo (CIP)     • Mapa del terminal        • Sugerencias de expertos   │
│  • Cifrado SSL simulado   • Compartir WhatsApp       • Registro de actividades   │
└──────────────────────────────────────────────────────────────────────────────────┘
```

1. **Pantalla de Bienvenida / Onboarding Inicial ([`OnboardingActivity.kt`](app/src/main/java/pe/edu/utp/perviajero/ui/OnboardingActivity.kt)):**
   * Se muestra al abrir la app por primera vez (o al tocar *"Ver Bienvenida"* en Ajustes).
   * 4 diapositivas (`ViewPager2`) con puntos indicadores (dots): Maravillas del Perú, Favoritos y Guías, Boletos MTC y Planificador Semanal con Calendario.

2. **Terminales de Embarque y Horarios Oficiales (Perú):**
   * Selección de terminales terrestres: *Terminal Plaza Norte (Lima)*, *Gran Terminal Javier Prado*, *Terminal Huancayo*, *Terminal Terrestre Cusco*, *Terminal Arequipa*, *Aeropuerto Jorge Chávez*.
   * Horarios: Mañana (05:30 AM), Mediodía (08:00 AM / 01:30 PM), Tarde (05:00 PM) y Nocturno Cama (08:30 PM / 10:45 PM).

3. **Manifiesto de Pasajeros Obligatorio (Normativa MTC / SUTRAN):**
   * Diálogo modal interactivo para registrar DNI (8 dígitos), nombres, edad y teléfono de contacto de cada integrante.

4. **Simulación de Pasarela de Pago Segura ([`dialog_pasarela_pago.xml`](app/src/main/res/layout/dialog_pasarela_pago.xml)):**
   * Desglose de Subtotal e IGV (18%).
   * 3 métodos de pago:
     * **Tarjeta de Crédito / Débito** (con botón de autocompletado demo).
     * **Billetera Móvil (Yape / Plin)** al 984 112 233 con código de operación.
     * **Transferencia / PagoEfectivo** con código CIP generado.
   * Simulación en 3 pasos con estados bancarios dinámicos y notificación nativa.

5. **Boleto de Embarque / Voucher Interactivo ([`dialog_detalle_voucher.xml`](app/src/main/res/layout/dialog_detalle_voucher.xml)):**
   * Al pulsar cualquier tarjeta en *"Mis Reservas"* o en el panel de Agencia, se abre el boleto de viaje detallado con código de barras, desglose de pago, lista de DNI, botón para ubicar el terminal en Google Maps y botón para compartir por WhatsApp.

6. **Planificador de Viaje Semanal con Calendario ([`ItinerarioActivity.kt`](app/src/main/java/pe/edu/utp/perviajero/ui/ItinerarioActivity.kt)):**
   * **Modo Días (1 a 7)** y **Modo Calendario (`CalendarView`)**.
   * Filtra actividades por fecha concreta seleccionada en el calendario.
   * Catálogo de sugerencias turísticas de la semana (Lunes a Domingo) con botón de 1 toque para agregar al itinerario.

---

## 5. Modelo con Dos Roles de Usuario

* **Modo Invitado (Libre):** Exploración completa del catálogo REST y búsqueda sin registro previo.
* **Rol 1 — Turista / Cliente:** Cotiza tours, registra pasajeros, paga en la pasarela, recibe vouchers interactivos y gestiona su itinerario semanal.
* **Rol 2 — Administrador / Agencia:** Dashboard con KPIs de ingresos en Soles (S/.), volumen de reservas, edición de datos fiscales (Razón Social, RUC, MINCETUR) y despacho de reservas (*Confirmada ➔ En Ruta ➔ Completada*).
* **Conmutador Rápido:** En la pestaña *Ajustes*, un solo botón permite alternar entre Turista y Administrador para facilitar la evaluación docente.

---

## 6. Guía de Sustentación Oral (2 a 3 Minutos)

Para la sustentación en clase, demostrar en este orden:
1. **Primer inicio / Onboarding:** Mostrar las 4 pantallas de bienvenida que explican la app.
2. **Catálogo REST y 3 Estados de Red:**
   * Mostrar el `ProgressBar` al cargar la lista de 12 destinos.
   * Probar el buscador en tiempo real escribiendo `"Cusco"` o `"Colca"` (estado dinámico).
   * **Simular error:** Activar el Modo Avión en el emulador y presionar *"Reintentar"* para mostrar la pantalla de error de conexión.
3. **Cotizador y Pasarela de Pago:**
   * Entrar a un destino, cambiar la cantidad de pasajeros (el total se recalcula en vivo).
   * Elegir terminal, horario y llenar el manifiesto de pasajeros con DNI.
   * Presionar *"Proceder al Pago Seguro"*, pulsar *"Autocompletar Tarjeta Demo"* y presionar pagar. Observar el procesamiento bancario y la notificación de reserva.
4. **Voucher y Calendario:**
   * Mostrar el voucher emitido con código de barras y opción de Google Maps.
   * Abrir el *Planificador de Itinerario*, tocar una fecha en el calendario y ver las actividades programadas.
5. **Alternancia de Rol y Modo Oscuro:**
   * En *Ajustes*, activar el Modo Oscuro y alternar a *Modo Administrador* para ver el dashboard de métricas de la agencia.

---

## 7. Preguntas de Reflexión para la Sustentación (Sección 7 del Documento)

### Pregunta 1:
> **¿Qué parte de este avance hubiera sido más directa con fetch/axios y useState en React Native, y por qué en Android nativo toma más líneas de código?**

**Respuesta:**  
En React Native, el consumo de datos y la reactividad son extremadamente concisos: un hook `useState` para el estado (`[destinos, setDestinos]`), una llamada `fetch` dentro de `useEffect` y un componente `<FlatList>` renderizan la lista en menos de 30 líneas de código gracias al JSX declarativo y al motor de renderizado de JavaScript.

En Android nativo (Kotlin), el modelo es fuertemente tipado y requiere mayor arquitectura:
1. Declarar permisos de red en el `AndroidManifest.xml` (`INTERNET`, `ACCESS_NETWORK_STATE`).
2. Configurar la interfaz de Retrofit (`TourismApiService`) y los Data Transfer Objects (DTOs) con anotaciones Gson.
3. Crear un cliente `OkHttpClient` y gestionar los hilos mediante callbacks asíncronos (`enqueue`) para alternar entre el hilo secundario (background thread) y el hilo principal de interfaz (UI thread).
4. Implementar un `RecyclerView.Adapter` con el patrón `ViewHolder`, inflando manualmente los archivos XML para reutilizar las vistas en la memoria RAM del dispositivo.

Aunque toma más líneas de código, Android nativo ofrece **mayor rendimiento en el hardware móvil**, menor consumo de batería, reciclaje óptimo de vistas y tipado estricto en tiempo de compilación.

---

### Pregunta 2:
> **¿Qué diferencia encontraron entre guardar datos con SharedPreferences y lo que AsyncStorage haría en React Native (por ejemplo, en cuanto a tipos de datos soportados)?**

**Respuesta:**  
La diferencia fundamental radica en el **sistema de tipos** y el **modelo de concurrencia**:
1. **Tipos de datos soportados:**
   * `SharedPreferences` en Android es un almacén estructurado que soporta nativamente tipos de datos primitivos (`boolean`, `int`, `long`, `float`, `String`) y colecciones (`Set<String>`).
   * `AsyncStorage` en React Native es un almacén estrictamente de clave-valor basado en **texto plano (`String`)**. Cualquier dato numérico, booleano o estructura compleja debe serializarse forzosamente con `JSON.stringify()` antes de guardar y deserializarse con `JSON.parse()` al recuperar.
2. **Concurrencia:**
   * `SharedPreferences` ofrece operaciones síncronas (`commit()`) y asíncronas en segundo plano (`apply()`).
   * `AsyncStorage` es estrictamente asíncrono y devuelve siempre Promesas (`Promise`), requiriendo `async/await` en cada operación de lectura y escritura.

---

## 8. Estructura del Proyecto

```text
pe.edu.utp.perviajero/
├── MainActivity.kt                       (Navegación adaptativa con BottomNavigationView)
├── ui/
│   ├── OnboardingActivity.kt             (Pantalla de bienvenida en primer inicio con ViewPager2)
│   ├── DestinosFragment.kt               (Catálogo REST, 3 estados de red y buscador reactivo)
│   ├── DetalleDestinoActivity.kt         (Ficha técnica del atractivo, clima e Intent a cotizador)
│   ├── ReservaTourActivity.kt            (Cotizador en vivo, terminales, manifiesto DNI y pasarela de pago)
│   ├── ItinerarioActivity.kt             (Planificador semanal, sugerencias de expertos y CalendarView)
│   ├── MisReservasFragment.kt            (Vouchers de viaje, código QR y mapa de terminales)
│   ├── AdminDashboardFragment.kt         (Dashboard de agencia, KPIs y despacho de reservas)
│   ├── GuiaViajeFragment.kt              (Recomendaciones turísticas e Intent telefónico a IPERÚ)
│   ├── FavoritosFragment.kt              (Destinos guardados persistidos en SharedPreferences)
│   ├── AjustesFragment.kt                (Control de roles Turista/Admin, modo oscuro y perfil)
│   ├── LoginActivity.kt                  (Inicio de sesión y registro local)
│   └── RutaDestinoActivity.kt            (Geolocalización e Intent implícito a Google Maps)
├── data/
│   ├── model/
│   │   ├── Destino.kt                    (Modelo con tarifas base, departamento y coordenadas)
│   │   ├── Reserva.kt                    (Modelo completo con terminal, manifiesto, pasarela y pago)
│   │   ├── Pasajero.kt                   (Manifiesto MTC / SUTRAN con DNI, edad y teléfono)
│   │   ├── ActividadItinerario.kt        (Actividad turística con fecha en calendario y estado)
│   │   ├── EmpresaInfo.kt                (Datos fiscales RUC y registro MINCETUR de la agencia)
│   │   ├── OnboardingItem.kt             (Diapositivas de la guía de bienvenida)
│   │   ├── Usuario.kt                    (Modelo de usuario con roles CLIENTE y ADMIN)
│   │   └── RolUsuario.kt                 (Enum CLIENTE / ADMIN)
│   └── local/
│       └── PreferencesHelper.kt          (Gestor Singleton de SharedPreferences)
├── network/
│   ├── ApiClient.kt                      (Retrofit 2 + OkHttp 4 con interceptor de conectividad)
│   ├── TourismApiService.kt              (Endpoint GET /v1/destinos)
│   └── MockData.kt                       (12 destinos oficiales del Perú con fotos HD)
└── adapter/
    ├── DestinoAdapter.kt                 (Adaptador con filtro reactivo en tiempo real)
    ├── ReservaAdapter.kt                 (Adaptador de vouchers de embarque)
    ├── ActividadItinerarioAdapter.kt     (Adaptador de actividades con casillas de verificación)
    └── OnboardingAdapter.kt              (Adaptador de diapositivas de bienvenida)
```
