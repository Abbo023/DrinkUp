package com.example.provaprogetto.ui.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.databinding.IngredientsBinding
import com.example.provaprogetto.drink.Ingredient
import com.example.provaprogetto.ui.shopList.ShopListFragmentViewModel

class IngredientAdapter(private val viewModel: ShopListFragmentViewModel) : ListAdapter<Ingredient, IngredientAdapter.ViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = IngredientsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = getItem(position)
        holder.bind(ingredient)
    }

    inner class ViewHolder(private val binding: IngredientsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ingredient: Ingredient) {
            binding.name.text = ingredient.strIngredient1
            binding.measure.text = ingredient.strMeasure1

            binding.buttonShop.setOnClickListener {
                ingredient.strIngredient1?.let { it1 -> viewModel.addIngredient(it1)
                Toast.makeText(binding.root.context, "${ingredient.strIngredient1} aggiunto alla lista della spesa", Toast.LENGTH_SHORT).show()}
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Ingredient>() {
            override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
                return oldItem.strIngredient1 == newItem.strIngredient1
            }

            override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
                return oldItem == newItem
            }
        }
    }
}
