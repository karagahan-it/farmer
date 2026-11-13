package ir.topograde.app.data

import ir.topograde.app.model.SurveyPoint

object CsvCodec {
    fun export(points: List<SurveyPoint>): String = buildString {
        appendLine("name,x,y,z")
        points.forEach { p -> appendLine("${p.name},${p.x},${p.y},${p.z}") }
    }

    fun parse(csv: String): List<SurveyPoint> {
        return csv.lineSequence().drop(1).mapNotNull { line ->
            val c = line.split(",").map { it.trim() }
            if (c.size < 4) return@mapNotNull null
            val x = c[1].toDoubleOrNull() ?: return@mapNotNull null
            val y = c[2].toDoubleOrNull() ?: return@mapNotNull null
            val z = c[3].toDoubleOrNull() ?: return@mapNotNull null
            SurveyPoint(System.nanoTime(), c[0].ifBlank { "P" }, x, y, z)
        }.toList()
    }
}