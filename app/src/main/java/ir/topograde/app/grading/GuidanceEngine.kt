package ir.topograde.app.grading

data class GuidanceStatus(
    val existingZ:Double,
    val targetZ:Double,
    val delta:Double,
    val tolerance:Double,
    val state:GuidanceState
)
enum class GuidanceState { CUT, ON_GRADE, FILL }

object GuidanceEngine {
    fun evaluate(x:Double,y:Double,z:Double,surface:DesignSurface,tolerance:Double=0.03):GuidanceStatus {
        val target=surface.designZ(x,y); val d=z-target
        val state=when{d>tolerance->GuidanceState.CUT;d< -tolerance->GuidanceState.FILL;else->GuidanceState.ON_GRADE}
        return GuidanceStatus(z,target,d,tolerance,state)
    }
}