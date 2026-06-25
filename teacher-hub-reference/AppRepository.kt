package com.abutorab.teacher.hub.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class AppRepository(private val dao: AppDao) {
    private val syncManager by lazy { com.abutorab.teacher.hub.sync.SyncManager(this) }
    private val scope = CoroutineScope(Dispatchers.IO)
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val allStudentsGlobal: Flow<List<StudentEntity>> = dao.getAllStudentsGlobal()
    val allMarksGlobal: Flow<List<MarkEntity>> = dao.getAllMarksGlobal()

    fun getStudentsByYear(year: Int): Flow<List<StudentEntity>> {
        return dao.getStudentsByYear(year)
    }

    fun getAllMarks(year: Int, term: String): Flow<List<MarkEntity>> {
        return dao.getAllMarks(year, term)
    }

    suspend fun createInitialDataIfEmpty() {
        if (dao.getSubjectCount() == 0) {
            val sampleSubjects = listOf(
                SubjectEntity("BAN1", "Bangla 1", 100, 33, hasMcq = true, maxMcq = 30, hasWritten = true, maxWritten = 70, hasPractical = false, maxPractical = 0),
                SubjectEntity("ENG1", "English 1", 100, 33, hasMcq = false, maxMcq = 0, hasWritten = true, maxWritten = 100, hasPractical = false, maxPractical = 0),
                SubjectEntity("MATH", "Math", 100, 33, hasMcq = true, maxMcq = 30, hasWritten = true, maxWritten = 70, hasPractical = false, maxPractical = 0),
                SubjectEntity("ICT", "ICT", 50, 17, hasMcq = true, maxMcq = 25, hasWritten = false, maxWritten = 0, hasPractical = true, maxPractical = 25)
            )
            dao.insertSubjects(sampleSubjects)
        }
    }

    suspend fun insertSubject(subject: SubjectEntity) {
        dao.insertSubject(subject)
        auth.currentUser?.uid?.let { uid ->
            scope.launch { syncManager.pushSingleChange(uid, "subjects", subject.id, subject) }
        }
    }

    suspend fun updateSubject(subject: SubjectEntity) {
        dao.updateSubject(subject)
        auth.currentUser?.uid?.let { uid ->
            scope.launch { syncManager.pushSingleChange(uid, "subjects", subject.id, subject) }
        }
    }

    suspend fun deleteSubject(subject: SubjectEntity) {
        dao.deleteSubject(subject)
        auth.currentUser?.uid?.let { uid ->
            scope.launch { syncManager.deleteSingleDocument(uid, "subjects", subject.id) }
        }
    }

    fun getMarksForSubject(subjectId: String, year: Int, term: String): Flow<List<MarkEntity>> {
        return dao.getMarksForSubject(subjectId, year, term)
    }

    suspend fun insertStudent(student: StudentEntity) {
        dao.insertStudent(student)
        auth.currentUser?.uid?.let { uid ->
            scope.launch { syncManager.pushSingleChange(uid, "students", "${student.year}_${student.rollNumber}", student) }
        }
    }

    suspend fun insertStudents(students: List<StudentEntity>) {
        dao.insertStudents(students)
        auth.currentUser?.uid?.let { uid ->
            scope.launch {
                students.forEach { student ->
                    syncManager.pushSingleChange(uid, "students", "${student.year}_${student.rollNumber}", student)
                }
            }
        }
    }

    suspend fun updateStudent(student: StudentEntity) {
        dao.updateStudent(student)
        auth.currentUser?.uid?.let { uid ->
            scope.launch { syncManager.pushSingleChange(uid, "students", "${student.year}_${student.rollNumber}", student) }
        }
    }

    suspend fun deleteStudent(student: StudentEntity) {
        dao.deleteStudent(student)
        auth.currentUser?.uid?.let { uid ->
            scope.launch { syncManager.deleteSingleDocument(uid, "students", "${student.year}_${student.rollNumber}") }
        }
    }

    suspend fun saveMark(rollNumber: Int, subjectId: String, mcq: Int?, written: Int?, practical: Int?, year: Int, term: String) {
        val mark = MarkEntity(
            rollNumber = rollNumber,
            subjectId = subjectId,
            mcq = mcq,
            written = written,
            practical = practical,
            year = year,
            term = term
        )
        dao.insertMark(mark)
        auth.currentUser?.uid?.let { uid ->
            scope.launch { syncManager.pushSingleChange(uid, "marks", "${year}_${term}_${rollNumber}_${subjectId}", mark) }
        }
    }

    suspend fun getStudentCountGlobal(): Int {
        return dao.getStudentCountGlobal()
    }

    suspend fun clearAllDataGlobal() {
        dao.deleteAllMarksGlobal()
        dao.deleteAllStudentsGlobal()
        dao.deleteAllSubjects()
    }

    suspend fun saveAllData(subjects: List<SubjectEntity>, students: List<StudentEntity>, marks: List<MarkEntity>) {
        dao.insertSubjectsReplace(subjects)
        dao.insertStudentsReplace(students)
        dao.insertMarks(marks)
    }
}
