# Especificación de Requerimientos del Sistema (SDD-REQ)
**Proyecto:** PerúViajero  
**Documento:** `planeamiento/01_especificacion_requerimientos.md`  
**Estado:** Aprobado para Construcción  

---

## 1. Alcance y Objetivos

El sistema consiste en una aplicación móvil Android nativa que consume información turística desde una API REST, permite el filtrado interactivo en tiempo real sin recargar la pantalla, persiste el estado de favoritos del usuario y adapta su interfaz visual entre modo claro y oscuro.

---

## 2. Requerimientos Funcionales (RF)

### Bloque A: Fundamentos y Navegación (Base Curricular Semanas 1 a 5 - APF1)
* **RF-01 (Navegación Multinivel):** La aplicación debe contar con un menú de navegación principal (`BottomNavigationView`) con 3 secciones persistentes:
  * *Destinos* (Catálogo general)
  * *Favoritos* (Listado de elementos guardados)
  * *Ajustes* (Configuración y personalización)
* **RF-02 (Barra Superior Material Design):** Debe existir una `Toolbar` en `MainActivity` que muestre el título del aplicativo y el ícono oficial de la aplicación (`setDisplayShowHomeEnabled`, `setIcon`).
* **RF-03 (Navegación hacia Detalle):** Al pulsar sobre cualquier destino del catálogo, el sistema debe abrir mediante un `Intent` explícito con `Bundle`/Extras la pantalla `DetalleDestinoActivity`, pasando los datos del destino seleccionado (nombre, foto, descripción, calificación, departamento).
* **RF-04 (Ciclo de Vida Controlado):** Los componentes de UI (`Activities` y `Fragments`) deben sincronizar sus llamadas en `onCreate`, `onResume` y `onDestroy` garantizando que no se dupliquen llamadas de red al rotar o pausar la aplicación.

### Bloque B: Datos, Red y Persistencia (Novedades Semanas 6 a 10 - APF2)
* **RF-05 (Consumo API REST Asíncrono):** La app debe realizar una petición HTTP `GET` en segundo plano (hilo background) hacia el endpoint de destinos turísticos usando la librería **Retrofit 2** y procesar la respuesta sin congelar la interfaz de usuario (`UI Thread`).
* **RF-06 (Renderizado en RecyclerView):** La app debe renderizar una lista de al menos 10 destinos turísticos usando un `RecyclerView` y un `RecyclerView.Adapter` con patrón `ViewHolder`.
* **RF-07 (Gestión de los 3 Estados de Interfaz):**
  * *Estado Cargando:* Muestra un `ProgressBar` mientras se realiza la consulta HTTP.
  * *Estado Éxito:* Oculta el `ProgressBar` y despliega la lista en el `RecyclerView`.
  * *Estado Error:* Si la conexión falla (emulador en modo avión/sin red), oculta la lista y muestra un mensaje de advertencia junto a un botón *"Reintentar"* que vuelve a invocar la petición.
* **RF-08 (Estado Dinámico / Buscador Reactivo):** La pantalla de Destinos debe incluir un campo de búsqueda (`SearchView` o `EditText` con `TextWatcher`) que filtre la lista de atractivos en tiempo real conforme el usuario escribe, sin recrear la Activity.
* **RF-09 (Persistencia con SharedPreferences):**
  * Cada tarjeta de destino debe tener un botón interactivo (ícono de corazón/estrella) para marcar o desmarcar como favorito.
  * Los IDs de los destinos marcados deben guardarse en `SharedPreferences`.
  * Al cerrar por completo la app y volver a abrirla, los elementos previamente marcados deben permanecer en la sección *Favoritos*.
* **RF-10 (Selector de Tema Visual Claro/Oscuro):** En la sección de Ajustes debe existir un Switch que active o desactive el Modo Oscuro (`AppCompatDelegate.setDefaultNightMode()`). La selección debe guardarse en `SharedPreferences` y aplicarse en cada arranque de la app.

---

## 3. Requerimientos No Funcionales (RNF)

* **RNF-01 (Compatibilidad Android):** La aplicación debe ejecutarse fluidamente desde Android 5.0 (API 21 - Lollipop) hasta Android 14+ (API 34).
* **RNF-02 (Arquitectura en Paquetes):** El código debe estructurarse estrictamente bajo el estándar del sílabo:
  * `pe.edu.utp.peruviajero.ui`
  * `pe.edu.utp.peruviajero.data`
  * `pe.edu.utp.peruviajero.network`
  * `pe.edu.utp.peruviajero.adapter`
* **RNF-03 (Rendimiento en UI):** La carga de imágenes desde URLs remotas debe gestionarse asíncronamente con memoria caché (mediante Glide) para evitar saturación de memoria RAM (OutOfMemoryError).
* **RNF-04 (Manejo de Errores y Resiliencia):** Las fallas de conexión no deben provocar cierres inesperados (*crashes*) de la aplicación.

