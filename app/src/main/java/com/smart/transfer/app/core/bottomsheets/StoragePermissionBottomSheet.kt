package com.smart.transfer.app.com.smart.transfer.app.core.bottomsheets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Choreographer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.core.appenums.ChooseFileNextScreenType
import com.smart.transfer.app.com.smart.transfer.app.core.appenums.PermissionStatus
import com.smart.transfer.app.com.smart.transfer.app.core.sharedpreference.SharedPrefManager
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.ui.ChooseFileActivity
import com.smart.transfer.app.com.smart.transfer.app.features.mobileToPc.ui.MobileToPcActivity
import com.smart.transfer.app.databinding.BottomSheetLayoutBinding
import com.smart.transfer.app.features.androidtoios.ui.AndroidToIosActivity
import com.smart.transfer.app.features.localshare.ui.HandlePermissionActivity
import com.smart.transfer.app.features.remoltyshare.RemotelyShareActivity


class StoragePermissionBottomSheet(private val chooseFileNextScreenType: ChooseFileNextScreenType) : BottomSheetDialogFragment() {
    private val sharedPrefManager by lazy { SharedPrefManager.getInstance(requireContext()) }

    private var _binding: BottomSheetLayoutBinding? = null
    private val binding get() = _binding!!
    // Register a launcher for requesting permissions (recommended for modern Android development)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted
                proceedAfterPermissionGranted()
            } else {
                // Permission denied
                handlePermissionDenied()
            }
        }
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetLayoutBinding.inflate(inflater, container, false)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUIBasedOnPermission()

        binding.btnGrantAccess.setOnClickListener {
            checkStoragePermission()

        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }


//        binding.btnClose.setOnClickListener {
//            dismiss()
//        }
    }
    private fun updateUIBasedOnPermission() {
        when (getStoragePermissionStatus()) {
            PermissionStatus.GRANTED -> {
                binding.btnGrantAccess.text = "Access Granted"
                binding.helpImage.setImageResource(R.drawable.ic_storage_permission)  // Example success image
                binding.needHelpText.text = "You’re All Set!"
                binding.helpDescriptionText.text = "Storage permission is granted. You can now proceed."
                navigateToDesiredScreen(chooseFileNextScreenType)
            }
            PermissionStatus.RequireGRANTED -> {
                binding.btnGrantAccess.text = "Grant Access"
                binding.helpImage.setImageResource(R.drawable.ic_storage_permission)  // Example info image
                binding.needHelpText.text = "We need your help"
                binding.helpDescriptionText.text = "To provide you with the best experience, this app needs access to your storage to save and manage files."
            }
            PermissionStatus.DENIED -> {
                binding.btnGrantAccess.text = "Allow Access"
                binding.helpImage.setImageResource(R.drawable.ic_permission_sad)  // Example warning image
                binding.needHelpText.text = "We can’t Help without your permission"
                binding.helpDescriptionText.text = "Storage access is necessary to send and receive files. Without it, the app cannot function properly. Grant permission to continue using file transfer features.We do not access or share your personal data."

            }
            PermissionStatus.PERMANENTLY_DENIED -> {
                binding.btnGrantAccess.text = "Allow Access"
                binding.helpImage.setImageResource(R.drawable.ic_permission_sad)  // Example error image
                binding.needHelpText.text = "We can’t Help without your permission"
                binding.helpDescriptionText.text = "Storage access is necessary to send and receive files. Without it, the app cannot function properly. Grant permission to continue using file transfer features.We do not access or share your personal data."

            }
        }
    }
    private fun navigateToDesiredScreen(chooseFileNextScreenType: ChooseFileNextScreenType) {
        when (chooseFileNextScreenType) {
            ChooseFileNextScreenType.MobileToPc -> {

                // Handle MobileToPc case
                val intent = Intent(requireContext(), ChooseFileActivity::class.java)
                intent.putExtra("ChooseFileNextScreenType", chooseFileNextScreenType) // Convert to String
                sharedPrefManager.setSelectAllCheckBoxStatus(true)
                startActivity(intent)
                dismiss()

            }
            ChooseFileNextScreenType.AndroidToIos -> {
                // Handle AndroidToIos case
                val intent = Intent(requireContext(), ChooseFileActivity::class.java)
                intent.putExtra("ChooseFileNextScreenType", chooseFileNextScreenType) // Convert to String
                sharedPrefManager.setSelectAllCheckBoxStatus(true)
                startActivity(intent)
                dismiss()
            }
            ChooseFileNextScreenType.LocalShare -> {
                // Handle AndroidToIos case
                val intent = Intent(requireContext(), HandlePermissionActivity::class.java)
                intent.putExtra("from", 1) // Convert to String
                sharedPrefManager.setSelectAllCheckBoxStatus(true)
                startActivity(intent)
                dismiss()

            }
            ChooseFileNextScreenType.LocalReceive -> {
                // Handle AndroidToIos case
                val intent = Intent(requireContext(), HandlePermissionActivity::class.java)
                intent.putExtra("from", 2) // Convert to String
                sharedPrefManager.setSelectAllCheckBoxStatus(true)
                startActivity(intent)
                dismiss()

            }
            ChooseFileNextScreenType.Remote -> {
                // Handle AndroidToIos case
                val intent = Intent(requireContext(), RemotelyShareActivity::class.java)
                intent.putExtra("ChooseFileNextScreenType", chooseFileNextScreenType) // Convert to String
                sharedPrefManager.setSelectAllCheckBoxStatus(false)
                startActivity(intent)
                dismiss()
            }
            else -> {
                // Handle other cases (if any)
            }
        }


    }

    private fun checkStoragePermission() {
        when {
            // Check if permission is already granted
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                proceedAfterPermissionGranted()
            }
            // For Android 10 and above, handle Scoped Storage
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                handleScopedStorage()
            }
            // For Android 6.0 (API 23) and above, request runtime permission
            else -> {
                requestStoragePermission()
            }
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Explain why the permission is needed
            showPermissionRationale()
        } else {
            // Request the permission
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            dismiss()

        }
    }

    private fun handleScopedStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) and above requires MANAGE_EXTERNAL_STORAGE for broad access
            if (Environment.isExternalStorageManager()) {
                dismiss()
                proceedAfterPermissionGranted()
            } else {
                // Request MANAGE_EXTERNAL_STORAGE permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:com.smartdatatransfer.easytransfer.filetransfer.sendanydata.smartswitchmobile.copydata")
                startActivity(intent)
                dismiss()
            }
        } else {
            // Android 10 (API 29) uses Scoped Storage, but WRITE_EXTERNAL_STORAGE is still required
            requestStoragePermission()
        }
    }

    private fun proceedAfterPermissionGranted() {
        // Perform your storage-related operations here
        Log.d("StoragePermission", "Permission granted, proceed with storage operations")
    }

    private fun handlePermissionDenied() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Explain why the permission is needed
            showPermissionRationale()
        } else {
            // Permission denied permanently, guide the user to app settings
            showPermissionSettingsDialog()
        }
    }

    private fun showPermissionRationale() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        dismiss()
    }

    private fun showPermissionSettingsDialog() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", "com.smartdatatransfer.easytransfer.filetransfer.sendanydata.smartswitchmobile.copydata", null)
        intent.data = uri
        startActivity(intent)
        dismiss()
    }
    private fun getStoragePermissionStatus(): PermissionStatus {
        return when {
            // ✅ Android 11+ (Scoped Storage)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (Environment.isExternalStorageManager()) {
                    PermissionStatus.GRANTED
                } else {
                    PermissionStatus.RequireGRANTED
                }
            }

            // ✅ Android 6-10: Check WRITE_EXTERNAL_STORAGE permission
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                PermissionStatus.GRANTED
            }

            // ✅ Check if the user has denied permission before
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                PermissionStatus.DENIED
            }

            // ✅ If the user **has never interacted** with the permission dialog before
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val permissionState = requireActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (permissionState == PackageManager.PERMISSION_DENIED) {
                        PermissionStatus.PERMANENTLY_DENIED
                    } else {
                        PermissionStatus.RequireGRANTED  // User has never seen the dialog yet
                    }
                } else {
                    PermissionStatus.PERMANENTLY_DENIED
                }
            }
        }
    }


}
