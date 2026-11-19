package pe.edu.utp.perviajero

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.utp.perviajero.util.AvisoPayloadParser

class AvisoPayloadParserTest {

    @Test
    fun parsePayload_validCompleteJson_parsesAllFields() {
        val json = """
            {"tipo":"alerta","titulo":"Lluvias en Cusco","cuerpo":"Usa impermeable","region":"Cusco","url":"https://www.gob.pe/senamhi"}
        """.trimIndent()
        val aviso = AvisoPayloadParser.parsePayload(json, idGenerator = { 42 }, nowMillis = 1000L)
        assertNotNull(aviso)
        assertEquals(42, aviso?.id)
        assertEquals("alerta", aviso?.tipo)
        assertEquals("Lluvias en Cusco", aviso?.titulo)
        assertEquals("Cusco", aviso?.region)
        assertEquals("https://www.gob.pe/senamhi", aviso?.url)
        assertFalse(aviso?.leido ?: true)
    }

    @Test
    fun parsePayload_missingOptionalFields_usesDefaults() {
        val json = """{"titulo":"Aviso rápido","cuerpo":"Mensaje informativo"}"""
        val aviso = AvisoPayloadParser.parsePayload(json, idGenerator = { 7 })
        assertNotNull(aviso)
        assertEquals("aviso", aviso?.tipo)
        assertEquals("", aviso?.region)
        assertEquals("", aviso?.url)
    }

    @Test
    fun parsePayload_invalidOrMissingRequired_returnsNull() {
        assertNull(AvisoPayloadParser.parsePayload("{json roto"))
        assertNull(AvisoPayloadParser.parsePayload("""{"tipo":"alerta","cuerpo":"Sin titulo"}"""))
        assertNull(AvisoPayloadParser.parsePayload("""{"titulo":"Sin cuerpo"}"""))
    }

    @Test
    fun isSafeUrl_blocksUnsafeSchemesAndAllowsHttpHttps() {
        assertTrue(AvisoPayloadParser.isSafeUrl("https://www.peru.travel/es"))
        assertTrue(AvisoPayloadParser.isSafeUrl("http://peruviajero.pe/info"))
        assertFalse(AvisoPayloadParser.isSafeUrl("javascript:alert(1)"))
        assertFalse(AvisoPayloadParser.isSafeUrl("file:///etc/passwd"))
        assertFalse(AvisoPayloadParser.isSafeUrl(""))
    }
}
