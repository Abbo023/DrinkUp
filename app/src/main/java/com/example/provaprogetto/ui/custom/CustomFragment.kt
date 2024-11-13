package com.example.provaprogetto.ui.custom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.FragmentCustomBinding
import com.example.provaprogetto.ui.filter.IngredientFilterAdapter
import com.example.provaprogetto.ui.filter.IngredientFilterViewModel
import com.example.provaprogetto.ui.shopList.ShopListFragmentViewModel
import com.google.android.material.button.MaterialButton

class CustomFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomFragmentAdapter
    private val viewModel: CustomFragmentViewModel by viewModels()
    private var _binding: FragmentCustomBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchButton: Button
    private val filterViewModel: IngredientFilterViewModel by viewModels()
    private val shopListViewModel: ShopListFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerViewCustom
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = CustomFragmentAdapter(
            requireContext(),
            onItemClick = { item ->
                val action = CustomFragmentDirections.actionCustomFragmentToRecipeDetailFragment1(item)
                findNavController().navigate(action)
            },
            onLikeClick = { recipe -> viewModel.toggleLike(recipe) }
        )

        recyclerView.adapter = adapter

        viewModel.recipe.observe(viewLifecycleOwner, Observer { recipe ->
            _binding?.let { adapter.submitList(recipe) }
        })

        viewModel.isFiltered.observe(viewLifecycleOwner, Observer { isFiltered ->

            _binding?.filterIndicator?.visibility = if (isFiltered) View.VISIBLE else View.GONE
        })

        viewModel.restoreFilterState()

        searchButton = binding.searchButton
        searchButton.setOnClickListener { showSearchPopup() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showSearchPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.search_popup, null)
        val buttonShop = dialogView.findViewById<MaterialButton>(R.id.shop_button)
        val drinkNameInput = dialogView.findViewById<EditText>(R.id.drink_name_input)
        val ingredientsInput = dialogView.findViewById<EditText>(R.id.ingredients_input)
        val addButton = dialogView.findViewById<Button>(R.id.add_button)
        val ingredientSpinner = dialogView.findViewById<Spinner>(R.id.ingredient_spinner)
        val selectedIngredientsList = dialogView.findViewById<RecyclerView>(R.id.selected_ingredients_list)

        selectedIngredientsList.layoutManager = LinearLayoutManager(requireContext())

        val popupAdapter = IngredientFilterAdapter(mutableListOf()) { ingredient ->
            filterViewModel.removeIngredient(ingredient)
        }
        selectedIngredientsList.adapter = popupAdapter

        filterViewModel.ingredientsList.observe(viewLifecycleOwner) { ingredients ->
            popupAdapter.setData(ingredients)
        }

        filterViewModel.predefinedIngredients.observe(viewLifecycleOwner) { ingredients ->
            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ingredients)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            ingredientSpinner.adapter = spinnerAdapter
        }

        ingredientSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedIngredient = parent?.getItemAtPosition(position).toString()
                if (selectedIngredient != " ") {
                    filterViewModel.addIngredient(selectedIngredient)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        drinkNameInput.setText(viewModel.currentDrinkName)
        filterViewModel.ingredientsList.value?.let { ingredients ->
            Log.d("PredFragment", "Ingredienti selezionati: $ingredients")
            popupAdapter.setData(ingredients)
        }

        addButton.setOnClickListener {
            val ingredientFromInput = ingredientsInput.text.toString().trim()
            if (ingredientFromInput.isNotEmpty()) {
                filterViewModel.addIngredient(ingredientFromInput)
                ingredientsInput.text.clear()
            }
        }

        buttonShop.setOnClickListener {
            shopListViewModel.shoppingList.value?.forEach { shopIngredient ->
                filterViewModel.addIngredient(shopIngredient.ingredient)
            }
        }

        builder.setView(dialogView)
            .setPositiveButton("Filtra") { _, _ ->
                val drinkName = drinkNameInput.text.toString()
                val selectedIngredients = filterViewModel.ingredientsList.value ?: emptyList()
                viewModel.searchDrinksByIngredientsAndName(selectedIngredients, drinkName)
            }
            .setNegativeButton("Annulla") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Ripristina") { _, _ ->
                drinkNameInput.text.clear()
                viewModel.currentDrinkName = null
                viewModel.currentIngredients = emptyList()
                filterViewModel.clearIngredients()
                viewModel.fetchAllDrinks()
            }

        val dialog = builder.create()
        dialog.show()
    }
}
