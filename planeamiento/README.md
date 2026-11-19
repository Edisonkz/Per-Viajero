# PerúViajero — Planeamiento Estratégico y Metodología SDD

**Proyecto:** PerúViajero (`pe.edu.utp.perviajero`)  
**Curso:** Desarrollo de Aplicaciones Móviles — Universidad Tecnológica del Perú (UTP)  
**Empresa / Modelo de Negocio:** *PerúViajero Tours & Travel S.A.C.* (RUC: `20608945123` • Registro MINCETUR)  
**Integrantes:** Kevin Salazar de Jesús • Edison Edgar Sivipaucar Quispe • Gerson Osmar Machuca Abanto  

---

## 1. Problemática: ¿Por qué nace este proyecto?

El turismo interno y receptivo en el Perú enfrenta brechas operativas y digitales recurrentes que afectan tanto al viajero como a las agencias formales:

1. **Fragmentación de la información turística:** Los viajeros deben consultar múltiples aplicaciones separadas para descubrir destinos, verificar rutas GPS, revisar reseñas reales de otros turistas, organizar su itinerario diario y contactar una agencia formal.
2. **Informalidad en reservas y embarques:** Gran parte de las reservas turísticas locales se coordinan por mensajes informales sin un manifiesto formal de pasajeros con DNI (normativa MTC / SUTRAN), sin desglose tributario transparente (Subtotal + IGV) ni boletos verificables con código QR.
3. **Baja conectividad en zonas andinas y amazónicas:** Muchos atractivos del Perú cuentan con señal móvil intermitente, lo que exige herramientas con respaldo offline (caché local de destinos, brújula por sensores físicos y rutas con fallback geodésico).
4. **Falta de herramientas móviles para gestión de agencias:** Las pequeñas y medianas agencias turísticas carecen de una interfaz móvil administrativa para controlar reservas por destino y estado, actualizar su catálogo de lugares turísticos por ubicación política exacta (Departamento → Provincia → Distrito) y emitir alertas operativas a sus pasajeros.

---

## 2. Objetivos del Proyecto

### Objetivo General
Diseñar y desarrollar **PerúViajero**, una aplicación móvil nativa en Android (Kotlin) que integre exploración turística del Perú, comunidad social multi-foto, navegación GPS/brújula offline, reserva formal con pasarela segura y boleto QR, además de un módulo administrativo completo para agencias de turismo.

### Objetivos Específicos
- **Experiencia de entrada sin fricción (*Try Before You Sign Up*):** Iniciar con una animación de marca (`SplashActivity`) y bienvenida interactiva (`OnboardingActivity`), permitiendo navegar libremente como **Visitante** y solicitando autenticación únicamente en acciones transaccionales o sociales (`AuthGuardHelper`).
- **Consumo de APIs REST con resiliencia offline:** Integrar APIs públicas para el catálogo de destinos, Ubigeo oficial del Perú (25 departamentos, 196 provincias y 1,874 distritos) y trazado de rutas viales (OSRM), con caché local y respaldo en `assets/services/*.json`.
- **Formalización del flujo de reserva turística:** Implementar cotización reactiva en vivo, importación de acompañantes desde contactos (`READ_CONTACTS` + `ContentResolver`), manifiesto de pasajeros con DNI, pasarela de pago SSL (Tarjeta, Yape/Plin, CIP) y emisión de Boarding Pass con código QR.
- **Gestión administrativa diferenciada (RBAC):** Proveer un entorno visual exclusivo para el rol **Administrador** (*Andean Slate* `#1B3B4B`) con KPIs en vivo, filtro de reservas por destino/estado, CRUD de lugares turísticos con selector en cascada de Ubigeo, moderación de Comunidad y emisión de alertas Push.
- **Cumplimiento técnico integral (Semanas 1–16):** Garantizar arquitectura desacoplada (`Repository` + `ServiceLocator`), gestión de permisos en 4 estados, notificaciones agrupadas con 4 canales y `TaskStackBuilder`, pruebas unitarias JVM y compilación Release optimizada con R8.

---

## 3. Visión y Misión

### Visión
Consolidarse como la plataforma móvil referente del turismo formal en el Perú, conectando de manera transparente, segura e inteligente a millones de viajeros con las maravillas naturales, culturales y gastronómicas de las 25 regiones del país.

### Misión
Brindar a turistas nacionales, extranjeros y agencias acreditadas por MINCETUR una herramienta móvil intuitiva, confiable y resiliente que simplifique cada etapa del viaje —desde el descubrimiento y la planificación hasta la reserva certificada con boleto digital y la experiencia compartida en comunidad— impulsando el turismo formal y sostenible en todo el Perú.

---

## 4. Roles del Aplicativo y Control de Acceso (RBAC)

La aplicación implementa **3 niveles de acceso** con identidad visual diferenciada:

| Rol | Paleta Visual | Alcance y Capacidades Principales |
| :--- | :--- | :--- |
| **Visitante (`INVITADO`)** | Esmeralda Andino (`#006837`) | Rol por defecto al iniciar la app. Navegación libre en **Explorar** (filtros por categoría, departamento y provincia), **Detalle Hero**, **Mapa GPS**, **Brújula Offline**, **Guía IPERÚ**, lectura de **Comunidad** (ver fotos y comentarios) y **Bandeja de Avisos públicos**. Al intentar reservar, comentar, publicar o editar su itinerario, `AuthGuardHelper` solicita iniciar sesión en contexto. |
| **Turista Registrado (`CLIENTE`)** | Esmeralda Andino (`#006837`) | Todo lo del Visitante más: cotizar y reservar tours, importar acompañantes desde Contactos, pagar en la pasarela segura, consultar y compartir sus **Boletos Oficiales QR** en *Mis Reservas*, gestionar su **Itinerario Semanal** con calendario, publicar experiencias **multi-foto** (hasta 5 fotos), reaccionar (`Útil`) y comentar en Comunidad. |
| **Administrador de Agencia (`ADMIN`)** | **Azul Pizarra Ejecutivo (*Andean Slate* `#1B3B4B` / `#102835`)** | Acceso exclusivo al **Panel Ejecutivo de Agencia**: 4 KPIs en vivo (reservas, ingresos `S/.`, pasajeros y tour top), datos fiscales MINCETUR editables, gestión de **Reservas filtradas por Destino/Estado/Buscador** con cambio de estado y notificación automática, **CRUD completo de Lugares Turísticos** (con selector Departamento → Provincia → Distrito), **Moderación de publicaciones y comentarios** en Comunidad y **Emisor de Alertas Push** hacia la Bandeja de Avisos. |

---

## 5. Credenciales de Testing (Pruebas y Sustentación)

Las cuentas semilla se cargan desde [`app/src/main/assets/services/usuarios.json`](../app/src/main/assets/services/usuarios.json) (sin credenciales hardcodeadas en las vistas):

| Rol | Nombre | Correo Electrónico | Contraseña | Acceso Rápido |
| :--- | :--- | :--- | :--- | :--- |
| **Turista (`CLIENTE`)** | Kevin Turista | `kevin@peruviajero.pe` | `123456` | Botón *"Acceso Rápido Turista"* en `LoginActivity` |
| **Turista (`CLIENTE`)** | Edison Viajero | `edison@peruviajero.pe` | `123456` | Registro / Login estándar |
| **Administrador (`ADMIN`)** | Gerson Administrador | `gerson@peruviajero.pe` | `admin123` | Botón *"Acceso Rápido Admin"* en `LoginActivity` o menú lateral |

> **Nota:** También es posible crear nuevas cuentas desde el botón **Registrarse** en `LoginActivity` o alternar el rol desde el menú lateral (*Drawer*) para demostraciones rápidas en clase.

---

## 6. APIs y Servicios Externos Utilizados

| Servicio / API | Endpoint / Fuente | Uso en PerúViajero | Respaldo Offline |
| :--- | :--- | :--- | :--- |
| **API REST de Destinos Turísticos** | `GET https://raw.githubusercontent.com/Edisonkz/Per-Viajero/main/destinos.json` | Catálogo oficial de destinos del Perú con fotos, coordenadas GPS, categoría y calificación (Retrofit 2 + OkHttp 4 con caché de 10 MB). | Caché en `SharedPreferences` + fusión en vivo con destinos creados por el Admin (`DestinoService`). |
| **API Oficial de Ubigeos del Perú** | `GET https://raw.githubusercontent.com/joseluisq/ubigeos-peru/master/json/{departamentos,provincias,distritos}.json` | Selector en cascada **Departamento → Provincia → Distrito** (25 departamentos, 196 provincias y 1,874 distritos) en el módulo Admin CRUD de lugares turísticos (`UbigeoPeruService`). | Caché local persistente + [`assets/services/ubigeo_peru_fallback.json`](../app/src/main/assets/services/ubigeo_peru_fallback.json). |
| **OSRM Routing API** | `GET https://router.project-osrm.org/route/v1/driving/{lon1},{lat1};{lon2},{lat2}` | Trazado de rutas viales reales, distancia exacta y tiempo estimado desde la ubicación GPS del usuario hasta el destino (`RoutingService`). | Fallback geodésico con fórmula de Haversine (`GeoMath`). |
| **OpenStreetMap / OSMDroid** | Tiles públicos de OpenStreetMap (`MAPNIK`) | Renderizado del mapa interactivo nativo sin dependencia de facturación en `MapaRutaActivity` y `RutaDestinoActivity`. | Caché local de tiles de OSMDroid. |
| **Firebase Cloud Messaging (FCM) + Simulador Local** | `PeruViajeroMessagingService` + `AlarmManager` (`AvisoPushSimulator`) | Recepción de alertas turísticas JSON con 4 canales por prioridad, agrupación `InboxStyle`, `TaskStackBuilder` y bandeja persistente. | Semilla inicial en [`assets/services/avisos.json`](../app/src/main/assets/services/avisos.json) + simulador JSON integrado en `BandejaAvisosActivity`. |
| **Android `ContactsContract` (`ContentResolver`)** | `ContactsContract.CommonDataKinds.Phone.CONTENT_URI` | Importación de acompañantes de viaje en `ReservaTourActivity` con `GestorPermisos` de 4 estados. | Registro manual de pasajeros (degradación elegante). |

---

## 7. Metodología Spec-Driven Development (SDD) y Documentos de Planeamiento

En **PerúViajero**, cada funcionalidad se planifica y valida con especificaciones previas antes de implementarse:

| Archivo | Enfoque de la Especificación | Contenido Clave |
| :--- | :--- | :--- |
| [01_especificacion_requerimientos.md](01_especificacion_requerimientos.md) | **Requisitos del Sistema** | Requerimientos funcionales (RF) y no funcionales (RNF). |
| [02_contrato_api_datos.md](02_contrato_api_datos.md) | **Contratos de Datos y API** | Esquemas JSON, Endpoints REST, DTOs Retrofit y `SharedPreferences`. |
| [03_especificacion_arquitectura_ui.md](03_especificacion_arquitectura_ui.md) | **Arquitectura y UI/UX** | Estados (Carga, Éxito, Error), jerarquía de pantallas y temas Claro/Oscuro. |
| [04_plan_desarrollo_sdd_tareas.md](04_plan_desarrollo_sdd_tareas.md) | **Plan de Ejecución y DoD** | Desglose ordenado por fases y criterios de aceptación. |
| [05_plan_agencia_turismo_servicios.md](05_plan_agencia_turismo_servicios.md) | **Modelo Comercial y Roles** | Modelo de agencia formal, cotizador multivariable y métricas. |
| [06_catalogo_pantallas_y_roles.md](06_catalogo_pantallas_y_roles.md) | **Catálogo de Pantallas y Roles** | Detalle técnico de pantallas, permisos y flujos por rol. |
| [roadmap_s10_s16/README.md](roadmap_s10_s16/README.md) | **Roadmap Semanas 10 a 16** | Evolución completa: Comunidad, GPS, Brújula, Permisos, Push y Release R8. |
| [../docs/checklists_terminados/README_CHECKLIST_Y_ADMIN.md](../docs/checklists_terminados/README_CHECKLIST_Y_ADMIN.md) | **Checklist Maestro Completado (Fases A–N)** | Trazabilidad completa de todas las fases ejecutadas y verificadas. |
