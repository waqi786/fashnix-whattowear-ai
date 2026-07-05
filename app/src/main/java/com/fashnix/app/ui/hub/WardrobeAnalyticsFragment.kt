package com.fashnix.app.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentWardrobeAnalyticsBinding
import com.fashnix.app.ui.home.HomeViewModel
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

/**
 * WardrobeAnalyticsFragment: Deep-dive into the user's fashion economics and utilization.
 * Features:
 * - Net Worth Trend Analysis.
 * - Utilization Metrics.
 * - Cost-Per-Wear Efficiency.
 */
@AndroidEntryPoint
class WardrobeAnalyticsFragment : Fragment() {

    private var _binding: FragmentWardrobeAnalyticsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWardrobeAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLuxuryAnalytics()
        observeNeuralData()
    }

    private fun setupLuxuryAnalytics() {
        binding.efficiencyCard.addExpertHoverEffect()
        binding.utilizationCard.addExpertHoverEffect()
        binding.valuationCard.addExpertHoverEffect()
        
        binding.analyticsToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeNeuralData() {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.vaultValue.collectLatest { value ->
                binding.totalValuationText.text = currencyFormat.format(value)
                binding.valuationSubtext.text = "TOTAL VAULT VALUATION"
            }
        }

        viewModel.sustainabilityMetrics.observe(viewLifecycleOwner) { metrics ->
            val cpw = metrics["cpw"] ?: 0.0
            val utilization = metrics["avg_utilization"]?.toInt() ?: 0
            
            binding.cpwValue.text = currencyFormat.format(cpw)
            binding.utilizationProgress.progress = utilization
            binding.utilizationPercentage.text = "$utilization%"
            
            if (utilization > 70) {
                binding.utilizationPercentage.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            }
        }

        viewModel.wardrobeItems.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                val mostWorn = items.maxByOrNull { it.wearCount }
                binding.topItemName.text = mostWorn?.name ?: "N/A"
                binding.topItemWears.text = "${mostWorn?.wearCount ?: 0} WEARS"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
