package com.example.provaprogetto.ui.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R

class IngredientFilterAdapter(
    private val ingredients: MutableList<String>,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<IngredientFilterAdapter.IngredientFilterViewHolder>() {

    inner class IngredientFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientName: TextView = itemView.findViewById(R.id.ingredient_name)
        private val removeButton: ImageButton = itemView.findViewById(R.id.remove_button)

        fun bind(ingredient: String) {
            ingredientName.text = ingredient
            removeButton.setOnClickListener {
                onRemoveClick(ingredient)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientFilterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_filter, parent, false)
        return IngredientFilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientFilterViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.bind(ingredient)
    }

    override fun getItemCount(): Int = ingredients.size

    fun setData(newIngredients: List<String>) {
        ingredients.clear()
        ingredients.addAll(newIngredients)
        notifyDataSetChanged()
    }
}
