package com.abutorab.teacher.hub.sync

import com.abutorab.teacher.hub.data.AppRepository
import com.abutorab.teacher.hub.data.MarkEntity
import com.abutorab.teacher.hub.data.StudentEntity
import com.abutorab.teacher.hub.data.SubjectEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class SyncManager(private val repository: AppRepository) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun hasCloudData(uid: String): Boolean {
        return try {
            val snapshot = firestore.collection("users").document(uid).collection("students").limit(1).get().await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun hasLocalData(): Boolean {
        return repository.getStudentCountGlobal() > 0
    }

    suspend fun pushAllToCloud(uid: String) {
        val userDocRef = firestore.collection("users").document(uid)
        
        val students = repository.allStudentsGlobal.first()
        val subjects = repository.allSubjects.first()
        val marks = repository.allMarksGlobal.first()

        val batch = firestore.batch()

        students.forEach { student ->
            val ref = userDocRef.collection("students").document("${student.year}_${student.rollNumber}")
            batch.set(ref, student)
        }

        subjects.forEach { subject ->
            val ref = userDocRef.collection("subjects").document(subject.id)
            batch.set(ref, subject)
        }

        marks.forEach { mark ->
            val ref = userDocRef.collection("marks").document("${mark.year}_${mark.term}_${mark.rollNumber}_${mark.subjectId}")
            batch.set(ref, mark)
        }

        batch.commit().await()
    }

    suspend fun pullAllFromCloud(uid: String) {
        val userDocRef = firestore.collection("users").document(uid)

        val studentsSnapshot = userDocRef.collection("students").get().await()
        val subjectsSnapshot = userDocRef.collection("subjects").get().await()
        val marksSnapshot = userDocRef.collection("marks").get().await()

        val students = studentsSnapshot.documents.mapNotNull { it.toObject(StudentEntity::class.java) }
        val subjects = subjectsSnapshot.documents.mapNotNull { it.toObject(SubjectEntity::class.java) }
        val marks = marksSnapshot.documents.mapNotNull { it.toObject(MarkEntity::class.java) }

        repository.clearAllDataGlobal()
        repository.saveAllData(subjects, students, marks)
    }

    suspend fun pushSingleChange(uid: String, collection: String, id: String, data: Any) {
        try {
            firestore.collection("users").document(uid).collection(collection).document(id).set(data).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteSingleDocument(uid: String, collection: String, id: String) {
        try {
            firestore.collection("users").document(uid).collection(collection).document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
