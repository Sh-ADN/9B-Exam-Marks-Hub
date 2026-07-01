package com.abutorab.marks9b.ui.screens

import com.abutorab.marks9b.data.local.entity.ApplicabilityType
import com.abutorab.marks9b.data.local.entity.MarkEntity
import com.abutorab.marks9b.data.local.entity.SheetRole
import com.abutorab.marks9b.data.local.entity.StudentEntity
import com.abutorab.marks9b.data.local.entity.SubjectEntity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

data class SubjectResult(
    val subject: SubjectEntity,
    val mcqMarks: Int?,
    val writtenMarks: Int?,
    val practicalMarks: Int?,
    val total: Int,
    val letterGrade: String,
    val gradePoint: Double
)

data class StudentResult(
    val student: StudentEntity,
    val subjectResults: List<SubjectResult>,
    val grandTotal: Int,
    val failedCount: Int,
    val gpa: Double?,
    val letterGrade: String,
    val position: Int?
)

object TabulationEngine {

    fun letterGrade(total: Int, fullMarks: Int, sheetRole: SheetRole): String {
        if (total == 0) return ""
        if (sheetRole == SheetRole.ICT || fullMarks == 50) {
            return when {
                total >= 40 -> "A+"
                total >= 35 -> "A"
                total >= 30 -> "A-"
                total >= 25 -> "B"
                total >= 20 -> "C"
                total >= 17 -> "D"
                else -> "F"
            }
        } else {
            return when {
                total >= 80 -> "A+"
                total >= 70 -> "A"
                total >= 60 -> "A-"
                total >= 50 -> "B"
                total >= 40 -> "C"
                total >= 33 -> "D"
                else -> "F"
            }
        }
    }

    fun gradePointFromLetter(lg: String): Double {
        return when (lg) {
            "A+" -> 5.0
            "A" -> 4.0
            "A-" -> 3.5
            "B" -> 3.0
            "C" -> 2.0
            "D" -> 1.0
            "F" -> 0.0
            "" -> 0.0
            else -> 0.0
        }
    }

    fun combinedGP(combined: Int, outOf: Int): Double {
        return when {
            combined >= 160 -> 5.0
            combined >= 140 -> 4.0
            combined >= 120 -> 3.5
            combined >= 100 -> 3.0
            combined >= 80 -> 2.0
            combined >= 66 -> 1.0
            combined == 0 -> 0.0
            else -> 0.0
        }
    }

    fun optionalBonus(gp: Double): Double {
        return max(0.0, gp - 2.0)
    }

    fun compute(students: List<StudentEntity>, subjects: List<SubjectEntity>, marks: List<MarkEntity>): List<StudentResult> {
        val studentResults = mutableListOf<StudentResult>()

        for (student in students) {
            val applicableSubjects = subjects.filter { subject ->
                when (subject.applicabilityType) {
                    ApplicabilityType.ALL.name -> true
                    ApplicabilityType.RELIGION.name -> subject.applicabilityValue == student.religion
                    ApplicabilityType.GROUP.name -> subject.applicabilityValue == student.group
                    ApplicabilityType.OPTIONAL_TYPE.name -> subject.applicabilityValue == student.optionalType
                    else -> false
                }
            }

            val subjectResults = mutableListOf<SubjectResult>()
            var grandTotal = 0
            var failedCount = 0

            var bangla1Total = 0
            var bangla2Total = 0
            var eng1Total = 0
            var eng2Total = 0

            val individualGPs = mutableListOf<Double>()
            var optionalBonusGP = 0.0

            for (subject in applicableSubjects) {
                val mark = marks.find { it.studentId == student.id && it.subjectId == subject.id }
                val total = (mark?.mcqMarks ?: 0) + (mark?.writtenMarks ?: 0) + (mark?.practicalMarks ?: 0)
                grandTotal += total

                val sheetRole = try {
                    SheetRole.valueOf(subject.sheetRole)
                } catch (e: Exception) {
                    SheetRole.NONE
                }

                val lg = letterGrade(total, subject.fullMarks, sheetRole)
                val gp = gradePointFromLetter(lg)

                if (lg == "F" && sheetRole != SheetRole.OPTIONAL) {
                    failedCount++
                }

                subjectResults.add(SubjectResult(subject, mark?.mcqMarks, mark?.writtenMarks, mark?.practicalMarks, total, lg, gp))

                when (sheetRole) {
                    SheetRole.BANGLA1 -> bangla1Total = total
                    SheetRole.BANGLA2 -> bangla2Total = total
                    SheetRole.ENG1 -> eng1Total = total
                    SheetRole.ENG2 -> eng2Total = total
                    SheetRole.MATH,
                    SheetRole.RELIGION,
                    SheetRole.BGS_OR_SCIENCE,
                    SheetRole.ELECTIVE1,
                    SheetRole.ELECTIVE2,
                    SheetRole.ELECTIVE3,
                    SheetRole.ICT -> {
                        individualGPs.add(gp)
                    }
                    SheetRole.OPTIONAL -> {
                        optionalBonusGP = optionalBonus(gp)
                    }
                    else -> {}
                }
            }

            val banglaGP = combinedGP(bangla1Total + bangla2Total, 200)
            val engGP = combinedGP(eng1Total + eng2Total, 200)

            val gpSum = individualGPs.sum() + banglaGP + engGP + optionalBonusGP

            val gpa: Double? = if (grandTotal == 0) null else {
                val raw = if (failedCount > 0) 0.0 else min(5.0, gpSum / 9.0)
                round(raw * 100.0) / 100.0
            }

            val overallLg = when {
                gpa == null -> ""
                failedCount > 0 -> "F"
                gpa >= 5.0 -> "A+"
                gpa >= 4.0 -> "A"
                gpa >= 3.5 -> "A-"
                gpa >= 3.0 -> "B"
                gpa >= 2.0 -> "C"
                gpa >= 1.0 -> "D"
                else -> "F"
            }

            studentResults.add(
                StudentResult(
                    student = student,
                    subjectResults = subjectResults,
                    grandTotal = grandTotal,
                    failedCount = failedCount,
                    gpa = gpa,
                    letterGrade = overallLg,
                    position = null
                )
            )
        }

        val rankedResults = studentResults.map { result ->
            if (result.grandTotal == 0) {
                result.copy(position = null)
            } else {
                val rank = studentResults.count { it.failedCount < result.failedCount } +
                        studentResults.count { it.failedCount == result.failedCount && it.grandTotal > result.grandTotal } + 1
                result.copy(position = rank)
            }
        }

        return rankedResults
    }
}