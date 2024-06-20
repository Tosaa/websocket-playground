package io.tosaa.playground

import App
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import java.net.Inet4Address


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connectivityManager: ConnectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ipAddresses = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)?.linkAddresses
        val localIPAddress = ipAddresses?.firstOrNull { it.address is Inet4Address }?.address as? Inet4Address
        println("onCreate(): Addresses = ${ipAddresses?.joinToString()}")
        println("onCreate(): local ip address = $localIPAddress")
        setContent {
            App(localIPAddress.toString())
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}