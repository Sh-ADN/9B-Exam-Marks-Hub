package com.abutorab.teacher.hub.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abutorab.teacher.hub.domain.TeacherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TeacherViewModel, onChangeYearTerm: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedTerm by viewModel.selectedTerm.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val themeState by viewModel.themeState.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeState) {
        "light" -> false
        "dark" -> true
        else -> isSystemDark
    }

    LaunchedEffect(selectedTerm, currentRoute) {
        if (selectedTerm == "SOMONNITO" && currentRoute == "quick_edit") {
            navController.navigate("tabulation") {
                popUpTo("quick_edit") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Change Year/Term") },
                    label = { Text("বছর/টার্ম পরিবর্তন করুন") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onChangeYearTerm()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                    label = { Text("Students") },
                    selected = currentRoute == "students",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("students") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = "Subjects") },
                    label = { Text("Subjects") },
                    selected = currentRoute == "subjects",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("subjects") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = when (currentRoute) {
                                    "students" -> "Students"
                                    "subjects" -> "Subjects"
                                    "tabulation" -> "Tabulation"
                                    "marksheet" -> "Marksheet"
                                    "quick_edit" -> "Quick Edit"
                                    "settings" -> "Settings"
                                    else -> "Teacher Hub"
                                }
                            )
                            val termText = when(selectedTerm) {
                                "ARDHOBARSHIK" -> "অর্ধবার্ষিক"
                                "BARSHIK" -> "বার্ষিক"
                                "SOMONNITO" -> "সমন্বিত"
                                else -> ""
                            }
                            if (termText.isNotEmpty()) {
                                Text(
                                    text = "${selectedYear.toBengaliNumerals()} • $termText",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Drawer")
                        }
                    },
                    actions = {
                        ThemeToggleButton(
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = { viewModel.toggleTheme(isDarkTheme) }
                        )
                    }
                )
            },
            bottomBar = {
                if (currentRoute in listOf("quick_edit", "tabulation", "marksheet")) {
                    NavigationBar {
                        if (selectedTerm != "SOMONNITO") {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Edit, contentDescription = "Quick Edit") },
                                label = { Text("Quick Edit") },
                                selected = currentRoute == "quick_edit",
                                onClick = {
                                    navController.navigate("quick_edit") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.List, contentDescription = "Tabulation") },
                            label = { Text("Tabulation") },
                            selected = currentRoute == "tabulation",
                            onClick = {
                                navController.navigate("tabulation") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.PersonSearch, contentDescription = "Marksheet") },
                            label = { Text("Marksheet") },
                            selected = currentRoute == "marksheet",
                            onClick = {
                                navController.navigate("marksheet") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "quick_edit",
                modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding).fillMaxSize()
            ) {
                composable("students") { StudentsScreen(viewModel) }
                composable("subjects") { SubjectsScreen(viewModel) }
                composable("quick_edit") { QuickEditScreen(viewModel) }
                composable("tabulation") { TabulationScreen(viewModel) }
                composable("marksheet") { MarksheetScreen(viewModel) }
                composable("settings") { SettingsScreen(viewModel) }
            }
        }
    }
}

@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isDarkTheme) 360f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "ThemeToggleRotation"
    )

    IconButton(onClick = onThemeToggle) {
        Box(modifier = Modifier.graphicsLayer(rotationZ = rotation)) {
            Crossfade(
                targetState = isDarkTheme,
                animationSpec = tween(durationMillis = 500),
                label = "ThemeToggleCrossfade"
            ) { dark ->
                if (dark) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Switch to light theme",
                        tint = Color(0xFFFFC107)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Nightlight,
                        contentDescription = "Switch to dark theme",
                        tint = Color(0xFF2C3E50)
                    )
                }
            }
        }
    }
}
