package ir.topograde.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ir.topograde.app.data.CsvCodec
import ir.topograde.app.data.ProjectStore
import ir.topograde.app.geometry.TerrainEngine
import ir.topograde.app.model.CoordinateSystem
import ir.topograde.app.model.Project
import ir.topograde.app.model.SurveyPoint
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = ProjectStore(applicationContext)
        setContent { MaterialTheme { TopoGradeApp(store) } }
    }
}

private enum class Screen { Projects, Survey }

@Composable private fun TopoGradeApp(store: ProjectStore) {
    var screen by remember { mutableStateOf(Screen.Projects) }
    var selected by remember { mutableStateOf<Project?>(null) }
    when (screen) {
        Screen.Projects -> ProjectsScreen(store) { selected = it; screen = Screen.Survey }
        Screen.Survey -> selected?.let { SurveyScreen(store, it) { screen = Screen.Projects } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun ProjectsScreen(store: ProjectStore, onOpen: (Project) -> Unit) {
    var projects by remember { mutableStateOf(store.getProjects()) }
    var showCreate by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("TopoGrade | پروژه‌ها") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showCreate = true }) { Text("+") } }
    ) { pad ->
        if (projects.isEmpty()) Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) { Text("هنوز پروژه‌ای ساخته نشده است") }
        else LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(projects, key = { it.id }) { p ->
                ElevatedCard(Modifier.fillMaxWidth().clickable { onOpen(p) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(p.name, style = MaterialTheme.typography.titleMedium)
                        Text("${p.coordinateSystem.title}${p.utmZone?.let { " | Zone $it" } ?: ""}")
                    }
                }
            }
        }
    }
    if (showCreate) CreateProjectDialog(
        onDismiss = { showCreate = false },
        onCreate = { name, crs, zone -> store.createProject(name, crs, zone); projects = store.getProjects(); showCreate = false }
    )
}

@Composable private fun CreateProjectDialog(onDismiss: () -> Unit, onCreate: (String, CoordinateSystem, Int?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var crs by remember { mutableStateOf(CoordinateSystem.LOCAL) }
    var zone by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پروژه جدید") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("نام پروژه") }, singleLine = true)
            Text("سیستم مختصات")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CoordinateSystem.entries.forEach { item -> FilterChip(selected = crs == item, onClick = { crs = item }, label = { Text(item.name) }) }
            }
            if (crs == CoordinateSystem.UTM) OutlinedTextField(zone, { zone = it.filter(Char::isDigit).take(2) }, label = { Text("UTM Zone (1-60)") }, singleLine = true)
        } },
        confirmButton = { TextButton(enabled = name.isNotBlank() && (crs != CoordinateSystem.UTM || (zone.toIntOrNull()?.let { it in 1..60 } == true)), onClick = { onCreate(name.trim(), crs, zone.toIntOrNull()) }) { Text("ساخت") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SurveyScreen(store: ProjectStore, project: Project, onBack: () -> Unit) {
    var points by remember(project.id) { mutableStateOf(store.getPoints(project.id)) }
    var addDialog by remember { mutableStateOf(false) }
    var editPoint by remember { mutableStateOf<SurveyPoint?>(null) }
    var importDialog by remember { mutableStateOf(false) }
    var exportDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text(project.name) }, navigationIcon = { TextButton(onClick = onBack) { Text("بازگشت") } }) },
        floatingActionButton = { FloatingActionButton(onClick = { addDialog = true }) { Text("+ نقطه") } }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { importDialog = true }, label = { Text("Import CSV") })
                    AssistChip(onClick = { exportDialog = true }, label = { Text("Export CSV") })
                    AssistChip(onClick = {}, label = { Text(project.coordinateSystem.name) })
                }
            }
            item { SurveySummary(points) }
            item { PointMap(points) }
            item { Text("نقاط برداشت (${points.size})", style = MaterialTheme.typography.titleMedium) }
            items(points, key = { it.id }) { p -> PointRow(p, onEdit = { editPoint = p }, onDelete = { store.deletePoint(project.id,p.id); points = store.getPoints(project.id) }) }
        }
    }
    if (addDialog) PointDialog(null, points.size + 1, { addDialog = false }) { p -> store.addPoint(project.id,p); points = store.getPoints(project.id); addDialog = false }
    editPoint?.let { current -> PointDialog(current, points.size + 1, { editPoint = null }) { p -> store.updatePoint(project.id,p); points = store.getPoints(project.id); editPoint = null } }
    if (importDialog) CsvImportDialog(onDismiss = { importDialog = false }) { csv -> store.appendPoints(project.id, CsvCodec.parse(csv)); points = store.getPoints(project.id); importDialog = false }
    if (exportDialog) CsvExportDialog(CsvCodec.export(points)) { exportDialog = false }
}

@Composable private fun PointRow(p: SurveyPoint, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(p.name, style = MaterialTheme.typography.titleSmall); Text("Z ${"%.3f".format(p.z)}") }
            Text("X ${"%.3f".format(p.x)} | Y ${"%.3f".format(p.y)}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = onEdit) { Text("ویرایش") }; TextButton(onClick = onDelete) { Text("حذف") } }
        }
    }
}

@Composable private fun SurveySummary(points: List<SurveyPoint>) {
    val minZ=points.minOfOrNull{it.z}; val maxZ=points.maxOfOrNull{it.z}; val avg=if(points.isEmpty()) null else points.map{it.z}.average()
    val tris = remember(points) { TerrainEngine.triangulate(points) }
    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
        Text("خلاصه برداشت", style = MaterialTheme.typography.titleMedium)
        Text("نقاط: ${points.size} | مثلث‌های TIN: ${tris.size}")
        Text("Z min: ${minZ?.let{"%.3f".format(it)}?:"-"} | max: ${maxZ?.let{"%.3f".format(it)}?:"-"} | avg: ${avg?.let{"%.3f".format(it)}?:"-"}")
    } }
}

@Composable private fun PointMap(points: List<SurveyPoint>) {
    val tris = remember(points) { TerrainEngine.triangulate(points) }
    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
        Text("TIN / نمای نقاط", style = MaterialTheme.typography.titleMedium)
        if(points.isEmpty()) Box(Modifier.fillMaxWidth().height(220.dp),contentAlignment=Alignment.Center){Text("نقطه‌ای ثبت نشده")}
        else Canvas(Modifier.fillMaxWidth().height(260.dp)) {
            val minX=points.minOf{it.x}; val maxX=points.maxOf{it.x}; val minY=points.minOf{it.y}; val maxY=points.maxOf{it.y}
            val dx=max(maxX-minX,1.0); val dy=max(maxY-minY,1.0); val p=20f; val w=size.width-2*p; val h=size.height-2*p
            fun xy(s:SurveyPoint)=Offset(p+((s.x-minX)/dx).toFloat()*w,p+h-((s.y-minY)/dy).toFloat()*h)
            tris.forEach { t -> val a=xy(t.a); val b=xy(t.b); val c=xy(t.c); drawLine(Color.Gray,a,b,1.5f); drawLine(Color.Gray,b,c,1.5f); drawLine(Color.Gray,c,a,1.5f) }
            points.forEach { drawCircle(Color(0xFF1565C0),6f,xy(it)) }
        }
    } }
}

@Composable private fun PointDialog(existing: SurveyPoint?, pointNumber: Int, onDismiss: () -> Unit, onSave: (SurveyPoint) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "P$pointNumber") }; var x by remember { mutableStateOf(existing?.x?.toString() ?: "") }; var y by remember { mutableStateOf(existing?.y?.toString() ?: "") }; var z by remember { mutableStateOf(existing?.z?.toString() ?: "") }
    val xv=x.toDoubleOrNull(); val yv=y.toDoubleOrNull(); val zv=z.toDoubleOrNull(); val valid=name.isNotBlank()&&xv!=null&&yv!=null&&zv!=null
    AlertDialog(onDismissRequest=onDismiss,title={Text(if(existing==null)"افزودن نقطه" else "ویرایش نقطه")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        OutlinedTextField(name,{name=it},label={Text("نام")}); OutlinedTextField(x,{x=it},label={Text("X / Easting")}); OutlinedTextField(y,{y=it},label={Text("Y / Northing")}); OutlinedTextField(z,{z=it},label={Text("Z / Elevation")})
    }},confirmButton={TextButton(enabled=valid,onClick={onSave(SurveyPoint(existing?.id?:System.currentTimeMillis(),name.trim(),xv!!,yv!!,zv!!))}){Text("ذخیره")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})
}

@Composable private fun CsvImportDialog(onDismiss:()->Unit,onImport:(String)->Unit){ var csv by remember{mutableStateOf("name,x,y,z\n")}; AlertDialog(onDismissRequest=onDismiss,title={Text("Import CSV")},text={OutlinedTextField(csv,{csv=it},modifier=Modifier.fillMaxWidth().height(220.dp),label={Text("CSV")})},confirmButton={TextButton(onClick={onImport(csv)}){Text("ورود")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}}) }
@Composable private fun CsvExportDialog(csv:String,onDismiss:()->Unit){ AlertDialog(onDismissRequest=onDismiss,title={Text("Export CSV")},text={OutlinedTextField(csv,{},readOnly=true,modifier=Modifier.fillMaxWidth().height(220.dp))},confirmButton={TextButton(onClick=onDismiss){Text("بستن")}}) }