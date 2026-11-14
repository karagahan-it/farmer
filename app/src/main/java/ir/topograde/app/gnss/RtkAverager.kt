package ir.topograde.app.gnss

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

data class AveragedFix(
    val fix: GnssFix,
    val sampleCount: Int,
    val horizontalSpreadM: Double,
    val verticalSpreadM: Double
)

object RtkAverager {
    suspend fun average(samples: Flow<GnssFix>, durationMs: Long = 5000, minSamples: Int = 5): AveragedFix {
        val collected = mutableListOf<GnssFix>()
        val start = System.currentTimeMillis()
        withTimeout(durationMs + 2500) {
            samples.filter { it.fixType == FixType.RTK_FIXED && it.altitude != null }.first { fix ->
                collected += fix
                System.currentTimeMillis() - start >= durationMs && collected.size >= minSamples
            }
        }
        val lat = collected.map { it.latitude }.average()
        val lon = collected.map { it.longitude }.average()
        val alt = collected.mapNotNull { it.altitude }.average()
        val refLat = Math.toRadians(lat)
        fun eastM(f: GnssFix) = Math.toRadians(f.longitude - lon) * 6378137.0 * kotlin.math.cos(refLat)
        fun northM(f: GnssFix) = Math.toRadians(f.latitude - lat) * 6378137.0
        val horizontal = collected.maxOf { kotlin.math.hypot(eastM(it), northM(it)) }
        val vertical = collected.maxOf { kotlin.math.abs((it.altitude ?: alt) - alt) }
        val avgHdop = collected.mapNotNull { it.hdop }.takeIf { it.isNotEmpty() }?.average()
        val avgAge = collected.mapNotNull { it.correctionAgeSeconds }.takeIf { it.isNotEmpty() }?.average()
        return AveragedFix(
            GnssFix(lat, lon, alt, collected.last().satellites, avgHdop, FixType.RTK_FIXED, collected.last().utcTime, avgAge, collected.last().baseStationId),
            collected.size, horizontal, vertical
        )
    }
}