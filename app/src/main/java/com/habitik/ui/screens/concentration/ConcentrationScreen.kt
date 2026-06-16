package com.habitik.ui.screens.concentration

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitik.ui.theme.*

@Composable
fun ConcentrationScreen(
    taskId: Int,
    viewModel: ConcentrationViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    val view = LocalView.current
    val context = LocalContext.current
    val activity = context as? Activity

    var showDndPermissionDialog by remember { mutableStateOf(false) }
    val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && notificationManager != null) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                showDndPermissionDialog = true
            }
        }
    }

    // Enter Immersive Fullscreen Mode & Pause Notifications
    DisposableEffect(view) {
        viewModel.setConcentrationModeActive(true)
        
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        
        var originalFilter: Int? = null
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && notificationManager != null) {
            try {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    originalFilter = notificationManager.currentInterruptionFilter
                    notificationManager.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_NONE)
                }
            } catch (e: Exception) {
                // Ignore any DND permission/security exception
            }
        }
        
        onDispose {
            viewModel.setConcentrationModeActive(false)
            
            window?.let {
                WindowCompat.getInsetsController(it, view).show(WindowInsetsCompat.Type.systemBars())
            }
            
            // Restore original DND level
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && notificationManager != null && originalFilter != null) {
                try {
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        notificationManager.setInterruptionFilter(originalFilter)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    if (uiState.task == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0B1E)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val task = uiState.task!!
    val categoryColor = getCategoryColor(task.category)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        categoryColor.copy(alpha = 0.35f),
                        Color(0xFF0F0B1E)
                    ),
                    radius = 1200f
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar: Task Name & Exit Button
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = task.category.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Color.White)
                    }
                }
            }

            // Central Concentration Timer Ring
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Outer ticking ring
                    Canvas(modifier = Modifier.size(300.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        
                        // Background track
                        drawArc(
                            color = Color.White.copy(alpha = 0.05f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )

                        // Main progress arc
                        drawArc(
                            brush = SolidColor(categoryColor),
                            startAngle = -90f,
                            sweepAngle = 360f * (1f - uiState.progress),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Ticking details inside the circle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = getCategoryEmoji(task.category),
                            fontSize = 44.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = uiState.remainingTime,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp),
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1.5).sp
                        )
                        Text(
                            text = if (uiState.isFocusing) "FOCUSING" else "PAUSED",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )
                    }
                }
            }

            // Immersive Control Actions Row
            Row(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume Focus Button
                val buttonBgColor = if (uiState.isFocusing) Color.White.copy(alpha = 0.08f) else categoryColor
                val buttonContentColor = if (uiState.isFocusing) Color.White else Color.White
                
                Surface(
                    onClick = {
                        if (uiState.isFocusing) viewModel.onPauseFocus()
                        else viewModel.onStartFocus()
                    },
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = buttonBgColor,
                    contentColor = buttonContentColor,
                    border = if (uiState.isFocusing) BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isFocusing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isFocusing) "Pause" else "Start"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isFocusing) "PAUSE" else "START FOCUS",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // Done Button
                Surface(
                    onClick = {
                        viewModel.onDoneTask()
                        onBack()
                    },
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    contentColor = Color(0xFF0F0B1E)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Complete")
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "COMPLETE",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }

        if (showDndPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showDndPermissionDialog = false },
                title = { Text("Mute All Notifications", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                text = { Text("To block all notifications and distractions while focusing, Habitik needs permission to toggle Do Not Disturb. Would you like to enable this?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDndPermissionDialog = false
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }
                        }
                    ) {
                        Text("ENABLE DND", fontWeight = FontWeight.Bold, color = categoryColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDndPermissionDialog = false }) {
                        Text("LATER", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}
