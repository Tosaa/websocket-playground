import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import platform.posix.fabs

actual class ServerImpl : Server {
    override fun start(port: String): Boolean {
        println("Not yet implemented")
        return false
    }

    override fun stop() {
        println("Not yet implemented")
    }

    override fun sentMessageToAll(byteArray: ByteArray): Boolean {
        println("Not yet implemented")
        return false
    }

    override val isRunning: StateFlow<Boolean>
        get() = MutableStateFlow(false)

    override val receivedBytes: StateFlow<ByteArray>
        get() = MutableStateFlow(ByteArray(0))

    override val connections: StateFlow<List<ConnectionInfo>>
        get() = MutableStateFlow(emptyList())
}