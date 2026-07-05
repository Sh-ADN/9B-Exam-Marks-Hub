package com.abutorab.marks9b.ui.screens

data class SubjectStats(
    val sheetRole: String,
    val applicabilityValue: String?,
    val subjectName: String,
    val fullMarks: Int,
    val average: Double,
    val highest: Int,
    val lowest: Int,
    val passed: Int,
    val failed: Int
)

data class StudentRanking(
    val position: Int,
    val roll: Int,
    val name: String,
    val total: Int,
    val gpa: Double?,
    val failedCount: Int
)

data class DashboardStats(
    val totalStudents: Int,
    val gradedStudents: Int,
    val ungradedStudents: Int,
    val passed: Int,
    val failed: Int,
    val passRate: Double,
    val averageTotal: Double,
    val medianTotal: Double,
    val highestTotal: Int,
    val averageGpa: Double,
    val gradeDistribution: Map<String, Int>,
    val subjectStats: List<SubjectStats>,
    val hardestSubjects: List<SubjectStats>,
    val marksByRoll: List<Pair<Int, Int>>,
    val rankedStudents: List<StudentRanking>
)

object DashboardEngine {
    fun compute(results: List<StudentResult>): DashboardStats {
        val graded = results.filter { it.gpa != null }
        val ungraded = results.filter { it.gpa == null }

        val passed = graded.count { it.failedCount == 0 }
        val failed = graded.count { it.failedCount > 0 }
        val passRate = if (graded.isNotEmpty()) (passed.toDouble() / graded.size) * 100.0 else 0.0

        val totals = graded.map { it.grandTotal }
        val averageTotal = if (totals.isNotEmpty()) totals.average() else 0.0
        val sortedTotals = totals.sorted()
        val medianTotal = if (sortedTotals.isEmpty()) 0.0 else {
            val mid = sortedTotals.size / 2
            if (sortedTotals.size % 2 == 0) (sortedTotals[mid - 1] + sortedTotals[mid]) / 2.0 else sortedTotals[mid].toDouble()
        }
        val highestTotal = totals.maxOrNull() ?: 0
        val averageGpa = graded.mapNotNull { it.gpa }.let { if (it.isNotEmpty()) it.average() else 0.0 }

        val gradeOrder = listOf("A+", "A", "A-", "B", "C", "D", "F")
        val gradeDistribution = gradeOrder.associateWith { grade -> graded.count { it.letterGrade == grade } }

        val allSubjectResults = results.flatMap { it.subjectResults }.filter { it.letterGrade.isNotEmpty() }
        val subjectStats = allSubjectResults
            .groupBy { it.subject.sheetRole to it.subject.applicabilityValue }
            .map { (_, group) ->
                val first = group.first()
                val scores = group.map { it.total }
                SubjectStats(
                    sheetRole = first.subject.sheetRole,
                    applicabilityValue = first.subject.applicabilityValue,
                    subjectName = first.subject.name,
                    fullMarks = first.subject.fullMarks,
                    average = if (scores.isNotEmpty()) scores.average() else 0.0,
                    highest = scores.maxOrNull() ?: 0,
                    lowest = scores.minOrNull() ?: 0,
                    passed = group.count { it.letterGrade != "F" },
                    failed = group.count { it.letterGrade == "F" }
                )
            }
            .sortedBy { TabulationDisplay.canonicalOrder(it.sheetRole, it.applicabilityValue) }

        val difficultyCandidates = subjectStats.filter { it.fullMarks > 0 }
        val minRatio = difficultyCandidates.minOfOrNull { it.average / it.fullMarks }
        val hardestSubjects = if (minRatio == null) emptyList() else {
            difficultyCandidates.filter { kotlin.math.abs((it.average / it.fullMarks) - minRatio) < 0.0001 }
        }

        val marksByRoll = results.map { it.student.roll to it.grandTotal }.sortedBy { it.first }

        val rankedStudents = graded
            .mapNotNull { result ->
                result.position?.let { pos ->
                    StudentRanking(pos, result.student.roll, result.student.name, result.grandTotal, result.gpa, result.failedCount)
                }
            }
            .sortedBy { it.position }

        return DashboardStats(
            totalStudents = results.size,
            gradedStudents = graded.size,
            ungradedStudents = ungraded.size,
            passed = passed,
            failed = failed,
            passRate = passRate,
            averageTotal = averageTotal,
            medianTotal = medianTotal,
            highestTotal = highestTotal,
            averageGpa = averageGpa,
            gradeDistribution = gradeDistribution,
            subjectStats = subjectStats,
            hardestSubjects = hardestSubjects,
            marksByRoll = marksByRoll,
            rankedStudents = rankedStudents
        )
    }
}
