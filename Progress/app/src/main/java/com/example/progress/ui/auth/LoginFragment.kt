package com.example.progress.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progress.R
import com.example.progress.databinding.FragmentLoginBinding
import com.example.progress.utils.SessionManager

class LoginFragment : Fragment() {
    // Binding reference (cleared when view is destroyed)
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate binding
        _binding = FragmentLoginBinding.inflate(inflater, container,
            false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.loginPassword.text.toString()
            Log.d("LoginFragment", "Email: $email, Password: $password")
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Email and Password are required",
                    Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, password)
            }
        }

        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            Log.d("LoginFragment", "authResult: $result")
            result.onSuccess { authResponse ->
                // Handle successful login (e.g., navigate to home screen)
                Toast.makeText(requireContext(), "Welcome ${authResponse.user.name}", Toast.LENGTH_LONG).show()
                val session = SessionManager(requireContext().applicationContext)
                session.saveAuthToken(authResponse.tokens.accessToken)

                // Navigate to HomeFragment
//                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }.onFailure { error ->
                // Handle login failure
                Toast.makeText(requireContext(), "Login failed: ${error.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks
        _binding = null
    }

}