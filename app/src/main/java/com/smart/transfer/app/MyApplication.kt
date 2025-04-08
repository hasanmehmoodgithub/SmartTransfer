package com.smart.transfer.app;

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale


class MyApplication : Application() {


    val context: MyApplication
        get() = instance?.context!!

    override fun onCreate() {
        super.onCreate()
        instance = this

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)




    }




    companion object {
        @JvmStatic
        var instance: MyApplication? = null
    }
}