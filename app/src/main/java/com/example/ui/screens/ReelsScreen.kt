package com.example.ui.screens

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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrillDownFilter
import com.example.data.model.TestSliceSource
import com.example.data.model.VideoItem
import com.example.ui.components.DrillDownSelector
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.delay

@Composable
fun ReelsScreen(
    viewModel: ExamPrepViewModel,
    onStartTestSlice: (TestSliceSource) -> Unit
) {
    val drillFilter by viewModel.drillFilter.collectAsState()

    if (drillFilter.exam == null || drillFilter.subject == null || drillFilter.chapter == null) {
        DrillDownSelector(
            title = "Reels: Choose Subject & Chapter",
            exams = viewModel.listExams(),
            currentFilter = drillFilter,
            getSubjects = { viewModel.listSubjects(it) },
            getChapters = { ex, sub -> viewModel.listChapters(ex, sub) },
            getTopics = { ex, sub, ch -> viewModel.listTopics(ex, sub, ch) },
            onFilterChanged = { viewModel.setFilter(it) },
            onSliceReady = { viewModel.setFilter(it) }
        )
    } else {
        // Videos filtered strictly for this slice ONLY (never mix other subjects)
        val videos = remember(drillFilter) { viewModel.videosForSlice(drillFilter) }

        if (videos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No video reels found for ${drillFilter.chapter}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.setFilter(drillFilter.copy(chapter = null))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Choose Another Chapter")
                }
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { videos.size })

            Box(modifier = Modifier.fillMaxSize()) {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val video = videos[page]
                    ReelVideoItem(
                        video = video,
                        onTestFromVideo = {
                            onStartTestSlice(TestSliceSource.VideoRevision(video.videoId, video.title))
                        },
                        onLogUsage = { openedAt, timeSpent, startedTest ->
                            viewModel.closeVideo(video, openedAt, timeSpent, startedTest)
                        }
                    )
                }

                // Top Floating Slice Filter Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Reels: ${drillFilter.chapter}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${drillFilter.exam} • ${drillFilter.subject}",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.setFilter(drillFilter.copy(chapter = null))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Switch", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReelVideoItem(
    video: VideoItem,
    onTestFromVideo: () -> Unit,
    onLogUsage: (openedAt: Long, timeSpentSec: Int, startedTest: Boolean) -> Unit
) {
    val openedAt = remember { System.currentTimeMillis() }
    var isPlaying by remember { mutableStateOf(true) }
    var currentSeconds by remember { mutableFloatStateOf(0f) }
    val totalSeconds = video.durationSec.toFloat().coerceAtLeast(60f)

    LaunchedEffect(isPlaying) {
        while (isPlaying && currentSeconds < totalSeconds) {
            delay(1000)
            currentSeconds = (currentSeconds + 1f).coerceAtMost(totalSeconds)
        }
    }

    DisposableEffect(video.videoId) {
        onDispose {
            onLogUsage(openedAt, currentSeconds.toInt(), false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        // Video Visualizer / Canvas Center
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D47A1).copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { isPlaying = !isPlaying }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = video.title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${video.chapter} • ${video.topic}",
                color = Color(0xFF90CAF9),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Path: ${video.videoPath}",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Bottom Controls Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f), Color.Black)
                    )
                )
                .padding(20.dp)
        ) {
            // Scrubber
            Slider(
                value = currentSeconds,
                onValueChange = { currentSeconds = it },
                valueRange = 0f..totalSeconds,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF42A5F5),
                    activeTrackColor = Color(0xFF42A5F5),
                    inactiveTrackColor = Color.DarkGray
                ),
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val curMin = currentSeconds.toInt() / 60
                val curSec = currentSeconds.toInt() % 60
                val totMin = totalSeconds.toInt() / 60
                val totSec = totalSeconds.toInt() % 60

                Text(
                    text = String.format("%02d:%02d / %02d:%02d", curMin, curSec, totMin, totSec),
                    color = Color.LightGray,
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    text = "Swipe up for next reel",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Test from this video (started_test = 1)
            Button(
                onClick = {
                    onLogUsage(openedAt, currentSeconds.toInt(), true)
                    onTestFromVideo()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test from this video", fontWeight = FontWeight.Bold)
            }
        }
    }
}
