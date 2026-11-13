package ir.topograde.app

import ir.topograde.app.geometry.Triangle
import ir.topograde.app.grading.ConstantElevation
import ir.topograde.app.grading.VolumeEngine
import ir.topograde.app.model.SurveyPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class VolumeEngineTest {
    @Test fun splitsMixedCutFillTriangle() {
        val t=Triangle(
            SurveyPoint(1,"A",0.0,0.0,1.0),
            SurveyPoint(2,"B",2.0,0.0,-1.0),
            SurveyPoint(3,"C",0.0,2.0,-1.0)
        )
        val v=VolumeEngine.integrate(listOf(t),ConstantElevation(0.0))
        assertEquals(1.0/6.0,v.cutVolume,1e-9)
        assertEquals(5.0/6.0,v.fillVolume,1e-9)
    }
}