package ir.topograde.app.sensors

import kotlin.math.abs

data class SensorChannelConfig(
    val minimumValues: List<Double>,
    val maximumValues: List<Double>,
    val maxAgeMillis: Long,
    val maxDeltaPerSecond: List<Double>
) {
    init {
        require(minimumValues.isNotEmpty())
        require(minimumValues.size == maximumValues.size)
        require(minimumValues.size == maxDeltaPerSecond.size)
        require(maxAgeMillis > 0L)
        require(minimumValues.indices.all { minimumValues[it].isFinite() && maximumValues[it].isFinite() && minimumValues[it] <= maximumValues[it] })
        require(maxDeltaPerSecond.all { it.isFinite() && it > 0.0 })
    }
}

data class SensorChannelSample(
    val values: List<Double>,
    val timestampElapsedMs: Long,
    val connected: Boolean = true,
    val accuracy: Int = 3
)

class SensorChannelMonitor {
    private val previous = mutableMapOf<String, SensorChannelSample>()

    @Synchronized
    fun evaluate(
        id: SensorId,
        title: String,
        sample: SensorChannelSample?,
        config: SensorChannelConfig,
        nowElapsedMs: Long
    ): SensorHealthStatus {
        if (sample == null || !sample.connected) {
            return SensorHealthStatus(id, title, SensorHealthLevel.DISCONNECTED, "ارتباط سنسور قطع است")
        }
        if (sample.values.size != config.minimumValues.size || sample.values.any { !it.isFinite() }) {
            return SensorHealthStatus(id, title, SensorHealthLevel.BLOCKED, "مقدار دریافتی معتبر نیست")
        }
        if (sample.timestampElapsedMs > nowElapsedMs + 100L) {
            return SensorHealthStatus(id, title, SensorHealthLevel.BLOCKED, "زمان ثبت اطلاعات معتبر نیست")
        }
        val age = (nowElapsedMs - sample.timestampElapsedMs).coerceAtLeast(0L)
        if (age > config.maxAgeMillis) {
            return SensorHealthStatus(id, title, SensorHealthLevel.BLOCKED, "اطلاعات سنسور قدیمی شده", age)
        }
        if (sample.values.indices.any { sample.values[it] < config.minimumValues[it] || sample.values[it] > config.maximumValues[it] }) {
            return SensorHealthStatus(id, title, SensorHealthLevel.BLOCKED, "مقدار خارج از محدوده مجاز است", age)
        }
        val old = previous[id.name]
        if (old != null) {
            val dt = (sample.timestampElapsedMs - old.timestampElapsedMs) / 1000.0
            if (dt <= 0.0) {
                return SensorHealthStatus(id, title, SensorHealthLevel.BLOCKED, "ترتیب زمانی نمونه‌ها درست نیست", age)
            }
            if (old.values.size == sample.values.size) {
                val jump = sample.values.indices.any {
                    abs(sample.values[it] - old.values[it]) / dt > config.maxDeltaPerSecond[it]
                }
                if (jump) return SensorHealthStatus(id, title, SensorHealthLevel.BLOCKED, "تغییر ناگهانی و غیرعادی تشخیص داده شد", age)
            }
        }
        if (sample.accuracy <= 0) return SensorHealthStatus(id, title, SensorHealthLevel.CAUTION, "دقت سنسور پایین یا نامشخص است", age)
        previous[id.name] = sample
        return SensorHealthStatus(id, title, SensorHealthLevel.GOOD, "سنسور پایدار است", age)
    }

    @Synchronized
    fun reset(id: SensorId) { previous.remove(id.name) }

    @Synchronized
    fun resetAll() { previous.clear() }
}
