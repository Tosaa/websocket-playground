import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import kotlin.Exception

actual class ServerImpl : Server {
    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private var websocket: WebSocketServer? = null
        set(value) {
            if (value == null) {
                backgroundScope.launch {
                    println("websocket = null -> stop previous websocket")
                    field?.stop()
                }
            }
            field = value
            _isRunning.value = value != null
        }

    private val _connections = MutableStateFlow<List<ConnectionInfo>>(emptyList())
    private val _isRunning = MutableStateFlow(false)
    private val _receivedBytes = MutableStateFlow(ByteArray(0))

    override fun start(port: String): Boolean {
        return if (websocket != null) {
            println("start(): Already started")
            false
        } else {
            port.toIntOrNull()?.let {
                websocket = MyWebSocketServer(
                    port = it,
                    onConnected = { socket ->

                        val existingSocket =
                            _connections.value.firstOrNull { it.identifier == socket.resourceDescriptor }
                        if (existingSocket == null) {
                            val newConnectionInfo = ConnectionInfo(socket.resourceDescriptor).also {
                                it.setConnected(true)
                            }
                            _connections.value = _connections.value.plus(newConnectionInfo)
                        } else {
                            existingSocket.setConnected(true)
                        }
                    },
                    onDisconnected = { socket ->
                        val existingSocket =
                            _connections.value.firstOrNull { it.identifier == socket.resourceDescriptor }
                        if (existingSocket == null) {
                            val newConnectionInfo = ConnectionInfo(socket.resourceDescriptor).also {
                                it.setConnected(false)
                            }
                            _connections.value = _connections.value.plus(newConnectionInfo)
                        } else {
                            existingSocket.setConnected(false)
                        }
                    },
                    onReceivedBytes = { socket, bytes ->
                        _connections.value.firstOrNull { it.identifier == socket.resourceDescriptor }
                            ?.receivedMessage(bytes)
                    },
                    onError = { socket, error ->
                        _connections.value.firstOrNull<ConnectionInfo> { it.identifier == socket.resourceDescriptor }
                            ?.setError(error)
                    }
                ).also { webSocketServer ->
                    println("start(): Starting $webSocketServer")
                    backgroundScope.launch {
                        try {
                            webSocketServer.start()
                            println("start(): webSocketServer started")
                            println("Local IP Address is: ${Inet4Address.getLocalHost()}")
                        } catch (e: Exception) {
                            println("start(): Error during start of webSocketServer ${e.stackTraceToString()}")
                        }
                    }
                }
                true
            } ?: false.also {
                println("start(): Cannot convert port $port to Int")
            }
        }
    }

    override fun stop() {
        val activeSocket = websocket
        if (activeSocket != null) {
            _isRunning.value = false
            activeSocket.stop()
            websocket = null
        }
    }

    override fun sentMessageToAll(byteArray: ByteArray): Boolean {
        return websocket?.let {
            it.broadcast(byteArray)
            true
        } ?: false
    }

    override val isRunning: StateFlow<Boolean>
        get() = _isRunning

    override val receivedBytes: StateFlow<ByteArray>
        get() = _receivedBytes

    override val connections: StateFlow<List<ConnectionInfo>> = _connections

    private class MyWebSocketServer(
        port: Int? = null,
        val onConnected: (org.java_websocket.WebSocket) -> Unit,
        val onDisconnected: (org.java_websocket.WebSocket) -> Unit,
        val onReceivedBytes: (org.java_websocket.WebSocket, ByteArray) -> Unit,
        val onError: (org.java_websocket.WebSocket, String) -> Unit
    ) : WebSocketServer(InetSocketAddress(port ?: STD_PORT)) {

        init {
            isReuseAddr = true
        }

        override fun onOpen(conn: org.java_websocket.WebSocket?, handshake: ClientHandshake?) {
            conn ?: return
            handshake ?: return
            println("onOpen(): $conn, $handshake")
            onConnected(conn)
        }

        override fun onClose(conn: org.java_websocket.WebSocket?, code: Int, reason: String?, remote: Boolean) {
            conn ?: return
            reason ?: return
            println("onClose(): $conn, $code, $reason, $remote")
            onDisconnected(conn)
        }

        override fun onMessage(conn: org.java_websocket.WebSocket?, message: String?) {
            conn ?: return
            message ?: return
            println("onMessage(): $conn, $message")
        }

        override fun onError(conn: org.java_websocket.WebSocket?, ex: Exception?) {
            conn ?: return
            ex ?: return
            println("onError(): $conn, ${ex.stackTraceToString()}")
            onError(conn, ex.message ?: ex.localizedMessage)
        }

        override fun onStart() {
            println("onStart()")
        }

        override fun stop() {
            super.stop()
            println("stop()")
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun onMessage(conn: org.java_websocket.WebSocket?, message: ByteBuffer?) {
            conn ?: return
            message ?: return
            super.onMessage(conn, message)
            println("onMessage(): $conn, ${message.array().toHexString()}")
            onReceivedBytes(conn, message.array())
        }

        override fun toString(): String {
            return "${this.javaClass.simpleName}(address=${this.address}, port=${this.port}, connections=${this.connections.size})"
        }
    }


    companion object {
        private val STD_PORT = 50123
    }
}