package ir.topograde.app.analytics

import ir.topograde.app.logging.GuidanceLogEntry

data class GuidanceStats(
    val frames:Int,
    val onGradePercent:Double,
    val cutPercent:Double,
    val fillPercent:Double,
    val goodQualityPercent:Double,
    val cautionPercent:Double,
    val blockedPercent:Double,
    val meanAbsDeltaM:Double,
    val maxAbsDeltaM:Double
)

object GuidanceAnalytics {
    fun summarize(entries:List<GuidanceLogEntry>,toleranceM:Double=0.03):GuidanceStats {
        if(entries.isEmpty()) return GuidanceStats(0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)
        val n=entries.size.toDouble()
        val on=entries.count{kotlin.math.abs(it.deltaZ)<=toleranceM}
        val cut=entries.count{it.deltaZ>toleranceM}
        val fill=entries.count{it.deltaZ< -toleranceM}
        val good=entries.count{it.qualityLevel=="GOOD"}
        val caution=entries.count{it.qualityLevel=="CAUTION"}
        val blocked=entries.count{it.qualityLevel=="BLOCKED"}
        val abs=entries.map{kotlin.math.abs(it.deltaZ)}
        return GuidanceStats(entries.size,on/n*100,cut/n*100,fill/n*100,good/n*100,caution/n*100,blocked/n*100,abs.average(),abs.maxOrNull()?:0.0)
    }
}