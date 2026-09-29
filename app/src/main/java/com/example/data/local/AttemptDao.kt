package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {
    @Query("SELECT * FROM attempts ORDER BY attemptedAt DESC")
    fun getAllAttempts(): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts ORDER BY attemptedAt DESC")
    suspend fun getAllAttemptsList(): List<AttemptEntity>

    @Query("SELECT * FROM attempts WHERE questionId = :questionId ORDER BY attemptedAt DESC")
    fun getAttemptsForQuestion(questionId: String): Flow<List<AttemptEntity>>

    @Query("SELECT COUNT(*) FROM attempts")
    fun getTotalAttemptCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM attempts WHERE isCorrect = 1")
    fun getCorrectAttemptCount(): Flow<Int>

    @Query("SELECT DISTINCT questionId FROM attempts WHERE isCorrect = 0")
    fun getIncorrectQuestionIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: AttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempts(attempts: List<AttemptEntity>)

    @Query("DELETE FROM attempts")
    suspend fun clearAllAttempts()
}
