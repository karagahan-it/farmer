package ir.topograde.app.geometry

import ir.topograde.app.model.SurveyPoint
import kotlin.math.abs

data class Triangle(val a: SurveyPoint, val b: SurveyPoint, val c: SurveyPoint)
data class Segment(val x1: Double, val y1: Double, val x2: Double, val y2: Double, val elevation: Double)

object TerrainEngine {
    fun triangulate(points: List<SurveyPoint>): List<Triangle> = Delaunay.triangulate(points)

    fun contourLevels(points: List<SurveyPoint>, interval: Double): List<Double> {
        if (points.isEmpty() || interval <= 0.0) return emptyList()
        val min = points.minOf { it.z }; val max = points.maxOf { it.z }
        val start = kotlin.math.ceil(min / interval) * interval
        val out = mutableListOf<Double>(); var z = start
        while (z <= max + 1e-9) { out += z; z += interval }
        return out
    }

    fun contours(triangles: List<Triangle>, level: Double): List<Segment> = triangles.mapNotNull { t ->
        val hits = listOf(t.a to t.b, t.b to t.c, t.c to t.a).mapNotNull { (p,q) -> intersect(p,q,level) }.distinct()
        if (hits.size >= 2) Segment(hits[0].first,hits[0].second,hits[1].first,hits[1].second,level) else null
    }

    private fun intersect(a: SurveyPoint,b: SurveyPoint,z: Double): Pair<Double,Double>? {
        if ((z-a.z)*(z-b.z)>0 || abs(a.z-b.z)<1e-9) return null
        val t=(z-a.z)/(b.z-a.z); if(t !in 0.0..1.0) return null
        return (a.x+t*(b.x-a.x)) to (a.y+t*(b.y-a.y))
    }
}