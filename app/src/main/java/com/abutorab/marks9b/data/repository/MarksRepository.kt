package com.abutorab.marks9b.data.repository

import com.abutorab.marks9b.data.local.dao.*
import com.abutorab.marks9b.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class MarksRepository(
    private val yearDao: YearDao,
    private val termDao: TermDao,
    private val studentDao: StudentDao,
    private val subjectDao: SubjectDao,
    private val markDao: MarkDao
) {
    fun getAllYears(): Flow<List<YearEntity>> = yearDao.getAllYears()
    suspend fun insertYear(year: YearEntity) = yearDao.insert(year)
    suspend fun updateYear(year: YearEntity) = yearDao.update(year)
    suspend fun deleteYear(year: YearEntity) = yearDao.delete(year)

    fun getTermsForYear(yearId: Int): Flow<List<TermEntity>> = termDao.getTermsForYear(yearId)
    fun getTermById(termId: Int): Flow<TermEntity?> = termDao.getTermById(termId)
    suspend fun insertTerm(term: TermEntity): Long = termDao.insert(term)
    suspend fun updateTerm(term: TermEntity) = termDao.update(term)
    suspend fun deleteTerm(term: TermEntity) = termDao.delete(term)

    fun getStudentsForYear(yearId: Int): Flow<List<StudentEntity>> = studentDao.getStudentsForYear(yearId)
    suspend fun insertStudent(student: StudentEntity) = studentDao.insert(student)
    suspend fun updateStudent(student: StudentEntity) = studentDao.update(student)
    suspend fun deleteStudent(student: StudentEntity) = studentDao.delete(student)

    fun getSubjectsForTerm(termId: Int): Flow<List<SubjectEntity>> = subjectDao.getSubjectsForTerm(termId)
    suspend fun insertSubject(subject: SubjectEntity) = subjectDao.insert(subject)
    suspend fun updateSubject(subject: SubjectEntity) = subjectDao.update(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = subjectDao.delete(subject)

    fun getMarksForSubject(subjectId: Int): Flow<List<MarkEntity>> = markDao.getMarksForSubject(subjectId)
    suspend fun saveMark(mark: MarkEntity) {
        markDao.upsertMark(mark)
    }
}
