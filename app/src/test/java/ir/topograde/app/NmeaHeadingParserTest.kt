package ir.topograde.app

import ir.topograde.app.gnss.NmeaHeadingParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NmeaHeadingParserTest {
    @Test fun parsesHdt(){
        val h=NmeaHeadingParser.parse("\$GPHDT,123.45,T")
        assertNotNull(h)
        assertEquals(123.45,h!!,1e-9)
    }
}