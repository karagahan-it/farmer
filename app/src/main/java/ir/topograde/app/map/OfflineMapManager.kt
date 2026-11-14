package ir.topograde.app.map

import android.content.Context
import org.json.JSONObject
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

    private val manager get() = OfflineManager.getInstance(context)

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
        val metadata = JSONObject().put("name", name).toString().toByteArray()
        manager.createOfflineRegion(definition, metadata, object : OfflineManager.CreateOfflineRegionCallback {
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

    fun listPacks(onResult:(List<OfflinePack>)->Unit,onError:(String)->Unit) {
        manager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                val regions = offlineRegions.orEmpty()
                if (regions.isEmpty()) { onResult(emptyList()); return }
                val result = MutableList<OfflinePack?>(regions.size) { null }
                var remaining = regions.size
                regions.forEachIndexed { index, region ->
                    region.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
                        override fun onStatus(status: OfflineRegionStatus?) {
                            if (status == null) { result[index] = OfflinePack(region.id, readName(region), false, false, 0, 0, 0); remaining--; if (remaining == 0) onResult(result.filterNotNull().sortedBy { it.name }); return }
                            result[index] = OfflinePack(
                                id = region.id,
                                name = readName(region),
                                active = status.downloadState == OfflineRegion.STATE_ACTIVE,
                                complete = status.isComplete,
                                completedResources = status.completedResourceCount,
                                requiredResources = status.requiredResourceCount,
                                completedBytes = status.completedResourceSize
                            )
                            remaining--
                            if (remaining == 0) onResult(result.filterNotNull().sortedBy { it.name })
                        }
                        override fun onError(error: String?) {
                            result[index] = OfflinePack(region.id, readName(region), false, false, 0, 0, 0)
                            remaining--
                            if (remaining == 0) onResult(result.filterNotNull().sortedBy { it.name })
                        }
                    })
                }
            }
            override fun onError(error: String) { onError(error) }
        })
    }

    fun setPackActive(id:Long,active:Boolean,onDone:()->Unit,onError:(String)->Unit) {
        manager.getOfflineRegion(id, object : OfflineManager.GetOfflineRegionCallback {
            override fun onRegion(offlineRegion: OfflineRegion) {
                offlineRegion.setDownloadState(if(active) OfflineRegion.STATE_ACTIVE else OfflineRegion.STATE_INACTIVE)
                onDone()
            }
            override fun onRegionNotFound() { onError("Offline pack not found") }
            override fun onError(error: String) { onError(error) }
        })
    }

    fun deletePack(id:Long,onDone:()->Unit,onError:(String)->Unit) {
        manager.getOfflineRegion(id, object : OfflineManager.GetOfflineRegionCallback {
            override fun onRegion(offlineRegion: OfflineRegion) {
                offlineRegion.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                    override fun onDelete() { onDone() }
                    override fun onError(error: String) { onError(error) }
                })
            }
            override fun onRegionNotFound() { onError("Offline pack not found") }
            override fun onError(error: String) { onError(error) }
        })
    }

    private fun readName(region:OfflineRegion):String = runCatching {
        JSONObject(region.metadata.toString(Charsets.UTF_8)).optString("name","Pack "+region.id)
    }.getOrDefault("Pack "+region.id)
}