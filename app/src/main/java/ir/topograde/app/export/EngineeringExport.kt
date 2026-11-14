package ir.topograde.app.export

import ir.topograde.app.grading.HeatPoint
import ir.topograde.app.grading.VolumeSummary

object EngineeringExport {
    fun cutFillCsv(points:List<HeatPoint>,volume:VolumeSummary):String = buildString {
        appendLine("name,x,y,existing_z,design_z,delta,class")
        points.forEach { h -> appendLine("${h.point.name},${h.point.x},${h.point.y},${h.point.z},${h.designZ},${h.delta},${h.clazz}") }
        appendLine()
        appendLine("cut_volume,fill_volume,net_volume,triangles")
        appendLine("${volume.cutVolume},${volume.fillVolume},${volume.netVolume},${volume.triangleCount}")
    }

    fun cutFillDxf(points:List<HeatPoint>):String = buildString {
        appendLine("0"); appendLine("SECTION"); appendLine("2"); appendLine("ENTITIES")
        points.forEach { h ->
            appendLine("0"); appendLine("POINT"); appendLine("8"); appendLine(h.clazz.name)
            appendLine("10"); appendLine(h.point.x)
            appendLine("20"); appendLine(h.point.y)
            appendLine("30"); appendLine(h.designZ)
        }
        appendLine("0"); appendLine("ENDSEC"); appendLine("0"); appendLine("EOF")
    }
}