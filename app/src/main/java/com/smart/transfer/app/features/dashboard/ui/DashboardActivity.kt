package com.smart.transfer.app.features.dashboard.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.features.history.view.HistoryFragment


import com.smart.transfer.app.databinding.ActivityDashboardBinding
import np.com.susanthapa.curved_bottom_navigation.CbnMenuItem

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load Default Fragment
        loadFragment(HomeFragment())


//        binding.bottomNavigation.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.navigation_home -> loadFragment(HomeFragment())
//                R.id.navigation_storage -> loadFragment(StorageFragment())
//                R.id.navigation_history -> loadFragment(HistoryFragment())
//            }
//            true
//        }
        val menuItems = arrayOf(

            CbnMenuItem(
                R.drawable.ic_nav_home,
                R.drawable.avd_home,
                R.id.navigation_home
            ),

            CbnMenuItem(
                R.drawable.ic_nav_history,
                R.drawable.anim_history_icon,
                R.id.navigation_home
            ),
            CbnMenuItem(
                R.drawable.ic_nav_storage,
                R.drawable.anim_storage_icon,
                R.id.navigation_home
            )

        )

        binding.navview.setMenuItems(menuItems, 0)
        binding.navview.setOnMenuItemClickListener { cbnMenuItem, index ->
            when (index) {
               0-> loadFragment(HomeFragment())
                1-> loadFragment(StorageFragment())
               2 -> loadFragment(HistoryFragment())
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.dashboardFragment.id, fragment)
            .commit()
    }
}
