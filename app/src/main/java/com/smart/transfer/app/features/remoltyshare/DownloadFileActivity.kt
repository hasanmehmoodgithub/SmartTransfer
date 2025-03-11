package com.smart.transfer.app.features.remoltyshare



import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.smart.transfer.app.R
import com.smart.transfer.app.databinding.ActivityDownloadFileBinding

class DownloadFileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadFileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityDownloadFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        // Initially, show the paste layout and hide the downloading layout
        binding.codePasteLayout.visibility = View.VISIBLE
        binding.downloadLayout.visibility = View.GONE
//
//        // Handle Done Button Click
        binding.doneBtn.setOnClickListener {
            val pastedText = binding.etLink.text.toString().trim()
            if (pastedText.isNotEmpty()) {
                switchToDownloadLayout()
            }
        }
//
//        // Handle QR Code Button Click (Assume QR Scanner integration)
        binding.codePasteLayout.setOnClickListener {
            scanQRCode()
        }
    }

    // Function to switch from paste layout to downloading layout
    private fun switchToDownloadLayout() {
        binding.codePasteLayout.visibility = View.GONE
        binding.downloadLayout.visibility = View.VISIBLE
    }

    // Mock function for QR Code scanning (replace with actual implementation)
    private fun scanQRCode() {
        // Simulate scanning a QR code and extracting the link
        val scannedLink = "https://example.com/download"
        binding.etLink.setText(scannedLink)
        switchToDownloadLayout()
    }
}
