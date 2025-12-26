package com.abbie.alpvp.views

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abbie.alpvp.models.TaskModel
import com.abbie.alpvp.viewmodels.AppViewModelProvider
import com.abbie.alpvp.viewmodels.TaskListViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

private val AppGreen = Color(0xFF66A678)
private val AppBackground = Color(0xFFF5F7F5)
private val TextPrimary = Color(0xFF1A1C19)
private val TextSecondary = Color(0xFF757575)
private val OrangeAccent = Color(0xFFFF9800)

enum class PriorityLevel {
    URGENT,      // merah
    HIGH,        // orange
    NORMAL,      // green
    COMPLETED    // gray
}

private fun getPriorityColor(level: PriorityLevel): Color {
    return when (level) {
        PriorityLevel.URGENT -> Color(0xFFE53935)
        PriorityLevel.HIGH -> Color(0xFFFF9800)
        PriorityLevel.NORMAL -> Color(0xFF66A678)
        PriorityLevel.COMPLETED -> Color(0xFFE0E0E0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaskListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val todoTasks by viewModel.todoTasks.collectAsState()
    val finishedTasks by viewModel.finishedTasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchTasks()
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Manage Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = OrangeAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Deadlines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // empty state
                if (todoTasks.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No upcoming tasks",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Todo Tasks
                items(todoTasks.size) { index ->
                    val task = todoTasks[index]
                    TaskCardItem(
                        task = task,
                        priorityLevel = when (index) {
                            0 -> PriorityLevel.URGENT    // 1st task red
                            1 -> PriorityLevel.HIGH      // 2nd orange
                            else -> PriorityLevel.NORMAL // others green
                        },
                        onToggleComplete = {
                            viewModel.toggleTaskCompletion(task.id, true)
                        },
                        onDelete = {
                            viewModel.deleteTask(task.id)
                        }
                    )
                }

                // DONE
                if (finishedTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Completed Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(finishedTasks.size) { index ->
                        val task = finishedTasks[index]
                        TaskCardItem(
                            task = task,
                            priorityLevel = PriorityLevel.COMPLETED,
                            onToggleComplete = {
                                viewModel.toggleTaskCompletion(task.id, false)
                            },
                            onDelete = {
                                viewModel.deleteTask(task.id)
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, desc, deadline, scheduleId ->
                    viewModel.createTask(
                        scheduleId = scheduleId,
                        title = title,
                        deadline = deadline,
                        description = desc
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TaskCardItem(
    task: TaskModel,
    priorityLevel: PriorityLevel,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = getPriorityColor(priorityLevel)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // warna left border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(borderColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // content tasknya
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) Color.Gray else TextPrimary
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

            // action icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // checkbox
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = if (task.isCompleted) "Mark as incomplete" else "Mark as complete",
                    tint = if (task.isCompleted) AppGreen else Color(0xFFE0E0E0),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onToggleComplete() }
                )

                // delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete task",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String, Int?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var scheduleId by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->
                // match ke backend formatnya
                deadline = String.format("%02d-%02d-%04d", day, month + 1, year)
                errorMessage = null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    fun validateAndSubmit() {
        when {
            title.isBlank() -> errorMessage = "Task title is required!"
            deadline.isEmpty() -> errorMessage = "Deadline is required!"
            else -> onConfirm(
                title,
                if (description.isBlank()) null else description,
                deadline,
                scheduleId
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                "Add New Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showDatePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (deadline.isEmpty()) "Set Deadline *"
                        else "Deadline: $deadline"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { validateAndSubmit() },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private fun formatDeadline(deadline: String?): String {
    if (deadline.isNullOrBlank()) return "No deadline"

    return try {
        // parsing as ISO instant
        val instant = Instant.parse(deadline)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (_: DateTimeParseException) {
        try {
            // parsing as ISO local date
            val localDate = LocalDate.parse(deadline, DateTimeFormatter.ISO_LOCAL_DATE)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.getDefault())
            localDate.format(formatter)
        } catch (_: DateTimeParseException) {
            try {
                // parsing as DD-MM-YYYY
                val pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val localDate = LocalDate.parse(deadline, pattern)
                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.getDefault())
                localDate.format(formatter)
            } catch (_: Exception) {
                // biar g ngecrash
                deadline
            }
        }
    }
}