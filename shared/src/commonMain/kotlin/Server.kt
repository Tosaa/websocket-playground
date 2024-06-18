import kotlinx.coroutines.flow.StateFlow

interface Server {
    fun start(port:String):Boolean
    fun stop()
    fun sentMessageToAll(byteArray: ByteArray):Boolean
    val isRunning : StateFlow<Boolean>
    val receivedBytes : StateFlow<ByteArray>
    val connections : StateFlow<List<ConnectionInfo>>

}