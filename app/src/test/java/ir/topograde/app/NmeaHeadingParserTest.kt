package ir.topograde.app

import ir.topograde.app.gnss.NmeaHeadingParser
import org.junit.Assert.assertEquals
import org.junit.Test

class NmeaHeadingParserTest {
    @Test fun parsesHdt(){
        assertEquals(123.45,NmeaHeadingParser.parse("\$GPHDT,123.45,T"),1e-9)
    }
}