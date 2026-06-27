package com.abutorab.marks9b.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = TermEntity::class,
            parentColumns = ["id"],
            childColumns = ["termId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("termId")]
)
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val termId: Int,
    val name: String,
    val sheetRole: String,
    val applicabilityType: String,
    val applicabilityValue: String?,
    val fullMarks: Int,
    val mcqMax: Int?,
    val writtenMax: Int?,
    val practicalMax: Int?
)

enum class ApplicabilityType {
    ALL, RELIGION, GROUP, OPTIONAL_TYPE
}

enum class SheetRole(val code: String) {
    NONE("NONE"), 
    BANGLA1("BANGLA1"), 
    BANGLA2("BANGLA2"), 
    ENG1("ENG1"), 
    ENG2("ENG2"), 
    MATH("MATH"), 
    RELIGION("RELIGION"), 
    BGS_OR_SCIENCE("BGS_OR_SCIENCE"), 
    ELECTIVE1("ELECTIVE1"), 
    ELECTIVE2("ELECTIVE2"), 
    ELECTIVE3("ELECTIVE3"), 
    OPTIONAL("OPTIONAL"), 
    ICT("ICT");
    
    companion object {
        fun fromCode(code: String) = values().firstOrNull { it.code == code } ?: NONE
    }
}
