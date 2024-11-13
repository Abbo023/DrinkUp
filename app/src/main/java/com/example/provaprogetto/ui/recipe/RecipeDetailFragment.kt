package com.example.provaprogetto.ui.recipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.FragmentDrinkRecipeBinding
import com.example.provaprogetto.drink.Recipe
import com.example.provaprogetto.repository.Repository
import com.example.provaprogetto.ui.shopList.ShopListFragmentViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class RecipeDetailFragment : Fragment() {

    private val viewModel: DrinkRecipeFragmentViewModel by viewModels()
    private var _binding: FragmentDrinkRecipeBinding? = null
    private val binding get() = _binding!!
    private val args: RecipeDetailFragmentArgs by navArgs()
    private var shouldReturnToCustomFragment = false
    private val shopVM: ShopListFragmentViewModel by activityViewModels()
    private val repository: Repository = Repository()
    private var isLiked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrinkRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (_binding == null) return

        arguments?.let {
            shouldReturnToCustomFragment = it.getBoolean("shouldReturnToCustomFragment")
            Log.d("REC", "arguments: $it")
            Log.d("RecipeDetailFragment", "shouldReturnToFavFragment: $shouldReturnToCustomFragment")
        }

        val recipe = args.recipe
        viewModel.getRecipeDrink(recipe).observe(viewLifecycleOwner) { recipe ->
            recipe?.let {
                displayRecipeDetails(it)
            }

            if (recipe != null) {
                binding.fabFavorite.setOnClickListener {
                    isLiked = !isLiked
                    binding.fabFavorite.setImageResource(if (isLiked) R.drawable.heart_on else R.drawable.heart_off)
                    viewModel.toggleLike(recipe)
                }
            }
        }

        val navController = findNavController()
        binding.buttonBack.setOnClickListener {
            navController.navigateUp()
        }

        binding.ingredientRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ingredientRecyclerView.adapter = IngredientAdapter(shopVM)
        binding.ingredientRecyclerView.isNestedScrollingEnabled = false
    }

    private fun displayRecipeDetails(recipe: Recipe) {
        if (_binding == null) return

        binding.drinkName.text = recipe.name
        binding.instructions.text = recipe.instructions

        if (recipe.imageUrl.isNotEmpty()) {
            Picasso.get().load(recipe.imageUrl).into(binding.drinkImage)
        } else {
            binding.drinkImage.setImageResource(R.drawable.placeholder_image)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            if (_binding != null) {
                isLiked = repository.isFavoriteRecipe(recipe.id)
                binding.fabFavorite.setImageResource(if (isLiked) R.drawable.heart_on else R.drawable.heart_off)
            }
        }

        val ingredientAdapter = binding.ingredientRecyclerView.adapter as IngredientAdapter
        ingredientAdapter.submitList(recipe.obtainIngredientsList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
