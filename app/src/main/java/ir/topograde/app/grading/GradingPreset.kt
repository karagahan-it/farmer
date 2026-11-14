package ir.topograde.app.grading

enum class GradingMode { TARGET_ELEVATION, SINGLE_SLOPE, DUAL_SLOPE }

data class GradingPreset(
    val mode:GradingMode,
    val originX:Double,
    val originY:Double,
    val originZ:Double,
    val slopeXPercent:Double=0.0,
    val slopeYPercent:Double=0.0,
    val azimuthDeg:Double=0.0
) {
    fun toSurface():DesignSurface = when(mode){
        GradingMode.TARGET_ELEVATION -> ConstantElevation(originZ)
        GradingMode.SINGLE_SLOPE -> PlaneSurface(originX,originY,originZ,slopeXPercent,0.0)
        GradingMode.DUAL_SLOPE -> DualSlopeSurface(originX,originY,originZ,slopeXPercent,slopeYPercent,azimuthDeg)
    }
}