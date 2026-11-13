package ir.topograde.app.geometry

import ir.topograde.app.model.SurveyPoint
import kotlin.math.abs

data class Triangle(val a: SurveyPoint, val b: SurveyPoint, val c: SurveyPoint)
data class Segment(val x1: Double, val y1: Double, val x2: Double, val y2: Double, val elevation: Double)

object TerrainEngine {
 fun fanTriangulate(points: List<SurveyPoint>): List<Triangle> = if (points.size < 3) emptyList() else points.drop(2).mapIndexed { i, p -> Triangle(points[0], points[i+1], p) }
 fun contours(triangles: List<Triangle>, level: Double): List<Segment> = triangles.mapNotNull { t ->
   val hits = listOf(t.a to t.b, t.b to t.c, t.c to t.a).mapNotNull { (p,q) -> intersect(p,q,level) }
   if (hits.size >= 2) Segment(hits[0].first,hits[0].second,hits[1].first,hits[1].second,level) else null
 }
 private fun intersect(a: SurveyPoint,b: SurveyPoint,z: Double): Pair<Double,Double>? {
   if ((z-a.z)*(z-b.z)>0 || abs(a.z-b.z)<1e-9) return null
   val t=(z-a.z)/(b.z-a.z); if(t !in 0.0..1.0) return null
   return (a.x+t*(b.x-a.x)) to (a.y+t*(b.y-a.y))
 }
}
