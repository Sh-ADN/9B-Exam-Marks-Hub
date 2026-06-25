package com.abutorab.teacher.hub.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey var id: String = "",
    var title: String = "",
    var maxMarks: Int = 0,
    var passMarks: Int = 0,
    var hasMcq: Boolean = true,
    var maxMcq: Int = 100,
    var hasWritten: Boolean = true,
    var maxWritten: Int = 100,
    var hasPractical: Boolean = true,
    var maxPractical: Int = 0
)

@Entity(primaryKeys = ["rollNumber", "year"], tableName = "students")
data class StudentEntity(
    var rollNumber: Int = 0,
    var name: String = "",
    var year: Int = 0
)

@Entity(
    tableName = "marks",
    primaryKeys = ["rollNumber", "subjectId", "year", "term"],
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["rollNumber", "year"],
            childColumns = ["rollNumber", "year"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MarkEntity(
    var rollNumber: Int = 0,
    var subjectId: String = "",
    var mcq: Int? = null,
    var written: Int? = null,
    var practical: Int? = null,
    var year: Int = 0,
    var term: String = ""
)
