import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.net.Inet4Address

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SocketPlayground",
    ) {
        App(Inet4Address.getLocalHost().hostAddress)
    }
}