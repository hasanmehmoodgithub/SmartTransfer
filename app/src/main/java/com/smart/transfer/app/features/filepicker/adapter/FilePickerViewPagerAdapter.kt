package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.smart.transfer.app.features.filepicker.ui.fragments.DocumentPickerFragment
import com.smart.transfer.app.features.filepicker.ui.fragments.ImagePickerFragment
import com.smart.transfer.app.features.filepicker.ui.fragments.AudioPickerFragment
import com.smart.transfer.app.features.filepicker.ui.fragments.VideoPickerFragment


class FilePickerViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 4  // 4 fragments

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ImagePickerFragment()
            1 -> VideoPickerFragment()
            2 -> AudioPickerFragment()
            3 -> DocumentPickerFragment()
            else -> ImagePickerFragment()
        }
    }
}