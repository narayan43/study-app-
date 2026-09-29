package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.VideoEntity
import com.example.data.model.VideoQuestionCrossRef
import com.example.data.model.VideoUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY title ASC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE subject = :subject ORDER BY title ASC")
    fun getVideosBySubject(subject: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoUsage(usage: VideoUsageEntity)

    @Query("SELECT * FROM video_usage ORDER BY timestamp DESC")
    fun getAllVideoUsages(): Flow<List<VideoUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoQuestions(links: List<VideoQuestionCrossRef>)

    @Query("SELECT questionId FROM video_questions WHERE videoId = :videoId")
    suspend fun getQuestionIdsForVideo(videoId: String): List<String>

    @Query("SELECT videoId FROM video_questions WHERE questionId = :questionId")
    suspend fun getVideoIdsForQuestion(questionId: String): List<String>

    @Query("SELECT * FROM video_questions")
    suspend fun getAllVideoQuestionLinks(): List<VideoQuestionCrossRef>
}
