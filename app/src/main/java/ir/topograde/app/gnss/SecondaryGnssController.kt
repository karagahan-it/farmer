package ir.topograde.app.gnss

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SecondaryGnssController(private val scope: CoroutineScope) {
    private val _state = MutableStateFlow<GnssConnectionState>(GnssConnectionState.Disconnected)
    val state: StateFlow<GnssConnectionState> = _state.asStateFlow()
    private val _fix = MutableStateFlow<GnssFix?>(null)
    val fix: StateFlow<GnssFix?> = _fix.asStateFlow()
    private val _lastFixElapsedMs = MutableStateFlow<Long?>(null)
    val lastFixElapsedMs: StateFlow<Long?> = _lastFixElapsedMs.asStateFlow()
    private val _quality = MutableStateFlow(GnssQuality())
    val quality: StateFlow<GnssQuality> = _quality.asStateFlow()
    private var job: Job? = null
    private var transport: GnssTransport? = null

    fun start(factory: () -> GnssTransport) {
        stop()
        _fix.value = null
        _lastFixElapsedMs.value = null
        _quality.value = GnssQuality()
        job = scope.launch {
            val t = factory()
            transport = t
            _state.value = GnssConnectionState.Connecting
            runCatching { t.connect() }.onFailure {
                _state.value = GnssConnectionState.Error(it.message ?: "Secondary GNSS connect failed")
                return@launch
            }
            val connection = runCatching { t.state.first { it !is GnssConnectionState.Connecting } }
                .getOrElse { GnssConnectionState.Error(it.message ?: "Secondary GNSS state error") }
            _state.value = connection
            if (connection !is GnssConnectionState.Connected) return@launch

            runCatching {
                t.lines.collect { line ->
                    NmeaParser.parse(line)?.let { parsed ->
                        _fix.value = parsed
                        _lastFixElapsedMs.value = SystemClock.elapsedRealtime()
                    }
                    NmeaQualityParser.parse(line)?.let { parsedQuality ->
                        _quality.value = _quality.value.merge(parsedQuality)
                    }
                }
            }.onFailure {
                _state.value = GnssConnectionState.Error(it.message ?: "Secondary GNSS stream error")
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        val current = transport
        transport = null
        if (current != null) scope.launch { runCatching { current.disconnect() } }
        _fix.value = null
        _lastFixElapsedMs.value = null
        _quality.value = GnssQuality()
        _state.value = GnssConnectionState.Disconnected
    }

    private fun GnssQuality.merge(other: GnssQuality) = GnssQuality(
        pdop = other.pdop ?: pdop,
        hdop = other.hdop ?: hdop,
        vdop = other.vdop ?: vdop,
        horizontalSigmaMeters = other.horizontalSigmaMeters ?: horizontalSigmaMeters,
        verticalSigmaMeters = other.verticalSigmaMeters ?: verticalSigmaMeters,
        rmsMeters = other.rmsMeters ?: rmsMeters
    )
}
