package com.example.apiconnection.UI.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.apiconnection.Network.RetrofitClient
import com.example.apiconnection.Network.TokenManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val ctx = LocalContext.current
    val api = remember { RetrofitClient.api(ctx) }
    val tm = remember { TokenManager(ctx) }
    val scope = rememberCoroutineScope()
    val scaffold = rememberScaffoldState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Scaffold(scaffoldState = scaffold) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(text = "Iniciar sesión", style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(
                enabled = !loading,
                onClick = {
                    scope.launch {
                        try {
                            loading = true
                            val res = api.login(mapOf("email" to email, "password" to password))
                            tm.save(res.token)
                            scaffold.snackbarHostState.showSnackbar("¡Bienvenido ${res.user.name}!")
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            scaffold.snackbarHostState.showSnackbar("Error: ${e.message}")
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "Entrando..." else "Entrar") }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("register") }) { Text("Crear cuenta") }
        }
    }
}
