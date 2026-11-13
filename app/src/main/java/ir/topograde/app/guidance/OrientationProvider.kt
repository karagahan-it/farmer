package ir.topograde.app.guidance

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI

class OrientationProvider(context:Context) : SensorEventListener {
    private val manager=context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor=manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val _orientation=MutableStateFlow(Orientation())
    val orientation:StateFlow<Orientation> = _orientation.asStateFlow()

    fun start(){ sensor?.let { manager.registerListener(this,it,SensorManager.SENSOR_DELAY_GAME) } }
    fun stop(){ manager.unregisterListener(this) }
    fun available():Boolean = sensor != null

    override fun onSensorChanged(event:SensorEvent?) {
        if(event?.sensor?.type != Sensor.TYPE_ROTATION_VECTOR) return
        val matrix=FloatArray(9)
        val values=FloatArray(3)
        SensorManager.getRotationMatrixFromVector(matrix,event.values)
        SensorManager.getOrientation(matrix,values)
        fun deg(r:Float)=r*180.0/PI
        val az=(deg(values[0])+360.0)%360.0
        _orientation.value=Orientation(headingDeg=az,rollDeg=deg(values[2]),pitchDeg=deg(values[1]))
    }
    override fun onAccuracyChanged(sensor:Sensor?,accuracy:Int)=Unit
}