package com.abutorab.marks9b.ui.screens

import com.abutorab.marks9b.data.local.entity.SheetRole
import com.abutorab.marks9b.data.local.entity.SubjectEntity

object TabulationDisplay {

    // Column header label for Tabulation's grid — generic/merged where multiple
    // subjects share a slot, matching the physical register's own convention.
    fun bengaliLabel(sheetRole: String, applicabilityValue: String?): String = when (sheetRole) {
        SheetRole.BANGLA1.name -> "বাংলা ১ম"
        SheetRole.BANGLA2.name -> "বাংলা ২য়"
        SheetRole.ENG1.name -> "ইংরেজি ১ম"
        SheetRole.ENG2.name -> "ইংরেজি ২য়"
        SheetRole.MATH.name -> "গণিত"
        SheetRole.RELIGION.name -> "ধর্মীয় শিক্ষা"
        SheetRole.BGS_OR_SCIENCE.name -> if (applicabilityValue == "S") "বাংলাদেশ ও বিশ্বপরিচয়" else "সাধারণ বিজ্ঞান"
        SheetRole.ELECTIVE1.name -> "ইতিহাস/হিসাববিজ্ঞান/পদার্থবিজ্ঞান"
        SheetRole.ELECTIVE2.name -> "ভূগোল/ফিন্যান্স/রসায়ন"
        SheetRole.ELECTIVE3.name -> "পৌরনীতি/ব্যবসায় উদ্যোগ/জীববিজ্ঞান"
        SheetRole.OPTIONAL.name -> "উচ্চতর গণিত/কৃষিশিক্ষা"
        SheetRole.ICT.name -> "ICT"
        else -> ""
    }

    // Specific per-student subject name for Marksheet — Religion stays generic
    // (matching the physical marksheet's own single row for it), everything
    // else shows that student's own actual subject, not the merged slot label.
    fun bengaliSubjectName(subject: SubjectEntity): String {
        if (subject.sheetRole == SheetRole.RELIGION.name) return "ধর্মীয় শিক্ষা"
        return when (subject.name) {
            "Bangla 1st Paper" -> "বাংলা ১ম"
            "Bangla 2nd Paper" -> "বাংলা ২য়"
            "English 1st Paper" -> "ইংরেজি ১ম"
            "English 2nd Paper" -> "ইংরেজি ২য়"
            "Mathematics" -> "গণিত"
            "Information & Communication Technology" -> "তথ্য ও যোগাযোগ প্রযুক্তি"
            "Bangladesh & Global Studies" -> "বাংলাদেশ ও বিশ্বপরিচয়"
            "General Science" -> "সাধারণ বিজ্ঞান"
            "Physics" -> "পদার্থ বিজ্ঞান"
            "Chemistry" -> "রসায়ন বিজ্ঞান"
            "Biology" -> "জীব বিজ্ঞান"
            "Accounting" -> "হিসাব বিজ্ঞান"
            "Finance" -> "ফাইন্যান্স ও ব্যাংকিং"
            "B. Entrepreneurship" -> "ব্যবসায় উদ্যোগ"
            "History" -> "ইতিহাস"
            "Geography" -> "ভূগোল"
            "Civics" -> "পৌরনীতি"
            "Higher Mathematics" -> "উচ্চতর গণিত"
            "Agriculture Studies" -> "কৃষি শিক্ষা"
            else -> subject.name
        }
    }

    // Splits BGS and General Science into separate columns even though they
    // share one internal role — everything else groups as before.
    fun columnGroupKey(subject: SubjectEntity): String {
        return if (subject.sheetRole == SheetRole.BGS_OR_SCIENCE.name) {
            "${subject.sheetRole}_${subject.applicabilityValue}"
        } else {
            subject.sheetRole
        }
    }

    // Exact column order confirmed from the Excel header row.
    fun canonicalOrder(sheetRole: String, applicabilityValue: String?): Int = when (sheetRole) {
        SheetRole.BANGLA1.name -> 0
        SheetRole.BANGLA2.name -> 1
        SheetRole.ENG1.name -> 2
        SheetRole.ENG2.name -> 3
        SheetRole.MATH.name -> 4
        SheetRole.RELIGION.name -> 5
        SheetRole.BGS_OR_SCIENCE.name -> if (applicabilityValue == "S") 6 else 7
        SheetRole.ELECTIVE1.name -> 8
        SheetRole.ELECTIVE2.name -> 9
        SheetRole.ELECTIVE3.name -> 10
        SheetRole.OPTIONAL.name -> 11
        SheetRole.ICT.name -> 12
        else -> 99
    }

    // "18+42=60" style, matching how it's written by hand on the physical sheet.
    // A single-component subject (e.g. English) just shows the bare number.
    fun formatBreakdown(mcq: Int?, written: Int?, practical: Int?, total: Int): String {
        val parts = listOfNotNull(mcq, written, practical)
        if (parts.isEmpty() || total == 0) return "-"
        return if (parts.size <= 1) "$total" else parts.joinToString("+") + "=" + total
    }
}
