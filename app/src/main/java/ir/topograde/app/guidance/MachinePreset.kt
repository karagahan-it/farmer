package ir.topograde.app.guidance

data class MachinePreset(
    val name:String,
    val geometry:MachineGeometry,
    val headingConfig:HeadingConfig,
    val qualityThresholds:GuidanceQualityThresholds = GuidanceQualityThresholds()
)