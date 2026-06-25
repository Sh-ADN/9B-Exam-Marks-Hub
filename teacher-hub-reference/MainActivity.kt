package com.abutorab.teacher.hub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abutorab.teacher.hub.data.AppDatabase
import com.abutorab.teacher.hub.data.AppRepository
import com.abutorab.teacher.hub.domain.TeacherViewModel
import com.abutorab.teacher.hub.domain.TeacherViewModelFactory
import com.abutorab.teacher.hub.ui.screens.MainScreen
import com.abutorab.teacher.hub.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    android.util.Log.d("TeacherHub", "MainActivity created, version 2.0")
    enableEdgeToEdge()
    
    val db = AppDatabase.getDatabase(applicationContext)
    val repository = AppRepository(db.appDao())
    
    setContent {
      MyApplicationTheme {
        var showSplash by remember { mutableStateOf(true) }
        
        if (showSplash) {
          com.abutorab.teacher.hub.ui.screens.SplashScreen(
            onFinished = { showSplash = false }
          )
        } else {
          val viewModel: TeacherViewModel = viewModel(factory = TeacherViewModelFactory(application, repository))

          var pickedYear by remember { mutableStateOf<Int?>(null) }
          var pickedTerm by remember { mutableStateOf<String?>(null) }

          when {
              pickedYear == null -> {
                  com.abutorab.teacher.hub.ui.screens.YearPickerScreen(onYearSelected = { year ->
                      pickedYear = year
                  })
              }
              pickedTerm == null -> {
                  com.abutorab.teacher.hub.ui.screens.TermPickerScreen(
                      selectedYear = pickedYear!!,
                      onTermSelected = { term ->
                          pickedTerm = term
                          viewModel.setYearAndTerm(pickedYear!!, term)
                      },
                      onBack = { pickedYear = null }
                  )
              }
              else -> {
                  MainScreen(viewModel, onChangeYearTerm = {
                      pickedYear = null
                      pickedTerm = null
                  })
              }
          }
        }
      }
    }
  }
}


