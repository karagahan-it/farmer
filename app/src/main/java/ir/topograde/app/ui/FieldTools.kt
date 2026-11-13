package ir.topograde.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.topograde.app.grading.DesignPlane
import ir.topograde.app.grading.GradingEngine
import ir.topograde.app.map.OfflineMapManager
import ir.topograde.app.map.OfflinePack
import ir.topograde.app.model.SurveyPoint
import ir.topograde.app.survey.HeightInputType
import ir.topograde.app.survey.HeightSettings

@Composable
fun HeightSettingsDialog(current: HeightSettings, onDismiss: () -> Unit, onSave: (HeightSettings) -> Unit) {
    var antenna by remember { mutableStateOf(current.antennaHeightMeters.toString()) }
    var geoid by remember { mutableStateOf(current.geoidUndulationMeters.toString()) }
    var input by remember { mutableStateOf(current.inputType) }
    val a=antenna.toDoubleOrNull(); val g=geoid.toDoubleOrNull()
    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text("ارتفاع آنتن / Geoid")},
        text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedTextField(antenna,{antenna=it},label={Text("Antenna / Pole Height (m)")})
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                FilterChip(selected=input==HeightInputType.GGA_ORTHOMETRIC,onClick={input=HeightInputType.GGA_ORTHOMETRIC},label={Text("GGA MSL")})
                FilterChip(selected=input==HeightInputType.ELLIPSOIDAL,onClick={input=HeightInputType.ELLIPSOIDAL},label={Text("Ellipsoidal")})
            }
            if(input==HeightInputType.ELLIPSOIDAL) OutlinedTextField(geoid,{geoid=it},label={Text("Geoid undulation N (m)")})
        }},
        confirmButton={TextButton(enabled=a!=null && (input==HeightInputType.GGA_ORTHOMETRIC || g!=null),onClick={onSave(HeightSettings(a!!,input,g?:0.0))}){Text("ذخیره")}},
        dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}}
    )
}

@Composable
fun DesignPlaneDialog(current: DesignPlane?, onDismiss: () -> Unit, onSave: (DesignPlane) -> Unit) {
    var x by remember { mutableStateOf(current?.originX?.toString()?:"") }
    var y by remember { mutableStateOf(current?.originY?.toString()?:"") }
    var z by remember { mutableStateOf(current?.originZ?.toString()?:"") }
    var sx by remember { mutableStateOf(current?.slopeXPercent?.toString()?:"0") }
    var sy by remember { mutableStateOf(current?.slopeYPercent?.toString()?:"0") }
    val xv=x.toDoubleOrNull();val yv=y.toDoubleOrNull();val zv=z.toDoubleOrNull();val sxv=sx.toDoubleOrNull();val syv=sy.toDoubleOrNull()
    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text("Design Plane / Cut-Fill")},
        text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
            Row{OutlinedTextField(x,{x=it},label={Text("Origin X")},modifier=Modifier.weight(1f));Spacer(Modifier.width(6.dp));OutlinedTextField(y,{y=it},label={Text("Origin Y")},modifier=Modifier.weight(1f))}
            OutlinedTextField(z,{z=it},label={Text("Design Z at origin")})
            Row{OutlinedTextField(sx,{sx=it},label={Text("Slope X %")},modifier=Modifier.weight(1f));Spacer(Modifier.width(6.dp));OutlinedTextField(sy,{sy=it},label={Text("Slope Y %")},modifier=Modifier.weight(1f))}
        }},
        confirmButton={TextButton(enabled=xv!=null&&yv!=null&&zv!=null&&sxv!=null&&syv!=null,onClick={onSave(DesignPlane(xv!!,yv!!,zv!!,sxv!!,syv!!))}){Text("ذخیره")}},
        dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}}
    )
}

@Composable
fun CutFillCard(points: List<SurveyPoint>, plane: DesignPlane?) {
    if(plane==null) return
    val summary=remember(points,plane){GradingEngine.evaluate(points,plane)}
    ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){
        Text("Cut / Fill",style=MaterialTheme.typography.titleMedium)
        Text("Avg Cut: "+"%.3f m".format(summary.avgCut))
        Text("Avg Fill: "+"%.3f m".format(summary.avgFill))
        Text("Max |ΔZ|: "+"%.3f m".format(summary.maxAbsDelta))
    }}
}

@Composable
fun OfflinePackManagerDialog(manager: OfflineMapManager, onDismiss: () -> Unit) {
    var packs by remember { mutableStateOf<List<OfflinePack>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    fun refresh(){ manager.listPacks({packs=it},{error=it}) }
    LaunchedEffect(Unit){ refresh() }
    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text("Offline Map Packs")},
        text={Column(Modifier.heightIn(max=520.dp)){
            error?.let{Text(it)}
            if(packs.isEmpty()) Text("Pack آفلاینی وجود ندارد") else LazyColumn{items(packs,key={it.id}){p->
                ListItem(
                    headlineContent={Text(p.name)},
                    supportingContent={Text((if(p.complete)"Complete" else p.percent.toString()+"%")+" | "+(p.completedBytes/1024/1024)+" MB")},
                    trailingContent={Row{
                        TextButton(onClick={manager.setPackActive(p.id,!p.active,{refresh()},{error=it})}){Text(if(p.active)"Pause" else "Resume")}
                        TextButton(onClick={manager.deletePack(p.id,{refresh()},{error=it})}){Text("Delete")}
                    }}
                )
            }}
        }},
        confirmButton={TextButton(onClick=onDismiss){Text("بستن")}}
    )
}