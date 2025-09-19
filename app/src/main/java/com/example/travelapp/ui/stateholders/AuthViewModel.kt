package com.example.travelapp.ui.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: String? = null,
    val loggedInUserId: Int? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

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

    fun login(username: String, password: String) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val user = userRepository.login(username, password)

        if (user != null) {
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

    fun logout() {
        _uiState.update {
            it.copy(
                loggedInUser = null,
                errorMessage = null
            )
        }
    }

    fun setErrorMessage(errorMessage: String) {
        _uiState.update {
            it.copy(errorMessage = errorMessage)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}