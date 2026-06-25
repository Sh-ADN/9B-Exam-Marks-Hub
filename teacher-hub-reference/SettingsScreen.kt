package com.abutorab.teacher.hub.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.abutorab.teacher.hub.auth.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: com.abutorab.teacher.hub.domain.TeacherViewModel) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var ardhoPercent by remember { mutableStateOf(sharedPrefs.getInt("somonnito_ardho_percent", 50)) }

    val authManager = remember { AuthManager() }
    var currentUser by remember { mutableStateOf(authManager.getCurrentUser()) }
    var isSigningIn by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var showConflictDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val syncManager = viewModel.syncManager

    fun performSyncCheck(uid: String) {
        scope.launch {
            isSyncing = true
            val hasCloud = syncManager.hasCloudData(uid)
            val hasLocal = syncManager.hasLocalData()
            
            if (!hasCloud && hasLocal) {
                syncManager.pushAllToCloud(uid)
                snackbarHostState.showSnackbar("Data backed up to cloud")
            } else if (hasCloud && !hasLocal) {
                syncManager.pullAllFromCloud(uid)
                snackbarHostState.showSnackbar("Data restored from cloud")
            } else if (hasCloud && hasLocal) {
                showConflictDialog = true
            }
            isSyncing = false
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    authManager.firebaseAuthWithGoogle(idToken) { success, error ->
                        isSigningIn = false
                        if (success) {
                            currentUser = authManager.getCurrentUser()
                            Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show()
                            currentUser?.uid?.let { performSyncCheck(it) }
                        } else {
                            Toast.makeText(context, "Sign in failed: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    isSigningIn = false
                    Toast.makeText(context, "Sign in failed: No ID Token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                isSigningIn = false
                Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            isSigningIn = false
        }
    }

    if (showConflictDialog) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = { Text("Sync Conflict") },
            text = { Text("Both this phone and your cloud account have data. Which one do you want to keep?") },
            confirmButton = {
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = {
                        showConflictDialog = false
                        scope.launch {
                            isSyncing = true
                            currentUser?.uid?.let { syncManager.pushAllToCloud(it) }
                            isSyncing = false
                            snackbarHostState.showSnackbar("Data backed up to cloud")
                        }
                    }) {
                        Text("Keep this phone's data")
                    }
                    TextButton(onClick = {
                        showConflictDialog = false
                        scope.launch {
                            isSyncing = true
                            currentUser?.uid?.let { syncManager.pullAllFromCloud(it) }
                            isSyncing = false
                            snackbarHostState.showSnackbar("Data restored from cloud")
                        }
                    }) {
                        Text("Use cloud data")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showConflictDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (currentUser != null) {
                        Text(
                            text = "Signed in as:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    authManager.signOut(context)
                                    currentUser = null
                                    Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Sign out")
                            }
                            
                            Button(
                                onClick = { currentUser?.uid?.let { performSyncCheck(it) } },
                                enabled = !isSyncing
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Sync now")
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "You are not signed in.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = {
                                isSigningIn = true
                                launcher.launch(authManager.getGoogleSignInIntent(context))
                            },
                            enabled = !isSigningIn,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Sign in with Google")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "সমন্বিত নিয়ম (Combination Rule)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val barshikPercent = 100 - ardhoPercent
                    Text(
                        text = "অর্ধবার্ষিক ${ardhoPercent.toBengaliNumerals()}% + বার্ষিক ${barshikPercent.toBengaliNumerals()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "অর্ধবার্ষিক এর শতকরা হার নির্ধারণ করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Slider(
                        value = ardhoPercent.toFloat(),
                        onValueChange = { newValue ->
                            ardhoPercent = newValue.toInt()
                        },
                        onValueChangeFinished = {
                            sharedPrefs.edit().putInt("somonnito_ardho_percent", ardhoPercent).apply()
                            viewModel.notifySettingsChanged() // Notify viewmodel about change
                        },
                        valueRange = 0f..100f,
                        steps = 99
                    )
                }
            }
        }
    }
}
