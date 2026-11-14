package ir.topograde.app

import ir.topograde.app.gnss.NmeaQualityParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NmeaQualityParserTest {
    @Test fun parsesGsaDops() {
        val q=NmeaQualityParser.parse("\$GPGSA,A,3,04,05,09,12,24,25,29,31,,,,,1.8,1.0,1.5")
        assertNotNull(q); assertEquals(1.8,q!!.pdop!!,0.0); assertEquals(1.0,q.hdop!!,0.0); assertEquals(1.5,q.vdop!!,0.0)
    }
    @Test fun parsesGstSigma() {
        val q=NmeaQualityParser.parse("\$GPGST,024603.00,1.2,0.9,0.7,45.0,0.4,0.3,0.8")
        assertNotNull(q); assertEquals(0.5,q!!.horizontalSigmaMeters!!,1e-9); assertEquals(0.8,q.verticalSigmaMeters!!,0.0)
    }
}