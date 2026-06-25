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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abutorab.marks9b.ui.theme.Marks9bTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      Marks9bTheme {
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
            HomeScreen()
          }
        }
      }
    }
  }
}

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
  LaunchedEffect(Unit) {
    delay(1500)
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
fun HomeScreen() {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("9B Exam Marks Hub") },
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
        .padding(innerPadding),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "Coming soon",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
      )
    }
  }
}
