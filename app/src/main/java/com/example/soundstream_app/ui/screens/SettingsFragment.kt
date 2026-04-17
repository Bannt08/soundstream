package com.example.soundstream_app.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.soundstream_app.LoginActivity
import com.example.soundstream_app.R
import com.example.soundstream_app.data.SessionManager
import com.example.soundstream_app.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val avatarOptions = listOf(R.drawable.uth, R.drawable.img1, R.drawable.img2)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUserView()
        setupActions()
    }

    override fun onResume() {
        super.onResume()
        updateUserView()
    }

    private fun setupActions() {
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            SessionManager.logout(requireContext())
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnUpgradePremium.setOnClickListener {
            upgradeToPremium()
        }
    }

    private fun updateUserView() {
        val user = SessionManager.currentUser
        val isGuest = user?.isGuest == true
        val isLoggedIn = user != null && !isGuest

        binding.imgProfileAvatar.setImageResource(user?.avatarResId ?: R.drawable.uth)
        binding.tvUserName.text = getString(R.string.settings_user_name, user?.username ?: getString(R.string.guest_name))
        binding.tvMembership.text = when {
            user?.isGuest == true -> getString(R.string.membership_guest)
            user?.isPremium == true -> getString(R.string.membership_premium)
            user != null -> getString(R.string.membership_free)
            else -> getString(R.string.membership_not_logged_in)
        }
        binding.tvArtistStatus.text = if (user?.isArtist == true) {
            getString(R.string.now_artist)
        } else {
            getString(R.string.not_artist_yet)
        }
        binding.tvPremiumHint.text = when {
            user?.isGuest == true -> getString(R.string.settings_upload_hint_guest)
            user?.isPremium == true -> getString(R.string.settings_premium_hint)
            user != null -> getString(R.string.settings_upload_hint)
            else -> getString(R.string.settings_upload_hint_guest)
        }

        binding.btnLogin.visibility = if (user == null) View.VISIBLE else View.GONE
        binding.btnLogout.visibility = if (user != null) View.VISIBLE else View.GONE
        binding.btnEditProfile.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        binding.btnChangePassword.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        binding.btnUpgradePremium.visibility = if (isLoggedIn && user?.isPremium == false) View.VISIBLE else View.GONE
    }

    private fun showEditProfileDialog() {
        val user = SessionManager.currentUser ?: return
        val displayNameInput = createTextInput(R.string.display_name_hint).apply {
            setText(user.displayName)
        }
        var selectedAvatar = user.avatarResId
        val avatarSelection = createAvatarSelector(selectedAvatar) { selectedAvatar = it }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            addView(displayNameInput)
            addView(avatarSelection)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_profile_title))
            .setView(container)
            .setPositiveButton(getString(R.string.save_button)) { _, _ ->
                val displayName = displayNameInput.text?.toString()?.trim().orEmpty()
                if (displayName.isEmpty()) {
                    showToast(getString(R.string.display_name_required))
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    val updated = SessionManager.updateProfile(requireContext(), displayName, selectedAvatar)
                    if (updated) {
                        updateUserView()
                        showToast(getString(R.string.edit_profile_success))
                    } else {
                        showToast(getString(R.string.operation_failed))
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val currentInput = createTextInput(R.string.current_password_hint, true)
        val newPasswordInput = createTextInput(R.string.new_password_hint, true)
        val confirmPasswordInput = createTextInput(R.string.confirm_password_hint, true)

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            addView(currentInput)
            addView(newPasswordInput)
            addView(confirmPasswordInput)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_password_title))
            .setView(container)
            .setPositiveButton(getString(R.string.change_password_button)) { _, _ ->
                val currentPassword = currentInput.text?.toString().orEmpty()
                val newPassword = newPasswordInput.text?.toString().orEmpty()
                val confirmPassword = confirmPasswordInput.text?.toString().orEmpty()

                when {
                    currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() ->
                        showToast(getString(R.string.change_password_fill_all))
                    newPassword != confirmPassword ->
                        showToast(getString(R.string.password_confirm_mismatch))
                    else -> lifecycleScope.launch {
                        val changed = SessionManager.changePassword(requireContext(), currentPassword, newPassword)
                        if (changed) {
                            showToast(getString(R.string.password_change_success))
                        } else {
                            showToast(getString(R.string.password_change_failed))
                        }
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun upgradeToPremium() {
        lifecycleScope.launch {
            val upgraded = SessionManager.upgradeToPremium(requireContext())
            if (upgraded) {
                updateUserView()
                showToast(getString(R.string.premium_upgrade_success))
            } else {
                showToast(getString(R.string.operation_failed))
            }
        }
    }

    private fun createAvatarSelector(initialAvatar: Int, onAvatarSelected: (Int) -> Unit): LinearLayout {
        var selectedAvatar = initialAvatar
        val avatarViews = mutableListOf<ImageView>()
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 0)
        }

        avatarOptions.forEach { resId ->
            val avatarView = ImageView(requireContext()).apply {
                setImageResource(resId)
                scaleType = ImageView.ScaleType.CENTER_CROP
                adjustViewBounds = true
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = 12
                }
                setPadding(8, 8, 8, 8)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_avatar_circle)
                alpha = if (resId == selectedAvatar) 1f else 0.45f
                tag = resId
                setOnClickListener {
                    selectedAvatar = resId
                    avatarViews.forEach { view ->
                        view.alpha = if (view.tag == resId) 1f else 0.45f
                    }
                    onAvatarSelected(resId)
                }
            }
            avatarViews.add(avatarView)
            container.addView(avatarView)
        }

        return container
    }

    private fun createTextInput(hintRes: Int, isPassword: Boolean = false): EditText {
        return EditText(requireContext()).apply {
            hint = getString(hintRes)
            setSingleLine()
            inputType = if (isPassword) android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD else android.text.InputType.TYPE_CLASS_TEXT
            setPadding(18, 18, 18, 18)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 12
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
