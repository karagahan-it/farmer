package ir.topograde.app.geo

import kotlin.math.*

data class UtmCoordinate(
    val easting: Double,
    val northing: Double,
    val zone: Int,
    val northernHemisphere: Boolean
)

data class Wgs84Coordinate(
    val latitude: Double,
    val longitude: Double
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

    fun utmToWgs84(easting: Double, northing: Double, zone: Int, northernHemisphere: Boolean = true): Wgs84Coordinate {
        require(zone in 1..60)
        val a = 6378137.0
        val f = 1.0 / 298.257223563
        val k0 = 0.9996
        val e = sqrt(f * (2.0 - f))
        val e1 = (1.0 - sqrt(1.0 - e*e)) / (1.0 + sqrt(1.0 - e*e))
        val x = easting - 500000.0
        var y = northing
        if (!northernHemisphere) y -= 10000000.0
        val m = y / k0
        val mu = m / (a * (1.0 - e*e/4.0 - 3.0*e.pow(4)/64.0 - 5.0*e.pow(6)/256.0))
        val j1 = 3.0*e1/2.0 - 27.0*e1.pow(3)/32.0
        val j2 = 21.0*e1*e1/16.0 - 55.0*e1.pow(4)/32.0
        val j3 = 151.0*e1.pow(3)/96.0
        val j4 = 1097.0*e1.pow(4)/512.0
        val fp = mu + j1*sin(2*mu) + j2*sin(4*mu) + j3*sin(6*mu) + j4*sin(8*mu)
        val e2p = e*e/(1.0-e*e)
        val c1 = e2p*cos(fp).pow(2)
        val t1 = tan(fp).pow(2)
        val n1 = a/sqrt(1.0-e*e*sin(fp).pow(2))
        val r1 = a*(1.0-e*e)/(1.0-e*e*sin(fp).pow(2)).pow(1.5)
        val d = x/(n1*k0)
        val lat = fp - (n1*tan(fp)/r1) * (d*d/2.0 - (5.0+3.0*t1+10.0*c1-4.0*c1*c1-9.0*e2p)*d.pow(4)/24.0 + (61.0+90.0*t1+298.0*c1+45.0*t1*t1-252.0*e2p-3.0*c1*c1)*d.pow(6)/720.0)
        val lon0 = Math.toRadians((zone - 1) * 6 - 180 + 3.0)
        val lon = lon0 + (d - (1.0+2.0*t1+c1)*d.pow(3)/6.0 + (5.0-2.0*c1+28.0*t1-3.0*c1*c1+8.0*e2p+24.0*t1*t1)*d.pow(5)/120.0)/cos(fp)
        return Wgs84Coordinate(Math.toDegrees(lat), Math.toDegrees(lon))
    }
}