package com.example.kcalci.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kcalci.model.*

// Weight status calculation (inlined from Calculations.kt)
fun calculateWeightStatus(heightCm: Int, weightKg: Double, gender: Gender): WeightStatus {
    val weightRange = getHealthyWeightRange(heightCm, gender)
    
    return when {
        weightKg < weightRange.first -> {
            WeightStatus(
                status = "Underweight",
                message = "Your weight is below the healthy range for your height.",
                minWeight = weightRange.first,
                maxWeight = weightRange.second
            )
        }
        weightKg > weightRange.second -> {
            WeightStatus(
                status = "Overweight",
                message = "Your weight is above the healthy range for your height.",
                minWeight = weightRange.first,
                maxWeight = weightRange.second
            )
        }
        else -> {
            WeightStatus(
                status = "Healthy",
                message = "Your weight is within the healthy range for your height.",
                minWeight = weightRange.first,
                maxWeight = weightRange.second
            )
        }
    }
}

private fun getHealthyWeightRange(heightCm: Int, gender: Gender): Pair<Double, Double> {
    return when (gender) {
        Gender.FEMALE -> getFemaleWeightRange(heightCm)
        Gender.MALE -> getMaleWeightRange(heightCm)
    }
}

private fun getFemaleWeightRange(heightCm: Int): Pair<Double, Double> {
    return when (heightCm) {
        in 136..138 -> Pair(28.5, 34.9)
        in 139..141 -> Pair(30.8, 37.6)
        in 142..144 -> Pair(32.6, 39.9)
        in 145..146 -> Pair(34.9, 42.6)
        in 147..149 -> Pair(36.4, 44.9)
        in 150..151 -> Pair(39.0, 47.6)
        in 152..154 -> Pair(40.8, 49.9)
        in 155..156 -> Pair(43.1, 52.6)
        in 157..159 -> Pair(44.9, 54.9)
        in 160..162 -> Pair(47.2, 57.6)
        in 163..164 -> Pair(49.0, 59.9)
        in 165..167 -> Pair(51.2, 62.6)
        in 168..169 -> Pair(53.0, 64.8)
        in 170..172 -> Pair(55.3, 67.6)
        in 173..174 -> Pair(57.1, 69.8)
        in 175..177 -> Pair(59.4, 72.6)
        in 178..179 -> Pair(61.2, 74.8)
        in 180..182 -> Pair(63.5, 77.5)
        in 183..184 -> Pair(65.3, 79.8)
        in 185..187 -> Pair(67.6, 82.5)
        in 188..190 -> Pair(69.4, 84.8)
        in 191..193 -> Pair(71.6, 87.5)
        else -> {
            val bmi_min = 18.5
            val bmi_max = 24.9
            val heightM = heightCm / 100.0
            Pair(bmi_min * heightM * heightM, bmi_max * heightM * heightM)
        }
    }
}

private fun getMaleWeightRange(heightCm: Int): Pair<Double, Double> {
    return when (heightCm) {
        in 136..138 -> Pair(28.5, 34.9)
        in 139..141 -> Pair(30.8, 38.1)
        in 142..144 -> Pair(33.5, 40.8)
        in 145..146 -> Pair(35.8, 43.9)
        in 147..149 -> Pair(38.5, 46.7)
        in 150..151 -> Pair(40.8, 49.9)
        in 152..154 -> Pair(43.1, 53.0)
        in 155..156 -> Pair(45.8, 55.8)
        in 157..159 -> Pair(48.1, 58.9)
        in 160..162 -> Pair(50.8, 61.6)
        in 163..164 -> Pair(53.0, 64.8)
        in 165..167 -> Pair(55.3, 68.0)
        in 168..169 -> Pair(58.0, 70.7)
        in 170..172 -> Pair(60.3, 73.9)
        in 173..174 -> Pair(63.0, 76.6)
        in 175..177 -> Pair(65.3, 79.8)
        in 178..179 -> Pair(67.6, 83.0)
        in 180..182 -> Pair(70.3, 85.7)
        in 183..184 -> Pair(72.6, 88.9)
        in 185..187 -> Pair(75.3, 91.6)
        in 188..190 -> Pair(77.5, 94.8)
        in 191..193 -> Pair(79.8, 98.0)
        else -> {
            val bmi_min = 18.5
            val bmi_max = 24.9
            val heightM = heightCm / 100.0
            Pair(bmi_min * heightM * heightM, bmi_max * heightM * heightM)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSettingsScreen(
    userProfile: UserProfile,
    onProfileUpdate: (UserProfile) -> Unit,
    onBack: () -> Unit,
    lastRecords: List<DayRecord>,
    availableModels: List<String> = emptyList(),
    onRefreshModels: () -> Unit = {},
    onTestAI: () -> Unit = {},
    testStatus: String? = null,
    customFoods: List<Pair<String, Int>> = emptyList(),
    onSaveCustomFood: (String, Int) -> Unit = { _, _ -> },
    onDeleteCustomFood: (String) -> Unit = {}
) {
    var gender by remember { mutableStateOf(userProfile.gender) }
    var height by remember { mutableStateOf(userProfile.heightCm.toString()) }
    var weight by remember { mutableStateOf(userProfile.weightKg.toString()) }
    var targetCalories by remember { mutableStateOf(userProfile.targetCalories.toString()) }
    var apiKey by remember { mutableStateOf(userProfile.apiKey) }
    var selectedModel by remember { mutableStateOf(userProfile.aiModel) }
    
    var showRecords by remember { mutableStateOf(false) }
    var showModelSelector by remember { mutableStateOf(false) }
    
    val weightStatus = remember(height, weight, gender) {
        val h = height.toIntOrNull() ?: 170
        val w = weight.toDoubleOrNull() ?: 70.0
        calculateWeightStatus(h, w, gender)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF8B7355)
                )
            }
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4E37),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gender Selection
            item {
                EnhancedSettingsCard {
                    Column {
                        Text(
                            text = "Gender",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5D4E37)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            EnhancedGenderChip(
                                text = "Male",
                                isSelected = gender == Gender.MALE,
                                onClick = { gender = Gender.MALE }
                            )
                            EnhancedGenderChip(
                                text = "Female",
                                isSelected = gender == Gender.FEMALE,
                                onClick = { gender = Gender.FEMALE }
                            )
                        }
                    }
                }
            }
            
            // Height
            item {
                EnhancedSettingsCard {
                    EnhancedSettingsTextField(
                        label = "Height (cm)",
                        value = height,
                        onValueChange = { height = it }
                    )
                }
            }
            
            // Weight
            item {
                EnhancedSettingsCard {
                    Column {
                        EnhancedSettingsTextField(
                            label = "Weight (kg)",
                            value = weight,
                            onValueChange = { weight = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        EnhancedWeightStatusCard(weightStatus)
                    }
                }
            }
            
            // Target Calories
            item {
                EnhancedSettingsCard {
                    EnhancedSettingsTextField(
                        label = "Target Calories (per day)",
                        value = targetCalories,
                        onValueChange = { targetCalories = it }
                    )
                }
            }
            
            // API Key
            item {
                EnhancedSettingsCard {
                    Column {
                        Text(
                            text = "GitHub Personal Access Token",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5D4E37)
                        )
                        Text(
                            text = "Get your free token from github.com/settings/tokens",
                            fontSize = 12.sp,
                            color = Color(0xFF8B7355)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B7355),
                                unfocusedBorderColor = Color(0xFFD4C5B9)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onTestAI,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B7355))
                            ) { Text("Test AI") }
                            testStatus?.let { status ->
                                Text(status, color = Color(0xFF5D4E37))
                            }
                        }
                    }
                }
            }
            
            // AI Model Selection
            item {
                EnhancedSettingsCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI Model",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF5D4E37)
                                )
                                Text(
                                    text = selectedModel,
                                    fontSize = 14.sp,
                                    color = Color(0xFF8B7355)
                                )
                            }
                            IconButton(onClick = { showModelSelector = !showModelSelector }) {
                                Icon(
                                    imageVector = if (showModelSelector) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle model selector",
                                    tint = Color(0xFF8B7355)
                                )
                            }
                        }
                        
                        if (showModelSelector) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val models = if (availableModels.isNotEmpty()) availableModels else AIModel.values().map { it.modelName }
                                models.forEach { id ->
                                    EnhancedModelChip(
                                        text = id,
                                        isSelected = selectedModel == id,
                                        onClick = {
                                            selectedModel = id
                                            showModelSelector = false
                                        }
                                    )
                                }
                                // Manual model id input
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Or enter a model id manually",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8B7355)
                                )
                                OutlinedTextField(
                                    value = selectedModel,
                                    onValueChange = { selectedModel = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF8B7355),
                                        unfocusedBorderColor = Color(0xFFD4C5B9)
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
            
            // History
            item {
                EnhancedSettingsCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "History (Last 7 Days)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF5D4E37)
                                )
                                Text(
                                    text = "${lastRecords.size} records",
                                    fontSize = 14.sp,
                                    color = Color(0xFF8B7355)
                                )
                            }
                            IconButton(onClick = { showRecords = !showRecords }) {
                                Icon(
                                    imageVector = if (showRecords) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle",
                                    tint = Color(0xFF8B7355)
                                )
                            }
                        }
                        
                        if (showRecords && lastRecords.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            lastRecords.forEach { record ->
                                EnhancedHistoryItem(record)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
            
            // Save Button
            item {
                Button(
                    onClick = {
                        val h = height.toIntOrNull() ?: 170
                        val w = weight.toDoubleOrNull() ?: 70.0
                        val tc = targetCalories.toIntOrNull() ?: 2000
                        
                        onProfileUpdate(
                            UserProfile(
                                gender = gender,
                                heightCm = h,
                                weightKg = w,
                                targetCalories = tc,
                                apiKey = apiKey.trim(),
                                selectedCuisines = emptyList(),
                                aiModel = selectedModel
                            )
                        )
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B7355)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Save Changes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedSettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun EnhancedSettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF5D4E37)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8B7355),
                unfocusedBorderColor = Color(0xFFD4C5B9)
            ),
            singleLine = true
        )
    }
}

@Composable
private fun EnhancedGenderChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF8B7355) else Color.White)
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFF8B7355) else Color(0xFFD4C5B9),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF5D4E37),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun EnhancedModelChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF8B7355) else Color(0xFFFFF8F0))
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF8B7355) else Color(0xFFD4C5B9),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF5D4E37),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun EnhancedWeightStatusCard(status: WeightStatus) {
    val statusColor = when (status.status) {
        "Healthy" -> Color(0xFF4CAF50)
        "Underweight" -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(statusColor.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = status.status,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            Text(
                text = status.message,
                fontSize = 12.sp,
                color = Color(0xFF5D4E37)
            )
        }
        Text(
            text = "${status.minWeight.toInt()}-${status.maxWeight.toInt()} kg",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = statusColor
        )
    }
}

@Composable
private fun EnhancedHistoryItem(record: DayRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF8F0))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = record.date,
            fontSize = 14.sp,
            color = Color(0xFF5D4E37)
        )
        Text(
            text = "${record.totalCalories} / ${record.targetCalories} cal",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8B7355)
        )
    }
}
