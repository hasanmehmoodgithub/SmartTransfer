package com.smart.transfer.app.languageScreens

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.smart.transfer.app.MainActivity
import com.smart.transfer.app.databinding.ActivityLanguageSelectionBinding
import com.smart.transfer.app.onboarding.OnboardingActivity
import java.util.Locale

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding
    private var selectedLanguageIndex: Int? = null
    private lateinit var adapter: LanguageAdapter

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
            Language("🇵🇹   Portuguese", "pt"),
            Language("🇵🇱   Polish", "pl"),
            Language("🇷🇺   Russian", "ru"),
            Language("🇪🇸   Spanish", "es"),
            Language("🇹🇷   Turkish", "tr"),
            Language("🇮🇷   Persian", "fa"),
            Language("🇹🇭   Thai", "th"),
            Language("🇻🇳   Vietnamese", "vi")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable ViewBinding
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make status bar transparent
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = LanguageAdapter(languages) { index ->
            selectedLanguageIndex = index
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LanguageSelectionActivity)
            adapter = this@LanguageSelectionActivity.adapter
        }
    }

    private fun setupClickListeners() {

        binding.btnDone.setOnClickListener {
            selectedLanguageIndex?.let {
                val selectedLang = languages[it]
                Toast.makeText(this, "Selected: ${selectedLang.name}", Toast.LENGTH_SHORT).show()

                // Save language BEFORE applying locale
                saveLanguage(selectedLang.locale)

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
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }


    private fun saveLanguage(languageCode: String) {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        sharedPreferences.edit().putString("selected_language", languageCode).apply()
    }

    private fun loadSavedLanguage() {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = sharedPreferences.getString("selected_language", "en") ?: "en"
        setLocale(language)
    }
}
