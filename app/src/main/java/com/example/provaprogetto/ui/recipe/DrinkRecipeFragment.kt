package com.example.provaprogetto.ui.recipe

import android.os.Bundle
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
import com.example.provaprogetto.drink.Drink
import com.example.provaprogetto.repository.Repository
import com.example.provaprogetto.ui.shopList.ShopListFragmentViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class DrinkRecipeFragment : Fragment() {

    private val args: DrinkRecipeFragmentArgs by navArgs()
    private val viewModel: DrinkRecipeFragmentViewModel by viewModels()
    private var _binding: FragmentDrinkRecipeBinding? = null
    private val binding get() = _binding!!
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

        val drinkId = args.drinkId
        viewModel.getDrinkDetails(drinkId).observe(viewLifecycleOwner) { drink ->
            drink?.let {
                bindDrinkDetails(it)
                setupFavoriteButton(it)
            }
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.ingredientRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ingredientRecyclerView.adapter = IngredientAdapter(shopVM)
        binding.ingredientRecyclerView.isNestedScrollingEnabled = false
    }

    private fun setupFavoriteButton(drink: Drink) {
        binding.fabFavorite.setOnClickListener {
            isLiked = !isLiked
            binding.fabFavorite.setImageResource(if (isLiked) R.drawable.heart_on else R.drawable.heart_off)
            viewModel.toggleFavorite(drink)
        }
    }

    private fun bindDrinkDetails(drink: Drink) {
        viewLifecycleOwner.lifecycleScope.launch {
            _binding?.let { binding ->
                Picasso.get().load(drink.image).into(binding.drinkImage)
                binding.drinkName.text = drink.name
                binding.instructions.text = drink.getParsedInstruction()

                isLiked = repository.isFavoriteDrink(drink.id.toString())
                binding.fabFavorite.setImageResource(if (isLiked) R.drawable.heart_on else R.drawable.heart_off)

                (binding.ingredientRecyclerView.adapter as IngredientAdapter).submitList(drink.getIngredients())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
