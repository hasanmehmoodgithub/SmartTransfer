package com.smart.transfer.app.com.smart.transfer.app.features.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentsList = listOf(
        OnboardingStep1Fragment(),
        OnboardingStep2Fragment(),
        OnboardingStep3Fragment()
    )

    override fun createFragment(position: Int): Fragment = fragmentsList[position]

    override fun getItemCount(): Int = fragmentsList.size
}
