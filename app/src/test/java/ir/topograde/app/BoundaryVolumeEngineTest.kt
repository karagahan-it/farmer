package ir.topograde.app

import ir.topograde.app.geometry.Triangle
import ir.topograde.app.grading.BoundaryVolumeEngine
import ir.topograde.app.grading.ConstantElevation
import ir.topograde.app.model.SurveyPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class BoundaryVolumeEngineTest {
    @Test fun clipsVolumeToSquareBoundary() {
        val t=Triangle(SurveyPoint(1,"A",0.0,0.0,1.0),SurveyPoint(2,"B",10.0,0.0,1.0),SurveyPoint(3,"C",0.0,10.0,1.0))
        val boundary=listOf(SurveyPoint(10,"1",0.0,0.0,0.0),SurveyPoint(11,"2",5.0,0.0,0.0),SurveyPoint(12,"3",5.0,5.0,0.0),SurveyPoint(13,"4",0.0,5.0,0.0))
        val v=BoundaryVolumeEngine.integrate(listOf(t),boundary,ConstantElevation(0.0))
        assertEquals(25.0,v.cutVolume,1e-6)
        assertEquals(0.0,v.fillVolume,1e-9)
    }
}