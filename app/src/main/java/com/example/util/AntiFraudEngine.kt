package com.example.util

import com.example.data.local.EarnGramDatabase

object AntiFraudEngine {
    private const val MAX_LIKES_PER_10_SECONDS = 8
    private val recentLikeTimestamps = mutableMapOf<String, MutableList<Long>>()

    data class LikeValidationResult(
        val isAllowed: Boolean,
        val isEligibleForReward: Boolean,
        val reason: String
    )

    fun validateLikeAction(
        currentUserId: String,
        postOwnerId: String,
        isAlreadyLiked: Boolean
    ): LikeValidationResult {
        // 1. Self-like check: permitted for fun, but completely excluded from reward pool
        if (currentUserId == postOwnerId) {
            return LikeValidationResult(
                isAllowed = true,
                isEligibleForReward = false,
                reason = "Self-engagement: Excluded from reward pool calculation to maintain fairness."
            )
        }

        // 2. Already liked check: prevent duplicate reward triggers
        if (isAlreadyLiked) {
            return LikeValidationResult(
                isAllowed = false,
                isEligibleForReward = false,
                reason = "Duplicate like prevented."
            )
        }

        // 3. Rate limiting / Bot burst detection
        val now = System.currentTimeMillis()
        val userLikes = recentLikeTimestamps.getOrPut(currentUserId) { mutableListOf() }
        userLikes.removeAll { now - it > 10_000 } // Keep last 10 seconds

        if (userLikes.size >= MAX_LIKES_PER_10_SECONDS) {
            return LikeValidationResult(
                isAllowed = true,
                isEligibleForReward = false,
                reason = "High-frequency interaction detected: flagged as suspicious / ineligible for revenue share."
            )
        }

        userLikes.add(now)
        return LikeValidationResult(
            isAllowed = true,
            isEligibleForReward = true,
            reason = "Valid eligible engagement."
        )
    }

    /**
     * Calculates the creator's eligible reward score based on real engagement.
     * Weights: Photo like = 1.0, Video like = 1.5, Comment = 2.0, View = 0.1
     */
    fun calculatePostEligibleScore(
        mediaType: String,
        eligibleLikes: Int,
        commentsCount: Int,
        viewsCount: Int
    ): Double {
        val likeWeight = if (mediaType == "VIDEO") 1.5 else 1.0
        val commentWeight = 2.0
        val viewWeight = 0.1
        return (eligibleLikes * likeWeight) + (commentsCount * commentWeight) + (viewsCount * viewWeight)
    }

    /**
     * Validates withdrawal parameters to prevent fraudulent or malformed payouts.
     */
    fun validateWithdrawalRequest(
        amount: Double,
        availableBalance: Double,
        method: String,
        upiId: String,
        accountHolderName: String,
        accountNumber: String,
        ifscCode: String
    ): Pair<Boolean, String> {
        if (amount < 100.0) {
            return false to "Minimum withdrawal amount is ₹100 / न्यूनतम निकासी राशि ₹100 है।"
        }
        if (amount > availableBalance) {
            return false to "Insufficient available balance / अपर्याप्त उपलब्ध राशि।"
        }
        if (method == "UPI") {
            if (upiId.isBlank() || !upiId.contains("@") || upiId.length < 5) {
                return false to "Please enter a valid UPI ID (e.g. name@okhdfcbank) / कृपया मान्य UPI ID दर्ज करें।"
            }
        } else {
            if (accountHolderName.trim().length < 3) {
                return false to "Enter valid Account Holder Name / खाताधारक का नाम दर्ज करें।"
            }
            if (accountNumber.trim().length < 9 || accountNumber.trim().length > 18) {
                return false to "Account number must be 9-18 digits / खाता संख्या 9-18 अंकों की होनी चाहिए।"
            }
            val ifscRegex = Regex("^[A-Z]{4}0[A-Z0-9]{6}$", RegexOption.IGNORE_CASE)
            if (!ifscRegex.matches(ifscCode.trim())) {
                return false to "Invalid IFSC Code format (e.g. SBIN0001234) / अमान्य IFSC कोड प्रारूप।"
            }
        }
        return true to "OK"
    }
}
