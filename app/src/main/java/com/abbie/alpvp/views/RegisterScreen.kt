package com.abbie.alpvp.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abbie.alpvp.viewmodels.AppViewModelProvider
import com.abbie.alpvp.viewmodels.AuthUiState
import com.abbie.alpvp.viewmodels.AuthViewModel

private val PrimaryGreen = Color(0xFF66A678)
private val AppBgColor = Color(0xFFF5F7F5)
private val TextPrimary = Color(0xFF1A1C19)
private val TextSecondary = Color(0xFF757575)

@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var showTutorialDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.RegisterSuccess -> showTutorialDialog = true
            is AuthUiState.Error -> errorMessage = (uiState as AuthUiState.Error).msg
            else -> {}
        }
    }

    Scaffold(containerColor = AppBgColor) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Join Us!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Create your account to start tracking",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = PrimaryGreen,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = PrimaryGreen,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = PrimaryGreen,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    val image = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle Password", tint = TextSecondary)
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    errorMessage = null
                    viewModel.register(username, email, password)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Register", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = TextSecondary)
                Text(
                    text = "Login",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }

        if (showTutorialDialog) {
            AlertDialog(
                onDismissRequest = {
                    showTutorialDialog = false
                    onNavigateToHome()
                },
                containerColor = Color.White,
                icon = { Icon(Icons.Default.Celebration, contentDescription = null, tint = PrimaryGreen) },
                title = { Text("Welcome to DailyStep!", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Your account is ready.\n\n1. Add your daily activities\n2. Track your progress\n3. Build a streak!", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            showTutorialDialog = false
                            onNavigateToHome()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Let's Go!")
                    }
                }
            )
        }
    }
}