package com.abutorab.marks9b.data.local.dao

import androidx.room.*
import com.abutorab.marks9b.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface YearDao {
    @Query("SELECT * FROM years")
    fun getAllYears(): Flow<List<YearEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(year: YearEntity)

    @Update
    suspend fun update(year: YearEntity)

    @Delete
    suspend fun delete(year: YearEntity)
}

@Dao
interface TermDao {
    @Query("SELECT * FROM terms WHERE yearId = :yearId")
    fun getTermsForYear(yearId: Int): Flow<List<TermEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(term: TermEntity)

    @Update
    suspend fun update(term: TermEntity)

    @Delete
    suspend fun delete(term: TermEntity)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE termId = :termId ORDER BY roll ASC")
    fun getStudentsForTerm(termId: Int): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity)

    @Update
    suspend fun update(student: StudentEntity)

    @Delete
    suspend fun delete(student: StudentEntity)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE termId = :termId")
    fun getSubjectsForTerm(termId: Int): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: SubjectEntity)

    @Update
    suspend fun update(subject: SubjectEntity)

    @Delete
    suspend fun delete(subject: SubjectEntity)
}
