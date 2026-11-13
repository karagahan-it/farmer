package ir.topograde.app.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.topograde.app.grading.GradingPreset
import ir.topograde.app.grading.GuidanceEngine
import ir.topograde.app.grading.GuidanceState
import ir.topograde.app.guidance.BladeGeometryEngine
import ir.topograde.app.guidance.CrossSlopeGuidance
import ir.topograde.app.guidance.MachineGeometry
import ir.topograde.app.guidance.Orientation
import ir.topograde.app.logging.GuidanceLogEntry
import ir.topograde.app.model.SurveyPoint

@Composable
fun FullScreenGuidance(
    antennaPoint:SurveyPoint?,
    orientation:Orientation,
    geometry:MachineGeometry,
    preset:GradingPreset?,
    recording:Boolean,
    logCount:Int,
    imuAvailable:Boolean,
    onClose:()->Unit,
    onToggleRecording:()->Unit,
    onOpenGeometry:()->Unit,
    onReplay:()->Unit
) {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(Unit) {
        val old = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { if (activity != null && old != null) activity.requestedOrientation = old }
    }
    Dialog(onDismissRequest=onClose,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(Modifier.fillMaxSize(),color=Color(0xFF101418)) {
            if(antennaPoint==null || preset==null){
                Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
                    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)){
                        Text("GUIDANCE",style=MaterialTheme.typography.headlineLarge,color=Color.White)
                        Text("RTK position و Grading Surface لازم است",color=Color.White)
                        Button(onClick=onClose){Text("بازگشت")}
                    }
                }
            } else {
                val surface=preset.toSurface()
                val blade=remember(antennaPoint,orientation,geometry){BladeGeometryEngine.compute(antennaPoint.x,antennaPoint.y,antennaPoint.z,orientation,geometry)}
                val guidance=remember(blade,preset){GuidanceEngine.evaluate(blade.centerX,blade.centerY,blade.centerZ,surface,0.03)}
                val cross=remember(blade,orientation,geometry,preset){CrossSlopeGuidance.evaluate(blade,orientation,geometry,surface)}
                val stateColor=when(guidance.state){GuidanceState.CUT->Color(0xFFE53935);GuidanceState.ON_GRADE->Color(0xFF43A047);GuidanceState.FILL->Color(0xFF1E88E5)}
                BoxWithConstraints(Modifier.fillMaxSize().padding(18.dp)){
                    val wide=maxWidth>maxHeight
                    if(wide){
                        Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(18.dp)){
                            GuidancePrimaryPanel(guidance.delta,guidance.targetZ,guidance.existingZ,stateColor,guidance.state.name,Modifier.weight(1.4f))
                            GuidanceSecondaryPanel(orientation,blade.crossSlopePercent,cross.targetPercent,cross.errorPercent,cross.leftDeltaM,cross.rightDeltaM,recording,logCount,imuAvailable,onToggleRecording,onOpenGeometry,onReplay,onClose,Modifier.weight(1f))
                        }
                    } else {
                        Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(14.dp)){
                            GuidancePrimaryPanel(guidance.delta,guidance.targetZ,guidance.existingZ,stateColor,guidance.state.name,Modifier.weight(1f))
                            GuidanceSecondaryPanel(orientation,blade.crossSlopePercent,cross.targetPercent,cross.errorPercent,cross.leftDeltaM,cross.rightDeltaM,recording,logCount,imuAvailable,onToggleRecording,onOpenGeometry,onReplay,onClose,Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun GuidancePrimaryPanel(delta:Double,targetZ:Double,bladeZ:Double,stateColor:Color,state:String,modifier:Modifier){
    ElevatedCard(modifier.fillMaxHeight()){
        Column(Modifier.fillMaxSize().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceEvenly){
            Text(state,style=MaterialTheme.typography.headlineLarge,color=stateColor)
            Text("%+.3f m".format(delta),style=MaterialTheme.typography.displayLarge,color=stateColor)
            LinearProgressIndicator(progress={((delta.coerceIn(-0.30,0.30)+0.30)/0.60).toFloat()},modifier=Modifier.fillMaxWidth().height(16.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
                ValueBlock("BLADE Z","%.3f".format(bladeZ))
                ValueBlock("TARGET Z","%.3f".format(targetZ))
                ValueBlock("TOL","±0.030")
            }
        }
    }
}

@Composable private fun GuidanceSecondaryPanel(
    orientation:Orientation,actualSlope:Double,targetSlope:Double,errorSlope:Double,leftDelta:Double,rightDelta:Double,
    recording:Boolean,logCount:Int,imuAvailable:Boolean,onToggleRecording:()->Unit,onOpenGeometry:()->Unit,onReplay:()->Unit,onClose:()->Unit,modifier:Modifier
){
    ElevatedCard(modifier.fillMaxHeight()){
        Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            Text("BLADE / IMU",style=MaterialTheme.typography.titleLarge)
            Text("Heading ${"%.1f".format(orientation.headingDeg)}°   Roll ${"%+.2f".format(orientation.rollDeg)}°   Pitch ${"%+.2f".format(orientation.pitchDeg)}°")
            Text(if(imuAvailable)"Tablet IMU active" else "IMU unavailable")
            HorizontalDivider()
            Text("Cross slope",style=MaterialTheme.typography.titleMedium)
            Text("Actual ${"%+.2f".format(actualSlope)}%   Target ${"%+.2f".format(targetSlope)}%",style=MaterialTheme.typography.headlineSmall)
            Text("Error ${"%+.2f".format(errorSlope)}%")
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){ValueBlock("LEFT Δ","%+.3f m".format(leftDelta));ValueBlock("RIGHT Δ","%+.3f m".format(rightDelta))}
            Spacer(Modifier.weight(1f))
            Text("Log frames: $logCount")
            Button(onClick=onToggleRecording,modifier=Modifier.fillMaxWidth()){Text(if(recording)"Stop Logging" else "Start Logging")}
            OutlinedButton(onClick=onReplay,modifier=Modifier.fillMaxWidth()){Text("Replay")}
            OutlinedButton(onClick=onOpenGeometry,modifier=Modifier.fillMaxWidth()){Text("Machine Geometry")}
            TextButton(onClick=onClose,modifier=Modifier.fillMaxWidth()){Text("Close Guidance")}
        }
    }
}

@Composable private fun ValueBlock(label:String,value:String){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(label,style=MaterialTheme.typography.labelMedium);Text(value,style=MaterialTheme.typography.titleLarge)}}

@Composable
fun MachineGeometryDialog(current:MachineGeometry,onDismiss:()->Unit,onSave:(MachineGeometry)->Unit){
    var forward by remember{mutableStateOf(current.antennaToBladeForwardM.toString())}
    var right by remember{mutableStateOf(current.antennaToBladeRightM.toString())}
    var down by remember{mutableStateOf(current.antennaToBladeDownM.toString())}
    var width by remember{mutableStateOf(current.bladeWidthM.toString())}
    val f=forward.toDoubleOrNull();val r=right.toDoubleOrNull();val d=down.toDoubleOrNull();val w=width.toDoubleOrNull()
    AlertDialog(onDismissRequest=onDismiss,title={Text("Machine Geometry")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        OutlinedTextField(forward,{forward=it},label={Text("Antenna → Blade Forward (m)")})
        OutlinedTextField(right,{right=it},label={Text("Antenna → Blade Right (m)")})
        OutlinedTextField(down,{down=it},label={Text("Antenna → Blade Down (m)")})
        OutlinedTextField(width,{width=it},label={Text("Blade Width (m)")})
    }},confirmButton={TextButton(enabled=f!=null&&r!=null&&d!=null&&w!=null&&w>0,onClick={onSave(MachineGeometry(f!!,r!!,d!!,w!!))}){Text("ذخیره")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})
}

@Composable
fun GuidanceReplayDialog(entries:List<GuidanceLogEntry>,onDismiss:()->Unit){
    var index by remember(entries){mutableIntStateOf(0)}
    val max=(entries.size-1).coerceAtLeast(0)
    val e=entries.getOrNull(index)
    AlertDialog(onDismissRequest=onDismiss,title={Text("Guidance Replay")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
        if(e==null) Text("Log موجود نیست") else {
            Text("Frame ${index+1}/${entries.size}")
            Slider(value=index.toFloat(),onValueChange={index=it.toInt().coerceIn(0,max)},valueRange=0f..max.toFloat().coerceAtLeast(1f),steps=(max-1).coerceAtLeast(0))
            Text("ΔZ ${"%+.3f m".format(e.deltaZ)} | Blade ${"%.3f".format(e.bladeCenterZ)} | Target ${"%.3f".format(e.targetZ)}")
            Text("Heading ${"%.1f".format(e.headingDeg)}° | Roll ${"%+.2f".format(e.rollDeg)}° | Pitch ${"%+.2f".format(e.pitchDeg)}°")
            Text("Cross slope actual ${"%+.2f".format(e.actualCrossSlopePercent)}% / target ${"%+.2f".format(e.targetCrossSlopePercent)}%")
        }
    }},confirmButton={TextButton(onClick=onDismiss){Text("بستن")}})
}

private tailrec fun Context.findActivity(): Activity? = when(this){
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
