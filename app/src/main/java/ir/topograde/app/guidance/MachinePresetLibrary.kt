package ir.topograde.app.guidance

object MachinePresetLibraryCodec {
    fun export(presets:List<MachinePreset>):String = buildString {
        appendLine("name,forward,right,down,blade_width,heading_source,manual_heading,heading_offset,roll_zero,pitch_zero,min_sat,max_age,max_h_sigma,max_v_sigma,max_pdop")
        presets.forEach{p->val g=p.geometry;val h=p.headingConfig;val q=p.qualityThresholds;appendLine("${p.name},${g.antennaToBladeForwardM},${g.antennaToBladeRightM},${g.antennaToBladeDownM},${g.bladeWidthM},${h.source.name},${h.manualHeadingDeg},${h.headingOffsetDeg},${h.rollZeroDeg},${h.pitchZeroDeg},${q.minSatellites},${q.maxCorrectionAgeSec},${q.maxHorizontalSigmaM},${q.maxVerticalSigmaM},${q.maxPdop}")}
    }
}