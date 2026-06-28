package com.deliverydriver.resources

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.deliverydriver.resources.scanner.RouteViewModel
import com.deliverydriver.resources.scanner.ScannerScreen
import com.deliverydriver.resources.scanner.ScanResultsScreen
import com.deliverydriver.resources.ui.screens.*
import com.deliverydriver.resources.ui.theme.DeliveryDriverTheme
import com.deliverydriver.resources.updater.UpdateInfo
import com.deliverydriver.resources.updater.UpdateManager
import com.deliverydriver.resources.updater.checkForUpdate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RouteViewModel.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            DeliveryDriverTheme {
                DriverApp()
            }
        }
    }
}

// ── Navigation Routes ────────────────────────────────────────────

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Scanner : Screen("scanner", "Scanner", Icons.Filled.QrCodeScanner)
    data object ScanResults : Screen("scan_results", "Results", Icons.Filled.Sort)
    data object Dashboard : Screen("dashboard", "Home", Icons.Filled.Home)
    data object DeliveryGuide : Screen("delivery_guide", "Guide", Icons.Filled.MenuBook)
    data object Safety : Screen("safety", "Safety", Icons.Filled.Shield)
    data object RouteTools : Screen("route_tools", "Tools", Icons.Filled.Handyman)
    data object ReferenceHub : Screen("reference_hub", "Reference", Icons.Filled.Book)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val updateManager = remember { UpdateManager(context) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val info = checkForUpdate(context)
        if (info.available) {
            updateInfo = info
        }
    }

    val navController = rememberNavController()

    // Bottom nav screens (max 5)
    val bottomNavScreens = listOf(
        Screen.Scanner,
        Screen.Dashboard,
        Screen.DeliveryGuide,
        Screen.Safety,
        Screen.ReferenceHub
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on results screen
    val showBottomBar = currentDestination?.route != Screen.ScanResults.route &&
            currentDestination?.route != Screen.RouteTools.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    bottomNavScreens.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Scanner.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Scanner - main feature
            composable(Screen.Scanner.route) {
                ScannerScreen(
                    onNavigateToResults = {
                        navController.navigate(Screen.ScanResults.route)
                    },
                    onNavigateToResources = {
                        navController.navigate(Screen.Dashboard.route)
                    }
                )
            }

            // Scan Results / Organization
            composable(Screen.ScanResults.route) {
                ScanResultsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToScanner = {
                        navController.popBackStack(Screen.Scanner.route, false)
                    }
                )
            }

            // Existing screens
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
        }
    }

    updateInfo?.let { info ->
        var showDialog by remember { mutableStateOf(true) }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                icon = {
                    Icon(Icons.Filled.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                title = { Text("Update Available") },
                text = {
                    Column {
                        Text("Version ${info.latestVersion} is ready to install.")
                        if (info.releaseNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = info.releaseNotes,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        updateManager.downloadAndInstall(
                            downloadUrl = info.downloadUrl,
                            versionName = info.latestVersion
                        )
                    }) {
                        Text("Update & Install")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Later")
                    }
                }
            )
        }
    }
}
