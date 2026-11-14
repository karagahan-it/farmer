package ir.topograde.app.geo

import kotlin.math.*

data class CalibrationResidual(val index:Int,val dx:Double,val dy:Double){ val magnitude:Double get()=hypot(dx,dy) }
data class CalibrationSolution(val transform:Similarity2D,val residuals:List<CalibrationResidual>,val rms:Double)

object MultiPointCalibration {
    fun solve(controls: List<CalibrationControl>): CalibrationSolution {
        require(controls.size >= 2) { "At least two controls are required" }
        val sx0=controls.map{it.sourceX}.average(); val sy0=controls.map{it.sourceY}.average()
        val lx0=controls.map{it.localX}.average(); val ly0=controls.map{it.localY}.average()
        var a=0.0; var b=0.0; var denom=0.0
        controls.forEach { c ->
            val sx=c.sourceX-sx0; val sy=c.sourceY-sy0; val lx=c.localX-lx0; val ly=c.localY-ly0
            a += sx*lx + sy*ly
            b += sx*ly - sy*lx
            denom += sx*sx + sy*sy
        }
        require(denom > 1e-12) { "Control geometry is degenerate" }
        val scale = hypot(a,b)/denom
        val rot = atan2(b,a)
        val c=cos(rot); val s=sin(rot)
        val tx=lx0-scale*(c*sx0-s*sy0)
        val ty=ly0-scale*(s*sx0+c*sy0)
        val t=Similarity2D(scale,rot,tx,ty)
        val residuals=controls.mapIndexed { i,ctrl ->
            val p=t.apply(ctrl.sourceX,ctrl.sourceY)
            CalibrationResidual(i,p.first-ctrl.localX,p.second-ctrl.localY)
        }
        val rms=sqrt(residuals.sumOf { it.dx*it.dx+it.dy*it.dy }/(controls.size*2.0))
        return CalibrationSolution(t,residuals,rms)
    }
}