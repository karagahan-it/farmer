package ir.topograde.app

import ir.topograde.app.geo.CoordinateConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class CoordinateConverterTest {
    @Test fun centralMeridianHas500kEasting() {
        val utm = CoordinateConverter.wgs84ToUtm(0.0, 51.0, 39)
        assertEquals(500000.0, utm.easting, 0.01)
        assertEquals(0.0, utm.northing, 0.01)
        assertEquals(39, utm.zone)
    }
}