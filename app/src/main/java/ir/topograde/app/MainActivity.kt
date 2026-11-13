package ir.topograde.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.topograde.app.model.SurveyPoint

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MaterialTheme { TopoGradeScreen() } } }
}

@Composable fun TopoGradeScreen() {
 var points by remember { mutableStateOf(listOf(SurveyPoint(1,"P1",500000.0,4000000.0,102.35),SurveyPoint(2,"P2",500020.0,4000010.0,103.10),SurveyPoint(3,"P3",500035.0,3999995.0,101.80))) }
 Scaffold(topBar={ TopAppBar(title={Text("TopoGrade | نقشه‌برداری زمین")}) }) { pad ->
  Column(Modifier.padding(pad).padding(16.dp).fillMaxSize()) {
   Text("پروژه آزمایشی", style=MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp))
   Text("نقاط برداشت XYZ — هسته اولیه برای TIN، خطوط تراز و RTK")
   Spacer(Modifier.height(16.dp)); LazyColumn { items(points) { p -> Card(Modifier.fillMaxWidth().padding(vertical=4.dp)) { Column(Modifier.padding(12.dp)){ Text(p.name); Text("X ${p.x}   Y ${p.y}   Z ${p.z}") } } } }
  }
 }
}
