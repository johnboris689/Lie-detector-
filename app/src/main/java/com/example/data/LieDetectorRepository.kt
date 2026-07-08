package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LieDetectorRepository(
    private val scanHistoryDao: ScanHistoryDao,
    private val userProfileDao: UserProfileDao
) {
    val allHistory: Flow<List<ScanHistory>> = scanHistoryDao.getAllHistory()
        .flowOn(Dispatchers.IO)

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfileFlow()
        .flowOn(Dispatchers.IO)

    /**
     * Initializes or retrieves the UserProfile. Resets daily scan count if the date changed.
     */
    suspend fun getOrInitializeProfile(): UserProfile = withContext(Dispatchers.IO) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var profile = userProfileDao.getUserProfile()
        
        if (profile == null) {
            profile = UserProfile(
                id = 1,
                dailyScansRemaining = 3,
                lastScanDate = today,
                isAdsRemoved = false,
                isProPackUnlocked = false,
                isThemePackUnlocked = false,
                isSubscribed = false,
                activeTheme = "Cyber"
            )
            userProfileDao.insertUserProfile(profile)
        } else if (profile.lastScanDate != today) {
            // New day, reset free scan count
            profile = profile.copy(
                dailyScansRemaining = 3,
                lastScanDate = today
            )
            userProfileDao.insertUserProfile(profile)
        }
        return@withContext profile
    }

    /**
     * Checks if the user is allowed to perform a scan.
     * Subscribed or Pro Pack users always have unlimited scans.
     */
    suspend fun canPerformScan(): Boolean = withContext(Dispatchers.IO) {
        val profile = getOrInitializeProfile()
        if (profile.isSubscribed || profile.isProPackUnlocked) {
            return@withContext true
        }
        return@withContext profile.dailyScansRemaining > 0
    }

    /**
     * Decrements scan count for free users.
     */
    suspend fun useScan() = withContext(Dispatchers.IO) {
        val profile = getOrInitializeProfile()
        if (!profile.isSubscribed && !profile.isProPackUnlocked) {
            val updated = profile.copy(
                dailyScansRemaining = (profile.dailyScansRemaining - 1).coerceAtLeast(0)
            )
            userProfileDao.insertUserProfile(updated)
        }
    }

    /**
     * Grants a free bonus "Ultra Scan" (e.g. from watching a rewarded ad).
     */
    suspend fun grantAdScanBonus() = withContext(Dispatchers.IO) {
        val profile = getOrInitializeProfile()
        val updated = profile.copy(
            dailyScansRemaining = profile.dailyScansRemaining + 1
        )
        userProfileDao.insertUserProfile(updated)
    }

    /**
     * Save completed scan history.
     */
    suspend fun saveScan(type: String, score: Int, status: String, summary: String, details: String) = withContext(Dispatchers.IO) {
        val isPro = checkIsPro()
        val history = ScanHistory(
            scanType = type,
            truthScore = score,
            status = status,
            summary = summary,
            detailedReport = details,
            isPro = isPro
        )
        scanHistoryDao.insertHistory(history)
        useScan()
    }

    suspend fun deleteHistory(id: Int) = withContext(Dispatchers.IO) {
        scanHistoryDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        scanHistoryDao.clearAllHistory()
    }

    /**
     * Theme update
     */
    suspend fun updateTheme(themeName: String) = withContext(Dispatchers.IO) {
        val profile = getOrInitializeProfile()
        val updated = profile.copy(activeTheme = themeName)
        userProfileDao.insertUserProfile(updated)
    }

    /**
     * Smartwatch Connection Sync Toggle
     */
    suspend fun toggleSmartwatch(sync: Boolean) = withContext(Dispatchers.IO) {
        val profile = getOrInitializeProfile()
        val updated = profile.copy(smartwatchSynced = sync)
        userProfileDao.insertUserProfile(updated)
    }

    /**
     * Purchases and Billings simulations
     */
    suspend fun simulatePurchase(sku: String): Boolean = withContext(Dispatchers.IO) {
        val profile = getOrInitializeProfile()
        val updated = when (sku) {
            "remove_ads" -> profile.copy(isAdsRemoved = true)
            "pro_pack" -> profile.copy(
                isProPackUnlocked = true,
                isAdsRemoved = true,
                unlockedPacksJson = "[\"dating_pack\",\"interview_pack\",\"family_pack\",\"dare_pack\"]"
            )
            "pro_subscription" -> profile.copy(isSubscribed = true, isAdsRemoved = true)
            "theme_pack" -> profile.copy(isThemePackUnlocked = true)
            "dating_pack", "interview_pack", "family_pack", "dare_pack" -> {
                val currentPacks = try {
                    org.json.JSONArray(profile.unlockedPacksJson)
                } catch(e: Exception) {
                    org.json.JSONArray()
                }
                var found = false
                for (i in 0 until currentPacks.length()) {
                    if (currentPacks.getString(i) == sku) { found = true; break }
                }
                if (!found) currentPacks.put(sku)
                profile.copy(unlockedPacksJson = currentPacks.toString())
            }
            else -> profile
        }
        userProfileDao.insertUserProfile(updated)
        return@withContext true
    }

    suspend fun addCoins(amount: Int) = withContext(Dispatchers.IO) {
        val profile = getOrInitializeProfile()
        val updated = profile.copy(coins = profile.coins + amount)
        userProfileDao.insertUserProfile(updated)
    }

    suspend fun completeChallenge(today: String): Int = withContext(Dispatchers.IO) {
        val profile = getOrInitializeProfile()
        val newStreak = if (profile.lastChallengeDate.isNotEmpty()) {
            profile.streakDays + 1
        } else {
            1
        }
        val bonusCoins = if (newStreak % 7 == 0) 100 else 15
        val updated = profile.copy(
            coins = profile.coins + bonusCoins,
            streakDays = newStreak,
            lastChallengeDate = today
        )
        userProfileDao.insertUserProfile(updated)
        return@withContext bonusCoins
    }

    suspend fun unlockPack(sku: String) = withContext(Dispatchers.IO) {
        val profile = getOrInitializeProfile()
        val currentPacks = try {
            org.json.JSONArray(profile.unlockedPacksJson)
        } catch(e: Exception) {
            org.json.JSONArray()
        }
        var found = false
        for (i in 0 until currentPacks.length()) {
            if (currentPacks.getString(i) == sku) { found = true; break }
        }
        if (!found) currentPacks.put(sku)
        val updated = profile.copy(unlockedPacksJson = currentPacks.toString())
        userProfileDao.insertUserProfile(updated)
    }

    suspend fun restorePurchases() = withContext(Dispatchers.IO) {
        val profile = getOrInitializeProfile()
        val updated = profile.copy(
            isAdsRemoved = true,
            isProPackUnlocked = true,
            isThemePackUnlocked = true,
            isSubscribed = true,
            unlockedPacksJson = "[\"dating_pack\",\"interview_pack\",\"family_pack\",\"dare_pack\"]"
        )
        userProfileDao.insertUserProfile(updated)
    }

    private suspend fun checkIsPro(): Boolean {
        val profile = userProfileDao.getUserProfile()
        return profile?.isSubscribed == true || profile?.isProPackUnlocked == true
    }
}

object DependencyProvider {
    lateinit var database: AppDatabase
    lateinit var repository: LieDetectorRepository

    fun initialize(context: Context) {
        if (!::database.isInitialized) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "truthscan_db"
            )
            .fallbackToDestructiveMigration() // safe default for prototypes
            .build()
            
            repository = LieDetectorRepository(
                database.scanHistoryDao(),
                database.userProfileDao()
            )
        }
    }
}
