package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.EarnGramRepository
import com.example.util.LocalLanguageState
import kotlinx.coroutines.launch

@Composable
fun LoginOtpScreen(
    repository: EarnGramRepository,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val langState = LocalLanguageState.current
    val coroutineScope = rememberCoroutineScope()

    var phoneNumber by remember { mutableStateOf("9876543210") }
    var fullName by remember { mutableStateOf("Aman Sharma") }
    var otpCode by remember { mutableStateOf("123456") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Language Toggle at top
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilledTonalButton(
                onClick = { langState.toggleLanguage() },
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.testTag("auth_lang_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (langState.currentLanguage.name == "ENGLISH") "हिंदी" else "English",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Logo Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "EarnGram",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "EarnGram",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = langState.t(
                "Create, Share & Earn Real Ad-Revenue",
                "फोटो और वीडियो शेयर करें, और वास्तविक रिवार्ड्स कमाएं"
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Card Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (!isOtpSent) {
                        langState.t("Mobile Registration / Login", "मोबाइल नंबर से साइन अप / लॉगिन")
                    } else {
                        langState.t("Enter 6-Digit OTP", "6-अंकों का OTP दर्ज करें")
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (!isOtpSent) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text(langState.t("Full Name", "पूरा नाम")) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 10) phoneNumber = it.filter { ch -> ch.isDigit() } },
                        label = { Text(langState.t("10-Digit Mobile Number", "10 अंकों का मोबाइल नंबर")) },
                        prefix = { Text("+91 ") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_phone_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = langState.t(
                            "Rule: Only 1 primary verified account is permitted per mobile number.",
                            "नियम: एक सत्यापित मोबाइल नंबर से केवल एक प्राथमिक अकाउंट अनुमत है।"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            if (phoneNumber.length < 10) {
                                errorMessage = langState.t("Please enter a valid 10-digit number", "कृपया 10 अंकों का मोबाइल नंबर दर्ज करें")
                            } else {
                                errorMessage = null
                                isOtpSent = true
                                successMessage = langState.t("OTP sent to +91 $phoneNumber (Demo OTP: 123456)", "OTP भेजा गया +91 $phoneNumber पर (डेमो OTP: 123456)")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_send_otp_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = langState.t("Get OTP / आगे बढ़ें", "OTP प्राप्त करें"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Text(
                        text = langState.t("Code sent to +91 $phoneNumber", "+91 $phoneNumber पर कोड भेजा गया"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6) otpCode = it.filter { ch -> ch.isDigit() } },
                        label = { Text(langState.t("6-Digit Verification Code", "6-अंकों का वेरिफिकेशन कोड")) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_otp_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { isOtpSent = false }
                        ) {
                            Text(langState.t("Change Number", "नंबर बदलें"))
                        }

                        TextButton(
                            onClick = {
                                otpCode = "123456"
                                successMessage = langState.t("OTP resent: 123456", "OTP पुनः भेजा गया: 123456")
                            }
                        ) {
                            Text(langState.t("Resend OTP", "पुनः OTP भेजें"))
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                val result = repository.verifyOtpAndLogin(
                                    phoneNumber = "+91 $phoneNumber",
                                    otp = otpCode,
                                    fullName = fullName
                                )
                                isLoading = false
                                if (result.isSuccess) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_verify_login_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && otpCode.length == 6
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = langState.t("Verify & Start Earning", "सत्यापित करें और शुरू करें"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Error or Success alerts
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                AnimatedVisibility(visible = successMessage != null) {
                    successMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Compliance notice
        Text(
            text = langState.t(
                "By signing up, you agree to our Terms of Service, Community Guidelines, and 40% Ad Revenue Sharing Monetization Policy. 100% Secure & Compliant.",
                "साइन अप करके आप सेवा की शर्तों, सामुदायिक दिशानिर्देशों और 40% विज्ञापन राजस्व वितरण नीति से सहमत होते हैं।"
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
