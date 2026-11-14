package ir.topograde.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.topograde.app.guidance.BaselineCalibration
import ir.topograde.app.guidance.MachinePreset
import ir.topograde.app.report.SessionReport
import ir.topograde.app.report.SessionReportEngine

@Composable fun BaselineCalibrationDialog(current:BaselineCalibration,onDismiss:()->Unit,onSave:(BaselineCalibration)->Unit){
    var nominal by remember{mutableStateOf(current.nominalLengthM.toString())}
    var tol by remember{mutableStateOf(current.maxDeviationM.toString())}
    val n=nominal.toDoubleOrNull(); val t=tol.toDoubleOrNull()
    AlertDialog(onDismissRequest=onDismiss,title={Text("Dual GNSS Baseline Calibration")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        OutlinedTextField(nominal,{nominal=it},label={Text("Nominal baseline length (m)")})
        OutlinedTextField(tol,{tol=it},label={Text("Allowed deviation (m)")})
        Text("Primary antenna = rear, Secondary antenna = front.")
    }},confirmButton={TextButton(enabled=n!=null&&n>0&&t!=null&&t>=0,onClick={onSave(BaselineCalibration(n!!,t!!))}){Text("ذخیره")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})
}

@Composable fun SessionReportDialog(report:SessionReport,onDismiss:()->Unit,onExport:(String)->Unit){
    AlertDialog(onDismissRequest=onDismiss,title={Text("Session Report")},text={Column(verticalArrangement=Arrangement.spacedBy(7.dp)){
        Text("Duration: ${report.durationSeconds}s | Frames: ${report.frames}")
        Text("On-grade ${"%.1f".format(report.onGradePercent)}% | Cut ${"%.1f".format(report.cutPercent)}% | Fill ${"%.1f".format(report.fillPercent)}%")
        Text("Good RTK quality: ${"%.1f".format(report.goodQualityPercent)}%")
        Text("Mean |ΔZ| ${"%.3f".format(report.meanAbsDeltaM)} m | Max ${"%.3f".format(report.maxAbsDeltaM)} m")
        OutlinedButton(onClick={onExport(SessionReportEngine.toCsv(report))},modifier=Modifier.fillMaxWidth()){Text("Export report CSV")}
    }},confirmButton={TextButton(onClick=onDismiss){Text("بستن")}})
}

@Composable fun MachinePresetLibraryDialog(presets:List<MachinePreset>,onDismiss:()->Unit,onApply:(MachinePreset)->Unit,onDelete:(MachinePreset)->Unit){
    AlertDialog(onDismissRequest=onDismiss,title={Text("Machine Preset Library")},text={
        if(presets.isEmpty()) Text("Preset ذخیره‌شده‌ای وجود ندارد") else LazyColumn(Modifier.heightIn(max=420.dp)){items(presets,key={it.name}){p->
            ListItem(headlineContent={Text(p.name)},supportingContent={Text("Blade ${"%.2f".format(p.geometry.bladeWidthM)}m | ${p.headingConfig.source.name} | minSat ${p.qualityThresholds.minSatellites}")},trailingContent={Row{TextButton(onClick={onApply(p)}){Text("Apply")};TextButton(onClick={onDelete(p)}){Text("Delete")}}})
        }}
    },confirmButton={TextButton(onClick=onDismiss){Text("بستن")}})
}

@Composable fun ReportCsvDialog(csv:String,onDismiss:()->Unit){
    AlertDialog(onDismissRequest=onDismiss,title={Text("Session Report CSV")},text={OutlinedTextField(csv,{},readOnly=true,modifier=Modifier.fillMaxWidth().height(300.dp))},confirmButton={TextButton(onClick=onDismiss){Text("بستن")}})
}