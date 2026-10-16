# Especificación y Plan: Agencia de Servicios Turísticos con Doble Rol (SDD-AGENCY)
**Proyecto:** PerúViajero Tours & Travel S.A.C.  
**Documento:** `planeamiento/05_plan_agencia_turismo_servicios.md`  
**Estado:** Planeamiento Formal Aprobado  

---

## 1. Visión y Modelo de Negocio

El aplicativo se concibe como la plataforma móvil comercial de una **Agencia de Viajes y Operador Turístico Nacional**.

Para maximizar la experiencia de usuario y responder a la rúbrica con funcionalidades avanzadas, el sistema introduce una **arquitectura de acceso libre (Modo Invitado) con escalamiento a 2 Roles de Usuario diferenciados**:

```
                              ┌───────────────────────────────────┐
                              │     ACCESO INICIAL: INVITADO      │
                              │ - Navegación libre por catálogo   │
                              │ - Búsqueda de destinos en vivo    │
                              │ - Guía del viajero y mapas        │
                              └─────────────────┬─────────────────┘
                                                │
                                    ¿Desea Reservar / Gestionar?
                                                │
                     ┌──────────────────────────┴──────────────────────────┐
                     ▼                                                     ▼
      ┌─────────────────────────────┐                       ┌─────────────────────────────┐
      │   ROL 1: TURISTA / CLIENTE  │                       │   ROL 2: AGENTE / ADMIN     │
      ├─────────────────────────────┤                       ├─────────────────────────────┤
      │ • Cotizador interactivo     │                       │ • Dashboard con métricas    │
      │ • Selección fecha (Calendar)│                       │ • Total ingresos en Soles   │
      │ • Contador personas (+ / -) │                       │ • Total reservas activas    │
      │ • Escoge Movilidad & Hotel  │                       │ • Aprobación de reservas    │
      │ • Cálculo total en tiempo real│                     │ • Cambio de estados cliente │
      │ • Vouchers en "Mis Reservas"│                       │ • Gestión de tours/cupos    │
      └─────────────────────────────┘                       └─────────────────────────────┘
```

---

## 2. Definición Detallada de Roles y Casos de Uso

### 2.1 Modo Invitado (Guest - Sin Registro Previo)
* **CU-01 (Exploración Pública):** El usuario instala la app y navega directamente sin pantallas de bloqueo ni registros forzados.
* **CU-02 (Catálogo REST):** Visualiza los 12 destinos turísticos obtenidos desde la API REST vía Retrofit.
* **CU-03 (Búsqueda Reactiva):** Utiliza el buscador en tiempo real para filtrar atractivos por departamento o categoría.
* **CU-04 (Consulta de Rutas):** Accede a la ficha informativa, cómo llegar y apertura en Google Maps mediante Intent implícito.
* **CU-05 (Disparador de Autenticación):** Si el invitado pulsa en *"Cotizar y Reservar Tour"* o accede a *"Mis Reservas"*, la app le solicita iniciar sesión o registrarse para continuar.

---

### 2.2 Rol 1: Turista / Cliente (Viajero)
* **CU-06 (Perfil de Cliente):** Registra o inicia sesión con su nombre, correo y teléfono. Sus datos se persisten localmente en `SharedPreferences`.
* **CU-07 (Cotizador Dinámico Multivariable - Alta Interacción):**
  Al seleccionar un destino y pulsar *"Cotizar y Reservar"*, se abre la pantalla de cotización donde personaliza:
  1. **Fecha de Salida:** Apertura de selector de calendario nativo (`DatePickerDialog`).
  2. **Cantidad de Viajeros:** Contador interactivo `[ - ] 1 [ + ]` de 1 a 10 pasajeros con validación de límites.
  3. **Selección de Movilidad / Transporte:**
     - *Van Turística Estándar:* Incluida (+ S/ 0.00 / persona)
     - *Bus Panorámico Confort:* + S/ 25.00 / persona
     - *Camioneta 4x4 Aventura Off-Road:* + S/ 50.00 / persona
     - *Tren Ejecutivo Primera Clase (Inca Rail/PeruRail):* + S/ 90.00 / persona
  4. **Categoría de Alojamiento / Hotel:**
     - *Solo Tour (Sin Hotel):* + S/ 0.00
     - *Hotel Turista 3 Estrellas:* + S/ 45.00 por noche / persona
     - *Hotel Superior 4 Estrellas:* + S/ 95.00 por noche / persona
     - *Resort & Spa 5 Estrellas:* + S/ 180.00 por noche / persona
* **CU-08 (Cálculo del Presupuesto en Tiempo Real - Estado Dinámico):**
  La pantalla recalcula de forma instantánea el valor total en Soles (S/.) ante cualquier modificación de los inputs sin recrear la Activity.
  $$\text{Total} = \text{Pasajeros} \times (\text{Precio Base} + \text{Tarifa Movilidad} + \text{Tarifa Hotel})$$
* **CU-09 (Generación de Voucher Digital):**
  Al pulsar *"Confirmar Reserva"*, el sistema genera un código alfanumérico único (ej. `PV-2026-7841`), almacena el objeto en `SharedPreferences` y despliega un diálogo de confirmación de reserva exitosa.
* **CU-10 (Historial en "Mis Reservas"):**
  Pestaña dedicada con un `RecyclerView` donde el cliente visualiza sus vouchers generados, fecha de salida, desglose del paquete y estado de la reserva (*"Confirmada"*).

---

### 2.3 Rol 2: Administrador / Agente Turístico (Operador de la Agencia)
* **CU-11 (Dashboard Administrativo con Métricas en Vivo):**
  Pantalla especial para el operador que consolida los indicadores del negocio:
  - 📊 **Total de Reservas Registradas:** Contador global de compras realizadas en el sistema.
  - 💰 **Ingresos Totales Acumulados:** Suma monetaria en Soles (S/.) de todas las reservas generadas.
  - 🏆 **Tour Más Demandado:** Identificación del destino con mayor volumen de reservas.
* **CU-12 (Gestión y Control de Reservas de Clientes):**
  Listado completo de todas las reservas registradas en el sistema por los clientes. El administrador puede:
  - Cambiar el estado de la reserva: *"Confirmada"* ➔ *"En Ruta / Pasajeros en Viaje"* ➔ *"Tour Completado"*.
  - Visualizar los datos de contacto del cliente (Nombre, correo y cantidad de pasajeros).
* **CU-13 (Simulación Rápida de Rol Académico):**
  Para facilitar la demostración ante el profesor en 2 a 3 minutos, en la pantalla de Ajustes/Perfil se incluye un selector o botón:
  `[ Modo Turista ] ⇄ [ Modo Administrador ]`
  Permite conmutar de rol instantáneamente sin necesidad de cerrar sesión ni escribir contraseñas repetitivas.

---

## 3. Modelo de Entidades de Datos

### 3.1 Entidad `Usuario`
```kotlin
data class Usuario(
    val id: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val rol: RolUsuario // CLIENTE o ADMIN
)

enum class RolUsuario {
    CLIENTE,
    ADMIN
}
```

### 3.2 Entidad `Reserva` (Voucher Comercial)
```kotlin
data class Reserva(
    val codigoReserva: String,       // Ej. "PV-2026-5831"
    val destinoId: String,
    val destinoNombre: String,
    val destinoFoto: String,
    val fechaSalida: String,         // "18/11/2026"
    val cantidadPersonas: Int,       // 1 a 10
    val tipoMovilidad: String,       // "Bus Panorámico (+S/ 25)"
    val categoriaHotel: String,      // "Hotel 4 Estrellas (+S/ 95)"
    val costoTotal: Double,          // Calculado dinámicamente
    var estado: String,              // "Confirmada", "En Ruta", "Completada"
    val usuarioNombre: String,
    val usuarioEmail: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)
```

---

## 4. Estructura de Navegación Adaptativa por Rol

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │ Toolbar: Título + Logo + Indicador de Rol (Turista / Administrador)│ │
│ └────────────────────────────────────────────────────────────────────┘ │
│                                                                        │
│                      [ FragmentContainerView ]                         │
│                                                                        │
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │                     BottomNavigationView                           │ │
│ │                                                                    │ │
│ │ SI EL ROL ES TURISTA (CLIENTE):                                    │ │
│ │ [ 1. Destinos ] [ 2. Mis Reservas ] [ 3. Guía ] [ 4. Perfil/Ajustes] │ │
│ │                                                                    │ │
│ │ SI EL ROL ES ADMINISTRADOR (AGENCIA):                              │ │
│ │ [ 1. Destinos ] [ 2. Panel Admin ]  [ 3. Guía ] [ 4. Perfil/Ajustes] │ │
│ └────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Estrategia de Persistencia Local (`SharedPreferences` con Gson)

Para no depender de un servidor backend externo durante la evaluación, toda la lógica de sesión, roles y reservas se mantendrá en `PreferencesHelper.kt`:

1. **Sesión Activa:**
   - Clave `KEY_SESSION_USER`: Objeto `Usuario` serializado en JSON con Gson.
   - Si no existe valor, la app opera en **Modo Invitado**.
2. **Listado de Reservas:**
   - Clave `KEY_RESERVAS_LIST`: Lista `List<Reserva>` serializada en JSON.
   - Permite que cualquier reserva creada por un Turista aparezca inmediatamente en el **Panel de Administrador**, demostrando persistencia relacional local.
3. **Favoritos y Tema Visual:**
   - Mantiene las claves existentes `KEY_FAVORITOS` y `KEY_DARK_MODE`.

---

## 6. Sprints de Construcción (Para cuando se ordene codificar)

| Sprint | Enfoque | Tareas de Construcción |
| :---: | :--- | :--- |
| **Sprint 1** | **Modelos y Persistencia Extendida** | Implementar `Usuario.kt`, `Reserva.kt` y actualizar `PreferencesHelper.kt` con métodos de serialización Gson para sesión y lista de reservas. |
| **Sprint 2** | **Módulo de Login & Selección de Rol** | Diseñar `LoginActivity` con opciones de ingreso rápido: *"Acceder como Turista"* y *"Acceder como Administrador de Agencia"*. |
| **Sprint 3** | **Cotizador Interactivo de Tours** | Crear `ReservaTourActivity` con `DatePickerDialog`, contador reactivo `[ - ] [ + ]`, selección de transporte/hotel y cálculo dinámico en vivo. |
| **Sprint 4** | **Vistas de Reservas y Panel de Administrador** | Desarrollar `MisReservasFragment` para clientes y `AdminDashboardFragment` con métricas de ventas y control de estados para el administrador. |
| **Sprint 5** | **Integración y Navegación Adaptativa** | Conectar `MainActivity` para ajustar los menús según el rol activo y permitir conmutación rápida de roles en Ajustes. |
