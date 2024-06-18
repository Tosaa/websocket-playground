import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ConnectionInfo(
    val identifier: String,
) {
    fun setConnected(isConnected: Boolean) {
        _isConnected.value = isConnected
    }

    fun receivedMessage(byteArray: ByteArray) {
        _receivedMessages.add(byteArray)
        _lastMessage.value = byteArray
    }

    fun setError(errorMsg: String) {
        _error.value = errorMsg
    }

    private val _receivedMessages = mutableListOf<ByteArray>()
    val receivedMessages: List<ByteArray> = _receivedMessages

    private val _isConnected = MutableStateFlow<Boolean>(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _lastMessage = MutableStateFlow<ByteArray>(ByteArray(0))
    val lastMessage: StateFlow<ByteArray> = _lastMessage

    private val _error = MutableStateFlow<String>("")
    val error: StateFlow<String> = _error
}
