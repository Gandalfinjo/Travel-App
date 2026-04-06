package com.example.travelapp.ui.viewmodels

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.R
import com.example.travelapp.database.repositories.TripRepository
import com.example.travelapp.database.repositories.UserRepository
import com.example.travelapp.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class ProfileUiState(
    val isLoading: Boolean = true,
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val username: String = "",
    val profilePicturePath: String? = null,
    val totalTrips: Int = 0,
    val uniqueDestinations: Int = 0,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tripRepository: TripRepository,
    private val sessionManager: SessionManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = sessionManager.loggedInUserId.first() ?: return@launch
            val user = userRepository.getUserById(userId)
            val trips = tripRepository.getUserTrips(userId).first()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    firstname = user?.firstname ?: "",
                    lastname = user?.lastname ?: "",
                    email = user?.email ?: "",
                    username = user?.username ?: "",
                    profilePicturePath = user?.profilePicturePath,
                    totalTrips = trips.size,
                    uniqueDestinations = trips.map { t -> t.location }.toSet().size
                )
            }
        }
    }

    fun updateName(firstname: String, lastname: String) = viewModelScope.launch {
        val userId = sessionManager.loggedInUserId.first() ?: return@launch

        if (firstname.isBlank() || lastname.isBlank()) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.name_fields_cannot_be_empty)) }
            return@launch
        }

        userRepository.updateName(userId, firstname, lastname)

        sessionManager.saveSession(
            username = _uiState.value.username,
            userId = userId,
            firstname = firstname,
            lastname = lastname
        )

        _uiState.update {
            it.copy(
                firstname = firstname,
                lastname = lastname,
                successMessage = context.getString(R.string.name_updated_successfully)
            )
        }
    }

    fun updateEmail(email: String) = viewModelScope.launch {
        val userId = sessionManager.loggedInUserId.first() ?: return@launch

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.invalid_email_address)) }
            return@launch
        }

        val existing = userRepository.getUserByEmail(email)

        if (existing != null && existing.id != userId) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.email_is_already_in_use)) }
            return@launch
        }

        userRepository.updateEmail(userId, email)

        _uiState.update {
            it.copy(
                email = email,
                successMessage = context.getString(R.string.email_updated_successfully)
            )
        }
    }

    fun updateUsername(username: String) = viewModelScope.launch {
        val userId = sessionManager.loggedInUserId.first() ?: return@launch

        if (username.isBlank()) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.username_cannot_be_empty)) }
            return@launch
        }

        val existing = userRepository.getUserByUsername(username)

        if (existing != null && existing.id != userId) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.username_is_already_taken)) }
            return@launch
        }

        userRepository.updateUsername(userId, username)

        sessionManager.saveSession(
            username = username,
            userId = userId,
            firstname = _uiState.value.firstname,
            lastname = _uiState.value.lastname
        )

        _uiState.update {
            it.copy(
                username = username,
                successMessage = context.getString(R.string.username_updated_successfully)
            )
        }
    }

    fun updatePassword(current: String, new: String, confirm: String) = viewModelScope.launch {
        val userId = sessionManager.loggedInUserId.first() ?: return@launch

        if (new.length < 6) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.password_must_be_at_least_6_characters)) }
            return@launch
        }

        if (new != confirm) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.passwords_do_not_match)) }
            return@launch
        }

        val user = userRepository.getUserById(userId)

        val hashedCurrent = MessageDigest.getInstance("SHA-256")
            .digest(current.toByteArray())
            .joinToString("") { "%02x".format(it) }

        if (user?.password != hashedCurrent) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.current_password_is_incorrect)) }
            return@launch
        }

        userRepository.updatePassword(userId, new)

        _uiState.update { it.copy(successMessage = context.getString(R.string.password_updated_successfully)) }
    }

    fun updateProfilePicture(path: String?) = viewModelScope.launch {
        val userId = sessionManager.loggedInUserId.first() ?: return@launch
        userRepository.updateProfilePicture(userId, path)
        sessionManager.saveSession(
            username = _uiState.value.username,
            userId = userId,
            firstname = _uiState.value.firstname,
            lastname = _uiState.value.lastname,
            profilePicturePath = path
        )
        _uiState.update { it.copy(profilePicturePath = path) }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }
}