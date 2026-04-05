package com.example.travelapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.repositories.TripRepository
import com.example.travelapp.database.repositories.UserRepository
import com.example.travelapp.session.SessionManager
import com.example.travelapp.session.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: String? = null,
    val loggedInUserId: Int? = null,
    val isSessionChecked: Boolean = false
)

/**
 * ViewModel for user authentication (login and registration).
 *
 * Manages authentication state and coordinates with UserRepository
 * for user operations. Automatically logs in user after successful registration.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tripRepository: TripRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val themePreference: StateFlow<ThemePreference> = sessionManager.themePreference
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePreference.SYSTEM)

    init {
        viewModelScope.launch {
            sessionManager.loggedInUsername.collect { username ->
                val userId = sessionManager.loggedInUserId.first()
                _uiState.update {
                    it.copy(
                        loggedInUser = username,
                        loggedInUserId = userId,
                        isSessionChecked = true
                    )
                }

                return@collect
            }
        }
    }

    /**
     * Registers a new user and automatically logs them in.
     *
     * Validates that username and email are unique before creating the account.
     * On success, user is logged in automatically.
     *
     * @param firstname User's first name
     * @param lastname User's last name
     * @param email User's email address
     * @param username Desired username (must be unique)
     * @param password Plain text password (will be hashed)
     */
    fun register(firstname: String, lastname: String, email: String, username: String, password: String) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            val existingUsername = userRepository.getUserByUsername(username)
            val existingEmail = userRepository.getUserByEmail(email)

            when {
                existingUsername != null -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Username already exists"
                        )
                    }
                }
                existingEmail != null -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Email is already in use"
                        )
                    }
                }
                else -> {
                    userRepository.register(firstname, lastname, email, username, password)

                    val loggedInUser = userRepository.login(username, password)

                    if (loggedInUser != null) {
                        sessionManager.saveSession(loggedInUser.username, loggedInUser.id)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loggedInUser = loggedInUser.username,
                                loggedInUserId = loggedInUser.id
                            )
                        }
                    }
                    else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to login after registration"
                            )
                        }
                    }

                }
            }
        }
        catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * Authenticates a user with username and password.
     *
     * @param username Username to authenticate
     * @param password Plain text password (will be hashed for comparison)
     */
    fun login(username: String, password: String) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val user = userRepository.login(username, password)

        if (user != null) {
            sessionManager.saveSession(user.username, user.id)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    loggedInUser = user.username,
                    loggedInUserId = user.id
                )
            }
        }
        else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Invalid username or password"
                )
            }
        }
    }

    /**
     * Logs out the current user by clearing authentication state.
     */
    fun logout() = viewModelScope.launch {
        sessionManager.clearSession()

        _uiState.update {
            it.copy(
                loggedInUser = null,
                loggedInUserId = null,
                errorMessage = null
            )
        }
    }

    /**
     * Sets the selected theme preference and saves it to the DataStore
     *
     * @param theme Selected theme to be set
     */
    fun setThemePreference(theme: ThemePreference) = viewModelScope.launch {
        sessionManager.saveThemePreference(theme)
    }

    /**
     * Sets a custom error message in the UI state.
     *
     * @param errorMessage Error message to display
     */
    fun setErrorMessage(errorMessage: String) {
        _uiState.update {
            it.copy(errorMessage = errorMessage)
        }
    }

    /**
     * Clears any error message from the UI state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun syncTripStatuses(userId: Int) = viewModelScope.launch {
        tripRepository.syncTripStatuses(userId)
    }
}