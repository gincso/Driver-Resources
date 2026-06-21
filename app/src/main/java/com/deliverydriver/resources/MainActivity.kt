package com.deliverydriver.resources

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.deliverydriver.resources.ui.screens.*
import com.deliverydriver.resources.ui.theme.DeliveryDriverTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeliveryDriverTheme {
                DriverApp()
            }
        }
    }
}

// ── Navigation ───────────────────────────────────────────────────

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> androidx.compose.ui.graphics.vector.ImageVector) {
    data object Dashboard : Screen("dashboard", "Home", { Icons.Filled.Home })
    data object DeliveryGuide : Screen("delivery_guide", "Guide", { Icons.Filled.MenuBook })
    data object Safety : Screen("safety", "Safety", { Icons.Filled.Shield })
    data object RouteTools : Screen("route_tools", "Tools", { Icons.Filled.Handyman })
    data object ReferenceHub : Screen("reference_hub", "Reference", { Icons.Filled.Book })
    data object Settings : Screen("settings", "Settings", { Icons.Filled.Settings })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverApp() {
    val navController = rememberNavController()

    val screens = listOf(
        Screen.Dashboard,
        Screen.DeliveryGuide,
        Screen.Safety,
        Screen.RouteTools,
        Screen.ReferenceHub,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                val visibleScreens = screens.take(5) // Show 5 in bottom bar
                visibleScreens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon(),
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToCategory = { category ->
                        when (category) {
                            "tools" -> navController.navigate(Screen.RouteTools.route)
                            "safety" -> navController.navigate(Screen.Safety.route)
                            else -> navController.navigate(Screen.DeliveryGuide.route)
                        }
                    },
                    onNavigateToSafety = {
                        navController.navigate(Screen.Safety.route)
                    }
                )
            }
            composable(Screen.DeliveryGuide.route) {
                DeliveryGuideScreen()
            }
            composable(Screen.Safety.route) {
                SafetyScreen()
            }
            composable(Screen.RouteTools.route) {
                RouteToolsScreen()
            }
            composable(Screen.ReferenceHub.route) {
                ReferenceHubScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
