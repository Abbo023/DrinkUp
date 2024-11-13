package com.example.provaprogetto.ui.pred

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.RicetteBinding
import com.example.provaprogetto.drink.Drink
import com.example.provaprogetto.repository.Repository
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PredFragmentAdapter(
    private val context: Context,
    private val onItemClick: (Drink) -> Unit,
    private val onFavoriteClick: (Drink) -> Unit
) : ListAdapter<Drink, PredFragmentAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val repository: Repository = Repository()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RicetteBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, onItemClick, onFavoriteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = getItem(position)
        holder.bind(drink)
    }

    inner class ViewHolder(
        private val binding: RicetteBinding,
        private val onItemClick: (Drink) -> Unit,
        private val onFavoriteClick: (Drink) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        init {
            binding.imageDrink.clipToOutline = true
        }

        fun bind(drink: Drink) {
            drink.image?.let { setImage(it) }
            drink.name?.let { setName(it) }
            scope.launch {
                if (repository.isFavoriteDrink(drink.id.toString())) {
                    drink.isLike = true
                    binding.likesIcon.setImageResource(R.drawable.heart_on)
                } else {
                    drink.isLike = false
                    binding.likesIcon.setImageResource(R.drawable.heart_off)
                }
            }
            binding.root.setOnClickListener {
                onItemClick(drink)
            }

            binding.likesIcon.setOnClickListener {
                drink.isLike = !drink.isLike
                updateFavoriteIcon(drink.isLike)
                onFavoriteClick(drink)
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            if (isFavorite) {
                binding.likesIcon.setImageResource(R.drawable.heart_on)
            } else {
                binding.likesIcon.setImageResource(R.drawable.heart_off)
            }
        }

        private fun setImage(imageUrl: String) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder_image).into(binding.imageDrink)
        }

        private fun setName(name: String) {
            binding.nomeDrink.text = name
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Drink>() {
            override fun areItemsTheSame(oldItem: Drink, newItem: Drink): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Drink, newItem: Drink): Boolean {
                return oldItem == newItem
            }
        }
    }
}
