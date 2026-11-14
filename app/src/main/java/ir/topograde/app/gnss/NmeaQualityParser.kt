package ir.topograde.app.gnss

import kotlin.math.hypot

object NmeaQualityParser {
    fun parse(sentence:String):GnssQuality? {
        val raw=sentence.trim()
        if(raw.isEmpty() || raw[0].code!=36) return null
        val p=raw.substring(1).substringBefore("*").split(",")
        return when {
            p.firstOrNull()?.endsWith("GSA")==true -> parseGsa(p)
            p.firstOrNull()?.endsWith("GST")==true -> parseGst(p)
            else -> null
        }
    }

    private fun parseGsa(p:List<String>):GnssQuality? {
        if(p.size<4) return null
        val nums=p.takeLast(3).map{it.toDoubleOrNull()}
        return GnssQuality(pdop=nums.getOrNull(0),hdop=nums.getOrNull(1),vdop=nums.getOrNull(2))
    }

    private fun parseGst(p:List<String>):GnssQuality? {
        if(p.size<9) return null
        val rms=p.getOrNull(2)?.toDoubleOrNull()
        val latSigma=p.getOrNull(6)?.toDoubleOrNull()
        val lonSigma=p.getOrNull(7)?.toDoubleOrNull()
        val altSigma=p.getOrNull(8)?.toDoubleOrNull()
        return GnssQuality(horizontalSigmaMeters=if(latSigma!=null&&lonSigma!=null) hypot(latSigma,lonSigma) else null,verticalSigmaMeters=altSigma,rmsMeters=rms)
    }
}