package com.habitik.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitik.data.entity.TaskEntity
import com.habitik.ui.components.CircularCountdownRing
import com.habitik.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Animated background
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnimatedBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bgShift"
    )
    val bgBrush = Brush.radialGradient(
        colors = listOf(
            GlowViolet.copy(alpha = 0.22f + 0.08f * shift),
            GlowCyan.copy(alpha = 0.10f + 0.06f * (1f - shift)),
            BgBase
        ),
        center = Offset(200f + 150f * shift, 300f + 100f * shift),
        radius  = 1200f
    )
    Box(modifier = modifier.background(BgBase)) {
        Box(modifier = Modifier.fillMaxSize().background(bgBrush))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Root
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToBuilder: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier         = Modifier.fillMaxSize().padding(horizontal = 22.dp),
            contentPadding   = PaddingValues(top = 52.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            item { HeaderSection(onNavigateToBuilder) }
            item { Spacer(Modifier.height(28.dp)) }

            // Main focus card
            item {
                when {
                    uiState.currentTask != null ->
                        NowFocusCard(
                            task          = uiState.currentTask!!,
                            remainingTime = uiState.remainingTime,
                            progress      = uiState.progress
                        )
                    uiState.isRestDay -> RestDayCard()
                    else -> WaitingCard(uiState.nextTasks.firstOrNull())
                }
                Spacer(Modifier.height(36.dp))
            }

            // Coming up
            if (uiState.nextTasks.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .width(4.dp)
                                .height(20.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(GlowViolet, GlowCyan)
                                    )
                                )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Coming Up",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }
                items(uiState.nextTasks) { task ->
                    NextTaskItem(task)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderSection(onNavigateToBuilder: () -> Unit) {
    val today = remember {
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
        )
    }
    Row(
        modifier             = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment    = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text       = today,
                style      = MaterialTheme.typography.labelLarge,
                color      = GlowViolet.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text          = "Now Focusing",
                style         = MaterialTheme.typography.displaySmall,
                fontWeight    = FontWeight.Black,
                color         = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-1.5).sp
            )
            Text(
                text  = "Your daily flow is active",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Settings FAB – glass pill
        Surface(
            onClick       = onNavigateToBuilder,
            modifier      = Modifier.size(48.dp),
            shape         = CircleShape,
            color         = Color.White.copy(alpha = 0.07f),
            border        = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
            tonalElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Builder",
                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Now Focus Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NowFocusCard(task: TaskEntity, remainingTime: String, progress: Float) {
    val gradient  = getCategoryGradient(task.category)
    val color     = getCategoryColor(task.category)

    // Pulsing glow animation
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0.55f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            // outer glow shadow
            .shadow(
                elevation   = 32.dp,
                shape       = RoundedCornerShape(40.dp),
                spotColor   = color.copy(alpha = pulse),
                ambientColor = color.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(40.dp))
            .background(gradient)
    ) {
        // Shine overlay (top → transparent)
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                        endY = 400f
                    )
                )
        )
        // Diagonal shimmer stripe
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.04f)
                        ),
                        start = Offset(0f, 0f),
                        end   = Offset(1200f, 1200f)
                    )
                )
        )
        // Glass inner border
        Box(
            Modifier
                .fillMaxSize()
                .border(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.05f))
                    ),
                    RoundedCornerShape(40.dp)
                )
        )

        Column(
            modifier             = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment  = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.SpaceBetween
        ) {
            // Category pill + task name
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Text(
                        text          = task.category.uppercase(),
                        modifier      = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        style         = MaterialTheme.typography.labelLarge,
                        color         = Color.White,
                        fontWeight    = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text          = task.name,
                    style         = MaterialTheme.typography.headlineLarge,
                    fontWeight    = FontWeight.Black,
                    color         = Color.White,
                    letterSpacing = (-0.5).sp
                )
            }

            // Countdown ring
            CircularCountdownRing(
                progress      = progress,
                remainingTime = remainingTime,
                gradient      = Brush.linearGradient(
                    listOf(Color.White, Color.White.copy(alpha = 0.55f))
                ),
                modifier = Modifier.size(210.dp)
            )

            // Action buttons
            Row(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassActionButton(
                    icon           = Icons.Default.SkipNext,
                    label          = "Skip",
                    containerColor = Color.White.copy(alpha = 0.14f),
                    contentColor   = Color.White,
                    modifier       = Modifier.weight(1f)
                )
                GlassActionButton(
                    icon           = Icons.Default.Check,
                    label          = "Done",
                    containerColor = Color.White,
                    contentColor   = color,
                    modifier       = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action Button (glass style)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GlassActionButton(
    icon           : androidx.compose.ui.graphics.vector.ImageVector,
    label          : String,
    containerColor : Color,
    contentColor   : Color,
    modifier       : Modifier = Modifier
) {
    Surface(
        modifier       = modifier.height(58.dp),
        shape          = RoundedCornerShape(22.dp),
        color          = containerColor,
        contentColor   = contentColor,
        border         = if (containerColor != Color.White)
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                         else null,
        tonalElevation  = 0.dp
    ) {
        Row(
            modifier             = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment    = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Next Task Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NextTaskItem(task: TaskEntity) {
    val color = getCategoryColor(task.category)
    Surface(
        modifier       = Modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(24.dp),
        color          = Color.White.copy(alpha = 0.05f),
        border         = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
        tonalElevation  = 0.dp
    ) {
        Row(
            modifier             = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Category dot + gradient box
                Box(
                    Modifier
                        .size(50.dp)
                        .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = color.copy(alpha = 0.4f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(getCategoryGradient(task.category))
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        task.name,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Starts at ${task.startTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Arrow chip
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Rest Day Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RestDayCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .shadow(24.dp, RoundedCornerShape(40.dp), spotColor = GlowViolet.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(40.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFEC4899)))
            )
            .border(
                1.5.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)),
                RoundedCornerShape(40.dp)
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("All Done! 🎉", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Text(
                "Time to rest and recharge for tomorrow.",
                style     = MaterialTheme.typography.bodyLarge,
                color     = Color.White.copy(alpha = 0.82f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Waiting Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun WaitingCard(nextTask: TaskEntity?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                1.5.dp,
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.14f), Color.Transparent)
                ),
                RoundedCornerShape(40.dp)
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "TAKE A BREATH",
                style         = MaterialTheme.typography.labelLarge,
                color         = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 4.sp,
                fontWeight    = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                nextTask?.name ?: "No tasks scheduled",
                style         = MaterialTheme.typography.headlineMedium,
                color         = MaterialTheme.colorScheme.onSurface,
                fontWeight    = FontWeight.Black,
                textAlign     = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (nextTask != null) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GlowViolet.copy(alpha = 0.16f)
                ) {
                    Text(
                        "Starting at ${nextTask.startTime}",
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = GlowViolet.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
