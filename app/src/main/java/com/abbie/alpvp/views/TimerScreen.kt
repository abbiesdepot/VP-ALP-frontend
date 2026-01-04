package com.abbie.alpvp.views

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val AppGreen = Color(0xFF66A678)
private val AppBackground = Color(0xFFF5F7F5)
private val TextPrimary = Color(0xFF1A1C19)
private val TextSecondary = Color(0xFF757575)
private val OrangeAccent = Color(0xFFFF9800)

enum class TimerState {
    IDLE, FOCUS, SHORT_BREAK, LONG_BREAK, PAUSED
}

enum class AnimationType {
    NONE,
    LOADER_CAT,
    WATER_BUBBLE
}

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
    onNavigateToGame: () -> Unit
) {
    var timerState by remember { mutableStateOf(TimerState.IDLE) }
    var timeRemaining by remember { mutableStateOf(25 * 60) }
    var totalTime by remember { mutableStateOf(25 * 60) }
    var pomodoroCount by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    var focusMinutes by remember { mutableStateOf(25) }
    var shortBreakMinutes by remember { mutableStateOf(5) }
    var longBreakMinutes by remember { mutableStateOf(15) }
    var showSettings by remember { mutableStateOf(false) }

    var selectedAnimation by remember { mutableStateOf(AnimationType.LOADER_CAT) }
    var showAnimationPicker by remember { mutableStateOf(false) }
    var previousState by remember { mutableStateOf(TimerState.IDLE) }

    LaunchedEffect(isRunning, timeRemaining) {
        if (isRunning && timeRemaining > 0) {
            delay(1000L)
            if (isActive) {
                timeRemaining--
            }
        } else if (isRunning && timeRemaining == 0) {
            isRunning = false
            when (timerState) {
                TimerState.FOCUS -> {
                    pomodoroCount++
                    if (pomodoroCount % 4 == 0) {
                        timerState = TimerState.LONG_BREAK
                        timeRemaining = longBreakMinutes * 60
                        totalTime = longBreakMinutes * 60
                    } else {
                        timerState = TimerState.SHORT_BREAK
                        timeRemaining = shortBreakMinutes * 60
                        totalTime = shortBreakMinutes * 60
                    }
                    onNavigateToGame()
                }
                TimerState.SHORT_BREAK, TimerState.LONG_BREAK -> {
                    timerState = TimerState.IDLE
                    timeRemaining = focusMinutes * 60
                    totalTime = focusMinutes * 60
                }
                else -> {}
            }
        }
    }

    fun startFocusSession() {
        previousState = TimerState.FOCUS
        timerState = TimerState.FOCUS
        timeRemaining = focusMinutes * 60
        totalTime = focusMinutes * 60
        isRunning = true
    }

    fun pauseTimer() {
        if (timerState != TimerState.PAUSED) {
            previousState = timerState
        }
        isRunning = false
        timerState = TimerState.PAUSED
    }

    fun resumeTimer() {
        isRunning = true
        if (timerState == TimerState.PAUSED) {
            timerState = previousState
        }
    }

    fun resetTimer() {
        isRunning = false
        timerState = TimerState.IDLE
        timeRemaining = focusMinutes * 60
        totalTime = focusMinutes * 60
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
                    IconButton(onClick = { showSettings = true }) {
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
            if (pomodoroCount > 0) {
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
                            text = "$pomodoroCount Pomodoro${if (pomodoroCount > 1) "s" else ""} completed",
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
                    containerColor = when (timerState) {
                        TimerState.FOCUS, TimerState.PAUSED -> AppGreen.copy(alpha = 0.1f)
                        TimerState.SHORT_BREAK -> OrangeAccent.copy(alpha = 0.1f)
                        TimerState.LONG_BREAK -> Color(0xFF42A5F5).copy(alpha = 0.1f)
                        TimerState.IDLE -> Color.White
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = when (timerState) {
                        TimerState.FOCUS -> "Focus Time"
                        TimerState.SHORT_BREAK -> "Short Break"
                        TimerState.LONG_BREAK -> "Long Break"
                        TimerState.PAUSED -> "Paused"
                        TimerState.IDLE -> "Ready to Focus"
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (timerState) {
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
                    progress = if (totalTime > 0) timeRemaining.toFloat() / totalTime else 1f,
                    color = when (timerState) {
                        TimerState.FOCUS, TimerState.PAUSED -> AppGreen
                        TimerState.SHORT_BREAK -> OrangeAccent
                        TimerState.LONG_BREAK -> Color(0xFF42A5F5)
                        TimerState.IDLE -> Color.LightGray
                    }
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isRunning && selectedAnimation != AnimationType.NONE) {
                        val animationRes = getAnimationRes(selectedAnimation)
                        if (animationRes != null) {
                            LottieAnimationView(
                                animationRes = animationRes,
                                modifier = Modifier.size(120.dp)
                            )
                        }
                    }

                    if (isRunning && selectedAnimation != AnimationType.NONE) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = formatTime(timeRemaining),
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
                    onClick = { showAnimationPicker = true },
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
                // Reset Button
                if (timerState != TimerState.IDLE) {
                    IconButton(
                        onClick = { resetTimer() },
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
                            timerState == TimerState.IDLE -> startFocusSession()
                            isRunning -> pauseTimer()
                            else -> resumeTimer()
                        }
                    },
                    containerColor = when (timerState) {
                        TimerState.FOCUS, TimerState.PAUSED -> AppGreen
                        TimerState.SHORT_BREAK -> OrangeAccent
                        TimerState.LONG_BREAK -> Color(0xFF42A5F5)
                        TimerState.IDLE -> AppGreen
                    },
                    contentColor = Color.White,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(36.dp)
                    )
                }

                if (timerState != TimerState.IDLE) {
                    IconButton(
                        onClick = {
                            isRunning = false
                            when (timerState) {
                                TimerState.FOCUS -> {
                                    // Skip to appropriate break
                                    if ((pomodoroCount + 1) % 4 == 0) {
                                        timerState = TimerState.LONG_BREAK
                                        timeRemaining = longBreakMinutes * 60
                                        totalTime = longBreakMinutes * 60
                                    } else {
                                        timerState = TimerState.SHORT_BREAK
                                        timeRemaining = shortBreakMinutes * 60
                                        totalTime = shortBreakMinutes * 60
                                    }
                                }
                                else -> {
                                    timerState = TimerState.IDLE
                                    timeRemaining = focusMinutes * 60
                                    totalTime = focusMinutes * 60
                                }
                            }
                        },
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
                        minutes = focusMinutes,
                        color = AppGreen
                    )
                    TimeSettingChip(
                        label = "Short Break",
                        minutes = shortBreakMinutes,
                        color = OrangeAccent
                    )
                    TimeSettingChip(
                        label = "Long Break",
                        minutes = longBreakMinutes,
                        color = Color(0xFF42A5F5)
                    )
                }
            }
        }

        if (showSettings) {
            PomodoroSettingsDialog(
                focusMinutes = focusMinutes,
                shortBreakMinutes = shortBreakMinutes,
                longBreakMinutes = longBreakMinutes,
                onDismiss = { showSettings = false },
                onSave = { focus, shortBreak, longBreak ->
                    focusMinutes = focus
                    shortBreakMinutes = shortBreak
                    longBreakMinutes = longBreak
                    if (timerState == TimerState.IDLE) {
                        timeRemaining = focus * 60
                        totalTime = focus * 60
                    }
                    showSettings = false
                }
            )
        }

        if (showAnimationPicker) {
            AnimationPickerDialog(
                currentAnimation = selectedAnimation,
                onDismiss = { showAnimationPicker = false },
                onSelect = { animation ->
                    selectedAnimation = animation
                    showAnimationPicker = false
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
            // Icon or Animation Preview
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