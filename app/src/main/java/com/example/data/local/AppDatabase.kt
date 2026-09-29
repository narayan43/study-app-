package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AttemptEntity
import com.example.data.model.MockTestResultEntity
import com.example.data.model.NoteEntity
import com.example.data.model.NoteQuestionCrossRef
import com.example.data.model.NoteUsageEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoQuestionCrossRef
import com.example.data.model.VideoUsageEntity
import com.example.data.model.WeeklyReviewEntity

@Database(
    entities = [
        QuestionEntity::class,
        AttemptEntity::class,
        WeeklyReviewEntity::class,
        MockTestResultEntity::class,
        NoteEntity::class,
        NoteQuestionCrossRef::class,
        NoteUsageEntity::class,
        VideoEntity::class,
        VideoQuestionCrossRef::class,
        VideoUsageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun attemptDao(): AttemptDao
    abstract fun weeklyReviewDao(): WeeklyReviewDao
    abstract fun mockTestResultDao(): MockTestResultDao
    abstract fun noteDao(): NoteDao
    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "upsi_prep_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
