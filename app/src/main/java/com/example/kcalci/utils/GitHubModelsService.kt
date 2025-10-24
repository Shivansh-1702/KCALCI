package com.example.kcalci.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * GitHub Models AI Service for calorie estimation
 * Free access to GPT-4o-mini, GPT-4.1 and other models via GitHub
 * https://docs.github.com/en/github-models
 */
class GitHubModelsService(
    private val apiKey: String,
    private val modelName: String = "openai/gpt-4o-mini"
) {
    
    companion object {
        private const val GITHUB_ENDPOINT = "https://models.github.ai/inference/chat/completions"
        
        // Available GitHub Models (all FREE)
        const val MODEL_GPT_4O_MINI = "openai/gpt-4o-mini"
        const val MODEL_GPT_4_1 = "openai/gpt-4.1"
        const val MODEL_GPT_4O = "gpt-4o"
        
        val AVAILABLE_MODELS = listOf(
            MODEL_GPT_4O_MINI,
            MODEL_GPT_4_1,
            MODEL_GPT_4O
        )
        
        /**
         * Validate GitHub Personal Access Token
         */
        fun isValidApiKey(apiKey: String): Boolean {
            return apiKey.isNotBlank() && apiKey.startsWith("github_pat_")
        }
    }
    
    /**
     * Estimate calories and protein for a food item using GitHub Models
     * Returns Pair<calories, protein> or null
     */
    suspend fun estimateCalories(foodName: String): Pair<Int, Int>? = withContext(Dispatchers.IO) {
        try {
            if (foodName.isBlank()) {
                println("GitHubModelsService: Empty food name")
                return@withContext null
            }
            
            if (!isValidApiKey(apiKey)) {
                println("GitHubModelsService: Invalid GitHub token")
                return@withContext null
            }
            
            // Ultra-strict prompt: demand ONLY two numbers (calories and protein)
            val prompt = """
                Food: $foodName
                
                Return ONLY two numbers separated by a space: calories protein
                No words, no units, no explanation, no punctuation.
                Just two integers.
                
                Example responses:
                450 25
                120 5
                850 40
            """.trimIndent()
            
            println("GitHubModelsService: Estimating calories & protein for '$foodName' with model '$modelName'")
            
            val response = callGitHubAPI(prompt)
            
            println("GitHubModelsService: Raw response: '$response'")
            
            if (response.isNullOrBlank()) {
                println("GitHubModelsService: Empty API response")
                return@withContext null
            }
            
            // Parse calorie and protein values
            val result = parseCaloriesAndProtein(response)
            
            if (result != null) {
                val (calories, protein) = result
                if (calories in 20..5000 && protein in 0..500) {
                    println("GitHubModelsService: ✓ Estimated $calories cal, $protein g protein for '$foodName'")
                    return@withContext result
                } else {
                    println("GitHubModelsService: ✗ Invalid values: $calories cal, $protein g protein")
                    return@withContext null
                }
            } else {
                println("GitHubModelsService: ✗ Failed to parse response")
                return@withContext null
            }
            
        } catch (e: Exception) {
            println("GitHubModelsService ERROR: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }
    
    /**
     * Call GitHub Models API
     */
    private fun callGitHubAPI(prompt: String): String? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(GITHUB_ENDPOINT)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true
            connection.doInput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            val requestBody = JSONObject().apply {
                put("model", modelName)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are a calorie estimation bot. Return ONLY numbers, nothing else.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.0)
                put("top_p", 1.0)
                put("max_tokens", 20)
            }

            OutputStreamWriter(connection.outputStream).use { it.write(requestBody.toString()) }

            val code = connection.responseCode
            println("GitHubModelsService: HTTP $code")
            
            if (code == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val choices = jsonResponse.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.optJSONObject("message")
                    val content = message?.optString("content")
                    return content?.trim()
                }
            } else {
                val err = connection.errorStream?.bufferedReader()?.use { it.readText() }
                if (!err.isNullOrBlank()) println("GitHubModelsService: Error: $err")
            }
            return null
        } catch (e: Exception) {
            println("GitHubModelsService: API call failed - ${e.message}")
            e.printStackTrace()
            return null
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * Parse calories and protein from response
     * Expects format: "calories protein" or extracts first two numbers
     */
    private fun parseCaloriesAndProtein(response: String): Pair<Int, Int>? {
        try {
            val trimmed = response.trim()
            
            // Extract all numbers from response
            val numbers = Regex("(\\d+)").findAll(trimmed)
                .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
                .toList()
            
            // Need at least 2 numbers
            if (numbers.size >= 2) {
                val calories = numbers[0]
                val protein = numbers[1]
                return Pair(calories, protein)
            }
            
            // If only one number, assume it's calories and protein is 0
            if (numbers.size == 1) {
                return Pair(numbers[0], 0)
            }
            
            return null
        } catch (e: Exception) {
            println("GitHubModelsService: Parse error - ${e.message}")
            return null
        }
    }
    
    /**
     * Parse calories from response (legacy method for compatibility)
     */
    private fun parseCalories(response: String): Int? {
        // Try JSON first: {"calories": 123}
        try {
            val trimmed = response.trim()
            if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || trimmed.contains("\"calories\"")) {
                val obj = JSONObject(trimmed)
                val value = obj.optInt("calories", Int.MIN_VALUE)
                if (value != Int.MIN_VALUE) return value
            }
        } catch (_: Exception) {
            // Not JSON, continue
        }

        // Extract first integer (2-4 digits)
        val match = Regex("(\\d{2,4})").find(response)
        val value = match?.groupValues?.getOrNull(1)?.toIntOrNull()
        return value?.takeIf { it in 20..5000 }
    }
    
    /**
     * Quick connectivity test
     */
    suspend fun ping(): Boolean = withContext(Dispatchers.IO) {
        val prompt = "Return the number 123 only. No words, no punctuation."
        val resp = callGitHubAPI(prompt)
        val num = resp?.trim()?.replace(Regex("[^0-9]"), "")?.toIntOrNull()
        return@withContext num == 123
    }
    
    /**
     * Send a chat message and get response
     */
    suspend fun sendChat(message: String): String? = withContext(Dispatchers.IO) {
        return@withContext callGitHubAPI(message)
    }
}
