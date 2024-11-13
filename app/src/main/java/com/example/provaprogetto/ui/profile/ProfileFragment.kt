package com.example.provaprogetto.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.provaprogetto.R
import com.example.provaprogetto.databinding.FragmentProfileBinding
import com.example.provaprogetto.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button
    private val viewModel: ProfileFragmentViewModel by viewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        changePasswordButton = binding.changePasswordButton
        logoutButton = binding.logoutButton

        changePasswordButton.setOnClickListener { viewModel.changePassword() }
        logoutButton.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        viewModel.userData.observe(viewLifecycleOwner) { data ->
            binding.usernameCardView.findViewById<TextView>(R.id.username_text_view).text =
                data["username"]?.toString() ?: "Nome non disponibile"
            binding.emailCardView.findViewById<TextView>(R.id.email_text_view).text =
                data["email"]?.toString() ?: "Email non disponibile"
            binding.passwordCardView.findViewById<TextView>(R.id.password_text_view).text = "********"

        }
    }

}
