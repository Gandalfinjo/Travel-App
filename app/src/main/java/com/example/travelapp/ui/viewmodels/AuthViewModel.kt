package com.example.travelapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.travelapp.database.repositories.TripRepository
import com.example.travelapp.database.repositories.UserRepository
import com.example.travelapp.session.SessionManager
import com.example.travelapp.session.ThemePreference
import com.example.travelapp.worker.CurrencySyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val loggedInUserFirstname: String? = null,
    val loggedInUserLastname: String? = null,
    val loggedInUserProfilePicture: String? = null,
    val isSessionChecked: Boolean = false,
    val isLoggedIn: Boolean = false
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
    private val sessionManager: SessionManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val themePreference: StateFlow<ThemePreference> = sessionManager.themePreference
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePreference.SYSTEM)

    val defaultCurrency: StateFlow<String> = sessionManager.defaultCurrency
        .stateIn(viewModelScope, SharingStarted.Eagerly, "EUR")

    init {
        viewModelScope.launch {
            sessionManager.loggedInUsername.collect { username ->
                val userId = sessionManager.loggedInUserId.first()
                val firstname = sessionManager.loggedInUserFirstname.first()
                val lastname = sessionManager.loggedInUserLastname.first()
                val profilePicture = sessionManager.loggedInUserProfilePicture.first()

                _uiState.update {
                    it.copy(
                        loggedInUser = username,
                        loggedInUserId = userId,
                        loggedInUserFirstname = firstname,
                        loggedInUserLastname = lastname,
                        loggedInUserProfilePicture = profilePicture,
                        isSessionChecked = true,
                        isLoggedIn = username != null
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
                        sessionManager.saveSession(loggedInUser.username, loggedInUser.id, loggedInUser.firstname, loggedInUser.lastname)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loggedInUser = loggedInUser.username,
                                loggedInUserFirstname = loggedInUser.firstname,
                                loggedInUserLastname = loggedInUser.lastname,
                                loggedInUserId = loggedInUser.id,
                                isLoggedIn = true
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
            sessionManager.saveSession(user.username, user.id, user.firstname, user.lastname)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    loggedInUser = user.username,
                    loggedInUserFirstname = user.firstname,
                    loggedInUserLastname = user.lastname,
                    loggedInUserId = user.id,
                    isLoggedIn = true
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
     * Updates the profile picture for the logged-in user
     *
     * @param path Path to the profile picture (null if removing the profile picture)
     */
    fun updateProfilePicture(path: String?) {
        _uiState.update { it.copy(loggedInUserProfilePicture = path) }
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
                errorMessage = null,
                isLoggedIn = false
            )
        }
    }

    /**
     * Sets the selected currency as default and saves it to the DataStore
     *
     * @param currency Selected currency to be set as default
     */
    fun setDefaultCurrency(currency: String) = viewModelScope.launch {
        sessionManager.saveDefaultCurrency(currency)

        val syncRequest = OneTimeWorkRequestBuilder<CurrencySyncWorker>()
            .setInputData(workDataOf("NEW_CURRENCY" to currency))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Needs internet for rates
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "currency_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
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