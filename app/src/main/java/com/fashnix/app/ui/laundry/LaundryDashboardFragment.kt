package com.fashnix.app.ui.laundry

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fashnix.app.R
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.databinding.FragmentLaundryDashboardBinding
import com.fashnix.app.ui.wardrobe.WardrobeViewModel
import com.fashnix.app.util.addExpertHoverEffect
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * LaundryDashboardFragment: The Luxe Care Intelligence Hub.
 * Re-engineered for high-end asset preservation and professional management.
 */
@AndroidEntryPoint
class LaundryDashboardFragment : Fragment() {

    private var _binding: FragmentLaundryDashboardBinding? = null
    private val binding get() = _binding!!
    private val wardrobeViewModel: WardrobeViewModel by viewModels()

    private lateinit var adapter: LaundryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaundryDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupExpertUI()
        loadLaundryItems()
        
        binding.laundryToolbar.setNavigationOnClickListener { 
            performHaptic(VibrationEffect.EFFECT_CLICK)
            findNavController().navigateUp() 
        }
    }

    private fun setupExpertUI() {
        binding.dirtyBasketCard.addExpertHoverEffect()
        binding.cleanClothesCard.addExpertHoverEffect()
        binding.markAllCleanButton.addExpertHoverEffect()

        adapter = LaundryAdapter { item -> markAsClean(item) }
        binding.laundryRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LaundryDashboardFragment.adapter
            isNestedScrollingEnabled = false
            setHasFixedSize(false)
        }
        
        binding.markAllCleanButton.setOnClickListener {
            initiateMassPurification()
        }
    }

    private fun loadLaundryItems() {
        wardrobeViewModel.items.observe(viewLifecycleOwner) { items ->
            val dirtyItems = items.filter {
                it.laundryStatus == "NeedsWash" || it.laundryStatus == "Dirty" || it.wearCount >= it.laundryIntervalWears
            }
            adapter.submitList(dirtyItems)
            
            val totalCount = items.size
            val cleanCount = items.count { it.laundryStatus == "Clean" }
            val laundryProgress = if (totalCount > 0) (cleanCount * 100) / totalCount else 100
            
            binding.laundryProgress.setProgress(laundryProgress, true)
            
            binding.dirtyCountText.text = dirtyItems.size.toString()
            binding.cleanCountText.text = cleanCount.toString()
            
            val hasItems = dirtyItems.isNotEmpty()
            binding.emptyState.visibility = if (hasItems) View.GONE else View.VISIBLE
            binding.laundryRecycler.visibility = if (hasItems) View.VISIBLE else View.GONE
            binding.markAllCleanButton.visibility = if (hasItems) View.VISIBLE else View.GONE
            
            if (hasItems) runEntranceAnimation()
        }
        
        wardrobeViewModel.loadItems()
    }

    private fun runEntranceAnimation() {
        binding.laundryRecycler.alpha = 0f
        binding.laundryRecycler.translationY = 80f
        binding.laundryRecycler.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun markAsClean(item: ClothingItem) {
        performHaptic(VibrationEffect.EFFECT_HEAVY_CLICK)
        viewLifecycleOwner.lifecycleScope.launch {
            val updated = item.copy(
                laundryStatus = "Clean", 
                wearCount = 0,
                lastLaundryDate = System.currentTimeMillis()
            )
            val result = wardrobeViewModel.updateItem(updated)
            if (result.isSuccess) {
                Snackbar.make(binding.root, "ASSET PURIFIED: ${item.name}", Snackbar.LENGTH_SHORT).show()
                wardrobeViewModel.loadItems()
            }
        }
    }

    private fun initiateMassPurification() {
        performHaptic(VibrationEffect.EFFECT_DOUBLE_CLICK)
        val dirtyItems = adapter.currentList
        if (dirtyItems.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            binding.markAllCleanButton.isEnabled = false
            binding.markAllCleanButton.text = "MARKING CLEAN..."
            
            dirtyItems.forEach { item ->
                val updated = item.copy(laundryStatus = "Clean", wearCount = 0, lastLaundryDate = System.currentTimeMillis())
                wardrobeViewModel.updateItem(updated)
            }
            
            Snackbar.make(binding.root, "Laundry queue marked clean", Snackbar.LENGTH_LONG).show()
            wardrobeViewModel.loadItems()
            binding.markAllCleanButton.isEnabled = true
            binding.markAllCleanButton.text = "MARK ALL CLEAN"
        }
    }

    private fun performHaptic(effectId: Int) {
        val v = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            v.vibrate(VibrationEffect.createPredefined(effectId))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
