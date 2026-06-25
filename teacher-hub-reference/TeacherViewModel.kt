package com.abutorab.teacher.hub.domain

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abutorab.teacher.hub.data.AppRepository
import com.abutorab.teacher.hub.data.MarkEntity
import com.abutorab.teacher.hub.data.StudentEntity
import com.abutorab.teacher.hub.data.SubjectEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.round

// ... (SubjectResult and other data classes remain) ...

data class SubjectResult(
    val subject: SubjectEntity,
    val mcq: Int?,
    val written: Int?,
    val practical: Int?
) {
    val total get() = (mcq ?: 0) + (written ?: 0) + (practical ?: 0)
    val grade get() = calculateGrade(total, subject.maxMarks)
}

data class TabulationRow(
    val student: StudentEntity,
    val results: Map<String, SubjectResult>, // Key is SubjectEntity.id
    val totalMarks: Int,
    val finalGpa: Double,
    val finalGrade: String,
    val failedSubjectCount: Int = 0,
    val meritPosition: Int = 0 // Computed later
)

data class StudentWithMark(
    val student: StudentEntity,
    val mark: MarkEntity?
)

class TeacherViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    private val _ardhoPercent = MutableStateFlow(sharedPrefs.getInt("somonnito_ardho_percent", 50))
    
    fun notifySettingsChanged() {
        _ardhoPercent.value = sharedPrefs.getInt("somonnito_ardho_percent", 50)
    }

    val syncManager = com.abutorab.teacher.hub.sync.SyncManager(repository)


    private val themePreference = com.abutorab.teacher.hub.util.ThemePreference.getInstance(application)
    val themeState = themePreference.themeFlow

    fun toggleTheme(isCurrentlyDark: Boolean) {
        val newTheme = if (isCurrentlyDark) "light" else "dark"
        themePreference.setTheme(newTheme)
    }

    // --- GLOBAL SCOPE STATE ---
    private val _selectedYear = MutableStateFlow(2026)
    val selectedYear = _selectedYear.asStateFlow()

    private val _selectedTerm = MutableStateFlow("ARDHOBARSHIK")
    val selectedTerm = _selectedTerm.asStateFlow()

    fun setYearAndTerm(year: Int, term: String) {
        _selectedYear.value = year
        _selectedTerm.value = term
    }

    // Global
    val allSubjects = repository.allSubjects.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allStudents = _selectedYear.flatMapLatest { year ->
        repository.getStudentsByYear(year)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allMarks = combine(_selectedYear, _selectedTerm, _ardhoPercent, ::Triple)
        .flatMapLatest { (year, term, ardhoPct) ->
            if (term == "SOMONNITO") {
                combine(
                    repository.getAllMarks(year, "ARDHOBARSHIK"),
                    repository.getAllMarks(year, "BARSHIK"),
                    allStudents,
                    allSubjects
                ) { ardhoMarks, barshikMarks, students, subjectsList ->
                    val combinedMarks = mutableListOf<MarkEntity>()
                    for (student in students) {
                        for (subj in subjectsList) {
                            val computed = com.abutorab.teacher.hub.data.CalculationUtils.getSomonnitoMarks(
                                year = year,
                                rollNumber = student.rollNumber,
                                subjectId = subj.id,
                                ardhoPercent = ardhoPct,
                                ardhoMarks = ardhoMarks,
                                barshikMarks = barshikMarks
                            )
                            if (computed != null) {
                                combinedMarks.add(computed)
                            }
                        }
                    }
                    combinedMarks
                }
            } else {
                repository.getAllMarks(year, term)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            repository.createInitialDataIfEmpty()
        }
    }

    // --- STUDENTS DIRECTORY ---
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery = _studentSearchQuery.asStateFlow()
    
    val filteredStudents = combine(allStudents, _studentSearchQuery) { students, query ->
        if (query.isBlank()) {
            students
        } else {
            students.filter {
                it.name.contains(query, ignoreCase = true) || 
                it.rollNumber.toString().contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun onStudentSearchChanged(query: String) {
        _studentSearchQuery.value = query
    }

    fun addStudent(rollNumber: Int, name: String) {
        viewModelScope.launch {
            val student = StudentEntity(rollNumber = rollNumber, name = name, year = _selectedYear.value)
            repository.insertStudent(student)
        }
    }

    fun importStudentsFromCsv(csvData: String) {
        viewModelScope.launch {
            val lines = csvData.lines()
            val studentsToImport = mutableListOf<StudentEntity>()
            val currentYear = _selectedYear.value
            for (line in lines) {
                if (line.isBlank()) continue
                val parts = line.split(",")
                if (parts.size >= 2) {
                    val roll = parts[0].trim().toIntOrNull()
                    val name = parts[1].trim()
                    if (roll != null && name.isNotBlank()) {
                        studentsToImport.add(StudentEntity(rollNumber = roll, name = name, year = currentYear))
                    }
                }
            }
            if (studentsToImport.isNotEmpty()) {
                repository.insertStudents(studentsToImport)
            }
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun addSubject(id: String, title: String, maxMarks: Int, passMarks: Int, hasMcq: Boolean, maxMcq: Int, hasWritten: Boolean, maxWritten: Int, hasPractical: Boolean, maxPractical: Int) {
        viewModelScope.launch {
            repository.insertSubject(SubjectEntity(id, title, maxMarks, passMarks, hasMcq, maxMcq, hasWritten, maxWritten, hasPractical, maxPractical))
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    // --- QUICK EDIT STATE ---
    private val _selectedSubjectId = MutableStateFlow<String?>(null)
    val selectedSubjectId = _selectedSubjectId.asStateFlow()

    val selectedSubject = combine(_selectedSubjectId, allSubjects) { id, subjects ->
        if (id == null) subjects.firstOrNull() else subjects.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeQuickEditData = combine(selectedSubject, _selectedYear, _selectedTerm, ::Triple)
        .flatMapLatest { (subject, year, term) ->
            if (subject == null) {
                flowOf(emptyList())
            } else {
                combine(allStudents, repository.getMarksForSubject(subject.id, year, term)) { students, marks ->
                    val markMap = marks.associateBy { it.rollNumber }
                    students.map { student ->
                        StudentWithMark(student, markMap[student.rollNumber])
                    }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectSubject(subjectId: String) {
        _selectedSubjectId.value = subjectId
    }

    fun saveMark(rollNumber: Int, mcq: Int?, written: Int?, practical: Int?) {
        val subjectId = selectedSubject.value?.id ?: return
        viewModelScope.launch {
            repository.saveMark(rollNumber, subjectId, mcq, written, practical, _selectedYear.value, _selectedTerm.value)
        }
    }

    // --- TABULATION STATE ---
    val tabulationData = combine(allStudents, allMarks, allSubjects) { students, marks, subjects ->
        val marksByStudent = marks.groupBy { it.rollNumber }
        
        val tabulationRows = students.map { student ->
            val studentMarks = marksByStudent[student.rollNumber] ?: emptyList()
            val rawResults = studentMarks.associateBy { it.subjectId }
            
            // Build full results map ensuring every subject exists (even if missing/null)
            var totalMarks = 0
            var totalPoints = 0.0
            var hasFail = false
            var failedSubjectCount = 0
            var normalSubjectCount = 0
            
            val results = subjects.associate { subj ->
                val mark = rawResults[subj.id]
                val result = SubjectResult(subj, mark?.mcq, mark?.written, mark?.practical)
                
                val isInputted = mark?.mcq != null || mark?.written != null || mark?.practical != null
                
                if (isInputted) {
                    totalMarks += result.total
                    
                    val grade = result.grade
                    if (grade.point == 0.0) {
                        hasFail = true
                        failedSubjectCount++
                    }

                    totalPoints += grade.point
                    normalSubjectCount++
                }
                
                subj.id to result
            }

            var finalGpa = 0.0
            var finalGrade = if (normalSubjectCount == 0) "-" else "F"

            if (!hasFail && normalSubjectCount > 0) {
                finalGpa = totalPoints / normalSubjectCount
                if (finalGpa > 5.0) finalGpa = 5.0
                
                // Format up to 2 decimal places logically for the step comparison
                val gpaRounded = (round(finalGpa * 100) / 100.0)

                finalGrade = when {
                    gpaRounded >= 5.0 -> "A+"
                    gpaRounded >= 4.0 -> "A"
                    gpaRounded >= 3.5 -> "A-"
                    gpaRounded >= 3.0 -> "B"
                    gpaRounded >= 2.0 -> "C"
                    gpaRounded >= 1.0 -> "D"
                    else -> "F"
                }
            }

            TabulationRow(
                student = student,
                results = results,
                totalMarks = totalMarks,
                finalGpa = round(finalGpa * 100) / 100.0,
                finalGrade = finalGrade,
                failedSubjectCount = failedSubjectCount
            )
        }
        
        // Calculate merit position
        val sortedTabulation = tabulationRows.sortedWith(
            compareBy<TabulationRow> { it.failedSubjectCount }
                .thenByDescending { it.finalGpa }
                .thenByDescending { it.totalMarks }
        )
        
        sortedTabulation.mapIndexed { index, row ->
            row.copy(meritPosition = index + 1)
        }.sortedBy { it.student.rollNumber } // Return sorted by roll number for the grid
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    // --- MARKSHEET STATE ---
    private val _marksheetSearchQuery = MutableStateFlow("")
    val marksheetSearchQuery = _marksheetSearchQuery.asStateFlow()

    val searchedMarksheet = combine(tabulationData, _marksheetSearchQuery) { tabulation, query ->
        val queryRoll = query.toIntOrNull() ?: return@combine null
        tabulation.find { it.student.rollNumber == queryRoll }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onMarksheetSearchChanged(query: String) {
        _marksheetSearchQuery.value = query
    }
}
