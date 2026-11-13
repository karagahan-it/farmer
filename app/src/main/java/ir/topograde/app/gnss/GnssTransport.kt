package ir.topograde.app.gnss

import kotlinx.coroutines.flow.Flow

sealed interface GnssConnectionState {
    data object Disconnected : GnssConnectionState
    data object Connecting : GnssConnectionState
    data class Connected(val description: String) : GnssConnectionState
    data class Error(val message: String) : GnssConnectionState
}

interface GnssTransport {
    val state: Flow<GnssConnectionState>
    val lines: Flow<String>
    suspend fun connect()
    suspend fun disconnect()
    suspend fun write(bytes: ByteArray)
}