package com.example.apiconnection.UI.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.apiconnection.Network.TokenManager

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val tm = remember { TokenManager(context) }

    // Si no hay token → ir a login
    var startDest by remember { mutableStateOf(if (tm.hasToken()) "home" else "login") }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val selectedItem = when (currentRoute) {
        "home" -> 0
        "notifications" -> 1
        "settings" -> 2
        "profile" -> 3
        else -> -1
    }

    Scaffold(
        floatingActionButton = {
            if (currentRoute != null && !currentRoute.startsWith("edit/") && currentRoute != "login" && currentRoute != "register") {
                FloatingActionButton(
                    onClick = { navController.navigate("create") },
                    backgroundColor = Color(0xFF1976D2),
                    contentColor = Color.White
                ) { Icon(Icons.Default.Add, contentDescription = "Agregar") }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        bottomBar = {
            if (currentRoute != null && !currentRoute.startsWith("edit/")
                && currentRoute != "login" && currentRoute != "register"
            ) {
                Card(
                    elevation = 12.dp,
                    backgroundColor = Color.White,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        Modifier
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BottomNavIcon(icon = Icons.Default.Home, selected = selectedItem == 0) {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        BottomNavIcon(icon = Icons.Default.Notifications, selected = selectedItem == 1) {
                            navController.navigate("notifications") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        Spacer(modifier = Modifier.width(48.dp))
                        BottomNavIcon(icon = Icons.Default.Settings, selected = selectedItem == 2) {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        BottomNavIcon(icon = Icons.Default.Person, selected = selectedItem == 3) {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = startDest) {
                composable("login") { LoginScreen(navController) }
                composable("register") { RegisterScreen(navController) }

                composable("home") { HomePage(navController) }
                composable("notifications") { NotificationScreen(navController) }
                composable("settings") { SettingsScreen(navController) }
                composable("profile") { ProfileScreen(navController) }
                composable("create") { CreateArticleScreen(navController) }
                composable(
                    "edit/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    val articleId = backStackEntry.arguments?.getInt("id") ?: return@composable
                    EditArticleScreen(navController, articleId)
                }
                composable("show/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                    id?.let { ShowArticleScreen(navController, it) }
                }
            }
        }
    }
}

@Composable
fun BottomNavIcon(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color(0xFF1976D2) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
    }
}
