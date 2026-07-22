package com.abutorab.marks9b

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import com.abutorab.marks9b.data.local.entity.examPeriodLabel
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.abutorab.marks9b.data.local.MarksDatabase
import com.abutorab.marks9b.data.repository.MarksRepository
import com.abutorab.marks9b.ui.MarksViewModel
import com.abutorab.marks9b.ui.MarksViewModelFactory
import com.abutorab.marks9b.ui.screens.TabulationScreen
import com.abutorab.marks9b.ui.screens.CombinedTabulationScreen
import com.abutorab.marks9b.ui.screens.MarksheetScreen
import com.abutorab.marks9b.ui.screens.TermDetailScreen
import com.abutorab.marks9b.ui.screens.TermListScreen
import com.abutorab.marks9b.ui.screens.YearListScreen
import com.abutorab.marks9b.ui.screens.MarksEntryScreen
import com.abutorab.marks9b.ui.theme.Marks9bTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val db = MarksDatabase.getDatabase(applicationContext)
    val repository = MarksRepository(db.yearDao(), db.termDao(), db.studentDao(), db.subjectDao(), db.markDao())

    setContent {
      val systemTheme = androidx.compose.foundation.isSystemInDarkTheme()
      var isDarkTheme by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(systemTheme) }

      Marks9bTheme(darkTheme = isDarkTheme) {
        val viewModel: MarksViewModel = viewModel(factory = MarksViewModelFactory(application, repository))
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "splash") {
          composable("splash") {
            SplashScreen(onNavigateToHome = {
              navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
              }
            })
          }
          composable("home") {
            HomeScreen(
                viewModel = viewModel,
                isDarkTheme = isDarkTheme,
                onThemeToggle = { isDarkTheme = !isDarkTheme },
                onNavigateToManage = { navController.navigate("yearList") },
                onNavigateToTermDetail = { termId -> navController.navigate("termDetail/$termId") }
            )
          }
          composable("yearList") {
            YearListScreen(viewModel = viewModel, onNavigateToTerms = { yearId ->
              navController.navigate("termList/$yearId")
            })
          }
          composable(
            "termList/{yearId}",
            arguments = listOf(navArgument("yearId") { type = NavType.IntType })
          ) { backStackEntry ->
            val yearId = backStackEntry.arguments?.getInt("yearId") ?: 0
            TermListScreen(
              yearId = yearId, 
              viewModel = viewModel, 
              onNavigateToTermDetail = { termId ->
                navController.navigate("termDetail/$termId")
              },
              onNavigateToCombined = { navController.navigate("combined/$yearId") }
            )
          }
          composable(
              "combined/{yearId}",
              arguments = listOf(navArgument("yearId") { type = NavType.IntType })
          ) { backStackEntry ->
              val yearId = backStackEntry.arguments?.getInt("yearId") ?: 0
              CombinedTabulationScreen(yearId = yearId, viewModel = viewModel, onBack = { navController.popBackStack() })
          }
          composable(
            "termDetail/{termId}",
            arguments = listOf(navArgument("termId") { type = NavType.IntType })
          ) { backStackEntry ->
            val termId = backStackEntry.arguments?.getInt("termId") ?: 0
            TermDetailScreen(
                termId = termId, 
                viewModel = viewModel,
                onNavigateToMarksEntry = { term, subject ->
                    navController.navigate("marksEntry/$term/$subject")
                },
                onNavigateToTabulation = { tId -> navController.navigate("tabulation/$tId") },
                onNavigateToDashboard = { tId -> navController.navigate("dashboard/$tId") },
                onNavigateToMarksheet = { studentId -> navController.navigate("marksheet/$termId/$studentId") }
            )
          }
          composable(
            "tabulation/{termId}",
            arguments = listOf(navArgument("termId") { type = NavType.IntType })
          ) { backStackEntry ->
            val termId = backStackEntry.arguments?.getInt("termId") ?: 0
            TabulationScreen(
                termId = termId,
                viewModel = viewModel,
                onNavigateToMarksheet = { sId -> navController.navigate("marksheet/$termId/$sId") },
                onBack = { navController.popBackStack() }
            )
          }
          composable(
              "dashboard/{termId}",
              arguments = listOf(navArgument("termId") { type = NavType.IntType })
          ) { backStackEntry ->
              val termId = backStackEntry.arguments?.getInt("termId") ?: 0
              com.abutorab.marks9b.ui.screens.DashboardScreen(termId = termId, viewModel = viewModel, onBack = { navController.popBackStack() })
          }
          composable(
            "marksEntry/{termId}/{subjectId}",
            arguments = listOf(
                navArgument("termId") { type = NavType.IntType },
                navArgument("subjectId") { type = NavType.IntType }
            )
          ) { backStackEntry ->
            val termId = backStackEntry.arguments?.getInt("termId") ?: 0
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 0
            MarksEntryScreen(termId = termId, subjectId = subjectId, viewModel = viewModel)
          }
          composable(
            "marksheet/{termId}/{studentId}",
            arguments = listOf(
                navArgument("termId") { type = NavType.IntType },
                navArgument("studentId") { type = NavType.IntType }
            )
          ) { backStackEntry ->
            val termId = backStackEntry.arguments?.getInt("termId") ?: 0
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
            MarksheetScreen(termId = termId, studentId = studentId, viewModel = viewModel, onBack = { navController.popBackStack() })
          }
        }
      }
    }
  }
}

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
  LaunchedEffect(Unit) {
    delay(1100)
    onNavigateToHome()
  }
  
  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      androidx.compose.animation.AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(1000)) + scaleIn(tween(1000), initialScale = 0.8f)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(100.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "9B",
              style = MaterialTheme.typography.displayMedium,
              color = MaterialTheme.colorScheme.onPrimary,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = "9B Exam Marks Hub",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 24.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Exam Marks, Simplified.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MarksViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onNavigateToManage: () -> Unit,
    onNavigateToTermDetail: (Int) -> Unit
) {
  val context = LocalContext.current
  val activeTermContext by viewModel.getActiveTermContext(context).collectAsStateWithLifecycle(initialValue = null)
  val years by viewModel.getAllYears().collectAsStateWithLifecycle(initialValue = emptyList())

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("9B Exam Marks Hub") },
        actions = {
            com.abutorab.marks9b.ui.components.ThemeToggleButton(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(16.dp)
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
      ) {
          if (activeTermContext != null) {
              val term = activeTermContext!!
              val examPeriodLabel = examPeriodLabel(term.examPeriod)
              
              Card(
                  onClick = { onNavigateToTermDetail(term.termId) },
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                  modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
              ) {
                  Column(modifier = Modifier.padding(24.dp)) {
                      Text(
                          text = "${term.yearLabel} · ${term.termLabel}",
                          style = MaterialTheme.typography.titleLarge,
                          color = MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                          text = examPeriodLabel,
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Spacer(modifier = Modifier.height(24.dp))
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceEvenly
                      ) {
                          Column(horizontalAlignment = Alignment.CenterHorizontally) {
                              Text(
                                  text = term.studentCount.toString(),
                                  style = MaterialTheme.typography.headlineMedium,
                                  color = MaterialTheme.colorScheme.tertiary
                              )
                              Text(
                                  text = "Students",
                                  style = MaterialTheme.typography.labelLarge,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant
                              )
                          }
                          Column(horizontalAlignment = Alignment.CenterHorizontally) {
                              Text(
                                  text = term.subjectCount.toString(),
                                  style = MaterialTheme.typography.headlineMedium,
                                  color = MaterialTheme.colorScheme.tertiary
                              )
                              Text(
                                  text = "Subjects",
                                  style = MaterialTheme.typography.labelLarge,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant
                              )
                          }
                      }
                  }
              }
          } else {
              Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                  modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
              ) {
                  Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier = Modifier.padding(32.dp).fillMaxWidth()
                  ) {
                      Icon(
                          imageVector = Icons.Default.DateRange,
                          contentDescription = "No Term",
                          tint = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.size(48.dp)
                      )
                      Spacer(modifier = Modifier.height(16.dp))
                      Text(
                          text = "No active term yet",
                          style = MaterialTheme.typography.titleMedium,
                          color = MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                          text = "Open a term from Manage Data to see it here.",
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.padding(top = 8.dp),
                          textAlign = androidx.compose.ui.text.style.TextAlign.Center
                      )
                  }
              }
          }

          FilledTonalButton(onClick = onNavigateToManage) {
              Text("Manage Data")
          }
          
          Spacer(modifier = Modifier.height(16.dp))
          Text(
              text = "${years.size} years tracked",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
          )
      }
    }
  }
}
