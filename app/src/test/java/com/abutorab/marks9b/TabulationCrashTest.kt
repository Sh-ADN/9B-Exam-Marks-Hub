package com.abutorab.marks9b

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abutorab.marks9b.ui.screens.TabulationScreen
import com.abutorab.marks9b.ui.screens.CombinedTabulationScreen
import androidx.test.core.app.ApplicationProvider
import com.abutorab.marks9b.ui.MarksViewModel
import com.abutorab.marks9b.data.local.MarksDatabase
import com.abutorab.marks9b.data.repository.MarksRepository

@RunWith(AndroidJUnit4::class)
class TabulationCrashTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testTabulationRendering() {
        var error: Throwable? = null
        try {
            val app = ApplicationProvider.getApplicationContext<android.app.Application>()
            val db = MarksDatabase.getDatabase(app)
            val repository = MarksRepository(db.yearDao(), db.termDao(), db.studentDao(), db.subjectDao(), db.markDao())
            val viewModel = MarksViewModel(app, repository)
            composeTestRule.setContent {
                TabulationScreen(termId = 1, viewModel = viewModel, onBack = {}, onNavigateToMarksheet = {})
            }
        } catch (e: Throwable) {
            error = e
            e.printStackTrace()
        }
        assert(error == null)
    }

    @Test
    fun testCombinedTabulationRendering() {
        var error: Throwable? = null
        try {
            val app = ApplicationProvider.getApplicationContext<android.app.Application>()
            val db = MarksDatabase.getDatabase(app)
            val repository = MarksRepository(db.yearDao(), db.termDao(), db.studentDao(), db.subjectDao(), db.markDao())
            val viewModel = MarksViewModel(app, repository)
            composeTestRule.setContent {
                CombinedTabulationScreen(yearId = 1, viewModel = viewModel, onBack = {})
            }
        } catch (e: Throwable) {
            error = e
            e.printStackTrace()
        }
        assert(error == null)
    }
}
