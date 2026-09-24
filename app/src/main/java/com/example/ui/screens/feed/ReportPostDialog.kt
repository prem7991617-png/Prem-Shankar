package com.example.ui.screens.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Post
import com.example.data.model.ReportReason
import com.example.data.repository.EarnGramRepository
import com.example.util.LocalLanguageState
import kotlinx.coroutines.launch

@Composable
fun ReportPostDialog(
    post: Post,
    repository: EarnGramRepository,
    onDismiss: () -> Unit,
    onReportSubmitted: () -> Unit
) {
    val langState = LocalLanguageState.current
    val coroutineScope = rememberCoroutineScope()
    var selectedReason by remember { mutableStateOf(ReportReason.SPAM_OR_BOT) }
    var additionalDetails by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = langState.t("Report Content / रिपोर्ट करें", "सामग्री की रिपोर्ट करें"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = langState.t(
                        "Help keep EarnGram safe and compliant. Why are you reporting this post?",
                        "EarnGram को सुरक्षित और नीति-सम्मत बनाए रखें। आप इस पोस्ट की रिपोर्ट क्यों कर रहे हैं?"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                val reasons = listOf(
                    ReportReason.SPAM_OR_BOT to langState.t("Spam or Bot Activity", "स्पैम या बॉट गतिविधि"),
                    ReportReason.FAKE_ENGAGEMENT to langState.t("Fake Engagement / Fraud Likes", "नकली जुड़ाव / फर्जी लाइक्स"),
                    ReportReason.UNSAFE_ILLEGAL_CONTENT to langState.t("Unsafe or Prohibited Content", "असुरक्षित या प्रतिबंधित सामग्री"),
                    ReportReason.HARASSMENT to langState.t("Harassment or Bullying", "उत्पीड़न या दुर्व्यवहार"),
                    ReportReason.COPYRIGHT_VIOLATION to langState.t("Copyright Infringement", "कॉपीराइट उल्लंघन"),
                    ReportReason.OTHER to langState.t("Other Reason", "अन्य कारण")
                )

                reasons.forEach { (reason, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedReason == reason),
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = additionalDetails,
                    onValueChange = { additionalDetails = it },
                    label = { Text(langState.t("Additional comments (optional)", "अतिरिक्त विवरण (वैकल्पिक)")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_details_input"),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    coroutineScope.launch {
                        repository.reportPost(
                            postId = post.id,
                            reason = selectedReason,
                            details = additionalDetails
                        )
                        isSubmitting = false
                        onReportSubmitted()
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text(langState.t("Submit Report", "रिपोर्ट भेजें"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(langState.t("Cancel", "रद्द करें"))
            }
        }
    )
}
