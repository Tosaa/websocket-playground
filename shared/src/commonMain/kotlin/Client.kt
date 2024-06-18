import kotlinx.coroutines.flow.StateFlow

interface Client {
    fun connectOnLocalHost(port: String)
    fun connect(address: String)
    fun disconnect()
    fun sentMessage(byteArray: ByteArray): Boolean

    val isConnected: StateFlow<Boolean>
    val receivedBytes: StateFlow<ByteArray>
}