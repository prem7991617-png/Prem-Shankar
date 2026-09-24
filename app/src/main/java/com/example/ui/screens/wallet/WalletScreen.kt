package com.example.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdSettings
import com.example.data.model.TransactionType
import com.example.data.model.Wallet
import com.example.data.model.WalletTransaction
import com.example.data.model.WithdrawalRequest
import com.example.data.model.WithdrawalStatus
import com.example.data.repository.EarnGramRepository
import com.example.ui.components.CompliantNativeAdCard
import com.example.util.LocalLanguageState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    repository: EarnGramRepository,
    modifier: Modifier = Modifier
) {
    val langState = LocalLanguageState.current
    val wallet by repository.getWallet().collectAsStateWithLifecycle(initialValue = Wallet("user_primary_101"))
    val transactions by repository.getTransactions().collectAsStateWithLifecycle(initialValue = emptyList())
    val withdrawals by repository.getWithdrawals().collectAsStateWithLifecycle(initialValue = emptyList())

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showExplainerModal by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Transactions Ledger, 1 = Withdrawals

    val currentWallet = wallet ?: Wallet("user_primary_101")
    val adSettings by repository.getAdSettings().collectAsStateWithLifecycle(initialValue = AdSettings())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = langState.t("Creator Wallet", "क्रिएटर वॉलेट और कमाई"),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showExplainerModal = true },
                        modifier = Modifier.testTag("revenue_rule_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Revenue Share Rule",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = langState.t("AVAILABLE BALANCE", "उपलब्ध बैलेंस"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "0% Payout Fee",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "₹${"%.2f".format(currentWallet.availableBalance)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        // Action Button
                        Button(
                            onClick = { showWithdrawDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("open_withdraw_dialog_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = currentWallet.availableBalance >= 100.0
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = langState.t("Withdraw Funds (Min ₹100)", "रुपये निकालें (न्यूनतम ₹100)"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        if (currentWallet.availableBalance < 100.0) {
                            Text(
                                text = langState.t(
                                    "Need ₹${"%.2f".format(100.0 - currentWallet.availableBalance)} more to reach minimum withdrawal threshold of ₹100.",
                                    "₹100 की न्यूनतम निकासी सीमा तक पहुँचने के लिए ₹${"%.2f".format(100.0 - currentWallet.availableBalance)} और चाहिए।"
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Metric Cards Grid (Total Earnings, Pending Rewards, Withdrawn Amount)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = langState.t("Total Earnings", "कुल कमाई"),
                        amount = "₹${"%.2f".format(currentWallet.totalEarnings)}",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = langState.t("Pending Review", "प्रक्रियाधीन"),
                        amount = "₹${"%.2f".format(currentWallet.pendingRewards)}",
                        icon = Icons.Default.HourglassTop,
                        iconTint = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = langState.t("Withdrawn", "निकाली गई"),
                        amount = "₹${"%.2f".format(currentWallet.withdrawnAmount)}",
                        icon = Icons.Default.CheckCircle,
                        iconTint = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 40% / 60% Revenue Sharing Model Highlight Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = langState.t("100% Eligible Ad Revenue Formula", "100% विज्ञापन राजस्व वितरण नियम"),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        // Split progress indicator
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = langState.t("40% Creator Reward Pool", "40% क्रिएटर रिवार्ड पूल"),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = langState.t("60% Platform Share", "60% प्लेटफॉर्म शेयर"),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            LinearProgressIndicator(
                                progress = { 0.40f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        Text(
                            text = langState.t(
                                "Example: If eligible ad revenue is ₹50,000 → ₹20,000 is distributed among active eligible creators based on genuine views, likes, and comments!",
                                "उदाहरण: यदि विज्ञापन राजस्व ₹50,000 है → तो ₹20,000 सक्रिय क्रिएटरों के बीच उनके वास्तविक जुड़ाव स्कोर के आधार पर वितरित किया जाता है!"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Sponsored Adsterra Partner Card on Wallet Screen
            if (adSettings.adsEnabled && adSettings.showOnWallet) {
                item {
                    CompliantNativeAdCard(
                        adSettings = adSettings,
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                }
            }

            // Ledger Tabs (Transactions vs Withdrawals)
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(langState.t("Transactions (${transactions.size})", "लेन-देन")) },
                        modifier = Modifier.testTag("tab_transactions")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(langState.t("Withdrawals (${withdrawals.size})", "निकासी इतिहास")) },
                        modifier = Modifier.testTag("tab_withdrawals")
                    )
                }
            }

            if (selectedTab == 0) {
                if (transactions.isEmpty()) {
                    item {
                        EmptyStateText(langState.t("No transactions yet.", "कोई लेन-देन नहीं है।"))
                    }
                } else {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionRow(tx = tx)
                    }
                }
            } else {
                if (withdrawals.isEmpty()) {
                    item {
                        EmptyStateText(langState.t("No withdrawal requests yet.", "कोई निकासी अनुरोध नहीं है।"))
                    }
                } else {
                    items(withdrawals, key = { it.id }) { wd ->
                        WithdrawalRow(wd = wd)
                    }
                }
            }
        }

        // Withdraw Dialog
        if (showWithdrawDialog) {
            WithdrawDialog(
                availableBalance = currentWallet.availableBalance,
                repository = repository,
                onDismiss = { showWithdrawDialog = false },
                onWithdrawalSubmitted = {
                    showWithdrawDialog = false
                }
            )
        }

        // 40/60 Explainer Modal Dialog
        if (showExplainerModal) {
            AlertDialog(
                onDismissRequest = { showExplainerModal = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = langState.t("Monetization & Revenue Sharing", "कमाई व विज्ञापन राजस्व वितरण नियम"),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = langState.t(
                                "1. Revenue Split: 100% of verified ad revenue from Google Ads & Adsterra is divided into:\n• 40% User Reward Pool\n• 60% Platform Share (Server, Maintenance, Taxes)",
                                "1. राजस्व विभाजन: Google Ads व Adsterra से प्राप्त 100% राजस्व:\n• 40% यूज़र रिवार्ड पूल\n• 60% प्लेटफॉर्म शेयर (सर्वर, मेंटेनेंस, टैक्स)"
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = langState.t(
                                "2. Anti-Fraud & Quality: Fake accounts, automated bots, self-likes, and spam engagement are strictly filtered out and given zero score.",
                                "2. एंटी-फ्रॉड सुरक्षा: फेक अकाउंट, बॉट्स, स्वयं के लाइक्स और स्पैम जुड़ाव को रिवार्ड गणना से बाहर रखा जाता है।"
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = langState.t(
                                "3. Payouts: Minimum withdrawal is ₹100 via Instant UPI or Direct Bank Transfer (NEFT/IMPS).",
                                "3. निकासी: न्यूनतम निकासी ₹100 है, जिसे तत्काल UPI या बैंक खाते द्वारा प्राप्त किया जा सकता है।"
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showExplainerModal = false }) {
                        Text(langState.t("Understood", "समझ गया"))
                    }
                }
            )
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    val isCredit = tx.type == TransactionType.REWARD_CREDIT || tx.type == TransactionType.BONUS || tx.type == TransactionType.WITHDRAWAL_REFUND
    val amountColor = if (isCredit) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(amountColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = amountColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = tx.description,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (isCredit) "+" else "-"}₹${"%.2f".format(tx.amount)}",
                fontWeight = FontWeight.Bold,
                color = amountColor,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun WithdrawalRow(wd: WithdrawalRequest) {
    val (statusColor, statusText) = when (wd.status) {
        WithdrawalStatus.PAID -> Color(0xFF10B981) to "PAID / सफल"
        WithdrawalStatus.APPROVED -> Color(0xFF3B82F6) to "APPROVED / स्वीकृत"
        WithdrawalStatus.PROCESSING -> Color(0xFF8B5CF6) to "PROCESSING / प्रक्रियाधीन"
        WithdrawalStatus.REJECTED -> Color(0xFFEF4444) to "REJECTED / अस्वीकृत"
        WithdrawalStatus.PENDING -> Color(0xFFF59E0B) to "PENDING / समीक्षाधीन"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${wd.method}: ${if (wd.upiId.isNotBlank()) wd.upiId else wd.accountHolderName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "₹${"%.2f".format(wd.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: ${wd.id} • ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(wd.createdAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (wd.rejectionReason.isNotBlank()) {
                Text(
                    text = "Reason: ${wd.rejectionReason}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyStateText(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
