package ir.topograde.app.guidance

import ir.topograde.app.grading.DesignSurface
import kotlin.math.*

data class CrossSlopeStatus(
    val actualPercent:Double,
    val targetPercent:Double,
    val errorPercent:Double,
    val leftDeltaM:Double,
    val rightDeltaM:Double
)

object CrossSlopeGuidance {
    fun evaluate(blade:BladePose,orientation:Orientation,geometry:MachineGeometry,surface:DesignSurface):CrossSlopeStatus {
        val h=Math.toRadians(orientation.headingDeg)
        val half=geometry.bladeWidthM/2.0
        val lx=blade.centerX-half*cos(h); val ly=blade.centerY+half*sin(h)
        val rx=blade.centerX+half*cos(h); val ry=blade.centerY-half*sin(h)
        val lTarget=surface.designZ(lx,ly); val rTarget=surface.designZ(rx,ry)
        val target=if(geometry.bladeWidthM<=1e-9)0.0 else (rTarget-lTarget)/geometry.bladeWidthM*100.0
        return CrossSlopeStatus(
            actualPercent=blade.crossSlopePercent,
            targetPercent=target,
            errorPercent=blade.crossSlopePercent-target,
            leftDeltaM=blade.leftZ-lTarget,
            rightDeltaM=blade.rightZ-rTarget
        )
    }
}