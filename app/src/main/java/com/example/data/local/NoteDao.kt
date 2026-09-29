package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NoteEntity
import com.example.data.model.NoteQuestionCrossRef
import com.example.data.model.NoteUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY title ASC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE subject = :subject ORDER BY title ASC")
    fun getNotesBySubject(subject: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("UPDATE notes SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteUsage(usage: NoteUsageEntity)

    @Query("SELECT * FROM notes_usage ORDER BY timestamp DESC")
    fun getAllNoteUsages(): Flow<List<NoteUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteQuestions(links: List<NoteQuestionCrossRef>)

    @Query("SELECT questionId FROM note_questions WHERE noteId = :noteId")
    suspend fun getQuestionIdsForNote(noteId: String): List<String>

    @Query("SELECT noteId FROM note_questions WHERE questionId = :questionId")
    suspend fun getNoteIdsForQuestion(questionId: String): List<String>

    @Query("SELECT * FROM note_questions")
    suspend fun getAllNoteQuestionLinks(): List<NoteQuestionCrossRef>
}
