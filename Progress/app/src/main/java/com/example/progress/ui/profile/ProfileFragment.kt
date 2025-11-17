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

        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun displayProfile(profile: com.example.progress.model.ProfileResponseDto) {
        binding.usernameTextView.text = profile.username
        binding.emailTextView.text = profile.email

        profile.description?.let {
            binding.descriptionTextView.text = it
            binding.descriptionTextView.visibility = View.VISIBLE
        } ?: run {
            binding.descriptionTextView.visibility = View.GONE
        }

        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = formatter.parse(profile.createdAt)
            val displayFormatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            val joinedValue = date?.let { displayFormatter.format(it) } ?: profile.createdAt
            val joinedText = getString(R.string.profile_joined_label, joinedValue)
            binding.joinDateTextView.text = joinedText
        } catch (_: Exception) {
            binding.joinDateTextView.text = getString(R.string.profile_joined_label, profile.createdAt)
        }

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
            } catch (_: Exception) {
            }
        }

        profile.coverImageUrl?.let { url ->
            binding.coverImageView.load(url) {
                placeholder(android.R.color.darker_gray)
                error(android.R.color.darker_gray)
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.profile_logout_title))
            .setMessage(getString(R.string.profile_logout_message))
            .setPositiveButton(R.string.yes) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun performLogout() {
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("access_token").apply()

        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.profile.value == null) {
            viewModel.loadProfile()
        } else {
            viewModel.profile.value?.id?.let { viewModel.loadHabitsByUser(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}