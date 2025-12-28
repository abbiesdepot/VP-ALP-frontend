package com.abbie.alpvp.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abbie.alpvp.viewmodels.PatternUiState
import com.abbie.alpvp.viewmodels.PatternViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val GameBgColor = Color(0xFFF5F7F5)
private val AppGreen = Color(0xFF66A678)
// Warna pastel cerah agar menarik untuk anak
private val TextPrimary = Color(0xFF1A1C19)
private val TileColor1 = Color(0xFF64B5F6) // Blue
private val TileColor2 = Color(0xFF81C784) // Green
private val TileColor3 = Color(0xFFFFB74D) // Orange
private val TileColor4 = Color(0xFFBA68C8) // Purple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternGameScreen(
    onNavigateBack: () -> Unit,
    viewModel: PatternViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Auto start saat masuk layar
    LaunchedEffect(Unit) {
        if (state.mode == PatternUiState.Mode.IDLE) {
            viewModel.startGame()
        }
    }

    Scaffold(
        containerColor = GameBgColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Brain Break", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = GameBgColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Score Badge
            Surface(
                color = AppGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AppGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Score: ${state.score}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppGreen
                    )
                }
            }

            // Message Text
            Text(
                text = state.message ?: "Relax & Play",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (state.mode == PatternUiState.Mode.FAIL) Color.Red else TextPrimary,
                modifier = Modifier.height(40.dp) // Fixed height agar UI tidak lompat
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Grid Area
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ReactiveGameTile(0, TileColor1, state, viewModel)
                        ReactiveGameTile(1, TileColor2, state, viewModel)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ReactiveGameTile(2, TileColor3, state, viewModel)
                        ReactiveGameTile(3, TileColor4, state, viewModel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Replay Button (Hanya muncul jika user bingung/salah)
            OutlinedButton(
                onClick = { viewModel.replay() },
                enabled = state.mode == PatternUiState.Mode.INPUT || state.mode == PatternUiState.Mode.FAIL,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show Pattern Again")
            }
        }
    }
}

@Composable
fun ReactiveGameTile(
    index: Int,
    baseColor: Color,
    state: PatternUiState,
    viewModel: PatternViewModel
) {
    val haptic = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val tileSize = (screenWidth - 100.dp) / 2
    val scope = rememberCoroutineScope()

    // Apakah tile ini sedang disorot oleh sistem (Show Mode)?
    val isSystemHighlighted = state.mode == PatternUiState.Mode.SHOWING && state.sequence.getOrNull(state.showingIndex) == index

    // Apakah tile ini boleh diklik?
    val isEnabled = state.mode == PatternUiState.Mode.INPUT

    // State lokal untuk efek "Flash" saat diklik user
    var isUserPressed by remember { mutableStateOf(false) }

    // Logika Animasi
    val targetScale = if (isSystemHighlighted || isUserPressed) 0.90f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "scale"
    )

    // Logika Warna: Saat ditekan/highlight, warna jadi lebih terang (putih campur warna)
    val targetColor = if (isSystemHighlighted || isUserPressed) baseColor.copy(alpha = 0.6f) else baseColor
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(150),
        label = "color"
    )

    Box(
        modifier = Modifier
            .size(tileSize)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(animatedColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (isEnabled) {
                            // 1. Efek Haptic (Getaran)
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)

                            // 2. Efek Visual (Flash & Shrink sebentar)
                            scope.launch {
                                isUserPressed = true
                                delay(100) // Durasi "Flash"
                                isUserPressed = false
                            }

                            // 3. Kirim logika ke ViewModel
                            viewModel.onTileTapped(index)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner circle decorative (biar kayak tombol arcade)
        Box(
            modifier = Modifier
                .size(tileSize * 0.6f)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        )

        // Flash overlay extra (opsional, biar makin 'pop')
        if (isUserPressed || isSystemHighlighted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    }
}