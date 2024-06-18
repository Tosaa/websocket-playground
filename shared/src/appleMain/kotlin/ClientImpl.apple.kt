import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class ClientImpl : Client {
    override fun connectOnLocalHost(port: String) {
        println("Not yet implemented")
    }

    override fun connect(address: String) {
        println("Not yet implemented")
    }

    override fun disconnect() {
        println("Not yet implemented")
    }

    override fun sentMessage(byteArray: ByteArray): Boolean {
        println("Not yet implemented")
        return false
    }

    override val isConnected: StateFlow<Boolean>
        get() = MutableStateFlow(false)

    override val receivedBytes: StateFlow<ByteArray>
        get() = MutableStateFlow(ByteArray(0))
}
