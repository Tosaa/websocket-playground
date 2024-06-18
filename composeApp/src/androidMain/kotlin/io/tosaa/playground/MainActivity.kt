package io.tosaa.playground

import App
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connectivityManager: ConnectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        /*val wifiInf: WifiInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            wifiMan.getNetworkCapabilities(null)?.transportInfo as? WifiInfo
        } else {
            println("Cannot get wifiInfo")
            null
        }*/
        val ipAddress = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)?.linkAddresses
        /*val ip: String = String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )*/
        println("onCreate(): My IPAddress: ${ipAddress?.joinToString()}")

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}