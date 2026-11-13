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
fun SignUpScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SignUpContent(
        uiState = uiState,
        modifier = modifier,
        onNameChange = viewModel::updateName,
        onPasswordChange = viewModel::updatePassword,
        onConfirmPasswordChange = viewModel::updateConfirmPassword,
        onSignUp = viewModel::signUp,
        onBack = {
            viewModel.clearForm()
            onBack()
        }
    )
}

@Composable
private fun SignUpContent(
    uiState: AuthUiState,
    modifier: Modifier = Modifier,
    onNameChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onSignUp: () -> Unit = {},
    onBack: () -> Unit = {}
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
            "Criar conta",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Novas contas entram como Jogador",
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
                imeAction = ImeAction.Next
            ),
            modifier = fieldModifier
        )
        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirmar senha") },
            singleLine = true,
            isError = uiState.passwordsMismatch,
            supportingText = { if (uiState.passwordsMismatch) Text("As senhas não conferem") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSignUp() }),
            modifier = fieldModifier
        )

        uiState.errorMessage?.let { errorMessage ->
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Button(onClick = onSignUp, enabled = !uiState.submitting, modifier = fieldModifier) {
            Text(if (uiState.submitting) "Criando..." else "Criar conta")
        }
        TextButton(onClick = onBack) {
            Text("Já tenho conta")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpPreview() {
    MinecraftClickerTheme {
        SignUpContent(
            uiState = AuthUiState(
                name = "alex",
                password = "1234",
                confirmPassword = "123",
                submitAttempted = true,
                loading = false
            )
        )
    }
}
