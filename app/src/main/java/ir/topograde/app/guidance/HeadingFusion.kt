package ir.topograde.app.guidance

import kotlin.math.*

data class HeadingFusionConfig(
    val gnssWeight:Double=0.85,
    val maxInnovationDeg:Double=45.0
)

object HeadingFusion {
    fun fuse(imuHeadingDeg:Double,gnssHeadingDeg:Double?,config:HeadingFusionConfig=HeadingFusionConfig()):Double {
        if(gnssHeadingDeg==null) return normalize(imuHeadingDeg)
        val innovation=wrap180(gnssHeadingDeg-imuHeadingDeg)
        if(abs(innovation)>config.maxInnovationDeg) return normalize(gnssHeadingDeg)
        return normalize(imuHeadingDeg + innovation*config.gnssWeight.coerceIn(0.0,1.0))
    }
    private fun wrap180(v:Double):Double { var x=(v+180.0)%360.0; if(x<0)x+=360.0; return x-180.0 }
    private fun normalize(v:Double):Double { var x=v%360.0; if(x<0)x+=360.0; return x }
}