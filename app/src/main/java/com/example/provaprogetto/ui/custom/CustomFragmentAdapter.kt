package com.example.provaprogetto.ui.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.RicetteBinding
import com.example.provaprogetto.drink.Recipe
import com.example.provaprogetto.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CustomFragmentAdapter(
    private val context: Context,
    private val onItemClick: (Recipe) -> Unit,
    private val onLikeClick: (Recipe) -> Unit) : ListAdapter<Recipe, CustomFragmentAdapter.RecipeViewHolder>(DIFF_CALLBACK) {

    private val auth: FirebaseAuth = Firebase.auth
    val tu = auth.currentUser?.displayName

    private val repository = Repository()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RicetteBinding.inflate(inflater, parent, false)
        return RecipeViewHolder(binding, onItemClick, onLikeClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class RecipeViewHolder(
        private val binding: RicetteBinding,
        private val onItemClick: (Recipe) -> Unit,
        private val onLikeClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        init {
            binding.imageDrink.clipToOutline = true
        }

        fun bind(recipe: Recipe) {
            recipe.imageUrl?.let { setImage(it) }
            recipe.name?.let { setName(it) }
            recipe.autore?.let { setAuthor(it) }

            scope.launch {
                val isFavorite = repository.isFavoriteRecipe(recipe.id)
                recipe.isLike = isFavorite
                updateFavoriteIcon(isFavorite)
            }

            binding.root.setOnClickListener {
                onItemClick(recipe)
            }

            binding.likesIcon.setOnClickListener {
                recipe.isLike = !recipe.isLike
                updateFavoriteIcon(recipe.isLike)
                onLikeClick(recipe)
            }
        }

        private fun updateFavoriteIcon(isLike: Boolean) {
            if (isLike) {
                binding.likesIcon.setImageResource(R.drawable.heart_on)
            } else {
                binding.likesIcon.setImageResource(R.drawable.heart_off)
            }
        }

        private fun setImage(imageUrl: String) {
            if (imageUrl.isNotEmpty()) {
                Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder_image)
                    .resize(200, 200).centerCrop().into(binding.imageDrink)
            } else {
                binding.imageDrink.setImageResource(R.drawable.placeholder_image)
            }
        }

        private fun setName(name: String) {
            binding.nomeDrink.text = name
        }

        private fun setAuthor(author: String?) {
            if (!author.isNullOrEmpty()) {
                binding.author.text = if (author == tu) {
                    context.getString(R.string.created_by, "te")
                } else {
                    context.getString(R.string.created_by, author)
                }
            } else {
                binding.author.visibility = View.GONE
            }
        }
    }

    companion object {
            private val DIFF_CALLBACK =
                object : DiffUtil.ItemCallback<Recipe>() {
                    override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                        return oldItem.id == newItem.id
                    }

                    override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                        return oldItem == newItem
                    }
                }
        }

    }

