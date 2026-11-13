package ir.topograde.app.gnss

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GnssSessionController(private val scope: CoroutineScope) {
    private val _state = MutableStateFlow<GnssConnectionState>(GnssConnectionState.Disconnected)
    val state: StateFlow<GnssConnectionState> = _state.asStateFlow()
    private val _fix = MutableStateFlow<GnssFix?>(null)
    val fix: StateFlow<GnssFix?> = _fix.asStateFlow()
    private val _quality = MutableStateFlow(GnssQuality())
    val quality: StateFlow<GnssQuality> = _quality.asStateFlow()
    private val _receiverHeadingDeg = MutableStateFlow<Double?>(null)
    val receiverHeadingDeg: StateFlow<Double?> = _receiverHeadingDeg.asStateFlow()
    private val _samples = MutableSharedFlow<GnssFix>(extraBufferCapacity = 64)
    val samples: SharedFlow<GnssFix> = _samples.asSharedFlow()
    private var sessionJob: Job? = null
    private var currentTransport: GnssTransport? = null
    private var shouldRun = false

    fun start(factory: () -> GnssTransport) {
        stop()
        shouldRun = true
        sessionJob = scope.launch {
            var retryMs = 1500L
            while (shouldRun) {
                val transport = factory()
                currentTransport = transport
                _state.value = GnssConnectionState.Connecting
                runCatching { transport.connect() }
                val connection = runCatching { transport.state.first { it !is GnssConnectionState.Connecting } }
                    .getOrElse { GnssConnectionState.Error(it.message ?: "Connection failed") }
                _state.value = connection
                if (connection is GnssConnectionState.Connected) {
                    retryMs = 1500L
                    var lastLineAt = System.currentTimeMillis()
                    val readJob = launch {
                        runCatching {
                            transport.lines.collect { line ->
                                lastLineAt = System.currentTimeMillis()
                                NmeaParser.parse(line)?.let { parsed ->
                                    _fix.value = parsed
                                    _samples.tryEmit(parsed)
                                }
                                NmeaQualityParser.parse(line)?.let { q -> _quality.value = _quality.value.merge(q) }
                                NmeaHeadingParser.parse(line)?.let { h -> _receiverHeadingDeg.value = h }
                            }
                        }.onFailure { _state.value = GnssConnectionState.Error(it.message ?: "GNSS stream error") }
                    }
                    while (shouldRun && readJob.isActive) {
                        delay(2000)
                        if (System.currentTimeMillis() - lastLineAt > 10000) {
                            _state.value = GnssConnectionState.Error("GNSS stream timeout; reconnecting")
                            readJob.cancelAndJoin()
                            break
                        }
                    }
                    runCatching { transport.disconnect() }
                }
                if (!shouldRun) break
                delay(retryMs)
                retryMs = (retryMs * 2).coerceAtMost(10000L)
            }
        }
    }

    suspend fun write(bytes: ByteArray) { currentTransport?.write(bytes) ?: error("GNSS transport is not connected") }

    fun stop() {
        shouldRun = false
        sessionJob?.cancel()
        sessionJob = null
        val transport = currentTransport
        currentTransport = null
        if (transport != null) scope.launch { runCatching { transport.disconnect() } }
        _state.value = GnssConnectionState.Disconnected
    }

    private fun GnssQuality.merge(other: GnssQuality) = GnssQuality(
        pdop = other.pdop ?: pdop, hdop = other.hdop ?: hdop, vdop = other.vdop ?: vdop,
        horizontalSigmaMeters = other.horizontalSigmaMeters ?: horizontalSigmaMeters,
        verticalSigmaMeters = other.verticalSigmaMeters ?: verticalSigmaMeters,
        rmsMeters = other.rmsMeters ?: rmsMeters
    )
}