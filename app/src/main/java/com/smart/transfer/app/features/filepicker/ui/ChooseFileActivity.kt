package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import com.smart.transfer.app.com.smart.transfer.app.core.appenums.ChooseFileNextScreenType
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter.FilePickerCategoryAdapter
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter.FilePickerViewPagerAdapter
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.FilePickerCategory
import com.smart.transfer.app.databinding.ActivityChooseFileBinding
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import com.smart.transfer.app.features.dashboard.ui.TransferItemActivity
import com.smart.transfer.app.features.filepicker.ui.fragments.SelectedAudiosManager
import com.smart.transfer.app.features.filepicker.ui.fragments.SelectedDocumentsManager
import com.smart.transfer.app.features.filepicker.ui.fragments.SelectedImagesManager
import com.smart.transfer.app.features.filepicker.ui.fragments.SelectedVideosManager

class ChooseFileActivity : BaseActivity() {
    private lateinit var categoryAdapter: FilePickerCategoryAdapter
    private lateinit var binding: ActivityChooseFileBinding // View Binding

    private val categories = listOf(
        FilePickerCategory("Images", R.drawable.ic_choose_image),
        FilePickerCategory("Videos", R.drawable.ic_choose_video),
        FilePickerCategory("Audio", R.drawable.ic_choose_audio),
        FilePickerCategory("Documents", R.drawable.ic_choose_doc)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseFileBinding.inflate(layoutInflater) // Inflate binding
        setContentView(binding.root)
        val chooseFileNextScreenType = intent.getSerializableExtra("ChooseFileNextScreenType") as? ChooseFileNextScreenType
clearPrevData();

        setupAppBar(binding.customToolbar.customToolbar, "Choose File", showBackButton = true)

        setupCategoryRecyclerView()
        setupViewPager()
        binding.btnNext.setOnClickListener(View.OnClickListener { it
            val intent = Intent(this, TransferItemActivity::class.java)
            intent.putExtra("ChooseFileNextScreenType", chooseFileNextScreenType) // Make sure ChooseFileNextScreenType implements Serializable or Parcelable
            startActivity(intent)


        })
    }

    private fun clearPrevData() {
        AllSelectedFilesManager.allSelectedFiles.clear()
        SelectedImagesManager.selectedImages.clear()
        SelectedVideosManager.selectedVideos.clear();
        SelectedDocumentsManager.selectedDocuments.clear()
        SelectedAudiosManager.selectedAudios.clear()
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = FilePickerCategoryAdapter(this, categories) { index ->
            binding.viewPager.currentItem = index
            categoryAdapter.updateSelection(index)
        }
        binding.categoryRecyclerView.adapter = categoryAdapter
    }

    private fun setupViewPager() {
        binding.viewPager.apply {
            adapter = FilePickerViewPagerAdapter(this@ChooseFileActivity)
            isUserInputEnabled = false
        }
    }
}

