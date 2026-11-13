package ir.topograde.app.guidance

data class OrientationFilterConfig(
    val rollPitchAlpha:Double=0.18,
    val headingAlpha:Double=0.25
)

class OrientationFilter(private var config:OrientationFilterConfig=OrientationFilterConfig()){
    private var last:Orientation?=null
    fun update(input:Orientation):Orientation {
        val prev=last
        if(prev==null){last=input;return input}
        val h=blendAngle(prev.headingDeg,input.headingDeg,config.headingAlpha)
        val r=prev.rollDeg+(input.rollDeg-prev.rollDeg)*config.rollPitchAlpha.coerceIn(0.0,1.0)
        val p=prev.pitchDeg+(input.pitchDeg-prev.pitchDeg)*config.rollPitchAlpha.coerceIn(0.0,1.0)
        return Orientation(h,r,p).also{last=it}
    }
    fun reset(){last=null}
    private fun blendAngle(a:Double,b:Double,alpha:Double):Double {
        var d=(b-a+180.0)%360.0; if(d<0)d+=360.0; d-=180.0
        var out=a+d*alpha.coerceIn(0.0,1.0); out%=360.0; if(out<0)out+=360.0; return out
    }
}