package ir.topograde.app.guidance

import kotlin.math.*

data class GnssAntennaPoint(val x: Double, val y: Double)

object DualGnssHeading {
    fun headingDeg(rear: GnssAntennaPoint, front: GnssAntennaPoint): Double {
        val dx = front.x - rear.x
        val dy = front.y - rear.y
        require(hypot(dx,dy) > 1e-6) { "GNSS antenna baseline too short" }
        var deg = Math.toDegrees(atan2(dx, dy))
        if (deg < 0) deg += 360.0
        return deg
    }
}