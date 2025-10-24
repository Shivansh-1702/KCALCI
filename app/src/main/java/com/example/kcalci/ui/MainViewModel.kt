package com.example.kcalci.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kcalci.data.DataStoreManager
import com.example.kcalci.model.*
import com.example.kcalci.utils.GitHubModelsService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)
    
    val userProfile: StateFlow<UserProfile> = dataStoreManager.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())
    
    val todayEntries: StateFlow<List<FoodEntry>> = dataStoreManager.todayEntriesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val history: StateFlow<List<DayRecord>> = dataStoreManager.historyFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val isFirstLaunch: StateFlow<Boolean> = dataStoreManager.isFirstLaunchFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _availableModels = MutableStateFlow(listOf(
        "openai/gpt-4o-mini",
        "openai/gpt-4.1",
        "gpt-4o"
    ))
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    private val _testStatus = MutableStateFlow<String?>(null)
    val testStatus: StateFlow<String?> = _testStatus.asStateFlow()
    
    init {
        checkForNewDay()
    }
    
    fun checkForNewDay() {
        viewModelScope.launch {
            dataStoreManager.checkAndResetForNewDay()
        }
    }
    
    fun addFoodWithAI(foodName: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val profile = userProfile.value
                
                if (profile.apiKey.isEmpty()) {
                    _errorMessage.value = "Please set your GitHub token in settings"
                    _isLoading.value = false
                    return@launch
                }
                
                val service = GitHubModelsService(
                    apiKey = profile.apiKey,
                    modelName = profile.aiModel
                )
                val result = service.estimateCalories(foodName)
                
                if (result != null) {
                    val (calories, protein) = result
                    val entry = FoodEntry(
                        name = foodName,
                        calories = calories,
                        protein = protein
                    )
                    dataStoreManager.addFoodEntry(entry)
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Could not estimate calories. Check your GitHub token or try again."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "API Error: ${e.localizedMessage ?: "Check your GitHub token"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun removeFoodEntry(entryId: String) {
        viewModelScope.launch {
            dataStoreManager.removeFoodEntry(entryId)
        }
    }
    
    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            dataStoreManager.saveUserProfile(profile)
        }
    }
    
    fun setFirstLaunchComplete() {
        viewModelScope.launch {
            dataStoreManager.setFirstLaunchComplete()
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }

    fun testAI() {
        viewModelScope.launch {
            try {
                _testStatus.value = "Testing"
                val profile = userProfile.value
                if (profile.apiKey.isBlank()) {
                    _testStatus.value = "Set GitHub token first"
                    return@launch
                }
                val service = GitHubModelsService(
                    apiKey = profile.apiKey,
                    modelName = profile.aiModel
                )
                val ok = service.ping()
                _testStatus.value = if (ok) "AI reachable " else "AI test failed"
            } catch (e: Exception) {
                _testStatus.value = "AI test error: ${e.localizedMessage ?: "unknown"}"
            }
        }
    }
}
