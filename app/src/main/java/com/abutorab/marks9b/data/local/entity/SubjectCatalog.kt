package com.abutorab.marks9b.data.local.entity

object SubjectCatalog {
    val subjects = listOf(
        SubjectData("Bangla 1st Paper", SheetRole.BANGLA1.name, ApplicabilityType.ALL.name, null, 30, 70, null, 100),
        SubjectData("Bangla 2nd Paper", SheetRole.BANGLA2.name, ApplicabilityType.ALL.name, null, 30, 70, null, 100),
        SubjectData("English 1st Paper", SheetRole.ENG1.name, ApplicabilityType.ALL.name, null, null, 100, null, 100),
        SubjectData("English 2nd Paper", SheetRole.ENG2.name, ApplicabilityType.ALL.name, null, null, 100, null, 100),
        SubjectData("Mathematics", SheetRole.MATH.name, ApplicabilityType.ALL.name, null, 30, 70, null, 100),
        SubjectData("Information & Communication Technology", SheetRole.ICT.name, ApplicabilityType.ALL.name, null, 25, null, 25, 50),
        SubjectData("Islam Religion and Moral Education", SheetRole.RELIGION.name, ApplicabilityType.RELIGION.name, "I", 30, 70, null, 100),
        SubjectData("Hindu Religion and Moral Education", SheetRole.RELIGION.name, ApplicabilityType.RELIGION.name, "H", 30, 70, null, 100),
        SubjectData("Buddhist Religion and Moral Education", SheetRole.RELIGION.name, ApplicabilityType.RELIGION.name, "B", 30, 70, null, 100),
        SubjectData("Bangladesh & Global Studies", SheetRole.BGS_OR_SCIENCE.name, ApplicabilityType.OPTIONAL_TYPE.name, "S", 30, 70, null, 100),
        SubjectData("General Science", SheetRole.BGS_OR_SCIENCE.name, ApplicabilityType.OPTIONAL_TYPE.name, "N", 30, 70, null, 100),
        SubjectData("Physics", SheetRole.ELECTIVE1.name, ApplicabilityType.GROUP.name, "S", 25, 50, 25, 100),
        SubjectData("Chemistry", SheetRole.ELECTIVE2.name, ApplicabilityType.GROUP.name, "S", 25, 50, 25, 100),
        SubjectData("Biology", SheetRole.ELECTIVE3.name, ApplicabilityType.GROUP.name, "S", 25, 50, 25, 100),
        SubjectData("Accounting", SheetRole.ELECTIVE1.name, ApplicabilityType.GROUP.name, "C", 30, 70, null, 100),
        SubjectData("Finance", SheetRole.ELECTIVE2.name, ApplicabilityType.GROUP.name, "C", 30, 70, null, 100),
        SubjectData("B. Entrepreneurship", SheetRole.ELECTIVE3.name, ApplicabilityType.GROUP.name, "C", 30, 70, null, 100),
        SubjectData("History", SheetRole.ELECTIVE1.name, ApplicabilityType.GROUP.name, "A", 30, 70, null, 100),
        SubjectData("Geography", SheetRole.ELECTIVE2.name, ApplicabilityType.GROUP.name, "A", 30, 70, null, 100),
        SubjectData("Civics", SheetRole.ELECTIVE3.name, ApplicabilityType.GROUP.name, "A", 30, 70, null, 100),
        SubjectData("Higher Mathematics", SheetRole.OPTIONAL.name, ApplicabilityType.OPTIONAL_TYPE.name, "S", 25, 50, 25, 100),
        SubjectData("Agriculture Studies", SheetRole.OPTIONAL.name, ApplicabilityType.OPTIONAL_TYPE.name, "N", 25, 50, 25, 100)
    )
}

data class SubjectData(
    val name: String,
    val sheetRole: String,
    val applicabilityType: String,
    val applicabilityValue: String?,
    val mcqMax: Int?,
    val writtenMax: Int?,
    val practicalMax: Int?,
    val fullMarks: Int
)
