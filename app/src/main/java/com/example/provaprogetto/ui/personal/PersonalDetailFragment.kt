package com.example.provaprogetto.ui.personal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.FragmentDrinkRecipeBinding
import com.example.provaprogetto.room.LocalPersonal
import com.example.provaprogetto.ui.recipe.IngredientAdapter
import com.example.provaprogetto.ui.shopList.ShopListFragmentViewModel
import com.squareup.picasso.Picasso


class PersonalDetailFragment : Fragment() {

    private val viewModel: PersonalFragmentViewModel by viewModels()
    private var _binding: FragmentDrinkRecipeBinding? = null
    private val binding get() = _binding!!
    private val args: PersonalDetailFragmentArgs by navArgs()
    private var shouldReturnToFavFragment: Boolean = false
    private val shopVM: ShopListFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrinkRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            shouldReturnToFavFragment = it.getBoolean("shouldReturnToCustomFragment")
            Log.d("REC", "arguments: $it")
            Log.d("PersonalDetailFragment", "shouldReturnToFavFragment: $shouldReturnToFavFragment")
        }

        val personal = args.personal
        viewModel.getRecipePersonal(personal).observe(viewLifecycleOwner) { item ->
            if (_binding != null) {
                displayPersonalDetails(item)
            }
        }

        binding.ingredientRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ingredientRecyclerView.adapter = IngredientAdapter(shopVM)
    }

    private fun displayPersonalDetails(personal: LocalPersonal) {
        if (_binding == null) return

        binding.drinkName.text = personal.name
        binding.instructions.text = personal.instructions

        if (personal.imageUrl.isNotEmpty()) {
            Picasso.get().load(personal.imageUrl).into(binding.drinkImage)
        } else {
            binding.drinkImage.setImageResource(R.drawable.aaaaaa)
        }

        val ingredientAdapter = binding.ingredientRecyclerView.adapter as IngredientAdapter
        ingredientAdapter.submitList(personal.obtainIngredientsList())

        if (shouldReturnToFavFragment) {
            navigateToFragmentOnBackPress(R.id.navigation_custom)
        }

        binding.buttonBack.setOnClickListener {
            val action = PersonalDetailFragmentDirections.actionPersonalDetailFragmentToPersonalFragment()
            findNavController().navigate(action)
        }

        binding.fabFavorite.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToFragmentOnBackPress(destinationFragmentId: Int) {
        val navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.navigate(destinationFragmentId)
            }
        })
    }
}
