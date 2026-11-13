package ir.topograde.app

import ir.topograde.app.geometry.FieldMetrics
import ir.topograde.app.model.CoordinateSystem
import ir.topograde.app.model.SurveyPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class FieldMetricsTest {
    @Test fun projectedTrackAndArea() {
        val track=listOf(SurveyPoint(1,"A",0.0,0.0,0.0),SurveyPoint(2,"B",3.0,4.0,0.0))
        assertEquals(5.0,FieldMetrics.trackLengthMeters(track,CoordinateSystem.LOCAL),1e-9)
        val square=listOf(SurveyPoint(1,"A",0.0,0.0,0.0),SurveyPoint(2,"B",10.0,0.0,0.0),SurveyPoint(3,"C",10.0,10.0,0.0),SurveyPoint(4,"D",0.0,10.0,0.0))
        assertEquals(100.0,FieldMetrics.boundaryAreaSquareMeters(square,CoordinateSystem.LOCAL),1e-9)
    }
}