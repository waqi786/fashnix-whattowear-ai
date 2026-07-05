package com.fashnix.app.ui.wardrobe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentWardrobeBinding
import com.fashnix.app.util.addExpertHoverEffect
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

/**
 * WardrobeFragment: The Digital Vault for high-end asset management.
 * Fully functional with real-time stats, filtering, and elite visual feedback.
 */
@AndroidEntryPoint
class WardrobeFragment : Fragment() {

    private var _binding: FragmentWardrobeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WardrobeViewModel by viewModels()
    private lateinit var adapter: ClothingItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWardrobeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupChips()
        observeViewModel()
        
        viewModel.loadItems()
    }

    private fun setupUI() {
        adapter = ClothingItemAdapter(
            onItemClick = { item ->
                val bundle = Bundle().apply { putString("clothingItemId", item.id) }
                findNavController().navigate(R.id.clothingItemDetailFragment, bundle)
            }
        )

        binding.wardrobeRecycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@WardrobeFragment.adapter
            setHasFixedSize(true)
            setItemViewCacheSize(20)
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.scanFab.setOnClickListener {
            findNavController().navigate(R.id.cameraFragment)
        }

        binding.vaultStatsCard.addExpertHoverEffect()
        binding.wardrobeToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupChips() {
        val categories = listOf("ALL", "Apparel", "Footwear", "Accessories", "Personal Care")
        binding.categoryChips.removeAllViews()
        
        categories.forEach { category ->
            val chip = layoutInflater.inflate(R.layout.layout_filter_chip, binding.categoryChips, false) as Chip
            chip.text = category.uppercase()
            chip.id = View.generateViewId()
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.setCategoryFilter(category)
            }
            binding.categoryChips.addView(chip)
            if (category == "ALL") chip.isChecked = true
        }
    }

    private fun observeViewModel() {
        viewModel.filteredItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.emptyState.isVisible = items.isEmpty()
            binding.totalPiecesText.text = items.size.toString()
            
            val readyItems = items.count { it.laundryStatus.equals("Clean", ignoreCase = true) }
            binding.vaultValuationText.text = readyItems.toString()
            
            val avgWear = if (items.isNotEmpty()) items.map { it.wearCount }.average() else 0.0
            binding.styleScoreText.text = String.format(Locale.US, "%.1f", (avgWear * 0.8 + 5.0).coerceAtMost(10.0))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
