package ir.topograde.app.guidance

data class MachineGeometry(
    val antennaToBladeForwardM:Double=2.5,
    val antennaToBladeRightM:Double=0.0,
    val antennaToBladeDownM:Double=2.0,
    val bladeWidthM:Double=3.7
)

data class Orientation(
    val headingDeg:Double=0.0,
    val rollDeg:Double=0.0,
    val pitchDeg:Double=0.0
)

data class BladePose(
    val centerX:Double,
    val centerY:Double,
    val centerZ:Double,
    val leftZ:Double,
    val rightZ:Double,
    val crossSlopePercent:Double
)