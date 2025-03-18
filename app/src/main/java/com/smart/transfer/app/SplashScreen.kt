package com.smart.transfer.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.smart.transfer.app.com.smart.transfer.app.core.makeStatusBarTransparent
import com.smart.transfer.app.com.smart.transfer.app.core.sharedpreference.SharedPrefManager
import com.smart.transfer.app.databinding.ActivitySplashScreenBinding
import com.smart.transfer.app.features.dashboard.ui.DashboardActivity
import com.smart.transfer.app.com.smart.transfer.app.features.languageScreens.LanguageSelectionActivity
import java.util.Locale

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val sharedPrefManager by lazy { SharedPrefManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        startSplashTimer()
    }

    private fun setupUI() {
        supportActionBar?.hide()
        makeStatusBarTransparent()

        binding.getStartedBtn.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        binding.getStartedBtn.setOnClickListener { handleButtonClick() }
    }

    private fun startSplashTimer() {
        object : CountDownTimer(2000, 1000) {  // Changed from 500ms to 2 seconds
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                binding.getStartedBtn.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }.start()
    }

    private fun handleButtonClick() {

        val isLanguageDone = sharedPrefManager.isLanguageCompleted()
        val languageLocale = sharedPrefManager.getSelectedLanguage()
        val nextScreen = if (isLanguageDone) {
            DashboardActivity::class.java
        } else {
            LanguageSelectionActivity::class.java
        }
        setLocale(languageLocale,nextScreen)

    }
    private fun setLocale(languageCode: String, nextScreen: Class<out AppCompatActivity>,) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        // Update application context
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        // Restart the activity for the language change to take effect
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(Intent(this, nextScreen))
        finish()



    }

}
