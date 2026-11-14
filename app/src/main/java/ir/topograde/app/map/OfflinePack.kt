package ir.topograde.app.map

data class OfflinePack(
    val id: Long,
    val name: String,
    val active: Boolean,
    val complete: Boolean,
    val completedResources: Long,
    val requiredResources: Long,
    val completedBytes: Long
) {
    val percent:Int get()=if(requiredResources<=0)0 else ((completedResources*100L)/requiredResources).toInt().coerceIn(0,100)
}