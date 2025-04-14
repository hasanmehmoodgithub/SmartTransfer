package com.smart.transfer.app.com.smart.transfer.app.features.history.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.tabs.TabLayout
import com.smart.transfer.app.R

import com.smart.transfer.app.features.history.view.RemoteFileShareHistoryFragment


class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val fragmentContainer = view.findViewById<FrameLayout>(R.id.fragmentContainer)

        // Add tabs
        tabLayout.addTab(tabLayout.newTab().setText("File Share"))
        tabLayout.addTab(tabLayout.newTab().setText("Remotely Share"))
        view.post {
            (tabLayout.getChildAt(0) as? ViewGroup)?.let { tabsContainer ->
                for (i in 0 until tabLayout.tabCount) {
                    tabsContainer.getChildAt(i)?.let { tabView ->
                        (tabView.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                            setMargins(8, 0, 8, 0) // 8dp margin on left/right
                            tabView.layoutParams = this
                        }
                    }
                }
            }
        }
        // Set initial fragment
        replaceFragment(LocalFileShareHistoryFragment())

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> replaceFragment(LocalFileShareHistoryFragment())
                    1 -> replaceFragment(RemoteFileShareHistoryFragment())

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
class FileShareFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_dashboard, container, false)
    }
}
