package ir.topograde.app.data

import android.content.Context
import ir.topograde.app.model.Project
import ir.topograde.app.model.SurveyPoint
import org.json.JSONArray
import org.json.JSONObject

class ProjectStore(context: Context) {
    private val prefs = context.getSharedPreferences("topograde_store", Context.MODE_PRIVATE)

    fun getProjects(): List<Project> {
        val raw = prefs.getString("projects", "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(Project(o.getLong("id"), o.getString("name"), o.getLong("createdAt")))
            }
        }.sortedByDescending { it.createdAt }
    }

    fun createProject(name: String): Project {
        val now = System.currentTimeMillis()
        val project = Project(now, name.trim(), now)
        val projects = getProjects().toMutableList().apply { add(project) }
        saveProjects(projects)
        return project
    }

    fun getPoints(projectId: Long): List<SurveyPoint> {
        val raw = prefs.getString("points_$projectId", "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(SurveyPoint(o.getLong("id"), o.getString("name"), o.getDouble("x"), o.getDouble("y"), o.getDouble("z")))
            }
        }
    }

    fun addPoint(projectId: Long, point: SurveyPoint) {
        val points = getPoints(projectId).toMutableList().apply { add(point) }
        val array = JSONArray()
        points.forEach { p ->
            array.put(JSONObject().put("id", p.id).put("name", p.name).put("x", p.x).put("y", p.y).put("z", p.z))
        }
        prefs.edit().putString("points_$projectId", array.toString()).apply()
    }

    private fun saveProjects(projects: List<Project>) {
        val array = JSONArray()
        projects.forEach { p ->
            array.put(JSONObject().put("id", p.id).put("name", p.name).put("createdAt", p.createdAt))
        }
        prefs.edit().putString("projects", array.toString()).apply()
    }
}