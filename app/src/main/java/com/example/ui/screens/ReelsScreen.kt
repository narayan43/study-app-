package com.example.ui.screens

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
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
    val reelsFilter by viewModel.reelsFilter.collectAsState()

    if (reelsFilter.exam == null) {
        DrillDownSelector(
            title = "Reels: Choose Exam or Subject",
            exams = viewModel.listExams(),
            currentFilter = reelsFilter,
            getSubjects = { viewModel.listSubjects(it) },
            getChapters = { ex, sub -> viewModel.listChapters(ex, sub) },
            getTopics = { ex, sub, ch -> viewModel.listTopics(ex, sub, ch) },
            onFilterChanged = { viewModel.setReelsFilter(it) },
            onSliceReady = { viewModel.setReelsFilter(it) }
        )
    } else {
        val rawVideos = remember(reelsFilter) { viewModel.videosForSlice(reelsFilter) }
        val videos = remember(rawVideos, reelsFilter) {
            when {
                reelsFilter.chapter != null -> rawVideos
                reelsFilter.subject != null -> rawVideos.sortedBy { it.chapter }
                else -> rawVideos.sortedWith(compareBy({ it.subject }, { it.chapter }))
            }
        }

        if (videos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No video reels found matching filter.",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.setReelsFilter(DrillDownFilter())
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Change Filter", color = Color.White, fontWeight = FontWeight.Bold)
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
                    ReelVideoPlayerItem(
                        video = video,
                        viewModel = viewModel,
                        onTestFromVideo = {
                            onStartTestSlice(TestSliceSource.VideoRevision(video.videoId, video.title))
                        }
                    )
                }

                // Top Floating Slice Filter Bar (on #000000 55% scrim)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color(0x8C000000)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val label = when {
                                reelsFilter.chapter != null -> "Chapter: ${reelsFilter.chapter}"
                                reelsFilter.subject != null -> "Subject: ${reelsFilter.subject}"
                                else -> "Exam: ${reelsFilter.exam}"
                            }
                            Text(
                                text = "Reels • $label",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${videos.size} reel(s) in feed",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.setReelsFilter(DrillDownFilter())
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Switch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ReelVideoPlayerItem(
    video: VideoItem,
    viewModel: ExamPrepViewModel,
    onTestFromVideo: () -> Unit
) {
    val context = LocalContext.current
    val openedAt = remember { System.currentTimeMillis() }
    var secondsPlayed by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsPlayed++
        }
    }

    val videoUri = remember(video.videoPath) {
        viewModel.resolveMediaUri(video.videoPath)
    }

    val exoPlayer = remember(video.videoId, videoUri) {
        if (videoUri != null) {
            try {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(videoUri))
                    prepare()
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ONE
                }
            } catch (_: Exception) {
                null
            }
        } else null
    }

    DisposableEffect(video.videoId) {
        onDispose {
            exoPlayer?.release()
            viewModel.closeVideo(video, openedAt, secondsPlayed, false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (videoUri != null && exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Missing file on #000000 55% scrim
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0x8C000000), shape = RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = video.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${video.subject} • ${video.chapter}",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Path: ${video.videoPath} (file not found)",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Bottom Controls Overlay (#000000 55% scrim)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x8C000000), Color(0xCC000000))
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = video.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${video.subject} • ${video.chapter} • ${video.durationSec / 60} mins",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Test from this video (Primary / OnPrimary)
            Button(
                onClick = {
                    viewModel.closeVideo(video, openedAt, secondsPlayed, true)
                    onTestFromVideo()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test from this video", fontWeight = FontWeight.Bold)
            }
        }
    }
}
