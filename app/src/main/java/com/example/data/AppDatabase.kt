package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scanType: String, // "Voice Stress", "Facial Micro-Expression", "Fingerprint Heartbeat", "Text Analysis"
    val timestamp: Long = System.currentTimeMillis(),
    val truthScore: Int, // 0 - 100%
    val status: String, // "TRUTH", "LIE", "SUSPICIOUS"
    val summary: String,
    val detailedReport: String,
    val isPro: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val dailyScansRemaining: Int = 3,
    val lastScanDate: String = "", // YYYY-MM-DD
    val isAdsRemoved: Boolean = false,
    val isProPackUnlocked: Boolean = false,
    val isThemePackUnlocked: Boolean = false,
    val isSubscribed: Boolean = false,
    val activeTheme: String = "Cyber", // "Cyber" (Default Cyan), "Neon" (Magenta/Purple), "FBI" (Military Slate)
    val smartwatchSynced: Boolean = false,
    val coins: Int = 100,
    val streakDays: Int = 3,
    val lastChallengeDate: String = "",
    val unlockedPacksJson: String = "[]"
)

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ScanHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ScanHistory)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM scan_history")
    suspend fun clearAllHistory()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}

@Database(entities = [ScanHistory::class, UserProfile::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun userProfileDao(): UserProfileDao
}
