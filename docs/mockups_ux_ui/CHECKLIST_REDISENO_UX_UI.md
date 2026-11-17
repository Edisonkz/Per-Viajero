# Checklist de Rediseño UX/UI — `PerúViajero`

Referencia visual en [docs/mockups_ux_ui](README.md).  
Estrategia de implementación incremental: primero el **Shell de Navegación + Pantallas 1 y 2 (`Explorar` y `Comunidad`)**, y luego las pantallas 3, 4, 5 y 6 (`Mis Reservas`, `Itinerario`, `Favoritos` y `Detalle Hero`).

---

## Fase A — Shell de Navegación Híbrida (`00_menu_hamburguesa_drawer.jpg`)
- [x] **Componente de barra inferior curva (`CurvedBottomBarBgView`):** Dibujar barra blanca/tarjeta con arco central en media luna suave y elevación limpia.
- [x] **Botón central elevado (`Mis Reservas`):** Botón circular `#006837` (54dp) flotando sobre el arco de media luna con ícono blanco de boleto y etiqueta inferior `"Mis Reservas"`.
- [x] **Menú de 5 tabs principales:** `Explorar` (nuevo ícono de montaña/pin `ic_explore_mountain`), `Comunidad`, `Mis Reservas` (central), `Itinerario`, `Favoritos`.
- [x] **Menú Lateral (`DrawerLayout` + `NavigationView`):**
  - [x] Header `#006837` con curva superior derecha envolvente hacia la cámara/notch (`DrawerHeaderBgView`), bordes derechos redondeados (`28dp`), foto circular (desde Cámara/avatar), nombre, correo y badge blanco con texto `#C1272D` (`TURISTA` / `ADMIN` / `INVITADO`).
  - [x] Opciones: *Mapa GPS y Rutas*, *Brújula Offline*, *Mi Itinerario Semanal*, *Guía e IPERÚ*, *Cambiar a Modo Admin/Turista*, *Modo Oscuro* (con `SwitchMaterial` en vivo), *Permisos y Diagnóstico*, *Ver Bienvenida* e *Iniciar/Cerrar Sesión*.
- [x] **Toolbar unificado:** Ícono hamburguesa `☰`, título dinámico de la pestaña activa, acceso directo a Brújula (`🧭`) y Notificaciones/Diagnóstico (`🔔`), con `WindowInsetsHelper` para no chocar con la barra de estado ni con los botones de navegación del sistema.

## Fase B — Pantalla 1: `Explorar` (`01_tab_explorar.jpg`)
- [x] **Buscador superior tipo píldora:** Campo redondeado `"Buscar atractivo o ciudad..."` con ícono de búsqueda y borrado rápido.
- [x] **Fila de 4 Accesos Rápidos (4 columnas):** `Mapa GPS`, `Brújula Offline`, `Publicar Foto`, `Guía IPERÚ` (con ícono dedicado de libro/guía `ic_book_guide`) en tarjetas blancas redondeadas con íconos en `#006837`.
- [x] **Chips horizontales de categoría:** `Todos`, `Arqueología`, `Naturaleza`, `Aventura`, `Cultura`, `Playa` (combinados con el buscador de texto).
- [x] **Tarjetas de destino (`item_destino.xml`):** Fotografía con bordes redondeados dentro de la tarjeta (y placeholder degradado andino `bg_destino_placeholder` si no hay imagen), corazón circular flotante en esquina superior derecha, título en negrita, subtítulo con ubicación + distancia GPS (`Cusco, Perú • 502 km`) y calificación con estrella dorada + número en `#C1272D`.

## Fase C — Pantalla 2: `Comunidad` (`02_tab_comunidad.jpg`)
- [x] **Convertir en `ComunidadFragment.kt`:** Reutilizable tanto en el Tab 2 del menú principal como dentro de `ComunidadActivity` (cuando se abre filtrada desde un destino).
- [x] **Chips de filtro por Región:** `Todas`, `Cusco`, `Arequipa`, `Ica`, `Puno`, `Áncash`, `Amazonas`, `Piura`, `Loreto`, `La Libertad`, `Lima`.
- [x] **Selector de Destino en tarjeta blanca:** `"Destino: Todos"` (o el destino filtrado).
- [x] **Tarjetas sociales (`item_publicacion.xml`):** Avatar + nombre + destino, dos pills suaves apilados a la derecha (Región en verde suave y `5.0 ★` en ámbar suave), foto full-width, texto de reseña, acciones (`Útil`, `Comentarios`, `Ver destino`).
- [x] **FAB `Publicar`:** Botón píldora `#006837` flotando sobre el borde derecho sin chocar con el arco central.

## Fase D — Pantalla 3: `Mis Reservas` (`03_tab_mis_reservas.jpg`)
- [x] **Franja superior de viajero + chips de estado:** `Todas`, `Confirmadas`, `En Ruta`, `Completadas`.
- [x] **Tarjetas estilo *Boarding Pass* (`BoardingPassBgView`):** Muescas laterales + línea punteada divisoria, código de reserva, badge de estado, monto en `#006837` y 3 botones rápidos por boleto (`Ver Boleto`, `Ruta GPS`, `Brújula`).
- [x] **Soporte de rol Admin:** Acceso al Panel de Agencia en modo Administrador con las mismas tarjetas y acciones rápidas.

## Fase E — Pantalla 4: `Itinerario` (`04_tab_itinerario.jpg`)
- [x] **Integrar `ItinerarioFragment.kt` en el Tab 4:** Tarjeta de progreso porcentual, selector segmentado `Por Días` | `Calendario`, chips de días (`Todos`, `Día 1`..`Día 7`), tarjetas de checklist, sugerencias semanales desde `ItinerarioService` y FAB `+ Actividad` (`ItinerarioActivity` queda como wrapper del mismo fragment).

## Capa de Datos Simulados / Servicios (`assets/services/*.json` + `services/`)
- [x] **Migración total de mocks a JSON:** `usuarios.json`, `empresa.json`, `reservas.json`, `comunidad.json`, `itinerario.json`, `sugerencias_semanales.json` leídos vía `JsonAssetReader` y servidos desde `UsuarioService`, `ReservaService`, `ComunidadService` e `ItinerarioService`.

## Fase F — Pantalla 5: `Favoritos` *(Siguiente paso)*
- [ ] **Actualizar `FavoritosFragment.kt`:** Franja resumen offline, filtros por región (Sierra, Costa, Selva), tarjetas con corazón rojo relleno, distancia GPS y botones rápidos `Ruta GPS` y `Reservar`.

## Fase G — Pantalla 6: `Detalle de Destino` Hero (`06_detalle_destino_hero.jpg`) *(Siguiente paso)*
- [ ] **Hero `CollapsingToolbarLayout`:** Foto a sangre arriba con botones circulares flotantes (atrás, favorito, compartir).
- [ ] **Grid 4-en-1 sin scroll:** `Ruta GPS`, `Brújula`, `Reseñas (N)`, `Itinerario`.
- [ ] **Previa de Comunidad + Barra Sticky inferior:** `"Desde S/ 150 / pers"` + botón píldora `"Cotizar y Reservar"`.
