package com.abutorab.marks9b

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abutorab.marks9b.ui.screens.MarksheetScreen
import androidx.test.core.app.ApplicationProvider
import com.abutorab.marks9b.ui.MarksViewModel
import com.abutorab.marks9b.data.local.MarksDatabase
import com.abutorab.marks9b.data.repository.MarksRepository

@RunWith(AndroidJUnit4::class)
class MarksheetCrashTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testMarksheetRendering() {
        var error: Throwable? = null
        try {
            val app = ApplicationProvider.getApplicationContext<android.app.Application>()
            val db = MarksDatabase.getDatabase(app)
            val repository = MarksRepository(db.yearDao(), db.termDao(), db.studentDao(), db.subjectDao(), db.markDao())
            val viewModel = MarksViewModel(app, repository)
            composeTestRule.setContent {
                MarksheetScreen(termId = 1, studentId = 1, viewModel = viewModel, onBack = {})
            }
        } catch (e: Throwable) {
            error = e
            e.printStackTrace()
        }
        assert(error == null)
    }
}
