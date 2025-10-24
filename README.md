# KCalci 

A professional Android calorie and protein tracking application powered by **GitHub Models** (free GPT-4o-mini & GPT-4.1) for intelligent nutrition estimation.

##  Features

###  Core Features
- **AI-Powered Nutrition Estimation**: Uses GitHub Models API (GPT-4o-mini) to automatically estimate calories AND protein for any food item
- **Free AI Access**: Leverages GitHub's free AI models - no paid API required!
- **Dual Macro Tracking**: Track both calories and protein intake simultaneously
- **Smart Protein Goals**: Auto-calculates protein target based on your body weight (1g per kg)
- **Material You Design**: Beautiful, modern UI with dynamic Material Design 3 theming
- **Weight Status Analysis**: Analyzes your height and weight against medical standards
- **Daily Tracking**: Track your food intake throughout the day with smooth, intuitive interface
- **Automatic Daily Reset**: Data automatically resets at midnight, saving previous day's records
- **Historical Records**: View previous daily records in settings
- **Offline Profile Storage**: All user data stored locally using DataStore

###  Weight Status Analysis
Based on medical height/weight chart for males and females:
- Determines if you're underweight, healthy weight, or overweight
- Shows your healthy weight range based on your height and gender
- Uses actual medical data ranges for accurate assessment

###  Privacy First
- All data stored locally using DataStore
- No cloud sync or data sharing
- Your GitHub token stored securely on device
- Only food names sent to AI for calorie/protein estimation

###  Professional UI
- Material You dynamic color theming
- Elevated cards with proper shadows
- Smooth animations (can be disabled)
- Clean, modern Compose UI
- Dual progress rings for calories and protein

##  Quick Start

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK with API level 26 or higher
- A GitHub Personal Access Token ([Get one here](https://github.com/settings/personal-access-tokens))

### Getting Your GitHub Token

1. Visit [GitHub Personal Access Tokens](https://github.com/settings/personal-access-tokens)
2. Click **"Generate new token"**  **"Generate new token (classic)"**
3. Give it a name (e.g., "KCalci App")
4. **No scopes needed** - just leave all checkboxes unchecked
5. Click **"Generate token"** at the bottom
6. Copy the token (starts with `github_pat_...`)
7. Enter this token in the app's Settings screen

**Note**: The token is free and gives you access to GPT-4o-mini and GPT-4.1 models!

### Installation

1. **Clone or Open the Project**
   ```bash
   git clone https://github.com/Shivansh-1702/kcalci.git
   cd kcalci
   ```

2. **Open in Android Studio**
   - Open the project in Android Studio
   - Wait for Gradle sync to complete

3. **Build and Run**
   - Connect an Android device or start an emulator (API 26+)
   - Click the "Run" button (green play icon) or press Shift+F10
   - The app will build and install on your device

### First Launch Setup

On first launch, enter your settings:

1. **Select Gender**: Male or Female
2. **Enter Height**: Your height in centimeters
3. **Enter Weight**: Your weight in kilograms (protein goal auto-calculated from this)
4. **Set Target Calories**: Your daily calorie goal (default: 2000)
5. **Enter GitHub Token**: Paste your GitHub personal access token
6. **Choose AI Model**: Select GPT-4o-mini (recommended, fast & free) or GPT-4.1
7. **Save Settings**: Tap "Save Settings" to continue

##  How to Use

### Adding Food Items
1. Type the food name in the input field (e.g., "banana", "chicken breast", "pizza")
2. Press Enter or tap the send button
3. The AI will automatically estimate **both calories and protein** and add it to your list
4. See the entry appear with calorie count and protein amount

### Viewing Progress
- **Top Card** shows two circular progress indicators:
  - **Calories**: Remaining kcal, consumed, and goal
  - **Protein**: Remaining grams, consumed, and goal (equals your weight in kg)
- Each food entry shows calories prominently with protein inline

### Managing Entries
- Tap any food item to remove it from your daily list

### Accessing Settings
1. Tap the settings icon () in the top right
2. Update your profile, GitHub token, target calories, or AI model
3. Test AI connectivity with the "Test AI" button
4. View "Last Records" to see previous days' nutrition data
5. Save changes when done

### Daily Reset
- At midnight (12:00 AM), the app automatically:
  - Saves today's calorie and protein data to history
  - Clears the food list for the new day
  - Maintains your settings and profile

##  Project Structure

```
app/src/main/java/com/example/kcalci/
 MainActivity.kt                    # Main entry point
 data/
    DataStoreManager.kt           # Local data persistence
 model/
    FoodEntry.kt                  # Data models
 ui/
    ModernMainScreen.kt           # Main UI
    EnhancedSettingsScreen.kt     # Settings UI
    MainViewModel.kt              # Business logic
    theme/                        # Material You theme
 utils/
     GitHubModelsService.kt        # GitHub Models API
     DailyResetWorker.kt           # Midnight reset worker
```

##  Technologies Used

- **Kotlin**: Primary language
- **Jetpack Compose**: Modern declarative UI
- **Material Design 3**: Dynamic theming
- **DataStore**: Encrypted local persistence
- **GitHub Models API**: Free AI calorie & protein estimation
- **WorkManager**: Background scheduling
- **Coroutines & Flow**: Async programming
- **MVVM Architecture**: ViewModel + StateFlow

##  AI Models

The app uses **GitHub Models API** with these free models:

| Model | Speed | Quality | Usage |
|-------|-------|---------|-------|
| `openai/gpt-4o-mini` |  Fast |  Good | **Recommended** - Default |
| `openai/gpt-4.1` |  Slower |  Best | Premium quality |
| `gpt-4o` |  Fast |  Great | Balanced |

Each food entry makes **one API call** to estimate both calories and protein.

**API Configuration:**
- Temperature: 0.0 (consistent results)
- Max tokens: 20 (two numbers)
- Strict prompting: "calories protein" format

##  Troubleshooting

### "Please set your GitHub token in settings"
- Enter a valid GitHub Personal Access Token
- Token should start with `github_pat_`
- No special scopes needed

### AI estimation fails
- Check internet connection
- Verify GitHub token is correct
- Use "Test AI" button in Settings
- Check for rate limits (unlikely)

### App doesn't reset at midnight
- Ensure background execution permission
- WorkManager will auto-retry

### Settings won't save
- All fields must have valid values
- Height and weight must be positive
- Target calories: 500-5000

### Protein not showing
- Update to latest version
- Protein shown inline with timestamp
- Top card shows both progress rings

##  Building for Release

1. Generate signed APK in Android Studio
2. Go to **Build > Generate Signed Bundle/APK**
3. Select **APK** and follow wizard
4. Choose **release** build variant
5. Sign with your keystore

##  Privacy & Data

-  All data stored locally (encrypted DataStore)
-  No analytics or tracking
-  GitHub token stored securely
-  Only food names sent to AI
-  No personal data sent to servers
-  Historical records local only

##  License

This project is for educational purposes.

##  Future Enhancements

-  Micronutrients tracking
-  Weekly/monthly charts
-  Barcode scanning
-  Water tracking
-  Meal categories
-  CSV export
-  Custom macro ratios
-  Exercise tracking

##  Support

For issues or questions:
1. Check troubleshooting section
2. Verify Gradle sync in Android Studio
3. Ensure valid GitHub token
4. Open an issue on GitHub

---

Built with  using Kotlin, Jetpack Compose & GitHub Models  
By [Shiv](https://github.com/Shivansh-1702)