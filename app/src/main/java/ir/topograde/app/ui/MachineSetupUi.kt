package ir.topograde.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.topograde.app.guidance.GuidanceQualityThresholds
import ir.topograde.app.guidance.MachineGeometry
import ir.topograde.app.guidance.HeadingConfig
import ir.topograde.app.guidance.MachinePreset
import ir.topograde.app.guidance.MountingCalibration
import ir.topograde.app.guidance.Orientation

@Composable fun QualityThresholdDialog(current:GuidanceQualityThresholds,onDismiss:()->Unit,onSave:(GuidanceQualityThresholds)->Unit){
    var sat by remember{mutableStateOf(current.minSatellites.toString())};var age by remember{mutableStateOf(current.maxCorrectionAgeSec.toString())};var hs by remember{mutableStateOf(current.maxHorizontalSigmaM.toString())};var vs by remember{mutableStateOf(current.maxVerticalSigmaM.toString())};var pd by remember{mutableStateOf(current.maxPdop.toString())}
    val sv=sat.toIntOrNull();val av=age.toDoubleOrNull();val hv=hs.toDoubleOrNull();val vv=vs.toDoubleOrNull();val pv=pd.toDoubleOrNull()
    AlertDialog(onDismissRequest=onDismiss,title={Text("Guidance Quality Thresholds")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        OutlinedTextField(sat,{sat=it.filter(Char::isDigit)},label={Text("Min satellites")})
        OutlinedTextField(age,{age=it},label={Text("Max correction age (s)")})
        OutlinedTextField(hs,{hs=it},label={Text("Max horizontal sigma (m)")})
        OutlinedTextField(vs,{vs=it},label={Text("Max vertical sigma (m)")})
        OutlinedTextField(pd,{pd=it},label={Text("Max PDOP")})
    }},confirmButton={TextButton(enabled=sv!=null&&sv>0&&av!=null&&hv!=null&&vv!=null&&pv!=null,onClick={onSave(GuidanceQualityThresholds(sv!!,av!!,hv!!,vv!!,pv!!))}){Text("ذخیره")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})
}

@Composable fun MountingCalibrationDialog(current:MountingCalibration,raw:Orientation,onDismiss:()->Unit,onSave:(MountingCalibration)->Unit){
    var h by remember{mutableStateOf(current.headingMountOffsetDeg.toString())};var r by remember{mutableStateOf(current.rollMountOffsetDeg.toString())};var p by remember{mutableStateOf(current.pitchMountOffsetDeg.toString())}
    val hv=h.toDoubleOrNull();val rv=r.toDoubleOrNull();val pv=p.toDoubleOrNull()
    AlertDialog(onDismissRequest=onDismiss,title={Text("IMU Mounting Calibration")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text("Offsets describe tablet mounting relative to machine axes.")
        OutlinedTextField(h,{h=it},label={Text("Heading mount offset °")})
        OutlinedTextField(r,{r=it},label={Text("Roll mount offset °")})
        OutlinedTextField(p,{p=it},label={Text("Pitch mount offset °")})
        OutlinedButton(onClick={h=raw.headingDeg.toString();r=raw.rollDeg.toString();p=raw.pitchDeg.toString()}){Text("Capture current as machine zero")}
    }},confirmButton={TextButton(enabled=hv!=null&&rv!=null&&pv!=null,onClick={onSave(MountingCalibration(hv!!,rv!!,pv!!))}){Text("ذخیره")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})
}

@Composable fun MachinePresetDialog(saved:MachinePreset?,geometry:MachineGeometry,heading:HeadingConfig,thresholds:GuidanceQualityThresholds,onDismiss:()->Unit,onSave:(MachinePreset)->Unit,onApply:(MachinePreset)->Unit){
    var name by remember{mutableStateOf(saved?.name?:"Grader Preset")}
    AlertDialog(onDismissRequest=onDismiss,title={Text("Machine Preset")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
        OutlinedTextField(name,{name=it},label={Text("Preset name")})
        Text("Current: blade ${"%.2f".format(geometry.bladeWidthM)}m | heading ${heading.source.name} | min sat ${thresholds.minSatellites}")
        if(saved!=null){HorizontalDivider();Text("Saved: ${saved.name}");OutlinedButton(onClick={onApply(saved)},modifier=Modifier.fillMaxWidth()){Text("Apply saved preset")}}
    }},confirmButton={TextButton(enabled=name.isNotBlank(),onClick={onSave(MachinePreset(name.trim(),geometry,heading,thresholds))}){Text("Save current")}},dismissButton={TextButton(onClick=onDismiss){Text("بستن")}})
}