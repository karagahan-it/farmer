package ir.topograde.app.report

import ir.topograde.app.analytics.GuidanceAnalytics
import ir.topograde.app.logging.GuidanceLogEntry

data class SessionReport(
    val startedAt:Long?,
    val endedAt:Long?,
    val durationSeconds:Long,
    val frames:Int,
    val onGradePercent:Double,
    val cutPercent:Double,
    val fillPercent:Double,
    val goodQualityPercent:Double,
    val meanAbsDeltaM:Double,
    val maxAbsDeltaM:Double
)

object SessionReportEngine {
    fun build(entries:List<GuidanceLogEntry>):SessionReport {
        val stats=GuidanceAnalytics.summarize(entries)
        val start=entries.minOfOrNull{it.timestamp}; val end=entries.maxOfOrNull{it.timestamp}
        val dur=if(start!=null&&end!=null)((end-start)/1000L).coerceAtLeast(0) else 0L
        return SessionReport(start,end,dur,stats.frames,stats.onGradePercent,stats.cutPercent,stats.fillPercent,stats.goodQualityPercent,stats.meanAbsDeltaM,stats.maxAbsDeltaM)
    }
    fun toCsv(report:SessionReport):String = buildString {
        appendLine("metric,value")
        appendLine("started_at,${report.startedAt ?: ""}")
        appendLine("ended_at,${report.endedAt ?: ""}")
        appendLine("duration_seconds,${report.durationSeconds}")
        appendLine("frames,${report.frames}")
        appendLine("on_grade_percent,${report.onGradePercent}")
        appendLine("cut_percent,${report.cutPercent}")
        appendLine("fill_percent,${report.fillPercent}")
        appendLine("good_quality_percent,${report.goodQualityPercent}")
        appendLine("mean_abs_delta_m,${report.meanAbsDeltaM}")
        appendLine("max_abs_delta_m,${report.maxAbsDeltaM}")
    }
}