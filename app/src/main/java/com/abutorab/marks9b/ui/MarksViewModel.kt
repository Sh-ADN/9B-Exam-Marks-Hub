package com.abutorab.marks9b.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abutorab.marks9b.data.local.entity.*
import com.abutorab.marks9b.data.repository.MarksRepository
import kotlinx.coroutines.flow.Flow
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
}
