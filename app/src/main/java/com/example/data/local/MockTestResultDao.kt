package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MockTestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MockTestResultDao {
    @Query("SELECT * FROM mock_test_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<MockTestResultEntity>>

    @Query("SELECT * FROM mock_test_results WHERE id = :id LIMIT 1")
    suspend fun getResultById(id: String): MockTestResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: MockTestResultEntity)

    @Query("SELECT COUNT(*) FROM mock_test_results")
    fun getTestCount(): Flow<Int>
}
