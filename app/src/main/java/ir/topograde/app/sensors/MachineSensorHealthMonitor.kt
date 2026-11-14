package ir.topograde.app.sensors

class MachineSensorHealthMonitor(
    private val configs: Map<SensorId, SensorChannelConfig>
) {
    private val channelMonitor = SensorChannelMonitor()

    fun evaluate(
        samples: Map<SensorId, SensorChannelSample>,
        nowElapsedMs: Long
    ): List<SensorHealthStatus> {
        val channels = listOf(
            SensorId.MACHINE_IMU to "شیب‌سنج صنعتی ماشین",
            SensorId.BLADE_POSITION to "موقعیت تیغه",
            SensorId.HYDRAULIC_PRESSURE to "فشار هیدرولیک",
            SensorId.WHEEL_SPEED to "سرعت چرخ",
            SensorId.STEERING_ANGLE to "زاویه فرمان",
            SensorId.SAFETY_CONTROLLER to "کنترلر ایمنی ماشین"
        )
        return channels.map { (id, title) ->
            val config = configs[id]
            if (config == null) {
                SensorHealthEngine.notConfigured(id, title)
            } else {
                channelMonitor.evaluate(id, title, samples[id], config, nowElapsedMs)
            }
        }
    }

    fun reset() = channelMonitor.resetAll()
}
