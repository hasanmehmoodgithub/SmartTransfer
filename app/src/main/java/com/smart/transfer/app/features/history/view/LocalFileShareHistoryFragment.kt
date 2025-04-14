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
import com.smart.transfer.app.features.dashboard.ui.BlankFragment
import com.smart.transfer.app.features.dashboard.ui.StorageFragment
import kotlinx.coroutines.launch

class LocalFileShareHistoryFragment : Fragment() {
    private var _binding: FragmentLocalFileShareHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private lateinit var adapter: HistoryAdapter

    private var currentPage = 0
    private val pageSize = 20
    private var isLoading = false
    private var currentTag = "local"
    private var currentFrom = "send"
    private val fullHistoryList = mutableListOf<History>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLocalFileShareHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        setupRecyclerView()
        setupViewPager()

        loadNextPage()
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
                resetAndLoad()
            }
        })
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter(requireContext(), fullHistoryList)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItem >= fullHistoryList.size - 10) {
                    loadNextPage()
                }
            }
        })
    }

    private fun resetAndLoad() {
        currentPage = 0
        fullHistoryList.clear()
        adapter.notifyDataSetChanged()
        loadNextPage()
    }

    private fun loadNextPage() {
        isLoading = true
        lifecycleScope.launch {
            val offset = currentPage * pageSize
            val newItems = database.historyDao().getPaginatedHistory(currentTag, currentFrom, pageSize, offset)
            if (newItems.isNotEmpty()) {
                fullHistoryList.addAll(newItems)
                adapter.notifyItemRangeInserted(fullHistoryList.size - newItems.size, newItems.size)
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


class LocalFileSharePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment =  BlankFragment()
}