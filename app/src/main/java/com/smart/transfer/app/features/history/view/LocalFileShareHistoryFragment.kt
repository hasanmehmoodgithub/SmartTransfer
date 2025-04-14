package com.smart.transfer.app.com.smart.transfer.app.features.history.view

import android.os.Bundle
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
import com.smart.transfer.app.com.smart.transfer.app.features.history.viewmodel.HistoryViewModel
import com.smart.transfer.app.databinding.FragmentLocalFileShareHistoryBinding
import com.smart.transfer.app.features.dashboard.ui.BlankFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocalFileShareHistoryFragment : Fragment() {

    private var _binding: FragmentLocalFileShareHistoryBinding? = null
    private val binding get() = _binding!!

    // Inject HistoryViewModel via Koin.
    private val viewModel: HistoryViewModel by viewModel()

    // ListAdapter based adapter for paginated history.
    private lateinit var adapter: HistoryAdapter

    // Local file share settings
    private var currentTag = "local"
    private var currentFrom = "send"  // Default type for local file share

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocalFileShareHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupViewPager()

        // Observe the paginated history LiveData from the ViewModel.
        viewModel.paginatedHistory.observe(viewLifecycleOwner) { historyList ->
            // Use submitList to update HistoryAdapter's data.
            adapter.submitList(historyList.toList())
        }

        // Perform the initial load with local tag and default "send" type.
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
                // When the tab changes, update the type (from).
                currentFrom = if (position == 0) "send" else "receive"
                // Reset paging and reload history data.
                viewModel.loadPaginatedHistory(currentTag, currentFrom, reset = true)
            }
        })
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Initialize the adapter (ListAdapter-based HistoryAdapter).
        adapter = HistoryAdapter(requireContext())
        binding.recyclerView.adapter = adapter

        // Add a scroll listener to trigger pagination when nearing the end of the list.
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                // Trigger loading more items if near the list's end.
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

class LocalFileSharePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment = BlankFragment()
}
