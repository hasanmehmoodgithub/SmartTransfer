package com.smart.transfer.app.com.smart.transfer.app.features.languageScreens

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.core.makeStatusBarTransparent
import com.smart.transfer.app.com.smart.transfer.app.core.sharedpreference.SharedPrefManager
import com.smart.transfer.app.com.smart.transfer.app.features.onboarding.OnboardingActivity
import com.smart.transfer.app.databinding.ActivityLanguageSelectionBinding
import com.smart.transfer.app.features.dashboard.ui.DashboardActivity
import java.util.Locale

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding
    private var selectedLanguageIndex: Int? = null
    private lateinit var adapter: LanguageAdapter
    private val sharedPrefManager by lazy { SharedPrefManager.getInstance(this) }

    companion object {
        private val languages = listOf(
            Language("🇬🇧   English", "en"),
            Language("🇸🇦   Arabic", "ar"),
            Language("🇨🇳   Chinese", "zh"),
            Language("🇫🇷   French", "fr"),
            Language("🇩🇪   German", "de"),
            Language("🇮🇳   Hindi", "hi"),
            Language("🇮🇩   Indonesian", "id"),
            Language("🇮🇹   Italian", "it"),
            Language("🇧🇷   Portuguese", "pt"), // Changed to Brazil 🇧🇷 as it's more common for Portuguese
            Language("🇵🇱   Polish", "pl"),
            Language("🇷🇺   Russian", "ru"),
            Language("🇪🇸   Spanish", "es"),
            Language("🇹🇷   Turkish", "tr"),
            Language("🇮🇷   Persian", "fa"),
            Language("🇹🇭   Thai", "th"),
            Language("🇻🇳   Vietnamese", "vi")
        )}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable ViewBinding
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.hide()
        makeStatusBarTransparent()
        setupRecyclerView()
        setupClickListeners()
       loadSavedLanguage()
    }

    private fun setupRecyclerView() {
        adapter = LanguageAdapter(languages) { index ->
            selectedLanguageIndex = index
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LanguageSelectionActivity)
            adapter = this@LanguageSelectionActivity.adapter
        }
        val isFromSplash = intent.getBooleanExtra("isFromSplash", true)


        if(isFromSplash){
                    adapter.setSelectedIndex(0)

        }

    }

    private fun setupClickListeners() {

        binding.btnDone.setOnClickListener {
            selectedLanguageIndex?.let {
                val selectedLang = languages[it]
             //   Toast.makeText(this, "Selected: ${selectedLang.name}", Toast.LENGTH_SHORT).show()

                // Save language BEFORE applying locale
                saveLanguage(selectedLang)

                // Apply the locale change
                setLocale(selectedLang.locale)
            } ?: Toast.makeText(this, "Please select a language", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setLocale(languageCode: String) {
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


        val isOnboardingDone = sharedPrefManager.isOnboardingCompleted()

        val nextScreen = if (isOnboardingDone) {
            DashboardActivity::class.java
        } else {
            OnboardingActivity::class.java
        }

        startActivity(Intent(this, nextScreen))
        finish()

    }


    private fun saveLanguage(language: Language) {
        sharedPrefManager.setSelectedLanguage( language.locale)
        sharedPrefManager.setSelectedLanguageName(language.name)
        sharedPrefManager.setLanguageCompleted(true)
    }


    private fun loadSavedLanguage() {
        val languageLocale = sharedPrefManager.getSelectedLanguage()
        val languageName =sharedPrefManager.getSelectedLanguageName()
        binding.selectedLanguageInclude.tvLanguage.text= languageName
        val colorRes = R.color.app_blue
        binding.selectedLanguageInclude.radioImg.setColorFilter(ContextCompat.getColor(binding.root.context, colorRes))


    }
}
