package com.example.ui.screens.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WithdrawalMethod
import com.example.data.repository.EarnGramRepository
import com.example.util.LocalLanguageState
import kotlinx.coroutines.launch

@Composable
fun WithdrawDialog(
    availableBalance: Double,
    repository: EarnGramRepository,
    onDismiss: () -> Unit,
    onWithdrawalSubmitted: () -> Unit
) {
    val langState = LocalLanguageState.current
    val coroutineScope = rememberCoroutineScope()

    var amountText by remember { mutableStateOf(if (availableBalance >= 100.0) "100" else "") }
    var selectedMethod by remember { mutableStateOf(WithdrawalMethod.UPI) }
    var upiId by remember { mutableStateOf("amansharma@okaxis") }
    var accountHolderName by remember { mutableStateOf("Aman Sharma") }
    var accountNumber by remember { mutableStateOf("918273645012") }
    var ifscCode by remember { mutableStateOf("HDFC0001234") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = langState.t("Request Withdrawal / निकासी", "राशि निकासी अनुरोध"),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Available Balance Info
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = langState.t("Available Balance:", "उपलब्ध बैलेंस:"),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "₹${"%.2f".format(availableBalance)}",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountText = input
                            errorMessage = null
                        }
                    },
                    label = { Text(langState.t("Withdrawal Amount (₹)", "निकासी राशि (₹)")) },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input"),
                    shape = RoundedCornerShape(10.dp),
                    supportingText = {
                        Text(
                            text = langState.t("Minimum: ₹100 • No fees", "न्यूनतम: ₹100 • कोई शुल्क नहीं"),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )

                // Method Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = (selectedMethod == WithdrawalMethod.UPI),
                        onClick = { selectedMethod = WithdrawalMethod.UPI },
                        label = { Text("UPI ID") },
                        leadingIcon = {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("withdraw_method_upi")
                    )

                    FilterChip(
                        selected = (selectedMethod == WithdrawalMethod.BANK_ACCOUNT),
                        onClick = { selectedMethod = WithdrawalMethod.BANK_ACCOUNT },
                        label = { Text(langState.t("Bank Transfer", "बैंक खाता")) },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("withdraw_method_bank")
                    )
                }

                if (selectedMethod == WithdrawalMethod.UPI) {
                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it; errorMessage = null },
                        label = { Text("UPI ID") },
                        placeholder = { Text("e.g. yourname@okhdfcbank") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_upi_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = accountHolderName,
                        onValueChange = { accountHolderName = it; errorMessage = null },
                        label = { Text(langState.t("Account Holder Name", "खाताधारक का नाम")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_holder_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it; errorMessage = null },
                        label = { Text(langState.t("Account Number", "बैंक खाता संख्या")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_account_number_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = ifscCode,
                        onValueChange = { ifscCode = it.uppercase(); errorMessage = null },
                        label = { Text(langState.t("IFSC Code", "IFSC कोड")) },
                        placeholder = { Text("e.g. HDFC0001234") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_ifsc_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Error message
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountValue < 100.0) {
                        errorMessage = langState.t("Minimum withdrawal amount is ₹100", "न्यूनतम निकासी राशि ₹100 है")
                        return@Button
                    }
                    if (amountValue > availableBalance) {
                        errorMessage = langState.t("Amount exceeds available balance", "राशि उपलब्ध बैलेंस से अधिक है")
                        return@Button
                    }

                    isSubmitting = true
                    coroutineScope.launch {
                        val result = repository.requestWithdrawal(
                            amount = amountValue,
                            method = selectedMethod,
                            upiId = upiId,
                            accountHolderName = accountHolderName,
                            accountNumber = accountNumber,
                            ifscCode = ifscCode
                        )
                        isSubmitting = false
                        if (result.isSuccess) {
                            onWithdrawalSubmitted()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Withdrawal request failed"
                        }
                    }
                },
                enabled = !isSubmitting && amountValue >= 100.0 && amountValue <= availableBalance,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_withdrawal_confirm_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(langState.t("Confirm & Submit", "पुष्टि करें और भेजें"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(langState.t("Cancel", "रद्द करें"))
            }
        }
    )
}
