package com.example.provaprogetto.ui.fav

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.RicetteBinding
import com.example.provaprogetto.drink.Drink
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

class FavFragmentAdapter(
    private val context: Context,
    private val onItemClick: (Any) -> Unit,
    private val onLikeClick: (Any) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private val auth: FirebaseAuth = Firebase.auth
    val currentUser = auth.currentUser?.displayName

    private val repository = Repository()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RicetteBinding.inflate(inflater, parent, false)
        return when (viewType) {
            VIEW_TYPE_DRINK -> DrinkViewHolder(binding, onItemClick, onLikeClick)
            VIEW_TYPE_RECIPE -> RecipeViewHolder(binding, onItemClick)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is DrinkViewHolder -> holder.bind(item as Drink)
            is RecipeViewHolder -> holder.bind(item as Recipe)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Drink -> VIEW_TYPE_DRINK
            is Recipe -> VIEW_TYPE_RECIPE
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    inner class DrinkViewHolder(
        private val binding: RicetteBinding,
        private val onItemClick: (Any) -> Unit,
        private val onLikeClick: (Drink) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imageDrink.clipToOutline = true
        }

        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun bind(drink: Drink) {
            drink.image?.let { setImage(it) }
            drink.name?.let { setName(it) }
            binding.author.visibility = View.GONE

            scope.launch {
                drink.isLike = repository.isFavoriteDrink(drink.id.toString())
                if (drink.isLike) {
                    binding.likesIcon.setImageResource(R.drawable.heart_on)
                } else {
                    binding.likesIcon.setImageResource(R.drawable.heart_off)
                }
            }
            binding.root.setOnClickListener {
                onItemClick(drink)
            }

            binding.likesIcon.setOnClickListener {
                if (!drink.isLike) {
                    binding.likesIcon.setImageResource(R.drawable.heart_on)
                } else {
                    binding.likesIcon.setImageResource(R.drawable.heart_off)
                }
                onLikeClick(drink)
            }
        }

        private fun setImage(imageUrl: String) {
            Picasso.get().load(imageUrl).into(binding.imageDrink)
        }

        private fun setName(name: String) {
            binding.nomeDrink.text = name
        }
    }

    inner class RecipeViewHolder(
        private val binding: RicetteBinding,
        private val onItemClick: (Any) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imageDrink.clipToOutline = true
        }

        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun bind(recipe: Recipe) {
            recipe.imageUrl?.let { setImage(it) }
            recipe.name?.let { setName(it) }
            recipe.autore?.let { setAuthor(it) }
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


        private fun setImage(imageUrl: String) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder_image).resize(200, 200).centerCrop().into(binding.imageDrink)
        }

        private fun setName(name: String) {
            binding.nomeDrink.text = name
        }

        private fun setAuthor(author: String?) {
            if (!author.isNullOrEmpty()) {
                binding.author.text = if (author == currentUser) {
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
        private const val VIEW_TYPE_DRINK = 1
        private const val VIEW_TYPE_RECIPE = 2

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is Drink && newItem is Drink -> oldItem.id == newItem.id
                    oldItem is Recipe && newItem is Recipe -> oldItem.id == newItem.id
                    else -> false
                }
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is Drink && newItem is Drink -> oldItem == newItem
                    oldItem is Recipe && newItem is Recipe -> oldItem == newItem
                    else -> false
                }
            }
        }
    }
}
