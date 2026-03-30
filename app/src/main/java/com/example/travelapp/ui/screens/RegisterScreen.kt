package com.example.travelapp.ui.screens

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
 * Screen for user registration with form validation.
 */
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onRegister: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current

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
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(48.dp))
        
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    BorderStroke(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    ), RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.create_your_account),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))
        
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.personal_info),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.first_name),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        OutlinedTextField(
                            value = firstname,
                            onValueChange = { firstname = it },
                            placeholder = { Text(text = stringResource(R.string.first_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.last_name),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        OutlinedTextField(
                            value = lastname,
                            onValueChange = { lastname = it },
                            placeholder = { Text(text = stringResource(R.string.last_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.email),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = if (it.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(it).matches())
                                context.getString(R.string.invalid_email_format) else null
                        },
                        placeholder = { Text(text = stringResource(R.string.you_example_com)) },
                        singleLine = true,
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(text = it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.account_info),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.username),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text(text = stringResource(R.string.choose_a_username)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.password),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = if (it.isNotEmpty() && it.length < 6)
                                context.getString(R.string.password_must_be_at_least_6_characters) else null
                            confirmPasswordError = if (confirmPassword.isNotEmpty() && confirmPassword != it)
                                context.getString(R.string.passwords_do_not_match) else null
                        },
                        placeholder = { Text(text = stringResource(R.string.at_least_6_characters)) },
                        singleLine = true,
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(text = it) } },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.confirm_password),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = if (it != password)
                                context.getString(R.string.passwords_do_not_match) else null
                        },
                        placeholder = { Text(text = stringResource(R.string.repeat_your_password)) },
                        singleLine = true,
                        isError = confirmPasswordError != null,
                        supportingText = confirmPasswordError?.let { { Text(text = it) } },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                when {
                    firstname.isBlank() || lastname.isBlank() || email.isBlank() ||
                            username.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                        authViewModel.setErrorMessage(context.getString(R.string.missing_fields))
                    password != confirmPassword -> {
                        confirmPasswordError = context.getString(R.string.passwords_do_not_match)
                        authViewModel.setErrorMessage(context.getString(R.string.passwords_do_not_match))
                    }
                    else -> authViewModel.register(firstname, lastname, email, username, password)
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            }
            else {
                Text(
                    text = stringResource(R.string.register),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onLoginClick) {
            Text(
                text = stringResource(R.string.already_have_an_account_login),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(48.dp))
    }

    LaunchedEffect(uiState.loggedInUser, uiState.errorMessage) {
        when {
            uiState.loggedInUser != null -> onRegister()
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
                TextButton(onClick = {
                    showAlert = false
                    authViewModel.clearError()
                }) { Text(text = stringResource(R.string.ok)) }
            },
            title = { Text(text = stringResource(R.string.registration)) },
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