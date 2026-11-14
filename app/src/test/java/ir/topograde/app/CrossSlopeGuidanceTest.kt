package ir.topograde.app

import ir.topograde.app.grading.PlaneSurface
import ir.topograde.app.guidance.*
import org.junit.Assert.assertEquals
import org.junit.Test

class CrossSlopeGuidanceTest {
    @Test fun flatBladeAgainstTwoPercentCrossSlope(){
        val geom=MachineGeometry(0.0,0.0,0.0,4.0)
        val blade=BladeGeometryEngine.compute(0.0,0.0,100.0,Orientation(0.0,0.0,0.0),geom)
        val g=CrossSlopeGuidance.evaluate(blade,Orientation(0.0,0.0,0.0),geom,PlaneSurface(0.0,0.0,100.0,2.0,0.0))
        assertEquals(2.0,g.targetPercent,1e-9)
        assertEquals(-2.0,g.errorPercent,1e-9)
    }
}