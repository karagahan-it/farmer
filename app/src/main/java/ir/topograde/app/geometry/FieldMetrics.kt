package ir.topograde.app.geometry

import ir.topograde.app.model.CoordinateSystem
import ir.topograde.app.model.SurveyPoint
import kotlin.math.*

object FieldMetrics {
    fun trackLengthMeters(points: List<SurveyPoint>, crs: CoordinateSystem): Double {
        if (points.size < 2) return 0.0
        return points.zipWithNext().sumOf { (a,b) -> distanceMeters(a,b,crs) }
    }

    fun boundaryAreaSquareMeters(points: List<SurveyPoint>, crs: CoordinateSystem): Double {
        if (points.size < 3) return 0.0
        return when (crs) {
            CoordinateSystem.WGS84 -> {
                val lat0 = Math.toRadians(points.map { it.y }.average())
                val r = 6378137.0
                val projected = points.map { p ->
                    val x = Math.toRadians(p.x) * r * cos(lat0)
                    val y = Math.toRadians(p.y) * r
                    x to y
                }
                shoelace(projected)
            }
            else -> shoelace(points.map { it.x to it.y })
        }
    }

    private fun distanceMeters(a: SurveyPoint,b: SurveyPoint,crs:CoordinateSystem):Double = when(crs){
        CoordinateSystem.WGS84 -> {
            val r=6378137.0; val p1=Math.toRadians(a.y); val p2=Math.toRadians(b.y); val dp=Math.toRadians(b.y-a.y); val dl=Math.toRadians(b.x-a.x)
            val h=sin(dp/2).pow(2)+cos(p1)*cos(p2)*sin(dl/2).pow(2); 2*r*atan2(sqrt(h),sqrt(1-h))
        }
        else -> hypot(b.x-a.x,b.y-a.y)
    }

    private fun shoelace(p:List<Pair<Double,Double>>):Double {
        var s=0.0
        for(i in p.indices){ val a=p[i]; val b=p[(i+1)%p.size]; s += a.first*b.second-b.first*a.second }
        return abs(s)/2.0
    }
}