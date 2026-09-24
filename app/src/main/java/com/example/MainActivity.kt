package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.LoginOtpScreen
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.upload.UploadScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.theme.EarnGramTheme
import com.example.util.LocalLanguageState
import com.example.util.rememberLanguageManager

enum class ScreenTab {
    FEED,
    UPLOAD,
    WALLET,
    NOTIFICATIONS,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as EarnGramApp
        val repository = app.repository

        setContent {
            val languageManager = rememberLanguageManager()

            CompositionLocalProvider(LocalLanguageState provides languageManager) {
                EarnGramTheme {
                    val currentUser by repository.currentUser.collectAsStateWithLifecycle(initialValue = null)
                    var currentTab by remember { mutableStateOf(ScreenTab.FEED) }
                    var isAdminRouteOpen by remember { mutableStateOf(false) }
                    val unreadNotifs by repository.getUnreadNotificationsCount().collectAsStateWithLifecycle(initialValue = 0)

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (currentUser == null) {
                            LoginOtpScreen(
                                repository = repository,
                                onLoginSuccess = {
                                    currentTab = ScreenTab.FEED
                                }
                            )
                        } else if (isAdminRouteOpen) {
                            AdminDashboardScreen(
                                repository = repository,
                                onNavigateBack = { isAdminRouteOpen = false }
                            )
                        } else {
                            Scaffold(
                                bottomBar = {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 6.dp
                                    ) {
                                        // 1. Feed
                                        NavigationBarItem(
                                            selected = (currentTab == ScreenTab.FEED),
                                            onClick = { currentTab = ScreenTab.FEED },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == ScreenTab.FEED) Icons.Filled.Home else Icons.Outlined.Home,
                                                    contentDescription = "Feed"
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = languageManager.t("Feed", "फ़ीड"),
                                                    fontWeight = if (currentTab == ScreenTab.FEED) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.testTag("nav_item_feed")
                                        )

                                        // 2. Upload
                                        NavigationBarItem(
                                            selected = (currentTab == ScreenTab.UPLOAD),
                                            onClick = { currentTab = ScreenTab.UPLOAD },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == ScreenTab.UPLOAD) Icons.Filled.AddCircle else Icons.Outlined.AddCircleOutline,
                                                    contentDescription = "Create",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = languageManager.t("Create", "अपलोड"),
                                                    fontWeight = if (currentTab == ScreenTab.UPLOAD) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.testTag("nav_item_upload")
                                        )

                                        // 3. Wallet
                                        NavigationBarItem(
                                            selected = (currentTab == ScreenTab.WALLET),
                                            onClick = { currentTab = ScreenTab.WALLET },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == ScreenTab.WALLET) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                                    contentDescription = "Wallet"
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = languageManager.t("Wallet ₹", "वॉलेट ₹"),
                                                    fontWeight = if (currentTab == ScreenTab.WALLET) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.testTag("nav_item_wallet")
                                        )

                                        // 4. Notifications
                                        NavigationBarItem(
                                            selected = (currentTab == ScreenTab.NOTIFICATIONS),
                                            onClick = { currentTab = ScreenTab.NOTIFICATIONS },
                                            icon = {
                                                BadgedBox(
                                                    badge = {
                                                        if (unreadNotifs > 0) {
                                                            Badge {
                                                                Text("$unreadNotifs")
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (currentTab == ScreenTab.NOTIFICATIONS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                                        contentDescription = "Notifications"
                                                    )
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = languageManager.t("Alerts", "सूचनाएं"),
                                                    fontWeight = if (currentTab == ScreenTab.NOTIFICATIONS) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.testTag("nav_item_notifications")
                                        )

                                        // 5. Profile
                                        NavigationBarItem(
                                            selected = (currentTab == ScreenTab.PROFILE),
                                            onClick = { currentTab = ScreenTab.PROFILE },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == ScreenTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                                    contentDescription = "Profile"
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = languageManager.t("Profile", "प्रोफाइल"),
                                                    fontWeight = if (currentTab == ScreenTab.PROFILE) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.testTag("nav_item_profile")
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    AnimatedContent(
                                        targetState = currentTab,
                                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                                        label = "screen_transition"
                                    ) { targetTab ->
                                        when (targetTab) {
                                            ScreenTab.FEED -> FeedScreen(
                                                repository = repository,
                                                onNavigateToAdmin = { isAdminRouteOpen = true }
                                            )
                                            ScreenTab.UPLOAD -> UploadScreen(
                                                repository = repository,
                                                onUploadSuccess = { currentTab = ScreenTab.FEED }
                                            )
                                            ScreenTab.WALLET -> WalletScreen(
                                                repository = repository
                                            )
                                            ScreenTab.NOTIFICATIONS -> NotificationsScreen(
                                                repository = repository
                                            )
                                            ScreenTab.PROFILE -> ProfileScreen(
                                                repository = repository,
                                                onNavigateToAdmin = { isAdminRouteOpen = true }
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
    }
}
