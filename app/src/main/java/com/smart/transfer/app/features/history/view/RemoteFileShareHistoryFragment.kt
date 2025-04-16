package com.smart.transfer.app.features.history.view



import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel

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
import com.smart.transfer.app.com.smart.transfer.app.features.history.view.LocalFileSharePagerAdapter
import com.smart.transfer.app.com.smart.transfer.app.features.history.viewmodel.HistoryViewModel
import com.smart.transfer.app.databinding.FragmentLocalFileShareHistoryBinding
import com.smart.transfer.app.databinding.FragmentRemoteFileShareHistoryBinding
import com.smart.transfer.app.features.dashboard.ui.BlankFragment
import com.smart.transfer.app.features.dashboard.ui.StorageFragment
import kotlinx.coroutines.launch
class RemoteFileShareHistoryFragment : Fragment() {
    private var _binding: FragmentRemoteFileShareHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HistoryAdapter

    // Using Koin to inject the HistoryViewModel
    private val viewModel: HistoryViewModel by viewModel()

    // Paging variables in the fragment (if needed for UI logic)
    private var currentTag = "remotely"
    private var currentFrom = "send"

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

        // Observe paginated history LiveData
        viewModel.paginatedHistory.observe(viewLifecycleOwner) { historyList ->
            adapter.submitList(historyList.toList())
        }

        // Initial load (reset paging)
        viewModel.loadPaginatedHistory(currentTag, currentFrom, reset = true)
    }

    private fun setupViewPager() {
        val adapterPager = LocalFileSharePagerAdapter(this)
        binding.viewPager.adapter = adapterPager

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Sent" else "Received"
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentFrom = if (position == 0) "send" else "receive"
                adapter.submitList(emptyList())

                viewModel.loadPaginatedHistory(currentTag, currentFrom, reset = true)
            }
        })
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter(requireContext(), )
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                // Trigger loading more items when the user scrolls near the end of the list
                if (lastVisibleItem >= adapter.itemCount - 5) {
                    viewModel.loadPaginatedHistory(currentTag, currentFrom)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
