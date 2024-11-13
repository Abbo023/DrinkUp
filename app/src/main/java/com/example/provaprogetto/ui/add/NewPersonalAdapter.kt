package com.example.provaprogetto.ui.add

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.ItemIngredientBinding
import com.example.provaprogetto.drink.Ingredient

class NewPersonalAdapter(
    private val ingredients: MutableList<Ingredient>,
    private var predefinedIngredients: List<String>,
    private val context: Context,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<NewPersonalAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size

    inner class IngredientViewHolder(val binding: ItemIngredientBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentIngredient: Ingredient? = null

        init {
            binding.editTextIngredientName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    currentIngredient?.strIngredient1 = s.toString()
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            binding.editTextIngredientQuantity.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    currentIngredient?.strMeasure1 = s.toString()
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, predefinedIngredients)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.ingredientSpinner.adapter = spinnerAdapter

            binding.ingredientSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedIngredient = parent?.getItemAtPosition(position).toString()
                    if (selectedIngredient.isNotEmpty()) {
                        binding.editTextIngredientName.setText(selectedIngredient)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        fun bind(ingredient: Ingredient) {
            currentIngredient = ingredient
            binding.editTextIngredientName.setText(ingredient.strIngredient1)
            binding.editTextIngredientQuantity.setText(ingredient.strMeasure1)
        }
    }

    fun updatePredefinedIngredients(ingredients: List<String>) {
        this.predefinedIngredients = ingredients
        notifyDataSetChanged()
    }

    fun focusLastIngredient() {
        recyclerView.post {
            val lastPosition = ingredients.size - 1
            recyclerView.scrollToPosition(lastPosition)
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(lastPosition) as? IngredientViewHolder
            viewHolder?.let {
                it.itemView.findViewById<EditText>(R.id.editTextIngredientName).requestFocus()
            }
        }
    }
}
