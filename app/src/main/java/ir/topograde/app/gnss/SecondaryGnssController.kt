package ir.topograde.app.gnss

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SecondaryGnssController(private val scope:CoroutineScope){
    private val _state=MutableStateFlow<GnssConnectionState>(GnssConnectionState.Disconnected)
    val state:StateFlow<GnssConnectionState> = _state.asStateFlow()
    private val _fix=MutableStateFlow<GnssFix?>(null)
    val fix:StateFlow<GnssFix?> = _fix.asStateFlow()
    private var job:Job?=null
    private var transport:GnssTransport?=null

    fun start(factory:()->GnssTransport){
        stop()
        job=scope.launch {
            val t=factory(); transport=t; _state.value=GnssConnectionState.Connecting
            runCatching { t.connect() }.onFailure { _state.value=GnssConnectionState.Error(it.message?:"Secondary GNSS connect failed"); return@launch }
            val st=runCatching { t.state.first { it !is GnssConnectionState.Connecting } }.getOrElse { GnssConnectionState.Error(it.message?:"Secondary GNSS state error") }
            _state.value=st
            if(st !is GnssConnectionState.Connected) return@launch
            runCatching { t.lines.collect { line -> NmeaParser.parse(line)?.let { _fix.value=it } } }
                .onFailure { _state.value=GnssConnectionState.Error(it.message?:"Secondary GNSS stream error") }
        }
    }
    fun stop(){ job?.cancel(); job=null; val t=transport; transport=null; if(t!=null)scope.launch{runCatching{t.disconnect()}}; _state.value=GnssConnectionState.Disconnected }
}