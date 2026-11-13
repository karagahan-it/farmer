package ir.topograde.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.topograde.app.guidance.HeadingConfig
import ir.topograde.app.guidance.HeadingSource
import ir.topograde.app.guidance.Orientation
import ir.topograde.app.logging.GuidanceLogEntry
import ir.topograde.app.map.MapLatLon
import ir.topograde.app.map.MapLibrePanel

@Composable
fun HeadingConfigDialog(current:HeadingConfig, imu:Orientation, onDismiss:()->Unit, onSave:(HeadingConfig)->Unit){
    var source by remember{mutableStateOf(current.source)}
    var manual by remember{mutableStateOf(current.manualHeadingDeg.toString())}
    var headingOffset by remember{mutableStateOf(current.headingOffsetDeg.toString())}
    var rollZero by remember{mutableStateOf(current.rollZeroDeg.toString())}
    var pitchZero by remember{mutableStateOf(current.pitchZeroDeg.toString())}
    val m=manual.toDoubleOrNull(); val ho=headingOffset.toDoubleOrNull(); val rz=rollZero.toDoubleOrNull(); val pz=pitchZero.toDoubleOrNull()
    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text("Heading / IMU Calibration")},
        text={Column(Modifier.heightIn(max=520.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("Heading source")
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                HeadingSource.entries.forEach { h -> FilterChip(selected=source==h,onClick={source=h},label={Text(when(h){HeadingSource.TABLET_IMU->"IMU";HeadingSource.DUAL_GNSS->"Dual GNSS";HeadingSource.MANUAL->"Manual"})}) }
            }
            if(source==HeadingSource.MANUAL) OutlinedTextField(manual,{manual=it},label={Text("Manual heading °")})
            OutlinedTextField(headingOffset,{headingOffset=it},label={Text("Heading offset °")})
            OutlinedTextField(rollZero,{rollZero=it},label={Text("Roll zero °")})
            OutlinedTextField(pitchZero,{pitchZero=it},label={Text("Pitch zero °")})
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                OutlinedButton(onClick={rollZero=imu.rollDeg.toString();pitchZero=imu.pitchDeg.toString()}){Text("Zero Roll/Pitch now")}
                OutlinedButton(onClick={headingOffset=(-imu.headingDeg).toString()}){Text("Zero Heading now")}
            }
            Text("Current IMU: H ${"%.1f".format(imu.headingDeg)}°  R ${"%+.2f".format(imu.rollDeg)}°  P ${"%+.2f".format(imu.pitchDeg)}°")
        }},
        confirmButton={TextButton(enabled=m!=null&&ho!=null&&rz!=null&&pz!=null,onClick={onSave(HeadingConfig(source,m!!,ho!!,rz!!,pz!!))}){Text("ذخیره")}},
        dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}}
    )
}

@Composable
fun GuidanceMapReplayDialog(entries:List<GuidanceLogEntry>, mapPoints:List<MapLatLon>, onDismiss:()->Unit){
    var index by remember(entries){mutableIntStateOf(0)}
    val max=(entries.size-1).coerceAtLeast(0)
    val e=entries.getOrNull(index)
    val current=mapPoints.getOrNull(index)
    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text("Guidance Replay on Map")},
        text={Column(Modifier.fillMaxWidth().heightIn(max=620.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            if(e==null || current==null) Text("Log قابل نمایش روی نقشه نیست") else {
                Text("Frame ${index+1}/${entries.size} | ΔZ ${"%+.3f m".format(e.deltaZ)} | Cross ${"%+.2f".format(e.actualCrossSlopePercent)}%")
                MapLibrePanel(current.latitude,current.longitude,track=mapPoints.take(index+1))
                Slider(value=index.toFloat(),onValueChange={index=it.toInt().coerceIn(0,max)},valueRange=0f..max.toFloat().coerceAtLeast(1f),steps=(max-1).coerceAtLeast(0))
            }
        }},
        confirmButton={TextButton(onClick=onDismiss){Text("بستن")}}
    )
}