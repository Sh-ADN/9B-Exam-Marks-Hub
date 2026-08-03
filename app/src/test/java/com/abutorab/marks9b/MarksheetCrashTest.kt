package com.abutorab.marks9b

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import com.abutorab.marks9b.R

@RunWith(AndroidJUnit4::class)
class MarksheetCrashTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testFontLoading() {
        var error: Throwable? = null
        try {
            composeTestRule.setContent {
                Text("Hello", fontFamily = FontFamily(Font(R.font.galada)))
            }
        } catch (e: Throwable) {
            error = e
            e.printStackTrace()
        }
        assert(error == null)
    }
}
