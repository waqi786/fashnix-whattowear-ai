package com.fashnix.app.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fashnix.app.R

class OnboardingAdapter(activity: OnboardingActivity) : FragmentStateAdapter(activity) {

    private val pages = listOf(
        OnboardingPageFragment.newInstance(
            activity.getString(R.string.onboarding_title_1),
            activity.getString(R.string.onboarding_subtitle_1),
            R.raw.fashion_loading
        ),
        OnboardingPageFragment.newInstance(
            activity.getString(R.string.onboarding_title_2),
            activity.getString(R.string.onboarding_subtitle_2),
            R.raw.fashion_loading
        ),
        OnboardingPageFragment.newInstance(
            activity.getString(R.string.onboarding_title_3),
            activity.getString(R.string.onboarding_subtitle_3),
            R.raw.fashion_loading
        )
    )

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment = pages[position]
}