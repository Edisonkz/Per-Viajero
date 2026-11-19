package pe.edu.utp.perviajero

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.utp.perviajero.data.model.PublicacionViajero

class PublicacionViajeroTest {

    @Test
    fun obtenerFotosReturnsMultiPhotosWhenPresent() {
        val pub = PublicacionViajero(
            id = "p1",
            autorNombre = "Camila",
            autorEmail = "c@pe",
            region = "Cusco",
            destinoId = "1",
            destinoNombre = "Machu Picchu",
            fotoUri = "https://img/1.jpg",
            fotosUris = listOf("https://img/1.jpg", "https://img/2.jpg", "https://img/3.jpg"),
            calificacion = 5f,
            comentario = "Increíble",
            fechaPublicacion = "19/11/2026"
        )
        assertEquals(3, pub.obtenerFotos().size)
        assertEquals("https://img/1.jpg", pub.fotoPrincipal())
    }

    @Test
    fun obtenerFotosFallsBackToSingleFotoUriForLegacyPosts() {
        val pub = PublicacionViajero(
            id = "p2",
            autorNombre = "Mateo",
            autorEmail = "m@pe",
            region = "Ica",
            destinoId = "3",
            destinoNombre = "Huacachina",
            fotoUri = "https://img/single.jpg",
            fotosUris = null,
            calificacion = 4.5f,
            comentario = "Genial",
            fechaPublicacion = "19/11/2026"
        )
        assertEquals(listOf("https://img/single.jpg"), pub.obtenerFotos())
    }
}
