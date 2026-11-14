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
import ir.topograde.app.geometry.TerrainEngine
import ir.topograde.app.grading.GradingPreset
import ir.topograde.app.grading.GuidanceEngine
import ir.topograde.app.grading.GuidanceState
import ir.topograde.app.model.SurveyPoint
import ir.topograde.app.profile.ProfileEngine
import ir.topograde.app.profile.ProfileSample
import kotlin.math.hypot

@Composable
fun GuidanceCard(current:SurveyPoint?,preset:GradingPreset?) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Grader Guidance",style=MaterialTheme.typography.titleLarge)
            if(current==null || preset==null){
                Text("برای Guidance به موقعیت RTK و Grading Surface نیاز است")
            } else {
                val g=remember(current,preset){GuidanceEngine.evaluate(current.x,current.y,current.z,preset.toSurface(),0.03)}
                val stateText=when(g.state){GuidanceState.CUT->"CUT";GuidanceState.ON_GRADE->"ON GRADE";GuidanceState.FILL->"FILL"}
                val stateColor=when(g.state){GuidanceState.CUT->Color(0xFFB71C1C);GuidanceState.ON_GRADE->Color(0xFF2E7D32);GuidanceState.FILL->Color(0xFF0D47A1)}
                Text(stateText,color=stateColor,style=MaterialTheme.typography.headlineMedium)
                Text("Existing Z: "+"%.3f m".format(g.existingZ))
                Text("Target Z: "+"%.3f m".format(g.targetZ))
                Text("ΔZ: "+"%+.3f m".format(g.delta),style=MaterialTheme.typography.headlineSmall,color=stateColor)
                LinearProgressIndicator(
                    progress={((g.delta.coerceIn(-0.30,0.30)+0.30)/0.60).toFloat()},
                    modifier=Modifier.fillMaxWidth()
                )
                Text("Tolerance: ±"+"%.3f m".format(g.tolerance))
            }
        }
    }
}

@Composable
fun ProfileSectionCard(points:List<SurveyPoint>,boundary:List<SurveyPoint>,preset:GradingPreset?) {
    if(preset==null) return
    val line=when { boundary.size>=2 -> boundary.first() to boundary.last(); points.size>=2 -> points.first() to points.last(); else -> null }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("Profiles / Cross Section",style=MaterialTheme.typography.titleLarge)
            if(line==null){
                Text("حداقل دو نقطه برای ساخت پروفایل لازم است")
            } else {
                val triangles=remember(points){TerrainEngine.triangulate(points)}
                val a=line.first; val b=line.second
                val surface=remember(preset){preset.toSurface()}
                val longitudinal=remember(triangles,preset,a,b){ProfileEngine.sample(triangles,surface,a.x,a.y,b.x,b.y,1.0)}
                val mx=(a.x+b.x)/2.0; val my=(a.y+b.y)/2.0
                val dx=b.x-a.x; val dy=b.y-a.y; val len=hypot(dx,dy).coerceAtLeast(1e-9)
                val nx=-dy/len; val ny=dx/len; val half=10.0
                val cross=remember(triangles,preset,mx,my,nx,ny){ProfileEngine.sample(triangles,surface,mx-nx*half,my-ny*half,mx+nx*half,my+ny*half,0.5)}
                Text("Longitudinal profile")
                ProfileChart(longitudinal)
                Text("Cross section (20 m)")
                ProfileChart(cross)
            }
        }
    }
}

@Composable private fun ProfileChart(samples:List<ProfileSample>) {
    if(samples.isEmpty()){Box(Modifier.fillMaxWidth().height(140.dp),contentAlignment=Alignment.Center){Text("داده‌ای در TIN موجود نیست")};return}
    Canvas(Modifier.fillMaxWidth().height(180.dp)) {
        val valid=samples.filter{it.existingZ!=null}
        val zValues=samples.map{it.designZ}+valid.mapNotNull{it.existingZ}
        if(zValues.isEmpty()) return@Canvas
        val minZ=zValues.minOrNull()?:0.0; val maxZ=zValues.maxOrNull()?:minZ+1.0; val dz=(maxZ-minZ).coerceAtLeast(0.1)
        val maxD=(samples.maxOfOrNull{it.distance}?:1.0).coerceAtLeast(1.0)
        fun xy(d:Double,z:Double)=Offset((d/maxD*size.width).toFloat(),(size.height-(z-minZ)/dz*size.height).toFloat())
        samples.zipWithNext().forEach{(a,b)->drawLine(Color(0xFF1565C0),xy(a.distance,a.designZ),xy(b.distance,b.designZ),2f)}
        valid.zipWithNext().forEach{(a,b)->drawLine(Color(0xFF6D4C41),xy(a.distance,a.existingZ!!),xy(b.distance,b.existingZ!!),3f)}
    }
}