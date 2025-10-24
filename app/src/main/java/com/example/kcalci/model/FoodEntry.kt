package com.example.kcalci.model

data class FoodEntry(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val calories: Int,
    val protein: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProfile(
    val gender: Gender = Gender.MALE,
    val heightCm: Int = 170,
    val weightKg: Double = 70.0,
    val targetCalories: Int = 2000,
    val targetProtein: Int = 70, // Default to body weight in kg
    val apiKey: String = "",
    val selectedCuisines: List<String> = emptyList(),
    val aiModel: String = "openai/gpt-4o-mini"
)

enum class Gender {
    MALE, FEMALE
}

enum class AIModel(val modelName: String, val displayName: String) {
    GPT_4O_MINI("openai/gpt-4o-mini", "GPT-4o Mini (Fast & Free)"),
    GPT_4_1("openai/gpt-4.1", "GPT-4.1 (Best Quality)"),
    GPT_4O("gpt-4o", "GPT-4o (Balanced)")
}

data class DayRecord(
    val date: String, // Format: yyyy-MM-dd
    val foodEntries: List<FoodEntry>,
    val totalCalories: Int,
    val totalProtein: Int,
    val targetCalories: Int,
    val targetProtein: Int
)

data class WeightStatus(
    val status: String, // "Healthy", "Underweight", "Overweight"
    val message: String,
    val minWeight: Double,
    val maxWeight: Double
)