package pe.edu.utp.perviajero

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.utp.perviajero.util.GeoMath

class GeoMathTest {

    @Test
    fun haversine_samePoint_returnsZero() {
        val dist = GeoMath.haversineDistanceKm(-13.1631, -72.5450, -13.1631, -72.5450)
        assertEquals(0.0, dist, 0.001)
    }

    @Test
    fun haversine_limaToMachuPicchu_isAround500Km() {
        val dist = GeoMath.haversineDistanceKm(-12.0464, -77.0428, -13.1631, -72.5450)
        assertTrue("Expected ~502 km, got $dist", dist in 490.0..520.0)
    }

    @Test
    fun bearing_dueNorthAndDueEast_areAccurate() {
        val north = GeoMath.calculateBearingDegrees(-12.0, -77.0, -11.0, -77.0)
        val east = GeoMath.calculateBearingDegrees(0.0, -77.0, 0.0, -76.0)
        assertEquals(0f, north, 0.5f)
        assertEquals(90f, east, 0.5f)
    }

    @Test
    fun cardinalPoint_mapsDegreesCorrectly() {
        assertEquals("N", GeoMath.cardinalPoint(359f))
        assertEquals("NE", GeoMath.cardinalPoint(45f))
        assertEquals("E", GeoMath.cardinalPoint(90f))
        assertEquals("S", GeoMath.cardinalPoint(180f))
        assertEquals("O", GeoMath.cardinalPoint(270f))
    }

    @Test
    fun relativeTurnAngle_choosesShortestDirection() {
        assertEquals(20f, GeoMath.relativeTurnAngle(350f, 10f), 0.1f)
        assertEquals(-20f, GeoMath.relativeTurnAngle(10f, 350f), 0.1f)
    }
}
