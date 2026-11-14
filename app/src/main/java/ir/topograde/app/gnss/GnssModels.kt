package ir.topograde.app.gnss

enum class FixType(val label: String) {
    NONE("NO FIX"),
    GPS("GPS"),
    DGPS("DGPS"),
    RTK_FIXED("RTK FIX"),
    RTK_FLOAT("RTK FLOAT"),
    ESTIMATED("ESTIMATED")
}

data class GnssFix(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val satellites: Int,
    val hdop: Double?,
    val fixType: FixType,
    val utcTime: String? = null,
    val correctionAgeSeconds: Double? = null,
    val baseStationId: String? = null
)