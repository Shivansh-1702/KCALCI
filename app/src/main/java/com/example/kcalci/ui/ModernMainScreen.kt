package com.example.kcalci.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.kcalci.model.FoodEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernMainScreen(
    todayEntries: List<FoodEntry>,
    targetCalories: Int,
    targetProtein: Int,
    onAddFood: (String) -> Unit,
    onRemoveFood: (String) -> Unit,
    onSettingsClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var foodInputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    
    // Scroll to bottom when new entry is added
    LaunchedEffect(todayEntries.size) {
        if (todayEntries.isNotEmpty()) {
            delay(100)
            listState.scrollToItem(todayEntries.size)
        }
    }
    
    val totalCalories = todayEntries.sumOf { it.calories }
    val totalProtein = todayEntries.sumOf { it.protein }
    val remainingCalories = targetCalories - totalCalories
    val remainingProtein = targetProtein - totalProtein
    val progress = (totalCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
    val proteinProgress = (totalProtein.toFloat() / targetProtein.toFloat()).coerceIn(0f, 1f)
    
    Scaffold(
        containerColor = colorScheme.surface,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    colorScheme.surface,
                                    colorScheme.surface.copy(alpha = 0.95f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KCalci",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            
                            IconButton(
                                onClick = onSettingsClick,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primaryContainer.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        CalorieCard(
                            remainingCalories = remainingCalories,
                            targetCalories = targetCalories,
                            consumedCalories = totalCalories,
                            progress = progress,
                            remainingProtein = remainingProtein,
                            targetProtein = targetProtein,
                            consumedProtein = totalProtein,
                            proteinProgress = proteinProgress
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.Transparent,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    colorScheme.surface.copy(alpha = 0.95f),
                                    colorScheme.surface
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            color = colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shadowElevation = 2.dp
                        ) {
                            TextField(
                                value = foodInputText,
                                onValueChange = { foodInputText = it },
                                placeholder = {
                                    Text(
                                        text = "What did you eat?",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = colorScheme.primary
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = colorScheme.onSurface
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (foodInputText.isNotBlank()) {
                                            onAddFood(foodInputText.trim())
                                            foodInputText = ""
                                        }
                                    }
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Restaurant,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                        }
                        
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = if (foodInputText.isNotBlank()) 
                                colorScheme.primary 
                            else 
                                colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shadowElevation = if (foodInputText.isNotBlank()) 4.dp else 0.dp,
                            onClick = {
                                if (foodInputText.isNotBlank() && !isLoading) {
                                    onAddFood(foodInputText.trim())
                                    foodInputText = ""
                                }
                            }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp,
                                        color = colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (foodInputText.isNotBlank()) 
                                            colorScheme.onPrimary 
                                        else 
                                            colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (todayEntries.isEmpty() && !isLoading) {
                EmptyState()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = todayEntries,
                        key = { it.id }
                    ) { entry ->
                        FoodEntryCard(
                            entry = entry,
                            onRemove = { onRemoveFood(entry.id) }
                        )
                    }
                    
                    if (isLoading) {
                        item {
                            LoadingCard()
                        }
                    }
                }
            }
            
            errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = colorScheme.errorContainer,
                    contentColor = colorScheme.onErrorContainer
                ) {
                    Text(message)
                }
            }
        }
    }
}

@Composable
fun CalorieCard(
    remainingCalories: Int,
    targetCalories: Int,
    consumedCalories: Int,
    progress: Float,
    remainingProtein: Int,
    targetProtein: Int,
    consumedProtein: Int,
    proteinProgress: Float
) {
    val colorScheme = MaterialTheme.colorScheme
    val calorieStatusColor = when {
        remainingCalories >= targetCalories * 0.5 -> colorScheme.tertiary
        remainingCalories >= 0 -> colorScheme.primary
        else -> colorScheme.error
    }
    val proteinStatusColor = when {
        remainingProtein >= targetProtein * 0.5 -> colorScheme.secondary
        remainingProtein >= 0 -> colorScheme.primaryContainer
        else -> colorScheme.errorContainer
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            calorieStatusColor.copy(alpha = 0.08f),
                            proteinStatusColor.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Calories Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Calories Remaining",
                        style = MaterialTheme.typography.titleSmall,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = remainingCalories.toString(),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = calorieStatusColor
                            )
                        )
                        Text(
                            text = "kcal",
                            style = MaterialTheme.typography.titleMedium,
                            color = calorieStatusColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoChip(
                            label = "Eaten",
                            value = "$consumedCalories",
                            icon = Icons.Outlined.LocalDining,
                            tint = calorieStatusColor
                        )
                        InfoChip(
                            label = "Goal",
                            value = "$targetCalories",
                            icon = Icons.Outlined.Flag,
                            tint = calorieStatusColor
                        )
                    }
                }
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(110.dp),
                        strokeWidth = 12.dp,
                        color = calorieStatusColor,
                        trackColor = calorieStatusColor.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = calorieStatusColor
                        )
                        Text(
                            text = "eaten",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 1.dp,
                color = colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            // Protein Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Protein Remaining",
                        style = MaterialTheme.typography.titleSmall,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = remainingProtein.toString(),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = proteinStatusColor
                            )
                        )
                        Text(
                            text = "g",
                            style = MaterialTheme.typography.titleMedium,
                            color = proteinStatusColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoChip(
                            label = "Eaten",
                            value = "${consumedProtein}g",
                            icon = Icons.Outlined.FitnessCenter,
                            tint = proteinStatusColor
                        )
                        InfoChip(
                            label = "Goal",
                            value = "${targetProtein}g",
                            icon = Icons.Outlined.Flag,
                            tint = proteinStatusColor
                        )
                    }
                }
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { proteinProgress },
                        modifier = Modifier.size(110.dp),
                        strokeWidth = 12.dp,
                        color = proteinStatusColor,
                        trackColor = proteinStatusColor.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(proteinProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = proteinStatusColor
                        )
                        Text(
                            text = "eaten",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FoodEntryCard(
    entry: FoodEntry,
    onRemove: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDeleteDialog = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Restaurant,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(entry.timestamp),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                        if (entry.protein > 0) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${entry.protein}g protein",
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${entry.calories}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                    }
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Remove Entry",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Remove \"${entry.name}\" (${entry.calories} cal)?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        onRemove()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colorScheme.errorContainer
                    )
                ) {
                    Text("Remove", color = colorScheme.onErrorContainer)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LoadingCard() {
    val colorScheme = MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp
            )
            Text(
                text = "Estimating calories with AI...",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState() {
    val colorScheme = MaterialTheme.colorScheme
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Restaurant,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            
            Text(
                text = "No meals yet today",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Start logging your meals below\nAI will estimate the calories for you",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
