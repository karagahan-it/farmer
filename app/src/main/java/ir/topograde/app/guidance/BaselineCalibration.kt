package ir.topograde.app.guidance

data class BaselineCalibration(
    val nominalLengthM:Double=2.0,
    val maxDeviationM:Double=0.10
)

data class BaselineDeviation(
    val measuredM:Double,
    val nominalM:Double,
    val deviationM:Double,
    val withinTolerance:Boolean
)

object BaselineCalibrationEngine {
    fun evaluate(measuredM:Double, calibration:BaselineCalibration):BaselineDeviation {
        val d=measuredM-calibration.nominalLengthM
        return BaselineDeviation(measuredM,calibration.nominalLengthM,d,kotlin.math.abs(d)<=calibration.maxDeviationM)
    }
}