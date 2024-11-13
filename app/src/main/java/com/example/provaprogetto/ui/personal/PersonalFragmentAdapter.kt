package com.example.provaprogetto.ui.personal

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.RicetteFavBinding
import com.example.provaprogetto.room.LocalPersonal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

 class PersonalFragmentAdapter(
    private val context: Context,
    private val onItemClick: (LocalPersonal) -> Unit,
    private val onUploadClick: (LocalPersonal, () -> Unit) -> Unit,
) : ListAdapter<LocalPersonal, PersonalFragmentAdapter.PersonalViewHolder>(DIFF_CALLBACK) {


     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonalViewHolder {
         val inflater = LayoutInflater.from(parent.context)
         val binding = RicetteFavBinding.inflate(inflater, parent, false)
         return PersonalViewHolder(binding, onItemClick, onUploadClick)
     }

     override fun onBindViewHolder(holder: PersonalViewHolder, position: Int) {
         holder.bind(getItem(position))
     }

     override fun onBindViewHolder(holder: PersonalViewHolder, position: Int, payloads: List<Any>) {
         if (payloads.isNotEmpty() && payloads[0] is Boolean) {
             holder.updateUploadIcon(getItem(position).isUpload, payloads[0] as Boolean)
         } else {
             super.onBindViewHolder(holder, position, payloads)
         }
     }

     fun updateItem(updatedPersonal: LocalPersonal) {
         val currentList = currentList.toMutableList()
         val position = currentList.indexOfFirst { it.personalId == updatedPersonal.personalId }
         if (position != -1) {
             currentList[position] = updatedPersonal
             submitList(currentList)
             notifyItemChanged(position)
         }
     }

     fun updateProcessingState(personalId: String, isProcessing: Boolean) {
         val position = currentList.indexOfFirst { it.id.toString() == personalId }
         if (position != -1) {
             notifyItemChanged(position, isProcessing)
         }
     }


     inner class PersonalViewHolder(
        private val binding: RicetteFavBinding,
        private val onItemClick: (LocalPersonal) -> Unit,
        private val onUploadClick: (LocalPersonal, () -> Unit) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imageDrink.clipToOutline = true
        }

        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun bind(personal: LocalPersonal) {

            val imageUri = personal.imageUrl?.let { Uri.parse(it) }
            imageUri?.let { setImage(it.toString()) }
            personal.name?.let { setName(it) }
            personal.autore?.let { setAuthor(it) }

            scope.launch {
                if (personal.isUpload) {
                    binding.uploadIcon.setImageResource(R.drawable.upload_off)
                } else {
                    binding.uploadIcon.setImageResource(R.drawable.upload_on)
                }
            }
            Log.d("PersonalFragmentAdapter", "Image URI: $imageUri")

            binding.root.setOnClickListener {
                onItemClick(personal)
            }

            updateUploadIcon(personal.isUpload, false)

            binding.uploadIcon.setOnClickListener {
                binding.uploadIcon.isClickable = false
                binding.uploadIcon.isEnabled = false
                updateUploadIcon(personal.isUpload, true)

                onUploadClick(personal) {
                    updateUploadIcon(personal.isUpload, false)
                    binding.uploadIcon.isEnabled = true
                    binding.uploadIcon.isClickable = true
                }
            }
        }

         fun updateUploadIcon(isUploaded: Boolean, isProcessing: Boolean) {
            when {
                isProcessing -> binding.uploadIcon.setImageResource(R.drawable.ic_loading)
                isUploaded -> binding.uploadIcon.setImageResource(R.drawable.upload_off)
                else -> binding.uploadIcon.setImageResource(R.drawable.upload_on)
            }
        }


        private fun setImage(imageUri: String) {
            val uri = Uri.parse(imageUri)
            Log.d("PersonalFragmentAdapter", "Image URI: $uri")

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    Picasso.get()
                        .load(uri)
                        .into(binding.imageDrink, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                Log.d("PersonalFragmentAdapter", "Immagine caricata con successo")
                            }override fun onError(e: Exception?) {
                                Log.e("PersonalFragmentAdapter", "Errore caricamento immagine: ${e?.message}")
                            }
                        })
                } else {
                    Log.e("PersonalFragmentAdapter", "InputStream nullo")
                }
            } catch (e: Exception) {
                Log.e("PersonalFragmentAdapter", "Errore in setImage: ${e.message}")
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
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LocalPersonal>() {
            override fun areItemsTheSame(oldItem: LocalPersonal, newItem: LocalPersonal): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LocalPersonal, newItem: LocalPersonal): Boolean {
                return oldItem == newItem
            }
        }
    }
}


