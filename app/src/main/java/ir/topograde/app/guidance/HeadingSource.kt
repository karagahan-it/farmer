package ir.topograde.app.guidance

enum class HeadingSource { TABLET_IMU, DUAL_GNSS, MANUAL }

data class HeadingConfig(
    val source: HeadingSource = HeadingSource.TABLET_IMU,
    val manualHeadingDeg: Double = 0.0,
    val headingOffsetDeg: Double = 0.0,
    val rollZeroDeg: Double = 0.0,
    val pitchZeroDeg: Double = 0.0
)

object HeadingResolver {
    fun resolve(imu: Orientation, dualHeadingDeg: Double?, config: HeadingConfig): Orientation {
        val heading = when (config.source) {
            HeadingSource.TABLET_IMU -> imu.headingDeg
            HeadingSource.DUAL_GNSS -> dualHeadingDeg ?: imu.headingDeg
            HeadingSource.MANUAL -> config.manualHeadingDeg
        }
        return Orientation(
            headingDeg = normalize(heading + config.headingOffsetDeg),
            rollDeg = imu.rollDeg - config.rollZeroDeg,
            pitchDeg = imu.pitchDeg - config.pitchZeroDeg
        )
    }
    private fun normalize(v: Double): Double { var x=v%360.0; if(x<0)x+=360.0; return x }
}