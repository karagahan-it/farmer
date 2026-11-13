package ir.topograde.app

import ir.topograde.app.gnss.FixType
import ir.topograde.app.gnss.NmeaParser
import org.junit.Assert.*
import org.junit.Test

class NmeaParserTest {
    @Test fun parsesRtkFixedGga() {
        val fix = NmeaParser.parse("$GPGGA,123519,4807.038,N,01131.000,E,4,12,0.7,545.4,M,46.9,M,,*47".replace("*47",""))
        assertNotNull(fix); assertEquals(FixType.RTK_FIXED, fix!!.fixType); assertEquals(12, fix.satellites)
        assertEquals(48.1173, fix.latitude, 0.0001); assertEquals(11.516666, fix.longitude, 0.0001)
    }
}