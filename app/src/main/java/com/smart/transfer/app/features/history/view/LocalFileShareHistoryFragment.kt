package com.smart.transfer.app.features.history.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.database.AppDatabase
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History
import com.smart.transfer.app.com.smart.transfer.app.features.history.view.HistoryAdapter
import com.smart.transfer.app.databinding.FragmentLocalFileShareHistoryBinding
import com.smart.transfer.app.features.dashboard.ui.BlankFragment
import com.smart.transfer.app.features.dashboard.ui.StorageFragment
import kotlinx.coroutines.launch

class LocalFileShareHistoryFragment : Fragment() {
    private var _binding: FragmentLocalFileShareHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocalFileShareHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupRecyclerView()
        database = AppDatabase.getDatabase(requireContext())

        getHistoryData("local", "send")
    }

    private fun setupViewPager() {
        val adapter = LocalFileSharePagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Sent" else "Received"
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val from = if (position == 0) "send" else "receive"
                getHistoryData("local", from)
            }
        })
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }



    private fun getHistoryData(tag: String, from: String) {
        lifecycleScope.launch {
            val historyList = database.historyDao().getHistoryByTagAndFrom(tag, from)
            adapter = HistoryAdapter(requireContext(), historyList)
            binding.recyclerView.adapter = adapter
            if (historyList.isNotEmpty()) {
                historyList.forEach { item ->
                    Log.e("historyList", "Retrieved: ${item.filePath} - ${item.tag} - ${item.from}")
                }
            } else {
                Log.e("historyList", "No Data Found for Tag: $tag and From: $from")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class LocalFileSharePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment =  BlankFragment()
}