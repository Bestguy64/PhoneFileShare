package com.example.fileshare

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var server: FileServer? = null
    private val port = 8080

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvUrl = findViewById<TextView>(R.id.tvUrl)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)

        btnStart.setOnClickListener {
            try {
                server = FileServer(this, port)
                server?.start()
                val ip = getLocalIpAddress()
                tvUrl.text = "http://$ip:$port"
            } catch (e: Exception) {
                tvUrl.text = "Start failed: ${e.message}"
            }
        }

        btnStop.setOnClickListener {
            server?.stop()
            tvUrl.text = "Server stopped"
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (_: Exception) { }
        return "127.0.0.1"
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
    }
}
