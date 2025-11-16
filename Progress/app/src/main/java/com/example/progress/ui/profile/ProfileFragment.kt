package com.example.progress.ui.profile

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.progress.R
import com.example.progress.databinding.FragmentProfileBinding
import coil.load
import coil.transform.CircleCropTransformation
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var habitAdapter: HabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Load profile and habits
        viewModel.loadProfile()
        viewModel.loadHabits()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter()
        binding.habitsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }
    }

    private fun setupObservers() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                displayProfile(it)
            }
        }

        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            if (habits.isEmpty()) {
                binding.noHabitsTextView.visibility = View.VISIBLE
                binding.habitsRecyclerView.visibility = View.GONE
            } else {
                binding.noHabitsTextView.visibility = View.GONE
                binding.habitsRecyclerView.visibility = View.VISIBLE
                habitAdapter.updateHabits(habits)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.addHabitButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_addHabitFragment)
        }

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun displayProfile(profile: com.example.progress.model.ProfileResponseDto) {
        binding.usernameTextView.text = profile.username
        binding.emailTextView.text = profile.email

        // Display description if available
        profile.description?.let {
            binding.descriptionTextView.text = it
            binding.descriptionTextView.visibility = View.VISIBLE
        } ?: run {
            binding.descriptionTextView.visibility = View.GONE
        }

        // Format and display join date
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = formatter.parse(profile.createdAt)
            val displayFormatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            binding.joinDateTextView.text = "Joined: ${displayFormatter.format(date)}"
        } catch (e: Exception) {
            binding.joinDateTextView.text = "Joined: ${profile.createdAt}"
        }

        // Load profile image
        profile.profileImageUrl?.let { url ->
            binding.profileImageView.load(url) {
                placeholder(android.R.drawable.ic_menu_myplaces)
                error(android.R.drawable.ic_menu_myplaces)
                transformations(CircleCropTransformation())
            }
        } ?: profile.profileImageBase64?.let { base64 ->
            try {
                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.profileImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // Keep default image
            }
        }

        // Load cover image
        profile.coverImageUrl?.let { url ->
            binding.coverImageView.load(url) {
                placeholder(android.R.color.darker_gray)
                error(android.R.color.darker_gray)
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear stored token
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("access_token").apply()

        // Navigate back to login
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}