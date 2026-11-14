package ir.topograde.app

import ir.topograde.app.grading.ConstantElevation
import ir.topograde.app.grading.GuidanceEngine
import ir.topograde.app.grading.GuidanceState
import org.junit.Assert.assertEquals
import org.junit.Test

class GuidanceEngineTest {
    @Test fun classifiesCutFillAndGrade(){
        val s=ConstantElevation(100.0)
        assertEquals(GuidanceState.CUT,GuidanceEngine.evaluate(0.0,0.0,100.10,s).state)
        assertEquals(GuidanceState.FILL,GuidanceEngine.evaluate(0.0,0.0,99.90,s).state)
        assertEquals(GuidanceState.ON_GRADE,GuidanceEngine.evaluate(0.0,0.0,100.01,s).state)
    }
}