package com.abbie.alpvp.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abbie.alpvp.viewmodels.AnimationType
import com.abbie.alpvp.viewmodels.TimerState
import com.abbie.alpvp.viewmodels.TimerViewModel
import com.airbnb.lottie.compose.*

private val AppGreen = Color(0xFF66A678)
private val AppBackground = Color(0xFFF5F7F5)
private val TextPrimary = Color(0xFF1A1C19)
private val TextSecondary = Color(0xFF757575)
private val OrangeAccent = Color(0xFFFF9800)

private fun getAnimationRes(type: AnimationType): Int? {
    return when (type) {
        AnimationType.NONE -> null
        AnimationType.LOADER_CAT -> com.abbie.alpvp.R.raw.loader_cat
        AnimationType.WATER_BUBBLE -> com.abbie.alpvp.R.raw.water_bubble
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRewards: () -> Unit = {},
    onNavigateToGame: () -> Unit,
    viewModel: TimerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.shouldNavigateToGame) {
        if (uiState.shouldNavigateToGame) {
            onNavigateToGame()
            viewModel.clearGameNavigation()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pomodoro Timer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.setShowSettings(true) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "timer",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateBack()
                        "rewards" -> onNavigateToRewards()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.pomodoroCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AppGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.pomodoroCount} Pomodoro${if (uiState.pomodoroCount > 1) "s" else ""} completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (uiState.timerState) {
                        TimerState.FOCUS, TimerState.PAUSED -> AppGreen.copy(alpha = 0.1f)
                        TimerState.SHORT_BREAK -> OrangeAccent.copy(alpha = 0.1f)
                        TimerState.LONG_BREAK -> Color(0xFF42A5F5).copy(alpha = 0.1f)
                        TimerState.IDLE -> Color.White
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = when (uiState.timerState) {
                        TimerState.FOCUS -> "Focus Time"
                        TimerState.SHORT_BREAK -> "Short Break"
                        TimerState.LONG_BREAK -> "Long Break"
                        TimerState.PAUSED -> "Paused"
                        TimerState.IDLE -> "Ready to Focus"
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (uiState.timerState) {
                        TimerState.FOCUS, TimerState.PAUSED -> AppGreen
                        TimerState.SHORT_BREAK -> OrangeAccent
                        TimerState.LONG_BREAK -> Color(0xFF42A5F5)
                        TimerState.IDLE -> TextPrimary
                    }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                CircularProgressTimer(
                    progress = if (uiState.totalTime > 0) uiState.timeRemaining.toFloat() / uiState.totalTime else 1f,
                    color = when (uiState.timerState) {
                        TimerState.FOCUS, TimerState.PAUSED -> AppGreen
                        TimerState.SHORT_BREAK -> OrangeAccent
                        TimerState.LONG_BREAK -> Color(0xFF42A5F5)
                        TimerState.IDLE -> Color.LightGray
                    }
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uiState.isRunning && uiState.selectedAnimation != AnimationType.NONE) {
                        val animationRes = getAnimationRes(uiState.selectedAnimation)
                        if (animationRes != null) {
                            LottieAnimationView(
                                animationRes = animationRes,
                                modifier = Modifier.size(120.dp)
                            )
                        }
                    }

                    if (uiState.isRunning && uiState.selectedAnimation != AnimationType.NONE) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = formatTime(uiState.timeRemaining),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 56.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToGame,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text("🎮 Play Mini Game", color = AppGreen)
                }

                OutlinedButton(
                    onClick = { viewModel.setShowAnimationPicker(true) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text("Choose Animation")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.timerState != TimerState.IDLE) {
                    IconButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                FloatingActionButton(
                    onClick = {
                        when {
                            uiState.timerState == TimerState.IDLE -> viewModel.startFocusSession()
                            uiState.isRunning -> viewModel.pauseTimer()
                            else -> viewModel.resumeTimer()
                        }
                    },
                    containerColor = when (uiState.timerState) {
                        TimerState.FOCUS, TimerState.PAUSED -> AppGreen
                        TimerState.SHORT_BREAK -> OrangeAccent
                        TimerState.LONG_BREAK -> Color(0xFF42A5F5)
                        TimerState.IDLE -> AppGreen
                    },
                    contentColor = Color.White,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isRunning) "Pause" else "Start",
                        modifier = Modifier.size(36.dp)
                    )
                }

                if (uiState.timerState != TimerState.IDLE) {
                    IconButton(
                        onClick = { viewModel.skipToNext() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip",
                            tint = TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TimeSettingChip(
                        label = "Focus",
                        minutes = uiState.focusMinutes,
                        color = AppGreen
                    )
                    TimeSettingChip(
                        label = "Short Break",
                        minutes = uiState.shortBreakMinutes,
                        color = OrangeAccent
                    )
                    TimeSettingChip(
                        label = "Long Break",
                        minutes = uiState.longBreakMinutes,
                        color = Color(0xFF42A5F5)
                    )
                }
            }
        }

        if (uiState.showSettings) {
            PomodoroSettingsDialog(
                focusMinutes = uiState.focusMinutes,
                shortBreakMinutes = uiState.shortBreakMinutes,
                longBreakMinutes = uiState.longBreakMinutes,
                onDismiss = { viewModel.setShowSettings(false) },
                onSave = { focus, shortBreak, longBreak ->
                    viewModel.updateSettings(focus, shortBreak, longBreak)
                    viewModel.setShowSettings(false)
                }
            )
        }

        if (uiState.showAnimationPicker) {
            AnimationPickerDialog(
                currentAnimation = uiState.selectedAnimation,
                onDismiss = { viewModel.setShowAnimationPicker(false) },
                onSelect = { animation ->
                    viewModel.setSelectedAnimation(animation)
                    viewModel.setShowAnimationPicker(false)
                }
            )
        }
    }
}

@Composable
fun CircularProgressTimer(
    progress: Float,
    color: Color
) {
    Canvas(modifier = Modifier.size(280.dp)) {
        drawCircle(
            color = Color.LightGray.copy(alpha = 0.2f),
            style = Stroke(width = 16.dp.toPx())
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun TimeSettingChip(
    label: String,
    minutes: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${minutes}m",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun PomodoroSettingsDialog(
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int) -> Unit
) {
    var tempFocus by remember { mutableStateOf(focusMinutes) }
    var tempShortBreak by remember { mutableStateOf(shortBreakMinutes) }
    var tempLongBreak by remember { mutableStateOf(longBreakMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                "Timer Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                TimePickerRow(
                    label = "Focus Duration",
                    minutes = tempFocus,
                    color = AppGreen,
                    onMinutesChange = { tempFocus = it }
                )

                TimePickerRow(
                    label = "Short Break",
                    minutes = tempShortBreak,
                    color = OrangeAccent,
                    onMinutesChange = { tempShortBreak = it }
                )

                TimePickerRow(
                    label = "Long Break",
                    minutes = tempLongBreak,
                    color = Color(0xFF42A5F5),
                    onMinutesChange = { tempLongBreak = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(tempFocus, tempShortBreak, tempLongBreak) },
                colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun TimePickerRow(
    label: String,
    minutes: Int,
    color: Color,
    onMinutesChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { if (minutes > 1) onMinutesChange(minutes - 1) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = color
                )
            }

            Text(
                text = "$minutes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.width(40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            IconButton(
                onClick = { if (minutes < 60) onMinutesChange(minutes + 1) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = color
                )
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

@Composable
fun LottieAnimationView(
    animationRes: Int,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

@Composable
fun AnimationPickerDialog(
    currentAnimation: AnimationType,
    onDismiss: () -> Unit,
    onSelect: (AnimationType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                "Choose Animation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Select an animation to display during focus sessions:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimationOptionCard(
                    title = "No Animation",
                    description = "Focus without distractions",
                    isSelected = currentAnimation == AnimationType.NONE,
                    icon = Icons.Default.Block,
                    onClick = { onSelect(AnimationType.NONE) }
                )

                AnimationOptionCard(
                    title = "Loader Cat",
                    description = "Cute cat loading animation",
                    isSelected = currentAnimation == AnimationType.LOADER_CAT,
                    icon = Icons.Default.Pets,
                    onClick = { onSelect(AnimationType.LOADER_CAT) },
                    showPreview = true,
                    animationRes = com.abbie.alpvp.R.raw.loader_cat
                )

                AnimationOptionCard(
                    title = "Water Bubble",
                    description = "Calming water animation",
                    isSelected = currentAnimation == AnimationType.WATER_BUBBLE,
                    icon = Icons.Default.WaterDrop,
                    onClick = { onSelect(AnimationType.WATER_BUBBLE) },
                    showPreview = true,
                    animationRes = com.abbie.alpvp.R.raw.water_bubble
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AppGreen)
            }
        }
    )
}

@Composable
fun AnimationOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    showPreview: Boolean = false,
    animationRes: Int? = null
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppGreen.copy(alpha = 0.1f) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = if (isSelected) AppGreen else Color(0xFFE0E0E0)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showPreview && animationRes != null && isSelected) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    LottieAnimationView(
                        animationRes = animationRes,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AppGreen.copy(alpha = 0.2f) else Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) AppGreen else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) AppGreen else TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = AppGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppGreen,
                indicatorColor = AppGreen.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "timer",
            onClick = { onNavigate("timer") },
            icon = { Icon(Icons.Default.Timer, contentDescription = null) },
            label = { Text("Timer") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppGreen,
                indicatorColor = AppGreen.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "rewards",
            onClick = { onNavigate("rewards") },
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
            label = { Text("Rewards") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppGreen,
                indicatorColor = AppGreen.copy(alpha = 0.1f)
            )
        )
    }
}