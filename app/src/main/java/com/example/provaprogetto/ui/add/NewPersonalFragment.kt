package com.example.provaprogetto.ui.add

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.provaprogetto.databinding.FragmentNewRecipeBinding
import com.example.provaprogetto.drink.Ingredient
import com.example.provaprogetto.room.LocalPersonal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class NewPersonalFragment : Fragment() {

    private val viewModel: NewPersonalViewModel by viewModels()
    private lateinit var binding: FragmentNewRecipeBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null

    private lateinit var ingredientAdapter: NewPersonalAdapter
    private val ingredients = mutableListOf<Ingredient>()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNewRecipeBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val savedImagePath = saveImageToInternalStorage(it)
                if (savedImagePath != null) {
                    imageUri = Uri.fromFile(File(savedImagePath))
                    binding.imageViewRecipe.setImageURI(imageUri)
                } else {
                    Toast.makeText(requireContext(), "Immagine non salvata", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonSaveRecipe.setOnClickListener {
            val name = binding.editTextRecipeName.text.toString()
            val instructions = binding.editTextRecipeInstructions.text.toString()
            auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val username = user?.displayName ?: "Unknown User"

            if (imageUri == null) {
                Toast.makeText(requireContext(), "Seleziona un'immagine", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isNotEmpty() && ingredients.isNotEmpty() && instructions.isNotEmpty()) {
                disableUI()
                updateIngredientsFromAdapter()
                CoroutineScope(Dispatchers.IO).launch {
                    val localPersonal = createAndSaveLocalPersonal(
                        name,
                        ingredients,
                        instructions,
                        imageUri.toString(),
                        username
                    )
                    withContext(Dispatchers.Main) {
                        val action = NewPersonalFragmentDirections.actionNewPersonalFragmentToPersonalDetailFragment(localPersonal)
                        findNavController().navigate(action)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Riempi tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonAddIngredient.setOnClickListener {
            addIngredientAndFocusLast()
        }
        val navController = findNavController()
        binding.buttonBack.setOnClickListener {
            navController.navigateUp()
        }

        setupRecyclerView()

        binding.buttonLoadImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        viewModel.predefinedIngredients.observe(viewLifecycleOwner) { ingredients ->
            ingredientAdapter.updatePredefinedIngredients(ingredients)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        ingredientAdapter = NewPersonalAdapter(ingredients, emptyList(), requireContext(), binding.recyclerViewIngredients)
        binding.recyclerViewIngredients.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewIngredients.adapter = ingredientAdapter
        binding.recyclerViewIngredients.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
    }

    private fun disableUI() {
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonSaveRecipe.isEnabled = false
        binding.buttonAddIngredient.isEnabled = false
        binding.buttonLoadImage.isEnabled = false
        binding.editTextRecipeName.isEnabled = false
        binding.editTextRecipeInstructions.isEnabled = false
        binding.recyclerViewIngredients.isEnabled = false
    }

    private fun addIngredientAndFocusLast() {
        ingredients.add(Ingredient("", ""))
        val newPosition = ingredients.size - 1
        ingredientAdapter.notifyItemInserted(newPosition)
        ingredientAdapter.focusLastIngredient()
    }

    private fun updateIngredientsFromAdapter() {
        for (i in 0 until ingredientAdapter.itemCount) {
            val holder = binding.recyclerViewIngredients.findViewHolderForAdapterPosition(i) as? NewPersonalAdapter.IngredientViewHolder
            holder?.let {
                ingredients[i].strIngredient1 = it.binding.editTextIngredientName.text.toString()
                ingredients[i].strMeasure1 = it.binding.editTextIngredientQuantity.text.toString()
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val bitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
            val file = File(requireContext().filesDir, "${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            Log.e("saveImageToInternalStorage", "Errore durante il salvataggio dell'immagine", e)
            null
        }
    }

    private suspend fun createAndSaveLocalPersonal(
        name: String,
        ingredients: List<Ingredient>,
        instructions: String,
        imageUri: String,
        autore: String,
    ): LocalPersonal {
        Log.d("createAndSaveLocalPersonal", "Inizio creazione oggetto")
        val localPersonal = LocalPersonal(
            personalId = "",
            name = name,
            ingredients = ingredients,
            instructions = instructions,
            imageUrl = imageUri,
            autore = autore
        )

        try {
            Log.d("createAndSaveLocalPersonal", "Tentativo di inserimento nel database")

            viewModel.localRepository.insertPersonal(localPersonal)

            Log.d("createAndSaveLocalPersonal", "Inserimento riuscito")

        } catch (e: Exception) {
            Log.e("createAndSaveLocalPersonal", "Errore durante l'inserimento nel database", e)
        }
        return localPersonal
    }
}
