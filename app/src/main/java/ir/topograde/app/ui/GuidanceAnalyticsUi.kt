package ir.topograde.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.topograde.app.analytics.GuidanceAnalytics
import ir.topograde.app.guidance.BaselineQuality
import ir.topograde.app.guidance.BaselineQualityLevel
import ir.topograde.app.gnss.GnssConnectionState
import ir.topograde.app.logging.GuidanceLogEntry

@Composable
fun GuidanceAnalyticsCard(entries:List<GuidanceLogEntry>,baseline:BaselineQuality?,secondaryState:GnssConnectionState){
    val stats=remember(entries){GuidanceAnalytics.summarize(entries)}
    ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
        Text("Field Performance",style=MaterialTheme.typography.titleLarge)
        Text("Frames: ${stats.frames} | Mean |ΔZ|: ${"%.3f".format(stats.meanAbsDeltaM)} m | Max: ${"%.3f".format(stats.maxAbsDeltaM)} m")
        Text("On-grade ${"%.1f".format(stats.onGradePercent)}% | Cut ${"%.1f".format(stats.cutPercent)}% | Fill ${"%.1f".format(stats.fillPercent)}%")
        Text("RTK quality: Good ${"%.1f".format(stats.goodQualityPercent)}% | Caution ${"%.1f".format(stats.cautionPercent)}% | Blocked ${"%.1f".format(stats.blockedPercent)}%")
        HorizontalDivider()
        Text("Secondary GNSS: "+when(secondaryState){GnssConnectionState.Disconnected->"Disconnected";GnssConnectionState.Connecting->"Connecting";is GnssConnectionState.Connected->"Connected: "+secondaryState.description;is GnssConnectionState.Error->"Error: "+secondaryState.message})
        baseline?.let { b ->
            val label=when(b.level){BaselineQualityLevel.GOOD->"GOOD";BaselineQualityLevel.SHORT->"SHORT";BaselineQualityLevel.LONG->"LONG";BaselineQualityLevel.INVALID->"INVALID"}
            Text("Baseline: ${"%.3f".format(b.lengthMeters)} m | $label | Heading ${b.headingDeg?.let{"%.2f°".format(it)}?:"-"}")
            if(b.level!=BaselineQualityLevel.GOOD) Text(b.message,color=MaterialTheme.colorScheme.error)
        }
    }}
}