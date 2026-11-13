package ir.topograde.app

import ir.topograde.app.guidance.BladeGeometryEngine
import ir.topograde.app.guidance.MachineGeometry
import ir.topograde.app.guidance.Orientation
import org.junit.Assert.assertEquals
import org.junit.Test

class BladeGeometryEngineTest {
    @Test fun headingNorthOffsetsBladeForwardInY(){
        val b=BladeGeometryEngine.compute(100.0,200.0,10.0,Orientation(0.0,0.0,0.0),MachineGeometry(2.5,0.0,2.0,4.0))
        assertEquals(100.0,b.centerX,1e-9);assertEquals(202.5,b.centerY,1e-9);assertEquals(8.0,b.centerZ,1e-9)
    }
    @Test fun rollProducesCrossSlope(){
        val b=BladeGeometryEngine.compute(0.0,0.0,10.0,Orientation(0.0,5.0,0.0),MachineGeometry(0.0,0.0,0.0,4.0))
        assertEquals(kotlin.math.tan(Math.toRadians(5.0))*100.0,b.crossSlopePercent,1e-9)
    }
}