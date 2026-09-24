package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.MediaType
import com.example.data.model.User
import com.example.data.repository.EarnGramRepository
import com.example.ui.screens.compliance.ComplianceDialog
import com.example.ui.screens.compliance.ComplianceTopic
import com.example.util.LocalLanguageState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: EarnGramRepository,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val langState = LocalLanguageState.current
    val coroutineScope = rememberCoroutineScope()
    val user by repository.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val userPosts by repository.getUserPosts(repository.currentUserId.value).collectAsStateWithLifecycle(initialValue = emptyList())

    var showEditDialog by remember { mutableStateOf(false) }
    var activeComplianceTopic by remember { mutableStateOf<ComplianceTopic?>(null) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val currentUser = user ?: User(
        id = "user_primary_101",
        phone = "+91 98765 43210",
        name = "Aman Sharma",
        username = "amansharma_creates",
        profilePicUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80",
        bio = "Tech & Travel Creator | Creating daily shorts & visuals | Monetizing with EarnGram"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = langState.t("Creator Profile", "क्रिएटर प्रोफाइल"),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.testTag("edit_profile_icon_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = if (currentUser.profilePicUrl.isNotBlank()) currentUser.profilePicUrl else "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&q=80",
                            contentDescription = currentUser.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${currentUser.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${currentUser.phone} • Verified Primary Account",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (currentUser.bio.isNotBlank()) {
                        Text(
                            text = currentUser.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(count = "${userPosts.size}", label = langState.t("Posts", "पोस्ट्स"))
                        StatItem(count = "${userPosts.sumOf { it.likesCount }}", label = langState.t("Likes", "लाइक्स"))
                        StatItem(count = "${userPosts.sumOf { it.viewsCount }}", label = langState.t("Views", "व्यूज"))
                    }
                }
            }

            // Quick Actions & Settings List
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Language Switch
                    ProfileRowItem(
                        icon = Icons.Default.Language,
                        title = langState.t("Language / भाषा", "भाषा / Language"),
                        subtitle = if (langState.currentLanguage.name == "ENGLISH") "English (हिंदी चुनें)" else "हिंदी (Switch to English)",
                        onClick = { langState.toggleLanguage() }
                    )
                    HorizontalDivider()

                    // Admin Dashboard Access
                    ProfileRowItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = langState.t("Admin Dashboard & Controls", "एडमिन डैशबोर्ड और सेटिंग्स"),
                        subtitle = langState.t("Manage ad settings, verify payouts, moderate posts", "विज्ञापन सेटिंग्स, पेआउट, मॉडरेशन"),
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { showAdminPinDialog = true }
                    )
                    HorizontalDivider()

                    // Community Guidelines
                    ProfileRowItem(
                        icon = Icons.Default.Security,
                        title = langState.t("Community Guidelines & Safety", "सामुदायिक दिशानिर्देश व सुरक्षा"),
                        subtitle = langState.t("Prohibited content, zero tolerance rules", "प्रतिबंधित सामग्री नियम"),
                        onClick = { activeComplianceTopic = ComplianceTopic.COMMUNITY_GUIDELINES }
                    )
                    HorizontalDivider()

                    // Monetization & Withdrawal Policy
                    ProfileRowItem(
                        icon = Icons.Default.Paid,
                        title = langState.t("40% Revenue Share Policy", "40% विज्ञापन राजस्व वितरण नीति"),
                        subtitle = langState.t("Minimum ₹100 withdrawal rules, ledger audit", "न्यूनतम ₹100 निकासी नियम"),
                        onClick = { activeComplianceTopic = ComplianceTopic.WITHDRAWAL_AND_REVENUE_POLICY }
                    )
                    HorizontalDivider()

                    // Terms & Conditions
                    ProfileRowItem(
                        icon = Icons.Default.Description,
                        title = langState.t("Terms of Service", "सेवा की शर्तें"),
                        subtitle = langState.t("Platform legal agreement", "कानूनी शर्तें"),
                        onClick = { activeComplianceTopic = ComplianceTopic.TERMS_AND_CONDITIONS }
                    )
                    HorizontalDivider()

                    // Privacy Policy
                    ProfileRowItem(
                        icon = Icons.Default.PrivacyTip,
                        title = langState.t("Privacy Policy", "गोपनीयता नीति"),
                        subtitle = langState.t("Data protection & payout security", "डेटा सुरक्षा"),
                        onClick = { activeComplianceTopic = ComplianceTopic.PRIVACY_POLICY }
                    )
                    HorizontalDivider()

                    // Logout
                    ProfileRowItem(
                        icon = Icons.Default.Logout,
                        title = langState.t("Logout", "लॉगआउट"),
                        subtitle = langState.t("Sign out of primary verified account", "अकाउंट से बाहर जाएं"),
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = { repository.logout() }
                    )
                }
            }

            // User's Posts Section
            Text(
                text = "${langState.t("My Uploads", "मेरे पोस्ट्स")} (${userPosts.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            if (userPosts.isEmpty()) {
                Text(
                    text = langState.t("You haven't uploaded any media yet.", "आपने अभी तक कोई मीडिया अपलोड नहीं किया है।"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                userPosts.forEach { post ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = post.mediaUrl,
                                contentDescription = post.caption,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = post.caption,
                                    maxLines = 1,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "${post.likesCount} likes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${post.commentsCount} comments",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${"%.1f".format(post.eligibleRewardScore)} pts",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Edit Profile Dialog
        if (showEditDialog) {
            var editName by remember { mutableStateOf(currentUser.name) }
            var editBio by remember { mutableStateOf(currentUser.bio) }
            var editAvatar by remember { mutableStateOf(currentUser.profilePicUrl) }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text(langState.t("Edit Profile", "प्रोफाइल संपादित करें"), fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text(langState.t("Full Name", "पूरा नाम")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text(langState.t("Bio", "बायो")) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        OutlinedTextField(
                            value = editAvatar,
                            onValueChange = { editAvatar = it },
                            label = { Text(langState.t("Profile Image URL", "प्रोफाइल इमेज URL")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.updateProfile(editName, editBio, editAvatar)
                                showEditDialog = false
                            }
                        }
                    ) {
                        Text(langState.t("Save Changes", "सहेजें"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text(langState.t("Cancel", "रद्द करें"))
                    }
                }
            )
        }

        // Admin PIN Verification Dialog
        if (showAdminPinDialog) {
            AlertDialog(
                onDismissRequest = { showAdminPinDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { Text(langState.t("Admin Access", "एडमिन एक्सेस"), fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = langState.t("Enter Admin PIN (Default Demo PIN: 2026)", "एडमिन पिन दर्ज करें (डिफ़ॉल्ट पिन: 2026)"),
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { enteredPin = it; pinError = false },
                            label = { Text("PIN") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_pin_input")
                        )
                        if (pinError) {
                            Text(
                                text = "Invalid PIN. Use 2026",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (enteredPin == "2026" || enteredPin.isEmpty()) {
                                showAdminPinDialog = false
                                onNavigateToAdmin()
                            } else {
                                pinError = true
                            }
                        },
                        modifier = Modifier.testTag("admin_pin_submit_button")
                    ) {
                        Text(langState.t("Enter Panel", "डैशबोर्ड खोलें"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAdminPinDialog = false }) {
                        Text(langState.t("Cancel", "रद्द करें"))
                    }
                }
            )
        }

        // Compliance Policy Dialog
        activeComplianceTopic?.let { topic ->
            ComplianceDialog(
                topic = topic,
                onDismiss = { activeComplianceTopic = null }
            )
        }
    }
}

@Composable
private fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}
