package ir.topograde.app.gnss

import kotlin.math.abs

data class GnssTimeSyncStatus(
    val primaryMillisOfDay:Long?,
    val secondaryMillisOfDay:Long?,
    val differenceMs:Long?,
    val synchronized:Boolean
)

object GnssTimeSync {
    fun evaluate(primaryUtc:String?,secondaryUtc:String?,maxDifferenceMs:Long=250):GnssTimeSyncStatus {
        val p=parseUtcMillis(primaryUtc); val s=parseUtcMillis(secondaryUtc)
        if(p==null || s==null) return GnssTimeSyncStatus(p,s,null,false)
        var d=abs(p-s)
        val day=86_400_000L
        d=minOf(d,day-d)
        return GnssTimeSyncStatus(p,s,d,d<=maxDifferenceMs)
    }

    fun parseUtcMillis(value:String?):Long? {
        if(value.isNullOrBlank() || value.length<6)return null
        val hh=value.substring(0,2).toIntOrNull()?:return null
        val mm=value.substring(2,4).toIntOrNull()?:return null
        val sec=value.substring(4).toDoubleOrNull()?:return null
        return ((hh*3600+mm*60)*1000L+(sec*1000.0).toLong())
    }
}