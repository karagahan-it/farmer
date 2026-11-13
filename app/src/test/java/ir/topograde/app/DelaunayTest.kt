package ir.topograde.app

import ir.topograde.app.geometry.Delaunay
import ir.topograde.app.model.SurveyPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class DelaunayTest {
    @Test fun squareCreatesTwoTriangles() {
        val p = listOf(
            SurveyPoint(1,"A",0.0,0.0,0.0),
            SurveyPoint(2,"B",10.0,0.0,1.0),
            SurveyPoint(3,"C",10.0,10.0,2.0),
            SurveyPoint(4,"D",0.0,10.0,1.0)
        )
        assertEquals(2, Delaunay.triangulate(p).size)
    }
}