package ir.topograde.app

import ir.topograde.app.sensors.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test

class MachineSafetySupervisorTest {
    @Test fun automaticLevelRemainsBlockedWithoutHardwareInterlocks() {
        val statuses = SensorId.values().map { id ->
            SensorHealthStatus(id, id.name, if (id == SensorId.PRIMARY_GNSS || id == SensorId.TABLET_IMU) SensorHealthLevel.GOOD else SensorHealthLevel.NOT_CONFIGURED, "")
        }
        val result = MachineSafetySupervisor.evaluate(MachineOperationMode.AUTO_LEVEL, statuses)
        assertFalse(result.canEnableAutoLevel)
        assertEquals(MachineSafetyState.BLOCKED, result.state)
    }

    @Test fun emergencyStopAlwaysBlocksAllModes() {
        val result = MachineSafetySupervisor.evaluate(MachineOperationMode.SURVEY, emptyList(), MachineInterlocks(emergencyStopPressed = true))
        assertEquals(MachineSafetyState.EMERGENCY_STOP, result.state)
        assertFalse(result.canUseGuidance)
    }
}
