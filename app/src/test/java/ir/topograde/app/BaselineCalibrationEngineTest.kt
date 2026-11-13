package ir.topograde.app

import ir.topograde.app.guidance.BaselineCalibration
import ir.topograde.app.guidance.BaselineCalibrationEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BaselineCalibrationEngineTest {
    @Test fun acceptsWithinTolerance(){
        val r=BaselineCalibrationEngine.evaluate(2.04,BaselineCalibration(2.0,0.05))
        assertTrue(r.withinTolerance)
    }
    @Test fun rejectsOutsideTolerance(){
        val r=BaselineCalibrationEngine.evaluate(2.20,BaselineCalibration(2.0,0.05))
        assertFalse(r.withinTolerance)
    }
}