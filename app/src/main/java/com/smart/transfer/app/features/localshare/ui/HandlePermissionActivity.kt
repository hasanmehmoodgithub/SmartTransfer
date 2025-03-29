package com.smart.transfer.app.features.localshare.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import com.smart.transfer.app.com.smart.transfer.app.core.appenums.ChooseFileNextScreenType
import com.smart.transfer.app.databinding.ActivityHandlePermissionBinding
import com.smart.transfer.app.features.dashboard.ui.DashboardActivity

class HandlePermissionActivity : BaseActivity() {

    private lateinit var binding: ActivityHandlePermissionBinding
    private var from: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHandlePermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<LinearLayout>(R.id.custom_toolbar)
        setupAppBar(toolbar, "Permission", showBackButton = true)

         from = intent.getIntExtra("from",1)

        // Check permissions on startup
        checkPermissions()

        // Button Click Listeners
        binding.btnLocation.setOnClickListener { requestLocationPermission() }
        binding.btnWifi.setOnClickListener { enableWifi() }
        binding.btnNearby.setOnClickListener { requestNearbyWifiPermission() }
    }

    /**
     * Checks all required permissions and updates the UI accordingly.
     */
    private fun checkPermissions() {
        val isLocationGranted = isLocationPermissionGranted()
        val isWifiEnabled = isWifiEnabled()
        val isNearbyWifiGranted = isNearbyWifiPermissionGranted()

        // Update UI elements
        updateUI(isLocationGranted, binding.imgLocation, binding.btnLocation)
        updateUI(isWifiEnabled, binding.imgWifi, binding.btnWifi)

        // Show Nearby Wi-Fi Devices permission only for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            updateUI(isNearbyWifiGranted, binding.imgNearby, binding.btnNearby)
        } else {
            binding.NearByServiceCard.visibility = View.GONE

        }

        // Navigate to Main Screen if all conditions are met
        if (isLocationGranted && isWifiEnabled && (isNearbyWifiGranted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)) {
            navigateToMain()
        }
    }

    /**
     * Updates UI elements based on permission states.
     */
    private fun updateUI(isGranted: Boolean, imageView: View, button: View) {
        imageView.visibility = if (isGranted) View.VISIBLE else View.INVISIBLE
        button.visibility = if (isGranted) View.GONE else View.VISIBLE
    }

    /**
     * Checks if location permission is granted.
     */
    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if Wi-Fi is enabled.
     */
    private fun isWifiEnabled(): Boolean {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    /**
     * Checks if NEARBY_WIFI_DEVICES permission is granted (Android 13+).
     */
    private fun isNearbyWifiPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }

    /**
     * Requests location permission and handles permanent denial.
     */
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Permission Denied Once → Show Explanation
            showPermissionExplanationDialog(
                "Location Permission Required",
                "This app requires location access to function properly.",
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            // Request Permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Opens Wi-Fi settings to enable Wi-Fi.
     */
    private fun enableWifi() {
        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    /**
     * Requests NEARBY_WIFI_DEVICES permission only if on Android 13+.
     */
    private fun requestNearbyWifiPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.NEARBY_WIFI_DEVICES)) {
                // Permission Denied Once → Show Explanation
                showPermissionExplanationDialog(
                    "Nearby Wi-Fi Permission Required",
                    "This app needs Nearby Wi-Fi access for sharing data.",
                    Manifest.permission.NEARBY_WIFI_DEVICES
                )
            } else {
                // Request Permission
                requestPermissionLauncher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }
    }

    /**
     * Handles permission request results and updates UI accordingly.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkPermissions() // Re-check permissions and update UI
            } else {
                // Check if user permanently denied permission
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showSettingsDialog("Location Permission Required", "You have permanently denied location access. Please enable it in settings.")
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.NEARBY_WIFI_DEVICES)) {
                    showSettingsDialog("Nearby Wi-Fi Permission Required", "You have permanently denied Nearby Wi-Fi access. Please enable it in settings.")
                }
            }
        }

    /**
     * Shows a dialog explaining why the permission is needed.
     */
    private fun showPermissionExplanationDialog(title: String, message: String, permission: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Allow") { _, _ -> requestPermissionLauncher.launch(permission) }
            .setNegativeButton("Deny") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Shows a settings dialog when a permission is permanently denied.
     */
    private fun showSettingsDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Navigates to the main dashboard if all permissions are granted.
     */
    private fun navigateToMain() {
//        if(from==1)
//        {
//            //share
//                    startActivity(Intent(this, DashboardActivity::class.java))
//        finish()
//        }
//        else{
//            //receive
//                    startActivity(Intent(this, DashboardActivity::class.java))
//        finish()
//        }

    }
}
