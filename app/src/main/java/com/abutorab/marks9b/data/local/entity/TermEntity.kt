package com.abutorab.marks9b.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "terms",
    foreignKeys = [
        ForeignKey(
            entity = YearEntity::class,
            parentColumns = ["id"],
            childColumns = ["yearId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("yearId")]
)
data class TermEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val yearId: Int,
    val label: String,
    val examPeriod: String
)

enum class ExamPeriod {
    MID_TERM, ANNUAL
}
