package com.abutorab.marks9b.data.remote

import com.abutorab.marks9b.data.local.entity.StudentEntity

data class RosterDiff(
    val toInsert: List<SheetsSyncService.RosterEntry>,
    val incompleteInSheet: List<SheetsSyncService.RosterEntry>,
    val toUpdate: List<Pair<StudentEntity, SheetsSyncService.RosterEntry>>,
    val missingLocally: List<StudentEntity>
)

object RosterSync {
    fun diff(localStudents: List<StudentEntity>, fetchedRoster: List<SheetsSyncService.RosterEntry>): RosterDiff {
        val localByRoll = localStudents.associateBy { it.roll }
        val fetchedRolls = fetchedRoster.map { it.roll }.toSet()

        val newEntries = fetchedRoster.filter { it.roll !in localByRoll.keys }
        val toInsert = newEntries.filter { it.religion.isNotBlank() && it.group.isNotBlank() && it.optionalType.isNotBlank() }
        val incompleteInSheet = newEntries.filter { it.religion.isBlank() || it.group.isBlank() || it.optionalType.isBlank() }

        val toUpdate = fetchedRoster.mapNotNull { entry ->
            val local = localByRoll[entry.roll]
            if (local != null && local.name != entry.name) local to entry else null
        }
        val missingLocally = localStudents.filter { it.roll !in fetchedRolls }

        return RosterDiff(toInsert, incompleteInSheet, toUpdate, missingLocally)
    }
}
