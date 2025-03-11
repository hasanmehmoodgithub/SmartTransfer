package com.smart.transfer.app.features.remoltyshare

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.smart.transfer.app.databinding.ActivityUploadingFilesBinding

class UploadingFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadingFilesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadingFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start upload animation
        startUploadingAnimation()
    }

    private fun startUploadingAnimation() {
        // Show uploading layout
        binding.uploadingLayout.visibility = View.VISIBLE
        binding.congratslayout.visibility = View.GONE

        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 3000 // 3 seconds
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            binding.progressBar.progress = progress
            binding.tvPercentage.text = "$progress%"

            // Show Congrats layout when progress reaches 100
            if (progress == 100) {
                showCongratsScreen()
            }
        }
        animator.start()
    }

    private fun showCongratsScreen() {
        binding.uploadingLayout.visibility = View.GONE
        binding.congratslayout.visibility = View.VISIBLE
    }
}
