package com.example.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdSettings
import com.example.data.model.Post
import com.example.data.repository.EarnGramRepository
import com.example.ui.components.CompliantNativeAdCard
import com.example.util.LocalLanguageState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    repository: EarnGramRepository,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val langState = LocalLanguageState.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Latest, 1 = Popular
    val isPopular = selectedTabIndex == 1

    val posts by repository.getApprovedPosts(isPopular).collectAsStateWithLifecycle(initialValue = emptyList())
    val adSettings by repository.getAdSettings().collectAsStateWithLifecycle(initialValue = AdSettings())

    var activeCommentPost by remember { mutableStateOf<Post?>(null) }
    var activeReportPost by remember { mutableStateOf<Post?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Text(
                            text = "EarnGram",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Language Switcher
                    FilledTonalButton(
                        onClick = { langState.toggleLanguage() },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("feed_language_toggle_button")
                    ) {
                        Text(
                            text = if (langState.currentLanguage.name == "ENGLISH") "हिंदी" else "EN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Admin Dashboard Shortcut
                    IconButton(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.testTag("feed_admin_shortcut_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Panel",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row: Latest (ताज़ा) vs Popular (लोकप्रिय)
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = langState.t("Latest", "ताज़ा"),
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_latest")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = langState.t("Popular / Trending", "लोकप्रिय / ट्रेंडिंग"),
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_popular")
                )
            }

            if (posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = langState.t(
                                "No posts in this feed yet.",
                                "इस फ़ीड में अभी कोई पोस्ट नहीं है।"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = langState.t(
                                "Upload a photo or video to be the first creator!",
                                "पहले क्रिएटर बनने के लिए कोई फ़ोटो या वीडियो अपलोड करें!"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(posts, key = { _, post -> post.id }) { index, post ->
                        PostCard(
                            post = post,
                            onLikeClick = {
                                coroutineScope.launch {
                                    val (success, message) = repository.toggleLike(post.id)
                                    snackbarHostState.showSnackbar(message)
                                }
                            },
                            onCommentClick = {
                                activeCommentPost = post
                            },
                            onReportClick = {
                                activeReportPost = post
                            }
                        )

                        // Insert Compliant Native Ad Card based on configured interval
                        if (adSettings.adsEnabled &&
                            adSettings.showOnHomeFeed &&
                            (index + 1) % adSettings.feedAdInterval == 0
                        ) {
                            CompliantNativeAdCard(adSettings = adSettings)
                        }
                    }
                }
            }
        }

        // Active Comments Bottom Sheet
        activeCommentPost?.let { post ->
            CommentsBottomSheet(
                post = post,
                repository = repository,
                onDismiss = { activeCommentPost = null }
            )
        }

        // Active Report Dialog
        activeReportPost?.let { post ->
            ReportPostDialog(
                post = post,
                repository = repository,
                onDismiss = { activeReportPost = null },
                onReportSubmitted = {
                    activeReportPost = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            langState.t("Report received. Thank you for keeping EarnGram safe.", "रिपोर्ट प्राप्त हुई। EarnGram को सुरक्षित रखने के लिए धन्यवाद।")
                        )
                    }
                }
            )
        }
    }
}
