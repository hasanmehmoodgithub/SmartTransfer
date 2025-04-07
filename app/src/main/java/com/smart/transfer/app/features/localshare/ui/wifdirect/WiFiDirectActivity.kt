package com.smart.transfer.app.features.localshare.ui.wifdirect

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.smart.transfer.app.R
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import kotlin.math.log

class WiFiDirectActivity : AppCompatActivity() {

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver
    private val intentFilter = IntentFilter()
    private val peers = mutableListOf<WifiP2pDevice>()
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var discoverBtn: Button
    private lateinit var selectFilesBtn: Button
    private var groupOwnerIp: String? = null
    private val PORT = 8080
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wi_fi_direct)

        discoverBtn = findViewById(R.id.discoverBtn)
        selectFilesBtn = findViewById(R.id.selectFilesBtn)
        listView = findViewById(R.id.peersListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView.adapter = adapter

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        discoverBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission handling
                return@setOnClickListener
            }
            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Toast.makeText(this@WiFiDirectActivity, "Discovery Started", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(this@WiFiDirectActivity, "Discovery Failed: $reason", Toast.LENGTH_SHORT).show()
                }
            })
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val device = peers[position]
            val config = WifiP2pConfig().apply {
                deviceAddress = device.deviceAddress
            }

            manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Toast.makeText(this@WiFiDirectActivity, "Connecting to ${device.deviceName}", Toast.LENGTH_SHORT).show()

                    manager.requestConnectionInfo(channel) { info ->
                        if (info.groupFormed) {
                            // Retrieve the groupOwner IP (this device if it's the group owner)
                            groupOwnerIp = info.groupOwnerAddress.hostAddress
                            discoverBtn.text=groupOwnerIp
                            Log.d("WiFiDirect", "Group Owner IP: $groupOwnerIp")

                            // Now you can call sendFiles with the groupOwnerIp

                        }
                    }
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(this@WiFiDirectActivity, "Connection failed: $reason", Toast.LENGTH_SHORT).show()
                }
            })
        }

        selectFilesBtn.setOnClickListener {
            sendFilesFromPath(groupOwnerIp!!)

        }

        setupReceiver()
        getLocalIpAddress()
    }

    private fun setupReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        if (ActivityCompat.checkSelfPermission(
                                this@WiFiDirectActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                                this@WiFiDirectActivity,
                                Manifest.permission.NEARBY_WIFI_DEVICES
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        manager.requestPeers(channel) { peerList ->
                            peers.clear()
                            peers.addAll(peerList.deviceList)
                            adapter.clear()
                            adapter.addAll(peers.map { it.deviceName })
                        }
                    }
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                        if (networkInfo?.isConnected == true) {
                            manager.requestConnectionInfo(channel) { info ->
                                if (info.groupFormed) {
                                    groupOwnerIp = info.groupOwnerAddress.hostAddress
                                    Log.d("WiFiDirect", "Group Owner IP: $groupOwnerIp")
                                    if (info.isGroupOwner) {
                                        startReceiverServer()
                                    } else {
                                        selectFilesBtn.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startReceiverServer() {
        val serverThread = Thread {
            try {
                val serverSocket = ServerSocket(PORT)
                while (true) {
                    val socket = serverSocket.accept()
                    val input = socket.getInputStream()

                    // Read the file name (including extension) from the incoming data
                    val fileNameBuffer = ByteArray(1024)  // Adjust buffer size if necessary
                    val fileNameLength = input.read(fileNameBuffer)
                    val fileName = String(fileNameBuffer, 0, fileNameLength).trim()  // Extract the file name

                    // Create a file in the Downloads directory with the real name and extension
                    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadDir, fileName)

                    // Output stream to save the file
                    val output = FileOutputStream(file)
                    input.copyTo(output)

                    runOnUiThread {
                        Toast.makeText(this, "File received: ${file.name}", Toast.LENGTH_SHORT).show()
                    }

                    socket.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Server error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        serverThread.start()
    }






    private fun sendFilesFromPath(groupOwnerIp: String) {
        // Get the file paths from the AllSelectedFilesManager
        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }

        if (paths.isEmpty()) {
            Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                // Connect to the group owner
                val socket = Socket(groupOwnerIp, PORT)
                val output = socket.getOutputStream()

                for (path in paths) {
                    val file = File(path)
                    if (file.exists()) {
                        val input = file.inputStream()
                        val fileName = file.name  // Preserve original file name with extension

                        // Send the file name (with extension) first
                        output.write(fileName.toByteArray()) // Send the file name
                        output.flush()  // Ensure the file name is sent immediately

                        // Now send the actual file content
                        input.copyTo(output) // Send file content
                        input.close()
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "File not found: $path", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                socket.close()

                runOnUiThread {
                    Toast.makeText(this, "Files Sent", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Log.e("Failed", "${e.message} $e")
                    Toast.makeText(this, "Failed to send files: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }





    override fun onResume() {
        super.onResume()
       registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
      unregisterReceiver(receiver)
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

