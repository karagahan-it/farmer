package ir.topograde.app

import ir.topograde.app.guidance.*
import org.junit.Assert.assertTrue
import org.junit.Test

class MachinePresetLibraryCodecTest {
    @Test fun exportsLibrary(){
        val csv=MachinePresetLibraryCodec.export(listOf(MachinePreset("G1",MachineGeometry(),HeadingConfig(),GuidanceQualityThresholds())))
        assertTrue(csv.contains("heading_source"))
        assertTrue(csv.contains("G1"))
    }
}