package ir.topograde.app

import ir.topograde.app.geo.CoordinateConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class CoordinateRoundTripTest {
    @Test fun utmRoundTrip() {
        val u=CoordinateConverter.wgs84ToUtm(35.6892,51.3890,39)
        val w=CoordinateConverter.utmToWgs84(u.easting,u.northing,39,true)
        assertEquals(35.6892,w.latitude,1e-5); assertEquals(51.3890,w.longitude,1e-5)
    }
}