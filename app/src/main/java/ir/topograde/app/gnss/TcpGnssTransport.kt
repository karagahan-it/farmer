package ir.topograde.app.gnss

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class TcpGnssTransport(
    private val host: String,
    private val port: Int,
    private val connectTimeoutMs: Int = 5000
) : GnssTransport {
    private val _state = MutableStateFlow<GnssConnectionState>(GnssConnectionState.Disconnected)
    override val state: Flow<GnssConnectionState> = _state
    private var socket: Socket? = null

    override val lines: Flow<String> = callbackFlow {
        val current = socket
        if (current == null || !current.isConnected) { close(IllegalStateException("TCP GNSS is not connected")); return@callbackFlow }
        val thread = Thread {
            runCatching { BufferedReader(InputStreamReader(current.getInputStream())).use { reader -> while (!Thread.currentThread().isInterrupted) { val line = reader.readLine() ?: break; trySend(line) } } }
                .onFailure { _state.value = GnssConnectionState.Error(it.message ?: "TCP read error") }
            close()
        }
        thread.start(); awaitClose { thread.interrupt() }
    }

    override suspend fun connect() = withContext(Dispatchers.IO) {
        _state.value = GnssConnectionState.Connecting
        runCatching { Socket().also { it.connect(InetSocketAddress(host, port), connectTimeoutMs); socket = it } }
            .onSuccess { _state.value = GnssConnectionState.Connected("$host:$port") }
            .onFailure { _state.value = GnssConnectionState.Error(it.message ?: "TCP connection failed") }
        Unit
    }

    override suspend fun write(bytes: ByteArray) = withContext(Dispatchers.IO) {
        val current = socket ?: error("TCP GNSS is not connected")
        current.getOutputStream().write(bytes); current.getOutputStream().flush()
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) { runCatching { socket?.close() }; socket = null; _state.value = GnssConnectionState.Disconnected }
}