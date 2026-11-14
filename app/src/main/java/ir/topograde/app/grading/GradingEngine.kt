package ir.topograde.app.grading

import ir.topograde.app.model.SurveyPoint
import kotlin.math.abs

data class DesignPlane(
    val originX: Double,
    val originY: Double,
    val originZ: Double,
    val slopeXPercent: Double = 0.0,
    val slopeYPercent: Double = 0.0
) {
    fun designZ(x:Double,y:Double):Double = originZ + (x-originX)*(slopeXPercent/100.0) + (y-originY)*(slopeYPercent/100.0)
}

data class CutFillSample(val point:SurveyPoint,val designZ:Double,val delta:Double){
    val cutMeters:Double get()=if(delta>0) delta else 0.0
    val fillMeters:Double get()=if(delta<0) -delta else 0.0
}
data class CutFillSummary(val samples:List<CutFillSample>,val avgCut:Double,val avgFill:Double,val maxAbsDelta:Double)

object GradingEngine {
    fun evaluate(points:List<SurveyPoint>,plane:DesignPlane):CutFillSummary {
        val samples=points.map { p -> val dz=plane.designZ(p.x,p.y); CutFillSample(p,dz,p.z-dz) }
        val cuts=samples.filter{it.cutMeters>0}.map{it.cutMeters}
        val fills=samples.filter{it.fillMeters>0}.map{it.fillMeters}
        return CutFillSummary(
            samples,
            if(cuts.isEmpty())0.0 else cuts.average(),
            if(fills.isEmpty())0.0 else fills.average(),
            samples.maxOfOrNull{abs(it.delta)}?:0.0
        )
    }
}