package com.abutorab.marks9b.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "marks",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("studentId"),
        Index("subjectId"),
        Index(value = ["studentId", "subjectId"], unique = true)
    ]
)
data class MarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val subjectId: Int,
    val mcqMarks: Int?,
    val writtenMarks: Int?,
    val practicalMarks: Int?
)
