package com.abbie.alpvp.views

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abbie.alpvp.models.RewardModel
import com.abbie.alpvp.viewmodels.AppViewModelProvider
import com.abbie.alpvp.viewmodels.RewardsViewModel

private val AppGreen = Color(0xFF66A678)
private val AppBackground = Color(0xFFF5F7F5)
private val TextPrimary = Color(0xFF1A1C19)
private val TextSecondary = Color(0xFF757575)
private val YellowAccent = Color(0xFFFDD835)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTimer: () -> Unit = {},
    viewModel: RewardsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedReward by remember { mutableStateOf<RewardModel?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadRewards()
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
                        "Achievement Gallery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            RewardsBottomNavBar(
                currentRoute = "rewards",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateBack()
                        "timer" -> onNavigateToTimer()
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.earnedRewards.isEmpty() && uiState.lockedRewards.isEmpty() && !uiState.isLoading -> {
                    EmptyState()
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // udh dpt
                        items(uiState.earnedRewards) { reward ->
                            RewardCard(
                                reward = reward,
                                isEarned = true,
                                onClick = {
                                    selectedReward = reward
                                    showDetailDialog = true
                                }
                            )
                        }

                        // locked
                        items(uiState.lockedRewards) { reward ->
                            RewardCard(
                                reward = reward,
                                isEarned = false,
                                onClick = {
                                    selectedReward = reward
                                    showDetailDialog = true
                                }
                            )
                        }
                    }
                }
            }


            if (uiState.error != null && !uiState.isLoading) {
                ErrorState(
                    onRetry = { viewModel.loadRewards() }
                )
            }
        }

        if (showDetailDialog && selectedReward != null) {
            RewardDetailDialog(
                reward = selectedReward!!,
                isEarned = uiState.earnedRewards.contains(selectedReward),
                onDismiss = { showDetailDialog = false }
            )
        }
    }
}

@Composable
fun RewardCard(
    reward: RewardModel,
    isEarned: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isEarned) YellowAccent.copy(alpha = 0.15f) else Color.White
    val borderColor = if (isEarned) YellowAccent else Color(0xFFE0E0E0)
    val textAlpha = if (isEarned) 1f else 0.5f

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // img
            Icon(
                imageVector = getRewardIcon(reward.triggerType),
                contentDescription = reward.title,
                tint = if (isEarned) getRewardColor(reward.triggerType) else Color.LightGray,
                modifier = Modifier
                    .size(64.dp)
                    .alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // title
            Text(
                text = reward.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // desc
            Text(
                text = reward.description ?: if (isEarned) "Unlocked!" else "Locked",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha),
                maxLines = 2
            )
        }
    }
}

@Composable
fun RewardDetailDialog(
    reward: RewardModel,
    isEarned: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = getRewardIcon(reward.triggerType),
                    contentDescription = reward.title,
                    tint = if (isEarned) getRewardColor(reward.triggerType) else Color.LightGray,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isEarned) {
                    // yg udh dpt rewards detail
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppGreen.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Unlocked!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppGreen
                            )
                        }
                    }

                    Text(
                        text = reward.description ?: "You've earned this achievement!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                } else {
                    // LOCKED & HOW TO UNLOCK
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "How to Unlock",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = getUnlockDescription(reward.triggerType, reward.threshold),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
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
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = AppGreen)
            Text(
                text = "Loading rewards...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No rewards yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Keep completing tasks to unlock achievements!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Failed to load rewards",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please try again",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = AppGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

private fun getRewardIcon(triggerType: String): ImageVector {
    return when (triggerType) {
        "STREAK" -> Icons.Default.LocalFireDepartment
        "TASK_COUNT" -> Icons.Default.CheckCircle
        "DAILY_COMPLETION" -> Icons.Default.Star
        else -> Icons.Default.EmojiEvents
    }
}

private fun getRewardColor(triggerType: String): Color {
    return when (triggerType) {
        "STREAK" -> Color(0xFFFF7043)
        "TASK_COUNT" -> Color(0xFF66A678)
        "DAILY_COMPLETION" -> Color(0xFFFDD835)
        else -> Color(0xFF66A678)
    }
}

private fun getUnlockDescription(triggerType: String, threshold: Int): String {
    return when (triggerType) {
        "STREAK" -> "Complete your tasks for $threshold days in a row"
        "TASK_COUNT" -> "Complete a total of $threshold tasks"
        "DAILY_COMPLETION" -> "Complete all your tasks for the day"
        else -> "Complete the required actions to unlock"
    }
}

@Composable
private fun RewardsBottomNavBar(
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