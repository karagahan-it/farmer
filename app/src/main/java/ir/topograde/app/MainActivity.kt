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
import ir.topograde.app.data.ProjectStore
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

@Composable
private fun TopoGradeApp(store: ProjectStore) {
    var screen by remember { mutableStateOf(Screen.Projects) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    when (screen) {
        Screen.Projects -> ProjectsScreen(store) { selectedProject = it; screen = Screen.Survey }
        Screen.Survey -> selectedProject?.let { project -> SurveyScreen(store, project) { screen = Screen.Projects } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectsScreen(store: ProjectStore, onOpen: (Project) -> Unit) {
    var projects by remember { mutableStateOf(store.getProjects()) }
    var showCreate by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("TopoGrade | پروژه‌ها") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showCreate = true }) { Text("+") } }
    ) { pad ->
        if (projects.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هنوز پروژه‌ای ساخته نشده است")
            }
        } else {
            LazyColumn(
                Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    ElevatedCard(Modifier.fillMaxWidth().clickable { onOpen(project) }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(project.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("برای ورود به برداشت و نقاط لمس کنید")
                        }
                    }
                }
            }
        }
    }
    if (showCreate) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("پروژه جدید") },
            text = { OutlinedTextField(name, { name = it }, label = { Text("نام پروژه") }, singleLine = true) },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    store.createProject(name); projects = store.getProjects(); showCreate = false
                }) { Text("ساخت") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("انصراف") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurveyScreen(store: ProjectStore, project: Project, onBack: () -> Unit) {
    var points by remember(project.id) { mutableStateOf(store.getPoints(project.id)) }
    var showAdd by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text(project.name) }, navigationIcon = { TextButton(onClick = onBack) { Text("بازگشت") } }) },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Text("+ نقطه") } }
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SurveySummary(points) }
            item { PointMap(points) }
            item { Text("نقاط برداشت (${points.size})", style = MaterialTheme.typography.titleMedium) }
            items(points, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text(p.name, style = MaterialTheme.typography.titleSmall); Text("X: ${"%.3f".format(p.x)}"); Text("Y: ${"%.3f".format(p.y)}") }
                        Text("Z: ${"%.3f".format(p.z)}")
                    }
                }
            }
        }
    }
    if (showAdd) {
        AddPointDialog(points.size + 1, { showAdd = false }) { point ->
            store.addPoint(project.id, point); points = store.getPoints(project.id); showAdd = false
        }
    }
}

@Composable
private fun SurveySummary(points: List<SurveyPoint>) {
    val minZ = points.minOfOrNull { it.z }
    val maxZ = points.maxOfOrNull { it.z }
    val avgZ = if (points.isEmpty()) null else points.map { it.z }.average()
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("خلاصه برداشت", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("تعداد نقاط: ${points.size}")
            Text("کمترین ارتفاع: ${minZ?.let { "%.3f".format(it) } ?: "-"}")
            Text("بیشترین ارتفاع: ${maxZ?.let { "%.3f".format(it) } ?: "-"}")
            Text("میانگین ارتفاع: ${avgZ?.let { "%.3f".format(it) } ?: "-"}")
        }
    }
}

@Composable
private fun PointMap(points: List<SurveyPoint>) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("نمای اولیه نقشه نقاط", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (points.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { Text("با افزودن نقاط، پراکندگی اینجا نمایش داده می‌شود") }
            } else {
                Canvas(Modifier.fillMaxWidth().height(240.dp)) {
                    val minX = points.minOf { it.x }; val maxX = points.maxOf { it.x }
                    val minY = points.minOf { it.y }; val maxY = points.maxOf { it.y }
                    val dx = max(maxX - minX, 1.0); val dy = max(maxY - minY, 1.0)
                    val pad = 24f; val w = size.width - pad * 2; val h = size.height - pad * 2
                    drawRect(Color.Gray.copy(alpha = 0.35f), Offset(pad, pad), androidx.compose.ui.geometry.Size(w, h), style = Stroke(2f))
                    points.forEach { p ->
                        val px = pad + ((p.x - minX) / dx).toFloat() * w
                        val py = pad + h - ((p.y - minY) / dy).toFloat() * h
                        drawCircle(Color(0xFF1565C0), 7f, Offset(px, py))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPointDialog(pointNumber: Int, onDismiss: () -> Unit, onAdd: (SurveyPoint) -> Unit) {
    var name by remember { mutableStateOf("P$pointNumber") }
    var x by remember { mutableStateOf("") }; var y by remember { mutableStateOf("") }; var z by remember { mutableStateOf("") }
    val xv = x.toDoubleOrNull(); val yv = y.toDoubleOrNull(); val zv = z.toDoubleOrNull()
    val valid = name.isNotBlank() && xv != null && yv != null && zv != null
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("افزودن نقطه برداشت") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("نام نقطه") }, singleLine = true)
            OutlinedTextField(x, { x = it }, label = { Text("X / Easting") }, singleLine = true)
            OutlinedTextField(y, { y = it }, label = { Text("Y / Northing") }, singleLine = true)
            OutlinedTextField(z, { z = it }, label = { Text("Z / Elevation") }, singleLine = true)
        } },
        confirmButton = { TextButton(enabled = valid, onClick = { onAdd(SurveyPoint(System.currentTimeMillis(), name.trim(), xv!!, yv!!, zv!!)) }) { Text("ذخیره نقطه") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}