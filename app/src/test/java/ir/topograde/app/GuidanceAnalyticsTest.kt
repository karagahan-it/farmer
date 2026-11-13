package ir.topograde.app

import ir.topograde.app.analytics.GuidanceAnalytics
import ir.topograde.app.logging.GuidanceLogEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class GuidanceAnalyticsTest {
    private fun e(d:Double,q:String)=GuidanceLogEntry(1,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,d,0.0,0.0,q)
    @Test fun summarizesPerformance(){
        val s=GuidanceAnalytics.summarize(listOf(e(0.0,"GOOD"),e(0.10,"GOOD"),e(-0.10,"CAUTION"),e(0.01,"BLOCKED")))
        assertEquals(50.0,s.onGradePercent,1e-9)
        assertEquals(25.0,s.cutPercent,1e-9)
        assertEquals(25.0,s.fillPercent,1e-9)
        assertEquals(50.0,s.goodQualityPercent,1e-9)
    }
}