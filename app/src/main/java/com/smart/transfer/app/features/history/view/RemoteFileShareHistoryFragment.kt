package com.smart.transfer.app.features.history.view



import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.database.AppDatabase
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History
import com.smart.transfer.app.com.smart.transfer.app.features.history.view.HistoryAdapter
import com.smart.transfer.app.databinding.FragmentLocalFileShareHistoryBinding
import com.smart.transfer.app.databinding.FragmentRemoteFileShareHistoryBinding
import com.smart.transfer.app.features.dashboard.ui.BlankFragment
import com.smart.transfer.app.features.dashboard.ui.StorageFragment
import kotlinx.coroutines.launch

class RemoteFileShareHistoryFragment : Fragment() {
    private var _binding: FragmentRemoteFileShareHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var adapter: HistoryAdapter

    private var currentTag = "remotely"
    private var currentFrom = "receive"
    private var isLoading = false
    private var currentPage = 0
    private val pageSize = 20
    private val allHistoryList = mutableListOf<History>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemoteFileShareHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupViewPager()
        database = AppDatabase.getDatabase(requireContext())

        getHistoryData(reset = true)
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
                currentFrom = if (position == 0) "send" else "receive"
                getHistoryData(reset = true)
            }
        })
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter(requireContext(), allHistoryList)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && lastVisibleItem >= allHistoryList.size - 5) {
                    getHistoryData()
                }
            }
        })
    }

    private fun getHistoryData(reset: Boolean = false) {
        if (isLoading) return

        isLoading = true
        if (reset) {
            currentPage = 0
            allHistoryList.clear()
            adapter.notifyDataSetChanged()
        }

        val offset = currentPage * pageSize

        lifecycleScope.launch {
            val newItems = database.historyDao().getPaginatedHistory(currentTag, currentFrom, pageSize, offset)
            if (newItems.isNotEmpty()) {
                allHistoryList.addAll(newItems)
                adapter.notifyDataSetChanged()
                currentPage++
            }
            isLoading = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

