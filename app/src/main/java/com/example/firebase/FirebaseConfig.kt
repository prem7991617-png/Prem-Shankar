package com.example.firebase

/**
 * Firebase Production Backend Configuration
 *
 * How to connect this app to your Firebase Project:
 * 1. Go to Firebase Console (https://console.firebase.google.com/)
 * 2. Create a project and add an Android app with package name:
 *    com.aistudio.earngram.vzqkm
 * 3. Download 'google-services.json' and place it in the '/app/' directory.
 * 4. Enable Firebase Authentication (Phone Auth).
 * 5. Enable Cloud Firestore and deploy the provided 'firestore.rules'.
 * 6. Enable Cloud Storage and deploy 'storage.rules'.
 * 7. Deploy Cloud Functions from '/functions/' for server-side 40/60% revenue sharing calculations.
 */
object FirebaseConfig {
    // Configurable placeholders
    const val FIREBASE_PROJECT_ID = "earngram-prod-2026"
    const val FIREBASE_STORAGE_BUCKET = "earngram-prod-2026.appspot.com"
    const val FIREBASE_API_KEY = "AIzaSy_YOUR_FIREBASE_API_KEY_HERE"
    const val FIREBASE_APP_ID = "1:905562883174:android:a1b2c3d4e5f67890"

    // Firestore Collections
    object Collections {
        const val USERS = "users"
        const val POSTS = "posts"
        const val LIKES = "likes"
        const val COMMENTS = "comments"
        const val WALLETS = "wallets"
        const val TRANSACTIONS = "transactions"
        const val WITHDRAWALS = "withdrawals"
        const val REPORTS = "reports"
        const val REWARD_CYCLES = "reward_cycles"
        const val AD_REVENUE = "ad_revenue"
        const val NOTIFICATIONS = "notifications"
        const val ADMIN_SETTINGS = "admin_settings"
    }

    // Server-Side Anti-Fraud and Monetization Constants
    const val USER_REWARD_POOL_PERCENT = 40.0 // 40%
    const val PLATFORM_SHARE_PERCENT = 60.0 // 60%
    const val MINIMUM_WITHDRAWAL_INR = 100.0 // ₹100
}
