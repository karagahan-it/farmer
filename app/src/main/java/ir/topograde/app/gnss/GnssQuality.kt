package ir.topograde.app.gnss

data class GnssQuality(
    val pdop: Double? = null,
    val hdop: Double? = null,
    val vdop: Double? = null,
    val horizontalSigmaMeters: Double? = null,
    val verticalSigmaMeters: Double? = null,
    val rmsMeters: Double? = null
)