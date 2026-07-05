package com.fashnix.app.ui.wardrobe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.databinding.FragmentEditItemBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditItemFragment : Fragment() {

    private var _binding: FragmentEditItemBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WardrobeViewModel by viewModels()
    private val args: EditItemFragmentArgs by navArgs()

    private var currentItem: ClothingItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupNavigation()
        setupDropdowns()
        observeViewModel()
        
        viewModel.loadItem(args.clothingItemId)
    }

    private fun setupNavigation() {
        binding.editToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        
        binding.saveButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun setupDropdowns() {
        val categories = arrayOf("Apparel", "Footwear", "Accessories", "Personal Care")
        val occasions = arrayOf("Professional", "Casual", "Festive", "Sports", "Formal")
        
        binding.categoryDropdown.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories))
        binding.occasionDropdown.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, occasions))
    }

    private fun observeViewModel() {
        viewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            if (item != null && currentItem == null) {
                currentItem = item
                populateFields(item)
            }
        }
    }

    private fun populateFields(item: ClothingItem) {
        binding.apply {
            nameEditText.setText(item.name)
            brandEditText.setText(item.brand)
            priceEditText.setText(item.price.toString())
            categoryDropdown.setText(item.category, false)
            occasionDropdown.setText(item.occasion, false)
        }
    }

    private fun saveChanges() {
        val item = currentItem ?: return
        
        val updatedItem = item.copy(
            name = binding.nameEditText.text.toString(),
            brand = binding.brandEditText.text.toString(),
            price = binding.priceEditText.text.toString().toDoubleOrNull() ?: 0.0,
            category = binding.categoryDropdown.text.toString(),
            occasion = binding.occasionDropdown.text.toString()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val result = viewModel.updateItem(updatedItem)
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "ITEM UPDATED", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "UPDATE FAILED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
