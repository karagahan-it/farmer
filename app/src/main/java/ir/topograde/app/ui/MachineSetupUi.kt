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
import ir.topograde.app.guidance.OrientationCalibrationEngine
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.launch

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

@Composable fun MountingCalibrationDialog(
    current: MountingCalibration,
    raw: Orientation,
    samples: SharedFlow<Orientation>,
    onDismiss: () -> Unit,
    onSave: (MountingCalibration) -> Unit
) {
    var h by remember { mutableStateOf(current.headingMountOffsetDeg.toString()) }
    var r by remember { mutableStateOf(current.rollMountOffsetDeg.toString()) }
    var p by remember { mutableStateOf(current.pitchMountOffsetDeg.toString()) }
    var collecting by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("برای کالیبراسیون، ماشین را روی سطح صاف متوقف و تبلت را روی پایه محکم ثابت کنید.") }
    val scope = rememberCoroutineScope()
    val hv = h.toDoubleOrNull()
    val rv = r.toDoubleOrNull()
    val pv = p.toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("کالیبراسیون شیب‌سنج تبلت") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ماشین باید ثابت و روی سطح صاف باشد. هنگام اندازه‌گیری حرکت نکنید.")
                OutlinedButton(
                    enabled = !collecting,
                    onClick = {
                        scope.launch {
                            collecting = true
                            resultText = "در حال اندازه‌گیری؛ لطفاً تبلت و ماشین را تکان ندهید…"
                            val captured = withTimeoutOrNull(4000L) { samples.take(60).toList() }.orEmpty()
                            val result = OrientationCalibrationEngine.analyze(captured)
                            if (result == null) {
                                resultText = "نمونه کافی دریافت نشد. دوباره تلاش کنید."
                            } else if (!result.stable) {
                                resultText = "ماشین حرکت دارد یا سطح ثابت نیست. دوباره روی زمین صاف اندازه بگیرید."
                            } else {
                                r = result.meanRollDeg.toString()
                                p = result.meanPitchDeg.toString()
                                resultText = "کالیبراسیون پایدار: ${result.sampleCount} نمونه | نوسان Roll ${"%.2f".format(result.rollStdDevDeg)}° | Pitch ${"%.2f".format(result.pitchStdDevDeg)}°"
                            }
                            collecting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (collecting) "در حال اندازه‌گیری…" else "اندازه‌گیری خودکار صفر شیب") }
                Text(resultText, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(h, { h = it }, label = { Text("اصلاح جهت (درجه)") })
                OutlinedTextField(r, { r = it }, label = { Text("صفر Roll (درجه)") })
                OutlinedTextField(p, { p = it }, label = { Text("صفر Pitch (درجه)") })
                Text("جهت مطلق را با دو GPS تنظیم کنید؛ این کالیبراسیون فقط Roll و Pitch را صفر می‌کند.")
                Text("مقدار فعلی: H ${"%.1f".format(raw.headingDeg)}°  R ${"%+.2f".format(raw.rollDeg)}°  P ${"%+.2f".format(raw.pitchDeg)}°")
            }
        },
        confirmButton = {
            TextButton(enabled = hv != null && rv != null && pv != null && !collecting, onClick = {
                onSave(MountingCalibration(hv!!, rv!!, pv!!))
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("بستن") } }
    )
}

@Composable fun MachinePresetDialog(saved:MachinePreset?,geometry:MachineGeometry,heading:HeadingConfig,thresholds:GuidanceQualityThresholds,onDismiss:()->Unit,onSave:(MachinePreset)->Unit,onApply:(MachinePreset)->Unit){
    var name by remember{mutableStateOf(saved?.name?:"Grader Preset")}
    AlertDialog(onDismissRequest=onDismiss,title={Text("Machine Preset")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
        OutlinedTextField(name,{name=it},label={Text("Preset name")})
        Text("Current: blade ${"%.2f".format(geometry.bladeWidthM)}m | heading ${heading.source.name} | min sat ${thresholds.minSatellites}")
        if(saved!=null){HorizontalDivider();Text("Saved: ${saved.name}");OutlinedButton(onClick={onApply(saved)},modifier=Modifier.fillMaxWidth()){Text("Apply saved preset")}}
    }},confirmButton={TextButton(enabled=name.isNotBlank(),onClick={onSave(MachinePreset(name.trim(),geometry,heading,thresholds))}){Text("Save current")}},dismissButton={TextButton(onClick=onDismiss){Text("بستن")}})
}