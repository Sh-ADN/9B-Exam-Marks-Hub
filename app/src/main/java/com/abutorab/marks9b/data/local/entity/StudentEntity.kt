package com.abutorab.marks9b.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
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
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val yearId: Int,
    val roll: Int,
    val name: String,
    val religion: String,
    val group: String,
    val optionalType: String
)

enum class Religion(val code: String) {
    ISLAM("I"), HINDU("H"), BUDDHIST("B");
    companion object {
        fun fromCode(code: String) = values().firstOrNull { it.code == code } ?: ISLAM
    }
}

enum class StudentGroup(val code: String) {
    SCIENCE("S"), COMMERCE("C"), ARTS("A");
    companion object {
        fun fromCode(code: String) = values().firstOrNull { it.code == code } ?: SCIENCE
    }
}

enum class OptionalType(val code: String) {
    HIGHER_MATH("S"), AGRICULTURE("N");
    companion object {
        fun fromCode(code: String) = values().firstOrNull { it.code == code } ?: HIGHER_MATH
    }
}
