package com.smart.transfer.app.features.dashboard.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.smart.transfer.app.com.smart.transfer.app.core.appenums.ChooseFileNextScreenType
import com.smart.transfer.app.com.smart.transfer.app.core.bottomsheets.StoragePermissionBottomSheet
import com.smart.transfer.app.databinding.FragmentHomeBinding
import com.smart.transfer.app.com.smart.transfer.app.features.mobileToPc.ui.MobileToPcActivity
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.ui.ChooseFileActivity
import com.smart.transfer.app.features.localshare.ui.HandlePermissionActivity
import com.smart.transfer.app.features.setting.SettingActivity
import com.smart.transfer.app.features.setting.ShareAppActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupClickListeners()
        return binding.root
    }

    private fun setupClickListeners() {


        binding.settingImgBtn.setOnClickListener {
            startActivity(Intent(requireContext(), SettingActivity::class.java))

        }

        binding.shareImgBtn.setOnClickListener {
            startActivity(Intent(requireContext(), ShareAppActivity::class.java))
        }

        binding.shareFileLocaly.setOnClickListener {
            // Handle share file locally button click
            StoragePermissionBottomSheet(ChooseFileNextScreenType.LocalShare).show(childFragmentManager, "shareFileLocal")


        }

        binding.recieveFileLocaly.setOnClickListener {
            StoragePermissionBottomSheet(ChooseFileNextScreenType.LocalReceive).show(childFragmentManager, "receiveFileLocal")
        }


        binding.remotelyShare.setOnClickListener {
            StoragePermissionBottomSheet(ChooseFileNextScreenType.Remote).show(childFragmentManager, "remotelyShare")


        }

        binding.androidToIos.setOnClickListener {

            StoragePermissionBottomSheet(ChooseFileNextScreenType.AndroidToIos).show(childFragmentManager, "androidToIos")

        }

        binding.mobileToPcCard.setOnClickListener {
            StoragePermissionBottomSheet(ChooseFileNextScreenType.MobileToPc).show(childFragmentManager, "mobileToPcCard")



        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
