package ir.topograde.app.guidance

import ir.topograde.app.gnss.FixType
import ir.topograde.app.gnss.GnssFix
import ir.topograde.app.gnss.GnssQuality

enum class GuidanceQualityLevel { BLOCKED, CAUTION, GOOD }

data class GuidanceQualityStatus(
    val level: GuidanceQualityLevel,
    val reasons: List<String>
)

object GuidanceQualityGate {
    fun evaluate(fix: GnssFix?, quality: GnssQuality, thresholds:GuidanceQualityThresholds = GuidanceQualityThresholds()): GuidanceQualityStatus {
        if (fix == null) return GuidanceQualityStatus(GuidanceQualityLevel.BLOCKED,listOf("No GNSS fix"))
        val reasons=mutableListOf<String>()
        var blocked=false
        if (fix.fixType != FixType.RTK_FIXED) { reasons += "RTK FIX required"; blocked=true }
        if (fix.satellites < thresholds.minSatellites) reasons += "Satellites < ${thresholds.minSatellites}"
        if ((fix.correctionAgeSeconds ?: 0.0) > thresholds.maxCorrectionAgeSec) reasons += "Correction age > ${thresholds.maxCorrectionAgeSec}s"
        if ((quality.horizontalSigmaMeters ?: 0.0) > thresholds.maxHorizontalSigmaM) reasons += "Horizontal sigma high"
        if ((quality.verticalSigmaMeters ?: 0.0) > thresholds.maxVerticalSigmaM) reasons += "Vertical sigma high"
        if ((quality.pdop ?: 0.0) > thresholds.maxPdop) reasons += "PDOP high"
        return when {
            blocked -> GuidanceQualityStatus(GuidanceQualityLevel.BLOCKED,reasons)
            reasons.isNotEmpty() -> GuidanceQualityStatus(GuidanceQualityLevel.CAUTION,reasons)
            else -> GuidanceQualityStatus(GuidanceQualityLevel.GOOD,emptyList())
        }
    }
}