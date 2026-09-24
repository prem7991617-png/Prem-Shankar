package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.EarnGramRepository
import com.example.util.LocalLanguageState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: EarnGramRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val langState = LocalLanguageState.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val adminStats by repository.getAdminStats().collectAsStateWithLifecycle(initialValue = AdminOverviewStats())
    val pendingPosts by repository.getAllPendingPosts().collectAsStateWithLifecycle(initialValue = emptyList())
    val pendingWithdrawals by repository.getAllPendingWithdrawals().collectAsStateWithLifecycle(initialValue = emptyList())
    val reports by repository.getAllReports().collectAsStateWithLifecycle(initialValue = emptyList())
    val allUsers by repository.getAllUsers().collectAsStateWithLifecycle(initialValue = emptyList())
    val adSettings by repository.getAdSettings().collectAsStateWithLifecycle(initialValue = AdSettings())
    val rewardCycles by repository.getRewardCycles().collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedAdminTab by remember { mutableIntStateOf(0) }
    // 0: Overview & Revenue Cycle, 1: Withdrawals, 2: Post Moderation, 3: Reports, 4: Users, 5: Ad Settings

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = langState.t("Admin Control Center", "एडमिन कंट्रोल सेंटर"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Scrollable Admin Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedAdminTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = { Text(langState.t("Overview & 40/60 Cycle", "ओवरव्यू व 40/60 साइकिल")) },
                    modifier = Modifier.testTag("admin_tab_overview")
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = { Text("${langState.t("Withdrawals", "निकासी")} (${pendingWithdrawals.size})") },
                    modifier = Modifier.testTag("admin_tab_withdrawals")
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = { Text("${langState.t("Moderation", "मॉडरेशन")} (${pendingPosts.size})") },
                    modifier = Modifier.testTag("admin_tab_moderation")
                )
                Tab(
                    selected = selectedAdminTab == 3,
                    onClick = { selectedAdminTab = 3 },
                    text = { Text("${langState.t("Reports", "रिपोर्ट्स")} (${reports.size})") },
                    modifier = Modifier.testTag("admin_tab_reports")
                )
                Tab(
                    selected = selectedAdminTab == 4,
                    onClick = { selectedAdminTab = 4 },
                    text = { Text(langState.t("Users & Anti-Fraud", "यूज़र्स व एंटी-फ्रॉड")) },
                    modifier = Modifier.testTag("admin_tab_users")
                )
                Tab(
                    selected = selectedAdminTab == 5,
                    onClick = { selectedAdminTab = 5 },
                    text = { Text(langState.t("Ad Settings", "विज्ञापन सेटिंग्स")) },
                    modifier = Modifier.testTag("admin_tab_ads")
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedAdminTab) {
                    0 -> {
                        // Overview KPIs & 40/60 Split Distribution Trigger
                        item {
                            RevenueSplitCard(
                                stats = adminStats,
                                onRunDistribution = { revenue, cycleName ->
                                    coroutineScope.launch {
                                        val cycle = repository.executeMonthlyRevenueDistribution(revenue, cycleName)
                                        snackbarHostState.showSnackbar(
                                            "Distributed ₹${"%.2f".format(cycle.userPoolAmount)} (40%) to ${cycle.eligibleCreatorsCount} creators!"
                                        )
                                    }
                                }
                            )
                        }

                        item {
                            Text(
                                text = langState.t("Platform Analytics", "प्लेटफॉर्म एनालिटिक्स"),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        item {
                            AnalyticsGrid(stats = adminStats)
                        }

                        if (rewardCycles.isNotEmpty()) {
                            item {
                                Text(
                                    text = langState.t("Revenue Sharing Audit Log", "राजस्व वितरण ऑडिट रिकॉर्ड"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            items(rewardCycles, key = { it.id }) { cycle ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = cycle.cycleName, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Total Ad Revenue: ₹${"%.2f".format(cycle.totalAdRevenue)} • 40% User Pool: ₹${"%.2f".format(cycle.userPoolAmount)} • 60% Platform: ₹${"%.2f".format(cycle.platformShareAmount)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Creators Credited: ${cycle.eligibleCreatorsCount} • Aggregate Score: ${"%.1f".format(cycle.totalEligibleScore)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Withdrawals Approval & Payout
                        item {
                            Text(
                                text = "${langState.t("Pending Withdrawal Requests", "लंबित निकासी अनुरोध")} (${pendingWithdrawals.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (pendingWithdrawals.isEmpty()) {
                            item {
                                Text(
                                    text = langState.t("No pending requests.", "कोई लंबित अनुरोध नहीं है।"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(pendingWithdrawals, key = { it.id }) { wd ->
                                AdminWithdrawalCard(
                                    wd = wd,
                                    onApprove = {
                                        coroutineScope.launch {
                                            repository.updateWithdrawalStatus(wd.id, WithdrawalStatus.APPROVED)
                                            snackbarHostState.showSnackbar("Withdrawal ${wd.id} APPROVED.")
                                        }
                                    },
                                    onMarkPaid = {
                                        coroutineScope.launch {
                                            repository.updateWithdrawalStatus(wd.id, WithdrawalStatus.PAID, txRef = "UPI-BANK-${System.currentTimeMillis()}")
                                            snackbarHostState.showSnackbar("Withdrawal ${wd.id} marked as PAID. User ledger updated.")
                                        }
                                    },
                                    onReject = { reason ->
                                        coroutineScope.launch {
                                            repository.updateWithdrawalStatus(wd.id, WithdrawalStatus.REJECTED, rejectionReason = reason)
                                            snackbarHostState.showSnackbar("Withdrawal ${wd.id} REJECTED and funds refunded.")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    2 -> {
                        // Content Moderation (Pending Posts)
                        item {
                            Text(
                                text = "${langState.t("Posts Awaiting Moderation", "समीक्षाधीन पोस्ट्स")} (${pendingPosts.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (pendingPosts.isEmpty()) {
                            item {
                                Text(
                                    text = langState.t("Queue is clear! All uploads are moderated.", "कतार खाली है! सभी पोस्ट्स जांची जा चुकी हैं।"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(pendingPosts, key = { it.id }) { post ->
                                AdminPostModerationCard(
                                    post = post,
                                    onApprove = {
                                        coroutineScope.launch {
                                            repository.approvePost(post.id)
                                            snackbarHostState.showSnackbar("Post approved and published to feed.")
                                        }
                                    },
                                    onReject = { reason ->
                                        coroutineScope.launch {
                                            repository.rejectPost(post.id, reason)
                                            snackbarHostState.showSnackbar("Post rejected.")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    3 -> {
                        // Reports Review
                        item {
                            Text(
                                text = "${langState.t("User Content Reports", "यूज़र सामग्री रिपोर्ट्स")} (${reports.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (reports.isEmpty()) {
                            item {
                                Text(
                                    text = langState.t("No reports filed.", "कोई रिपोर्ट दर्ज नहीं है।"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(reports, key = { it.id }) { report ->
                                AdminReportCard(
                                    report = report,
                                    onTakeDown = {
                                        coroutineScope.launch {
                                            repository.resolveReport(report.id, removeContent = true)
                                            snackbarHostState.showSnackbar("Report resolved: Content removed.")
                                        }
                                    },
                                    onDismiss = {
                                        coroutineScope.launch {
                                            repository.resolveReport(report.id, removeContent = false)
                                            snackbarHostState.showSnackbar("Report dismissed.")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    4 -> {
                        // Users & Anti-Fraud Suspensions
                        item {
                            Text(
                                text = "${langState.t("Registered Creators", "पंजीकृत क्रिएटर्स")} (${allUsers.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        items(allUsers, key = { it.id }) { u ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = u.name, fontWeight = FontWeight.Bold)
                                            if (u.isSuspended) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "SUSPENDED",
                                                        color = MaterialTheme.colorScheme.error,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${u.phone} • @${u.username}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.toggleUserSuspension(u.id)
                                                snackbarHostState.showSnackbar(
                                                    if (!u.isSuspended) "User ${u.name} suspended." else "User ${u.name} unsuspended."
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (u.isSuspended) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text(if (u.isSuspended) "Unsuspend" else "Suspend")
                                    }
                                }
                            }
                        }
                    }

                    5 -> {
                        // Ad Settings Configuration (Google AdMob & Adsterra)
                        item {
                            AdminAdSettingsCard(
                                settings = adSettings,
                                onSave = { updated ->
                                    coroutineScope.launch {
                                        repository.updateAdSettings(updated)
                                        snackbarHostState.showSnackbar("Ad settings updated successfully.")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RevenueSplitCard(
    stats: AdminOverviewStats,
    onRunDistribution: (Double, String) -> Unit
) {
    var revenueInput by remember { mutableStateOf("50000") }
    var cycleName by remember { mutableStateOf("September 2026 Distribution") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Revenue Sharing Engine",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "40% / 60% Rule",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            OutlinedTextField(
                value = revenueInput,
                onValueChange = { revenueInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Total Verified Ad Revenue (₹)") },
                prefix = { Text("₹ ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val rev = revenueInput.toDoubleOrNull() ?: 0.0
            val pool40 = rev * 0.40
            val platform60 = rev * 0.60

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "40% Creator Pool:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "₹${"%.2f".format(pool40)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "60% Platform Share:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "₹${"%.2f".format(platform60)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    if (rev > 0) {
                        onRunDistribution(rev, cycleName)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_distribute_revenue_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.MonetizationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Execute Distribution to Creator Wallets")
            }
        }
    }
}

@Composable
private fun AnalyticsGrid(stats: AdminOverviewStats) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(title = "Total Users", value = "${stats.totalUsers}", modifier = Modifier.weight(1f))
            StatCard(title = "Active Users", value = "${stats.activeUsers}", modifier = Modifier.weight(1f))
            StatCard(title = "Suspended", value = "${stats.suspendedUsersCount}", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(title = "Total Posts", value = "${stats.totalPosts}", modifier = Modifier.weight(1f))
            StatCard(title = "Photos", value = "${stats.totalPhotos}", modifier = Modifier.weight(1f))
            StatCard(title = "Videos", value = "${stats.totalVideos}", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(title = "Pending Payouts", value = "₹${"%.0f".format(stats.pendingWithdrawalsAmount)}", modifier = Modifier.weight(1f))
            StatCard(title = "Completed Payouts", value = "${stats.completedWithdrawalsCount}", modifier = Modifier.weight(1f))
            StatCard(title = "Reported Posts", value = "${stats.reportedPostsCount}", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun AdminWithdrawalCard(
    wd: WithdrawalRequest,
    onApprove: () -> Unit,
    onMarkPaid: () -> Unit,
    onReject: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = wd.userName, fontWeight = FontWeight.Bold)
                Text(text = "₹${"%.2f".format(wd.amount)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "${wd.method}: ${if (wd.method == WithdrawalMethod.UPI) wd.upiId else "${wd.accountHolderName} | Acct: ${wd.accountNumber} | IFSC: ${wd.ifscCode}"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(text = "Phone: ${wd.userPhone} • Ref: ${wd.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMarkPaid,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Mark Paid", fontSize = 12.sp)
                }
                Button(
                    onClick = onApprove,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { onReject("Invalid account credentials") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AdminPostModerationCard(
    post: Post,
    onApprove: () -> Unit,
    onReject: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.DarkGray)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "${post.mediaType.name} by ${post.userName}", fontWeight = FontWeight.Bold)
                    Text(text = post.caption, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    Text(text = "Status: PENDING MODERATION", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF59E0B))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve & Publish")
                }
                OutlinedButton(
                    onClick = { onReject("Violates community guidelines") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AdminReportCard(
    report: PostReport,
    onTakeDown: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "Reason: ${report.reason.name}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Text(text = "Reporter: ${report.reporterName} • Post ID: ${report.postId}", style = MaterialTheme.typography.bodySmall)
            if (report.comments.isNotBlank()) {
                Text(text = "Details: ${report.comments}", style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTakeDown,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Remove Post")
                }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@Composable
private fun AdminAdSettingsCard(
    settings: AdSettings,
    onSave: (AdSettings) -> Unit
) {
    var adsEnabled by remember { mutableStateOf(settings.adsEnabled) }
    var admobAppId by remember { mutableStateOf(settings.admobAppId) }
    var admobBannerUnitId by remember { mutableStateOf(settings.admobBannerUnitId) }
    var admobNativeUnitId by remember { mutableStateOf(settings.admobNativeUnitId) }
    var adsterraPubId by remember { mutableStateOf(settings.adsterraPublisherId) }
    var adsterraPlacement by remember { mutableStateOf(settings.adsterraBannerPlacementId) }
    var adsterraDirectLink by remember { mutableStateOf(settings.adsterraDirectLink) }
    var feedInterval by remember { mutableIntStateOf(settings.feedAdInterval) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Advertisement Network Config", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            // Setup Guide Box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "📌 Adsterra Ads Setup Guide (ऐड कैसे लगाएं):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "1. Adsterra.com पर Publisher Account में लॉगिन करें।\n2. 'Direct Links' में जाकर 'Generate Direct Link' पर क्लिक करें और लिंक कॉपी करें।\n3. नीचे 'Adsterra Direct Link' बॉक्स में पेस्ट करें।\n4. यदि 300x250 बैनर लगाना है, तो 'Websites' से Placement Key या Script पेस्ट करें।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Ads Platform-wide")
                Switch(checked = adsEnabled, onCheckedChange = { adsEnabled = it })
            }

            Text("Adsterra Network Settings", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = adsterraDirectLink,
                onValueChange = { adsterraDirectLink = it },
                label = { Text("Adsterra Direct Link (Smartlink URL)") },
                placeholder = { Text("e.g. https://www.profitablecpmrate.com/...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = adsterraPlacement,
                onValueChange = { adsterraPlacement = it },
                label = { Text("Adsterra Placement Key / Banner Script") },
                placeholder = { Text("e.g. 1a2b3c4d5e or 300x250 script") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = adsterraPubId,
                onValueChange = { adsterraPubId = it },
                label = { Text("Adsterra Publisher ID (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Google AdMob Settings (Optional)", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = admobAppId,
                onValueChange = { admobAppId = it },
                label = { Text("AdMob App ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = admobNativeUnitId,
                onValueChange = { admobNativeUnitId = it },
                label = { Text("Native Feed Unit ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Feed Ad Frequency (every N posts): $feedInterval")
                Row {
                    FilledTonalButton(onClick = { if (feedInterval > 2) feedInterval-- }) { Text("-") }
                    Spacer(modifier = Modifier.width(6.dp))
                    FilledTonalButton(onClick = { if (feedInterval < 10) feedInterval++ }) { Text("+") }
                }
            }

            Button(
                onClick = {
                    onSave(
                        settings.copy(
                            adsEnabled = adsEnabled,
                            admobAppId = admobAppId,
                            admobNativeUnitId = admobNativeUnitId,
                            adsterraPublisherId = adsterraPubId,
                            adsterraBannerPlacementId = adsterraPlacement,
                            adsterraDirectLink = adsterraDirectLink,
                            feedAdInterval = feedInterval
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Ad Configurations")
            }
        }
    }
}
