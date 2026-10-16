# Especificación de Contrato de Datos y API (SDD-DATA)
**Proyecto:** PerúViajero  
**Documento:** `planeamiento/02_contrato_api_datos.md`  
**Estado:** Validado  

---

## 1. Contrato de la API REST Pública

### 1.1 Definición del Servicio
* **Protocolo:** HTTPS
* **Formato de intercambio:** JSON (`application/json; charset=utf-8`)
* **Método HTTP:** `GET`
* **Endpoint:** `/destinos` (o archivo estructurado `destinos_peru.json` servido desde CDN/Raw)

### 1.2 Esquema del Objeto `Destino` (Data Schema)

| Campo | Tipo | Requerido | Descripción | Ejemplo |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `String` | Sí | Identificador único del destino turístico | `"1"` |
| `nombre` | `String` | Sí | Nombre oficial del atractivo | `"Machu Picchu"` |
| `departamento` | `String` | Sí | Región o departamento de ubicación | `"Cusco"` |
| `categoria` | `String` | Sí | Clasificación (Arqueología, Naturaleza, Aventura, Playa) | `"Arqueología"` |
| `descripcion` | `String` | Sí | Reseña informativa del destino | `"Imponente ciudadela incaica..."` |
| `calificacion` | `double` | Sí | Puntuación promedio (1.0 a 5.0) | `4.9` |
| `imageUrl` | `String` | Sí | URL directa de la fotografía en alta calidad | `"https://images.unsplash.com/..."` |
| `latitud` | `double` | No | Coordenada geográfica (para APF3 - GPS) | `-13.1631` |
| `longitud` | `double` | No | Coordenada geográfica (para APF3 - GPS) | `-72.5450` |

### 1.3 Payload JSON Oficial (12 Destinos Peruanos)

```json
[
  {
    "id": "1",
    "nombre": "Santuario Histórico de Machu Picchu",
    "departamento": "Cusco",
    "categoria": "Arqueología",
    "descripcion": "Obra maestra de la ingeniería y arquitectura incaica ubicada a 2,430 m s. n. m., reconocida como Maravilla del Mundo Moderno.",
    "calificacion": 4.9,
    "imageUrl": "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=800",
    "latitud": -13.1631,
    "longitud": -72.5450
  },
  {
    "id": "2",
    "nombre": "Cañón del Colca",
    "departamento": "Arequipa",
    "categoria": "Naturaleza",
    "descripcion": "Uno de los cañones más profundos del planeta, famoso por el vuelo majestuoso del cóndor andino y sus paisajes volcánicos.",
    "calificacion": 4.8,
    "imageUrl": "https://images.unsplash.com/photo-1589308078059-be1415eab4c3?w=800",
    "latitud": -15.6094,
    "longitud": -71.8906
  },
  {
    "id": "3",
    "nombre": "Oasis de la Huacachina",
    "departamento": "Ica",
    "categoria": "Aventura",
    "descripcion": "Laguna natural en medio del desierto costero rodeada de imponentes dunas, ideal para paseos en carros tubulares y sandboarding.",
    "calificacion": 4.7,
    "imageUrl": "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=800",
    "latitud": -14.0875,
    "longitud": -75.7633
  },
  {
    "id": "4",
    "nombre": "Lago Titicaca e Islas Uros",
    "departamento": "Puno",
    "categoria": "Cultura",
    "descripcion": "El lago navegable más alto del mundo (3,812 m s. n. m.), hogar de comunidades ancestrales que habitan islas flotantes de totora.",
    "calificacion": 4.8,
    "imageUrl": "https://images.unsplash.com/photo-1578922746465-3a80a228f223?w=800",
    "latitud": -15.8402,
    "longitud": -69.3333
  },
  {
    "id": "5",
    "nombre": "Montaña de Siete Colores (Vinicunca)",
    "departamento": "Cusco",
    "categoria": "Naturaleza",
    "descripcion": "Maravilla geológica a 5,200 m s. n. m. formada por sedimentos minerales que reflejan franjas multicolores turquesa, doradas y rojas.",
    "calificacion": 4.6,
    "imageUrl": "https://images.unsplash.com/photo-1580619305218-8423a7ef79b4?w=800",
    "latitud": -13.8694,
    "longitud": -71.3031
  },
  {
    "id": "6",
    "nombre": "Líneas y Geoglifos de Nazca",
    "departamento": "Ica",
    "categoria": "Arqueología",
    "descripcion": "Enigmáticos grabados precolombinos en las pampas de Jumana que representan animales gigantes y figuras geométricas apreciables desde el aire.",
    "calificacion": 4.7,
    "imageUrl": "https://images.unsplash.com/photo-1544986581-efac024faf62?w=800",
    "latitud": -14.7167,
    "longitud": -75.1333
  },
  {
    "id": "7",
    "nombre": "Fortaleza de Kuélap",
    "departamento": "Amazonas",
    "categoria": "Arqueología",
    "descripcion": "Gran complejo amurallado edificado por la cultura Chachapoyas en la cresta de la montaña selvática, con teleférico panorámico de acceso.",
    "calificacion": 4.7,
    "imageUrl": "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800",
    "latitud": -6.4172,
    "longitud": -77.9236
  },
  {
    "id": "8",
    "nombre": "Laguna 69 y Parque Nac. Huascarán",
    "departamento": "Áncash",
    "categoria": "Aventura",
    "descripcion": "Espejo de agua azul turquesa intenso alimentado por el deshielo de los nevados de la Cordillera Blanca, meca del senderismo de altura.",
    "calificacion": 4.9,
    "imageUrl": "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
    "latitud": -9.0067,
    "longitud": -77.6156
  },
  {
    "id": "9",
    "nombre": "Playas y Malecón de Máncora",
    "departamento": "Piura",
    "categoria": "Playa",
    "descripcion": "Balneario de aguas cálidas en el norte peruano, renombrado por sus olas para el surf, gastronomía marina y vida nocturna.",
    "calificacion": 4.5,
    "imageUrl": "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
    "latitud": -4.1067,
    "longitud": -81.0475
  },
  {
    "id": "10",
    "nombre": "Reserva Nacional de Paracas",
    "departamento": "Ica",
    "categoria": "Naturaleza",
    "descripcion": "Área marina protegida que alberga lobos de mar, pingüinos de Humboldt, aves guaneras y playas rojizas frente al océano Pacífico.",
    "calificacion": 4.8,
    "imageUrl": "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800",
    "latitud": -13.8667,
    "longitud": -76.2500
  },
  {
    "id": "11",
    "nombre": "Río Amazonas y Selva de Iquitos",
    "departamento": "Loreto",
    "categoria": "Naturaleza",
    "descripcion": "Expediciones fluviales por el río más caudaloso del mundo, observación de delfines rosados y senderos en selva virgen.",
    "calificacion": 4.8,
    "imageUrl": "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=800",
    "latitud": -3.7437,
    "longitud": -73.2516
  },
  {
    "id": "12",
    "nombre": "Complejo Arqueológico Chan Chan",
    "departamento": "La Libertad",
    "categoria": "Arqueología",
    "descripcion": "La ciudadela de barro más grande de América prehispánica, capital del reino Chimú con muros de adobe decorados con relieves marinos.",
    "calificacion": 4.6,
    "imageUrl": "https://images.unsplash.com/photo-1596401057633-54a8fe8ef647?w=800",
    "latitud": -8.1081,
    "longitud": -79.0747
  }
]
```

---

## 2. Definición de la Interfaz Retrofit (`TourismApiService.java`)

```java
package pe.edu.utp.peruviajero.network;

import java.util.List;
import pe.edu.utp.peruviajero.data.model.Destino;
import retrofit2.Call;
import retrofit2.http.GET;

public interface TourismApiService {
    @GET("destinos") // O ruta relativa definida en ApiClient
    Call<List<Destino>> getDestinos();
}
```

---

## 3. Esquema de Persistencia Local (`SharedPreferences`)

* **Nombre del archivo de preferencias:** `"peruviajero_prefs"`
* **Modo de acceso:** `Context.MODE_PRIVATE`

| Clave (Key) | Tipo | Valor por Defecto | Propósito |
| :--- | :--- | :--- | :--- |
| `KEY_FAVORITOS` | `Set<String>` | `emptySet()` | Colección de IDs de destinos guardados como favoritos. |
| `KEY_DARK_MODE` | `boolean` | `false` | Indica si el modo oscuro está activo. |
| `KEY_ULTIMA_BUSQUEDA` | `String` | `""` | Recuerda el último término buscado en el filtro. |

