package com.smart.transfer.app.com.smart.transfer.app.core.sharedpreference


import android.content.Context
import android.content.SharedPreferences


class SharedPrefManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "testing1"
        //keys
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val KEY_SELECTED_LANGUAGE_NAME = "selected_language_name"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_Language_COMPLETED = "language_completed"
        private const val KEY_SelectAllValue = "select_all_value"
        //default values
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_LANGUAGE_NAME = "🇬🇧 English"

        @Volatile
        private var instance: SharedPrefManager? = null

        fun getInstance(context: Context): SharedPrefManager {
            return instance ?: synchronized(this) {
                instance ?: SharedPrefManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getSelectedLanguage(): String =
        sharedPreferences.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

    fun setSelectedLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_SELECTED_LANGUAGE, language).apply()
    }

    fun getSelectedLanguageName(): String =
        sharedPreferences.getString(KEY_SELECTED_LANGUAGE_NAME, DEFAULT_LANGUAGE_NAME) ?: DEFAULT_LANGUAGE_NAME

    fun setSelectedLanguageName(languageName: String) {
        sharedPreferences.edit().putString(KEY_SELECTED_LANGUAGE_NAME, languageName).apply()
    }
    fun isLanguageCompleted(): Boolean =
        sharedPreferences.getBoolean(KEY_Language_COMPLETED, false)

    fun setLanguageCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_Language_COMPLETED, completed).apply()
    }
    fun isOnboardingCompleted(): Boolean =
        sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isSelectAllCheckBoxStatus(): Boolean =
        sharedPreferences.getBoolean(KEY_SelectAllValue, false)

    fun setSelectAllCheckBoxStatus(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SelectAllValue, completed).apply()
    }
}