package com.example.minecraftclicker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.minecraftclicker.MinecraftClickerApplication
import com.example.minecraftclicker.model.Tab
import com.example.minecraftclicker.viewmodel.AppViewModelFactory
import com.example.minecraftclicker.viewmodel.AuthUiState
import com.example.minecraftclicker.viewmodel.AuthViewModel

@Composable
fun App() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as MinecraftClickerApplication
    val factory = remember(app) { AppViewModelFactory(app) }

    val authViewModel: AuthViewModel = viewModel(factory = factory)

    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(authUiState.isLoggedIn, currentRoute) {
        val route = currentRoute ?: return@LaunchedEffect
        val onPublicScreen = route in Routes.publicRoutes
        if (authUiState.isLoggedIn && onPublicScreen) {
            navController.navigate(Tab.CLICKER.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        } else if (!authUiState.isLoggedIn && !onPublicScreen) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val showBars = authUiState.isLoggedIn && currentRoute in Routes.tabRoutes

    Scaffold(
        topBar = {
            if (showBars) {
                AppTopBar(uiState = authUiState, onLogout = authViewModel::logout)
            }
        },
        bottomBar = {
            if (showBars) {
                AppBottomBar(
                    tabs = authUiState.visibleTabs,
                    currentRoute = currentRoute,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.fillMaxSize().padding(innerPadding)

        NavHost(navController = navController, startDestination = Routes.LOGIN) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    modifier = contentModifier,
                    onCreateAccount = { navController.navigate(Routes.SIGN_UP) }
                )
            }
            composable(Routes.SIGN_UP) {
                SignUpScreen(
                    viewModel = authViewModel,
                    modifier = contentModifier,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Tab.CLICKER.route) { ClickerScreen(modifier = contentModifier) }
            composable(Tab.UPGRADES.route) { UpgradesScreen(modifier = contentModifier) }
            composable(Tab.RANKING.route) { RankingScreen(modifier = contentModifier) }
            composable(Tab.STATS.route) { StatsScreen(modifier = contentModifier) }
            composable(Tab.ADMIN.route) {
                if (authUiState.isAdmin) {
                    AdminScreen(modifier = contentModifier)
                } else {
                    AccessDenied(modifier = contentModifier)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(uiState: AuthUiState, onLogout: () -> Unit) {
    TopAppBar(
        title = {
            val roleLabel = if (uiState.isAdmin) " (Admin)" else ""
            Text("Olá, ${uiState.currentPlayer?.name.orEmpty()}$roleLabel")
        },
        actions = {
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun AppBottomBar(
    tabs: List<Tab>,
    currentRoute: String?,
    navController: NavHostController
) {
    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(Tab.CLICKER.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.iconVector(), contentDescription = null) },
                label = { Text(tab.label) }
            )
        }
    }
}

private fun Tab.iconVector(): ImageVector = when (this) {
    Tab.CLICKER -> Icons.Default.Build
    Tab.UPGRADES -> Icons.Default.ShoppingCart
    Tab.RANKING -> Icons.Default.Star
    Tab.STATS -> Icons.AutoMirrored.Filled.List
    Tab.ADMIN -> Icons.Default.Settings
}

@Composable
private fun AccessDenied(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text("Acesso negado: área restrita ao Admin", color = MaterialTheme.colorScheme.error)
    }
}
