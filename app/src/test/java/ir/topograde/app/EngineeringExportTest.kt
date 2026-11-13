package ir.topograde.app

import ir.topograde.app.export.EngineeringExport
import ir.topograde.app.grading.ConstantElevation
import ir.topograde.app.grading.HeatmapEngine
import ir.topograde.app.grading.VolumeSummary
import ir.topograde.app.model.SurveyPoint
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineeringExportTest {
    @Test fun csvContainsSummary() {
        val heat=HeatmapEngine.classify(listOf(SurveyPoint(1,"P1",1.0,2.0,3.0)),ConstantElevation(2.5))
        val csv=EngineeringExport.cutFillCsv(heat,VolumeSummary(10.0,4.0,6.0,1))
        assertTrue(csv.contains("cut_volume,fill_volume,net_volume"))
        assertTrue(csv.contains("P1,1.0,2.0,3.0,2.5,0.5"))
    }
}