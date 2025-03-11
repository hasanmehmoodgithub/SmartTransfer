package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.smart.transfer.app.features.filepicker.ImagesFragment


class FilePickerViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 4  // 4 fragments

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ImagesFragment()
            1 -> ImagesFragment()
            2 -> ImagesFragment()
            3 -> ImagesFragment()
            else -> ImagesFragment()
        }
    }
}