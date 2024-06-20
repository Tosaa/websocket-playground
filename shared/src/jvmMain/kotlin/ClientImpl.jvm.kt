import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

actual class ClientImpl actual constructor() : Client {
    private var webSocketClient: WebSocketClient? = null
        set(value) {
            if (value == null) {
                backgroundScope.launch {
                    println("websocket = null -> close previous websocket")
                    field?.close()
                }
            }
            field = value
            _isConnected.value = value != null
        }

    private val backgroundScope = CoroutineScope(Dispatchers.IO)

    private val _isConnected = MutableStateFlow(false)
    private val _receivedBytes = MutableStateFlow(ByteArray(0))
    private val _error = MutableStateFlow("")

    override fun connectOnLocalHost(port: String) = connect("ws://localhost:$port")

    override fun connect(address: String) {
        if (webSocketClient == null) {
            println("Try to connect to $address")
            webSocketClient = MyWebSocketClient(
                address = address,
                onConnected = { _isConnected.value = true },
                onDisconnected = {
                    webSocketClient?.close()
                    _isConnected.value = false
                    webSocketClient = null
                },
                onReceivedBytes = { _receivedBytes.value = it },
                onError = { _error.value = it }
            ).also {
                backgroundScope.launch {
                    try {
                        it.connectBlocking(10, TimeUnit.SECONDS)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _error.value = e.message ?: e.stackTraceToString()
                        it.close()
                        _isConnected.value = false
                        webSocketClient = null
                    }
                }
            }
        }
    }

    override fun disconnect() {
        webSocketClient?.let {
            it.close()
            _isConnected.value = false
            webSocketClient = null
        }
    }

    override fun sentMessage(byteArray: ByteArray): Boolean {
        return webSocketClient?.let { client ->
            try {
                client.send(byteArray)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } ?: false
    }

    override val isConnected: StateFlow<Boolean>
        get() = _isConnected

    override val receivedBytes: StateFlow<ByteArray>
        get() = _receivedBytes


    private class MyWebSocketClient(
        address: String,
        val onConnected: () -> Unit,
        val onDisconnected: () -> Unit,
        val onReceivedBytes: (ByteArray) -> Unit,
        val onError: (String) -> Unit
    ) : WebSocketClient(URI(address)) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            println("onOpen(): $handshakedata")
            onConnected()
        }

        override fun onMessage(message: String?) {
            println("onMessage(): $message")
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            println("onClose(): $code, $reason, $remote")
            onDisconnected()
        }

        override fun onError(ex: Exception?) {
            ex ?: return
            println("onError(): $ex")
            onError(ex.message ?: ex.localizedMessage)
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun onMessage(bytes: ByteBuffer?) {
            bytes ?: return
            println("onMessage(): ${bytes.array().toHexString()}")
            onReceivedBytes(bytes.array())
            super.onMessage(bytes)
        }
    }
}