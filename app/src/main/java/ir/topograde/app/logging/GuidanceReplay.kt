package ir.topograde.app.logging

data class ReplayFrame(val index:Int,val entry:GuidanceLogEntry)

object GuidanceReplay {
    fun frames(entries:List<GuidanceLogEntry>):List<ReplayFrame> = entries.mapIndexed { index,e -> ReplayFrame(index,e) }
}