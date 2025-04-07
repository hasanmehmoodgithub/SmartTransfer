package com.smart.transfer.app.features.localshare.ui.wifdirect


import com.smart.transfer.app.R
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class WiFiDirectActivity2 : AppCompatActivity(), WifiP2pManager.ConnectionInfoListener {

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: Channel
    private lateinit var receiver: BroadcastReceiver
    private lateinit var peersList: ListView
    private lateinit var btnDiscover: Button
    private lateinit var btnSend: Button

    private var peers = mutableListOf<WifiP2pDevice>()
    private var selectedFilePath: String? = null
    private var isGroupOwner = false
    private lateinit var groupOwnerAddress: InetAddress
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1001
        private const val FILE_PICK_REQUEST_CODE = 1002
        private const val PORT = 8888
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wi_fi_direct2)

        peersList = findViewById(R.id.peers_list)
        btnDiscover = findViewById(R.id.btn_discover)
        btnSend = findViewById(R.id.btn_send)
btnSend.setOnClickListener(View.OnClickListener {
    onClickStartServer()
})
        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        setupPermissions()
        setupBroadcastReceiver()
        setupClickListeners()
    }

    private fun setupPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )

        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun setupBroadcastReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                            Toast.makeText(context, "Wi-Fi Direct is enabled", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Wi-Fi Direct is not enabled", Toast.LENGTH_SHORT).show()
                        }
                    }
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        if (ActivityCompat.checkSelfPermission(
                                this@WiFiDirectActivity2,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                                this@WiFiDirectActivity2,
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
                        manager.requestPeers(channel) { peersList ->
                            peers.clear()
                            peers.addAll(peersList.deviceList)
                            updatePeersList()
                        }
                    }
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                        if (networkInfo?.isConnected == true) {
                            manager.requestConnectionInfo(channel, this@WiFiDirectActivity2)
                        } else {
                            Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        registerReceiver(receiver, intentFilter)
    }

    private fun setupClickListeners() {
        btnDiscover.setOnClickListener {
            discoverPeers()
        }

//        btnSend.setOnClickListener {
//            // In a real app, you would launch a file picker here
//            // For simplicity, we'll use a hardcoded file path
//            selectedFilePath = "/path/to/your/file.jpg"
//            if (isGroupOwner) {
//                Toast.makeText(this, "Group owner can't send files", Toast.LENGTH_SHORT).show()
//            } else if (selectedFilePath != null) {
//                // In a real app, you would have selected a peer first
//                // Here we assume the first peer is the target
//                if (peers.isNotEmpty()) {
//                    connectToPeer(peers[0])
//
//                }
//            } else {
//                Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show()
//            }
//        }

        peersList.setOnItemClickListener { _, _, position, _ ->
            connectToPeer(peers[position])
        }
    }

    private fun discoverPeers() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
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
        manager.discoverPeers(channel, object : ActionListener {
            override fun onSuccess() {
                Toast.makeText(this@WiFiDirectActivity2, "Discovery Started", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(reasonCode: Int) {
                Toast.makeText(this@WiFiDirectActivity2, "Discovery Failed: $reasonCode", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePeersList() {
        val deviceNames = peers.map { it.deviceName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)
        peersList.adapter = adapter
    }

    private fun connectToPeer(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
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
        manager.connect(channel, config, object : ActionListener {
            override fun onSuccess() {
                Toast.makeText(this@WiFiDirectActivity2, "Connected to ${device.deviceName}", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(this@WiFiDirectActivity2, "Failed to connect: $reason", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo) {
        // This is called when connection info is available
        isGroupOwner = info.isGroupOwner

        if (info.groupFormed) {
            if (info.isGroupOwner) {
                // This device is the server/group owner
                startServerSocket()
            } else {
                // This device is the client - get group owner address
                groupOwnerAddress = info.groupOwnerAddress
                Toast.makeText(this, "Connected to group owner: ${groupOwnerAddress.hostAddress}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendFiles(fileList: List<File>, hostAddress: InetAddress) {
        Thread {
            try {
                val socket = Socket(hostAddress, PORT)
                val outputStream = socket.getOutputStream()
                val dataOutputStream = DataOutputStream(outputStream)

                // Send number of files first
                dataOutputStream.writeInt(fileList.size)

                for (file in fileList) {
                    // Send file name and size
                    dataOutputStream.writeUTF(file.name)
                    dataOutputStream.writeLong(file.length())

                    // Send file content
                    FileInputStream(file).use { fileStream ->
                        fileStream.copyTo(outputStream)
                    }

                    runOnUiThread {
                        Toast.makeText(this, "Sent: ${file.name}", Toast.LENGTH_SHORT).show()
                    }
                }

                socket.close()
                runOnUiThread {
                    Toast.makeText(this, "All files sent!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Send failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
    fun onClickStartServer() {
        val fileList = AllSelectedFilesManager.allSelectedFiles
            .mapNotNull { it["path"] as? String }
            .map { File(it) }
            .filter { it.exists() }

        if (fileList.isEmpty()) {
            Toast.makeText(this, "No valid files selected", Toast.LENGTH_SHORT).show()
            return
        }

        if (isGroupOwner) {
            startServerSocket() // Your existing server function
        } else {
            sendFiles(fileList, groupOwnerAddress) // Use the new send function
        }
    }
    private fun startServerSocket() {
        Thread {
            try {
                ServerSocket(PORT).use { serverSocket ->
                    val clientSocket = serverSocket.accept()
                    val inputStream = DataInputStream(clientSocket.getInputStream())

                    val numFiles = inputStream.readInt()

                    for (i in 0 until numFiles) {
                        val fileName = inputStream.readUTF()
                        val fileSize = inputStream.readLong()

                        File(getExternalFilesDir(null), fileName).outputStream().use { fileOut ->
                            var remaining = fileSize
                            val buffer = ByteArray(1024)

                            while (remaining > 0) {
                                val read = inputStream.read(buffer, 0, minOf(buffer.size, remaining.toInt()))
                                if (read == -1) break
                                fileOut.write(buffer, 0, read)
                                remaining -= read
                            }
                        }

                        runOnUiThread {
                            Toast.makeText(this, "Received: $fileName", Toast.LENGTH_SHORT).show()
                        }
                    }

                    clientSocket.close()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Receive error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun connectToServer(serverAddress: InetAddress) {
        Thread {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(serverAddress, PORT), 5000)

                // Send file
                val file = File(selectedFilePath)
                val inputStream = FileInputStream(file)
                val outputStream = BufferedOutputStream(socket.getOutputStream())

                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                socket.close()

                runOnUiThread {
                    Toast.makeText(this, "File sent successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to send file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        })
    }
}