package com.habitik.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navDeepLink
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.habitik.ui.screens.builder.RoutineBuilderScreen
import com.habitik.ui.screens.dashboard.DashboardScreen
import com.habitik.ui.screens.profile.ProfileScreen
import com.habitik.ui.screens.profile.AdminSettingsScreen
import com.habitik.ui.screens.timeline.TimelineScreen
import com.habitik.ui.theme.*
import com.habitik.ui.screens.home.TaskDetailScreen
import com.habitik.ui.screens.concentration.ConcentrationScreen

@Composable
fun MainScreen(
    rootNavController: NavHostController
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Hide bottom bar on detail screens and when add_task is opened from Timeline (with time arg)
    val isAddTaskWithTime = currentRoute?.startsWith("add_task") == true 
        && navBackStackEntry?.arguments?.getString("time") != null
    val showBottomBar = currentRoute != null 
        && currentRoute != "admin_settings" 
        && !currentRoute.startsWith("task_detail")
        && !currentRoute.startsWith("concentration")
        && !isAddTaskWithTime
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PremiumBottomBar(navController = navController)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = "dashboard"
            ) {
                composable("dashboard") {
                    DashboardScreen(
                        onNavigateToBuilder = { navController.navigate("add_task") },
                        onNavigateToConcentration = { taskId -> navController.navigate("concentration/$taskId") }
                    )
                }
                composable("timeline") {
                    TimelineScreen(
                        onNavigateToDetail = { taskId -> navController.navigate("task_detail/$taskId") },
                        onAddTaskAtTime = { time ->
                            navController.navigate("add_task?time=$time") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = "add_task?time={time}",
                    arguments = listOf(
                        androidx.navigation.navArgument("time") {
                            type = androidx.navigation.NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val time = backStackEntry.arguments?.getString("time")
                    // When coming from Timeline (time != null), pop back after sheet dismiss
                    RoutineBuilderScreen(
                        initialTime = time,
                        onSheetDismissed = if (time != null) {
                            { navController.popBackStack() }
                        } else null
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        onNavigateToAdmin = { navController.navigate("admin_settings") }
                    )
                }
                composable("admin_settings") {
                    AdminSettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "task_detail/{taskId}",
                    deepLinks = listOf(
                        navDeepLink { uriPattern = "habitik://task_detail/{taskId}" }
                    )
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0
                    TaskDetailScreen(
                        taskId = taskId,
                        onNavigateToConcentration = { id -> navController.navigate("concentration/$id") },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "concentration/{taskId}",
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(400, easing = EaseInOutCubic)
                        ) + fadeIn(animationSpec = tween(400))
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(400, easing = EaseInOutCubic)
                        ) + fadeOut(animationSpec = tween(400))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(300))
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(400, easing = EaseInOutCubic)
                        ) + fadeOut(animationSpec = tween(400))
                    }
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0
                    ConcentrationScreen(
                        taskId = taskId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem("dashboard", "Home", Icons.Default.Home),
        BottomNavItem("timeline", "Timeline", Icons.Default.CalendarMonth),
        BottomNavItem("add_task", "Routine", Icons.Default.Assignment),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute?.split("?")?.firstOrNull() == item.route
                BottomTabItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomTabItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "content"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            if (isSelected) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.title,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)
