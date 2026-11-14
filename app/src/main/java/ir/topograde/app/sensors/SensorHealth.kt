package ir.topograde.app.sensors

import ir.topograde.app.gnss.FixType
import ir.topograde.app.gnss.GnssFix
import ir.topograde.app.gnss.GnssQuality
import ir.topograde.app.guidance.GuidanceQualityThresholds

enum class SensorId {
    PRIMARY_GNSS, SECONDARY_GNSS, TABLET_IMU, MACHINE_IMU, BLADE_POSITION,
    HYDRAULIC_PRESSURE, WHEEL_SPEED, STEERING_ANGLE, LASER_RECEIVER, SAFETY_CONTROLLER
}

enum class SensorHealthLevel { GOOD, CAUTION, BLOCKED, DISCONNECTED, WAITING, NOT_CONFIGURED }

data class SensorHealthStatus(
    val id: SensorId,
    val title: String,
    val level: SensorHealthLevel,
    val message: String,
    val ageMillis: Long? = null
)

object SensorHealthEngine {
    fun notConfigured(id: SensorId, title: String) =
        SensorHealthStatus(id, title, SensorHealthLevel.NOT_CONFIGURED, "هنوز به دستگاه وصل نشده")

    fun evaluateGnss(
        id: SensorId,
        title: String,
        connected: Boolean,
        fix: GnssFix?,
        lastFixElapsedMs: Long?,
        quality: GnssQuality,
        nowElapsedMs: Long,
        thresholds: GuidanceQualityThresholds = GuidanceQualityThresholds()
    ): SensorHealthStatus {
        if (!connected) return SensorHealthStatus(id, title, SensorHealthLevel.DISCONNECTED, "گیرنده وصل نیست")
        if (fix == null || lastFixElapsedMs == null) {
            return SensorHealthStatus(id, title, SensorHealthLevel.WAITING, "در انتظار دریافت موقعیت")
        }
        val age = (nowElapsedMs - lastFixElapsedMs).coerceAtLeast(0L)
        if (age > 3000L) return SensorHealthStatus(id, title, SensorHealthLevel.BLOCKED, "اطلاعات گیرنده قدیمی شده", age)
        if (!fix.latitude.isFinite() || !fix.longitude.isFinite() ||
            fix.latitude !in -90.0..90.0 || fix.longitude !in -180.0..180.0 ||
            fix.altitude?.isFinite() != true) {
            return SensorHealthStatus(id, title, SensorHealthLevel.BLOCKED, "موقعیت دریافتی معتبر نیست", age)
        }
        if (fix.fixType == FixType.NONE || fix.fixType == FixType.GPS || fix.fixType == FixType.DGPS ||
            fix.fixType == FixType.ESTIMATED) {
            return SensorHealthStatus(id, title, SensorHealthLevel.BLOCKED, "دقت RTK برای این کار کافی نیست", age)
        }
        val warnings = mutableListOf<String>()
        if (fix.fixType != FixType.RTK_FIXED) warnings += "دقت RTK کامل نیست"
        if (age > 1500L) warnings += "دریافت اطلاعات کند شده"
        if (fix.satellites < thresholds.minSatellites) warnings += "تعداد ماهواره کم است"
        val correctionAge = fix.correctionAgeSeconds
        if (correctionAge == null || !correctionAge.isFinite()) warnings += "زمان اصلاح RTK مشخص نیست"
        else if (correctionAge < 0.0 || correctionAge > thresholds.maxCorrectionAgeSec) warnings += "زمان اصلاح RTK نامعتبر یا دیر است"
        val h = quality.horizontalSigmaMeters
        val v = quality.verticalSigmaMeters
        val pdop = quality.pdop
        if (h == null || v == null || pdop == null || !h.isFinite() || !v.isFinite() || !pdop.isFinite()) {
            warnings += "اطلاعات دقت کامل نیست"
        } else if (h < 0.0 || v < 0.0 || pdop <= 0.0 || h > thresholds.maxHorizontalSigmaM || v > thresholds.maxVerticalSigmaM || pdop > thresholds.maxPdop) {
            warnings += "خطای اندازه‌گیری زیاد است"
        }
        return if (warnings.isEmpty()) {
            SensorHealthStatus(id, title, SensorHealthLevel.GOOD, "داده‌ها مناسب است", age)
        } else {
            SensorHealthStatus(id, title, SensorHealthLevel.CAUTION, warnings.distinct().joinToString("، "), age)
        }
    }

    fun evaluateImu(snapshot: ir.topograde.app.guidance.OrientationSensorSnapshot, nowElapsedMs: Long): SensorHealthStatus {
        val id = SensorId.TABLET_IMU
        if (!snapshot.available) return SensorHealthStatus(id, "شیب‌سنج تبلت", SensorHealthLevel.NOT_CONFIGURED, "این تبلت سنسور حرکتی مناسب ندارد")
        if (!snapshot.registrationSucceeded) return SensorHealthStatus(id, "شیب‌سنج تبلت", SensorHealthLevel.DISCONNECTED, "خواندن سنسور شروع نشده")
        val last = snapshot.lastSampleElapsedMs
            ?: return SensorHealthStatus(id, "شیب‌سنج تبلت", SensorHealthLevel.WAITING, "در انتظار نمونه سنسور")
        val age = (nowElapsedMs - last).coerceAtLeast(0L)
        if (age > 1500L) return SensorHealthStatus(id, "شیب‌سنج تبلت", SensorHealthLevel.BLOCKED, "اطلاعات شیب‌سنج قطع یا قدیمی شده", age)
        if (snapshot.accuracy <= 0) return SensorHealthStatus(id, "شیب‌سنج تبلت", SensorHealthLevel.CAUTION, "دقت سنسور هنوز تأیید نشده", age)
        if (!snapshot.headingAbsolute) {
            return SensorHealthStatus(id, "شیب‌سنج تبلت", SensorHealthLevel.CAUTION, "جهت‌یابی تبلت نسبی است؛ برای جهت ماشین از دو GPS استفاده کنید", age)
        }
        if (age > 500L || snapshot.accuracy == 1) {
            return SensorHealthStatus(id, "شیب‌سنج تبلت", SensorHealthLevel.CAUTION, "دقت یا سرعت به‌روزرسانی متوسط است", age)
        }
        return SensorHealthStatus(id, "شیب‌سنج تبلت", SensorHealthLevel.GOOD, "اطلاعات حرکتی دریافت می‌شود", age)
    }
}
