package pe.edu.utp.perviajero.network

// Datos oficiales para respaldo y pruebas locales (12 destinos del Perú)
object MockData {
    val DESTINOS_JSON = """
    [
      {
        "id": "1",
        "nombre": "Santuario Histórico de Machu Picchu",
        "departamento": "Cusco",
        "categoria": "Arqueología",
        "descripcion": "Obra maestra de la ingeniería y arquitectura incaica ubicada a 2,430 m s. n. m., reconocida como una de las Nuevas Siete Maravillas del Mundo Moderno.",
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
        "descripcion": "Uno de los cañones más profundos del planeta, famoso por el vuelo majestuoso del cóndor andino y sus impresionantes andenerías preincas.",
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
        "descripcion": "Laguna natural en medio del desierto costero rodeada de imponentes dunas de arena, punto de partida para carros tubulares y sandboarding.",
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
        "descripcion": "El lago navegable más alto del mundo (3,812 m s. n. m.), hogar de comunidades milenarias que habitan islas flotantes construidas con totora.",
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
        "descripcion": "Formación geológica única en la Cordillera del Vilcanota a 5,200 m s. n. m., famosa por sus franjas multicolores originadas por sedimentos minerales.",
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
        "descripcion": "Enigmáticos trazos prehispánicos sobre el desierto que representan animales gigantescos (el colibrí, el mono, la araña) visibles desde avioneta.",
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
        "descripcion": "Imponente complejo fortificado de la civilización Chachapoyas sobre la cresta de la selva alta, accesible mediante el primer teleférico de Perú.",
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
        "descripcion": "Laguna de color azul turquesa al pie del nevado Chacraraju en la Cordillera Blanca, una de las rutas de senderismo de montaña más bellas del Perú.",
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
        "descripcion": "Destino costero de aguas cálidas, ideal para la práctica de surf, avistamiento de ballenas jorobadas y gastronomía a base de pescados frescos.",
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
        "descripcion": "Área marina costera protegida que alberga colonias de lobos marinos, pingüinos de Humboldt, formaciones rocosas y la emblemática Playa Roja.",
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
        "descripcion": "Exploración del bosque tropical amazónico más extenso del mundo, paseos fluviales en busca de delfines rosados y visitas a comunidades nativas.",
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
        "descripcion": "La ciudad de adobe más grande de América precolombina y capital del reino Chimú, destacada por sus muros tallados con figuras geométricas y marinas.",
        "calificacion": 4.6,
        "imageUrl": "https://images.unsplash.com/photo-1596401057633-54a8fe8ef647?w=800",
        "latitud": -8.1081,
        "longitud": -79.0747
      }
    ]
    """.trimIndent()
}

