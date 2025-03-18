package com.smart.transfer.app.com.smart.transfer.app.features.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.smart.transfer.app.com.smart.transfer.app.core.makeStatusBarTransparent
import com.smart.transfer.app.com.smart.transfer.app.core.sharedpreference.SharedPrefManager
import com.smart.transfer.app.databinding.ActivityOnboardingBinding
import com.smart.transfer.app.features.dashboard.ui.DashboardActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val sharedPrefManager by lazy { SharedPrefManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeStatusBarTransparent()
        setupViewPager()
        setupClickListeners()
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = OnboardingAdapter(this)
        TabLayoutMediator(binding.intoTabLayout, binding.viewPager) { _, _ -> }.attach()
    }

    private fun setupClickListeners() {
        binding.nextButton.setOnClickListener {
            when {
                binding.viewPager.currentItem < LAST_ONBOARDING_SCREEN ->
                    binding.viewPager.currentItem += 1
                else -> completeOnboarding()
            }
        }

        binding.skipButton.setOnClickListener { completeOnboarding() }
    }

    private fun completeOnboarding() {
        sharedPrefManager.setOnboardingCompleted(true)
        navigateToMainScreen()
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    companion object {
        private const val LAST_ONBOARDING_SCREEN = 2
    }
}
