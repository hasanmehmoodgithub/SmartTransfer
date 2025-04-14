package com.smart.transfer.app;

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration

import androidx.appcompat.app.AppCompatDelegate
import com.smart.transfer.app.di.appModules
import com.smart.transfer.app.di.databaseModule
import com.smart.transfer.app.di.repositoryModule
import com.smart.transfer.app.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import java.util.Locale


class MyApplication : Application() {


    val context: MyApplication
        get() = instance?.context!!

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidContext(this@MyApplication)
            modules(
                appModules
            )
        }


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)




    }




    companion object {
        @JvmStatic
        var instance: MyApplication? = null
    }
}