package com.abutorab.marks9b.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abutorab.marks9b.data.local.entity.*
import com.abutorab.marks9b.data.repository.MarksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import android.content.Context
import com.abutorab.marks9b.data.local.PreferencesHelper
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

data class ActiveTermContext(
    val yearId: Int, val yearLabel: String,
    val termId: Int, val termLabel: String, val examPeriod: String,
    val studentCount: Int, val subjectCount: Int
)

class MarksViewModel(
    application: Application,
    private val repository: MarksRepository
) : AndroidViewModel(application) {

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun getActiveTermContext(context: Context): Flow<ActiveTermContext?> {
        val lastTermId = PreferencesHelper.getLastOpenedTerm(context) ?: return flowOf(null)
        return getTermById(lastTermId).flatMapLatest { term ->
            if (term == null) return@flatMapLatest flowOf(null)
            combine(
                getYearById(term.yearId),
                getStudentsForYear(term.yearId),
                getSubjectsForTerm(term.id)
            ) { year, students, subjects ->
                if (year == null) null
                else ActiveTermContext(
                    yearId = year.id,
                    yearLabel = year.label,
                    termId = term.id,
                    termLabel = term.label,
                    examPeriod = term.examPeriod,
                    studentCount = students.size,
                    subjectCount = subjects.size
                )
            }
        }
    }

    fun getAllYears(): Flow<List<YearEntity>> = repository.getAllYears()
    fun getYearById(yearId: Int): Flow<YearEntity?> = repository.getYearById(yearId)
    fun insertYear(label: String, sheetId: String?) = viewModelScope.launch { repository.insertYear(YearEntity(label = label, sheetId = sheetId)) }
    fun updateYear(year: YearEntity) = viewModelScope.launch { repository.updateYear(year) }
    fun deleteYear(year: YearEntity) = viewModelScope.launch { repository.deleteYear(year) }

    fun getTermsForYear(yearId: Int): Flow<List<TermEntity>> = repository.getTermsForYear(yearId)
    fun getTermById(termId: Int): Flow<TermEntity?> = repository.getTermById(termId)
    fun insertTerm(yearId: Int, label: String, examPeriod: String) = viewModelScope.launch {
        val termId = repository.insertTerm(TermEntity(yearId = yearId, label = label, examPeriod = examPeriod))
        com.abutorab.marks9b.data.local.entity.SubjectCatalog.subjects.forEach { data ->
            repository.insertSubject(
                SubjectEntity(
                    termId = termId.toInt(),
                    name = data.name,
                    sheetTabName = data.sheetTabName,
                    sheetRole = data.sheetRole,
                    applicabilityType = data.applicabilityType,
                    applicabilityValue = data.applicabilityValue,
                    fullMarks = data.fullMarks,
                    mcqMax = data.mcqMax,
                    writtenMax = data.writtenMax,
                    practicalMax = data.practicalMax
                )
            )
        }
    }
    fun updateTerm(term: TermEntity) = viewModelScope.launch { repository.updateTerm(term) }
    fun deleteTerm(term: TermEntity) = viewModelScope.launch { repository.deleteTerm(term) }

    fun getStudentsForYear(yearId: Int): Flow<List<StudentEntity>> = repository.getStudentsForYear(yearId)
    fun insertStudent(student: StudentEntity) = viewModelScope.launch { repository.insertStudent(student) }
    fun updateStudent(student: StudentEntity) = viewModelScope.launch { repository.updateStudent(student) }
    fun deleteStudent(student: StudentEntity) = viewModelScope.launch { repository.deleteStudent(student) }

    fun getSubjectsForTerm(termId: Int): Flow<List<SubjectEntity>> = repository.getSubjectsForTerm(termId)
    fun insertSubject(subject: SubjectEntity) = viewModelScope.launch { repository.insertSubject(subject) }
    fun updateSubject(subject: SubjectEntity) = viewModelScope.launch { repository.updateSubject(subject) }
    fun deleteSubject(subject: SubjectEntity) = viewModelScope.launch { repository.deleteSubject(subject) }

    fun getMarksForSubject(subjectId: Int): Flow<List<MarkEntity>> = repository.getMarksForSubject(subjectId)
    fun getMarksForTerm(termId: Int): Flow<List<MarkEntity>> = repository.getMarksForTerm(termId)
    fun saveMark(mark: MarkEntity) = viewModelScope.launch { 
        repository.saveMark(mark) 
    }

    fun saveMarks(marks: List<MarkEntity>) = viewModelScope.launch {
        repository.saveMarks(marks)
    }

    fun importStudentsFromCsv(yearId: Int, uri: android.net.Uri, context: android.content.Context) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                val lines = reader.readLines()
                if (lines.isNotEmpty()) {
                    for (i in 1 until lines.size) {
                        val line = lines[i]
                        val parts = line.split(",")
                        if (parts.size >= 5) {
                            val roll = parts[0].trim().toIntOrNull() ?: continue
                            val name = parts[1].trim()
                            val religion = parts[2].trim()
                            val group = parts[3].trim()
                            val optional = parts[4].trim()
                            repository.insertStudent(StudentEntity(yearId = yearId, roll = roll, name = name, religion = religion, group = group, optionalType = optional))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var undoAction: (suspend () -> Unit)? = null

    suspend fun snapshotAndDeleteYear(year: YearEntity) {
        val terms = repository.getTermsForYear(year.id).first()
        val students = repository.getStudentsForYear(year.id).first()
        val subjects = mutableListOf<SubjectEntity>()
        for (term in terms) {
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
        val subjects = repository.getSubjectsForTerm(term.id).first()
        undoAction = {
            repository.insertTerm(term)
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
