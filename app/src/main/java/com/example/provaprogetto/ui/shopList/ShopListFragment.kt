package com.example.provaprogetto.ui.shopList

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R

class ShopListFragment : Fragment() {

    private val viewModel: ShopListFragmentViewModel by activityViewModels()
    private lateinit var shopListAdapter: ShopListFragmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_shoplist, container, false)

        val ingredientInput: EditText = rootView.findViewById(R.id.ingredient_input)
        val addButton: Button = rootView.findViewById(R.id.add_ingredient_button)
        val autoCompleteTextView: AutoCompleteTextView = rootView.findViewById(R.id.predefined_ingredients_spinner)
        val recyclerView: RecyclerView = rootView.findViewById(R.id.shopping_list_recyclerview)

        shopListAdapter = ShopListFragmentAdapter { ingredient, quantity ->
            viewModel.updateQuantity(ingredient, quantity)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = shopListAdapter

        viewModel.predefinedIngredients.observe(viewLifecycleOwner) { predefinedIngredients ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                predefinedIngredients
            )
            autoCompleteTextView.setAdapter(adapter)
        }

        addButton.setOnClickListener {
            val ingredient = ingredientInput.text.toString()
            if (ingredient.isNotBlank()) {
                viewModel.addIngredient(ingredient)
                ingredientInput.text.clear()
            }
        }

        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val selectedIngredient = parent.getItemAtPosition(position).toString()
            if (position != 0) {
                viewModel.addIngredient(selectedIngredient)
                autoCompleteTextView.text.clear()
                autoCompleteTextView.clearFocus()
            }
        }

        viewModel.shoppingList.observe(viewLifecycleOwner) { shopListIngredients ->
            Log.d("ShopListFragment", "Update recyclerView con: $shopListIngredients")
            shopListAdapter.submitList(shopListIngredients.toList())
        }

        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    viewModel.removeIngredient(shopListAdapter.currentList[viewHolder.adapterPosition])
                }


            })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return rootView
    }
}
