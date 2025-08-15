package com.example.apiconnection.UI.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.apiconnection.MODELS.UserDto
import com.example.apiconnection.Network.RetrofitClient
import com.example.apiconnection.Network.TokenManager
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {
    val ctx = LocalContext.current
    val api = remember { RetrofitClient.api(ctx) }
    val tm = remember { TokenManager(ctx) }

    val scope = rememberCoroutineScope()
    val scaffold = rememberScaffoldState()

    var me by remember { mutableStateOf<UserDto?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    // Cargar datos del usuario (opcional)
    LaunchedEffect(Unit) {
        try {
            me = api.me()
        } catch (_: Exception) {
            // Si falla, igual podrás cerrar sesión.
        }
    }

    Scaffold(scaffoldState = scaffold, topBar = {
        TopAppBar(title = { Text("Perfil") })
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = me?.name ?: "Usuario", style = MaterialTheme.typography.h6)
            Text(text = me?.email ?: "—", style = MaterialTheme.typography.body2)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { showConfirm = true },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (loading) "Cerrando sesión..." else "Cerrar sesión")
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { if (!loading) showConfirm = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que deseas salir de tu cuenta?") },
            confirmButton = {
                TextButton(
                    enabled = !loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            try {
                                // intenta cerrar sesión en el backend (si el token ya expiró, ignoramos el error)
                                runCatching { api.logout() }
                                // limpia token local SIEMPRE
                                tm.clear()
                                // navega a Login y limpia el back stack
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                // si algo falla, igual limpiamos token y mandamos a login
                                tm.clear()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } finally {
                                loading = false
                                showConfirm = false
                            }
                        }
                    }
                ) { Text("Sí, salir") }
            },
            dismissButton = {
                TextButton(
                    enabled = !loading,
                    onClick = { showConfirm = false }
                ) { Text("Cancelar") }
            }
        )
    }
}
