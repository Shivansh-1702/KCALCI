package com.example.kcalci

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kcalci.ui.*
import com.example.kcalci.ui.theme.KcalciTheme
import com.example.kcalci.utils.DailyResetWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge with visible status bar
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Schedule daily reset worker
        DailyResetWorker.schedule(this)
        
        setContent {
            KcalciTheme(dynamicColor = true) {
                KcalciApp()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check for new day when app resumes
    }
}

@Composable
fun KcalciApp(viewModel: MainViewModel = viewModel()) {
    val userProfile by viewModel.userProfile.collectAsState()
    val todayEntries by viewModel.todayEntries.collectAsState()
    val history by viewModel.history.collectAsState()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    
    // Check for new day on app start
    LaunchedEffect(Unit) {
        viewModel.checkForNewDay()
    }
    
    // Never auto-show settings - only via button click
    
    // Clear error after 3 seconds
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (showSettings) {
            EnhancedSettingsScreen(
                userProfile = userProfile,
                onProfileUpdate = { profile ->
                    viewModel.updateUserProfile(profile)
                    if (isFirstLaunch) {
                        viewModel.setFirstLaunchComplete()
                    }
                },
                onBack = {
                    if (!isFirstLaunch && userProfile.apiKey.isNotEmpty()) {
                        showSettings = false
                    }
                },
                lastRecords = history,
                availableModels = viewModel.availableModels.collectAsState().value,
                onRefreshModels = { },
                onTestAI = { viewModel.testAI() },
                testStatus = viewModel.testStatus.collectAsState().value
            )
        } else {
            ModernMainScreen(
                todayEntries = todayEntries,
                targetCalories = userProfile.targetCalories,
                targetProtein = userProfile.targetProtein,
                onAddFood = { foodName ->
                    viewModel.addFoodWithAI(foodName)
                },
                onRemoveFood = { entryId ->
                    viewModel.removeFoodEntry(entryId)
                },
                onSettingsClick = {
                    showSettings = true
                },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
    }
}