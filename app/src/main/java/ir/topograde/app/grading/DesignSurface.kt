package ir.topograde.app.grading

import ir.topograde.app.model.SurveyPoint

sealed interface DesignSurface {
    fun designZ(x:Double,y:Double):Double
}

data class ConstantElevation(val elevation:Double):DesignSurface {
    override fun designZ(x:Double,y:Double)=elevation
}

data class PlaneSurface(
    val originX:Double,
    val originY:Double,
    val originZ:Double,
    val slopeXPercent:Double=0.0,
    val slopeYPercent:Double=0.0
):DesignSurface {
    override fun designZ(x:Double,y:Double):Double = originZ + (x-originX)*(slopeXPercent/100.0) + (y-originY)*(slopeYPercent/100.0)
}

data class DualSlopeSurface(
    val originX:Double,
    val originY:Double,
    val originZ:Double,
    val primarySlopePercent:Double,
    val secondarySlopePercent:Double,
    val azimuthDeg:Double
):DesignSurface {
    override fun designZ(x:Double,y:Double):Double {
        val a=Math.toRadians(azimuthDeg)
        val dx=x-originX; val dy=y-originY
        val along=dx*kotlin.math.sin(a)+dy*kotlin.math.cos(a)
        val cross=dx*kotlin.math.cos(a)-dy*kotlin.math.sin(a)
        return originZ + along*(primarySlopePercent/100.0) + cross*(secondarySlopePercent/100.0)
    }
}

data class SurfaceSample(val source:SurveyPoint,val designZ:Double,val delta:Double)