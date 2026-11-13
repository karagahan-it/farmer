package ir.topograde.app.logging

data class GuidanceLogEntry(
    val timestamp:Long,
    val x:Double,
    val y:Double,
    val antennaZ:Double,
    val headingDeg:Double,
    val rollDeg:Double,
    val pitchDeg:Double,
    val bladeCenterZ:Double,
    val targetZ:Double,
    val deltaZ:Double,
    val actualCrossSlopePercent:Double,
    val targetCrossSlopePercent:Double
)

object GuidanceLogCodec {
    fun toCsv(entries:List<GuidanceLogEntry>):String = buildString {
        appendLine("timestamp,x,y,antenna_z,heading_deg,roll_deg,pitch_deg,blade_z,target_z,delta_z,actual_cross_slope_pct,target_cross_slope_pct")
        entries.forEach { e -> appendLine("${e.timestamp},${e.x},${e.y},${e.antennaZ},${e.headingDeg},${e.rollDeg},${e.pitchDeg},${e.bladeCenterZ},${e.targetZ},${e.deltaZ},${e.actualCrossSlopePercent},${e.targetCrossSlopePercent}") }
    }
}