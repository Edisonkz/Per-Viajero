# Metodología de Desarrollo Dirigido por Especificaciones (Spec-Driven Development - SDD)
**Proyecto:** PerúViajero (`pe.edu.utp.peruviajero`)  
**Curso:** Desarrollo de Aplicaciones Móviles — UTP  
**Evaluación:** Avance de Proyecto Final 2 (APF2) — Semana 10 (con retrocompatibilidad APF1 Semana 5)  

---

## 1. ¿Qué es Spec-Driven Development (SDD) en este Proyecto?

El **Desarrollo Dirigido por Especificaciones (SDD)** establece que *ninguna línea de código de producción se escribe hasta que las especificaciones funcionales, los contratos de datos y los criterios de aceptación estén formalmente definidos y validados*.

En **PerúViajero**, SDD garantiza:
1. **Cero ambigüedad:** Se definen de antemano los modelos JSON, interfaces Retrofit, layouts XML y claves de `SharedPreferences`.
2. **Cumplimiento estricto de la rúbrica (20/20):** Cada especificación mapea directamente a un criterio evaluable del PDF de APF2 y las diapositivas de clase (Semanas 1 a 10).
3. **Continuidad comprobable:** Articula los entregables de APF1 (navegación y vistas) con los de APF2 (datos asíncronos y persistencia).

---

## 2. Estructura de Documentos de Planeamiento (Carpeta `/planeamiento`)

| Archivo | Enfoque de la Especificación | Contenido Clave |
| :--- | :--- | :--- |
| [01_especificacion_requerimientos.md](file:///c:/Users/Edison/Downloads/Proyectos/Universidad/Desarrollo%20de%20aplicaciones/Proyecto%20APP/planeamiento/01_especificacion_requerimientos.md) | **Requisitos del Sistema** | Requerimientos funcionales (RF) y no funcionales (RNF), trazabilidad curricular (Semana 1 a 10). |
| [02_contrato_api_datos.md](file:///c:/Users/Edison/Downloads/Proyectos/Universidad/Desarrollo%20de%20aplicaciones/Proyecto%20APP/planeamiento/02_contrato_api_datos.md) | **Contratos de Datos y API** | Esquema JSON formal, Endpoints REST, DTOs de Retrofit, esquema de almacenamiento local `SharedPreferences`. |
| [03_especificacion_arquitectura_ui.md](file:///c:/Users/Edison/Downloads/Proyectos/Universidad/Desarrollo%20de%20aplicaciones/Proyecto%20APP/planeamiento/03_especificacion_arquitectura_ui.md) | **Arquitectura y UI/UX** | Máquina de estados (Carga, Éxito, Error), jerarquía de pantallas, sistema de temas Claro/Oscuro y navegación. |
| [04_plan_desarrollo_sdd_tareas.md](file:///c:/Users/Edison/Downloads/Proyectos/Universidad/Desarrollo%20de%20aplicaciones/Proyecto%20APP/planeamiento/04_plan_desarrollo_sdd_tareas.md) | **Plan de Ejecución y DoD** | Desglose ordenado de tareas por fases, criterios de aceptación (*Definition of Done*) y checklist de verificación. |
| [05_plan_agencia_turismo_servicios.md](file:///c:/Users/Edison/Downloads/Proyectos/Universidad/Desarrollo%20de%20aplicaciones/Proyecto%20APP/planeamiento/05_plan_agencia_turismo_servicios.md) | **Modelo Comercial y Roles** | Modelo de negocio de agencia de turismo, cotizador multivariable y panel de métricas para la agencia. |
| [06_catalogo_pantallas_y_roles.md](file:///c:/Users/Edison/Downloads/Proyectos/Universidad/Desarrollo%20de%20aplicaciones/Proyecto%20APP/planeamiento/06_catalogo_pantallas_y_roles.md) | **Catálogo de Pantallas y Roles** | Especificación técnica de las 8 pantallas del sistema, permisos por rol (Invitado/Turista/Admin) y flujo de navegación. |

---

## 3. Matriz de Trazabilidad Rúbrica ↔ Especificaciones SDD

```
┌───────────────────────────────────────────────┬─────────────────────────────┬───────────────────────────┐
│ Criterio de la Rúbrica UTP                    │ Peso (%) │ Mapeo en SDD     │ Componente en Código      │
├───────────────────────────────────────────────┼──────────┼──────────────────┼───────────────────────────┤
│ 1. Navegación de APF1 sin errores             │ 10%      │ SDD-UI-01        │ BottomNav + Fragments     │
│ 2. Consumo API REST y RecyclerView (+10 items)│ 30%      │ SDD-DATA-01/02   │ Retrofit + DestinoAdapter │
│ 3. Tres estados visibles (Carga/Éxito/Error)  │ 20%      │ SDD-UI-02        │ ProgressBar/Recycler/Err  │
│ 4. Estado Dinámico en pantalla (useState eq.) │ 20%      │ SDD-STATE-01     │ Filtro SearchView instant │
│ 5. Persistencia SharedPreferences + Tema      │ 15%      │ SDD-DATA-03      │ PreferencesHelper + Theme │
│ 6. Dominio del código y sustentación          │ 5%       │ SDD-DOC-01       │ README + Guía de Defensa  │
└───────────────────────────────────────────────┴──────────┴──────────────────┴───────────────────────────┘
```

---

## 4. Flujo de Trabajo para el Desarrollo

Una vez revisada esta carpeta de planeamiento, el desarrollo se ejecutará siguiendo estrictamente las especificaciones en 4 sprints de código:
1. **Sprint 1:** Estructura Gradle, dependencias, recursos (colores, temas, strings) y Layouts XML.
2. **Sprint 2:** Modelos POJO, cliente Retrofit y Servicio API con endpoints validados.
3. **Sprint 3:** Navegación (BottomNavigationView), Fragments, RecyclerView Adapter y Estados de carga.
4. **Sprint 4:** Estado reactivo (buscador), SharedPreferences (favoritos) y Selector de tema visual.

