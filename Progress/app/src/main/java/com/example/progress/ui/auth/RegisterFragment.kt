package com.example.progress.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.progress.R
import com.example.progress.databinding.FragmentRegisterBinding
import com.example.progress.utils.SessionManager


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val username = binding.editTextUsername.text.toString()
            Log.d("RegisterFragment", "Email: $email, Password: $password, Username: $username")
            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(context, "Email, Password and Username are required", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.signup(username, email, password)
            }
        }

        binding.btnSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            Log.d("RegisterFragment", "registerResult: $result")
            result.onSuccess { authResponse ->
                Toast.makeText(requireContext(), "${authResponse.user.name} successfully registered", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Register failed: ${error.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}