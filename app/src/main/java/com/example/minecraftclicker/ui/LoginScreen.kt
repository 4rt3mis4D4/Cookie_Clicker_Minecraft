package com.example.minecraftclicker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minecraftclicker.ui.theme.MinecraftClickerTheme
import com.example.minecraftclicker.viewmodel.AuthUiState
import com.example.minecraftclicker.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    onCreateAccount: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        uiState = uiState,
        modifier = modifier,
        onNameChange = viewModel::updateName,
        onPasswordChange = viewModel::updatePassword,
        onSignIn = viewModel::signIn,
        onCreateAccount = {
            viewModel.clearForm()
            onCreateAccount()
        }
    )
}

@Composable
private fun LoginContent(
    uiState: AuthUiState,
    modifier: Modifier = Modifier,
    onNameChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onSignIn: () -> Unit = {},
    onCreateAccount: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Minecraft Clicker",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Entre para começar a minerar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val fieldModifier = Modifier.fillMaxWidth().widthIn(max = 480.dp)

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Usuário") },
            singleLine = true,
            isError = uiState.nameInvalid,
            supportingText = { if (uiState.nameInvalid) Text("Informe pelo menos 3 caracteres") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = fieldModifier
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Senha") },
            singleLine = true,
            isError = uiState.passwordInvalid,
            supportingText = { if (uiState.passwordInvalid) Text("Informe pelo menos 4 caracteres") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSignIn() }),
            modifier = fieldModifier
        )

        uiState.errorMessage?.let { errorMessage ->
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Button(onClick = onSignIn, modifier = fieldModifier) {
            Text("Entrar")
        }
        TextButton(onClick = onCreateAccount) {
            Text("Não tem conta? Criar conta")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    MinecraftClickerTheme {
        LoginContent(uiState = AuthUiState(name = "steve", loading = false))
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginErrorPreview() {
    MinecraftClickerTheme {
        LoginContent(
            uiState = AuthUiState(
                name = "steve",
                password = "12",
                submitAttempted = true,
                errorMessage = "Usuário ou senha inválidos",
                loading = false
            )
        )
    }
}
