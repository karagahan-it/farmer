package ir.topograde.app.grading

import ir.topograde.app.geometry.Triangle
import kotlin.math.abs

data class VolumeSummary(
    val cutVolume:Double,
    val fillVolume:Double,
    val netVolume:Double,
    val triangleCount:Int
)

private data class V(val x:Double,val y:Double,val d:Double)

object VolumeEngine {
    fun integrate(triangles:List<Triangle>,surface:DesignSurface):VolumeSummary {
        var cut=0.0
        var fill=0.0
        triangles.forEach { t ->
            val poly=listOf(
                V(t.a.x,t.a.y,t.a.z-surface.designZ(t.a.x,t.a.y)),
                V(t.b.x,t.b.y,t.b.z-surface.designZ(t.b.x,t.b.y)),
                V(t.c.x,t.c.y,t.c.z-surface.designZ(t.c.x,t.c.y))
            )
            cut += integratePositive(clip(poly,true))
            fill += integratePositive(clip(poly,false).map { it.copy(d=-it.d) })
        }
        return VolumeSummary(cut,fill,cut-fill,triangles.size)
    }

    private fun clip(poly:List<V>,keepPositive:Boolean):List<V> {
        if(poly.isEmpty()) return emptyList()
        val out=mutableListOf<V>()
        for(i in poly.indices){
            val a=poly[i]; val b=poly[(i+1)%poly.size]
            val aIn=if(keepPositive) a.d>=0 else a.d<=0
            val bIn=if(keepPositive) b.d>=0 else b.d<=0
            if(aIn && bIn){ out+=b }
            else if(aIn && !bIn){ out+=intersection(a,b) }
            else if(!aIn && bIn){ out+=intersection(a,b); out+=b }
        }
        return out
    }

    private fun intersection(a:V,b:V):V {
        val t=if(abs(a.d-b.d)<1e-12) 0.5 else a.d/(a.d-b.d)
        return V(a.x+t*(b.x-a.x),a.y+t*(b.y-a.y),0.0)
    }

    private fun integratePositive(poly:List<V>):Double {
        if(poly.size<3) return 0.0
        var volume=0.0
        val a=poly[0]
        for(i in 1 until poly.size-1){
            val b=poly[i]; val c=poly[i+1]
            val area=abs((b.x-a.x)*(c.y-a.y)-(b.y-a.y)*(c.x-a.x))/2.0
            volume += area*(a.d+b.d+c.d)/3.0
        }
        return volume
    }
}