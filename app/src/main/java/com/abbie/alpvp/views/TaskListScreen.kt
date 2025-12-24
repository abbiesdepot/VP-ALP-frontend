import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abbie.alpvp.models.TaskModel
import com.abbie.alpvp.viewmodels.AppViewModelProvider
import com.abbie.alpvp.viewmodels.TaskListViewModel
import java.text.SimpleDateFormat
import java.util.*

private val AppGreen = Color(0xFF66A678)
private val AppBackground = Color(0xFFF5F7F5)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1C19)
private val TextSecondary = Color(0xFF757575)
private val OrangeAccent = Color(0xFFFF9800)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    userId: Int,
    token: String,
    onNavigateToDashboard: () -> Unit,
    viewModel: TaskListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTasks(userId, token)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
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
                    IconButton(onClick = onNavigateToDashboard) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (uiState.upcomingTasks.isEmpty() && !uiState.isLoading) {
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

                items(uiState.upcomingTasks) { task ->
                    TaskCardItem(
                        task = task,
                        onToggleComplete = {
                            viewModel.toggleTaskCompletion(task, token, userId)
                        },
                        onDelete = {
                            viewModel.deleteTask(task.id, token, userId)
                        }
                    )
                }

                // Completed Tasks Section (if any)
                if (uiState.completedTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Completed Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(uiState.completedTasks) { task ->
                        TaskCardItem(
                            task = task,
                            onToggleComplete = {
                                viewModel.toggleTaskCompletion(task, token, userId)
                            },
                            onDelete = {
                                viewModel.deleteTask(task.id, token, userId)
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
                onConfirm = { title, description, deadline, scheduleId ->
                    viewModel.createTask(
                        scheduleId = scheduleId,
                        title = title,
                        deadline = deadline,
                        description = description,
                        token = token,
                        userId = userId
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
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = when {
        task.isCompleted -> Color(0xFFE0E0E0)
        else -> when {
            task.title.contains("Read", ignoreCase = true) -> Color(0xFFE53935) // Red
            task.title.contains("Study", ignoreCase = true) -> Color(0xFF1E88E5) // Blue
            task.title.contains("Finish", ignoreCase = true) -> Color(0xFF8E24AA) // Purple
            else -> AppGreen
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
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
                    .background(borderColor)
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CHECK
                Icon(
                    imageVector = if (task.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.CheckCircle,
                    contentDescription = "Done",
                    tint = if (task.isCompleted) AppGreen else Color(0xFFE0E0E0),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onToggleComplete() }
                )

                // DELETE
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
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
                // format DD-MM-YYYY match backend
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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = scheduleId?.toString() ?: "",
                    onValueChange = {
                        scheduleId = it.toIntOrNull()
                    },
                    label = { Text("Schedule ID (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
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

fun formatDeadline(deadline: String): String {
    return try {
        // ngehandle DD-MM-YYYY n YYYY-MM-DD formats
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val date = when {
            deadline.contains("-") -> {
                val parts = deadline.split("-")
                when {
                    parts[0].length == 4 -> {
                        // YYYY-MM-DD format
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(deadline)
                    }
                    else -> {
                        // DD-MM-YYYY format
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(deadline)
                    }
                }
            }
            else -> null
        }

        if (date != null) {
            outputFormat.format(date)
        } else {
            deadline
        }
    } catch (e: Exception) {
        deadline
    }
}