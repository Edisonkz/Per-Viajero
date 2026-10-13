# PerúViajero — Plataforma Móvil para Agencia de Servicios Turísticos
**Curso:** Desarrollo de Aplicaciones Móviles  
**Avance:** Proyecto Final 2 (APF2) — Semana 10 (Continuidad APF1 Semana 5)  
**Institución:** Universidad Tecnológica del Perú (UTP)  
**Empresa / Modelo de Negocio:** *PerúViajero Tours & Travel S.A.C.*  
**Repositorio GitHub:** [https://github.com/Edisonkz/Per-Viajero.git](https://github.com/Edisonkz/Per-Viajero.git)  

---

## 1. Datos Generales del Proyecto

| Campo | Detalle |
| :--- | :--- |
| **Nombre de la aplicación** | PerúViajero (Agencia de Viajes & Operador Turístico) |
| **Integrantes del grupo** | Kevin [Completar] / Gerson [Completar] |
| **Package name** | `pe.edu.utp.perviajero` |
| **API pública consumida** | API REST de Destinos y Paquetes Turísticos del Perú |
| **Endpoint(s) utilizados** | `GET /v1/destinos.json` (12 destinos oficiales con tarifas base, fotos HD y geolocalización) |

---

## 2. Visión de Negocio y Modelo con Dos Roles

La aplicación implementa un modelo de **Acceso Libre Inicial (Modo Invitado)** con escalamiento a **2 Roles de Usuario** diferenciados según la necesidad del cliente o de la empresa:

```
                            ┌───────────────────────────────────────┐
                            │      MODO INVITADO (ACCESO LIBRE)     │
                            │  • Exploración de destinos REST       │
                            │  • Búsqueda en tiempo real            │
                            │  • Guía de viaje e Intent a Maps      │
                            └───────────────────┬───────────────────┘
                                                │
                                    ¿Desea Reservar / Gestionar?
                                                │
                     ┌──────────────────────────┴──────────────────────────┐
                     ▼                                                     ▼
      ┌─────────────────────────────┐                       ┌─────────────────────────────┐
      │   ROL 1: TURISTA / CLIENTE  │                       │   ROL 2: AGENTE / ADMIN     │
      ├─────────────────────────────┤                       ├─────────────────────────────┤
      │ • Cotizador interactivo     │                       │ • Dashboard de métricas     │
      │ • Selección fecha (Calendar)│                       │ • Ingresos totales en S/.   │
      │ • Contador personas (+ / -) │                       │ • Total reservas activas    │
      │ • Escoge Movilidad & Hotel  │                       │ • Destino más solicitado    │
      │ • Total dinámico en vivo    │                       │ • Aprobación de reservas    │
      │ • Vouchers en "Mis Reservas"│                       │ • Cambio de estados cliente │
      └─────────────────────────────┘                       └─────────────────────────────┘
```

### 2.1 Modo Invitado (Guest Mode)
- Cualquier usuario puede abrir la aplicación sin login forzoso.
- Consulta el catálogo de 12 destinos turísticos (Machu Picchu, Colca, Paracas, etc.) consumido vía API REST.
- Utiliza el buscador y visualiza la ficha informativa y rutas.
- Al hacer clic en *"Cotizar y Reservar"* o entrar a *"Mis Reservas"*, la app le ofrece iniciar sesión o continuar como cliente.

### 2.2 Rol 1: Turista / Cliente (Viajero)
- **Perfil de Viajero:** Inicia sesión o se registra con sus datos locales.
- **Cotizador Dinámico Interactivo (Alta Interacción):**
  - **Fecha de Salida:** Selector de fecha nativo (`DatePickerDialog`).
  - **Cantidad de Pasajeros:** Contador interactivo `[ - ] 1 [ + ]` de 1 a 10 personas.
  - **Selección de Movilidad:** Van Turística (+S/ 0), Bus Panorámico (+S/ 25), 4x4 Aventura (+S/ 50) o Tren Ejecutivo (+S/ 90 por pers.).
  - **Selección de Hospedaje:** Solo tour (+S/ 0), Hotel 3★ (+S/ 45), Hotel 4★ (+S/ 95) o Resort 5★ (+S/ 180 por noche/pers.).
  - **Cálculo de Presupuesto en Tiempo Real:** El total en Soles (S/.) se recalcula al instante ante cualquier cambio de los controles sin recargar la pantalla.
- **Generación de Voucher y "Mis Reservas":** Al confirmar la reserva, se crea un código de voucher único (ej. `PV-2026-7841`) guardado en `SharedPreferences` y visible en la pestaña *"Mis Reservas"*.

### 2.3 Rol 2: Administrador / Agente Turístico (Operador de la Agencia)
- **Dashboard de Métricas del Negocio:**
  - 📊 **Total de Reservas:** Cantidad global de contrataciones recibidas.
  - 💰 **Ingresos Totales Acumulados:** Monto en Soles (S/.) facturado por la agencia.
  - 🏆 **Destino Más Popular:** Destino con mayor volumen de reservas.
- **Gestión de Reservas de Clientes:** Listado administrativo con opción para cambiar el estado de las reservas (*"Confirmada"* ➔ *"En Ruta"* ➔ *"Completada"*).
- **Conmutador Rápido de Rol (Para Sustentación):** Selector en Ajustes que permite alternar entre `[ Modo Turista ]` y `[ Modo Administrador ]` con 1 solo toque, ideal para demostrar ambos roles ante el profesor sin perder tiempo.

---

## 3. Tabla de Equivalencias (Sílabo Oficial UTP)

| Concepto (React Native — Sílabo) | Implementación Nativa Exigida (Android) | Implementación en PerúViajero |
| :--- | :--- | :--- |
| **Hooks `useState` / `useEffect`** | Variables de instancia + refresco manual de UI, o ciclo de vida (`onCreate`, `onResume`). | **Doble implementación reactiva:**<br>1. `DestinosFragment.kt`: Filtro de búsqueda en tiempo real sobre el `RecyclerView` mediante `TextWatcher`.<br>2. `ReservaTourActivity.kt`: Calculadora dinámica de cotización que recalcula el total en vivo según pasajeros, movilidad y hotel. |
| **`fetch` / `axios`** | Consumo de API REST pública usando Retrofit, OkHttp o HttpURLConnection. | **Retrofit 2** con **GsonConverterFactory** y llamadas asíncronas `enqueue()` en segundo plano mediante `ApiClient.kt` y `TourismApiService.kt`. |
| **`AsyncStorage`** | Persistencia local simple con `SharedPreferences`. | `PreferencesHelper.kt` gestiona almacenamiento de:<br>• Sesión del usuario y rol activo (Cliente / Admin).<br>• IDs de destinos favoritos (`Set<String>`).<br>• Colección de reservas y vouchers creados (JSON vía Gson).<br>• Preferencia de Modo Oscuro. |
| **Integración de temas** | `DayNight` theme de Material Components o selector manual guardado en `SharedPreferences`. | `AjustesFragment.kt` con switch para conmutar entre Modo Claro y Modo Oscuro mediante `AppCompatDelegate.setDefaultNightMode()` persistido en sesión. |

---

## 4. Requisitos Funcionales y No Funcionales

### Requerimientos Funcionales (RF)
- [x] **RF-01 (Navegación Adaptativa):** Menú `BottomNavigationView` que adapta sus opciones según el rol activo (Turista: Destinos, Mis Reservas, Guía, Ajustes / Admin: Destinos, Panel Admin, Guía, Ajustes).
- [x] **RF-02 (Toolbar Material Design):** Barra superior con título dinámico, ícono adaptativo y badge de rol activo.
- [x] **RF-03 (Navegación a Detalle y Cotización):** Intent explícito con Bundle hacia `DetalleDestinoActivity` y `ReservaTourActivity`.
- [x] **RF-04 (Intents Implícitos del Sistema):**
  - Apertura de navegación GPS en **Google Maps** (`geo:lat,lng`).
  - Marcación telefónica directa a la central de **IPERÚ** (`tel:015748000`).
- [x] **RF-05 (Consumo REST Asíncrono):** Peticiones `GET` en segundo plano mediante Retrofit 2 con deserialización automática.
- [x] **RF-06 (Renderizado en RecyclerView):** Catálogo de 12 destinos con Glide, y lista de vouchers en "Mis Reservas".
- [x] **RF-07 (Tres Estados de Red Visibles):**
  - ⏳ **Cargando:** `ProgressBar` centrado.
  - ✅ **Éxito:** `RecyclerView` visible con tarjetas de tours.
  - ❌ **Error:** Vista de falla con mensaje y botón **"Reintentar"** (simulado apagando internet en el emulador).
- [x] **RF-08 (Estado Dinámico / Cotizador en Vivo):** Buscador reactivo y calculadora interactiva de precios sin recargar pantallas.
- [x] **RF-09 (Persistencia con SharedPreferences):** Sesión, favoritos y reservas persistentes que sobreviven al cierre de la aplicación.
- [x] **RF-10 (Tema Claro / Oscuro):** Soporte visual completo para ambos temas.

---

## 5. Estructura Arquitectónica del Código Fuente

```text
pe.edu.utp.perviajero/
├── ui/
│   ├── MainActivity.kt                 (Navegación adaptativa según rol Turista / Admin)
│   ├── LoginActivity.kt                (Login rápido con 1 tap para demostración y credenciales)
│   ├── DestinosFragment.kt             (Catálogo REST, 3 estados de red y buscador reactivo)
│   ├── GuiaViajeFragment.kt            (Guía para turistas e Intent implícito telefónico a IPERÚ)
│   ├── FavoritosFragment.kt            (Lista de favoritos persistidos en SharedPreferences)
│   ├── MisReservasFragment.kt          (Módulo Turista: historial de vouchers y estado de tours)
│   ├── AdminDashboardFragment.kt       (Módulo Admin: KPIs de ventas y despacho de reservas)
│   ├── AjustesFragment.kt              (Perfil, alternancia rápida de rol y Modo Oscuro)
│   ├── DetalleDestinoActivity.kt       (Vista detallada del tour y llamada al cotizador)
│   ├── ReservaTourActivity.kt          (Cotizador interactivo: fecha, pasajeros, movilidad, hotel y voucher)
│   └── RutaDestinoActivity.kt          (Coordenadas GPS e Intent implícito a Google Maps)
├── data/
│   ├── model/
│   │   ├── Destino.kt                  (Modelo de datos con tarifas base y fotos HD)
│   │   ├── Reserva.kt                  (Modelo de reserva turística con estados de despacho)
│   │   └── Usuario.kt                  (Modelo de usuario con roles CLIENTE y ADMIN)
│   └── local/
│       └── PreferencesHelper.kt        (Gestor Singleton de SharedPreferences, sesiones y reservas)
├── network/
│   ├── ApiClient.kt                    (Cliente Retrofit + OkHttp con interceptor offline/online)
│   ├── TourismApiService.kt            (Interfaz de servicio REST)
│   └── MockData.kt                     (Dataset oficial de 12 destinos turísticos del Perú)
└── adapter/
    ├── DestinoAdapter.kt               (Adaptador del catálogo de tours con filtro en tiempo real)
    └── ReservaAdapter.kt               (Adaptador de vouchers con soporte para clientes y administradores)
```

---

## 6. Sustentación Oral (Preguntas de Reflexión del Docente)

1. **¿Qué parte de este avance hubiera sido más directa con fetch/axios y useState en React Native, y por qué en Android nativo toma más líneas de código?**  
   *Respuesta:* En React Native, un hook `useState` y una llamada `fetch` dentro de `useEffect` actualizan reactivamente el componente con muy pocas líneas. En Android nativo, se requiere estructurar permisos en el `AndroidManifest`, configurar interfaces de Retrofit, POJOs con serialización, un `RecyclerView.Adapter` con patrón `ViewHolder` para reciclar vistas en memoria RAM y gestionar los hilos mediante callbacks asíncronos (`enqueue`) entre el background thread y el UI thread.

2. **¿Qué diferencia encontraron entre guardar datos con SharedPreferences y lo que AsyncStorage haría en React Native?**  
   *Respuesta:* `SharedPreferences` en Android admite nativamente tipos de datos primitivos (`boolean`, `int`, `float`, `String`, `Set<String>`) con operaciones síncronas (`commit`) y asíncronas (`apply`). En cambio, `AsyncStorage` en React Native opera estrictamente de forma asíncrona mediante promesas y únicamente almacena cadenas de texto (`String`), obligando a serializar y deserializar manualmente con `JSON.stringify` y `JSON.parse`.
