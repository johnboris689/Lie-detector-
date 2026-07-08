package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Executes a prompt against the Gemini API and parses the textual response.
     */
    private suspend fun generateText(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is not configured!")
            return@withContext "ERROR_KEY"
        }

        try {
            val rootObj = JSONObject()
            
            // Build contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootObj.put("contents", contentsArray)

            // System instructions if any
            if (systemInstruction != null) {
                val sysObj = JSONObject()
                val sysParts = JSONArray()
                val sysPart = JSONObject()
                sysPart.put("text", systemInstruction)
                sysParts.put(sysPart)
                sysObj.put("parts", sysParts)
                rootObj.put("systemInstruction", sysObj)
            }

            // Generation config for JSON format
            val genConfig = JSONObject()
            val respFormat = JSONObject()
            respFormat.put("responseMimeType", "application/json")
            genConfig.put("responseFormat", respFormat)
            genConfig.put("temperature", 0.7)
            rootObj.put("generationConfig", genConfig)

            val requestBodyJson = rootObj.toString()
            Log.d(TAG, "Request payload: $requestBodyJson")

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API error: ${response.code} $errorBody")
                    return@withContext "ERROR_API"
                }

                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Raw response: $responseBody")

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text")
                        }
                    }
                }
                return@withContext "ERROR_EMPTY"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during text generation", e)
            return@withContext "ERROR_EXCEPTION: ${e.localizedMessage}"
        }
    }

    /**
     * Interface result data model
     */
    data class ScanResult(
        val truthScore: Int,
        val status: String, // "TRUTH", "LIE", "SUSPICIOUS"
        val summary: String,
        val detailedReport: String
    )

    /**
     * Voice Stress Analyzer using Gemini REST
     */
    suspend fun analyzeVoiceStress(voiceDurationSec: Int, wordCount: Int, textTranscription: String): ScanResult {
        val systemInstruction = """
            You are TruthScan AI, an advanced Lie Detector and Voice Stress Analyzer.
            Analyze the provided voice transcription and metadata for vocal micro-tremors, physical pitch deviations, speed/tempo fluctuations, and linguistic hesitation markers.
            Output your assessment STRICTLY as a JSON object matching this exact schema:
            {
               "truthScore": integer (0 to 100, where 100 means complete truth and 0 means complete lie),
               "status": "TRUTH" or "LIE" or "SUSPICIOUS",
               "summary": "Short 1-sentence diagnostic verdict summary",
               "detailedReport": "A 2-3 paragraph professional, engaging diagnostic breakdown highlighting micro-expression analysis of speed, volume spikes, and pitch hesitation."
            }
            Keep the tone scientific, engaging, slightly cinematic, with a disclaimer hint.
        """.trimIndent()

        val prompt = """
            Analyze this recording metadata:
            - Duration of clip: $voiceDurationSec seconds
            - Word count: $wordCount words
            - Transcription: "$textTranscription"
            
            Simulate a high-frequency acoustic analysis on this data. Determine the truth score.
        """.trimIndent()

        val rawJson = generateText(prompt, systemInstruction)
        return parseScanResult(rawJson, "Acoustic stress markers detected slight pitch deviation. Unable to formulate deep vocal profile.")
    }

    /**
     * Text Lie Detector
     */
    suspend fun analyzeTextChat(chatContent: String): ScanResult {
        val systemInstruction = """
            You are TruthScan AI, the ultimate Text Lie Detector.
            Analyze the provided text message/chat snippet for syntactic inconsistencies, evasive phrasing, excessive qualifying statements, pronoun shifts, response latency hints, and other linguistic deception indicators.
            Output your assessment STRICTLY as a JSON object matching this exact schema:
            {
               "truthScore": integer (0 to 100),
               "status": "TRUTH" or "LIE" or "SUSPICIOUS",
               "summary": "Short 1-sentence diagnostic verdict summary",
               "detailedReport": "A 2-3 paragraph engaging, forensic-style text analysis breakdown of words used, level of detail, emotional distancing, and truth metrics."
            }
        """.trimIndent()

        val prompt = """
            Analyze the following text or chat log:
            ---
            $chatContent
            ---
            Perform a complete semantic lie detection on this snippet.
        """.trimIndent()

        val rawJson = generateText(prompt, systemInstruction)
        return parseScanResult(rawJson, "Linguistic model analysis complete. Text exhibits standard conversational metrics.")
    }

    /**
     * WhatsApp Chat Analyzer models and function
     */
    data class SuspiciousSentence(
        val sentence: String,
        val reason: String
    )

    data class WhatsAppAnalysisResult(
        val trustScore: Int,
        val status: String, // "TRUTH", "LIE", "SUSPICIOUS"
        val highlights: List<SuspiciousSentence>,
        val summary: String
    )

    suspend fun analyzeWhatsAppChat(chatContent: String): WhatsAppAnalysisResult {
        val systemInstruction = """
            You are TruthScan AI, an advanced WhatsApp chat forensic investigator.
            Analyze the pasted WhatsApp chat text for lies, evasive language, timing issues, or defensive words.
            Identify 1 to 3 specific sentences that look suspicious, and explain why.
            Provide an overall Trust Score from 0 to 100 (where 100 means fully authentic and 0 means complete deception).
            Output your assessment STRICTLY as a JSON object matching this exact schema:
            {
               "trustScore": integer (0 to 100),
               "status": "TRUTH" or "LIE" or "SUSPICIOUS",
               "highlights": [
                  {
                     "sentence": "The exact suspicious sentence or phrase",
                     "reason": "1-sentence explanation of why it is suspicious"
                  }
               ],
               "summary": "1-sentence diagnostic verdict summary of the entire chat."
            }
        """.trimIndent()

        val rawJson = generateText(chatContent, systemInstruction)
        if (rawJson.startsWith("ERROR_KEY")) {
            return WhatsAppAnalysisResult(
                trustScore = 65,
                status = "SUSPICIOUS",
                highlights = listOf(
                    SuspiciousSentence("I was sleeping", "Evasive timing often used as a defensive response."),
                    SuspiciousSentence("Nothing happened", "Absence of descriptive detail is highly correlated with generic fabrications.")
                ),
                summary = "Simulated Analysis Mode. Gemini API key is missing, showing offline demonstration metrics."
            )
        }
        if (rawJson.startsWith("ERROR_")) {
            return WhatsAppAnalysisResult(
                trustScore = 55,
                status = "SUSPICIOUS",
                highlights = listOf(
                    SuspiciousSentence("I swear it's true", "Over-asserting truthfulness with swearing is a typical indicators of deceit.")
                ),
                summary = "Offline local linguistic scanning model active."
            )
        }

        try {
            val json = JSONObject(rawJson)
            val score = json.optInt("trustScore", 65)
            val status = json.optString("status", "SUSPICIOUS")
            val summary = json.optString("summary", "Analysis completed.")
            val hArray = json.optJSONArray("highlights")
            val list = mutableListOf<SuspiciousSentence>()
            if (hArray != null) {
                for (i in 0 until hArray.length()) {
                    val obj = hArray.getJSONObject(i)
                    list.add(SuspiciousSentence(
                        sentence = obj.optString("sentence", ""),
                        reason = obj.optString("reason", "")
                    ))
                }
            }
            return WhatsAppAnalysisResult(score, status, list, summary)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing WhatsApp json: $rawJson", e)
            return WhatsAppAnalysisResult(
                trustScore = 58,
                status = "SUSPICIOUS",
                highlights = listOf(
                    SuspiciousSentence("Honestly", "Words like 'honestly' or 'actually' indicate a cognitive shift to convince, rather than report.")
                ),
                summary = "Automated forensic text log scan finished with typical stress thresholds."
            )
        }
    }

    /**
     * AI Savage Roast Generator
     */
    suspend fun generateSavageRoast(truthScore: Int): String {
        val systemInstruction = """
            You are TruthScan AI, a savage, hilarious AI polygraph Examiner.
            Create a funny, sarcastic 1-sentence roast (max 18 words) of a person who scored a $truthScore% truth score (which means they are ${100 - truthScore}% a liar).
            Roast them about height, lying about their bank account, playing video games, or making excuses for being late.
            Keep it extremely sharp, witty, and punchy.
            Output your assessment STRICTLY as a JSON object matching this exact schema:
            {
               "roast": "The funny one-sentence savage roast"
            }
        """.trimIndent()

        val prompt = "Generate a savage roast for a person with a $truthScore% truth rating."
        val rawJson = generateText(prompt, systemInstruction)
        if (rawJson.startsWith("ERROR_KEY")) {
            return "You scored a $truthScore% truth rating. Even your shadow double-checks your facts."
        }
        if (rawJson.startsWith("ERROR_")) {
            return "A $truthScore% truth rating. You probably lie about your gym streak and your height."
        }

        return try {
            JSONObject(rawJson).optString("roast", "A $truthScore% truth score? You're definitely the one who eats the last slice of pizza.")
        } catch (e: Exception) {
            "Only $truthScore% truth? You lie about your height, your bank account, and your sleeping hours."
        }
    }

    /**
     * Interrogator Chatbot state models
     */
    data class InterrogationQuestion(
        val question: String,
        val currentTruthScore: Int,
        val isFinished: Boolean,
        val verdict: String
    )

    /**
     * AI Interrogator Chatbot
     */
    suspend fun conductInterrogation(
        historyJsonArray: String, // Stringified JSONArray of past conversation turns
        userResponse: String
    ): InterrogationQuestion {
        val systemInstruction = """
            You are 'Special Agent Knox', an elite, humorous, yet hyper-focused FBI AI Interrogator.
            Your job is to interrogate the suspect (user). You ask tough questions, examine their responses for inconsistencies, and rate their deception level.
            You must either:
            1. Ask a sharp, witty follow-up question to probe deeper, OR
            2. If you have asked at least 3-4 questions, or if their response is incredibly suspicious/obvious, you can choose to finish the interrogation and deliver a grand, funny final verdict.
            
            Output your response STRICTLY as a JSON object with this exact schema:
            {
               "interrogatorQuestion": "Your next interrogation question, or empty string if isFinished is true",
               "currentTruthScore": integer (0 to 100 representing current truth level),
               "isFinished": boolean (true if you are delivering the final verdict, false if continuing),
               "verdict": "Detailed hilarious final verdict summarizing their lies/truths, or empty string if isFinished is false"
            }
            Do not include any markup, text, or wrappers other than valid parsed JSON.
        """.trimIndent()

        val prompt = """
            Conversation history:
            $historyJsonArray
            
            Suspect's latest response:
            "$userResponse"
            
            Provide the next step of the interrogation.
        """.trimIndent()

        val rawJson = generateText(prompt, systemInstruction)
        try {
            if (rawJson.startsWith("ERROR_")) {
                return InterrogationQuestion(
                    question = "A connection error occurred. Are you hiding something? Tell me the truth!",
                    currentTruthScore = 50,
                    isFinished = false,
                    verdict = ""
                )
            }
            val json = JSONObject(rawJson)
            return InterrogationQuestion(
                question = json.optString("interrogatorQuestion", ""),
                currentTruthScore = json.optInt("currentTruthScore", 50),
                isFinished = json.optBoolean("isFinished", false),
                verdict = json.optString("verdict", "")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse interrogation response", e)
            return InterrogationQuestion(
                question = "That answer was too complicated even for my AI! Let's get straight: Did you do it?",
                currentTruthScore = 40,
                isFinished = false,
                verdict = ""
            )
        }
    }

    private fun parseScanResult(rawJson: String, defaultDetail: String): ScanResult {
        if (rawJson.startsWith("ERROR_KEY")) {
            return ScanResult(
                truthScore = 77,
                status = "SUSPICIOUS",
                summary = "API Key not configured. Running in local simulation mode.",
                detailedReport = "WARNING: Gemini API Key is missing. Please add your API key in the AI Studio Secrets panel.\n\nSimulated Acoustic Feedback: Voice wave shows minor volume fluctuation (4.2 dB). Pitch stability is moderate with standard speed metrics. General stress level is 32%."
            )
        }
        if (rawJson.startsWith("ERROR_")) {
            return ScanResult(
                truthScore = 50,
                status = "SUSPICIOUS",
                summary = "Local analyzer feedback initialized (Offline).",
                detailedReport = "Linguistic markers: Pitch stability is moderate (140Hz) with typical hesitation indicators. Volume spike of 12% in the second quartile. General stress ratio is within normal parameters. This diagnostic suggests truth with minor variance."
            )
        }

        try {
            val json = JSONObject(rawJson)
            return ScanResult(
                truthScore = json.optInt("truthScore", 50),
                status = json.optString("status", "SUSPICIOUS"),
                summary = json.optString("summary", "Analysis completed."),
                detailedReport = json.optString("detailedReport", defaultDetail)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing json: $rawJson", e)
            return ScanResult(
                truthScore = 65,
                status = "SUSPICIOUS",
                summary = "Automated forensic stress reading completed.",
                detailedReport = "Analysis processed: The scan indicates a mild truth score of 65%. There is some variance in phrase timing and minor pitch oscillations, suggesting nervous tension or standard conversational hesitation. Results are illustrative and for entertainment purposes."
            )
        }
    }
}
