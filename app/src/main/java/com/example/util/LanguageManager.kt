package com.example.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage {
    ENGLISH, HINDI
}

class LanguageState {
    var currentLanguage by mutableStateOf(AppLanguage.ENGLISH)
        private set

    fun toggleLanguage() {
        currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
    }

    fun setLanguage(lang: AppLanguage) {
        currentLanguage = lang
    }

    fun t(en: String, hi: String): String {
        return if (currentLanguage == AppLanguage.HINDI) hi else en
    }
}

val LocalLanguageState = compositionLocalOf { LanguageState() }

@Composable
fun rememberLanguageManager(): LanguageState = androidx.compose.runtime.remember { LanguageState() }


object AppStrings {
    // Nav
    const val NAV_HOME_EN = "Home"
    const val NAV_HOME_HI = "होम"
    const val NAV_UPLOAD_EN = "Upload"
    const val NAV_UPLOAD_HI = "अपलोड"
    const val NAV_WALLET_EN = "Wallet"
    const val NAV_WALLET_HI = "वॉलेट"
    const val NAV_NOTIF_EN = "Alerts"
    const val NAV_NOTIF_HI = "सूचनाएं"
    const val NAV_PROFILE_EN = "Profile"
    const val NAV_PROFILE_HI = "प्रोफाइल"

    // Feed
    const val TAB_LATEST_EN = "Latest"
    const val TAB_LATEST_HI = "ताज़ा"
    const val TAB_POPULAR_EN = "Popular / Trending"
    const val TAB_POPULAR_HI = "लोकप्रिय / ट्रेंडिंग"
    const val REPORT_POST_EN = "Report Post"
    const val REPORT_POST_HI = "पोस्ट की शिकायत करें"
    const val SHARE_POST_EN = "Share"
    const val SHARE_POST_HI = "शेयर करें"
    const val COMMENTS_EN = "Comments"
    const val COMMENTS_HI = "टिप्पणियाँ"
    const val WRITE_COMMENT_EN = "Add a thoughtful comment..."
    const val WRITE_COMMENT_HI = "एक टिप्पणी लिखें..."

    // Wallet
    const val WALLET_TITLE_EN = "Creator Wallet & Earnings"
    const val WALLET_TITLE_HI = "क्रिएटर वॉलेट और कमाई"
    const val TOTAL_EARNINGS_EN = "Total Earnings"
    const val TOTAL_EARNINGS_HI = "कुल कमाई"
    const val AVAILABLE_BALANCE_EN = "Available Balance"
    const val AVAILABLE_BALANCE_HI = "उपलब्ध बैलेंस"
    const val PENDING_REWARDS_EN = "Pending Rewards"
    const val PENDING_REWARDS_HI = "प्रक्रियाधीन रिवार्ड्स"
    const val WITHDRAWN_AMOUNT_EN = "Withdrawn Amount"
    const val WITHDRAWN_AMOUNT_HI = "निकाली गई राशि"
    const val WITHDRAW_NOW_EN = "Withdraw Funds"
    const val WITHDRAW_NOW_HI = "रुपये निकालें"
    const val MIN_WITHDRAWAL_NOTE_EN = "Minimum withdrawal: ₹100 • 0% Platform withdrawal fee"
    const val MIN_WITHDRAWAL_NOTE_HI = "न्यूनतम निकासी: ₹100 • 0% प्लेटफॉर्म निकासी शुल्क"
    const val REVENUE_SHARE_TITLE_EN = "40% Ad-Revenue Sharing Model"
    const val REVENUE_SHARE_TITLE_HI = "40% विज्ञापन राजस्व वितरण मॉडल"

    // Upload
    const val UPLOAD_TITLE_EN = "Create & Monetize Post"
    const val UPLOAD_TITLE_HI = "पोस्ट बनाएं और कमाएं"
    const val SELECT_MEDIA_EN = "Choose Photo or Video"
    const val SELECT_MEDIA_HI = "फोटो या वीडियो चुनें"
    const val CAPTION_HINT_EN = "Write an engaging caption... (Add #hashtags)"
    const val CAPTION_HINT_HI = "एक आकर्षक कैप्शन लिखें... (#hashtags जोड़ें)"
    const val MODERATION_NOTICE_EN = "Posts undergo automated & community moderation before distribution."
    const val MODERATION_NOTICE_HI = "पोस्ट वितरण से पहले ऑटोमेटेड और मॉडरेशन जांच से गुजरती हैं।"
}
