package ir.topograde.app.geo

import kotlin.math.*

data class UtmCoordinate(
    val easting: Double,
    val northing: Double,
    val zone: Int,
    val northernHemisphere: Boolean
)

object CoordinateConverter {
    fun wgs84ToUtm(latitude: Double, longitude: Double, forcedZone: Int? = null): UtmCoordinate {
        require(latitude in -80.0..84.0) { "UTM supports latitude -80..84" }
        val zone = forcedZone ?: (((longitude + 180.0) / 6.0).toInt() + 1).coerceIn(1, 60)
        val a = 6378137.0
        val f = 1.0 / 298.257223563
        val k0 = 0.9996
        val e2 = f * (2.0 - f)
        val ep2 = e2 / (1.0 - e2)
        val lat = Math.toRadians(latitude)
        val lon = Math.toRadians(longitude)
        val lon0 = Math.toRadians((zone - 1) * 6 - 180 + 3.0)
        val sinLat = sin(lat); val cosLat = cos(lat); val tanLat = tan(lat)
        val n = a / sqrt(1.0 - e2 * sinLat * sinLat)
        val t = tanLat * tanLat
        val c = ep2 * cosLat * cosLat
        val aa = cosLat * (lon - lon0)
        val e4 = e2 * e2; val e6 = e4 * e2
        val m = a * (
            (1 - e2/4 - 3*e4/64 - 5*e6/256) * lat
            - (3*e2/8 + 3*e4/32 + 45*e6/1024) * sin(2*lat)
            + (15*e4/256 + 45*e6/1024) * sin(4*lat)
            - (35*e6/3072) * sin(6*lat)
        )
        val easting = k0 * n * (aa + (1-t+c)*aa.pow(3)/6 + (5-18*t+t*t+72*c-58*ep2)*aa.pow(5)/120) + 500000.0
        var northing = k0 * (m + n*tanLat*(aa*aa/2 + (5-t+9*c+4*c*c)*aa.pow(4)/24 + (61-58*t+t*t+600*c-330*ep2)*aa.pow(6)/720))
        val north = latitude >= 0.0
        if (!north) northing += 10000000.0
        return UtmCoordinate(easting, northing, zone, north)
    }
}