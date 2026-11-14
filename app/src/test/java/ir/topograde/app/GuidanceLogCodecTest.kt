package ir.topograde.app

import ir.topograde.app.logging.GuidanceLogCodec
import ir.topograde.app.logging.GuidanceLogEntry
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidanceLogCodecTest {
    @Test fun exportsCsv(){
        val csv=GuidanceLogCodec.toCsv(listOf(GuidanceLogEntry(1,1.0,2.0,3.0,10.0,1.0,2.0,2.5,2.4,0.1,1.2,1.0)))
        assertTrue(csv.contains("actual_cross_slope_pct"))
        assertTrue(csv.contains("1,1.0,2.0,3.0"))
    }
}