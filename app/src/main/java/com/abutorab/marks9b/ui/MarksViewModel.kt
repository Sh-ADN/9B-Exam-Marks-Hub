package com.abutorab.marks9b.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abutorab.marks9b.data.local.entity.*
import com.abutorab.marks9b.data.repository.MarksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MarksViewModel(
    application: Application,
    private val repository: MarksRepository
) : AndroidViewModel(application) {
    fun getAllYears(): Flow<List<YearEntity>> = repository.getAllYears()
    fun insertYear(label: String) = viewModelScope.launch { repository.insertYear(YearEntity(label = label)) }
    fun deleteYear(year: YearEntity) = viewModelScope.launch { repository.deleteYear(year) }

    fun getTermsForYear(yearId: Int): Flow<List<TermEntity>> = repository.getTermsForYear(yearId)
    fun insertTerm(yearId: Int, label: String) = viewModelScope.launch { repository.insertTerm(TermEntity(yearId = yearId, label = label)) }
    fun deleteTerm(term: TermEntity) = viewModelScope.launch { repository.deleteTerm(term) }

    fun getStudentsForTerm(termId: Int): Flow<List<StudentEntity>> = repository.getStudentsForTerm(termId)
    fun insertStudent(student: StudentEntity) = viewModelScope.launch { repository.insertStudent(student) }
    fun deleteStudent(student: StudentEntity) = viewModelScope.launch { repository.deleteStudent(student) }

    fun getSubjectsForTerm(termId: Int): Flow<List<SubjectEntity>> = repository.getSubjectsForTerm(termId)
    fun insertSubject(subject: SubjectEntity) = viewModelScope.launch { repository.insertSubject(subject) }
    fun deleteSubject(subject: SubjectEntity) = viewModelScope.launch { repository.deleteSubject(subject) }

    private var undoAction: (suspend () -> Unit)? = null

    suspend fun snapshotAndDeleteYear(year: YearEntity) {
        val terms = repository.getTermsForYear(year.id).first()
        val students = mutableListOf<StudentEntity>()
        val subjects = mutableListOf<SubjectEntity>()
        for (term in terms) {
            students.addAll(repository.getStudentsForTerm(term.id).first())
            subjects.addAll(repository.getSubjectsForTerm(term.id).first())
        }
        undoAction = {
            repository.insertYear(year)
            terms.forEach { repository.insertTerm(it) }
            students.forEach { repository.insertStudent(it) }
            subjects.forEach { repository.insertSubject(it) }
        }
        repository.deleteYear(year)
    }

    suspend fun snapshotAndDeleteTerm(term: TermEntity) {
        val students = repository.getStudentsForTerm(term.id).first()
        val subjects = repository.getSubjectsForTerm(term.id).first()
        undoAction = {
            repository.insertTerm(term)
            students.forEach { repository.insertStudent(it) }
            subjects.forEach { repository.insertSubject(it) }
        }
        repository.deleteTerm(term)
    }

    suspend fun snapshotAndDeleteStudent(student: StudentEntity) {
        undoAction = { repository.insertStudent(student) }
        repository.deleteStudent(student)
    }

    suspend fun snapshotAndDeleteSubject(subject: SubjectEntity) {
        undoAction = { repository.insertSubject(subject) }
        repository.deleteSubject(subject)
    }

    fun undoLastDelete() {
        undoAction?.let { action ->
            viewModelScope.launch {
                action()
                undoAction = null
            }
        }
    }
}
