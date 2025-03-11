package com.smart.transfer.app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.smart.transfer.app.onboarding.OnboardingActivity
import com.smart.transfer.app.com.smart.transfer.app.core.makeStatusBarTransparent
import com.smart.transfer.app.databinding.ActivitySplashScreenBinding
import com.smart.transfer.app.languageScreens.LanguageSelectionActivity

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

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

        //loading ads
        binding.getStartedBtn.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

binding.getStartedBtn.setOnClickListener {
    startActivity(Intent(this, MainActivity::class.java))
    finish()
}

    }

    private fun startSplashTimer() {
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                binding.getStartedBtn.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }.start()
    }


}
