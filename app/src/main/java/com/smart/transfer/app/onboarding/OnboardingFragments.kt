package com.smart.transfer.app.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.smart.transfer.app.databinding.OnboardingStep1FragmentBinding
import com.smart.transfer.app.databinding.OnboardingStep2FragmentBinding
import com.smart.transfer.app.databinding.OnboardingStep3FragmentBinding

class OnboardingStep1Fragment : Fragment() {

    private var _binding: OnboardingStep1FragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OnboardingStep1FragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevents memory leaks
    }
}
class OnboardingStep2Fragment : Fragment() {

    private var _binding: OnboardingStep2FragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OnboardingStep2FragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
class OnboardingStep3Fragment : Fragment() {

    private var _binding: OnboardingStep3FragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OnboardingStep3FragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}