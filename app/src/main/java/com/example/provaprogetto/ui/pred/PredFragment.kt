package com.example.provaprogetto.ui.pred

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.FragmentPredBinding
import com.example.provaprogetto.ui.filter.IngredientFilterAdapter
import com.example.provaprogetto.ui.filter.IngredientFilterViewModel
import com.example.provaprogetto.ui.shopList.ShopListFragmentViewModel
import com.google.android.material.button.MaterialButton


class PredFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredFragmentAdapter
    private lateinit var searchButton: Button
    private lateinit var filterIndicator: ImageView
    private var _binding: FragmentPredBinding? = null
    private val binding get() = _binding!!
    private val filterViewModel: IngredientFilterViewModel by viewModels()
    private val shopListViewModel: ShopListFragmentViewModel by viewModels()
    private val viewModel: PredFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPredBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerViewPred
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        filterIndicator = binding.filterIndicator

        adapter = PredFragmentAdapter(
            requireContext(),
            onItemClick = { drink ->
                val action = PredFragmentDirections.actionPredFragmentToDrinkRecipeFragment1(drink.id)
                findNavController().navigate(action)
            },
            onFavoriteClick = { drink ->
                viewModel.toggleFavorite(drink)
            }
        )

        recyclerView.adapter = adapter

        viewModel.drinks.observe(viewLifecycleOwner, Observer { drinks ->

            _binding?.let { adapter.submitList(drinks) }
        })

        viewModel.isFiltered.observe(viewLifecycleOwner, Observer { isFiltered ->
            _binding?.filterIndicator?.visibility = if (isFiltered) View.VISIBLE else View.GONE
        })

        searchButton = binding.searchButton
        searchButton.setOnClickListener { showSearchPopup() }

        viewModel.restoreFilterState()

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
        val drinkNameInput = dialogView.findViewById<EditText>(R.id.drink_name_input)
        val ingredientsInput = dialogView.findViewById<EditText>(R.id.ingredients_input)
        val addButton = dialogView.findViewById<Button>(R.id.add_button)
        val buttonShop = dialogView.findViewById<MaterialButton>(R.id.shop_button)
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
            Log.d("PredFragment", "Ingredienti dal viewModel: $ingredients")
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
            val shoppingListIngredients = shopListViewModel.shoppingList.value
            shoppingListIngredients?.forEach { shopIngredient ->
                filterViewModel.addIngredient(shopIngredient.ingredient)
            }
        }

        builder.setView(dialogView)
            .setPositiveButton("Filtra") { _, _ ->
                val drinkName = drinkNameInput.text.toString()
                val selectedIngredients = filterViewModel.ingredientsList.value ?: emptyList()
                viewModel.searchDrinksByIngredientsAndName(selectedIngredients, drinkName)
            }
            .setNegativeButton("Annulla") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Ripristina") { _, _ ->
                drinkNameInput.text.clear()
                viewModel.currentDrinkName = null
                viewModel.currentIngredients = emptyList()
                filterViewModel.clearIngredients()
                viewModel.fetchDrinkList()
            }


        val dialog = builder.create()
        dialog.show()

    }


}
