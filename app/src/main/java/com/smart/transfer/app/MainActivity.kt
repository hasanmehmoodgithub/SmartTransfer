package com.smart.transfer.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate

import com.smart.transfer.app.com.smart.transfer.app.core.makeStatusBarTransparent
import com.smart.transfer.app.databinding.ActivityMainBinding
import com.smart.transfer.app.features.filepicker.FilePickerActivity
import com.smart.transfer.app.features.mobileToPc.MobileToPcActivity
import com.smart.transfer.app.features.remoltyshare.RemotelyShareActivity
import com.smart.transfer.app.features.setting.SettingActivity
import com.smart.transfer.app.features.setting.ShareAppActivity


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setupUI()
    }

    private fun setupUI() {
//        supportActionBar?.hide()
//        makeStatusBarTransparent()
//        binding.mobileToPcCard.setOnClickListener {
//            startActivity(Intent(this, MobileToPcActivity::class.java))
//
//        }



        }

    fun mobileToPcOnClick(view: View) {


       // startActivity(Intent(this, FilePickerActivity::class.java))
        startActivity(Intent(this, MobileToPcActivity::class.java))

    }

    fun onClickRemotelyShare(view: View) {
        startActivity(Intent(this, RemotelyShareActivity::class.java))

    }

    fun onClickSetting(view: View) {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    fun onClickShareApp(view: View) {
        startActivity(Intent(this, ShareAppActivity::class.java))
    }

}

