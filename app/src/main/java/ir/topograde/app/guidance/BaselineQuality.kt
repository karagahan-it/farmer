package ir.topograde.app.guidance

import kotlin.math.*

enum class BaselineQualityLevel { INVALID, SHORT, GOOD, LONG }
data class BaselineQuality(
    val lengthMeters:Double,
    val headingDeg:Double?,
    val level:BaselineQualityLevel,
    val message:String
)

object BaselineQualityEngine {
    fun evaluate(rear:GnssAntennaPoint?,front:GnssAntennaPoint?,minLengthM:Double=0.5,maxLengthM:Double=10.0):BaselineQuality {
        if(rear==null || front==null) return BaselineQuality(0.0,null,BaselineQualityLevel.INVALID,"Secondary GNSS unavailable")
        val dx=front.x-rear.x; val dy=front.y-rear.y; val len=hypot(dx,dy)
        if(len<1e-6) return BaselineQuality(len,null,BaselineQualityLevel.INVALID,"Baseline invalid")
        val heading=DualGnssHeading.headingDeg(rear,front)
        return when {
            len<minLengthM -> BaselineQuality(len,heading,BaselineQualityLevel.SHORT,"Baseline shorter than %.2f m".format(minLengthM))
            len>maxLengthM -> BaselineQuality(len,heading,BaselineQualityLevel.LONG,"Baseline longer than %.2f m".format(maxLengthM))
            else -> BaselineQuality(len,heading,BaselineQualityLevel.GOOD,"Baseline OK")
        }
    }
}