package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WeeklyReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyReviewDao {
    @Query("SELECT * FROM weekly_reviews ORDER BY weekStart DESC")
    fun getAllWeeklyReviews(): Flow<List<WeeklyReviewEntity>>

    @Query("SELECT * FROM weekly_reviews ORDER BY weekStart DESC")
    suspend fun getAllWeeklyReviewsList(): List<WeeklyReviewEntity>

    @Query("SELECT * FROM weekly_reviews WHERE id = :id LIMIT 1")
    suspend fun getWeeklyReviewById(id: Long): WeeklyReviewEntity?

    @Query("SELECT * FROM weekly_reviews WHERE weekStart = :weekStart LIMIT 1")
    suspend fun getWeeklyReviewByWeekStart(weekStart: String): WeeklyReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyReview(review: WeeklyReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyReviews(reviews: List<WeeklyReviewEntity>)

    @Update
    suspend fun updateWeeklyReview(review: WeeklyReviewEntity)

    @Delete
    suspend fun deleteWeeklyReview(review: WeeklyReviewEntity)
}
