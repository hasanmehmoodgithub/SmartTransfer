package com.smart.transfer.app.com.smart.transfer.app.core



import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.Window

fun Activity.makeStatusBarTransparent() {
    window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    window.statusBarColor = Color.TRANSPARENT
}
