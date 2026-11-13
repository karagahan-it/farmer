package ir.topograde.app

import ir.topograde.app.grading.DesignPlane
import ir.topograde.app.grading.GradingEngine
import ir.topograde.app.model.SurveyPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class GradingEngineTest {
    @Test fun evaluatesCutAndFill() {
        val plane=DesignPlane(0.0,0.0,10.0,0.0,0.0)
        val s=GradingEngine.evaluate(listOf(SurveyPoint(1,"A",0.0,0.0,11.0),SurveyPoint(2,"B",1.0,0.0,9.0)),plane)
        assertEquals(1.0,s.avgCut,1e-9); assertEquals(1.0,s.avgFill,1e-9); assertEquals(1.0,s.maxAbsDelta,1e-9)
    }
}