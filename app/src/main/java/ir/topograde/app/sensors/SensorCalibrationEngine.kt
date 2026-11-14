package ir.topograde.app.sensors

data class LinearSensorCalibration(
    val scale: Double,
    val offset: Double
) {
    init {
        require(scale.isFinite() && scale != 0.0)
        require(offset.isFinite())
    }

    fun convert(rawValue: Double): Double {
        require(rawValue.isFinite())
        return rawValue * scale + offset
    }
}

object SensorCalibrationEngine {
    fun fromTwoReferencePoints(
        rawPoint1: Double,
        knownValue1: Double,
        rawPoint2: Double,
        knownValue2: Double
    ): LinearSensorCalibration {
        require(rawPoint1.isFinite() && rawPoint2.isFinite())
        require(knownValue1.isFinite() && knownValue2.isFinite())
        require(rawPoint1 != rawPoint2) { "Calibration reference readings must be different" }
        val scale = (knownValue2 - knownValue1) / (rawPoint2 - rawPoint1)
        require(scale.isFinite() && scale != 0.0) { "Invalid calibration scale" }
        val offset = knownValue1 - rawPoint1 * scale
        return LinearSensorCalibration(scale, offset)
    }
}
