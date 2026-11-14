package ir.topograde.app

import ir.topograde.app.sensors.SensorChannelConfig
import ir.topograde.app.sensors.SensorChannelMonitor
import ir.topograde.app.sensors.SensorChannelSample
import ir.topograde.app.sensors.SensorHealthLevel
import ir.topograde.app.sensors.SensorId
import org.junit.Assert.assertEquals
import org.junit.Test

class SensorChannelMonitorTest {
    private val config = SensorChannelConfig(listOf(0.0), listOf(10.0), 500L, listOf(2.0))

    @Test fun rejectsOutOfRange() {
        val result = SensorChannelMonitor().evaluate(SensorId.BLADE_POSITION, "Blade", SensorChannelSample(listOf(11.0), 1000L), config, 1100L)
        assertEquals(SensorHealthLevel.BLOCKED, result.level)
    }

    @Test fun rejectsSuddenJump() {
        val monitor = SensorChannelMonitor()
        monitor.evaluate(SensorId.BLADE_POSITION, "Blade", SensorChannelSample(listOf(2.0), 1000L), config, 1000L)
        val result = monitor.evaluate(SensorId.BLADE_POSITION, "Blade", SensorChannelSample(listOf(5.0), 1100L), config, 1100L)
        assertEquals(SensorHealthLevel.BLOCKED, result.level)
    }

    @Test fun rejectsStaleSample() {
        val result = SensorChannelMonitor().evaluate(SensorId.BLADE_POSITION, "Blade", SensorChannelSample(listOf(2.0), 1000L), config, 1700L)
        assertEquals(SensorHealthLevel.BLOCKED, result.level)
    }
}
