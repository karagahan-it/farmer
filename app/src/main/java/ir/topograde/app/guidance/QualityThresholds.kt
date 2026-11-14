package ir.topograde.app.guidance

data class GuidanceQualityThresholds(
    val minSatellites:Int = 8,
    val maxCorrectionAgeSec:Double = 5.0,
    val maxHorizontalSigmaM:Double = 0.05,
    val maxVerticalSigmaM:Double = 0.08,
    val maxPdop:Double = 3.0
)