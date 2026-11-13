package ir.topograde.app.data

import android.content.Context
import ir.topograde.app.geo.LocalSiteCalibration
import ir.topograde.app.geo.Similarity2D
import ir.topograde.app.grading.DesignPlane
import ir.topograde.app.grading.GradingMode
import ir.topograde.app.grading.GradingPreset
import ir.topograde.app.guidance.MachineGeometry
import ir.topograde.app.logging.GuidanceLogEntry
import ir.topograde.app.model.CoordinateSystem
import ir.topograde.app.model.Project
import ir.topograde.app.model.SurveyPoint
import ir.topograde.app.survey.HeightInputType
import ir.topograde.app.survey.HeightSettings
import org.json.JSONArray
import org.json.JSONObject

class ProjectStore(context: Context) {
    private val prefs = context.getSharedPreferences("topograde_store", Context.MODE_PRIVATE)

    fun getProjects(): List<Project> {
        val array = JSONArray(prefs.getString("projects", "[]") ?: "[]")
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(Project(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    createdAt = o.getLong("createdAt"),
                    coordinateSystem = runCatching { CoordinateSystem.valueOf(o.optString("coordinateSystem","LOCAL")) }.getOrDefault(CoordinateSystem.LOCAL),
                    utmZone = if (o.has("utmZone") && !o.isNull("utmZone")) o.getInt("utmZone") else null,
                    northernHemisphere = o.optBoolean("northernHemisphere", true)
                ))
            }
        }.sortedByDescending { it.createdAt }
    }

    fun createProject(name: String, coordinateSystem: CoordinateSystem = CoordinateSystem.LOCAL, utmZone: Int? = null): Project {
        val now = System.currentTimeMillis()
        val project = Project(now, name.trim(), now, coordinateSystem, utmZone)
        saveProjects(getProjects() + project)
        return project
    }

    fun updateProject(project: Project) { saveProjects(getProjects().map { if (it.id == project.id) project else it }) }

    fun getPoints(projectId: Long): List<SurveyPoint> = readPointList("points_$projectId")
    fun addPoint(projectId: Long, point: SurveyPoint) = savePointList("points_$projectId", getPoints(projectId) + point)
    fun updatePoint(projectId: Long, point: SurveyPoint) = savePointList("points_$projectId", getPoints(projectId).map { if (it.id == point.id) point else it })
    fun deletePoint(projectId: Long, pointId: Long) = savePointList("points_$projectId", getPoints(projectId).filterNot { it.id == pointId })
    fun replacePoints(projectId: Long, points: List<SurveyPoint>) = savePointList("points_$projectId", points)
    fun appendPoints(projectId: Long, points: List<SurveyPoint>) = savePointList("points_$projectId", getPoints(projectId) + points)

    fun getTrack(projectId: Long): List<SurveyPoint> = readPointList("track_$projectId")
    fun appendTrackPoint(projectId: Long, point: SurveyPoint) = savePointList("track_$projectId", getTrack(projectId) + point)
    fun clearTrack(projectId: Long) = savePointList("track_$projectId", emptyList())

    fun getBoundary(projectId: Long): List<SurveyPoint> = readPointList("boundary_$projectId")
    fun appendBoundaryPoint(projectId: Long, point: SurveyPoint) = savePointList("boundary_$projectId", getBoundary(projectId) + point)
    fun clearBoundary(projectId: Long) = savePointList("boundary_$projectId", emptyList())

    fun saveHeightSettings(projectId:Long, settings:HeightSettings) {
        val o=JSONObject().put("antennaHeightMeters",settings.antennaHeightMeters).put("inputType",settings.inputType.name).put("geoidUndulationMeters",settings.geoidUndulationMeters)
        prefs.edit().putString("height_$projectId",o.toString()).apply()
    }
    fun getHeightSettings(projectId:Long):HeightSettings {
        val raw=prefs.getString("height_$projectId",null) ?: return HeightSettings()
        return runCatching { val o=JSONObject(raw); HeightSettings(o.optDouble("antennaHeightMeters",0.0),runCatching{HeightInputType.valueOf(o.optString("inputType","GGA_ORTHOMETRIC"))}.getOrDefault(HeightInputType.GGA_ORTHOMETRIC),o.optDouble("geoidUndulationMeters",0.0)) }.getOrDefault(HeightSettings())
    }

    fun saveDesignPlane(projectId:Long, plane:DesignPlane) {
        val o=JSONObject().put("originX",plane.originX).put("originY",plane.originY).put("originZ",plane.originZ).put("slopeXPercent",plane.slopeXPercent).put("slopeYPercent",plane.slopeYPercent)
        prefs.edit().putString("design_plane_$projectId",o.toString()).apply()
    }
    fun getDesignPlane(projectId:Long):DesignPlane? {
        val raw=prefs.getString("design_plane_$projectId",null) ?: return null
        return runCatching { val o=JSONObject(raw); DesignPlane(o.getDouble("originX"),o.getDouble("originY"),o.getDouble("originZ"),o.optDouble("slopeXPercent",0.0),o.optDouble("slopeYPercent",0.0)) }.getOrNull()
    }

    fun saveGradingPreset(projectId:Long, preset:GradingPreset) {
        val o=JSONObject()
            .put("mode",preset.mode.name)
            .put("originX",preset.originX)
            .put("originY",preset.originY)
            .put("originZ",preset.originZ)
            .put("slopeXPercent",preset.slopeXPercent)
            .put("slopeYPercent",preset.slopeYPercent)
            .put("azimuthDeg",preset.azimuthDeg)
        prefs.edit().putString("grading_preset_$projectId",o.toString()).apply()
    }
    fun getGradingPreset(projectId:Long):GradingPreset? {
        val raw=prefs.getString("grading_preset_$projectId",null) ?: return null
        return runCatching {
            val o=JSONObject(raw)
            GradingPreset(
                mode=runCatching{GradingMode.valueOf(o.getString("mode"))}.getOrDefault(GradingMode.TARGET_ELEVATION),
                originX=o.getDouble("originX"),
                originY=o.getDouble("originY"),
                originZ=o.getDouble("originZ"),
                slopeXPercent=o.optDouble("slopeXPercent",0.0),
                slopeYPercent=o.optDouble("slopeYPercent",0.0),
                azimuthDeg=o.optDouble("azimuthDeg",0.0)
            )
        }.getOrNull()
    }
    fun saveMachineGeometry(projectId:Long, geometry:MachineGeometry) {
        val o=JSONObject()
            .put("forward",geometry.antennaToBladeForwardM)
            .put("right",geometry.antennaToBladeRightM)
            .put("down",geometry.antennaToBladeDownM)
            .put("bladeWidth",geometry.bladeWidthM)
        prefs.edit().putString("machine_$projectId",o.toString()).apply()
    }
    fun getMachineGeometry(projectId:Long):MachineGeometry {
        val raw=prefs.getString("machine_$projectId",null) ?: return MachineGeometry()
        return runCatching {
            val o=JSONObject(raw)
            MachineGeometry(o.optDouble("forward",2.5),o.optDouble("right",0.0),o.optDouble("down",2.0),o.optDouble("bladeWidth",3.7))
        }.getOrDefault(MachineGeometry())
    }
    fun getGuidanceLogs(projectId:Long):List<GuidanceLogEntry> {
        val arr=JSONArray(prefs.getString("guidance_log_$projectId","[]")?:"[]")
        return buildList {
            for(i in 0 until arr.length()){
                val o=arr.getJSONObject(i)
                add(GuidanceLogEntry(
                    o.getLong("timestamp"),o.getDouble("x"),o.getDouble("y"),o.getDouble("antennaZ"),
                    o.getDouble("headingDeg"),o.getDouble("rollDeg"),o.getDouble("pitchDeg"),
                    o.getDouble("bladeCenterZ"),o.getDouble("targetZ"),o.getDouble("deltaZ"),
                    o.getDouble("actualCrossSlopePercent"),o.getDouble("targetCrossSlopePercent")
                ))
            }
        }
    }
    fun appendGuidanceLog(projectId:Long, e:GuidanceLogEntry) {
        val list=getGuidanceLogs(projectId)+e
        val arr=JSONArray()
        list.takeLast(10000).forEach { x -> arr.put(JSONObject()
            .put("timestamp",x.timestamp).put("x",x.x).put("y",x.y).put("antennaZ",x.antennaZ)
            .put("headingDeg",x.headingDeg).put("rollDeg",x.rollDeg).put("pitchDeg",x.pitchDeg)
            .put("bladeCenterZ",x.bladeCenterZ).put("targetZ",x.targetZ).put("deltaZ",x.deltaZ)
            .put("actualCrossSlopePercent",x.actualCrossSlopePercent).put("targetCrossSlopePercent",x.targetCrossSlopePercent)) }
        prefs.edit().putString("guidance_log_$projectId",arr.toString()).apply()
    }
    fun clearGuidanceLogs(projectId:Long){ prefs.edit().remove("guidance_log_$projectId").apply() }
    fun saveLocalCalibration(projectId: Long, calibration: LocalSiteCalibration) {
        val t = calibration.transform
        val o = JSONObject().put("utmZone", calibration.utmZone).put("north", calibration.northernHemisphere).put("scale", t.scale).put("rotationRad", t.rotationRad).put("tx", t.tx).put("ty", t.ty)
        prefs.edit().putString("local_cal_$projectId", o.toString()).apply()
    }
    fun getLocalCalibration(projectId: Long): LocalSiteCalibration? {
        val raw = prefs.getString("local_cal_$projectId", null) ?: return null
        return runCatching { val o = JSONObject(raw); LocalSiteCalibration(o.getInt("utmZone"),o.optBoolean("north", true),Similarity2D(o.getDouble("scale"),o.getDouble("rotationRad"),o.getDouble("tx"),o.getDouble("ty"))) }.getOrNull()
    }

    private fun readPointList(key: String): List<SurveyPoint> {
        val array = JSONArray(prefs.getString(key, "[]") ?: "[]")
        return buildList { for (i in 0 until array.length()) { val o = array.getJSONObject(i); add(SurveyPoint(o.getLong("id"), o.getString("name"), o.getDouble("x"), o.getDouble("y"), o.getDouble("z"))) } }
    }
    private fun savePointList(key: String, points: List<SurveyPoint>) {
        val array = JSONArray(); points.forEach { p -> array.put(JSONObject().put("id",p.id).put("name",p.name).put("x",p.x).put("y",p.y).put("z",p.z)) }; prefs.edit().putString(key, array.toString()).apply()
    }
    private fun saveProjects(projects: List<Project>) {
        val array = JSONArray(); projects.forEach { p -> array.put(JSONObject().put("id",p.id).put("name",p.name).put("createdAt",p.createdAt).put("coordinateSystem",p.coordinateSystem.name).put("utmZone",p.utmZone).put("northernHemisphere",p.northernHemisphere)) }; prefs.edit().putString("projects", array.toString()).apply()
    }
}