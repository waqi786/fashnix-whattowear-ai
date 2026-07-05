package com.fashnix.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentMorningSaviorBinding
import com.fashnix.app.util.SafeImageLoader
import com.fashnix.app.utils.SampleData

class MorningSaviorFragment : Fragment() {

    private var _binding: FragmentMorningSaviorBinding? = null
    private val binding get() = _binding!!

    private val wardrobeImages by lazy { SampleData.getSampleItems("preview").map { it.imageUrl } }

    private val outfitData by lazy { listOf(
        OutfitCard(
            "Navy Power Suit",
            "White Shirt",
            "Navy Pants",
            "Black Derby",
            "Steel Blazer",
            "Office-ready.",
            wardrobeImages.getOrNull(0).orEmpty()
        ),
        OutfitCard(
            "Urban Rain Armor",
            "Charcoal Sweat",
            "Slim Jeans",
            "Weatherproof Boots",
            "Hooded Trench",
            "Weather-ready.",
            wardrobeImages.getOrNull(5).orEmpty()
        ),
        OutfitCard(
            "Weekend Light Play",
            "Minimal Tee",
            "Soft Chinos",
            "White Sneakers",
            "Light Bomber",
            "Quick casual.",
            wardrobeImages.getOrNull(15).orEmpty()
        )
    ) }

    private var activeIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMorningSaviorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindOutfit(outfitData[activeIndex])
        setupActions()
    }

    private fun bindOutfit(outfit: OutfitCard) {
        binding.currentOutfitLabel.text = outfit.title
        binding.currentOutfitMood.text = outfit.description
        binding.currentOutfitDetails.text = "${outfit.top} + ${outfit.bottom} + ${outfit.shoes}"
        binding.currentOutfitUrgency.text = "AI picked from wardrobe."
        SafeImageLoader.loadWardrobeImage(binding.currentOutfitImage, outfit.imageUrl)
        binding.aiAnswer.text = "AI score 92% - clean, weather-ready, balanced colors."
    }

    private fun setupActions() {
        binding.wearButton.setOnClickListener {
            val current = outfitData[activeIndex]
            binding.aiAnswer.text = "Locked: ${current.top}, ${current.bottom}, ${current.shoes}."
        }

        binding.compareButton.setOnClickListener {
            if (activeIndex < outfitData.lastIndex) {
                activeIndex += 1
            } else {
                activeIndex = 0
            }
            bindOutfit(outfitData[activeIndex])
        }

        binding.chipShoes.setOnClickListener {
            binding.currentOutfitDetails.text = "${outfitData[activeIndex].top} + ${outfitData[activeIndex].bottom} + Black Derby"
            binding.aiAnswer.text = "Shoes swapped. Cleaner formal balance."
        }
        binding.chipPants.setOnClickListener {
            binding.currentOutfitDetails.text = "${outfitData[activeIndex].top} + Navy Pants + ${outfitData[activeIndex].shoes}"
            binding.aiAnswer.text = "Bottom updated. Work-ready fit."
        }
        binding.chipShirt.setOnClickListener {
            binding.currentOutfitDetails.text = "White Shirt + ${outfitData[activeIndex].bottom} + ${outfitData[activeIndex].shoes}"
            binding.aiAnswer.text = "Top updated. Sharp contrast."
        }
        binding.chipJacket.setOnClickListener {
            binding.currentOutfitUrgency.text = "${outfitData[activeIndex].jacket} ready if needed."
            binding.aiAnswer.text = "Layer ready. Use only if weather drops."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class OutfitCard(
        val title: String,
        val top: String,
        val bottom: String,
        val shoes: String,
        val jacket: String,
        val description: String,
        val imageUrl: String
    )
}
