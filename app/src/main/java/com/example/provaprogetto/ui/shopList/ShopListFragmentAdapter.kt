package com.example.provaprogetto.ui.shopList

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R
import com.example.provaprogetto.drink.ShopListIngredients

class ShopListFragmentAdapter(
    private val onQuantityChanged: (String, Int) -> Unit
) : ListAdapter<ShopListIngredients, ShopListFragmentAdapter.ShopListViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shoplist, parent, false)
        return ShopListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopListViewHolder, position: Int) {
        val ingredient = getItem(position)
        holder.bind(ingredient)
    }

    inner class ShopListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientName: TextView = itemView.findViewById(R.id.ingredient_name)
        private val ingredientQuantity: EditText = itemView.findViewById(R.id.ingredient_quantity)
        private var previousQuantity: String? = null

        fun bind(ingredient: ShopListIngredients) {
            ingredientName.text = ingredient.ingredient
            ingredientQuantity.setText(ingredient.quantity.toString())

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    previousQuantity = s?.toString()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    ingredient.quantity = s?.toString()?.toIntOrNull() ?: 0
                }

                override fun afterTextChanged(s: Editable?) {
                    val newQuantity = s?.toString()?.toIntOrNull() ?: 0

                    if (previousQuantity?.toIntOrNull() != newQuantity) {
                        ingredient.quantity = newQuantity
                        onQuantityChanged(ingredient.ingredient, newQuantity)
                    }
                }
            }

            ingredientQuantity.addTextChangedListener(textWatcher)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ShopListIngredients>() {
        override fun areItemsTheSame(oldItem: ShopListIngredients, newItem: ShopListIngredients): Boolean {
            return oldItem.ingredient == newItem.ingredient
        }

        override fun areContentsTheSame(oldItem: ShopListIngredients, newItem: ShopListIngredients): Boolean {
            return oldItem == newItem
        }
    }
}
