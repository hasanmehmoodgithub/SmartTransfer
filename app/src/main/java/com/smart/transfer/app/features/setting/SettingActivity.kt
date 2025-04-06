package com.smart.transfer.app.features.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.smart.transfer.app.R
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.smart.transfer.app.databinding.ActivitySettingBinding
import com.smart.transfer.app.com.smart.transfer.app.features.languageScreens.LanguageSelectionActivity


class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
// Get app version dynamically



        // Set version name to UI
     //   binding.appVersionText.text = "Version: $versionName"
        // Initialize View Binding
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button click listener
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        // Language Selection
        binding.llLanguage.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            intent.putExtra("isFromSplash", false)
            startActivity(intent)

        }

        // Privacy Policy
        binding.llPrivacyPolicy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://yourprivacypolicy.com")))
        }

        // Share This App
        binding.llShare.setOnClickListener {
            startActivity(Intent(this, ShareAppActivity::class.java))
//            val shareIntent = Intent(Intent.ACTION_SEND).apply {
//                type = "text/plain"
//                putExtra(Intent.EXTRA_TEXT, "Check out Smart Transfer App!")
//            }
//            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        // Rate Us
        binding.llRateUs.setOnClickListener {
            val uri = Uri.parse("market://details?id=com.smartdatatransfer.easytransfer.filetransfer.sendanydata.smartswitchmobile.copydata")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        // Version (show version dynamically)
        binding.llVersion.setOnClickListener {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            binding.llVersion.setOnClickListener {
                binding.llVersion.isEnabled = false // Prevent multiple taps
                binding.llVersion.postDelayed({ binding.llVersion.isEnabled = true }, 1000)
                showToast("App Version: $versionName")
            }
        }
        binding.appVersionText.text = "Version: ${getAppVersion()}"
    }
    private fun getAppVersion(): String {
        return try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName // Returns the version name from build.gradle
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}