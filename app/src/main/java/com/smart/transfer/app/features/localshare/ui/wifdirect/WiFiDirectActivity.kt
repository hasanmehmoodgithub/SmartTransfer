package com.smart.transfer.app.features.localshare.ui.wifdirect

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.smart.transfer.app.R
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

class WiFiDirectActivity : AppCompatActivity() {
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var peers: MutableList<WifiP2pDevice>
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val peerNames = mutableListOf<String>()

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private companion object {
        private const val PORT = 8888
    }

    private lateinit var discoverBtn: Button
    private lateinit var selectFilesBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wi_fi_direct)

        discoverBtn = findViewById(R.id.discoverBtn)
        selectFilesBtn = findViewById(R.id.selectFilesBtn)
        listView = findViewById(R.id.peersListView)
        peers = mutableListOf()

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, peerNames)
        listView.adapter = adapter

        discoverBtn.setOnClickListener {
            discoverPeers()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val device = peers[position]
            connectToDevice(device)
        }

        selectFilesBtn.setOnClickListener {
//            sendFilesFromPath(groupOwnerIp!!)
        }

    }

    @SuppressLint("MissingPermission")
    private fun discoverPeers() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(this@WiFiDirectActivity, "Discovery Started", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(
                    this@WiFiDirectActivity,
                    "Discovery Failed: $reason",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(
                    this@WiFiDirectActivity,
                    "Connecting to ${device.deviceName}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(reason: Int) {
                val error = when (reason) {
                    WifiP2pManager.P2P_UNSUPPORTED -> "P2P unsupported"
                    WifiP2pManager.BUSY -> "Framework busy"
                    WifiP2pManager.ERROR -> "Internal error"
                    else -> "Unknown error"
                }
                Log.e("WiFi", "Connect failed: $error")
                Toast.makeText(
                    this@WiFiDirectActivity,
                    "Connection failed: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun startFileReceiver() {
        Thread {
            try {
                val serverSocket = ServerSocket(PORT)

                while (true) {
                    val client = serverSocket.accept()
                    val inputStream = client.getInputStream()

                    val file = File(getExternalFilesDir(null), "received_file.jpg")
                    val fos = FileOutputStream(file)

                    val buffer = ByteArray(1024)
                    val bytesRead = inputStream.read(buffer)
                    val text = String(buffer, 0, bytesRead)

                    if (text == "DONE") {
                        Log.d("WiFi", "Receiver: DONE received")
                        break
                    }
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        fos.write(buffer, 0, len)
                    }

                    fos.close()
                    inputStream.close()
                    client.close()
                    serverSocket.close()

                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "File received: ${file.absolutePath}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
                resetSession()
            } catch (e: Exception) {
                Log.e("TAG", "startFileReceiver: $e")
                runOnUiThread {
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    fun sendFile(filePaths: List<String>, hostAddress: String) {
        Thread {
            try {
                for (filePath in filePaths) {
                    val file = File(filePath)
                    if (!file.exists()) {
                        Log.e("WiFi", "File does not exist: $filePath")
                        continue
                    }
                    val socket = Socket()
                    socket.connect(InetSocketAddress(hostAddress, PORT))

                    val outputStream = socket.getOutputStream()

                    val fis = FileInputStream(File(filePath))

                    val buffer = ByteArray(1024)
                    var len: Int
                    while (fis.read(buffer).also { len = it } != -1) {
                        outputStream.write(buffer, 0, len)
                    }

                    fis.close()
                    outputStream.close()
                    socket.close()
                }

                val doneSocket = Socket()
                doneSocket.connect(InetSocketAddress(hostAddress, PORT), 5000)
                val doneStream = doneSocket.getOutputStream()
                doneStream.write("DONE".toByteArray())
                doneStream.flush()
                doneStream.close()
                doneSocket.close()

                resetSession()

                runOnUiThread {
                    Toast.makeText(this, "File sent", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("TAG", "sendFile: $e")
                runOnUiThread {
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }


    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e("TAG", "BroadcastReceiver: ${intent?.action}")
            when (intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    Log.e("TAG", "WIFI_P2P_STATE_CHANGED_ACTION: ")
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Toast.makeText(
                            this@WiFiDirectActivity,
                            "WiFi Direct ON",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@WiFiDirectActivity,
                            "WiFi Direct OFF",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if (ActivityCompat.checkSelfPermission(
                            this@WiFiDirectActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                            this@WiFiDirectActivity,
                            Manifest.permission.NEARBY_WIFI_DEVICES
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    manager.requestPeers(channel) { peerList ->
                        peers.clear()
                        peers.addAll(peerList.deviceList)

                        peerNames.clear()
                        peers.forEach { peerNames.add(it.deviceName) }
                        adapter.notifyDataSetChanged()
                    }
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    Log.e("TAG", "WIFI_P2P_CONNECTION_CHANGED_ACTION:")
                    val networkInfo =
                        intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                    if (networkInfo?.isConnected == true) {
                        manager.requestConnectionInfo(channel) { info ->
                            Log.e("TAG", "WIFI_P2P_CONNECTION_CHANGED_ACTION: $info")
                            if (info.groupFormed) {
                                Toast.makeText(
                                    this@WiFiDirectActivity, "Devices connected", Toast.LENGTH_SHORT
                                ).show()

                                if (info.isGroupOwner) {
                                    startFileReceiver()
                                } else {
                                    val hostAddress = info.groupOwnerAddress.hostAddress
                                    val paths =
                                        AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
                                    if (hostAddress != null) {
                                        sendFile(paths, hostAddress)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun resetSession() {
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.e("WiFi", "Group removed, ready to reconnect")
            }

            override fun onFailure(reason: Int) {
                Log.e("WiFi", "Failed to remove group: $reason")
            }
        })
    }


    override fun onResume() {
        super.onResume()
        registerReceiver(wifiReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(wifiReceiver)
    }


    private fun getLocalIpAddress(): String? {
        try {
            NetworkInterface.getNetworkInterfaces()?.let { interfaces ->
                for (networkInterface in interfaces) {
                    for (address in networkInterface.inetAddresses) {
                        if (!address.isLoopbackAddress && address is Inet4Address) {
                            val ipAddress = address.hostAddress ?: continue
                            runOnUiThread {
                                selectFilesBtn.text = ipAddress

                            }
                            return ipAddress
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("Network", "Failed to get IP address", ex)
        }
        return null
    }
}

