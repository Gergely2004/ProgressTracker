package com.example.progress.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.example.progress.R
import com.example.progress.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                binding.profileImageView.setImageURI(selectedUri)
                android.util.Log.d("EditProfile", "Image selected: $selectedUri")
                viewModel.uploadProfileImage(selectedUri)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Failed to process image", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                displayProfile(it)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.saveButton.isEnabled = !isLoading
            binding.cancelButton.isEnabled = !isLoading
            binding.usernameEditText.isEnabled = !isLoading
            binding.changePhotoButton.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), getString(R.string.profile_updated_success), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            val username = binding.usernameEditText.text?.toString()?.trim()

            if (username.isNullOrEmpty()) {
                binding.usernameInputLayout.error = getString(R.string.username_required)
                return@setOnClickListener
            }

            binding.usernameInputLayout.error = null
            viewModel.updateProfile(username)
        }

        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.changePhotoButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.profileImageView.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun displayProfile(profile: com.example.progress.model.ProfileResponseDto) {
        binding.emailTextView.text = profile.email
        binding.usernameEditText.setText(profile.username)

        profile.profileImageUrl?.let { url ->
            binding.profileImageView.load(url) {
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                transformations(CircleCropTransformation())
            }
        } ?: profile.profileImageBase64?.let { base64String ->
            try {
                val base64Data = if (base64String.startsWith("data:image")) {
                    base64String.substringAfter("base64,", "")
                } else {
                    base64String
                }

                if (base64Data.isNotEmpty()) {
                    val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.profileImageView.setImageBitmap(bitmap)
                } else {
                    binding.profileImageView.setImageResource(R.drawable.ic_profile)
                }
            } catch (e: Exception) {
                android.util.Log.e("EditProfile", "Failed to decode Base64 image", e)
                binding.profileImageView.setImageResource(R.drawable.ic_profile)
            }
        } ?: run {
            binding.profileImageView.setImageResource(R.drawable.ic_profile)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
