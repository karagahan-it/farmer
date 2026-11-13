package ir.topograde.app.model

data class Project(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val coordinateSystem: CoordinateSystem = CoordinateSystem.LOCAL,
    val utmZone: Int? = null,
    val northernHemisphere: Boolean = true
)