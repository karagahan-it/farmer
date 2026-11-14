package ir.topograde.app

import ir.topograde.app.geometry.TerrainEngine
import ir.topograde.app.model.SurveyPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class TerrainEngineTest {
    @Test fun triangleFromThreePoints() {
        val p=listOf(
            SurveyPoint(1,"A",0.0,0.0,0.0),
            SurveyPoint(2,"B",10.0,0.0,10.0),
            SurveyPoint(3,"C",0.0,10.0,5.0)
        )
        assertEquals(1,TerrainEngine.triangulate(p).size)
    }
}