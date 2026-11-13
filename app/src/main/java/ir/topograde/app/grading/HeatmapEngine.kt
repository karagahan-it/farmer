package ir.topograde.app.grading

import ir.topograde.app.model.SurveyPoint
import kotlin.math.abs

enum class CutFillClass { DEEP_CUT, CUT, NEUTRAL, FILL, DEEP_FILL }
data class HeatPoint(val point:SurveyPoint,val designZ:Double,val delta:Double,val clazz:CutFillClass)

object HeatmapEngine {
    fun classify(points:List<SurveyPoint>,surface:DesignSurface,neutralTolerance:Double=0.03,deepThreshold:Double=0.30):List<HeatPoint> = points.map { p ->
        val dz=p.z-surface.designZ(p.x,p.y)
        val c=when {
            abs(dz)<=neutralTolerance -> CutFillClass.NEUTRAL
            dz>=deepThreshold -> CutFillClass.DEEP_CUT
            dz>0 -> CutFillClass.CUT
            dz<=-deepThreshold -> CutFillClass.DEEP_FILL
            else -> CutFillClass.FILL
        }
        HeatPoint(p,surface.designZ(p.x,p.y),dz,c)
    }
}