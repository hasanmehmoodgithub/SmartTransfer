package com.smart.transfer.app.onboarding

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.smart.transfer.app.MainActivity
import com.smart.transfer.app.com.smart.transfer.app.core.makeStatusBarTransparent
import com.smart.transfer.app.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(ONBOARDING_PREFS, MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeStatusBarTransparent()

        if (isOnboardingCompleted()) {
//            navigateToMainScreen()
//            return
        }

        setupViewPager()
        setupClickListeners()
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = OnboardingAdapter(this)
        TabLayoutMediator(binding.intoTabLayout, binding.viewPager) { _, _ -> }.attach()
    }

    private fun setupClickListeners() {
        binding.nextButton.setOnClickListener {
            if (binding.viewPager.currentItem < LAST_ONBOARDING_SCREEN) {
                binding.viewPager.currentItem += 1
            } else {
                completeOnboarding()
            }
        }
        binding.skipButton.setOnClickListener { completeOnboarding() }
    }

    private fun completeOnboarding() {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
        navigateToMainScreen()
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    companion object {
        private const val ONBOARDING_PREFS = "OnboardingPrefs"
        private const val KEY_ONBOARDING_COMPLETED = "OnboardingCompleted"
        private const val LAST_ONBOARDING_SCREEN = 2
    }
}
