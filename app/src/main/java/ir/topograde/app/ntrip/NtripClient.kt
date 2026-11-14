package ir.topograde.app.ntrip

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.Socket
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NtripConfig(
    val host: String,
    val port: Int = 2101,
    val mountPoint: String,
    val username: String,
    val password: String
)

class NtripClient(private val config: NtripConfig) {
    private var socket: Socket? = null

    suspend fun connect(onRtcm: suspend (ByteArray) -> Unit) = withContext(Dispatchers.IO) {
        val s = Socket(config.host, config.port); socket = s
        val out = BufferedOutputStream(s.getOutputStream())
        val token = Base64.getEncoder().encodeToString("${config.username}:${config.password}".toByteArray())
        val request = buildString {
            append("GET /${config.mountPoint} HTTP/1.0\r\n")
            append("User-Agent: NTRIP TopoGrade/0.5\r\n")
            append("Authorization: Basic $token\r\n")
            append("Accept: */*\r\n\r\n")
        }
        out.write(request.toByteArray()); out.flush()
        val input = BufferedInputStream(s.getInputStream())
        val header = StringBuilder(); var matched = 0
        while (true) {
            val b = input.read(); if (b < 0) error("NTRIP closed during headers")
            header.append(b.toChar())
            matched = if ((matched == 0 && b == 13) || (matched == 1 && b == 10) || (matched == 2 && b == 13) || (matched == 3 && b == 10)) matched + 1 else if (b == 13) 1 else 0
            if (matched == 4) break
            if (header.length > 8192) error("NTRIP header too large")
        }
        val first = header.lineSequence().firstOrNull().orEmpty()
        if (!(first.contains("200") || first.startsWith("ICY 200"))) error("NTRIP rejected: $first")
        val buffer = ByteArray(4096)
        while (!s.isClosed) {
            val n = input.read(buffer); if (n <= 0) break
            onRtcm(buffer.copyOf(n))
        }
    }

    fun close() { runCatching { socket?.close() }; socket = null }
}