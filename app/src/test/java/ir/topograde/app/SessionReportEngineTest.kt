package ir.topograde.app

import ir.topograde.app.logging.GuidanceLogEntry
import ir.topograde.app.report.SessionReportEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionReportEngineTest {
    private fun e(t:Long,d:Double,q:String)=GuidanceLogEntry(t,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,d,0.0,0.0,q)
    @Test fun buildsSessionSummary(){
        val r=SessionReportEngine.build(listOf(e(1000,0.0,"GOOD"),e(3000,0.10,"GOOD"),e(5000,-0.10,"CAUTION")))
        assertEquals(4L,r.durationSeconds)
        assertEquals(3,r.frames)
        assertTrue(r.onGradePercent>30.0)
        assertTrue(SessionReportEngine.toCsv(r).contains("mean_abs_delta_m"))
    }
}