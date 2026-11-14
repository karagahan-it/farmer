package ir.topograde.app.guidance

import kotlin.math.sqrt

data class OrientationCalibrationResult(
    val sampleCount: Int,
    val meanRollDeg: Double,
    val meanPitchDeg: Double,
    val rollStdDevDeg: Double,
    val pitchStdDevDeg: Double,
    val stable: Boolean
)

object OrientationCalibrationEngine {
    fun analyze(
        samples: List<Orientation>,
        minimumSamples: Int = 60,
        maximumStdDevDeg: Double = 0.30
    ): OrientationCalibrationResult? {
        if (samples.size < minimumSamples || maximumStdDevDeg <= 0.0) return null
        if (samples.any { !it.rollDeg.isFinite() || !it.pitchDeg.isFinite() }) return null
        val meanRoll = samples.map { it.rollDeg }.average()
        val meanPitch = samples.map { it.pitchDeg }.average()
        val rollStd = standardDeviation(samples.map { it.rollDeg }, meanRoll)
        val pitchStd = standardDeviation(samples.map { it.pitchDeg }, meanPitch)
        return OrientationCalibrationResult(
            samples.size, meanRoll, meanPitch, rollStd, pitchStd,
            rollStd <= maximumStdDevDeg && pitchStd <= maximumStdDevDeg
        )
    }

    fun applyAsMountingZero(
        current: MountingCalibration,
        result: OrientationCalibrationResult
    ): MountingCalibration {
        require(result.stable)
        return current.copy(
            rollMountOffsetDeg = result.meanRollDeg,
            pitchMountOffsetDeg = result.meanPitchDeg
        )
    }

    private fun standardDeviation(values: List<Double>, mean: Double): Double {
        val variance = values.sumOf { (it - mean) * (it - mean) } / values.size
        return sqrt(variance)
    }
}
