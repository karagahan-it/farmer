package ir.topograde.app.geometry

import ir.topograde.app.model.SurveyPoint
import kotlin.math.abs

data class Edge(val a: SurveyPoint, val b: SurveyPoint)

object Delaunay {
    fun triangulate(input: List<SurveyPoint>): List<Triangle> {
        if (input.size < 3) return emptyList()
        val minX = input.minOf { it.x }; val maxX = input.maxOf { it.x }
        val minY = input.minOf { it.y }; val maxY = input.maxOf { it.y }
        val d = maxOf(maxX - minX, maxY - minY).coerceAtLeast(1.0)
        val cx = (minX + maxX) / 2.0; val cy = (minY + maxY) / 2.0
        val s1 = SurveyPoint(-1, "__s1", cx - 20*d, cy - d, 0.0)
        val s2 = SurveyPoint(-2, "__s2", cx, cy + 20*d, 0.0)
        val s3 = SurveyPoint(-3, "__s3", cx + 20*d, cy - d, 0.0)
        var tris = mutableListOf(Triangle(s1,s2,s3))
        for (p in input) {
            val bad = tris.filter { inCircumcircle(p, it) }
            val edges = mutableListOf<Edge>()
            bad.forEach { t -> listOf(Edge(t.a,t.b),Edge(t.b,t.c),Edge(t.c,t.a)).forEach { e ->
                val idx = edges.indexOfFirst { sameEdge(it,e) }
                if (idx >= 0) edges.removeAt(idx) else edges.add(e)
            } }
            tris.removeAll(bad.toSet())
            edges.forEach { e -> tris.add(Triangle(e.a,e.b,p)) }
        }
        return tris.filter { t -> t.a.id >= 0 && t.b.id >= 0 && t.c.id >= 0 }
    }

    private fun sameEdge(e1: Edge, e2: Edge) =
        (e1.a.id == e2.a.id && e1.b.id == e2.b.id) || (e1.a.id == e2.b.id && e1.b.id == e2.a.id)

    private fun inCircumcircle(p: SurveyPoint, t: Triangle): Boolean {
        val ax = t.a.x - p.x; val ay = t.a.y - p.y
        val bx = t.b.x - p.x; val by = t.b.y - p.y
        val cx = t.c.x - p.x; val cy = t.c.y - p.y
        val det = (ax*ax + ay*ay) * (bx*cy - cx*by) -
            (bx*bx + by*by) * (ax*cy - cx*ay) +
            (cx*cx + cy*cy) * (ax*by - bx*ay)
        val orient = (t.b.x-t.a.x)*(t.c.y-t.a.y) - (t.b.y-t.a.y)*(t.c.x-t.a.x)
        if (abs(orient) < 1e-12) return false
        return if (orient > 0) det > 0 else det < 0
    }
}