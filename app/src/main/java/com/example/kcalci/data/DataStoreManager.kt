package com.example.kcalci.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.kcalci.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kcalci_prefs")

class DataStoreManager(private val context: Context) {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    companion object {
        private val GENDER_KEY = stringPreferencesKey("gender")
        private val HEIGHT_KEY = intPreferencesKey("height_cm")
        private val WEIGHT_KEY = doublePreferencesKey("weight_kg")
        private val TARGET_CALORIES_KEY = intPreferencesKey("target_calories")
        private val TARGET_PROTEIN_KEY = intPreferencesKey("target_protein")
        private val API_KEY = stringPreferencesKey("api_key")
        private val CURRENT_DATE_KEY = stringPreferencesKey("current_date")
        private val TODAY_ENTRIES_KEY = stringPreferencesKey("today_entries")
        private val HISTORY_KEY = stringPreferencesKey("history")
        private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")
        private val SELECTED_CUISINES_KEY = stringPreferencesKey("selected_cuisines")
        private val AI_MODEL_KEY = stringPreferencesKey("ai_model")
    }
    
    // User Profile
    suspend fun saveUserProfile(profile: UserProfile) {
        // Auto-calculate targetProtein from weightKg
        val updatedProfile = profile.copy(
            targetProtein = profile.weightKg.toInt()
        )
        
        context.dataStore.edit { prefs ->
            prefs[GENDER_KEY] = updatedProfile.gender.name
            prefs[HEIGHT_KEY] = updatedProfile.heightCm
            prefs[WEIGHT_KEY] = updatedProfile.weightKg
            prefs[TARGET_CALORIES_KEY] = updatedProfile.targetCalories
            prefs[TARGET_PROTEIN_KEY] = updatedProfile.targetProtein
            prefs[API_KEY] = updatedProfile.apiKey
            prefs[SELECTED_CUISINES_KEY] = gson.toJson(updatedProfile.selectedCuisines)
            prefs[AI_MODEL_KEY] = updatedProfile.aiModel
        }
        println("DataStoreManager: Saved profile - API key: ${if (updatedProfile.apiKey.isNotEmpty()) "SET (${updatedProfile.apiKey.take(15)}...)" else "EMPTY"}, Model: ${updatedProfile.aiModel}, Target Protein: ${updatedProfile.targetProtein}g")
    }
    
    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        val cuisinesJson = prefs[SELECTED_CUISINES_KEY] ?: "[]"
        val type = object : TypeToken<List<String>>() {}.type
        val cuisines: List<String> = gson.fromJson(cuisinesJson, type) ?: emptyList()
        
        val apiKey = prefs[API_KEY] ?: ""
        val aiModel = prefs[AI_MODEL_KEY] ?: "openai/gpt-4o-mini"
        val weightKg = prefs[WEIGHT_KEY] ?: 70.0
        val targetProtein = prefs[TARGET_PROTEIN_KEY] ?: weightKg.toInt()
        
        println("DataStoreManager: Loading profile - API key: ${if (apiKey.isNotEmpty()) "SET (${apiKey.take(15)}...)" else "EMPTY"}, Model: $aiModel")
        
        UserProfile(
            gender = Gender.valueOf(prefs[GENDER_KEY] ?: Gender.MALE.name),
            heightCm = prefs[HEIGHT_KEY] ?: 170,
            weightKg = weightKg,
            targetCalories = prefs[TARGET_CALORIES_KEY] ?: 2000,
            targetProtein = targetProtein,
            apiKey = apiKey,
            selectedCuisines = cuisines,
            aiModel = aiModel
        )
    }
    
    // Today's Food Entries
    suspend fun saveTodayEntries(entries: List<FoodEntry>) {
        context.dataStore.edit { prefs ->
            prefs[TODAY_ENTRIES_KEY] = gson.toJson(entries)
            prefs[CURRENT_DATE_KEY] = getCurrentDate()
        }
    }
    
    val todayEntriesFlow: Flow<List<FoodEntry>> = context.dataStore.data.map { prefs ->
        val storedDate = prefs[CURRENT_DATE_KEY] ?: ""
        val currentDate = getCurrentDate()
        
        // If date has changed, save yesterday's data to history and clear today
        if (storedDate.isNotEmpty() && storedDate != currentDate) {
            // This will be handled by the ViewModel
            emptyList()
        } else {
            val json = prefs[TODAY_ENTRIES_KEY] ?: "[]"
            val type = object : TypeToken<List<FoodEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }
    
    suspend fun addFoodEntry(entry: FoodEntry) {
        context.dataStore.edit { prefs ->
            val json = prefs[TODAY_ENTRIES_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<FoodEntry>>() {}.type
            val entries: MutableList<FoodEntry> = gson.fromJson(json, type) ?: mutableListOf()
            entries.add(entry)
            prefs[TODAY_ENTRIES_KEY] = gson.toJson(entries)
            prefs[CURRENT_DATE_KEY] = getCurrentDate()
        }
    }
    
    suspend fun removeFoodEntry(entryId: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[TODAY_ENTRIES_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<FoodEntry>>() {}.type
            val entries: MutableList<FoodEntry> = gson.fromJson(json, type) ?: mutableListOf()
            entries.removeAll { it.id == entryId }
            prefs[TODAY_ENTRIES_KEY] = gson.toJson(entries)
        }
    }
    
    // History (Last 30 days)
    suspend fun saveToHistory(dayRecord: DayRecord) {
        context.dataStore.edit { prefs ->
            val json = prefs[HISTORY_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<DayRecord>>() {}.type
            val history: MutableList<DayRecord> = gson.fromJson(json, type) ?: mutableListOf()
            
            // Remove if already exists (update)
            history.removeAll { it.date == dayRecord.date }
            
            // Add new record
            history.add(0, dayRecord)
            
            // Keep only last 30 days
            if (history.size > 30) {
                history.subList(30, history.size).clear()
            }
            
            prefs[HISTORY_KEY] = gson.toJson(history)
        }
    }
    
    val historyFlow: Flow<List<DayRecord>> = context.dataStore.data.map { prefs ->
        val json = prefs[HISTORY_KEY] ?: "[]"
        val type = object : TypeToken<List<DayRecord>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }
    
    // Check if we need to reset for new day
    suspend fun checkAndResetForNewDay(): Boolean {
        val storedDate = context.dataStore.data.map { it[CURRENT_DATE_KEY] ?: "" }.first()
        val currentDate = getCurrentDate()
        
        if (storedDate.isNotEmpty() && storedDate != currentDate) {
            // Save yesterday's data to history
            val entries = getTodayEntriesRaw()
            val profile = userProfileFlow.first()
            val targetCalories = profile.targetCalories
            val targetProtein = profile.targetProtein
            
            if (entries.isNotEmpty()) {
                val dayRecord = DayRecord(
                    date = storedDate,
                    foodEntries = entries,
                    totalCalories = entries.sumOf { it.calories },
                    totalProtein = entries.sumOf { it.protein },
                    targetCalories = targetCalories,
                    targetProtein = targetProtein
                )
                saveToHistory(dayRecord)
            }
            
            // Clear today's entries
            saveTodayEntries(emptyList())
            return true
        }
        return false
    }
    
    suspend fun getCurrentStoredDate(): String {
        return context.dataStore.data.map { it[CURRENT_DATE_KEY] ?: "" }.first()
    }
    
    private fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }
    
    // Read today's entries without any date-change logic
    private suspend fun getTodayEntriesRaw(): List<FoodEntry> {
        val prefs = context.dataStore.data.first()
        val json = prefs[TODAY_ENTRIES_KEY] ?: "[]"
        val type = object : TypeToken<List<FoodEntry>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    // First Launch
    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { prefs ->
            prefs[FIRST_LAUNCH_KEY] = false
        }
    }
    
    val isFirstLaunchFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[FIRST_LAUNCH_KEY] ?: true
    }
}