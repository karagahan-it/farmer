package ir.topograde.app.guidance

data class GuidanceAlert(
    val severity: AlertSeverity,
    val message: String
)
enum class AlertSeverity { INFO, WARNING, CRITICAL }

object GuidanceAlertEngine {
    fun evaluate(deltaZ: Double?, crossSlopeErrorPercent: Double?, quality: GuidanceQualityStatus): List<GuidanceAlert> {
        val out=mutableListOf<GuidanceAlert>()
        when (quality.level) {
            GuidanceQualityLevel.BLOCKED -> out += GuidanceAlert(AlertSeverity.CRITICAL,"Guidance blocked: "+quality.reasons.joinToString("; "))
            GuidanceQualityLevel.CAUTION -> out += GuidanceAlert(AlertSeverity.WARNING,"GNSS caution: "+quality.reasons.joinToString("; "))
            GuidanceQualityLevel.GOOD -> Unit
        }
        if (deltaZ != null && kotlin.math.abs(deltaZ) > 0.15) out += GuidanceAlert(AlertSeverity.WARNING,"Elevation error > 15 cm")
        if (crossSlopeErrorPercent != null && kotlin.math.abs(crossSlopeErrorPercent) > 1.0) out += GuidanceAlert(AlertSeverity.WARNING,"Cross-slope error > 1%")
        return out
    }
}