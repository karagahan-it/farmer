package ir.topograde.app.profile

import ir.topograde.app.geometry.Triangle
import ir.topograde.app.grading.DesignSurface
import kotlin.math.hypot

data class ProfileSample(val distance:Double,val x:Double,val y:Double,val existingZ:Double?,val designZ:Double)

object ProfileEngine {
    fun sample(triangles:List<Triangle>,surface:DesignSurface,x1:Double,y1:Double,x2:Double,y2:Double,stepMeters:Double=1.0):List<ProfileSample>{
        val len=hypot(x2-x1,y2-y1); if(len<1e-9)return emptyList(); val n=(len/stepMeters.coerceAtLeast(0.1)).toInt().coerceAtLeast(1)
        return (0..n).map { i -> val d=len*i/n;val t=d/len;val x=x1+t*(x2-x1);val y=y1+t*(y2-y1);ProfileSample(d,x,y,terrainZ(triangles,x,y),surface.designZ(x,y)) }
    }
    private fun terrainZ(triangles:List<Triangle>,x:Double,y:Double):Double?{
        for(t in triangles){val den=(t.b.y-t.c.y)*(t.a.x-t.c.x)+(t.c.x-t.b.x)*(t.a.y-t.c.y);if(kotlin.math.abs(den)<1e-12)continue
            val w1=((t.b.y-t.c.y)*(x-t.c.x)+(t.c.x-t.b.x)*(y-t.c.y))/den;val w2=((t.c.y-t.a.y)*(x-t.c.x)+(t.a.x-t.c.x)*(y-t.c.y))/den;val w3=1-w1-w2
            if(w1>=-1e-9&&w2>=-1e-9&&w3>=-1e-9)return w1*t.a.z+w2*t.b.z+w3*t.c.z
        };return null
    }
}