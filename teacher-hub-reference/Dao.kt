package com.abutorab.teacher.hub.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Subjects
    @Query("SELECT * FROM subjects ORDER BY title ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    @Query("SELECT COUNT(*) FROM students WHERE year = :year")
    suspend fun getStudentCount(year: Int): Int

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCountGlobal(): Int

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

    @Query("DELETE FROM students WHERE year = :year")
    suspend fun deleteAllStudents(year: Int)

    @Query("DELETE FROM marks WHERE year = :year AND term = :term")
    suspend fun deleteAllMarks(year: Int, term: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<MarkEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjectsReplace(subjects: List<SubjectEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentsReplace(students: List<StudentEntity>)

    // Students
    @Query("SELECT * FROM students WHERE year = :year ORDER BY rollNumber ASC")
    fun getStudentsByYear(year: Int): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)
    
    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Query("SELECT * FROM students WHERE rollNumber = :roll AND year = :year")
    suspend fun getStudentByRoll(roll: Int, year: Int): StudentEntity?

    // Marks
    @Query("SELECT * FROM marks WHERE subjectId = :subjectId AND year = :year AND term = :term")
    fun getMarksForSubject(subjectId: String, year: Int, term: String): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks WHERE rollNumber = :roll AND year = :year AND term = :term")
    fun getMarksForStudent(roll: Int, year: Int, term: String): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks WHERE year = :year AND term = :term")
    fun getAllMarks(year: Int, term: String): Flow<List<MarkEntity>>

    @Query("SELECT * FROM marks")
    fun getAllMarksGlobal(): Flow<List<MarkEntity>>

    @Query("SELECT * FROM students")
    fun getAllStudentsGlobal(): Flow<List<StudentEntity>>

    @Query("DELETE FROM students")
    suspend fun deleteAllStudentsGlobal()

    @Query("DELETE FROM marks")
    suspend fun deleteAllMarksGlobal()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMark(mark: MarkEntity)
}
