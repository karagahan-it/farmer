package ir.topograde.app.geo

import kotlin.math.*

data class CalibrationControl(val sourceX:Double,val sourceY:Double,val localX:Double,val localY:Double)
data class Similarity2D(val scale:Double,val rotationRad:Double,val tx:Double,val ty:Double){
    fun apply(x:Double,y:Double):Pair<Double,Double>{
        val c=cos(rotationRad); val s=sin(rotationRad)
        return (scale*(c*x-s*y)+tx) to (scale*(s*x+c*y)+ty)
    }
    fun inverse(localX:Double,localY:Double):Pair<Double,Double>{
        require(scale>1e-12)
        val x=(localX-tx)/scale; val y=(localY-ty)/scale
        val c=cos(rotationRad); val s=sin(rotationRad)
        return (c*x+s*y) to (-s*x+c*y)
    }
}
data class LocalSiteCalibration(val utmZone:Int,val northernHemisphere:Boolean,val transform:Similarity2D)

object LocalCalibration {
    fun fromTwoControls(a:CalibrationControl,b:CalibrationControl):Similarity2D {
        val sx=b.sourceX-a.sourceX; val sy=b.sourceY-a.sourceY
        val lx=b.localX-a.localX; val ly=b.localY-a.localY
        val sd=hypot(sx,sy); val ld=hypot(lx,ly)
        require(sd>1e-6&&ld>1e-6) { "Control points must be distinct" }
        val scale=ld/sd
        val rot=atan2(ly,lx)-atan2(sy,sx)
        val c=cos(rot); val s=sin(rot)
        val tx=a.localX-scale*(c*a.sourceX-s*a.sourceY)
        val ty=a.localY-scale*(s*a.sourceX+c*a.sourceY)
        return Similarity2D(scale,rot,tx,ty)
    }
}