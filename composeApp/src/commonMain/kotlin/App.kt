import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(ipaddress: String = "Unknown IPAddress") {
    MaterialTheme {
        val isServerOpen = remember { mutableStateOf(false) }
        val server = remember { mutableStateOf<Server>(ServerImpl()) }
        val isClientOpen = remember { mutableStateOf(false) }
        val client = remember { mutableStateOf<Client>(ClientImpl()) }
        when {
            isServerOpen.value && isClientOpen.value -> {
                Row {
                    Box(Modifier.padding(4.dp).weight(1f)) {
                        Server(server.value, ipaddress) {
                            isServerOpen.value = false
                        }
                    }
                    Spacer(
                        Modifier.width(1.dp).background(Color.LightGray).fillMaxHeight()
                            .padding(horizontal = 2.dp)
                    )
                    Box(Modifier.padding(4.dp).weight(1f)) {
                        Client(client.value) {
                            isClientOpen.value = false
                        }
                    }
                }
            }

            isServerOpen.value -> Box(Modifier.padding(4.dp)) {
                Server(server.value, ipaddress) {
                    isServerOpen.value = false
                }
            }

            isClientOpen.value -> Box(Modifier.padding(4.dp)) {
                Client(client.value) {
                    isClientOpen.value = false
                }
            }

            else -> {
                Row(Modifier.padding(4.dp)) {
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
fun Server(server: Server, ipaddress: String, onBackPressed: () -> Unit) {
    Column {
        val isRunning = server.isRunning.collectAsState()
        val port = remember { mutableStateOf("") }
        Row {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                "Back arrow",
                Modifier.clickable { onBackPressed() })
            Text("Server")
        }
        Row {
            TextField(ipaddress, { }, label = { Text("Address") }, readOnly = true)
            TextField(port.value, { port.value = it }, label = { Text("Port") })
        }
        if (isRunning.value) {
            Button({ server.stop() }) {
                Text("Stop Server")
            }
        } else {
            Button({ server.start(port.value) }) {
                Text("Start Server")
            }
        }
        val connections = server.connections.collectAsState()
        connections.value.forEach {
            val receivedMessage = it.lastMessage.collectAsState()
            val decodedResponse = try {
                Json.decodeFromString<Messages.Response>(receivedMessage.value.decodeToString())
            } catch (e: Exception) {
                null
            }
            val error = it.error.collectAsState()
            val connected = it.isConnected.collectAsState()
            Card(border = BorderStroke(1.dp, Color.LightGray)) {
                Column(Modifier.padding(10.dp)) {
                    val connectedString = if (connected.value) "Connected" else "Disconnected"
                    OutlinedTextField(
                        it.identifier,
                        {},
                        readOnly = true,
                        label = { Text("Identifier: $connectedString") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        decodedResponse?.toString()?.let { "Decoded: $it" } ?: receivedMessage.value.decodeToString(),
                        {},
                        readOnly = true,
                        label = { Text("Received String") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        receivedMessage.value.toHexString(),
                        {},
                        readOnly = true,
                        label = { Text("Received Bytes") },
                        singleLine = true
                    )
                    OutlinedTextField(error.value, {}, readOnly = true, label = { Text("Error") })
                }
            }
        }

        val message = remember { mutableStateOf("") }
        OutlinedTextField(
            message.value,
            { message.value = it },
            enabled = isRunning.value && connections.value.isNotEmpty(),
            label = { Text("Message to send") }
        )
        Button(
            {
                server.sentMessageToAll(message.value.encodeToByteArray())
                message.value = ""
            },
            enabled = isRunning.value && connections.value.isNotEmpty()
        ) {
            Text("Send MSG")
        }
        Button(
            {

                server.sentMessageToAll(Json.encodeToString<Messages.Request>(Messages.Request(message.value)).encodeToByteArray())
                message.value = ""
            },
            enabled = isRunning.value && connections.value.isNotEmpty()
        ) {
            Text("Send Request")
        }
    }
}

@Composable
fun Client(client: Client, onBackPressed: () -> Unit) {
    Column {
        val isConnected = client.isConnected.collectAsState()
        val address = remember { mutableStateOf("192.168.178.40") }
        val port = remember { mutableStateOf("") }
        Row {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                "Back arrow",
                Modifier.clickable { onBackPressed() })
            Text("Client")
        }
        Row {
            TextField(
                address.value,
                { address.value = it },
                label = { Text("Address", maxLines = 1) },
                modifier = Modifier.weight(3f)
            )
            TextField(
                port.value,
                { port.value = it },
                label = { Text("Port", maxLines = 1) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        if (isConnected.value) {
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

        val bytes = client.receivedBytes.collectAsState()
        val decodedRequest = try {
            Json.decodeFromString<Messages.Request>(bytes.value.decodeToString())
        } catch (e: Exception) {
            null
        }
        OutlinedTextField(
            decodedRequest?.toString()?.let { "Decoded: $it" } ?: bytes.value.decodeToString(),
            {},
            readOnly = true,
            label = { Text("Received") })

        val message = remember { mutableStateOf("") }
        OutlinedTextField(message.value, { message.value = it }, enabled = isConnected.value, label = { Text("Message to send") })
        Button({
            client.sentMessage(message.value.encodeToByteArray())
            message.value = ""
        }, enabled = isConnected.value) {
            Text("Send MSG")
        }

        Button({
            client.sentMessage(Json.encodeToString(Messages.Response(message.value)).encodeToByteArray())
            message.value = ""
        }, enabled = isConnected.value) {
            Text("Send Response")
        }

    }
}