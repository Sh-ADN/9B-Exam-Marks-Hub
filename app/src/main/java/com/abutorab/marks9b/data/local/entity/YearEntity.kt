package com.abutorab.marks9b.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "years")
data class YearEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String
)
