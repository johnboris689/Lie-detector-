package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.DependencyProvider
import com.example.data.UserProfile
import com.example.ui.AppNavigation
import com.example.ui.Screen
import com.example.ui.theme.TruthScanTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize local Room DB and repositories
        DependencyProvider.initialize(this)
        val repository = DependencyProvider.repository

        enableEdgeToEdge()

        setContent {
            val coroutineScope = rememberCoroutineScope()
            
            // Observe the user profile from the database
            val profileState by repository.userProfile.collectAsState(initial = null)
            
            // Local fallback if database is loading
            val currentProfile = profileState ?: UserProfile(
                id = 1,
                dailyScansRemaining = 3,
                lastScanDate = "",
                isAdsRemoved = false,
                isProPackUnlocked = false,
                isThemePackUnlocked = false,
                isSubscribed = false,
                activeTheme = "Cyber",
                smartwatchSynced = false
            )

            // Trigger profile initialization
            LaunchedEffect(Unit) {
                repository.getOrInitializeProfile()
            }

            // Simple navigation state machine
            var currentScreen by remember { mutableStateOf(Screen.Onboarding) }
            val navigationStack = remember { mutableStateListOf<Screen>() }

            val onNavigate: (Screen) -> Unit = { targetScreen ->
                navigationStack.add(currentScreen)
                currentScreen = targetScreen
            }

            val onBack: () -> Unit = {
                if (navigationStack.isNotEmpty()) {
                    currentScreen = navigationStack.removeLast()
                } else {
                    currentScreen = Screen.Dashboard
                }
            }

            TruthScanTheme(themeName = currentProfile.activeTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        profile = currentProfile,
                        repository = repository,
                        onNavigate = onNavigate,
                        currentScreen = currentScreen,
                        onBack = onBack
                    )
                }
            }
        }
    }
}
