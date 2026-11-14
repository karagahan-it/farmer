package ir.topograde.app.guidance

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI

data class OrientationSensorSnapshot(
    val available: Boolean = false,
    val sensorName: String? = null,
    val lastSampleElapsedMs: Long? = null,
    val accuracy: Int = -1,
    val sampleCount: Long = 0L,
    val registrationSucceeded: Boolean = false,
    val headingAbsolute: Boolean = false
)

class OrientationProvider(context: Context) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val absoluteHeading = sensor?.type == Sensor.TYPE_ROTATION_VECTOR

    private val _orientation = MutableStateFlow(Orientation())
    val orientation: StateFlow<Orientation> = _orientation.asStateFlow()

    private val _health = MutableStateFlow(
        OrientationSensorSnapshot(
            available = sensor != null,
            sensorName = sensor?.name,
            headingAbsolute = absoluteHeading
        )
    )
    val health: StateFlow<OrientationSensorSnapshot> = _health.asStateFlow()

    private val _samples = MutableSharedFlow<Orientation>(extraBufferCapacity = 256)
    val samples: SharedFlow<Orientation> = _samples.asSharedFlow()

    fun start() {
        val selected = sensor
        if (selected == null) {
            _health.value = OrientationSensorSnapshot(available = false)
            return
        }
        val registered = manager.registerListener(this, selected, SensorManager.SENSOR_DELAY_GAME)
        _health.value = _health.value.copy(
            available = true,
            sensorName = selected.name,
            registrationSucceeded = registered,
            lastSampleElapsedMs = null,
            sampleCount = 0L,
            accuracy = -1,
            headingAbsolute = absoluteHeading
        )
    }

    fun stop() {
        manager.unregisterListener(this)
        _health.value = _health.value.copy(registrationSucceeded = false)
    }

    fun available(): Boolean = sensor != null

    override fun onSensorChanged(event: SensorEvent?) {
        val selected = sensor ?: return
        if (event == null || event.sensor.type != selected.type) return
        val matrix = FloatArray(9)
        val values = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(matrix, event.values)
        SensorManager.getOrientation(matrix, values)
        fun deg(r: Float) = r * 180.0 / PI
        val az = (deg(values[0]) + 360.0) % 360.0
        val sample = Orientation(headingDeg = az, rollDeg = deg(values[2]), pitchDeg = deg(values[1]))
        if (!sample.headingDeg.isFinite() || !sample.rollDeg.isFinite() || !sample.pitchDeg.isFinite()) return

        _orientation.value = sample
        _samples.tryEmit(sample)
        val previous = _health.value
        _health.value = previous.copy(
            available = true,
            sensorName = event.sensor.name,
            lastSampleElapsedMs = event.timestamp / 1_000_000L,
            accuracy = event.accuracy,
            sampleCount = previous.sampleCount + 1L,
            registrationSucceeded = true
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type != this.sensor?.type) return
        _health.value = _health.value.copy(accuracy = accuracy)
    }
}
