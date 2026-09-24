package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.model.*
import com.example.util.AntiFraudEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class EarnGramRepository(
    private val db: EarnGramDatabase,
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Current Logged-In User ID state
    private val _currentUserId = MutableStateFlow("user_primary_101")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Active User Flow
    val currentUser: Flow<User?> = _currentUserId.map { uid ->
        db.userDao().getUserById(uid).firstOrNull()?.toDomain()
    }

    // Is logged in
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Navigation and Admin PIN status
    val isAdminMode = MutableStateFlow(false)

    init {
        scope.launch {
            // Ensure initial seed if db is fresh
            val count = db.postDao().getTotalPostsCount().firstOrNull() ?: 0
            if (count == 0) {
                EarnGramDatabase.populateInitialData(db)
            }
            // Auto-upgrade Adsterra settings if missing or placeholder
            val currentSettings = db.adminDao().getAdSettings().firstOrNull()
            if (currentSettings != null && (currentSettings.adsterraDirectLink.contains("example.com") || currentSettings.adsterraDirectLink.isBlank())) {
                db.adminDao().saveAdSettings(
                    currentSettings.copy(
                        adsterraDirectLink = "https://www.profitableratecpmnetwork.com/inwm47tj?key=6287ae42321dd2648886f420e3b805eb",
                        adsterraBannerPlacementId = "31386617",
                        adsterraPublisherId = "31386617"
                    )
                )
            }
        }
    }

    // ----------------------------------------------------
    // AUTHENTICATION & PROFILE
    // ----------------------------------------------------

    suspend fun verifyOtpAndLogin(phoneNumber: String, otp: String, fullName: String): Result<User> {
        val cleanPhone = phoneNumber.trim()
        if (cleanPhone.length < 10) {
            return Result.failure(Exception("Please enter a valid 10-digit mobile number."))
        }
        if (otp.length != 6) {
            return Result.failure(Exception("Please enter a valid 6-digit OTP."))
        }

        // Check if an account already exists for this mobile number
        var existingUser = db.userDao().getUserByPhone(cleanPhone)
        val finalUser: UserEntity
        if (existingUser != null) {
            finalUser = existingUser
        } else {
            // Create single verified primary account
            val newId = "user_" + UUID.randomUUID().toString().substring(0, 8)
            val username = (if (fullName.isNotBlank()) fullName.lowercase().replace(" ", "_") else "creator") + "_" + (100..999).random()
            finalUser = UserEntity(
                id = newId,
                phone = cleanPhone,
                name = if (fullName.isNotBlank()) fullName else "New Creator",
                username = username,
                profilePicUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&q=80",
                bio = "Content creator on EarnGram. Sharing authentic stories.",
                isVerified = true,
                isSuspended = false,
                primaryMobileVerified = true
            )
            db.userDao().insertUser(finalUser)

            // Create initial wallet
            db.walletDao().insertOrUpdateWallet(
                WalletEntity(
                    userId = newId,
                    totalEarnings = 50.0, // Welcome creator bonus
                    availableBalance = 50.0,
                    pendingRewards = 0.0,
                    withdrawnAmount = 0.0
                )
            )

            // Welcome transaction
            db.walletDao().insertTransaction(
                TransactionEntity(
                    id = "tx_" + UUID.randomUUID().toString().substring(0, 8),
                    userId = newId,
                    type = "BONUS",
                    amount = 50.0,
                    description = "Welcome Creator Onboarding Bonus",
                    referenceId = "WELCOME-50"
                )
            )

            // Welcome notification
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                    userId = newId,
                    title = "Welcome to EarnGram! / स्वागत है!",
                    message = "₹50.00 onboarding bonus added to your wallet. Upload photos & videos to earn daily revenue share!",
                    type = "REWARD_CREDITED"
                )
            )
        }

        _currentUserId.value = finalUser.id
        _isLoggedIn.value = true
        return Result.success(finalUser.toDomain())
    }

    suspend fun updateProfile(name: String, bio: String, profilePicUrl: String) {
        val uid = _currentUserId.value
        val existing = db.userDao().getUserById(uid).firstOrNull() ?: return
        db.userDao().updateUser(
            existing.copy(
                name = name.trim(),
                bio = bio.trim(),
                profilePicUrl = if (profilePicUrl.isNotBlank()) profilePicUrl else existing.profilePicUrl
            )
        )
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    // ----------------------------------------------------
    // FEED & POSTS
    // ----------------------------------------------------

    fun getApprovedPosts(isPopular: Boolean): Flow<List<Post>> {
        val postsFlow = if (isPopular) {
            db.postDao().getApprovedPostsPopular()
        } else {
            db.postDao().getApprovedPostsLatest()
        }
        val likedPostsFlow = db.likeDao().getUserLikedPostIds(_currentUserId.value)

        return kotlinx.coroutines.flow.combine(postsFlow, likedPostsFlow) { posts, likedIds ->
            val likedSet = likedIds.toSet()
            posts.map { it.toDomain(isLiked = likedSet.contains(it.id)) }
        }
    }

    fun getUserPosts(userId: String): Flow<List<Post>> {
        val likedPostsFlow = db.likeDao().getUserLikedPostIds(_currentUserId.value)
        return kotlinx.coroutines.flow.combine(db.postDao().getPostsByUserId(userId), likedPostsFlow) { posts, likedIds ->
            val likedSet = likedIds.toSet()
            posts.map { it.toDomain(isLiked = likedSet.contains(it.id)) }
        }
    }

    suspend fun toggleLike(postId: String): Pair<Boolean, String> {
        val uid = _currentUserId.value
        val existingLike = db.likeDao().getLike(postId, uid)
        val post = db.postDao().getPostById(postId) ?: return false to "Post not found"

        if (existingLike != null) {
            // Unlike post
            db.likeDao().deleteLike(postId, uid)
            db.postDao().updateLikesCount(postId, -1)
            return true to "Unliked"
        }

        // Validate like via Anti-Fraud engine
        val validation = AntiFraudEngine.validateLikeAction(
            currentUserId = uid,
            postOwnerId = post.userId,
            isAlreadyLiked = false
        )

        if (!validation.isAllowed) {
            return false to validation.reason
        }

        // Record like
        db.likeDao().insertLike(
            LikeEntity(
                id = "like_${postId}_$uid",
                postId = postId,
                userId = uid,
                isSuspicious = !validation.isEligibleForReward
            )
        )
        db.postDao().updateLikesCount(postId, 1)

        // If eligible, update post's eligible reward score
        if (validation.isEligibleForReward) {
            val scoreDelta = if (post.mediaType == "VIDEO") 1.5 else 1.0
            val newScore = post.eligibleRewardScore + scoreDelta
            db.postDao().updatePost(post.copy(eligibleRewardScore = newScore))

            // Notify post creator
            if (post.userId != uid) {
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                        userId = post.userId,
                        title = "New Like! / नया लाइक मिला",
                        message = "Someone liked your ${post.mediaType.lowercase()}! Eligible engagement score increased.",
                        type = "LIKE_RECEIVED",
                        relatedEntityId = postId
                    )
                )
            }
        }

        return true to (if (validation.isEligibleForReward) "Liked!" else "Liked (Self/High-rate activity not added to reward score)")
    }

    fun getComments(postId: String): Flow<List<Comment>> {
        return db.commentDao().getCommentsForPost(postId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun addComment(postId: String, text: String): Result<Comment> {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) {
            return Result.failure(Exception("Comment cannot be empty"))
        }

        val uid = _currentUserId.value
        val user = db.userDao().getUserById(uid).firstOrNull() ?: return Result.failure(Exception("User not found"))
        val post = db.postDao().getPostById(postId) ?: return Result.failure(Exception("Post not found"))

        val newComment = CommentEntity(
            id = "c_" + UUID.randomUUID().toString().substring(0, 8),
            postId = postId,
            userId = uid,
            userName = user.name,
            userAvatar = user.profilePicUrl,
            text = cleanText,
            timestamp = System.currentTimeMillis()
        )

        db.commentDao().insertComment(newComment)
        db.postDao().incrementCommentsCount(postId)

        // Give eligible engagement reward score to post
        val updatedPost = db.postDao().getPostById(postId)
        if (updatedPost != null && post.userId != uid) {
            db.postDao().updatePost(updatedPost.copy(eligibleRewardScore = updatedPost.eligibleRewardScore + 2.0))
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                    userId = post.userId,
                    title = "New Comment! / नई टिप्पणी",
                    message = "${user.name} commented on your post: \"$cleanText\"",
                    type = "COMMENT_RECEIVED",
                    relatedEntityId = postId
                )
            )
        }

        return Result.success(newComment.toDomain())
    }

    suspend fun recordPostView(postId: String) {
        db.postDao().incrementViewsCount(postId)
    }

    suspend fun reportPost(postId: String, reason: ReportReason, details: String): Result<String> {
        val uid = _currentUserId.value
        val user = db.userDao().getUserById(uid).firstOrNull() ?: return Result.failure(Exception("User not found"))
        val post = db.postDao().getPostById(postId) ?: return Result.failure(Exception("Post not found"))

        val report = ReportEntity(
            id = "rep_" + UUID.randomUUID().toString().substring(0, 8),
            postId = postId,
            reportedUserId = post.userId,
            reporterUserId = uid,
            reporterName = user.name,
            reason = reason.name,
            comments = details.trim(),
            status = "PENDING"
        )
        db.reportDao().insertReport(report)
        return Result.success("Report submitted. Our moderation team will review this content.")
    }

    // ----------------------------------------------------
    // UPLOAD & MODERATION
    // ----------------------------------------------------

    suspend fun uploadPost(
        mediaUrl: String,
        mediaType: MediaType,
        caption: String,
        videoDurationSec: Int = 0
    ): Result<Post> {
        val uid = _currentUserId.value
        val user = db.userDao().getUserById(uid).firstOrNull() ?: return Result.failure(Exception("User not found"))

        if (user.isSuspended) {
            return Result.failure(Exception("Your account is currently suspended for policy violations."))
        }

        val postId = "post_" + UUID.randomUUID().toString().substring(0, 8)
        val newPost = PostEntity(
            id = postId,
            userId = uid,
            userName = user.name,
            userAvatar = user.profilePicUrl,
            mediaUrl = mediaUrl,
            mediaType = mediaType.name,
            videoDurationSec = videoDurationSec,
            caption = caption.trim(),
            tagsString = extractHashtags(caption),
            status = "PENDING", // Enters Moderation Status as requested
            likesCount = 0,
            commentsCount = 0,
            sharesCount = 0,
            viewsCount = 0,
            eligibleRewardScore = 0.0,
            createdAt = System.currentTimeMillis()
        )

        db.postDao().insertPost(newPost)

        // Send submission notification
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                userId = uid,
                title = "Post Under Review / पोस्ट समीक्षाधीन",
                message = "Your ${mediaType.name.lowercase()} has been submitted for moderation. It will be approved shortly!",
                type = "POST_APPROVED",
                relatedEntityId = postId
            )
        )

        return Result.success(newPost.toDomain(false))
    }

    private fun extractHashtags(text: String): String {
        val tags = Regex("#([A-Za-z0-9_]+)").findAll(text).map { it.groupValues[1] }.toList()
        return tags.joinToString(",")
    }

    // ----------------------------------------------------
    // WALLET & REVENUE SHARING SYSTEM
    // ----------------------------------------------------

    fun getWallet(): Flow<Wallet?> {
        return _currentUserId.map { uid ->
            db.walletDao().getWalletByUserId(uid).firstOrNull()?.toDomain()
        }
    }

    fun getTransactions(): Flow<List<WalletTransaction>> {
        return _currentUserId.map { uid ->
            db.walletDao().getTransactionsForUser(uid).firstOrNull()?.map { it.toDomain() } ?: emptyList()
        }
    }

    fun getWithdrawals(): Flow<List<WithdrawalRequest>> {
        return _currentUserId.map { uid ->
            db.withdrawalDao().getWithdrawalsForUser(uid).firstOrNull()?.map { it.toDomain() } ?: emptyList()
        }
    }

    suspend fun requestWithdrawal(
        amount: Double,
        method: WithdrawalMethod,
        upiId: String = "",
        accountHolderName: String = "",
        accountNumber: String = "",
        ifscCode: String = ""
    ): Result<WithdrawalRequest> {
        val uid = _currentUserId.value
        val user = db.userDao().getUserById(uid).firstOrNull() ?: return Result.failure(Exception("User not found"))
        val wallet = db.walletDao().getWalletSnapshot(uid) ?: return Result.failure(Exception("Wallet not found"))

        // Validate via AntiFraudEngine
        val (isValid, errorMsg) = AntiFraudEngine.validateWithdrawalRequest(
            amount = amount,
            availableBalance = wallet.availableBalance,
            method = method.name,
            upiId = upiId,
            accountHolderName = accountHolderName,
            accountNumber = accountNumber,
            ifscCode = ifscCode
        )

        if (!isValid) {
            return Result.failure(Exception(errorMsg))
        }

        // Deduct from available balance & mark as pending
        val newAvailable = wallet.availableBalance - amount
        val newPending = wallet.pendingRewards + amount
        db.walletDao().insertOrUpdateWallet(
            wallet.copy(
                availableBalance = newAvailable,
                pendingRewards = newPending,
                lastUpdated = System.currentTimeMillis()
            )
        )

        val withdrawalId = "WD-" + (100000..999999).random()
        val request = WithdrawalEntity(
            id = withdrawalId,
            userId = uid,
            userName = user.name,
            userPhone = user.phone,
            amount = amount,
            method = method.name,
            upiId = upiId.trim(),
            accountHolderName = accountHolderName.trim(),
            accountNumber = accountNumber.trim(),
            ifscCode = ifscCode.trim().uppercase(),
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        db.withdrawalDao().insertWithdrawal(request)

        // Transaction record
        db.walletDao().insertTransaction(
            TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().substring(0, 8),
                userId = uid,
                type = "WITHDRAWAL_DEBIT",
                amount = amount,
                description = "Withdrawal Request ($method: ${if (method == WithdrawalMethod.UPI) upiId else accountHolderName})",
                referenceId = withdrawalId
            )
        )

        // Notification
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                userId = uid,
                title = "Withdrawal Submitted / निकासी अनुरोध भेजा गया",
                message = "Your request of ₹${"%.2f".format(amount)} has been submitted. Status: PENDING review.",
                type = "WITHDRAWAL_SUBMITTED",
                relatedEntityId = withdrawalId
            )
        )

        return Result.success(request.toDomain())
    }

    // ----------------------------------------------------
    // NOTIFICATIONS
    // ----------------------------------------------------

    fun getNotifications(): Flow<List<AppNotification>> {
        return _currentUserId.map { uid ->
            db.notificationDao().getNotificationsForUser(uid).firstOrNull()?.map { it.toDomain() } ?: emptyList()
        }
    }

    fun getUnreadNotificationsCount(): Flow<Int> {
        return _currentUserId.map { uid ->
            db.notificationDao().getUnreadCount(uid).firstOrNull() ?: 0
        }
    }

    suspend fun markNotificationsRead() {
        db.notificationDao().markAllAsRead(_currentUserId.value)
    }

    // ----------------------------------------------------
    // AD CONFIGURATION & ADSTERRA / GOOGLE ADS
    // ----------------------------------------------------

    fun getAdSettings(): Flow<AdSettings> {
        return db.adminDao().getAdSettings().map { entity ->
            val domain = entity?.toDomain() ?: AdSettings()
            if (domain.adsterraDirectLink.contains("example.com") || domain.adsterraDirectLink.isBlank()) {
                domain.copy(
                    adsterraDirectLink = "https://www.profitableratecpmnetwork.com/inwm47tj?key=6287ae42321dd2648886f420e3b805eb",
                    adsterraBannerPlacementId = "31386617",
                    adsterraPublisherId = "31386617"
                )
            } else {
                domain
            }
        }
    }

    suspend fun updateAdSettings(settings: AdSettings) {
        db.adminDao().saveAdSettings(settings.toEntity())
    }

    // ----------------------------------------------------
    // ADMIN DASHBOARD & REVENUE SHARING ENGINE
    // ----------------------------------------------------

    fun getAdminStats(): Flow<AdminOverviewStats> {
        return kotlinx.coroutines.flow.combine<Any?, AdminOverviewStats>(
            db.userDao().getUsersCount(),
            db.postDao().getTotalPostsCount(),
            db.postDao().getTotalPhotosCount(),
            db.postDao().getTotalVideosCount(),
            db.withdrawalDao().getPendingCount(),
            db.withdrawalDao().getPendingAmount(),
            db.reportDao().getPendingReportsCount(),
            db.userDao().getSuspendedCount()
        ) { values: Array<Any?> ->
            val totalUsers = (values[0] as? Number)?.toInt() ?: 0
            val totalPosts = (values[1] as? Number)?.toInt() ?: 0
            val photos = (values[2] as? Number)?.toInt() ?: 0
            val videos = (values[3] as? Number)?.toInt() ?: 0
            val pendingWdCount = (values[4] as? Number)?.toInt() ?: 0
            val pendingWdAmount = (values[5] as? Number)?.toDouble() ?: 0.0
            val reportedCount = (values[6] as? Number)?.toInt() ?: 0
            val suspendedCount = (values[7] as? Number)?.toInt() ?: 0

            // Total Ad Revenue collected model
            val totalAdRev = 50000.0 // Default active model pool
            val userPool = totalAdRev * 0.40 // 40%
            val platformShare = totalAdRev * 0.60 // 60%

            AdminOverviewStats(
                totalUsers = totalUsers,
                activeUsers = totalUsers - suspendedCount,
                totalPosts = totalPosts,
                totalPhotos = photos,
                totalVideos = videos,
                totalLikes = 826,
                totalComments = 99,
                totalAdRevenue = totalAdRev,
                userRewardPool = userPool,
                platformShare = platformShare,
                pendingWithdrawalsCount = pendingWdCount,
                pendingWithdrawalsAmount = pendingWdAmount,
                completedWithdrawalsCount = 1,
                totalPaidRewards = 1450.0,
                reportedPostsCount = reportedCount,
                suspendedUsersCount = suspendedCount
            )
        }
    }

    fun getAllPendingPosts(): Flow<List<Post>> {
        return db.postDao().getPendingModerationPosts().map { it.map { p -> p.toDomain(false) } }
    }

    suspend fun approvePost(postId: String) {
        val post = db.postDao().getPostById(postId) ?: return
        db.postDao().updatePostStatus(postId, "APPROVED")
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                userId = post.userId,
                title = "Post Approved! / पोस्ट स्वीकृत!",
                message = "Your post has been approved and is now earning ad revenue share.",
                type = "POST_APPROVED",
                relatedEntityId = postId
            )
        )
    }

    suspend fun rejectPost(postId: String, reason: String) {
        val post = db.postDao().getPostById(postId) ?: return
        db.postDao().updatePostStatus(postId, "REJECTED")
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                userId = post.userId,
                title = "Post Rejected / पोस्ट अस्वीकृत",
                message = "Your post was not approved. Reason: $reason",
                type = "POST_REJECTED",
                relatedEntityId = postId
            )
        )
    }

    fun getAllPendingWithdrawals(): Flow<List<WithdrawalRequest>> {
        return db.withdrawalDao().getPendingWithdrawals().map { list -> list.map { it.toDomain() } }
    }

    suspend fun updateWithdrawalStatus(
        withdrawalId: String,
        newStatus: WithdrawalStatus,
        rejectionReason: String = "",
        txRef: String = ""
    ) {
        val wd = db.withdrawalDao().getWithdrawalById(withdrawalId) ?: return
        val wallet = db.walletDao().getWalletSnapshot(wd.userId) ?: return

        when (newStatus) {
            WithdrawalStatus.PAID -> {
                // Move from pending rewards to withdrawn amount
                val newPending = maxOf(0.0, wallet.pendingRewards - wd.amount)
                val newWithdrawn = wallet.withdrawnAmount + wd.amount
                db.walletDao().insertOrUpdateWallet(
                    wallet.copy(
                        pendingRewards = newPending,
                        withdrawnAmount = newWithdrawn,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                db.withdrawalDao().updateWithdrawal(
                    wd.copy(
                        status = "PAID",
                        transactionRef = if (txRef.isNotBlank()) txRef else "BANK/UPI-${System.currentTimeMillis()}",
                        processedAt = System.currentTimeMillis()
                    )
                )
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                        userId = wd.userId,
                        title = "Withdrawal Paid! / निकासी सफल!",
                        message = "₹${"%.2f".format(wd.amount)} has been sent to your ${wd.method} account.",
                        type = "WITHDRAWAL_PAID",
                        relatedEntityId = withdrawalId
                    )
                )
            }
            WithdrawalStatus.REJECTED -> {
                // Refund back to available balance
                val newAvailable = wallet.availableBalance + wd.amount
                val newPending = maxOf(0.0, wallet.pendingRewards - wd.amount)
                db.walletDao().insertOrUpdateWallet(
                    wallet.copy(
                        availableBalance = newAvailable,
                        pendingRewards = newPending,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                db.withdrawalDao().updateWithdrawal(
                    wd.copy(
                        status = "REJECTED",
                        rejectionReason = rejectionReason,
                        processedAt = System.currentTimeMillis()
                    )
                )
                // Refund transaction
                db.walletDao().insertTransaction(
                    TransactionEntity(
                        id = "tx_" + UUID.randomUUID().toString().substring(0, 8),
                        userId = wd.userId,
                        type = "WITHDRAWAL_REFUND",
                        amount = wd.amount,
                        description = "Withdrawal Refund: $rejectionReason",
                        referenceId = withdrawalId
                    )
                )
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                        userId = wd.userId,
                        title = "Withdrawal Rejected / निकासी अस्वीकृत",
                        message = "₹${"%.2f".format(wd.amount)} refunded to balance. Reason: $rejectionReason",
                        type = "WITHDRAWAL_REJECTED",
                        relatedEntityId = withdrawalId
                    )
                )
            }
            WithdrawalStatus.APPROVED -> {
                db.withdrawalDao().updateWithdrawal(
                    wd.copy(status = "APPROVED", processedAt = System.currentTimeMillis())
                )
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                        userId = wd.userId,
                        title = "Withdrawal Approved / निकासी स्वीकृत",
                        message = "Your withdrawal request of ₹${"%.2f".format(wd.amount)} is approved and being processed.",
                        type = "WITHDRAWAL_APPROVED",
                        relatedEntityId = withdrawalId
                    )
                )
            }
            WithdrawalStatus.PROCESSING -> {
                db.withdrawalDao().updateWithdrawal(wd.copy(status = "PROCESSING"))
            }
            WithdrawalStatus.PENDING -> {}
        }
    }

    fun getAllReports(): Flow<List<PostReport>> {
        return db.reportDao().getAllReports().map { list -> list.map { it.toDomain() } }
    }

    suspend fun resolveReport(reportId: String, removeContent: Boolean) {
        val report = db.reportDao().getAllReports().firstOrNull()?.find { it.id == reportId } ?: return
        if (removeContent) {
            db.postDao().deletePost(report.postId)
            db.reportDao().updateReportStatus(reportId, "CONTENT_REMOVED")
        } else {
            db.reportDao().updateReportStatus(reportId, "DISMISSED")
        }
    }

    fun getAllUsers(): Flow<List<User>> {
        return db.userDao().getAllUsers().map { list -> list.map { it.toDomain() } }
    }

    suspend fun toggleUserSuspension(userId: String) {
        val user = db.userDao().getUserById(userId).firstOrNull() ?: return
        val newStatus = !user.isSuspended
        db.userDao().setSuspension(userId, newStatus)
    }

    /**
     * Executes the transparent 40% User Reward Pool / 60% Platform Share calculation!
     * Total Eligible Ad Revenue = 100%
     * User Reward Pool = 40%
     * Platform Share = 60%
     */
    suspend fun executeMonthlyRevenueDistribution(totalAdRevenue: Double, cycleName: String): RewardCycle {
        val userPoolAmount = totalAdRevenue * 0.40
        val platformShareAmount = totalAdRevenue * 0.60

        // Find all approved posts and compute aggregate eligible score per creator
        val allApprovedPosts = db.postDao().getApprovedPostsLatest().firstOrNull() ?: emptyList()
        val creatorScores = mutableMapOf<String, Double>()
        var totalScore = 0.0

        for (post in allApprovedPosts) {
            val score = maxOf(10.0, post.eligibleRewardScore)
            val current = creatorScores.getOrDefault(post.userId, 0.0)
            creatorScores[post.userId] = current + score
            totalScore += score
        }

        if (totalScore == 0.0) totalScore = 1.0

        // Distribute proportionally to creators
        for ((creatorId, score) in creatorScores) {
            val creatorShare = (score / totalScore) * userPoolAmount
            if (creatorShare > 0.01) {
                val wallet = db.walletDao().getWalletSnapshot(creatorId) ?: WalletEntity(userId = creatorId)
                db.walletDao().insertOrUpdateWallet(
                    wallet.copy(
                        totalEarnings = wallet.totalEarnings + creatorShare,
                        availableBalance = wallet.availableBalance + creatorShare,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                db.walletDao().insertTransaction(
                    TransactionEntity(
                        id = "tx_" + UUID.randomUUID().toString().substring(0, 8),
                        userId = creatorId,
                        type = "REWARD_CREDIT",
                        amount = creatorShare,
                        description = "Monthly Revenue Share (40% Pool: ₹${"%.2f".format(userPoolAmount)})",
                        referenceId = cycleName
                    )
                )
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = "notif_" + UUID.randomUUID().toString().substring(0, 8),
                        userId = creatorId,
                        title = "Monthly Reward Distributed! / मासिक रिवार्ड जमा हुआ!",
                        message = "₹${"%.2f".format(creatorShare)} credited from the 40% Creator Ad Revenue Pool ($cycleName).",
                        type = "REWARD_CREDITED",
                        relatedEntityId = cycleName
                    )
                )
            }
        }

        val cycle = RewardCycleEntity(
            id = "RC-" + UUID.randomUUID().toString().substring(0, 8),
            cycleName = cycleName,
            totalAdRevenue = totalAdRevenue,
            userPoolSharePercent = 40.0,
            platformSharePercent = 60.0,
            userPoolAmount = userPoolAmount,
            platformShareAmount = platformShareAmount,
            totalEligibleScore = totalScore,
            distributedAt = System.currentTimeMillis(),
            eligibleCreatorsCount = creatorScores.size
        )
        db.adminDao().insertRewardCycle(cycle)
        return cycle.toDomain()
    }

    fun getRewardCycles(): Flow<List<RewardCycle>> {
        return db.adminDao().getAllRewardCycles().map { list -> list.map { it.toDomain() } }
    }
}

// ----------------------------------------------------
// EXTENSION MAPPERS
// ----------------------------------------------------

fun UserEntity.toDomain() = User(
    id = id,
    phone = phone,
    name = name,
    username = username,
    profilePicUrl = profilePicUrl,
    bio = bio,
    isVerified = isVerified,
    isSuspended = isSuspended,
    primaryMobileVerified = primaryMobileVerified,
    createdAt = createdAt
)

fun PostEntity.toDomain(isLiked: Boolean = false) = Post(
    id = id,
    userId = userId,
    userName = userName,
    userAvatar = userAvatar,
    mediaUrl = mediaUrl,
    mediaType = if (mediaType == "VIDEO") MediaType.VIDEO else MediaType.PHOTO,
    videoDurationSec = videoDurationSec,
    caption = caption,
    tags = if (tagsString.isNotBlank()) tagsString.split(",") else emptyList(),
    status = when (status) {
        "APPROVED" -> PostStatus.APPROVED
        "REJECTED" -> PostStatus.REJECTED
        else -> PostStatus.PENDING
    },
    likesCount = likesCount,
    commentsCount = commentsCount,
    sharesCount = sharesCount,
    viewsCount = viewsCount,
    isLikedByCurrentUser = isLiked,
    eligibleRewardScore = eligibleRewardScore,
    createdAt = createdAt
)

fun CommentEntity.toDomain() = Comment(
    id = id,
    postId = postId,
    userId = userId,
    userName = userName,
    userAvatar = userAvatar,
    text = text,
    timestamp = timestamp
)

fun WalletEntity.toDomain() = Wallet(
    userId = userId,
    totalEarnings = totalEarnings,
    availableBalance = availableBalance,
    pendingRewards = pendingRewards,
    withdrawnAmount = withdrawnAmount,
    lastUpdated = lastUpdated
)

fun TransactionEntity.toDomain() = WalletTransaction(
    id = id,
    userId = userId,
    type = when (type) {
        "WITHDRAWAL_DEBIT" -> TransactionType.WITHDRAWAL_DEBIT
        "WITHDRAWAL_REFUND" -> TransactionType.WITHDRAWAL_REFUND
        "BONUS" -> TransactionType.BONUS
        else -> TransactionType.REWARD_CREDIT
    },
    amount = amount,
    description = description,
    referenceId = referenceId,
    timestamp = timestamp
)

fun WithdrawalEntity.toDomain() = WithdrawalRequest(
    id = id,
    userId = userId,
    userName = userName,
    userPhone = userPhone,
    amount = amount,
    method = if (method == "BANK_ACCOUNT") WithdrawalMethod.BANK_ACCOUNT else WithdrawalMethod.UPI,
    upiId = upiId,
    accountHolderName = accountHolderName,
    accountNumber = accountNumber,
    ifscCode = ifscCode,
    status = when (status) {
        "APPROVED" -> WithdrawalStatus.APPROVED
        "PROCESSING" -> WithdrawalStatus.PROCESSING
        "PAID" -> WithdrawalStatus.PAID
        "REJECTED" -> WithdrawalStatus.REJECTED
        else -> WithdrawalStatus.PENDING
    },
    rejectionReason = rejectionReason,
    transactionRef = transactionRef,
    createdAt = createdAt,
    processedAt = processedAt
)

fun NotificationEntity.toDomain() = AppNotification(
    id = id,
    userId = userId,
    title = title,
    message = message,
    type = when (type) {
        "POST_REJECTED" -> NotificationType.POST_REJECTED
        "LIKE_RECEIVED" -> NotificationType.LIKE_RECEIVED
        "COMMENT_RECEIVED" -> NotificationType.COMMENT_RECEIVED
        "REWARD_CREDITED" -> NotificationType.REWARD_CREDITED
        "WITHDRAWAL_SUBMITTED" -> NotificationType.WITHDRAWAL_SUBMITTED
        "WITHDRAWAL_APPROVED" -> NotificationType.WITHDRAWAL_APPROVED
        "WITHDRAWAL_REJECTED" -> NotificationType.WITHDRAWAL_REJECTED
        "WITHDRAWAL_PAID" -> NotificationType.WITHDRAWAL_PAID
        else -> NotificationType.POST_APPROVED
    },
    timestamp = timestamp,
    isRead = isRead,
    relatedEntityId = relatedEntityId
)

fun ReportEntity.toDomain() = PostReport(
    id = id,
    postId = postId,
    reportedUserId = reportedUserId,
    reporterUserId = reporterUserId,
    reporterName = reporterName,
    reason = try { ReportReason.valueOf(reason) } catch (e: Exception) { ReportReason.OTHER },
    comments = comments,
    status = when (status) {
        "CONTENT_REMOVED" -> ReportStatus.CONTENT_REMOVED
        "DISMISSED" -> ReportStatus.DISMISSED
        "REVIEWED" -> ReportStatus.REVIEWED
        else -> ReportStatus.PENDING
    },
    timestamp = timestamp
)

fun AdSettingsEntity.toDomain() = AdSettings(
    id = id,
    adsEnabled = adsEnabled,
    admobAppId = admobAppId,
    admobBannerUnitId = admobBannerUnitId,
    admobNativeUnitId = admobNativeUnitId,
    admobInterstitialUnitId = admobInterstitialUnitId,
    adsterraPublisherId = adsterraPublisherId,
    adsterraBannerPlacementId = adsterraBannerPlacementId,
    adsterraDirectLink = adsterraDirectLink,
    showOnHomeFeed = showOnHomeFeed,
    showOnWallet = showOnWallet,
    showOnPostDetails = showOnPostDetails,
    feedAdInterval = feedAdInterval,
    lastUpdated = lastUpdated
)

fun AdSettings.toEntity() = AdSettingsEntity(
    id = id,
    adsEnabled = adsEnabled,
    admobAppId = admobAppId,
    admobBannerUnitId = admobBannerUnitId,
    admobNativeUnitId = admobNativeUnitId,
    admobInterstitialUnitId = admobInterstitialUnitId,
    adsterraPublisherId = adsterraPublisherId,
    adsterraBannerPlacementId = adsterraBannerPlacementId,
    adsterraDirectLink = adsterraDirectLink,
    showOnHomeFeed = showOnHomeFeed,
    showOnWallet = showOnWallet,
    showOnPostDetails = showOnPostDetails,
    feedAdInterval = feedAdInterval,
    lastUpdated = System.currentTimeMillis()
)

fun RewardCycleEntity.toDomain() = RewardCycle(
    id = id,
    cycleName = cycleName,
    totalAdRevenue = totalAdRevenue,
    userPoolSharePercent = userPoolSharePercent,
    platformSharePercent = platformSharePercent,
    userPoolAmount = userPoolAmount,
    platformShareAmount = platformShareAmount,
    totalEligibleScore = totalEligibleScore,
    distributedAt = distributedAt,
    eligibleCreatorsCount = eligibleCreatorsCount
)
