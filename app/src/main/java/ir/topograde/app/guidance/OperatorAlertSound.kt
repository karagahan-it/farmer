package ir.topograde.app.guidance

import android.media.AudioManager
import android.media.ToneGenerator

class OperatorAlertSound {
    private val tone=ToneGenerator(AudioManager.STREAM_ALARM,70)
    private var lastAt=0L

    fun signal(level:GuidanceQualityLevel,hasGuidanceWarning:Boolean){
        val now=System.currentTimeMillis()
        val minGap=when(level){GuidanceQualityLevel.BLOCKED->1500L;GuidanceQualityLevel.CAUTION->3000L;GuidanceQualityLevel.GOOD->if(hasGuidanceWarning)3000L else Long.MAX_VALUE}
        if(now-lastAt<minGap)return
        when {
            level==GuidanceQualityLevel.BLOCKED -> tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,350)
            level==GuidanceQualityLevel.CAUTION || hasGuidanceWarning -> tone.startTone(ToneGenerator.TONE_PROP_BEEP2,180)
        }
        lastAt=now
    }
    fun release(){tone.release()}
}