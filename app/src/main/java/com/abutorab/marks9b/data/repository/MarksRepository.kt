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
    suspend fun insertTerm(term: TermEntity) = termDao.insert(term)
    suspend fun updateTerm(term: TermEntity) = termDao.update(term)
    suspend fun deleteTerm(term: TermEntity) = termDao.delete(term)

    fun getStudentsForTerm(termId: Int): Flow<List<StudentEntity>> = studentDao.getStudentsForTerm(termId)
    suspend fun insertStudent(student: StudentEntity) = studentDao.insert(student)
    suspend fun updateStudent(student: StudentEntity) = studentDao.update(student)
    suspend fun deleteStudent(student: StudentEntity) = studentDao.delete(student)

    fun getSubjectsForTerm(termId: Int): Flow<List<SubjectEntity>> = subjectDao.getSubjectsForTerm(termId)
    suspend fun insertSubject(subject: SubjectEntity) = subjectDao.insert(subject)
    suspend fun updateSubject(subject: SubjectEntity) = subjectDao.update(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = subjectDao.delete(subject)

    fun getMarksForSubject(subjectId: Int): Flow<List<MarkEntity>> = markDao.getMarksForSubject(subjectId)
    suspend fun saveMark(studentId: Int, subjectId: Int, marksObtained: Int) {
        val mark = MarkEntity(studentId = studentId, subjectId = subjectId, marksObtained = marksObtained)
        markDao.upsertMark(mark)
    }
}
