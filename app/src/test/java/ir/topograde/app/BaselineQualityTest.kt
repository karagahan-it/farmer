package ir.topograde.app

import ir.topograde.app.guidance.*
import org.junit.Assert.assertEquals
import org.junit.Test

class BaselineQualityTest {
    @Test fun acceptsTwoMeterBaseline(){
        val q=BaselineQualityEngine.evaluate(GnssAntennaPoint(0.0,0.0),GnssAntennaPoint(0.0,2.0))
        assertEquals(BaselineQualityLevel.GOOD,q.level)
        assertEquals(2.0,q.lengthMeters,1e-9)
        assertEquals(0.0,q.headingDeg!!,1e-9)
    }
    @Test fun rejectsShortBaseline(){
        val q=BaselineQualityEngine.evaluate(GnssAntennaPoint(0.0,0.0),GnssAntennaPoint(0.1,0.0))
        assertEquals(BaselineQualityLevel.SHORT,q.level)
    }
}