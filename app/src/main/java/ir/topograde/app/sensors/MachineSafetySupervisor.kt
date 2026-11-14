package ir.topograde.app.sensors

enum class MachineOperationMode { SURVEY, OPERATOR_GUIDANCE, AUTO_LEVEL, AUTONOMOUS }
enum class MachineSafetyState { READY, LIMITED, BLOCKED, EMERGENCY_STOP, MANUAL_OVERRIDE }

data class MachineInterlocks(
    val emergencyStopPressed: Boolean? = null,
    val manualOverrideActive: Boolean? = null,
    val operatorPresent: Boolean? = null,
    val controllerHeartbeatHealthy: Boolean? = null,
    val geofenceValid: Boolean? = null
)

data class MachineSafetyDecision(
    val state: MachineSafetyState,
    val canSurvey: Boolean,
    val canUseGuidance: Boolean,
    val canEnableAutoLevel: Boolean,
    val canEnableAutonomous: Boolean,
    val reasons: List<String>
)

object MachineSafetySupervisor {
    fun evaluate(
        requestedMode: MachineOperationMode,
        sensors: List<SensorHealthStatus>,
        interlocks: MachineInterlocks = MachineInterlocks(),
        actuatorInterfaceAvailable: Boolean = false
    ): MachineSafetyDecision {
        val byId = sensors.associateBy { it.id }
        val primary = byId[SensorId.PRIMARY_GNSS]
        val imu = byId[SensorId.TABLET_IMU]
        val guidanceOk = primary?.level in setOf(SensorHealthLevel.GOOD, SensorHealthLevel.CAUTION) &&
            imu?.level in setOf(SensorHealthLevel.GOOD, SensorHealthLevel.CAUTION)
        val surveyOk = primary?.level in setOf(SensorHealthLevel.GOOD, SensorHealthLevel.CAUTION)

        if (interlocks.emergencyStopPressed == true) {
            return MachineSafetyDecision(MachineSafetyState.EMERGENCY_STOP, false, false, false, false, listOf("توقف اضطراری فعال است"))
        }
        if (interlocks.manualOverrideActive == true) {
            return MachineSafetyDecision(MachineSafetyState.MANUAL_OVERRIDE, surveyOk, false, false, false, listOf("کنترل دستی فعال است"))
        }

        val levelSensors = listOf(
            SensorId.PRIMARY_GNSS, SensorId.SECONDARY_GNSS, SensorId.MACHINE_IMU,
            SensorId.BLADE_POSITION, SensorId.HYDRAULIC_PRESSURE, SensorId.WHEEL_SPEED,
            SensorId.SAFETY_CONTROLLER
        )
        val autoSensorsReady = levelSensors.all { byId[it]?.level == SensorHealthLevel.GOOD }
        val autoInterlocksReady = interlocks.emergencyStopPressed == false &&
            interlocks.manualOverrideActive == false &&
            interlocks.operatorPresent == true &&
            interlocks.controllerHeartbeatHealthy == true
        val autoLevelReady = autoSensorsReady && autoInterlocksReady && actuatorInterfaceAvailable

        val autonomousSensors = levelSensors + SensorId.STEERING_ANGLE
        val autonomousSensorsReady = autonomousSensors.all { byId[it]?.level == SensorHealthLevel.GOOD }
        val autonomousInterlocksReady = autoInterlocksReady && interlocks.geofenceValid == true
        val autonomousReady = autonomousSensorsReady && autonomousInterlocksReady && actuatorInterfaceAvailable

        val reasons = mutableListOf<String>()
        if (!surveyOk) reasons += "گیرنده اصلی موقعیت آماده نیست"
        if (!guidanceOk) reasons += "شیب‌سنج یا گیرنده اصلی آماده نیست"
        if (!autoSensorsReady) reasons += "همه سنسورهای لازم برای تنظیم خودکار نصب و تأیید نشده‌اند"
        if (!autoInterlocksReady) reasons += "مدارهای ایمنی ماشین هنوز تأیید نشده‌اند"
        if (!actuatorInterfaceAvailable) reasons += "کنترلر سخت‌افزاری ماشین هنوز متصل نیست"
        if (!autonomousSensorsReady) reasons += "سنسورهای لازم برای حرکت خودکار کامل نیستند"
        if (!autonomousInterlocksReady) reasons += "محدوده امن و حضور اپراتور تأیید نشده است"

        val state = when (requestedMode) {
            MachineOperationMode.SURVEY -> if (surveyOk) MachineSafetyState.READY else MachineSafetyState.BLOCKED
            MachineOperationMode.OPERATOR_GUIDANCE -> when {
                !guidanceOk -> MachineSafetyState.BLOCKED
                sensors.any { it.level == SensorHealthLevel.CAUTION } -> MachineSafetyState.LIMITED
                else -> MachineSafetyState.READY
            }
            MachineOperationMode.AUTO_LEVEL -> if (autoLevelReady) MachineSafetyState.READY else MachineSafetyState.BLOCKED
            MachineOperationMode.AUTONOMOUS -> if (autonomousReady) MachineSafetyState.READY else MachineSafetyState.BLOCKED
        }
        return MachineSafetyDecision(state, surveyOk, guidanceOk, autoLevelReady, autonomousReady, reasons.distinct())
    }
}
