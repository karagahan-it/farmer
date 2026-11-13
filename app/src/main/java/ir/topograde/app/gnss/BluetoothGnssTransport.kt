package ir.topograde.app.gnss

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class BluetoothGnssTransport(
    private val adapter: BluetoothAdapter,
    private val device: BluetoothDevice,
    private val serviceUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
) : GnssTransport {
    private val _state = MutableStateFlow<GnssConnectionState>(GnssConnectionState.Disconnected)
    override val state: Flow<GnssConnectionState> = _state
    private var socket: BluetoothSocket? = null

    @SuppressLint("MissingPermission") override suspend fun connect() = withContext(Dispatchers.IO) {
        _state.value = GnssConnectionState.Connecting
        runCatching { adapter.cancelDiscovery(); device.createRfcommSocketToServiceRecord(serviceUuid).also { it.connect(); socket = it } }
            .onSuccess { _state.value = GnssConnectionState.Connected(device.name ?: device.address) }
            .onFailure { _state.value = GnssConnectionState.Error(it.message ?: "Bluetooth connection failed") }
        Unit
    }

    override val lines: Flow<String> = callbackFlow {
        val current = socket
        if (current == null || !current.isConnected) { close(IllegalStateException("Bluetooth GNSS is not connected")); return@callbackFlow }
        val thread = Thread {
            runCatching { BufferedReader(InputStreamReader(current.inputStream)).use { reader -> while (!Thread.currentThread().isInterrupted) { val line = reader.readLine() ?: break; trySend(line) } } }
                .onFailure { _state.value = GnssConnectionState.Error(it.message ?: "Bluetooth read error") }
            close()
        }
        thread.start(); awaitClose { thread.interrupt() }
    }

    override suspend fun write(bytes: ByteArray) = withContext(Dispatchers.IO) {
        val current = socket ?: error("Bluetooth GNSS is not connected")
        current.outputStream.write(bytes); current.outputStream.flush()
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) { runCatching { socket?.close() }; socket = null; _state.value = GnssConnectionState.Disconnected }
}