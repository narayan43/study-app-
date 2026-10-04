package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.components.AddContentSheetChrome
import com.example.ui.components.AddVideoSheet
import com.example.ui.components.CircularAddButton
import com.example.ui.components.DrillDownSelector
import com.example.ui.components.LinkedQuestionImportSheet
import com.example.ui.components.LinkedSingleQuestionSheet
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.OutlinedTextField

@Composable
fun ReelsScreen(
    viewModel: ExamPrepViewModel,
    onStartTestSlice: (TestSliceSource) -> Unit
) {
    val reelsFilter by viewModel.reelsFilter.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddSheet by remember { mutableStateOf(false) }
    var isSliceSelected by remember { mutableStateOf(false) }

    val showReels = (reelsFilter.chapter != null) || isSliceSelected

    BackHandler(enabled = showReels || reelsFilter.exam != null) {
        if (showReels) {
            isSliceSelected = false
            when {
                reelsFilter.chapter != null -> viewModel.setReelsFilter(reelsFilter.copy(chapter = null, topic = null))
                reelsFilter.subject != null -> viewModel.setReelsFilter(reelsFilter.copy(subject = null, chapter = null, topic = null))
                else -> viewModel.setReelsFilter(DrillDownFilter())
            }
        } else if (reelsFilter.subject != null) {
            viewModel.setReelsFilter(reelsFilter.copy(subject = null, chapter = null, topic = null))
        } else if (reelsFilter.exam != null) {
            viewModel.setReelsFilter(DrillDownFilter())
        }
    }

    if (showAddSheet) {
        AddVideoSheet(
            lockedFilter = reelsFilter,
            viewModel = viewModel,
            onDismiss = { showAddSheet = false }
        )
    }

    if (!showReels) {
        DrillDownSelector(
            title = "Reels: Choose Exam or Subject",
            exams = viewModel.listExams(),
            currentFilter = reelsFilter,
            getSubjects = { viewModel.listSubjects(it) },
            getChapters = { ex, sub -> viewModel.listChapters(ex, sub) },
            getTopics = { ex, sub, ch -> viewModel.listTopics(ex, sub, ch) },
            onFilterChanged = {
                viewModel.setReelsFilter(it)
                isSliceSelected = false
            },
            onSliceReady = {
                viewModel.setReelsFilter(it)
                isSliceSelected = true
            },
            onAddClicked = { showAddSheet = true }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularAddButton(
                        onClick = { showAddSheet = true },
                        contentDescription = "Add Reel"
                    )
                    OutlinedButton(
                        onClick = {
                            isSliceSelected = false
                            when {
                                reelsFilter.chapter != null -> viewModel.setReelsFilter(reelsFilter.copy(chapter = null, topic = null))
                                reelsFilter.subject != null -> viewModel.setReelsFilter(reelsFilter.copy(subject = null, chapter = null, topic = null))
                                else -> viewModel.setReelsFilter(DrillDownFilter())
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Change Filter", color = Color.White, fontWeight = FontWeight.Bold)
                    }
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

                // Top Floating Slice Filter Bar: Slim, translucent, unobtrusive
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color(0x66000000)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val label = when {
                            reelsFilter.chapter != null -> reelsFilter.chapter
                            reelsFilter.subject != null -> reelsFilter.subject
                            else -> reelsFilter.exam
                        }
                        Text(
                            text = "Reels • $label (${videos.size})",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularAddButton(
                                onClick = { showAddSheet = true },
                                contentDescription = "Add Reel",
                                modifier = Modifier.size(30.dp)
                            )
                            Button(
                                onClick = {
                                    isSliceSelected = false
                                    when {
                                        reelsFilter.chapter != null -> viewModel.setReelsFilter(reelsFilter.copy(chapter = null, topic = null))
                                        reelsFilter.subject != null -> viewModel.setReelsFilter(reelsFilter.copy(subject = null, chapter = null, topic = null))
                                        else -> viewModel.setReelsFilter(DrillDownFilter())
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
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
    val scope = rememberCoroutineScope()
    val openedAt = remember { System.currentTimeMillis() }
    var secondsPlayed by remember { mutableIntStateOf(0) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var showAddSingleSheet by remember { mutableStateOf(false) }

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

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete Reel?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Are you sure you want to delete \"${video.title}\"?\n\nThis removes its entry from videos/videos.csv, unlinks questions, and deletes the video file if it exists. Logs will not be touched.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                            exoPlayer?.release()
                            scope.launch {
                                viewModel.deleteVideo(video.videoId, video.videoPath)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Bottom Controls Overlay: Small, at the edges, so video stays mostly visible
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x99000000), Color(0xDD000000))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Actions row for questions: Import questions.csv & Add single question
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { showImportSheet = true },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xCC2A2A2A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import questions.csv", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    FilledTonalButton(
                        onClick = { showAddSingleSheet = true },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xCC2A2A2A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add single question", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = video.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${video.chapter} • ${video.subject}",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.closeVideo(video, openedAt, secondsPlayed, true)
                                onTestFromVideo()
                            },
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(17.dp)
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x55000000))
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete reel",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showImportSheet) {
            LinkedQuestionImportSheet(
                targetType = "Reel",
                targetTitle = video.title,
                targetId = video.videoId,
                viewModel = viewModel,
                onDismiss = { showImportSheet = false },
                onSuccess = { count ->
                    showImportSheet = false
                }
            )
        }

        if (showAddSingleSheet) {
            LinkedSingleQuestionSheet(
                targetType = "Reel",
                targetTitle = video.title,
                targetId = video.videoId,
                viewModel = viewModel,
                onDismiss = { showAddSingleSheet = false },
                onSuccess = { qId ->
                    showAddSingleSheet = false
                }
            )
        }
    }
}
