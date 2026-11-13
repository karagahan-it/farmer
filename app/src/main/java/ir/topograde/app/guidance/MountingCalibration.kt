package ir.topograde.app.guidance

data class MountingCalibration(
    val headingMountOffsetDeg:Double=0.0,
    val rollMountOffsetDeg:Double=0.0,
    val pitchMountOffsetDeg:Double=0.0
)

object MountingCalibrationEngine {
    fun apply(raw:Orientation, calibration:MountingCalibration):Orientation = Orientation(
        headingDeg=((raw.headingDeg-calibration.headingMountOffsetDeg)%360.0+360.0)%360.0,
        rollDeg=raw.rollDeg-calibration.rollMountOffsetDeg,
        pitchDeg=raw.pitchDeg-calibration.pitchMountOffsetDeg
    )
}