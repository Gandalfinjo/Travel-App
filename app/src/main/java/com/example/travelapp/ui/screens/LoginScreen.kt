package com.example.travelapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travelapp.R
import com.example.travelapp.ui.viewmodels.AuthViewModel
import com.example.travelapp.ui.theme.TravelAppTheme

/**
 * Screen for user login with username and password.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onRegisterClick: () -> Unit,
    onLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                ),
                shadow = Shadow(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    offset = Offset(0f, 4f),
                    blurRadius = 8f
                )
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(R.string.login),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                letterSpacing = 3.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.15f),
                    offset = Offset(0f, 2f),
                    blurRadius = 4f
                )
            ),
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = stringResource(R.string.username)) }
        )

        Spacer(modifier = Modifier.padding(4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
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
            }
        )

        Spacer(modifier = Modifier.padding(4.dp))

        Button(
            onClick = { authViewModel.login(username, password) },
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
                Text(text = stringResource(R.string.login))
            }
        }

        Spacer(modifier = Modifier.padding(4.dp))

        TextButton(onClick = onRegisterClick) {
            Text(text = stringResource(R.string.don_t_have_an_account_register))
        }
    }

    LaunchedEffect(uiState.loggedInUser, uiState.errorMessage) {
        when {
            uiState.loggedInUser != null -> {
                onLogin()
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
            title = { Text(text = stringResource(R.string.login)) },
            text = { Text(alertMessage) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    TravelAppTheme {
        LoginScreen(
            onRegisterClick = {},
            onLogin = {}
        )
    }
}