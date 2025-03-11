package com.smart.transfer.app.com.smart.transfer.app
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.smart.transfer.app.R

open class BaseActivity : AppCompatActivity() {

    fun setupAppBar(toolbar: LinearLayout, title: String, showBackButton: Boolean) {

        val titleTextView = toolbar.findViewById<TextView>(R.id.toolbar_title)
        val backButton = toolbar.findViewById<ImageView>(R.id.back_button)

        titleTextView.text = title

        if (showBackButton) {
            backButton.visibility = View.VISIBLE
            backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        } else {
            backButton.visibility = View.GONE
        }
    }

}
