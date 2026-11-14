package ir.topograde.app.ui

import androidx.compose.runtime.*
import ir.topograde.app.data.ProjectStore
import ir.topograde.app.model.Project

private enum class Screen { Projects, Survey }

@Composable
fun TopoGradeApp(store: ProjectStore) {
    var screen by remember { mutableStateOf(Screen.Projects) }
    var selected by remember { mutableStateOf<Project?>(null) }
    when (screen) {
        Screen.Projects -> ProjectsScreen(store) { selected = it; screen = Screen.Survey }
        Screen.Survey -> selected?.let { project -> SurveyScreen(store, project) { screen = Screen.Projects } }
    }
}