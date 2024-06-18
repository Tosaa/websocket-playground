import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val isServerOpen = remember { mutableStateOf(false) }
        val server = remember { mutableStateOf<Server>(ServerImpl()) }
        val isClientOpen = remember { mutableStateOf(false) }
        val client = remember { mutableStateOf<Client>(ClientImpl()) }
        when {
            isServerOpen.value && isClientOpen.value -> {
                Row {
                    Box {
                        Server(server.value) {
                            isServerOpen.value = false
                        }
                    }
                    Box {
                        Client(client.value) {
                            isClientOpen.value = false
                        }
                    }
                }
            }

            isServerOpen.value -> Server(server.value) {
                isServerOpen.value = false
            }

            isClientOpen.value -> Client(client.value) {
                isClientOpen.value = false
            }

            else -> {
                Row {
                    Button({
                        isServerOpen.value = true
                    }) {
                        Text("Server")
                    }
                    Button({
                        isClientOpen.value = true
                    }) {
                        Text("Client")
                    }
                    Button({
                        isServerOpen.value = true
                        isClientOpen.value = true
                    }) {
                        Text("Both")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun Server(server: Server, onBackPressed: () -> Unit) {
    Column {
        val port = remember { mutableStateOf("") }
        Row {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Back arrow", Modifier.clickable { onBackPressed() })
            Text("Server")
        }
        TextField(port.value, { port.value = it })
        val isRunning = server.isRunning.collectAsState()
        if (isRunning.value) {
            val connections = server.connections.collectAsState()
            connections.value.forEach {
                val receivedMessage = it.lastMessage.collectAsState()
                val error = it.error.collectAsState()
                Text(it.identifier)
                Text("Received: ${receivedMessage.value.decodeToString()}")
                Text("Received: ${receivedMessage.value.toHexString()}")
                Text("Error: ${error.value}")
            }

            val message = remember { mutableStateOf("") }
            TextField(message.value, { message.value = it })
            Button({
                server.sentMessageToAll(message.value.encodeToByteArray())
                message.value = ""
            }) {
                Text("Send MSG")
            }

            Button({ server.stop() }) {
                Text("Stop Server")
            }
        } else {
            Button({ server.start(port.value) }) {
                Text("Start Server")
            }
        }
    }
}

@Composable
fun Client(client: Client, onBackPressed: () -> Unit) {
    Column {
        val address = remember { mutableStateOf("192.168.178.40") }
        val port = remember { mutableStateOf("") }
        Row {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Back arrow", Modifier.clickable { onBackPressed() })
            Text("Client")
        }
        TextField(address.value, { address.value = it })
        TextField(port.value, { port.value = it })
        val isConnected = client.isConnected.collectAsState()
        if (isConnected.value) {
            val bytes = client.receivedBytes.collectAsState()
            Text(bytes.value.decodeToString())

            val message = remember { mutableStateOf("") }
            TextField(message.value, { message.value = it })
            Button({
                client.sentMessage(message.value.encodeToByteArray())
                message.value = ""
            }) {
                Text("Send MSG")
            }
            Button({ client.disconnect() }) {
                Text("Disconnect")
            }
        } else {
            Button({
                if (address.value.isEmpty()) {
                    client.connectOnLocalHost(port.value)
                } else {
                    client.connect("ws://${address.value}:${port.value}")
                }
            }) {
                Text("Connect")
            }
        }
    }
}