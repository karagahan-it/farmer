package ir.topograde.app.gnss

object NmeaParser {
    fun parse(sentence: String): GnssFix? {
        val raw = sentence.trim()
        if (!raw.startsWith('
        val body = raw.substring(1).substringBefore("*")
        val p = body.split(",")
        if (p.isEmpty()) return null
        return when { p[0].endsWith("GGA") -> parseGga(p); else -> null }
    }

    private fun parseGga(p: List<String>): GnssFix? {
        if (p.size < 10) return null
        val lat = dmToDegrees(p[2], p[3]) ?: return null
        val lon = dmToDegrees(p[4], p[5]) ?: return null
        val quality = p[6].toIntOrNull() ?: 0
        return GnssFix(
            latitude = lat,
            longitude = lon,
            altitude = p[9].toDoubleOrNull(),
            satellites = p[7].toIntOrNull() ?: 0,
            hdop = p[8].toDoubleOrNull(),
            fixType = when (quality) { 1 -> FixType.GPS; 2 -> FixType.DGPS; 4 -> FixType.RTK_FIXED; 5 -> FixType.RTK_FLOAT; 6 -> FixType.ESTIMATED; else -> FixType.NONE },
            utcTime = p.getOrNull(1)?.ifBlank { null },
            correctionAgeSeconds = p.getOrNull(13)?.toDoubleOrNull(),
            baseStationId = p.getOrNull(14)?.ifBlank { null }
        )
    }

    private fun dmToDegrees(value: String, hemi: String): Double? {
        val n = value.toDoubleOrNull() ?: return null
        val degrees = kotlin.math.floor(n / 100.0)
        val minutes = n - degrees * 100.0
        var out = degrees + minutes / 60.0
        if (hemi == "S" || hemi == "W") out = -out
        return out
    }

    private fun checksumValid(sentence: String): Boolean {
        val star = sentence.indexOf("*")
        if (star < 0) return true
        val expected = sentence.substring(star + 1).take(2).toIntOrNull(16) ?: return false
        var checksum = 0
        for (i in 1 until star) checksum = checksum xor sentence[i].code
        return checksum == expected
    }
}) || !checksumValid(raw)) return null
        val body = raw.substring(1).substringBefore("*")
        val p = body.split(",")
        if (p.isEmpty()) return null
        return when { p[0].endsWith("GGA") -> parseGga(p); else -> null }
    }

    private fun parseGga(p: List<String>): GnssFix? {
        if (p.size < 10) return null
        val lat = dmToDegrees(p[2], p[3]) ?: return null
        val lon = dmToDegrees(p[4], p[5]) ?: return null
        val quality = p[6].toIntOrNull() ?: 0
        return GnssFix(
            latitude = lat,
            longitude = lon,
            altitude = p[9].toDoubleOrNull(),
            satellites = p[7].toIntOrNull() ?: 0,
            hdop = p[8].toDoubleOrNull(),
            fixType = when (quality) { 1 -> FixType.GPS; 2 -> FixType.DGPS; 4 -> FixType.RTK_FIXED; 5 -> FixType.RTK_FLOAT; 6 -> FixType.ESTIMATED; else -> FixType.NONE },
            utcTime = p.getOrNull(1)?.ifBlank { null },
            correctionAgeSeconds = p.getOrNull(13)?.toDoubleOrNull(),
            baseStationId = p.getOrNull(14)?.ifBlank { null }
        )
    }

    private fun dmToDegrees(value: String, hemi: String): Double? {
        val n = value.toDoubleOrNull() ?: return null
        val degrees = kotlin.math.floor(n / 100.0)
        val minutes = n - degrees * 100.0
        var out = degrees + minutes / 60.0
        if (hemi == "S" || hemi == "W") out = -out
        return out
    }

    private fun checksumValid(sentence: String): Boolean {
        val star = sentence.indexOf("*")
        if (star < 0) return true
        val expected = sentence.substring(star + 1).take(2).toIntOrNull(16) ?: return false
        var checksum = 0
        for (i in 1 until star) checksum = checksum xor sentence[i].code
        return checksum == expected
    }
}