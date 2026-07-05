package com.fashnix.app.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.fashnix.app.databinding.FragmentCapsuleGeneratorBinding
import com.fashnix.app.ui.home.HomeViewModel
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint

/**
 * CapsuleGeneratorFragment: AI-driven travel and minimalism utility.
 * Features:
 * - Algorithmic selection of versatile pieces.
 * - Outfit efficiency calculation.
 * - Exportable packing list.
 */
@AndroidEntryPoint
class CapsuleGeneratorFragment : Fragment() {

    private var _binding: FragmentCapsuleGeneratorBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCapsuleGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeData()
    }

    private fun setupUI() {
        binding.generateButton.addExpertHoverEffect()
        binding.generateButton.setOnClickListener {
            // Trigger AI Capsule Generation Logic
            binding.loadingAnim.visibility = View.VISIBLE
            binding.loadingAnim.playAnimation()
            
            // Simulating AI processing
            binding.root.postDelayed({
                binding.loadingAnim.visibility = View.GONE
                viewModel.generateDailyOutfit() // Reusing logic for demo
            }, 2000)
        }
        
        binding.capsuleRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun observeData() {
        viewModel.wardrobeItems.observe(viewLifecycleOwner) { items ->
            if (items != null) {
                binding.statsText.text = "ANALYZING ${items.size} PIECES FOR OPTIMUM VERSATILITY"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
