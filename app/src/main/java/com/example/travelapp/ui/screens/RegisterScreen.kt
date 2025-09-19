package com.example.travelapp.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travelapp.R
import com.example.travelapp.ui.stateholders.AuthViewModel
import com.example.travelapp.ui.theme.TravelAppTheme

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onRegister: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }

    var username by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(R.string.register),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = firstname,
            onValueChange = { firstname = it },
            label = { Text(text = stringResource(R.string.first_name)) }
        )

        Spacer(modifier = Modifier.padding(4.dp))

        OutlinedTextField(
            value = lastname,
            onValueChange = { lastname = it },
            label = { Text(text = stringResource(R.string.last_name)) }
        )

        Spacer(modifier = Modifier.padding(4.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = if (it.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                    "Invalid email format"
                } else null
            },
            label = { Text(text = stringResource(R.string.email)) },
            isError = emailError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        emailError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        Spacer(modifier = Modifier.padding(4.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = stringResource(R.string.username)) }
        )

        Spacer(modifier = Modifier.padding(4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = if (it.isNotEmpty() && it.length < 6) {
                    "Password must be at least 6 characters"
                } else null
                confirmPasswordError = if (confirmPassword.isNotEmpty() && confirmPassword != it) {
                    "Passwords do not match"
                } else null
            },
            label = { Text(text = stringResource(R.string.password)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff

                IconButton(
                    onClick = { passwordVisible = !passwordVisible }
                ) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                    )
                }
            },
            isError = passwordError != null
        )

        passwordError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        Spacer(modifier = Modifier.padding(4.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = if (it != password) {
                    "Passwords do not match"
                } else null
            },
            label = { Text(text = stringResource(R.string.confirm_password)) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff

                IconButton(
                    onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                ) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (confirmPasswordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                    )
                }
            },
            isError = confirmPasswordError != null
        )

        confirmPasswordError?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        Spacer(modifier = Modifier.padding(4.dp))

        Button(
            onClick = {
                if (firstname == "" || lastname == "" || email == "" || username == "" || password == "" || confirmPassword == "") {
                    authViewModel.setErrorMessage("Missing fields")
                }
                else if (password != confirmPassword) {
                    confirmPasswordError = "Passwords do not match"
                    authViewModel.setErrorMessage("Passwords do not match")
                }
                else {
                    authViewModel.register(firstname, lastname, email, username, password)
                }
            },
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
            else {
                Text(text = stringResource(R.string.register))
            }
        }

        Spacer(modifier = Modifier.padding(4.dp))

        TextButton(onClick = onLoginClick) {
            Text(text = stringResource(R.string.already_have_an_account_login))
        }
    }

    LaunchedEffect(uiState.loggedInUser, uiState.errorMessage) {
        when {
            uiState.loggedInUser != null -> {
                onRegister()
            }
            uiState.errorMessage != null -> {
                alertMessage = uiState.errorMessage!!
                showAlert = true
            }
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = {
                showAlert = false
                authViewModel.clearError()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAlert = false
                        authViewModel.clearError()
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            title = { Text(text = stringResource(R.string.registration))},
            text = { Text(alertMessage) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    TravelAppTheme {
        RegisterScreen(
            onLoginClick = {},
            onRegister = {}
        )
    }
}