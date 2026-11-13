package ir.topograde.app

import ir.topograde.app.geometry.Triangle
import ir.topograde.app.grading.ConstantElevation
import ir.topograde.app.model.SurveyPoint
import ir.topograde.app.profile.ProfileEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProfileEngineTest {
    @Test fun samplesTinAlongLine(){
        val t=Triangle(SurveyPoint(1,"A",0.0,0.0,0.0),SurveyPoint(2,"B",10.0,0.0,10.0),SurveyPoint(3,"C",0.0,10.0,0.0))
        val p=ProfileEngine.sample(listOf(t),ConstantElevation(5.0),0.0,0.0,5.0,0.0,1.0)
        assertNotNull(p.last().existingZ)
        assertEquals(5.0,p.last().existingZ!!,1e-9)
    }
}