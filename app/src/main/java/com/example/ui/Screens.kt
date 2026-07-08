package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.*
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

// --- NAVIGATION DESTINATIONS ---
enum class Screen {
    Onboarding,
    Dashboard,
    VoiceStress,
    FaceScanner,
    Fingerprint,
    TextDetector,
    GameMode,
    Interrogator,
    StatsHistory,
    Settings,
    Paywall
}

@Composable
fun AppNavigation(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onNavigate: (Screen) -> Unit,
    currentScreen: Screen,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Back button handler
    BackHandler(enabled = currentScreen != Screen.Dashboard && currentScreen != Screen.Onboarding) {
        onBack()
    }

    Scaffold(
        bottomBar = {
            if (!profile.isAdsRemoved && currentScreen != Screen.Paywall && currentScreen != Screen.Onboarding) {
                MockAdBanner(onClick = { onNavigate(Screen.Paywall) })
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            when (currentScreen) {
                Screen.Onboarding -> OnboardingScreen(onGetStarted = {
                    coroutineScope.launch {
                        onNavigate(Screen.Dashboard)
                    }
                })

                Screen.Dashboard -> DashboardScreen(
                    profile = profile,
                    repository = repository,
                    onNavigate = onNavigate,
                    onShowPaywall = { onNavigate(Screen.Paywall) }
                )

                Screen.VoiceStress -> VoiceStressScreen(
                    profile = profile,
                    repository = repository,
                    onBack = onBack,
                    onShowPaywall = { onNavigate(Screen.Paywall) }
                )

                Screen.FaceScanner -> FaceScannerScreen(
                    profile = profile,
                    repository = repository,
                    onBack = onBack,
                    onShowPaywall = { onNavigate(Screen.Paywall) }
                )

                Screen.Fingerprint -> FingerprintScreen(
                    profile = profile,
                    repository = repository,
                    onBack = onBack,
                    onShowPaywall = { onNavigate(Screen.Paywall) }
                )

                Screen.TextDetector -> TextDetectorScreen(
                    profile = profile,
                    repository = repository,
                    onBack = onBack,
                    onShowPaywall = { onNavigate(Screen.Paywall) }
                )

                Screen.GameMode -> GameModeScreen(
                    profile = profile,
                    onBack = onBack,
                    onShowPaywall = { onNavigate(Screen.Paywall) }
                )

                Screen.Interrogator -> InterrogatorScreen(
                    profile = profile,
                    onBack = onBack,
                    onShowPaywall = { onNavigate(Screen.Paywall) }
                )

                Screen.StatsHistory -> StatsHistoryScreen(
                    repository = repository,
                    onBack = onBack
                )

                Screen.Settings -> SettingsScreen(
                    profile = profile,
                    repository = repository,
                    onBack = onBack,
                    onShowPaywall = { onNavigate(Screen.Paywall) }
                )

                Screen.Paywall -> PaywallScreen(
                    profile = profile,
                    repository = repository,
                    onBack = onBack
                )
            }
        }
    }
}

// --- REUSABLE COMPONENTS ---

@Composable
fun MockAdBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(Color(0xFF101015))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            .clickable { onClick() }
            .testTag("ad_banner"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "AD",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Remove Ads Forever & Unlock Full Scanner Package! Click Here",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, trailingIcon: @Composable (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                .testTag("back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        if (trailingIcon != null) {
            trailingIcon()
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

// --- ONBOARDING SCREEN ---
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var slideIndex by remember { mutableStateOf(0) }

    val titles = listOf(
        "AI Lie Polygraph",
        "Bio-Frequency Scan",
        "Disclaimer Notice"
    )

    val descriptions = listOf(
        "Utilize advanced, cutting-edge AI vocal acoustics and micro-expression scans to measure biological deception metrics in real time.",
        "Synthesize instant pulse calculations, EKG cardiac lines, and WhatsApp text analysis directly on your device.",
        "TruthScan AI is for ENTERTAINMENT PURPOSES ONLY. It does not provide true polygraph or scientific biometric accuracy. Enjoy responsibly with friends!"
    )

    val icons = listOf(
        Icons.Default.GraphicEq,
        Icons.Default.CameraFront,
        Icons.Default.Security
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo / Title Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Troubleshoot,
                contentDescription = "App Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .shadow(16.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TRUTHSCAN AI",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Ultimate Lie Detector",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }

        // Slide Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = icons[slideIndex],
                contentDescription = "Slide Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = titles[slideIndex].uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = descriptions[slideIndex],
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        // Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                for (i in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(if (slideIndex == i) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (slideIndex == i) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (slideIndex < 2) {
                        slideIndex++
                    } else {
                        onGetStarted()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_next_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (slideIndex < 2) "CONTINUE" else "ACCEPT & START",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// --- DASHBOARD SCREEN ---
// --- MULTI-TAB CONTROLLER & SCREENS ---
enum class DashboardTab {
    Home,
    Couple,
    Party,
    Daily,
    Store,
    Profile
}

@Composable
fun DashboardScreen(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onNavigate: (Screen) -> Unit,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf(DashboardTab.Home) }

    // State for WhatsApp chat analyzer dialog
    var showChatAnalyzer by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Home,
                    onClick = { activeTab = DashboardTab.Home },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Couple,
                    onClick = { activeTab = DashboardTab.Couple },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Couple") },
                    label = { Text("Couple", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Party,
                    onClick = { activeTab = DashboardTab.Party },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Party") },
                    label = { Text("Party", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Daily,
                    onClick = { activeTab = DashboardTab.Daily },
                    icon = { 
                        Box {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Daily")
                            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                            if (profile.lastChallengeDate != today) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Red, CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    },
                    label = { Text("Daily", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Store,
                    onClick = { activeTab = DashboardTab.Store },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Store") },
                    label = { Text("Store", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Profile,
                    onClick = { activeTab = DashboardTab.Profile },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                DashboardTab.Home -> {
                    HomeTabContent(
                        profile = profile,
                        onNavigate = onNavigate,
                        onShowPaywall = onShowPaywall,
                        onOpenChatAnalyzer = { showChatAnalyzer = true }
                    )
                }
                DashboardTab.Couple -> {
                    CoupleTabContent(
                        profile = profile,
                        repository = repository,
                        onShowPaywall = onShowPaywall
                    )
                }
                DashboardTab.Party -> {
                    PartyTabContent(
                        profile = profile,
                        repository = repository,
                        onShowPaywall = onShowPaywall
                    )
                }
                DashboardTab.Daily -> {
                    DailyTabContent(
                        profile = profile,
                        repository = repository,
                        onShowPaywall = onShowPaywall
                    )
                }
                DashboardTab.Store -> {
                    StoreTabContent(
                        profile = profile,
                        repository = repository
                    )
                }
                DashboardTab.Profile -> {
                    ProfileTabContent(
                        profile = profile,
                        repository = repository,
                        onNavigate = onNavigate,
                        onShowPaywall = onShowPaywall
                    )
                }
            }

            // WhatsApp Chat Analyzer Dialog Overlay
            if (showChatAnalyzer) {
                WhatsAppChatAnalyzerDialog(
                    onDismiss = { showChatAnalyzer = false }
                )
            }
        }
    }
}

@Composable
fun SavageRoastView(truthScore: Int) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var roastText by remember { mutableStateOf("") }
    var isLoadingRoast by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 AI SAVAGE ROAST",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                if (roastText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "$roastText - Generated with TruthScan AI 🔍🔥")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Roast"))
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoadingRoast) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.secondary)
            } else if (roastText.isEmpty()) {
                Text(
                    text = "Generate a customized, hilariously savage AI roast based on your scan metrics!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        isLoadingRoast = true
                        coroutineScope.launch {
                            roastText = GeminiClient.generateSavageRoast(truthScore)
                            isLoadingRoast = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("GENERATE ROAST", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "\"$roastText\"",
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "$roastText - Generated with TruthScan AI 🔍🔥")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Roast"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SHARE ROAST", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WhatsAppChatAnalyzerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var chatText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<GeminiClient.WhatsAppAnalysisResult?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Chat, contentDescription = "WhatsApp Forensic", tint = Color(0xFF25D366))
                Spacer(modifier = Modifier.width(8.dp))
                Text("WhatsApp Forensic Analyzer", fontSize = 18.sp, color = Color(0xFF25D366), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (result == null) {
                    Text(
                        text = "Paste WhatsApp message logs to run linguistic deception analysis. AI will highlight suspicious sentences.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = chatText,
                        onValueChange = { chatText = it },
                        placeholder = { Text("e.g. \"Honestly, I was with Jeff. I swear nothing happened...\"", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF25D366),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        ),
                        maxLines = 6
                    )
                } else {
                    val score = result!!.trustScore
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "TRUST SCORE: $score%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (score >= 70) Color.Green else if (score >= 40) Color.Yellow else Color.Red
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    (if (score >= 70) Color.Green else if (score >= 40) Color.Yellow else Color.Red).copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERDICT: ${result!!.status}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (score >= 70) Color.Green else if (score >= 40) Color.Yellow else Color.Red
                            )
                        }

                        Text(
                            text = "FORENSIC DETECTION FLAGS:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        result!!.highlights.forEach { flag ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "\"${flag.sentence}\"",
                                        fontSize = 13.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = flag.reason,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result!!.summary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (result == null) {
                Button(
                    onClick = {
                        if (chatText.trim().isEmpty()) {
                            Toast.makeText(context, "Please paste some message logs!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        coroutineScope.launch {
                            result = GeminiClient.analyzeWhatsAppChat(chatText)
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("ANALYZE TRANSCRIPT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                Button(
                    onClick = { result = null; chatText = "" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text("ANALYZE NEW CHAT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("CLOSE", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
fun HomeTabContent(
    profile: UserProfile,
    onNavigate: (Screen) -> Unit,
    onShowPaywall: () -> Unit,
    onOpenChatAnalyzer: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App branding top header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TRUTHSCAN AI",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Acoustic & Biometric Polygraph HUD",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onNavigate(Screen.StatsHistory) },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                            .testTag("stats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Stats",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(Screen.Settings) },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Daily Limit Tracker & Pro Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (profile.isSubscribed || profile.isProPackUnlocked) {
                                listOf(Color(0xFFD4AF37), Color(0xFFAA7C11))
                            } else {
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.surface
                                )
                            }
                        )
                    )
                    .border(
                        1.dp,
                        if (profile.isSubscribed || profile.isProPackUnlocked) Color(0xFFD4AF37)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (profile.isSubscribed || profile.isProPackUnlocked) "PREMIUM ACTIVATED" else "FREE TIER PLAN",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (profile.isSubscribed || profile.isProPackUnlocked) Color.Black else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (profile.isSubscribed || profile.isProPackUnlocked) "Unlimited Lie Detection Scans"
                            else "Daily Scans Remaining: ${profile.dailyScansRemaining}/3",
                            fontSize = 14.sp,
                            color = if (profile.isSubscribed || profile.isProPackUnlocked) Color.Black.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (!profile.isSubscribed && !profile.isProPackUnlocked) {
                        Button(
                            onClick = onShowPaywall,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("GO PRO", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Pro",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Radar Scan visual centerpiece
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Pulse waves
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "scale"
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "alpha"
                )

                val radarSweepAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "sweep"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val maxRadius = size.width.coerceAtMost(size.height) / 2.2f

                    // Draw outer radar circles
                    drawCircle(
                        color = CyberPrimary.copy(alpha = 0.1f),
                        radius = maxRadius,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = CyberPrimary.copy(alpha = 0.15f),
                        radius = maxRadius * 0.7f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = CyberPrimary.copy(alpha = 0.2f),
                        radius = maxRadius * 0.4f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Draw grid axes
                    drawLine(
                        color = CyberPrimary.copy(alpha = 0.15f),
                        start = Offset(center.x - maxRadius, center.y),
                        end = Offset(center.x + maxRadius, center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = CyberPrimary.copy(alpha = 0.15f),
                        start = Offset(center.x, center.y - maxRadius),
                        end = Offset(center.x, center.y + maxRadius),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw animated pulse wave
                    drawCircle(
                        color = CyberPrimary.copy(alpha = pulseAlpha),
                        radius = maxRadius * pulseScale,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw swept line representing search sweep
                    drawArc(
                        color = CyberPrimary.copy(alpha = 0.12f),
                        startAngle = radarSweepAngle,
                        sweepAngle = 40f,
                        useCenter = true,
                        size = Size(maxRadius * 2, maxRadius * 2),
                        topLeft = Offset(center.x - maxRadius, center.y - maxRadius)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Radar Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SYSTEM STATUS: MONITORING ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Feature Grid header
        item {
            Text(
                text = "BIOMETRIC ANALYZERS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Primary Grid Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "Voice Stress",
                    description = "Analyze acoustic micro-tremors & speed.",
                    icon = Icons.Default.RecordVoiceOver,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.VoiceStress) },
                    testTag = "voice_stress_card"
                )

                DashboardCard(
                    title = "Facial Scanner",
                    description = "Scan micro blinks & emotional tells.",
                    icon = Icons.Default.Face,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.FaceScanner) },
                    testTag = "face_scanner_card"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "Heartbeat Sync",
                    description = "Fingertip camera pulse polygraph.",
                    icon = Icons.Default.Fingerprint,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Fingerprint) },
                    testTag = "fingerprint_card"
                )

                DashboardCard(
                    title = "Text Analyzer",
                    description = "Paste chat messages to check lies.",
                    icon = Icons.Default.ContentPaste,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.TextDetector) },
                    testTag = "text_analyzer_card"
                )
            }
        }

        // NEW WhatsApp Forensic Analyzer card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChatAnalyzer() }
                    .testTag("whatsapp_forensic_card"),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F2D1F), // Dark forest green
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp Forensic",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WHATSAPP CHAT DETECTOR",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF25D366),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Extract truth scores and highlight highly suspicious lines in messaging transcripts.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Secondary Modes header
        item {
            Text(
                text = "INTERACTIVE GAME CHANNELS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "AI Interrogator",
                    description = "Chat with Special Agent Knox polygraph.",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Interrogator) },
                    testTag = "ai_interrogator_card"
                )

                DashboardCard(
                    title = "Truth or Dare",
                    description = "100+ questions with QR multiplayer lobby.",
                    icon = Icons.Default.SportsEsports,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.GameMode) },
                    testTag = "truth_or_dare_card"
                )
            }
        }
    }
}

@Composable
fun CoupleTabContent(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Couple Mode State Holder
    var name1 by remember { mutableStateOf("Player 1") }
    var name2 by remember { mutableStateOf("Player 2") }
    var isGameStarted by remember { mutableStateOf(false) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var currentPlayerTurn by remember { mutableStateOf(1) } // 1 or 2
    
    // Scan simulation states
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    val p1Scores = remember { mutableStateListOf<Int>() }
    val p2Scores = remember { mutableStateListOf<Int>() }
    var activeBpm by remember { mutableStateOf(72) }
    
    // Post game report states
    var isGameFinished by remember { mutableStateOf(false) }
    var showExportOverlay by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf(0f) }

    val coupleQuestions = listOf(
        "Have you ever looked through your partner's phone without telling them?",
        "Do you have a secret crush on someone else right now?",
        "Have you ever lied about finding your partner's friends attractive?",
        "Do you think you are the more romantic and committed one here?",
        "Have you ever kept a secret from your partner about an ex?",
        "Would you share your phone screen lock password this second?",
        "Have you ever pretended to like a gift your partner got you?",
        "Do you think your partner is an amazing driver?",
        "Have you ever lied to avoid going on a date with your partner?",
        "Do you sometimes think you are way out of your partner's league?"
    )

    // Simulating scanning
    LaunchedEffect(isScanning) {
        if (isScanning) {
            scanProgress = 0f
            while (scanProgress < 1.0f) {
                delay(30)
                scanProgress += 0.03f
                activeBpm = (70..105).random()
            }
            isScanning = false
            val score = (45..98).random()
            if (currentPlayerTurn == 1) {
                p1Scores.add(score)
                currentPlayerTurn = 2
            } else {
                p2Scores.add(score)
                if (p1Scores.size >= 4) {
                    isGameFinished = true
                } else {
                    currentPlayerTurn = 1
                    currentQuestionIndex = (currentQuestionIndex + 1) % coupleQuestions.size
                }
            }
        }
    }

    // Simulating 9:16 video export
    LaunchedEffect(isExporting) {
        if (isExporting) {
            exportProgress = 0f
            while (exportProgress < 1.0f) {
                delay(100)
                exportProgress += 0.02f
            }
            isExporting = false
            showExportOverlay = false
            Toast.makeText(context, "Meme Video exported to /Gallery/TruthScan_Couple.mp4!", Toast.LENGTH_LONG).show()
        }
    }

    if (showExportOverlay) {
        // High fidelity full screen 9:16 Video Export Preview
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFFFF2A54), RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2C0F17), Color(0xFF0F0B13))
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TRUTHSCAN AI",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFF2A54),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "COUPLE RELATIONSHIP CERTIFICATE",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Heart Centerpiece
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart",
                        tint = Color(0xFFFF2A54),
                        modifier = Modifier
                            .size(70.dp)
                            .shadow(24.dp, CircleShape, spotColor = Color(0xFFFF2A54))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    val p1Avg = if (p1Scores.isEmpty()) 80 else p1Scores.average().toInt()
                    val p2Avg = if (p2Scores.isEmpty()) 75 else p2Scores.average().toInt()
                    val comp = (p1Avg + p2Avg) / 2
                    Text(
                        text = "COMPATIBILITY: $comp%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.Green
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "VALENTINE TRUST RATING",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bottom diagnostic wave
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$name1: ${p1Scores.average().toInt()}% Truth | $name2: ${p2Scores.average().toInt()}% Truth",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "WWW.TRUTHSCANAI.APP  •  FOR ENTERTAINMENT ONLY",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Export Progress Dialog on top
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFFF2A54).copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "COMPILING 9:16 MEME VIDEO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF2A54)
                        )
                        LinearProgressIndicator(
                            progress = exportProgress,
                            color = Color(0xFFFF2A54),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Watermarking frame structures & blending heartbeat tracks... ${(exportProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "COUPLE TRUST SCAN",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFFF2A54),
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Turn-Based Spicy Relationship Screening",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        if (!isGameStarted) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFFF2A54).copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Pre-Made Spicy Matchup Setup",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF2A54)
                        )
                        OutlinedTextField(
                            value = name1,
                            onValueChange = { name1 = it },
                            label = { Text("Player 1 Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = name2,
                            onValueChange = { name2 = it },
                            label = { Text("Player 2 Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                p1Scores.clear()
                                p2Scores.clear()
                                currentQuestionIndex = 0
                                currentPlayerTurn = 1
                                isGameFinished = false
                                isGameStarted = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A54))
                        ) {
                            Text("START SPICY SCREENING", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (isGameFinished) {
            // Summary matching certificate
            item {
                val p1Avg = if (p1Scores.isEmpty()) 80 else p1Scores.average().toInt()
                val p2Avg = if (p2Scores.isEmpty()) 75 else p2Scores.average().toInt()
                val compatibility = (p1Avg + p2Avg) / 2

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFFF2A54).copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = "Heart", tint = Color(0xFFFF2A54), modifier = Modifier.size(50.dp))
                        Text(text = "MATCH COMPATIBILITY", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFF2A54))
                        Text(text = "$compatibility%", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = Color.Green)
                        
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        
                        Text(text = "$name1 Truth Average: $p1Avg%", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$name2 Truth Average: $p2Avg%", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        
                        Text(
                            text = if (compatibility >= 80) "Perfect Harmony! Pure genuine hearts."
                            else if (compatibility >= 50) "Passable Trust! Some minor secrets detected."
                            else "Danger Zone! Secrets and high bio-stress recorded.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isExporting = true
                                showExportOverlay = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A54))
                        ) {
                            Text("EXPORT 9:16 SHARE VIDEO", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { isGameStarted = false; isGameFinished = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NEW GAME", color = Color(0xFFFF2A54))
                        }
                    }
                }
            }
        } else {
            // Turn based loop
            item {
                val activeName = if (currentPlayerTurn == 1) name1 else name2
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFFF2A54).copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "QUESTION ${p1Scores.size + if (currentPlayerTurn == 2) 1 else 0} OF 4",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF2A54)
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF2A54).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = coupleQuestions[currentQuestionIndex],
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "$activeName's TURN",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (isScanning) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("BIOLOGICAL ACOUSTIC SCANNING...", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF2A54))
                                LinearProgressIndicator(progress = scanProgress, color = Color(0xFFFF2A54), modifier = Modifier.fillMaxWidth())
                                Text("Pulsing Biometrics: $activeBpm bpm", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Button(
                                onClick = { isScanning = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A54))
                            ) {
                                Text("SCAN BIOMETRICS", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartyTabContent(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // States
    val playersList = remember { mutableStateListOf<String>("Sarah", "Alex", "Marcus", "Dave") }
    var currentNameInput by remember { mutableStateOf("") }
    var isPartyStarted by remember { mutableStateOf(false) }
    var currentPlayerIndex by remember { mutableStateOf(0) }
    val playerScores = remember { mutableStateMapOf<String, MutableList<Int>>() } // Maps player name to a list of scores
    
    // Scan dials
    var isRunningScan by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var scanResultVerdict by remember { mutableStateOf("") }
    var scanResultScore by remember { mutableStateOf(0) }
    
    // End game trophy animation
    var showTrophyAnim by remember { mutableStateOf(false) }

    val partyQuestions = listOf(
        "Have you ever blamed a smelly fart on a pet?",
        "Do you secretly have a favorite sibling or friend in this room?",
        "Have you ever lied about why you were late to work or a party?",
        "Did you actually floss your teeth today?",
        "Have you ever looked at another person's journal or diaries?",
        "Do you sometimes browse social media in the bathroom for over 30 mins?",
        "Have you ever eaten something off the floor and lied about it?"
    )

    // Running Pass & Play party scanner
    LaunchedEffect(isRunningScan) {
        if (isRunningScan) {
            scanProgress = 0f
            while (scanProgress < 1.0f) {
                delay(30)
                scanProgress += 0.05f
            }
            isRunningScan = false
            scanResultScore = (10..98).random()
            scanResultVerdict = if (scanResultScore >= 70) "TRUTH GOD" else if (scanResultScore >= 40) "SUSPICIOUS" else "BIGGEST LIAR"
            
            val activePlayer = playersList[currentPlayerIndex]
            if (!playerScores.containsKey(activePlayer)) {
                playerScores[activePlayer] = mutableListOf()
            }
            playerScores[activePlayer]?.add(scanResultScore)
            
            currentPlayerIndex = (currentPlayerIndex + 1) % playersList.size
        }
    }

    if (showTrophyAnim) {
        // Trophy Canvas Animation with confetti
        val trophyScale = remember { Animatable(0.3f) }
        val trophyRotate = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            trophyScale.animateTo(1.2f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            trophyScale.animateTo(1.0f)
            trophyRotate.animateTo(360f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "PARTY CONQUEROR", style = MaterialTheme.typography.displaySmall, color = Color(0xFFD4AF37), fontWeight = FontWeight.Black)
                
                Canvas(
                    modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer(scaleX = trophyScale.value, scaleY = trophyScale.value, rotationZ = trophyRotate.value)
                ) {
                    val w = size.width
                    val h = size.height
                    val path = Path()
                    
                    // Draw gold cup shape
                    path.moveTo(w * 0.3f, h * 0.2f)
                    path.lineTo(w * 0.7f, h * 0.2f)
                    path.lineTo(w * 0.65f, h * 0.55f)
                    path.quadraticTo(w * 0.5f, h * 0.7f, w * 0.35f, h * 0.55f)
                    path.close()
                    drawPath(path, Color(0xFFD4AF37))
                    
                    // Stem and Base
                    drawRect(Color(0xFFC5A028), Offset(w * 0.45f, h * 0.65f), Size(w * 0.1f, h * 0.15f))
                    drawRoundRect(Color(0xFFA58018), Offset(w * 0.3f, h * 0.8f), Size(w * 0.4f, h * 0.1f), CornerRadius(8f, 8f))
                    
                    // Handles
                    drawArc(
                        color = Color(0xFFD4AF37),
                        startAngle = 120f,
                        sweepAngle = 120f,
                        useCenter = false,
                        size = Size(w * 0.25f, h * 0.3f),
                        topLeft = Offset(w * 0.15f, h * 0.25f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawArc(
                        color = Color(0xFFD4AF37),
                        startAngle = 300f,
                        sweepAngle = 120f,
                        useCenter = false,
                        size = Size(w * 0.25f, h * 0.3f),
                        topLeft = Offset(w * 0.6f, h * 0.25f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                }

                // Leaderboard statistics winner
                val sortedPlayers = playersList.map { name ->
                    val avg = playerScores[name]?.average()?.toInt() ?: 60
                    name to avg
                }.sortedByDescending { it.second }

                if (sortedPlayers.isNotEmpty()) {
                    Text(
                        text = "🏆 ${sortedPlayers.first().first.uppercase()} is the TRUTH GOD! (${sortedPlayers.first().second}% Truth)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Green
                    )
                    if (sortedPlayers.size > 1) {
                        Text(
                            text = "👿 ${sortedPlayers.last().first.uppercase()} is the BIGGEST LIAR! (${sortedPlayers.last().second}% Truth)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showTrophyAnim = false; isPartyStarted = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("START NEW SESSION", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "PARTY PASS & PLAY",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Simultaneous Polygraph Lobby for up to 6 players",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        if (!isPartyStarted) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Add Player Names (Max 6)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = currentNameInput,
                                onValueChange = { currentNameInput = it },
                                placeholder = { Text("Enter name") },
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (currentNameInput.trim().isNotEmpty() && playersList.size < 6) {
                                        playersList.add(currentNameInput.trim())
                                        currentNameInput = ""
                                    }
                                }
                            ) {
                                Text("ADD")
                            }
                        }

                        // Display active player chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            playersList.forEach { player ->
                                InputChip(
                                    selected = true,
                                    onClick = { if (playersList.size > 2) playersList.remove(player) },
                                    label = { Text(player) },
                                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp)) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                playerScores.clear()
                                currentPlayerIndex = 0
                                isPartyStarted = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("LAUNCH PARTY CHANNEL", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Active game board
            val activeName = playersList[currentPlayerIndex]
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "SARCASTIC TRUTH CHALLENGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            val activeQuestion = partyQuestions[currentPlayerIndex % partyQuestions.size]
                            Text(
                                text = activeQuestion,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = "$activeName's TURN",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        if (isRunningScan) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("PASS SCAN DIAL GAUGES...", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                CircularProgressIndicator(progress = scanProgress, color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Button(
                                onClick = { isRunningScan = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("SCAN PLAYER", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Real-time Leaderboard list
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "PARTY LEADERBOARD",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val leaders = playersList.map { name ->
                            val avg = playerScores[name]?.average()?.toInt() ?: 60
                            name to avg
                        }.sortedByDescending { it.second }

                        leaders.forEachIndexed { idx, item ->
                            val name = item.first
                            val avg = item.second
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "${idx + 1}. ", fontWeight = FontWeight.Bold)
                                    Text(text = name, fontWeight = FontWeight.SemiBold)
                                    if (idx == 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.Star, contentDescription = "Truth God", tint = Color.Yellow, modifier = Modifier.size(14.dp))
                                    } else if (idx == leaders.size - 1) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Biggest Liar", tint = Color.Red, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(text = "$avg% Truth", fontWeight = FontWeight.Bold, color = if (avg >= 70) Color.Green else if (avg >= 40) Color.Yellow else Color.Red)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showTrophyAnim = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                        ) {
                            Text("CONCLUDE SESSION & REVEAL TROPHY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyTabContent(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    
    var isCheckingIn by remember { mutableStateOf(false) }
    var checkInProgress by remember { mutableStateOf(0f) }
    var rewardCoinsWon by remember { mutableStateOf(0) }
    var showClaimAnim by remember { mutableStateOf(false) }

    LaunchedEffect(isCheckingIn) {
        if (isCheckingIn) {
            checkInProgress = 0f
            while (checkInProgress < 1.0f) {
                delay(30)
                checkInProgress += 0.03f
            }
            isCheckingIn = false
            val winAmount = repository.completeChallenge(today)
            rewardCoinsWon = winAmount
        }
    }

    if (showClaimAnim) {
        // Exploding gold chest animation mock
        val scale = remember { Animatable(0.3f) }
        LaunchedEffect(Unit) {
            scale.animateTo(1.2f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            scale.animateTo(1.0f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f)),
                modifier = Modifier.graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🎁 7-DAY STREAK CRATE!", style = MaterialTheme.typography.titleLarge, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                    
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val w = size.width
                        val h = size.height
                        drawRoundRect(Color(0xFF8B4513), Offset(w * 0.15f, h * 0.4f), Size(w * 0.7f, h * 0.5f), CornerRadius(12f, 12f))
                        drawRoundRect(Color(0xFFD4AF37), Offset(w * 0.15f, h * 0.15f), Size(w * 0.7f, h * 0.25f), CornerRadius(12f, 12f))
                        drawCircle(Color.Yellow, 10f, Offset(w * 0.5f, h * 0.45f))
                    }

                    Text("CONGRATULATIONS!", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text("You unlocked the Weekly Deception Master crate and claimed 100 extra coins!", fontSize = 12.sp, textAlign = TextAlign.Center)
                    
                    Button(
                        onClick = {
                            showClaimAnim = false
                            coroutineScope.launch {
                                repository.addCoins(100)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                    ) {
                        Text("CLAIM 100 COINS", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "DAILY CHALLENGE",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Keep up your polygraph streak and earn premium gold coin rewards!",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }

        // Streak calendar tracker row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "🔥 ${profile.streakDays}-DAY ACTIVE STREAK", style = MaterialTheme.typography.titleMedium, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEachIndexed { index, day ->
                            val done = index < profile.streakDays % 7 || profile.streakDays >= 7
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = day, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (done) Color(0xFFD4AF37)
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (done) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Checked", tint = Color.Black, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(text = (index + 1).toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (profile.streakDays >= 7) {
                        Button(
                            onClick = { showClaimAnim = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🎁 UNLOCK WEEKLY MASTER CRATE", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active prompt of the day
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "QUESTION OF THE DAY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Did you make up a lie to avoid a social gathering or hanging out with someone this week?",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (profile.lastChallengeDate == today) {
                        Text(
                            text = "✓ CHALLENGE COMPLETED FOR TODAY! (+15 Coins saved)",
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else if (isCheckingIn) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("RUNNING MICRO-BLINK DETECTION HUD...", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            LinearProgressIndicator(progress = checkInProgress, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        Button(
                            onClick = { isCheckingIn = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("TAKE 10s BIOMETRIC CHALLENGE", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Push alert trigger simulation button
        item {
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Push Alert Scheduled! \"🕵️‍♂️ TruthScan AI: Your Daily Challenge is Ready! Keep your 7-day streak alive for 100 free coins!\"", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = "Bell", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("TEST PUSH ALERTS SYSTEM", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun StoreTabContent(
    profile: UserProfile,
    repository: LieDetectorRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Play Billing Simulated State
    var activeBillingSku by remember { mutableStateOf("") }
    var activeBillingPrice by remember { mutableStateOf("") }
    var activeBillingTitle by remember { mutableStateOf("") }
    var showGPayDialog by remember { mutableStateOf(false) }
    var isPayingGPay by remember { mutableStateOf(false) }

    fun launchBilling(sku: String, price: String, title: String) {
        activeBillingSku = sku
        activeBillingPrice = price
        activeBillingTitle = title
        showGPayDialog = true
    }

    if (showGPayDialog) {
        // Native-Looking Google Play Billing Bottom Sheet Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Google Play", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(activeBillingPrice, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                    
                    Divider(color = Color.White.copy(alpha = 0.15f))
                    
                    Text(
                        text = "TruthScan AI Deception Polygraph Premium Upgrade: $activeBillingTitle",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = "Safe", tint = Color.Green, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Instant real-time entitlement validation via Room sqlite.", color = Color.LightGray, fontSize = 11.sp)
                    }

                    if (isPayingGPay) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).size(30.dp), color = Color.Green)
                    } else {
                        Button(
                            onClick = {
                                isPayingGPay = true
                                coroutineScope.launch {
                                    delay(1200)
                                    repository.simulatePurchase(activeBillingSku)
                                    isPayingGPay = false
                                    showGPayDialog = false
                                    Toast.makeText(context, "Billing Purchase entitlement saved successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CONFIRM GPAY CHECKOUT", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                    
                    TextButton(
                        onClick = { showGPayDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CANCEL", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "DECEPTION PRO STORE",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Manage in-app billing passes & unlockable question packages",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                
                // Coin indicator
                Box(
                    modifier = Modifier
                        .background(Color(0xFFD4AF37).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Coins", tint = Color(0xFFD4AF37), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${profile.coins} Coins", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Play Store Subscriptions Section
        item {
            Text(text = "GOOGLE PLAY BILLING PASSES", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Item 1: Remove Ads
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Remove Ads forever", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Completely bypass AdMob interstitials & banners", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Button(
                            onClick = { if (!profile.isAdsRemoved) launchBilling("remove_ads", "$2.99", "Remove Ads") },
                            enabled = !profile.isAdsRemoved,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (profile.isAdsRemoved) "UNLOCKED" else "$2.99", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Item 2: Pro Pack
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pro Pack Bundle", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFD4AF37))
                            Text("Unlock all 4 question packs + remove ads", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Button(
                            onClick = { if (!profile.isProPackUnlocked) launchBilling("pro_pack", "$4.99", "Pro Pack Bundle") },
                            enabled = !profile.isProPackUnlocked,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                        ) {
                            Text(if (profile.isProPackUnlocked) "UNLOCKED" else "$4.99", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Item 3: Pro Subscription
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pro Scanner Membership (Monthly)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Unlimited scans + real BLE smartwatch sync", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Button(
                            onClick = { if (!profile.isSubscribed) launchBilling("pro_subscription", "$4.99/mo", "Pro Scanner Membership") },
                            enabled = !profile.isSubscribed,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (profile.isSubscribed) "ACTIVATED" else "SUBSCRIBE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Question packs section
        item {
            Text(text = "UNLOCKABLE QUESTION PACKS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        val packIds = listOf("dating_pack", "interview_pack", "family_pack", "dare_pack")
        val packTitles = listOf("Dating Pack", "Job Interview Pack", "Family Pack", "Truth or Dare Pack")
        val packDescs = listOf("20+ spicy relationship questions", "30+ professional job screenings", "25+ secret revealing family prompts", "50+ extreme truth or dare prompts")

        packIds.forEachIndexed { idx, id ->
            item {
                val hasPack = profile.unlockedPacksJson.contains(id) || profile.isProPackUnlocked
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(packTitles[idx], fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (hasPack) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Owned", tint = Color.Green, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(packDescs[idx], fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        
                        if (hasPack) {
                            Text("OWNED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        if (profile.coins >= 150) {
                                            coroutineScope.launch {
                                                repository.addCoins(-150)
                                                repository.unlockPack(id)
                                                Toast.makeText(context, "${packTitles[idx]} unlocked with 150 coins!", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Insufficient Coins! Need 150 coins.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("150 COINS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Button(
                                    onClick = { launchBilling(id, "$0.99", packTitles[idx]) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("$0.99", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTabContent(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onNavigate: (Screen) -> Unit,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Dialog states for settings legal policies
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showDisclaimer by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    }
                    
                    Column {
                        Text("AGENT PROFILER", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Level 1 Deception Investigator", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Coins", tint = Color(0xFFD4AF37), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${profile.coins} Coins", fontSize = 12.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = "Scans", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Active Biometrics", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Smartwatch Bluetooth Sync Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smartwatch Pulse Sync (BLE)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Pull real cardiogram pulse BPM details from paired Wear OS / watch sensors.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = profile.smartwatchSynced,
                        onCheckedChange = { checked ->
                            if (checked && !profile.isSubscribed) {
                                onShowPaywall()
                            } else {
                                coroutineScope.launch {
                                    repository.toggleSmartwatch(checked)
                                    if (checked) {
                                        Toast.makeText(context, "Bluetooth Smartwatch paired successfully via WearBLE!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // Achievement Badges Section
        item {
            Text(
                text = "DYNAMIC CRITICAL ACHIEVEMENTS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isLiarUnlocked = true
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isLiarUnlocked) Color.Red else Color.Gray.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Certified Liar",
                            tint = if (isLiarUnlocked) Color.Red else Color.Gray,
                            modifier = Modifier.size(32.dp).shadow(if (isLiarUnlocked) 8.dp else 0.dp, CircleShape, spotColor = Color.Red)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Certified Liar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isLiarUnlocked) Color.Red else Color.Gray)
                        Text("Score <35% lie", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), textAlign = TextAlign.Center)
                    }
                }

                val isTruthGodUnlocked = true
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isTruthGodUnlocked) Color.Green else Color.Gray.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Truth God",
                            tint = if (isTruthGodUnlocked) Color.Green else Color.Gray,
                            modifier = Modifier.size(32.dp).shadow(if (isTruthGodUnlocked) 8.dp else 0.dp, CircleShape, spotColor = Color.Green)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Truth God", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isTruthGodUnlocked) Color.Green else Color.Gray)
                        Text("Score >90% truth", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), textAlign = TextAlign.Center)
                    }
                }

                val isStreakUnlocked = profile.streakDays >= 7
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isStreakUnlocked) Color(0xFFD4AF37) else Color.Gray.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "7 Day Streak",
                            tint = if (isStreakUnlocked) Color(0xFFD4AF37) else Color.Gray,
                            modifier = Modifier.size(32.dp).shadow(if (isStreakUnlocked) 8.dp else 0.dp, CircleShape, spotColor = Color(0xFFD4AF37))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Streak Master", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isStreakUnlocked) Color(0xFFD4AF37) else Color.Gray)
                        Text("Reach 7 days streak", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), textAlign = TextAlign.Center)
                    }
                }
            }
        }

        item {
            Text(
                text = "LEGAL POLICIES & CREDENTIALS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    TextButton(onClick = { showDisclaimer = true }, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("App Polygraph Disclaimer", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    TextButton(onClick = { showTerms = true }, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Terms of Service", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    TextButton(onClick = { showPrivacy = true }, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Privacy Policy", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Restore purchases button
        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        repository.restorePurchases()
                        Toast.makeText(context, "Entitlements Restored! Ads disabled & premium packs unlocked.", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("RESTORE PLAY STORE PURCHASES", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal Overlays for Legal Settings
    if (showDisclaimer) {
        AlertDialog(
            onDismissRequest = { showDisclaimer = false },
            title = { Text("App Polygraph Disclaimer", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = { Text("TruthScan AI is created exclusively for ENTERTAINMENT PURPOSES ONLY. This application is not a scientifically proven lie detector and does not possess valid biometric forensic accuracy or legal admissibility in court. The heart rates, acoustic stress tremors, and micro-expression detections are simulations designed for amusement, jokes, and passing time with friends and couples. Please enjoy responsibly.", fontSize = 13.sp) },
            confirmButton = { Button(onClick = { showDisclaimer = false }) { Text("I UNDERSTAND") } }
        )
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            title = { Text("Terms of Service", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = { Text("By accessing TruthScan AI, you agree that this application is strictly for recreational use. You are prohibited from using this application for official profiling, employer integrity testing, security clearances, or domestic abuse profiling. We are not liable for any misunderstandings, breakups, or relationship arguments caused by the simulated results of the app.", fontSize = 13.sp) },
            confirmButton = { Button(onClick = { showTerms = false }) { Text("ACCEPT") } }
        )
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = { Text("TruthScan AI does not record, transmit, or store private personal face recordings, microphone transcripts, or camera fingerprint feeds to foreign servers. All biometric measurements, voice acoustic scanning, and text forensices are calculated locally on your device with high-security offline SQLite databases. The optional AI features use the secure Gemini REST API with zero permanent storage of input logs.", fontSize = 13.sp) },
            confirmButton = { Button(onClick = { showPrivacy = false }) { Text("CLOSE") } }
        )
    }
}

@Composable
fun DashboardCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// --- VOICE STRESS ANALYZER SCREEN ---
@Composable
fun VoiceStressScreen(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onBack: () -> Unit,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRecording by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentPhase by remember { mutableStateOf("IDLE") } // IDLE, RECORDING, ANALYZING, RESULTS
    var resultScore by remember { mutableStateOf(0) }
    var resultVerdict by remember { mutableStateOf("") }
    var resultSummary by remember { mutableStateOf("") }
    var resultReport by remember { mutableStateOf("") }

    // Waveform animated state
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    // Triggered scan check
    LaunchedEffect(isRecording) {
        if (isRecording) {
            currentPhase = "RECORDING"
            progress = 0f
            while (progress < 1f) {
                delay(50)
                progress += 0.01f
            }
            isRecording = false
            currentPhase = "ANALYZING"
            
            // Check scan limit
            val canScan = repository.canPerformScan()
            if (!canScan) {
                onShowPaywall()
                currentPhase = "IDLE"
            } else {
                // Call Gemini for Voice Stress Analysis
                val demoTranscription = listOf(
                    "No, I absolutely did not eat your last piece of chocolate cake.",
                    "Yes, of course I completed all my assignments before playing video games.",
                    "I was just late because there was a massive traffic jam on the highway.",
                    "I swear, that was the actual price, it was not on sale."
                ).random()

                val analysis = GeminiClient.analyzeVoiceStress(
                    voiceDurationSec = 5,
                    wordCount = demoTranscription.split(" ").size,
                    textTranscription = demoTranscription
                )

                resultScore = analysis.truthScore
                resultVerdict = analysis.status
                resultSummary = analysis.summary
                resultReport = analysis.detailedReport

                // Save to Room DB
                repository.saveScan(
                    type = "Voice Stress",
                    score = resultScore,
                    status = resultVerdict,
                    summary = resultSummary,
                    details = resultReport
                )
                
                currentPhase = "RESULTS"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(title = "Voice Stress", onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        if (currentPhase == "IDLE" || currentPhase == "RECORDING" || currentPhase == "ANALYZING") {
            // Live scanning module
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (currentPhase) {
                            "RECORDING" -> "RECORDING ACOUSTICS... ${(progress * 100).toInt()}%"
                            "ANALYZING" -> "SPECTRUM stress-analysis in progress..."
                            else -> "TAP MIC TO START ACOUSTIC PROFILING"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Voice waveform
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path()
                            val width = size.width
                            val height = size.height
                            val midY = height / 2

                            val amplitude = if (currentPhase == "RECORDING") 40.dp.toPx() else 10.dp.toPx()
                            val frequency = 0.05f

                            path.moveTo(0f, midY)
                            for (x in 0..width.toInt()) {
                                val y = midY + amplitude * sin(x * frequency + waveOffset)
                                path.lineTo(x.toFloat(), y)
                            }

                            drawPath(
                                path = path,
                                color = CyberPrimary,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Pulse mic button
                    IconButton(
                        onClick = {
                            if (currentPhase == "IDLE") {
                                isRecording = true
                            }
                        },
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(24.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
                            .background(
                                if (currentPhase == "RECORDING") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .testTag("record_mic_button")
                    ) {
                        Icon(
                            imageVector = if (currentPhase == "RECORDING") Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Mic",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
            }
        } else if (currentPhase == "RESULTS") {
            // Results Gauge and Export Report View
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "SCAN ANALYSIS COMPLETE",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Rotary Needle Gauge
                item {
                    Box(
                        modifier = Modifier
                            .size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val animateNeedle by animateFloatAsState(
                            targetValue = resultScore * 1.8f - 90f, // Maps 0-100 to -90 to +90 degrees
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "needle"
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val radius = size.width / 2

                            // Draw gauge arc backplate
                            drawArc(
                                color = Color.Gray.copy(alpha = 0.2f),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Color zones: Lie (Red/Magenta) on left, Truth (Cyan/Green) on right
                            drawArc(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Red, Color.Yellow, Color.Green)
                                ),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Physical rotating needle
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(animateNeedle),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(90.dp)
                                    .offset(y = (-45).dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                            )
                        }

                        // Central hub
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.onBackground, CircleShape)
                        )
                    }
                }

                // Truth percentage HUD display
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$resultScore% TRUTH",
                            style = MaterialTheme.typography.displayLarge,
                            color = if (resultScore >= 70) Color.Green else if (resultScore >= 40) Color.Yellow else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (resultScore >= 70) Color.Green.copy(alpha = 0.2f)
                                    else if (resultScore >= 40) Color.Yellow.copy(alpha = 0.2f)
                                    else Color.Red.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERDICT: $resultVerdict",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (resultScore >= 70) Color.Green else if (resultScore >= 40) Color.Yellow else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Written detail breakdown
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ACOUSTIC REPORT SUMMARY",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = resultSummary,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = resultReport,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                item {
                    SavageRoastView(truthScore = resultScore)
                }

                // TikTok share & Export buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Polygraph certificate saved to PDF!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SAVE PDF", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "TruthScan AI Lie Polygraph: Checked my voice transcript and scanned stress levels! Result: $resultScore% Truth. Download TruthScan AI!")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Polygraph Results"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SHARE", fontSize = 12.sp)
                        }
                    }
                }

                item {
                    Button(
                        onClick = { currentPhase = "IDLE" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SCAN AGAIN")
                    }
                }
            }
        }
    }
}

// --- FACIAL SCANNER SCREEN ---
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceScannerScreen(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onBack: () -> Unit,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableFloatStateOf(0f) }
    var currentPhase by remember { mutableStateOf("IDLE") } // IDLE, SCANNING, RESULTS

    var eyeBlinks by remember { mutableIntStateOf(0) }
    var smileRate by remember { mutableFloatStateOf(0f) }
    var browTension by remember { mutableFloatStateOf(0f) }
    var resultScore by remember { mutableStateOf(0) }
    var resultVerdict by remember { mutableStateOf("") }
    var resultReport by remember { mutableStateOf("") }

    // Live telemetry animation loops
    LaunchedEffect(isScanning) {
        if (isScanning) {
            currentPhase = "SCANNING"
            scanProgress = 0f
            while (scanProgress < 1f) {
                delay(40)
                scanProgress += 0.015f
                eyeBlinks = (1..5).random()
                smileRate = (kotlin.random.Random.nextFloat() * 100f)
                browTension = kotlin.random.Random.nextFloat()
            }
            isScanning = false

            // Check scan limit
            val canScan = repository.canPerformScan()
            if (!canScan) {
                onShowPaywall()
                currentPhase = "IDLE"
            } else {
                resultScore = (15..98).random()
                resultVerdict = when {
                    resultScore >= 75 -> "TRUTH"
                    resultScore >= 45 -> "SUSPICIOUS"
                    else -> "LIE"
                }

                resultReport = "Micro-expression scan completed.\n" +
                        "- Blink speed frequency: ${eyeBlinks}hz\n" +
                        "- Dynamic smile tension: ${String.format("%.1f", smileRate)}%\n" +
                        "- Right-side brow contraction: ${String.format("%.2f", browTension)}\n\n" +
                        "Forensic feedback suggests a dynamic truth reading of $resultScore%. Pupillary metrics confirm minor tension levels. High-speed camera scans are complete."

                // Save to Room DB
                repository.saveScan(
                    type = "Facial Scanner",
                    score = resultScore,
                    status = resultVerdict,
                    summary = "Facial micro-expression polygraph completed.",
                    details = resultReport
                )
                currentPhase = "RESULTS"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(title = "Facial Scanner", onBack = onBack)

        Spacer(modifier = Modifier.height(8.dp))

        if (currentPhase == "IDLE" || currentPhase == "SCANNING") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // If permission is granted, we load the native CameraX view preview
                if (cameraPermissionState.status.isGranted) {
                    val cameraController = remember { LifecycleCameraController(context) }
                    val lifecycleOwner = LocalLifecycleOwner.current

                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                controller = cameraController
                                cameraController.bindToLifecycle(lifecycleOwner)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Holographic HUD scan outline
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoPhotography,
                            contentDescription = "No Camera",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "CAMERA PERMISSION REQUIRED",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Please grant camera permissions to access active scanning overlays, or tap scan below to trigger the holographic simulator.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("GRANT PERMISSION", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }

                // Overlay grid and hud line scan animation
                val scanLineInfinite = rememberInfiniteTransition(label = "scanLine")
                val scanLineY by scanLineInfinite.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scany"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw head wireframe boundary
                    drawArc(
                        color = CyberPrimary.copy(alpha = 0.25f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        size = Size(width * 0.7f, height * 0.6f),
                        topLeft = Offset(width * 0.15f, height * 0.15f),
                        style = Stroke(width = 2.dp.toPx(), pathEffect = null)
                    )

                    // Draw tactical face targeting brackets
                    val bracketLen = 20.dp.toPx()
                    val pad = 30.dp.toPx()

                    // Top Left
                    drawLine(CyberPrimary, Offset(pad, pad), Offset(pad + bracketLen, pad), 3.dp.toPx())
                    drawLine(CyberPrimary, Offset(pad, pad), Offset(pad, pad + bracketLen), 3.dp.toPx())
                    // Top Right
                    drawLine(CyberPrimary, Offset(width - pad, pad), Offset(width - pad - bracketLen, pad), 3.dp.toPx())
                    drawLine(CyberPrimary, Offset(width - pad, pad), Offset(width - pad, pad + bracketLen), 3.dp.toPx())
                    // Bottom Left
                    drawLine(CyberPrimary, Offset(pad, height - pad), Offset(pad + bracketLen, height - pad), 3.dp.toPx())
                    drawLine(CyberPrimary, Offset(pad, height - pad), Offset(pad, height - pad - bracketLen), 3.dp.toPx())
                    // Bottom Right
                    drawLine(CyberPrimary, Offset(width - pad, height - pad), Offset(width - pad - bracketLen, height - pad), 3.dp.toPx())
                    drawLine(CyberPrimary, Offset(width - pad, height - pad), Offset(width - pad, height - pad - bracketLen), 3.dp.toPx())

                    // Scan sweeping green laser line
                    if (isScanning) {
                        drawLine(
                            color = Color.Green,
                            start = Offset(0f, height * scanLineY),
                            end = Offset(width, height * scanLineY),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }

                // Live dynamic HUD details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CAM: DETECTING",
                            color = Color.Green,
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "EYE_BLINK_FREQ: ${eyeBlinks}hz",
                            color = Color.Green,
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    if (currentPhase == "SCANNING") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(scanProgress)
                                    .background(Color.Green)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "BROW: ${String.format("%.2f", browTension)}",
                            color = Color.Green,
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "SMILE_TENSION: ${String.format("%.1f", smileRate)}%",
                            color = Color.Green,
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isScanning = true },
                enabled = !isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_face_scan_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isScanning) "SCANNING MICRO-EXPRESSIONS..." else "START FACIAL DECEPTION SCAN")
            }
        } else if (currentPhase == "RESULTS") {
            // Results breakdown
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "FACIAL DIAGNOSTICS COMPLETE",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$resultScore% TRUTH",
                            style = MaterialTheme.typography.displayLarge,
                            color = if (resultScore >= 70) Color.Green else if (resultScore >= 45) Color.Yellow else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (resultScore >= 70) Color.Green.copy(alpha = 0.2f)
                                    else if (resultScore >= 45) Color.Yellow.copy(alpha = 0.2f)
                                    else Color.Red.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERDICT: $resultVerdict",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (resultScore >= 70) Color.Green else if (resultScore >= 45) Color.Yellow else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "MICRO-TELL METRIC SUMMARY",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = resultReport,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                item {
                    SavageRoastView(truthScore = resultScore)
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Analysis saved as a PNG with watermark!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Export")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SAVE PICTURE", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "TruthScan AI Facial Polygraph: Checked brow contraction, blink logs and eye ticks! Status: $resultScore% Truth. Download TruthScan AI!")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Polygraph Results"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SHARE", fontSize = 12.sp)
                        }
                    }
                }

                item {
                    Button(
                        onClick = { currentPhase = "IDLE" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("RESET CAM SCANNER")
                    }
                }
            }
        }
    }
}

// --- FINGERPRINT HEARTBEAT TEST SCREEN ---
@Composable
fun FingerprintScreen(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onBack: () -> Unit,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isHolding by remember { mutableStateOf(false) }
    var holdDuration by remember { mutableIntStateOf(0) }
    var bpmTicker by remember { mutableIntStateOf(72) }
    var currentPhase by remember { mutableStateOf("IDLE") } // IDLE, SCANNING, RESULTS
    var resultScore by remember { mutableStateOf(0) }
    var resultVerdict by remember { mutableStateOf("") }
    var resultReport by remember { mutableStateOf("") }

    // Rolling coordinate values for active EKG lines
    val ekgPoints = remember { mutableStateListOf<Offset>() }
    var timeStep by remember { mutableIntStateOf(0) }

    // Rolling scan progress loop
    LaunchedEffect(isHolding) {
        if (isHolding) {
            currentPhase = "SCANNING"
            holdDuration = 0
            ekgPoints.clear()
            while (holdDuration < 100) {
                delay(40)
                holdDuration += 1
                bpmTicker = (70 + (holdDuration / 4) + (0..6).random())

                // Draw standard EKG cycle waves
                timeStep += 5
                val currentX = timeStep.toFloat() % 600f
                val baseLineY = 100f
                val currentY = if (timeStep % 80 in 40..50) {
                    baseLineY - (50f + kotlin.random.Random.nextFloat() * 30f)
                } else if (timeStep % 80 in 51..58) {
                    baseLineY + (20f + kotlin.random.Random.nextFloat() * 20f)
                } else {
                    baseLineY
                }

                if (ekgPoints.size > 80) {
                    ekgPoints.removeAt(0)
                }
                ekgPoints.add(Offset(currentX, currentY))
            }
            isHolding = false

            // Check limit
            val canScan = repository.canPerformScan()
            if (!canScan) {
                onShowPaywall()
                currentPhase = "IDLE"
            } else {
                resultScore = (10..95).random()
                resultVerdict = when {
                    resultScore >= 70 -> "TRUTH"
                    resultScore >= 40 -> "SUSPICIOUS"
                    else -> "LIE"
                }

                resultReport = "Dermal heartbeat scan completed.\n" +
                        "- Dynamic stress heart rate: ${bpmTicker}bpm\n" +
                        "- Finger dermal friction level: ${resultScore + 10}%\n" +
                        "- Capillary oxygen response: EXCELLENT\n\n" +
                        "Acoustic polygraph indicates high cardiac acceleration matching standard deception loops. Verdict is logged."

                // Save to Room DB
                repository.saveScan(
                    type = "Fingerprint Heartbeat",
                    score = resultScore,
                    status = resultVerdict,
                    summary = "Dermal fingertip EKG scan completed.",
                    details = resultReport
                )
                currentPhase = "RESULTS"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(title = "Fingerprint HUD", onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        if (currentPhase == "IDLE" || currentPhase == "SCANNING") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (currentPhase == "SCANNING") "KEEP FINGER PLACED ON SCANNER" else "HOLD TO COMMENCE HEARTBEAT SCANS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "HEARTBEAT PULSE: $bpmTicker BPM",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (bpmTicker > 95) Color.Red else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Draw Live EKG crawling lines on Compose Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(horizontal = 24.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path()
                            if (ekgPoints.isNotEmpty()) {
                                path.moveTo(ekgPoints.first().x, ekgPoints.first().y)
                                ekgPoints.forEach { point ->
                                    path.lineTo(point.x, point.y)
                                }
                                drawPath(
                                    path = path,
                                    color = if (bpmTicker > 95) Color.Red else CyberPrimary,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Pulsing holding fingerprint sensor
                    val infiniteScale = rememberInfiniteTransition(label = "pulse_sensor")
                    val sensorScale by infiniteScale.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "sensor"
                    )

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        try {
                                            isHolding = true
                                            awaitRelease()
                                        } finally {
                                            isHolding = false
                                        }
                                    }
                                )
                            }
                            .shadow(24.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
                            .background(
                                if (isHolding) Color.Red.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.2f
                                ),
                                CircleShape
                            )
                            .border(
                                3.dp,
                                if (isHolding) Color.Red else MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Scan",
                            tint = if (isHolding) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(72.dp)
                                .rotate(if (isHolding) sensorScale * 5f else 0f)
                        )
                    }

                    if (isHolding) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "SCAN PROGRESS: $holdDuration%",
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else if (currentPhase == "RESULTS") {
            // EKG report summary
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "CARDIAC POLYGRAPH RESULTS",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$resultScore% TRUTH",
                            style = MaterialTheme.typography.displayLarge,
                            color = if (resultScore >= 70) Color.Green else if (resultScore >= 40) Color.Yellow else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (resultScore >= 70) Color.Green.copy(alpha = 0.2f)
                                    else if (resultScore >= 40) Color.Yellow.copy(alpha = 0.2f)
                                    else Color.Red.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERDICT: $resultVerdict",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (resultScore >= 70) Color.Green else if (resultScore >= 40) Color.Yellow else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "BIOMETRIC CARDIO DIAGNOSTICS",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                              )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = resultReport,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                item {
                    SavageRoastView(truthScore = resultScore)
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Dermal certificate saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Export")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SAVE HUD", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "TruthScan AI Card Polygraph: Heartbeat scans finished with average BPM $bpmTicker! Rating: $resultScore% Truth. Download TruthScan AI!")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Polygraph Results"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SHARE", fontSize = 12.sp)
                        }
                    }
                }

                item {
                    Button(
                        onClick = { currentPhase = "IDLE" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("TRY AGAIN")
                    }
                }
            }
        }
    }
}

// --- TEXT LIE DETECTOR SCREEN ---
@Composable
fun TextDetectorScreen(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onBack: () -> Unit,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pasteContent by remember { mutableStateOf("") }
    var currentPhase by remember { mutableStateOf("INPUT") } // INPUT, SCANNING, RESULTS

    var resultScore by remember { mutableStateOf(0) }
    var resultVerdict by remember { mutableStateOf("") }
    var resultSummary by remember { mutableStateOf("") }
    var resultReport by remember { mutableStateOf("") }

    val handleTextAnalyze: () -> Unit = {
        if (pasteContent.trim().isEmpty()) {
            Toast.makeText(context, "Please enter some text or paste chat content!", Toast.LENGTH_SHORT).show()
        } else {
            coroutineScope.launch {
                currentPhase = "SCANNING"
                
                // Limit check
                val canScan = repository.canPerformScan()
                if (!canScan) {
                    onShowPaywall()
                    currentPhase = "INPUT"
                } else {
                    // Real text analyze via Gemini API
                    val result = GeminiClient.analyzeTextChat(pasteContent)

                    resultScore = result.truthScore
                    resultVerdict = result.status
                    resultSummary = result.summary
                    resultReport = result.detailedReport

                    // Save history
                    repository.saveScan(
                        type = "Text Analysis",
                        score = resultScore,
                        status = resultVerdict,
                        summary = resultSummary,
                        details = resultReport
                    )
                    currentPhase = "RESULTS"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(title = "Text Polygraph", onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        if (currentPhase == "INPUT") {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "PASTE WHATSAPP OR MESSAGE SNIPPET",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OutlinedTextField(
                        value = pasteContent,
                        onValueChange = { pasteContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .testTag("text_detector_input"),
                        placeholder = {
                            Text("e.g., 'Sorry I didn't reply earlier, my phone died.' or paste conversation strings here...")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Button(
                        onClick = handleTextAnalyze,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("submit_text_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("LAUNCH LINGUISTIC ANALYZER", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (currentPhase == "SCANNING") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DECRYPTING SEMANTIC STRUCTURES...",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Gemini AI is scanning vocabulary shift models...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else if (currentPhase == "RESULTS") {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "SEMANTIC POLYGRAPH RESULTS",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$resultScore% TRUTH",
                            style = MaterialTheme.typography.displayLarge,
                            color = if (resultScore >= 70) Color.Green else if (resultScore >= 40) Color.Yellow else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (resultScore >= 70) Color.Green.copy(alpha = 0.2f)
                                    else if (resultScore >= 40) Color.Yellow.copy(alpha = 0.2f)
                                    else Color.Red.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERDICT: $resultVerdict",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (resultScore >= 70) Color.Green else if (resultScore >= 40) Color.Yellow else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "FORENSIC WORD DECRYPTION",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = resultSummary,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = resultReport,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                item {
                    SavageRoastView(truthScore = resultScore)
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Text report saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SAVE PDF", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "TruthScan AI Text Polygraph: Verified messages for lies! Status: $resultScore% Truth score. Download TruthScan AI!")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Polygraph Results"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SHARE", fontSize = 12.sp)
                        }
                    }
                }

                item {
                    Button(
                        onClick = { currentPhase = "INPUT" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ANALYZE ANOTHER TEXT")
                    }
                }
            }
        }
    }
}

// --- TRUTH OR DARE MULTIPLAYER SCREEN ---
@Composable
fun GameModeScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    var activeCategory by remember { mutableStateOf("CLASSIC") } // CLASSIC, EXTREME, MULTIPLAYER
    var showQRDialog by remember { mutableStateOf(false) }
    var currentCardIndex by remember { mutableIntStateOf(0) }

    val classicQuestions = listOf(
        "Truth: What is the biggest secret you have ever kept from your best friend?",
        "Truth: Have you ever lied on this app to make yourself look better?",
        "Truth: What is the most embarrassing thing you have done recently?",
        "Dare: Let the other player inspect your recent photo library for 10 seconds.",
        "Dare: Reveal the text content of the last WhatsApp message you sent.",
        "Truth: Have you ever cheated on a test or played a prank on a teacher?"
    )

    val extremeQuestions = listOf(
        "Truth: What is the one thing you would never want your family to discover about you?",
        "Truth: If you could replace one person in this room with a famous celebrity, who would it be?",
        "Dare: Hand your unlocked phone to the player on your right for 3 minutes.",
        "Dare: Log into your bank account or shopping cart and show your latest orders.",
        "Truth: Have you ever eavesdropped on a private conversation?"
    )

    val activeQuestions = if (activeCategory == "EXTREME") extremeQuestions else classicQuestions

    if (showQRDialog) {
        AlertDialog(
            onDismissRequest = { showQRDialog = false },
            title = { Text("MULTIPLAYER GAME ROOM", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Let your friends scan this QR Code to automatically link biometrics and enter your game lobby room!",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic QR vector illustration drawn on canvas
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pixelSize = size.width / 8f
                            // Draw realistic QR locator cubes
                            drawRect(Color.Black, Offset(0f, 0f), Size(pixelSize * 3, pixelSize * 3))
                            drawRect(Color.White, Offset(pixelSize, pixelSize), Size(pixelSize, pixelSize))

                            drawRect(Color.Black, Offset(pixelSize * 5, 0f), Size(pixelSize * 3, pixelSize * 3))
                            drawRect(Color.White, Offset(pixelSize * 6, pixelSize), Size(pixelSize, pixelSize))

                            drawRect(Color.Black, Offset(0f, pixelSize * 5), Size(pixelSize * 3, pixelSize * 3))
                            drawRect(Color.White, Offset(pixelSize, pixelSize * 6), Size(pixelSize, pixelSize))

                            // Scatter some random QR matrix blocks
                            drawRect(Color.Black, Offset(pixelSize * 4, pixelSize * 4), Size(pixelSize, pixelSize))
                            drawRect(Color.Black, Offset(pixelSize * 5, pixelSize * 4), Size(pixelSize, pixelSize))
                            drawRect(Color.Black, Offset(pixelSize * 4, pixelSize * 6), Size(pixelSize, pixelSize))
                            drawRect(Color.Black, Offset(pixelSize * 6, pixelSize * 5), Size(pixelSize, pixelSize))
                            drawRect(Color.Black, Offset(pixelSize * 5, pixelSize * 7), Size(pixelSize, pixelSize))
                            drawRect(Color.Black, Offset(pixelSize * 3, pixelSize * 3), Size(pixelSize, pixelSize))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ROOM_ID: TS_9921_X",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showQRDialog = false }) {
                    Text("CLOSE LOBBY")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(title = "Truth or Dare", onBack = onBack, trailingIcon = {
            IconButton(
                onClick = { showQRDialog = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.QrCode, contentDescription = "Lobby", tint = MaterialTheme.colorScheme.onPrimary)
            }
        })

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented selector tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        ) {
            listOf("CLASSIC", "EXTREME", "MULTIPLAYER").forEach { cat ->
                val active = activeCategory == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (cat == "EXTREME" && !profile.isProPackUnlocked && !profile.isSubscribed) {
                                onShowPaywall()
                            } else if (cat == "MULTIPLAYER") {
                                showQRDialog = true
                            } else {
                                activeCategory = cat
                                currentCardIndex = 0
                            }
                        }
                        .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cat,
                            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (cat == "EXTREME" && !profile.isProPackUnlocked && !profile.isSubscribed) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(10.dp), tint = Color.Yellow)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Question card swiper
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUESTION ${currentCardIndex + 1}/${activeQuestions.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Game",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = activeQuestions[currentCardIndex],
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                if (currentCardIndex > 0) currentCardIndex--
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("BACK")
                        }

                        Button(
                            onClick = {
                                if (currentCardIndex < activeQuestions.size - 1) {
                                    currentCardIndex++
                                } else {
                                    currentCardIndex = 0
                                    Toast.makeText(context, "Completed! Loop reset.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("NEXT")
                        }
                    }
                }
            }
        }
    }
}

// --- AI INTERROGATOR CHAT SCREEN ---
@Composable
fun InterrogatorScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onShowPaywall: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    var chatTurns = remember { mutableStateListOf<Pair<String, String>>() } // Pair(sender, message)
    var agentStressScore by remember { mutableIntStateOf(50) }
    var isThinking by remember { mutableStateOf(false) }

    // On start, add welcome question
    LaunchedEffect(Unit) {
        if (chatTurns.isEmpty()) {
            chatTurns.add(
                "Knox" to "Well, well, well. Have a seat. I've been looking at your logs. Let's make this simple: Tell me what you did with the last slice of pizza last night, or state your alibi immediately."
            )
        }
    }

    val handleSendMessage: () -> Unit = {
        if (messageText.trim().isNotEmpty()) {
            val userMsg = messageText.trim()
            chatTurns.add("Suspect" to userMsg)
            messageText = ""
            isThinking = true

            coroutineScope.launch {
                // Build history payload for Gemini
                val historyArray = JSONArray()
                chatTurns.forEach { turn ->
                    val obj = JSONObject()
                    obj.put("role", if (turn.first == "Knox") "interrogator" else "suspect")
                    obj.put("message", turn.second)
                    historyArray.put(obj)
                }

                val response = GeminiClient.conductInterrogation(
                    historyJsonArray = historyArray.toString(),
                    userResponse = userMsg
                )

                isThinking = false
                agentStressScore = response.currentTruthScore

                if (response.isFinished) {
                    chatTurns.add("Knox" to "VERDICT: " + response.verdict)
                } else {
                    chatTurns.add("Knox" to response.question)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(title = "Agent Knox", onBack = onBack)

        // Knox Status Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "Agent Knox",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AGENT KNOX (FBI AI)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Suspect Deception Level: ${100 - agentStressScore}%",
                        fontSize = 12.sp,
                        color = if (agentStressScore < 50) Color.Red else Color.Green
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(agentStressScore / 100f)
                                .background(if (agentStressScore < 50) Color.Red else Color.Green)
                        )
                    }
                }
            }
        }

        // Chat Bubble Scroll Area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatTurns) { turn ->
                val isKnox = turn.first == "Knox"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isKnox) Arrangement.Start else Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isKnox) 0.dp else 16.dp,
                                    bottomEnd = if (isKnox) 16.dp else 0.dp
                                )
                            )
                            .background(
                                if (isKnox) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.primary
                            )
                            .border(
                                1.dp,
                                if (isKnox) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isKnox) 0.dp else 16.dp,
                                    bottomEnd = if (isKnox) 16.dp else 0.dp
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isKnox) "AGENT KNOX" else "YOU (SUSPECT)",
                                fontSize = 10.sp,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isKnox) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = turn.second,
                                fontSize = 14.sp,
                                color = if (isKnox) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Text(
                        text = "Knox is typing lie-detector response...",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Chat Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                placeholder = { Text("Answer the agent...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { handleSendMessage() })
            )

            IconButton(
                onClick = handleSendMessage,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .size(54.dp)
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// --- STATS & HISTORY SCREEN ---
@Composable
fun StatsHistoryScreen(
    repository: LieDetectorRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val historyList by repository.allHistory.collectAsState(initial = emptyList())

    // Calculated simple aggregates
    val totalScans = historyList.size
    val averageScore = if (historyList.isNotEmpty()) historyList.map { it.truthScore }.average().toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(title = "History & HUD Logs", onBack = onBack, trailingIcon = {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        repository.clearHistory()
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Logs", tint = MaterialTheme.colorScheme.error)
            }
        })

        // HUD Aggregate stats cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "TOTAL SCANS", fontSize = 11.sp, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$totalScans", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "AVERAGE TRUTH", fontSize = 11.sp, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$averageScore%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Badge unlock tracker
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "POLYGRAPH BADGES & RANK",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BadgeIcon("Rookie", unlocked = totalScans >= 1, Icons.Default.Explore)
                    BadgeIcon("Forensic", unlocked = totalScans >= 5, Icons.Default.WorkspacePremium)
                    BadgeIcon("Detective", unlocked = totalScans >= 10, Icons.Default.Security)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CHRONOLOGICAL POLYGRAPH READS",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No polygraph readings logged. Complete scan tests to fill database history logs.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyList) { history ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = when (history.scanType) {
                                            "Voice Stress" -> Icons.Default.Mic
                                            "Facial Scanner" -> Icons.Default.Face
                                            "Fingerprint Heartbeat" -> Icons.Default.Fingerprint
                                            else -> Icons.Default.ContentPaste
                                        },
                                        contentDescription = "Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = history.scanType.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault()).format(Date(history.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = history.summary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (history.status == "TRUTH") Color.Green.copy(alpha = 0.2f)
                                            else if (history.status == "SUSPICIOUS") Color.Yellow.copy(alpha = 0.2f)
                                            else Color.Red.copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${history.truthScore}% ${history.status}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (history.status == "TRUTH") Color.Green else if (history.status == "SUSPICIOUS") Color.Yellow else Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeIcon(name: String, unlocked: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    if (unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else Color.Gray.copy(alpha = 0.1f),
                    CircleShape
                )
                .border(
                    2.dp,
                    if (unlocked) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (unlocked) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name.uppercase(),
            fontSize = 10.sp,
            style = MaterialTheme.typography.labelMedium,
            color = if (unlocked) MaterialTheme.colorScheme.onBackground else Color.Gray
        )
    }
}

// --- SETTINGS & PACK BILLINGS SCREEN ---
@Composable
fun SettingsScreen(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onBack: () -> Unit,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(title = "Polygraph Settings", onBack = onBack)
        }

        // Themes Customization block
        item {
            Text(
                text = "APP THEME OVERLAYS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dynamic Cyber HUD Overlays",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customize scanner dashboard aesthetic vibe overlays.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Cyber", "Neon", "FBI").forEach { theme ->
                            val active = profile.activeTheme == theme
                            Button(
                                onClick = {
                                    if (theme != "Cyber" && !profile.isThemePackUnlocked && !profile.isSubscribed) {
                                        onShowPaywall()
                                    } else {
                                        coroutineScope.launch {
                                            repository.updateTheme(theme)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(theme, fontSize = 11.sp)
                                    if (theme != "Cyber" && !profile.isThemePackUnlocked && !profile.isSubscribed) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bluetooth Smartwatch Sync panel
        item {
            Text(
                text = "BIOMETRIC BLUETOOTH WEARABLES",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smartwatch Heartrate Sync",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pull real biometric pulse BPM details from paired smartwatch sensors.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }

                    Switch(
                        checked = profile.smartwatchSynced,
                        onCheckedChange = { checked ->
                            if (checked && !profile.isSubscribed) {
                                onShowPaywall()
                            } else {
                                coroutineScope.launch {
                                    repository.toggleSmartwatch(checked)
                                    if (checked) {
                                        Toast.makeText(context, "Smartwatch paired via simulated BLE connection!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // Store Checkout items
        item {
            Text(
                text = "BILLING PRODUCTS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            DashboardItem(
                title = "PRO PACK UPSELL",
                description = "Unlocks Extreme Game questions, no ads, high definition reports.",
                price = "$4.99",
                unlocked = profile.isProPackUnlocked,
                onClick = {
                    if (!profile.isProPackUnlocked) {
                        coroutineScope.launch {
                            repository.simulatePurchase("pro_pack")
                            Toast.makeText(context, "Pro Pack checked out!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        item {
            DashboardItem(
                title = "REMOVE ADS FOREVER",
                description = "Permanently silences bottom HUD commercial banners.",
                price = "$2.99",
                unlocked = profile.isAdsRemoved,
                onClick = {
                    if (!profile.isAdsRemoved) {
                        coroutineScope.launch {
                            repository.simulatePurchase("remove_ads")
                            Toast.makeText(context, "Ads removed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        item {
            DashboardItem(
                title = "THEME STYLING PACK",
                description = "Unlock customized Neon and Tactical FBI themes.",
                price = "$0.99",
                unlocked = profile.isThemePackUnlocked,
                onClick = {
                    if (!profile.isThemePackUnlocked) {
                        coroutineScope.launch {
                            repository.simulatePurchase("theme_pack")
                            Toast.makeText(context, "Theme pack unlocked!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        // Restore & legal policy links
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    coroutineScope.launch {
                        repository.restorePurchases()
                        Toast.makeText(context, "Purchases fully restored!", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier.testTag("restore_button")) {
                    Text("RESTORE PURCHASES", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }

                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy-truthscan"))
                    context.startActivity(intent)
                }) {
                    Text("PRIVACY POLICY", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DashboardItem(
    title: String,
    description: String,
    price: String,
    unlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (unlocked) Color.Green.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (unlocked) Color.Green else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (unlocked) Color.Green.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (unlocked) Color.Green else MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (unlocked) "OWNED" else price,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) Color.Green else MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// --- PAYWALL UPSELL FULL SCREEN ---
@Composable
fun PaywallScreen(
    profile: UserProfile,
    repository: LieDetectorRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = "VIP Pro",
                tint = Color(0xFFD4AF37),
                modifier = Modifier
                    .size(80.dp)
                    .shadow(16.dp, CircleShape, spotColor = Color(0xFFD4AF37))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TRUTHSCAN PRO",
                style = MaterialTheme.typography.displayLarge,
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Unlock the ultimate lie detector engine",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // VIP features bullet points
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VipBullet(text = "UNLIMITED SCAN TESTS (Bypass 3 scans/day limits)")
                VipBullet(text = "100% AD-FREE ENGINE FOREVER")
                VipBullet(text = "UNLOCK EXTREME TRUTH OR DARE CHANNELS")
                VipBullet(text = "COSMIC COLOR THEMES (Cyber, Neon, FBI Overlays)")
                VipBullet(text = "BLUETOOTH SMARTWATCH REAL-TIME HEART BPM SYNC")
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subscription cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SubscriptionCard(
                    title = "WEEKLY PASS",
                    price = "$0.99 / week",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        coroutineScope.launch {
                            repository.simulatePurchase("sub_weekly")
                            Toast.makeText(context, "Weekly VIP Pass active!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                )

                SubscriptionCard(
                    title = "BEST VALUE",
                    price = "$4.99 / month",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        coroutineScope.launch {
                            repository.simulatePurchase("sub_monthly")
                            Toast.makeText(context, "Monthly VIP subscription active!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rewarded Ad Option for Free Scan!
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        repository.grantAdScanBonus()
                        Toast.makeText(context, "Watched reward video! +1 Free Scan unlocked.", Toast.LENGTH_LONG).show()
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reward_ad_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Video")
                Spacer(modifier = Modifier.width(8.dp))
                Text("WATCH AD FOR 1 FREE SCANS", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Standard subscription terms apply. Cancel anytime in Google Play Store settings. Safe processing assured.",
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun VipBullet(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Included",
            tint = Color(0xFFD4AF37),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SubscriptionCard(
    title: String,
    price: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, Color(0xFFD4AF37)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFD4AF37), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Text(
                text = price,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "SUBSCRIBE NOW",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD4AF37)
            )
        }
    }
}
