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
    fun evaluate(fix: GnssFix?, quality: GnssQuality): GuidanceQualityStatus {
        if (fix == null) return GuidanceQualityStatus(GuidanceQualityLevel.BLOCKED,listOf("No GNSS fix"))
        val reasons=mutableListOf<String>()
        var blocked=false
        if (fix.fixType != FixType.RTK_FIXED) { reasons += "RTK FIX required"; blocked=true }
        if (fix.satellites < 8) reasons += "Low satellite count"
        if ((fix.correctionAgeSeconds ?: 0.0) > 5.0) reasons += "Correction age > 5 s"
        if ((quality.horizontalSigmaMeters ?: 0.0) > 0.05) reasons += "Horizontal sigma > 5 cm"
        if ((quality.verticalSigmaMeters ?: 0.0) > 0.08) reasons += "Vertical sigma > 8 cm"
        if ((quality.pdop ?: 0.0) > 3.0) reasons += "PDOP > 3"
        return when {
            blocked -> GuidanceQualityStatus(GuidanceQualityLevel.BLOCKED,reasons)
            reasons.isNotEmpty() -> GuidanceQualityStatus(GuidanceQualityLevel.CAUTION,reasons)
            else -> GuidanceQualityStatus(GuidanceQualityLevel.GOOD,emptyList())
        }
    }
}