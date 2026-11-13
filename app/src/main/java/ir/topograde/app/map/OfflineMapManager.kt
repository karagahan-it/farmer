package ir.topograde.app.map

import android.content.Context
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import kotlin.math.cos

data class OfflineProgress(
    val completedResources: Long,
    val requiredResources: Long,
    val completedBytes: Long,
    val complete: Boolean
) {
    val percent: Int get() = if (requiredResources <= 0) 0 else ((completedResources * 100L) / requiredResources).toInt().coerceIn(0,100)
}

class OfflineMapManager(private val context: Context) {
    companion object { const val STYLE_URL = "https://demotiles.maplibre.org/style.json" }

    fun downloadAround(
        name: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 2.0,
        minZoom: Double = 10.0,
        maxZoom: Double = 17.0,
        onProgress: (OfflineProgress) -> Unit,
        onError: (String) -> Unit
    ) {
        val latDelta = radiusKm / 111.32
        val lonDelta = radiusKm / (111.32 * cos(Math.toRadians(latitude)).coerceAtLeast(0.1))
        val bounds = LatLngBounds.from(latitude + latDelta, longitude + lonDelta, latitude - latDelta, longitude - lonDelta)
        val density = context.resources.displayMetrics.density
        val definition = OfflineTilePyramidRegionDefinition(STYLE_URL, bounds, minZoom, maxZoom, density)
        val metadata = ("{\"name\":\"" + name.replace("\"","") + "\"}").toByteArray()
        OfflineManager.getInstance(context).createOfflineRegion(definition, metadata, object : OfflineManager.CreateOfflineRegionCallback {
            override fun onCreate(offlineRegion: OfflineRegion) {
                offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                    override fun onStatusChanged(status: OfflineRegionStatus) {
                        onProgress(OfflineProgress(status.completedResourceCount,status.requiredResourceCount,status.completedResourceSize,status.isComplete))
                        if (status.isComplete) offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE)
                    }
                    override fun onError(error: OfflineRegionError) { onError(error.message ?: "Offline map error") }
                    override fun mapboxTileCountLimitExceeded(limit: Long) { onError("Offline tile limit exceeded: $limit") }
                })
                offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
            }
            override fun onError(error: String) { onError(error) }
        })
    }
}