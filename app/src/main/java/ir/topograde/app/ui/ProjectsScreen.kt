package ir.topograde.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.topograde.app.data.ProjectStore
import ir.topograde.app.model.CoordinateSystem
import ir.topograde.app.model.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(store: ProjectStore, onOpen: (Project) -> Unit) {
    var projects by remember { mutableStateOf(store.getProjects()) }
    var showCreate by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("TopoGrade | پروژه‌ها") }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { showCreate = true }, text = { Text("پروژه جدید") }) }
    ) { pad ->
        if (projects.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) { Text("هنوز پروژه‌ای ساخته نشده است") }
        } else {
            LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(projects, key = { it.id }) { p ->
                    ElevatedCard(Modifier.fillMaxWidth().clickable { onOpen(p) }) {
                        Column(Modifier.padding(18.dp)) {
                            Text(p.name, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(6.dp))
                            Text(p.coordinateSystem.title + (p.utmZone?.let { " | Zone " + it } ?: ""))
                        }
                    }
                }
            }
        }
    }
    if (showCreate) CreateProjectDialog(onDismiss = { showCreate = false }, onCreate = { name, crs, zone ->
        store.createProject(name, crs, zone); projects = store.getProjects(); showCreate = false
    })
}

@Composable
private fun CreateProjectDialog(onDismiss: () -> Unit, onCreate: (String, CoordinateSystem, Int?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var crs by remember { mutableStateOf(CoordinateSystem.LOCAL) }
    var zone by remember { mutableStateOf("") }
    val validZone = zone.toIntOrNull()?.let { it in 1..60 } == true
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پروژه جدید") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("نام پروژه") }, singleLine = true)
            Text("سیستم مختصات")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { CoordinateSystem.entries.forEach { item ->
                FilterChip(selected = crs == item, onClick = { crs = item }, label = { Text(item.name) })
            } }
            if (crs == CoordinateSystem.UTM) OutlinedTextField(zone, { zone = it.filter(Char::isDigit).take(2) }, label = { Text("UTM Zone (1–60)") }, singleLine = true)
        } },
        confirmButton = { TextButton(enabled = name.isNotBlank() && (crs != CoordinateSystem.UTM || validZone), onClick = { onCreate(name.trim(), crs, zone.toIntOrNull()) }) { Text("ساخت") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}