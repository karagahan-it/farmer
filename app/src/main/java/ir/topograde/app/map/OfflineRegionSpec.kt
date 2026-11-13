package ir.topograde.app.map

data class OfflineRegionSpec(
    val name: String,
    val minLatitude: Double,
    val minLongitude: Double,
    val maxLatitude: Double,
    val maxLongitude: Double,
    val minZoom: Double = 8.0,
    val maxZoom: Double = 18.0
)