package com.example.provaprogetto.ui.ranking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.RicetteRankingBinding
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

class RankingFragmentAdapter(
    private val context: Context,
    private val onItemClick: (Recipe) -> Unit,
    private val onLikeClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RankingFragmentAdapter.RecipeViewHolder>(DIFF_CALLBACK) {

    private val repository: Repository = Repository()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RicetteRankingBinding.inflate(inflater, parent, false)
        return RecipeViewHolder(binding, onItemClick, onLikeClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class RecipeViewHolder(
        private var binding: RicetteRankingBinding,
        private val onItemClick: (Recipe) -> Unit,
        private val onLikeClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imageDrink.clipToOutline = true
        }

        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun bind(recipe: Recipe, posizione: Int) {
            recipe.imageUrl?.let { setImage(it) }
            recipe.name?.let { setName(it) }
            recipe.autore?.let { setAuthor(it) }
            recipe.likes?.let { setLikes(it) }

            when (posizione) {
                1 -> binding.posizioneRanking.backgroundTintList = context.getColorStateList(R.color.gold)
                2 -> binding.posizioneRanking.backgroundTintList = context.getColorStateList(R.color.silver)
                3 -> binding.posizioneRanking.backgroundTintList = context.getColorStateList(R.color.bronze)
                else -> binding.posizioneRanking.backgroundTintList = context.getColorStateList(android.R.color.transparent)
            }

            binding.posizioneRanking.text = posizione.toString()

            scope.launch {
                recipe.isLike = repository.isFavoriteRecipe(recipe.id)
                if (recipe.isLike) {
                    binding.likesIcon.setImageResource(R.drawable.heart_on)
                } else {
                    binding.likesIcon.setImageResource(R.drawable.heart_off)
                }
            }

            binding.root.setOnClickListener {
                onItemClick(recipe)
            }

            binding.likesIcon.setOnClickListener {
                if (!recipe.isLike) {
                    binding.likesIcon.setImageResource(R.drawable.heart_on)
                } else {
                    binding.likesIcon.setImageResource(R.drawable.heart_off)
                }
                onLikeClick(recipe)
            }
        }

        private fun setLikes(likes: Int) {
            binding.numLikes.text = likes.toString()
            binding.numLikes.visibility = View.VISIBLE
        }

        private fun setImage(imageUrl: String) {
            if (!imageUrl.isNullOrEmpty()) {
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
                val auth: FirebaseAuth = Firebase.auth
                val tu = auth.currentUser?.displayName
                binding.author.text = if (author == tu) {
                    context.getString(R.string.created_by, "te")
                } else {
                    context.getString(R.string.created_by, author)
                }
                binding.author.visibility = View.VISIBLE
            } else {
                binding.author.visibility = View.GONE
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                return oldItem == newItem
            }
        }
    }
}

