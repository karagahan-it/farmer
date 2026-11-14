package ir.topograde.app.survey

enum class HeightInputType { GGA_ORTHOMETRIC, ELLIPSOIDAL }

data class HeightSettings(
    val antennaHeightMeters: Double = 0.0,
    val inputType: HeightInputType = HeightInputType.GGA_ORTHOMETRIC,
    val geoidUndulationMeters: Double = 0.0
)

object HeightCorrection {
    fun groundOrthometricHeight(rawHeight: Double, settings: HeightSettings): Double {
        val orthometric = when (settings.inputType) {
            HeightInputType.GGA_ORTHOMETRIC -> rawHeight
            HeightInputType.ELLIPSOIDAL -> rawHeight - settings.geoidUndulationMeters
        }
        return orthometric - settings.antennaHeightMeters
    }
}