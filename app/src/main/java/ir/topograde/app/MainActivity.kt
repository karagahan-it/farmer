package ir.topograde.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import ir.topograde.app.data.ProjectStore
import ir.topograde.app.ui.TopoGradeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = ProjectStore(applicationContext)
        setContent { MaterialTheme { TopoGradeApp(store) } }
    }
}