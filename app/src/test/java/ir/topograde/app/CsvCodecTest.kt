package ir.topograde.app

import ir.topograde.app.data.CsvCodec
import ir.topograde.app.model.SurveyPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvCodecTest {
    @Test fun roundTrip() {
        val src=listOf(SurveyPoint(1,"P1",1.0,2.0,3.0),SurveyPoint(2,"P2",4.0,5.0,6.0))
        val parsed=CsvCodec.parse(CsvCodec.export(src))
        assertEquals(2,parsed.size); assertEquals("P1",parsed[0].name); assertEquals(6.0,parsed[1].z,0.0)
    }
}