package com.abbie.alpvp.views

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
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
import com.abbie.alpvp.models.ScheduleActivityModel
import com.abbie.alpvp.viewmodels.ActivityListViewModel
import com.abbie.alpvp.viewmodels.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val AppGreen = Color(0xFF66A678)
private val AppBackground = Color(0xFFF5F7F5)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1C19)
private val TextSecondary = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityListScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: ActivityListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val activities by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<ScheduleActivityModel?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadActivities()
    }

    fun openEditDialog(activity: ScheduleActivityModel) {
        activityToEdit = activity
        showEditDialog = true
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Manage Activities",
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Total Tasks: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "${activities.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (activities.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No activities yet today.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            items(activities) { activity ->
                ActivityCardItem(
                    activity = activity,
                    onDelete = { viewModel.deleteActivity(activity.id) },
                    onEdit = { openEditDialog(activity) }
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }

        if (showEditDialog && activityToEdit != null) {
            val act = activityToEdit!!
            val initialStart = formatIsoTimePretty(act.startTime)
            val initialEnd = formatIsoTimePretty(act.endTime)

            EditActivityDialog(
                iconName = act.iconName,
                initialStart = initialStart,
                initialEnd = initialEnd,
                initialDesc = act.description,
                onDismiss = { showEditDialog = false },
                onConfirm = { newStartIso, newEndIso, newDesc ->
                    viewModel.updateActivity(
                        id = act.id,
                        iconName = act.iconName,
                        startTime = newStartIso,
                        endTime = newEndIso,
                        description = newDesc
                    )
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun ActivityCardItem(
    activity: ScheduleActivityModel,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current

    val isExpired = isTimePassed(activity.endTime)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(
                        if (isExpired) Color.LightGray.copy(alpha = 0.3f) else AppGreen.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = formatIsoTimePretty(activity.startTime),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpired) Color.Gray else AppGreen
                )
                Text(
                    text = "|",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isExpired) Color.Gray else AppGreen.copy(alpha = 0.5f),
                    modifier = Modifier.height(10.dp)
                )
                Text(
                    text = formatIsoTimePretty(activity.endTime),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpired) Color.Gray else AppGreen
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.iconName.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isExpired) Color.Gray else TextPrimary
                )

                if (activity.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }

                if (isExpired) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row {
                IconButton(
                    onClick = {
                        if (isExpired) {
                            Toast.makeText(context, "Cannot edit completed activity", Toast.LENGTH_SHORT).show()
                        } else {
                            onEdit()
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isExpired) Color.LightGray.copy(alpha = 0.3f)
                            else Color(0xFFE3F2FD)
                        )
                ) {
                    Icon(
                        imageVector = if (isExpired) Icons.Default.Lock else Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = if (isExpired) Color.Gray else Color(0xFF1E88E5),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(15.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE))
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditActivityDialog(
    iconName: String,
    initialStart: String,
    initialEnd: String,
    initialDesc: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var startTime by remember { mutableStateOf(initialStart) }
    var endTime by remember { mutableStateOf(initialEnd) }
    var description by remember { mutableStateOf(initialDesc) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun showTimePicker(initial: String, onTimeSelected: (String) -> Unit) {
        val parts = initial.split(":")
        val h = if (parts.size > 1) parts[0].toInt() else 8
        val m = if (parts.size > 1) parts[1].toInt() else 0

        TimePickerDialog(context, { _, hour, minute ->
            onTimeSelected(String.format("%02d:%02d", hour, minute))
            errorMessage = null
        }, h, m, true).show()
    }

    fun validateAndSubmit() {
        val startMins = startTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        val endMins = endTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }

        if (endMins <= startMins) {
            errorMessage = "End time must be later than start time!"
        } else {
            val dateNow = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val isoStart = "${dateNow}T${startTime}:00.000Z"
            val isoEnd = "${dateNow}T${endTime}:00.000Z"
            onConfirm(isoStart, isoEnd, description)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Edit Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Editing: ${iconName.replaceFirstChar { it.uppercase() }}", color = AppGreen, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))
                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { showTimePicker(startTime) { startTime = it } }, modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Start", fontSize = 10.sp, color = TextSecondary)
                            Text(startTime, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { showTimePicker(endTime) { endTime = it } }, modifier = Modifier.weight(1f)) {
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
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { validateAndSubmit() }, colors = ButtonDefaults.buttonColors(containerColor = AppGreen), shape = RoundedCornerShape(8.dp)) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

fun formatIsoTimePretty(isoString: String): String {
    return try {
        if (isoString.contains("T")) isoString.split("T")[1].substring(0, 5) else isoString
    } catch (e: Exception) { "" }
}

fun isTimePassed(isoEndTime: String): Boolean {
    return try {
        if (!isoEndTime.contains("T")) return false

        val timePart = isoEndTime.split("T")[1].substring(0, 5)
        val (endH, endM) = timePart.split(":").map { it.toInt() }

        val endTimeInMinutes = endH * 60 + endM

        val now = Calendar.getInstance()
        val currentH = now.get(Calendar.HOUR_OF_DAY)
        val currentM = now.get(Calendar.MINUTE)

        val currentTimeInMinutes = currentH * 60 + currentM

        currentTimeInMinutes >= endTimeInMinutes

    } catch (e: Exception) {
        false
    }
}