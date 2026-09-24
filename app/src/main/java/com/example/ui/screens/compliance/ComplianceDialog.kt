package com.example.ui.screens.compliance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.LocalLanguageState

enum class ComplianceTopic {
    TERMS_AND_CONDITIONS,
    PRIVACY_POLICY,
    COMMUNITY_GUIDELINES,
    WITHDRAWAL_AND_REVENUE_POLICY
}

@Composable
fun ComplianceDialog(
    topic: ComplianceTopic,
    onDismiss: () -> Unit
) {
    val langState = LocalLanguageState.current

    val (title, content) = when (topic) {
        ComplianceTopic.TERMS_AND_CONDITIONS -> {
            langState.t("Terms & Conditions", "सेवा की शर्तें") to langState.t(
                """
                1. Acceptance of Terms: By accessing or registering on EarnGram, you agree to comply with all platform regulations, community guidelines, and monetization policies.
                
                2. Age Requirement: Users must be at least 18 years of age (or minimum legal age in their jurisdiction) to create a verified monetization account and withdraw revenue.
                
                3. One Account Per Mobile Number: To maintain integrity and prevent fraudulent exploitation, each verified mobile number may only operate one primary account. Duplicate or multiple accounts are subject to automatic suspension and forfeiture of illegitimate balances.
                
                4. Content Ownership & Rights: You retain ownership of original photos and videos uploaded. You grant EarnGram a non-exclusive license to host and distribute the media to feed users.
                
                5. Compliance with Advertising Policies: Users must never manipulate or generate artificial impressions, fake clicks, or unauthorized automated traffic.
                """.trimIndent(),
                """
                1. शर्तों की स्वीकृति: EarnGram का उपयोग या पंजीकरण करके आप सभी नियमों, सामुदायिक दिशानिर्देशों और मौद्रिकीकरण नीतियों का पालन करने के लिए सहमत होते हैं।
                
                2. आयु आवश्यकता: मौद्रिकीकरण खाता बनाने और राजस्व निकालने के लिए उपयोगकर्ता की आयु कम से कम 18 वर्ष होनी चाहिए।
                
                3. प्रति मोबाइल नंबर एक खाता: प्रत्येक सत्यापित मोबाइल नंबर केवल एक प्राथमिक खाता संचालित कर सकता है। एकाधिक खाते स्वतः निलंबित हो जाएंगे।
                
                4. सामग्री स्वामित्व: आप अपने मूल फ़ोटो और वीडियो का स्वामित्व बनाए रखते हैं।
                
                5. विज्ञापन नीतियों का पालन: उपयोगकर्ता कभी भी कृत्रिम इम्प्रैशन, फर्जी क्लिक या बॉट ट्रैफ़िक उत्पन्न नहीं करेंगे।
                """.trimIndent()
            )
        }
        ComplianceTopic.PRIVACY_POLICY -> {
            langState.t("Privacy Policy", "गोपनीयता नीति") to langState.t(
                """
                1. Data Collection: We collect your verified mobile number, display name, public bio, uploaded photos/videos, and withdrawal payout details (UPI ID or Bank Account Number with IFSC).
                
                2. Security & Encryption: Financial ledger records and personal verification data are stored with strict security rules. Payout credentials are encrypted and never shared with third parties.
                
                3. Zero Unsolicited Data Sale: We do not sell your personal contact numbers or private details to third-party data brokers.
                
                4. Account Deletion: You have the right to request deletion of your account and purge uploaded media by contacting support.
                """.trimIndent(),
                """
                1. डेटा संग्रह: हम आपका सत्यापित मोबाइल नंबर, नाम, बायो, फ़ोटो/वीडियो और निकासी विवरण (UPI ID या बैंक खाता विवरण) सुरक्षित रखते हैं।
                
                2. सुरक्षा और एन्क्रिप्शन: सभी वित्तीय रिकॉर्ड सर्वर-साइड सुरक्षा नियमों के साथ सुरक्षित हैं।
                
                3. शून्य डेटा बिक्री: हम आपका व्यक्तिगत डेटा किसी तीसरे पक्ष को नहीं बेचते हैं।
                """.trimIndent()
            )
        }
        ComplianceTopic.COMMUNITY_GUIDELINES -> {
            langState.t("Community Guidelines", "सामुदायिक दिशानिर्देश") to langState.t(
                """
                EarnGram is built for authentic creativity and safe expression.
                
                STRICTLY PROHIBITED:
                • Adult, sexually explicit, or pornographic material
                • Violence, hate speech, harassment, or threats
                • Copyright-infringing photos or re-uploaded movies/shows without rights
                • Scams, pyramid schemes, or illegal activities
                • Bot automation, like-farming, or fake click groups
                
                VIOLATION CONSEQUENCES:
                Violating posts will be removed immediately. Accounts engaging in prohibited activity will be permanently suspended and disqualified from reward distributions.
                """.trimIndent(),
                """
                EarnGram सुरक्षित और रचनात्मक अभिव्यक्ति के लिए बनाया गया है।
                
                सख्ती से प्रतिबंधित:
                • अश्लील या वयस्क सामग्री
                • हिंसा, नफरत भरा भाषण, उत्पीड़न
                • कॉपीराइट उल्लंघन वाली सामग्री
                • बॉट ऑटोमेशन या फर्जी क्लिक समूह
                
                उल्लंघन पर कार्रवाई:
                उल्लंघन करने वाले पोस्ट तुरंत हटा दिए जाएंगे और खाता निलंबित कर दिया जाएगा।
                """.trimIndent()
            )
        }
        ComplianceTopic.WITHDRAWAL_AND_REVENUE_POLICY -> {
            langState.t("Withdrawal & Monetization Policy", "निकासी व राजस्व नीति") to langState.t(
                """
                REVENUE DISTRIBUTION FORMULA:
                Total verified advertising revenue = 100%
                • 40% User Reward Pool (Distributed to eligible creators based on genuine engagement score)
                • 60% Platform Share (Server hosting, bandwidth, security, payment gateways, and taxes)
                
                MINIMUM WITHDRAWAL:
                • The minimum payout threshold is ₹100.
                • Payout methods: Instant UPI or Direct Bank Transfer (NEFT/IMPS).
                
                FRAUD PREVENTION:
                • Self-likes, rapid bot clicks, and repetitive coordinated interactions are detected by the Anti-Fraud Engine and excluded from reward point calculations.
                • Zero tolerance for artificial ad traffic.
                """.trimIndent(),
                """
                राजस्व वितरण सूत्र:
                सत्यापित विज्ञापन राजस्व = 100%
                • 40% यूज़र रिवार्ड पूल (सच्चे जुड़ाव के आधार पर क्रिएटरों में वितरित)
                • 60% प्लेटफॉर्म शेयर (सर्वर, बैंडविड्थ, सुरक्षा व टैक्स)
                
                न्यूनतम निकासी:
                • न्यूनतम निकासी राशि ₹100 है।
                • माध्यम: UPI या बैंक खाता।
                
                धोखाधड़ी रोकथाम:
                • स्वयं के लाइक्स और बॉट क्लिक्स को रिवार्ड स्कोर से बाहर रखा जाता है।
                """.trimIndent()
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text(langState.t("Close", "बंद करें"))
            }
        }
    )
}
