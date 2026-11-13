package ir.topograde.app.gnss

object NmeaHeadingParser {
    fun parse(sentence:String):Double? {
        val raw=sentence.trim()
        if(raw.isEmpty() || raw[0].code!=36) return null
        val p=raw.substring(1).substringBefore("*").split(",")
        if(p.firstOrNull()?.endsWith("HDT")!=true) return null
        val heading=p.getOrNull(1)?.toDoubleOrNull() ?: return null
        return ((heading%360.0)+360.0)%360.0
    }
}