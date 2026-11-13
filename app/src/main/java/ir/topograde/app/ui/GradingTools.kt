package ir.topograde.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.topograde.app.export.EngineeringExport
import ir.topograde.app.geometry.TerrainEngine
import ir.topograde.app.grading.*
import ir.topograde.app.model.SurveyPoint
import kotlin.math.max

@Composable
fun GradingPresetDialog(current:GradingPreset?,onDismiss:()->Unit,onSave:(GradingPreset)->Unit){
    var mode by remember { mutableStateOf(current?.mode ?: GradingMode.TARGET_ELEVATION) }
    var x by remember { mutableStateOf(current?.originX?.toString()?:"") }
    var y by remember { mutableStateOf(current?.originY?.toString()?:"") }
    var z by remember { mutableStateOf(current?.originZ?.toString()?:"") }
    var sx by remember { mutableStateOf(current?.slopeXPercent?.toString()?:"0") }
    var sy by remember { mutableStateOf(current?.slopeYPercent?.toString()?:"0") }
    var az by remember { mutableStateOf(current?.azimuthDeg?.toString()?:"0") }
    val xv=x.toDoubleOrNull(); val yv=y.toDoubleOrNull(); val zv=z.toDoubleOrNull(); val sxv=sx.toDoubleOrNull(); val syv=sy.toDoubleOrNull(); val azv=az.toDoubleOrNull()
    val valid=xv!=null&&yv!=null&&zv!=null&&sxv!=null&&syv!=null&&azv!=null
    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text("Grading Surface")},
        text={Column(Modifier.heightIn(max=540.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                GradingMode.entries.forEach { m -> FilterChip(selected=mode==m,onClick={mode=m},label={Text(when(m){GradingMode.TARGET_ELEVATION->"Target Z";GradingMode.SINGLE_SLOPE->"Single";GradingMode.DUAL_SLOPE->"Dual"})}) }
            }
            Row{OutlinedTextField(x,{x=it},label={Text("Origin X")},modifier=Modifier.weight(1f));Spacer(Modifier.width(6.dp));OutlinedTextField(y,{y=it},label={Text("Origin Y")},modifier=Modifier.weight(1f))}
            OutlinedTextField(z,{z=it},label={Text("Origin / Target Z")})
            if(mode!=GradingMode.TARGET_ELEVATION) OutlinedTextField(sx,{sx=it},label={Text(if(mode==GradingMode.SINGLE_SLOPE)"Slope %" else "Primary slope %")})
            if(mode==GradingMode.DUAL_SLOPE){
                OutlinedTextField(sy,{sy=it},label={Text("Secondary slope %")})
                OutlinedTextField(az,{az=it},label={Text("Azimuth deg")})
            }
        }},
        confirmButton={TextButton(enabled=valid,onClick={onSave(GradingPreset(mode,xv!!,yv!!,zv!!,sxv!!,syv!!,azv!!))}){Text("ذخیره")}},
        dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}}
    )
}

@Composable
fun GradingAnalysisCard(points:List<SurveyPoint>,preset:GradingPreset?,onExport:(String)->Unit){
    if(preset==null){
        ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Grading / Cut-Fill",style=MaterialTheme.typography.titleMedium);Text("سطح طراحی هنوز تعریف نشده است")}}
        return
    }
    val surface=remember(preset){preset.toSurface()}
    val triangles=remember(points){TerrainEngine.triangulate(points)}
    val heat=remember(points,preset){HeatmapEngine.classify(points,surface)}
    val volume=remember(triangles,preset){VolumeEngine.integrate(triangles,surface)}
    ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text("Grading / Cut-Fill",style=MaterialTheme.typography.titleLarge)
        Text("Mode: "+preset.mode.name+" | TIN: "+triangles.size)
        Text("Cut volume: "+"%.2f m³".format(volume.cutVolume))
        Text("Fill volume: "+"%.2f m³".format(volume.fillVolume))
        Text("Net: "+"%.2f m³".format(volume.netVolume))
        HeatmapCanvas(heat)
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){
            LegendDot(Color(0xFFB71C1C),"Deep Cut"); LegendDot(Color(0xFFFF8A80),"Cut"); LegendDot(Color(0xFFBDBDBD),"OK"); LegendDot(Color(0xFF81D4FA),"Fill"); LegendDot(Color(0xFF0D47A1),"Deep Fill")
        }
        OutlinedButton(onClick={onExport(EngineeringExport.cutFillCsv(heat,volume))},enabled=points.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text("Export Cut/Fill CSV")}
    }}
}

@Composable private fun HeatmapCanvas(heat:List<HeatPoint>){
    if(heat.isEmpty()){Box(Modifier.fillMaxWidth().height(180.dp),contentAlignment=Alignment.Center){Text("برای Heatmap نقطه‌ای وجود ندارد")};return}
    Canvas(Modifier.fillMaxWidth().height(260.dp)){
        val minX=heat.minOf{it.point.x}; val maxX=heat.maxOf{it.point.x}; val minY=heat.minOf{it.point.y}; val maxY=heat.maxOf{it.point.y}
        val dx=max(maxX-minX,1.0); val dy=max(maxY-minY,1.0); val p=16f; val w=size.width-2*p; val h=size.height-2*p
        heat.forEach { hp ->
            val x=p+((hp.point.x-minX)/dx).toFloat()*w
            val y=p+h-((hp.point.y-minY)/dy).toFloat()*h
            val c=when(hp.clazz){CutFillClass.DEEP_CUT->Color(0xFFB71C1C);CutFillClass.CUT->Color(0xFFFF8A80);CutFillClass.NEUTRAL->Color(0xFFBDBDBD);CutFillClass.FILL->Color(0xFF81D4FA);CutFillClass.DEEP_FILL->Color(0xFF0D47A1)}
            drawCircle(c,9f,Offset(x,y))
        }
    }
}

@Composable private fun LegendDot(color:Color,label:String){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){Canvas(Modifier.size(10.dp)){drawCircle(color)};Text(label,style=MaterialTheme.typography.labelSmall)}}

@Composable
fun GradingExportDialog(csv:String,onDismiss:()->Unit){
    AlertDialog(onDismissRequest=onDismiss,title={Text("Cut/Fill CSV")},text={OutlinedTextField(csv,{},readOnly=true,modifier=Modifier.fillMaxWidth().height(300.dp))},confirmButton={TextButton(onClick=onDismiss){Text("بستن")}})
}