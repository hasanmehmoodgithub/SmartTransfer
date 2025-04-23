package com.smart.transfer.app.com.smart.transfer.app.core

import android.content.Context
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.smart.transfer.app.R

object AdConfigManager {
    var showBanner = false
    var showInterstitial = false
    var showNative = false
    var testing="test"
    fun initConfig(context: Context, onComplete: () -> Unit) {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1000
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
      //  remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            showBanner = remoteConfig.getBoolean("show_banner_ad")
            showInterstitial = remoteConfig.getBoolean("show_interstitial_ad")
            showNative = remoteConfig.getBoolean("show_native_ad")
            testing = remoteConfig.getString("testing")
            Log.w("value of remote config","$showBanner $showInterstitial $showNative $testing")
            onComplete()
        }
    }
}
