package com.abbie.alpvp.views

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abbie.alpvp.models.ScheduleActivityModel
import com.abbie.alpvp.models.TaskModel
import com.abbie.alpvp.viewmodels.AppViewModelProvider
import com.abbie.alpvp.viewmodels.DashboardState
import com.abbie.alpvp.viewmodels.DashboardViewModel
import com.abbie.alpvp.viewmodels.TaskListViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

// --- Color Constants ---
private val AppBgColor = Color(0xFFF5F7F5)
private val PrimaryGreen = Color(0xFF66A678)
private val TextPrimary = Color(0xFF1A1C19)
private val TextSecondary = Color(0xFF757575)

@Composable
fun DashboardScreen(
    onNavigateToActivityList: () -> Unit,
    onNavigateToTaskList: () -> Unit,
    onNavigateToTimer: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.dashboardState.collectAsState()
    val context = LocalContext.current

    // Handle Toast messages
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    // Load Data
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    DashboardContent(
        state = state,
        onAddActivity = { icon, start, end, desc ->
            viewModel.addActivity(icon, start, end, desc)
        },
        onNavigateToManage = onNavigateToActivityList,
        onNavigateToTaskManage = onNavigateToTaskList,
        onNavigateToTimer = onNavigateToTimer
    )
}

@Composable
fun DashboardContent(
    state: DashboardState,
    onAddActivity: (String, String, String, String) -> Unit,
    onNavigateToManage: () -> Unit,
    onNavigateToTaskManage: () -> Unit,
    onNavigateToTimer: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedIconName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = AppBgColor,
        bottomBar = {
            BottomNavBar(onNavigate = { route ->
                if (route == "timer") onNavigateToTimer()
                // "home" does nothing as we are already here
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // --- Header Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${state.username}!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF7043),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "3 Day Streak",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Progress Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${(state.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(state.progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF81C784), PrimaryGreen)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${state.completedCount} of ${state.totalCount} completed",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Next Schedule Card ---
            NextScheduleCard(
                currentActivity = state.currentActivity,
                upcomingActivities = state.upcomingActivities
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Activities Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Manage",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToManage() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ActivitySelectorRow { iconName ->
                selectedIconName = iconName
                showDialog = true
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Upcoming Tasks Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Deadlines",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Manage",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToTaskManage() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task List ViewModel Integration
            val taskListViewModel: TaskListViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val todoTasks by taskListViewModel.todoTasks.collectAsState()

            LaunchedEffect(Unit) {
                taskListViewModel.fetchTasks()
            }

            val tasksToShow = remember(todoTasks) { todoTasks.take(3) }
            Column {
                tasksToShow.forEachIndexed { index, task ->
                    val priorityColor = when (index) {
                        0 -> Color(0xFFE53935)
                        1 -> Color(0xFFFF9800)
                        else -> Color(0xFF66A678)
                    }

                    TaskItemClean(
                        task = task,
                        color = priorityColor,
                        onToggleComplete = {
                            taskListViewModel.toggleTaskCompletion(task.id, true)
                        }
                    )
                }
                if (tasksToShow.isEmpty()) {
                    Text(
                        text = "No upcoming tasks",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showDialog) {
        AddActivityDialog(
            iconName = selectedIconName,
            onDismiss = { showDialog = false },
            onConfirm = { start, end, desc ->
                onAddActivity(selectedIconName, start, end, desc)
                showDialog = false
            }
        )
    }
}

// --- COMPONENTS ---

@Composable
fun TaskItemClean(
    task: TaskModel,
    color: Color,
    onToggleComplete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDeadline(task.deadline),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Mark as complete",
                tint = if (task.isCompleted) Color(0xFF66A678) else Color(0xFFE0E0E0),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onToggleComplete() }
            )
        }
    }
}

@Composable
fun NextScheduleCard(
    currentActivity: ScheduleActivityModel?,
    upcomingActivities: List<ScheduleActivityModel>
) {
    val isCurrentActive = currentActivity != null
    val displayActivity = if (isCurrentActive) currentActivity!! else upcomingActivities.firstOrNull()

    val headerText = if (isCurrentActive) "Happening Now" else "Next on Schedule"
    val headerIcon = if (isCurrentActive) Icons.Default.PlayCircleFilled else Icons.Default.WbSunny
    val cardColor = if (isCurrentActive) Color(0xFF558B63) else Color(0xFF66A678)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = headerIcon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = headerText,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                if (isCurrentActive) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ACTIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (displayActivity != null) {
                Text(
                    text = displayActivity.iconName.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (displayActivity.description.isNotEmpty()) displayActivity.description else "No description",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${formatIsoTime(displayActivity.startTime)} - ${formatIsoTime(displayActivity.endTime)}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                val listToShow = if (isCurrentActive) upcomingActivities.take(2) else upcomingActivities.drop(1)

                if (listToShow.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "UP NEXT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    listToShow.forEach { nextItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color.White.copy(alpha = 0.6f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "${formatIsoTime(nextItem.startTime)} - ${formatIsoTime(nextItem.endTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(80.dp)
                                )
                            }
                            Text(
                                text = nextItem.iconName.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All caught up! No tasks right now.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ActivitySelectorRow(onIconSelected: (String) -> Unit) {
    data class ActivityOption(
        val name: String,
        val icon: ImageVector,
        val color: Color,
        val bgColor: Color
    )

    val items = listOf(
        ActivityOption("Wake up", Icons.Default.WbSunny, Color(0xFF66BB6A), Color(0xFFE8F5E9)),
        ActivityOption("Wash up", Icons.Default.Face, Color(0xFF42A5F5), Color(0xFFE3F2FD)),
        ActivityOption("Prayer", Icons.Default.Spa, Color(0xFFAB47BC), Color(0xFFF3E5F5)),
        ActivityOption("Eat", Icons.Default.Restaurant, Color(0xFFFFA726), Color(0xFFFFF3E0)),
        ActivityOption("Study", Icons.Default.MenuBook, Color(0xFFEF5350), Color(0xFFFFEBEE)),
        ActivityOption("Play", Icons.Default.SportsEsports, Color(0xFFFFCA28), Color(0xFFFFF8E1)),
        ActivityOption("Family", Icons.Default.Group, Color(0xFF26A69A), Color(0xFFE0F2F1)),
        ActivityOption("Sleep", Icons.Default.Bedtime, Color(0xFF5C6BC0), Color(0xFFE8EAF6))
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(items) { option ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(72.dp)
                    .clickable { onIconSelected(option.name.lowercase()) }
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(option.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.name,
                        tint = option.color,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = option.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AddActivityDialog(
    iconName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("09:00") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun showTimePicker(initial: String, onTimeSelected: (String) -> Unit) {
        val parts = initial.split(":")
        val h = if (parts.size > 1) parts[0].toInt() else 8
        val m = if (parts.size > 1) parts[1].toInt() else 0

        TimePickerDialog(context, { _, hour, minute ->
            onTimeSelected(String.format("%02d:%02d", hour, minute))
        }, h, m, true).show()
    }

    fun validateAndSubmit() {
        val startMins = startTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        val endMins = endTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }

        if (endMins <= startMins) {
            errorMessage = "End time must be later than start time!"
        } else {
            val dateNow = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            onConfirm("${dateNow}T${startTime}:00.000Z", "${dateNow}T${endTime}:00.000Z", description)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                "Add Activity",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text("Adding to: ${iconName.replaceFirstChar { it.uppercase() }}", color = PrimaryGreen, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(
                        onClick = { showTimePicker(startTime) { startTime = it } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Start", fontSize = 10.sp, color = TextSecondary)
                            Text(startTime, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { showTimePicker(endTime) { endTime = it } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("End", fontSize = 10.sp, color = TextSecondary)
                            Text(endTime, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { validateAndSubmit() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun BottomNavBar(onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = true,
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryGreen, indicatorColor = Color(0xFFE8F5E9))
        )
        NavigationBarItem(
            selected = false,
            onClick = { onNavigate("timer") },
            icon = { Icon(Icons.Default.Timer, contentDescription = null) },
            label = { Text("Timer") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryGreen)
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
            label = { Text("Rewards") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryGreen)
        )
    }
}

// --- UTILS ---

fun formatIsoTime(isoString: String): String {
    return try {
        if (isoString.contains("T")) isoString.split("T")[1].substring(0, 5) else isoString
    } catch (e: Exception) { "" }
}

private fun formatDeadline(deadline: String?): String {
    if (deadline.isNullOrBlank()) return "No deadline"

    return try {
        val instant = Instant.parse(deadline)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (_: DateTimeParseException) {
        try {
            val localDate = LocalDate.parse(deadline, DateTimeFormatter.ISO_LOCAL_DATE)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.getDefault())
            localDate.format(formatter)
        } catch (_: DateTimeParseException) {
            try {
                val pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val localDate = LocalDate.parse(deadline, pattern)
                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.getDefault())
                localDate.format(formatter)
            } catch (_: Exception) {
                deadline
            }
        }
    }
}