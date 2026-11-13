package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PaletteAmber
import com.example.ui.theme.PaletteCyan
import com.example.ui.theme.PaletteEmerald
import com.example.ui.theme.PaletteIndigo
import com.example.ui.theme.PaletteMagenta
import com.example.ui.theme.PaletteOrange
import com.example.ui.theme.PaletteRose
import com.example.ui.theme.PaletteSky
import com.example.ui.theme.PaletteViolet
import kotlinx.coroutines.delay
import java.util.Locale

private data class VideoScene(
    val title: String,
    val subtitle: String,
    val highlight: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color
)

@Composable
fun StartupVideoScreen(
    onDismiss: () -> Unit
) {
    val scenes = remember {
        listOf(
            VideoScene(
                title = "Welcome to My bills reminder",
                subtitle = "Everything in the app is visible and under the cover",
                highlight = "Full control over every upcoming bill deadline",
                icon = Icons.Default.PlayArrow,
                primaryColor = PaletteIndigo,
                secondaryColor = PaletteCyan
            ),
            VideoScene(
                title = "Multi-Color Calendar View",
                subtitle = "Every section has its own distinct color",
                highlight = "Spot due today, upcoming, and overdue bills instantly",
                icon = Icons.Default.CalendarMonth,
                primaryColor = PaletteSky,
                secondaryColor = PaletteViolet
            ),
            VideoScene(
                title = "40 Interactive Smart Features",
                subtitle = "Calculators, splitters, budget tools & quick actions",
                highlight = "Tap any feature card to run live tools & toggles",
                icon = Icons.Default.AutoAwesome,
                primaryColor = PaletteMagenta,
                secondaryColor = PaletteRose
            ),
            VideoScene(
                title = "10 Background Engine Tasks",
                subtitle = "Automatic overdue scanning, digest & cache cleanup",
                highlight = "Runs continuously in the background while active",
                icon = Icons.Default.NotificationsActive,
                primaryColor = PaletteOrange,
                secondaryColor = PaletteAmber
            ),
            VideoScene(
                title = "Cloud Sync, Room DB & Downloads",
                subtitle = "Save CSV & JSON backups directly to Downloads",
                highlight = "Ready for Guest & Google users alike!",
                icon = Icons.Default.Download,
                primaryColor = PaletteEmerald,
                secondaryColor = PaletteSky
            )
        )
    }

    var currentSceneIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var sceneProgress by remember { mutableFloatStateOf(0f) }

    val totalSeconds = scenes.size * 3
    val currentSecond = (currentSceneIndex * 3 + (sceneProgress * 3)).toInt().coerceAtMost(totalSeconds)

    // Auto-play video timeline loop immediately upon sign-in
    LaunchedEffect(isPlaying, currentSceneIndex) {
        if (isPlaying) {
            val steps = 30
            val stepDelay = 100L // 3 seconds per scene
            val startStep = (sceneProgress * steps).toInt()
            for (step in startStep..steps) {
                if (!isPlaying) break
                sceneProgress = step / steps.toFloat()
                delay(stepDelay)
            }
            if (isPlaying) {
                if (currentSceneIndex < scenes.lastIndex) {
                    currentSceneIndex++
                    sceneProgress = 0f
                } else {
                    // Loop back or pause cleanly on final frame
                    currentSceneIndex = 0
                    sceneProgress = 0f
                }
            }
        }
    }

    val activeScene = scenes[currentSceneIndex]

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            activeScene.primaryColor.copy(alpha = 0.22f),
                            activeScene.secondaryColor.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("startup_video_card"),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Video Player Viewport
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(activeScene.primaryColor, activeScene.secondaryColor)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Animated motion rings in background
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pulse = 0.6f + sceneProgress * 0.45f
                            drawCircle(
                                color = Color.White.copy(alpha = 0.10f),
                                radius = size.minDimension * 0.48f * pulse,
                                center = Offset(size.width * 0.5f, size.height * 0.46f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.14f),
                                radius = size.minDimension * 0.30f * (1.25f - sceneProgress * 0.25f),
                                center = Offset(size.width * 0.2f, size.height * 0.2f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.12f),
                                radius = size.minDimension * 0.24f * pulse,
                                center = Offset(size.width * 0.85f, size.height * 0.8f)
                            )
                        }

                        // Top overlay badge: LIVE VIDEO INTRO
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) Color(0xFF4ADE80) else Color(0xFFFBBF24))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlaying) "PLAYING INTRO • ${currentSceneIndex + 1}/${scenes.size}" else "PAUSED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        // Skip top-right
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "Skip",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Animated Scene Content
                        AnimatedContent(
                            targetState = activeScene,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "video_scene_transition"
                        ) { scene ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = scene.icon,
                                        contentDescription = scene.title,
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = scene.title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = scene.highlight,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.92f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Video Scrubber & Controls
                    LinearProgressIndicator(
                        progress = { (currentSceneIndex + sceneProgress) / scenes.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = activeScene.primaryColor,
                        trackColor = activeScene.primaryColor.copy(alpha = 0.18f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format(Locale.US, "00:%02d / 00:%02d", currentSecond, totalSeconds),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    currentSceneIndex = 0
                                    sceneProgress = 0f
                                    isPlaying = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Restart Video",
                                    tint = activeScene.primaryColor
                                )
                            }
                            IconButton(
                                onClick = { isPlaying = !isPlaying }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = activeScene.primaryColor
                                )
                            }
                            IconButton(
                                onClick = {
                                    currentSceneIndex = (currentSceneIndex + 1) % scenes.size
                                    sceneProgress = 0f
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next Scene",
                                    tint = activeScene.primaryColor
                                )
                            }
                        }
                    }

                    // Scene selector dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        scenes.forEachIndexed { idx, scene ->
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(if (idx == currentSceneIndex) 24.dp else 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (idx == currentSceneIndex) scene.primaryColor
                                        else scene.primaryColor.copy(alpha = 0.25f)
                                    )
                                    .clickable {
                                        currentSceneIndex = idx
                                        sceneProgress = 0f
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // App Title & Mandatory Tagline
                    Text(
                        text = "My bills reminder",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = activeScene.primaryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Everything in the app is visible and under the cover",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = activeScene.primaryColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("startup_video_enter_button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeScene.primaryColor
                        )
                    ) {
                        Text(
                            text = "Enter App Now",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
