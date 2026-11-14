package ir.topograde.app.grading

import ir.topograde.app.geometry.Triangle
import ir.topograde.app.model.SurveyPoint
import kotlin.math.abs

private data class P(val x:Double,val y:Double)
private data class Dp(val x:Double,val y:Double,val d:Double)

object BoundaryVolumeEngine {
    fun integrate(triangles:List<Triangle>, boundary:List<SurveyPoint>, surface:DesignSurface):VolumeSummary {
        if(boundary.size<3) return VolumeEngine.integrate(triangles,surface)
        val ears=earClip(boundary.map{P(it.x,it.y)})
        var cut=0.0; var fill=0.0; var count=0
        triangles.forEach { t ->
            val terrain=listOf(P(t.a.x,t.a.y),P(t.b.x,t.b.y),P(t.c.x,t.c.y))
            ears.forEach { ear ->
                val clipped=clipConvex(terrain,ear)
                if(clipped.size>=3){
                    val dpoly=clipped.map { p -> Dp(p.x,p.y,interpolateDelta(t,p.x,p.y,surface)) }
                    val pair=integrateSignedPolygon(dpoly)
                    cut+=pair.first; fill+=pair.second; count++
                }
            }
        }
        return VolumeSummary(cut,fill,cut-fill,count)
    }

    private fun interpolateDelta(t:Triangle,x:Double,y:Double,surface:DesignSurface):Double {
        val den=(t.b.y-t.c.y)*(t.a.x-t.c.x)+(t.c.x-t.b.x)*(t.a.y-t.c.y)
        if(abs(den)<1e-12) return 0.0
        val w1=((t.b.y-t.c.y)*(x-t.c.x)+(t.c.x-t.b.x)*(y-t.c.y))/den
        val w2=((t.c.y-t.a.y)*(x-t.c.x)+(t.a.x-t.c.x)*(y-t.c.y))/den
        val w3=1.0-w1-w2
        val z=w1*t.a.z+w2*t.b.z+w3*t.c.z
        return z-surface.designZ(x,y)
    }

    private fun integrateSignedPolygon(poly:List<Dp>):Pair<Double,Double>{
        var cut=0.0; var fill=0.0
        val a=poly[0]
        for(i in 1 until poly.size-1){
            val tri=listOf(a,poly[i],poly[i+1])
            cut+=integratePositive(clipDelta(tri,true))
            fill+=integratePositive(clipDelta(tri,false).map{it.copy(d=-it.d)})
        }
        return cut to fill
    }

    private fun clipDelta(poly:List<Dp>,positive:Boolean):List<Dp>{
        val out=mutableListOf<Dp>()
        for(i in poly.indices){
            val a=poly[i]; val b=poly[(i+1)%poly.size]
            val ai=if(positive)a.d>=0 else a.d<=0; val bi=if(positive)b.d>=0 else b.d<=0
            if(ai&&bi) out+=b
            else if(ai&&!bi) out+=zeroCross(a,b)
            else if(!ai&&bi){out+=zeroCross(a,b);out+=b}
        }
        return out
    }
    private fun zeroCross(a:Dp,b:Dp):Dp{ val t=if(abs(a.d-b.d)<1e-12)0.5 else a.d/(a.d-b.d); return Dp(a.x+t*(b.x-a.x),a.y+t*(b.y-a.y),0.0) }
    private fun integratePositive(poly:List<Dp>):Double{
        if(poly.size<3)return 0.0; var v=0.0; val a=poly[0]
        for(i in 1 until poly.size-1){val b=poly[i];val c=poly[i+1];val area=abs(cross(P(a.x,a.y),P(b.x,b.y),P(c.x,c.y)))/2.0;v+=area*(a.d+b.d+c.d)/3.0}
        return v
    }

    private fun clipConvex(subject:List<P>, clip:List<P>):List<P>{
        var output=subject
        val ccw=polygonArea(clip)>0
        for(i in clip.indices){
            val a=clip[i]; val b=clip[(i+1)%clip.size]; val input=output; output=emptyList(); if(input.isEmpty())break
            val next=mutableListOf<P>()
            for(j in input.indices){
                val p=input[j]; val q=input[(j+1)%input.size]; val pin=inside(p,a,b,ccw); val qin=inside(q,a,b,ccw)
                if(pin&&qin) next+=q
                else if(pin&&!qin) lineIntersection(p,q,a,b)?.let{next+=it}
                else if(!pin&&qin){lineIntersection(p,q,a,b)?.let{next+=it};next+=q}
            }
            output=next
        }
        return output
    }
    private fun inside(p:P,a:P,b:P,ccw:Boolean):Boolean { val c=cross(a,b,p); return if(ccw)c>=-1e-9 else c<=1e-9 }
    private fun lineIntersection(p1:P,p2:P,a:P,b:P):P?{
        val dx1=p2.x-p1.x;val dy1=p2.y-p1.y;val dx2=b.x-a.x;val dy2=b.y-a.y;val den=dx1*dy2-dy1*dx2
        if(abs(den)<1e-12)return null;val t=((a.x-p1.x)*dy2-(a.y-p1.y)*dx2)/den;return P(p1.x+t*dx1,p1.y+t*dy1)
    }

    private fun earClip(poly:List<P>):List<List<P>>{
        if(poly.size<3)return emptyList(); val verts=poly.toMutableList(); if(polygonArea(verts)<0)verts.reverse(); val out=mutableListOf<List<P>>(); var guard=0
        while(verts.size>3&&guard++<10000){
            var found=false
            for(i in verts.indices){val a=verts[(i-1+verts.size)%verts.size];val b=verts[i];val c=verts[(i+1)%verts.size];if(cross(a,b,c)<=1e-12)continue
                if(verts.indices.any{j->j!=i&&j!=(i-1+verts.size)%verts.size&&j!=(i+1)%verts.size&&pointInTriangle(verts[j],a,b,c)})continue
                out+=listOf(a,b,c);verts.removeAt(i);found=true;break}
            if(!found)break
        }
        if(verts.size==3)out+=verts.toList(); return out
    }
    private fun pointInTriangle(p:P,a:P,b:P,c:P):Boolean { val c1=cross(a,b,p);val c2=cross(b,c,p);val c3=cross(c,a,p);return c1>=-1e-9&&c2>=-1e-9&&c3>=-1e-9 }
    private fun cross(a:P,b:P,c:P)=(b.x-a.x)*(c.y-a.y)-(b.y-a.y)*(c.x-a.x)
    private fun polygonArea(p:List<P>):Double{var s=0.0;for(i in p.indices){val a=p[i];val b=p[(i+1)%p.size];s+=a.x*b.y-b.x*a.y};return s/2.0}
}