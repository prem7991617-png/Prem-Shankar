package com.example.data.model

enum class MediaType {
    PHOTO, VIDEO
}

enum class PostStatus {
    PENDING, APPROVED, REJECTED
}

data class User(
    val id: String,
    val phone: String,
    val name: String,
    val username: String,
    val profilePicUrl: String = "",
    val bio: String = "",
    val isVerified: Boolean = true,
    val isSuspended: Boolean = false,
    val primaryMobileVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Post(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val mediaUrl: String,
    val mediaType: MediaType = MediaType.PHOTO,
    val videoDurationSec: Int = 0,
    val caption: String,
    val tags: List<String> = emptyList(),
    val status: PostStatus = PostStatus.APPROVED,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val viewsCount: Int = 0,
    val isLikedByCurrentUser: Boolean = false,
    val eligibleRewardScore: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class LikeRecord(
    val id: String,
    val postId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuspicious: Boolean = false
)

data class Wallet(
    val userId: String,
    val totalEarnings: Double = 0.0,
    val availableBalance: Double = 0.0,
    val pendingRewards: Double = 0.0,
    val withdrawnAmount: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class TransactionType {
    REWARD_CREDIT,
    WITHDRAWAL_DEBIT,
    WITHDRAWAL_REFUND,
    BONUS
}

data class WalletTransaction(
    val id: String,
    val userId: String,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val referenceId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class WithdrawalMethod {
    UPI, BANK_ACCOUNT
}

enum class WithdrawalStatus {
    PENDING, APPROVED, PROCESSING, PAID, REJECTED
}

data class WithdrawalRequest(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val amount: Double,
    val method: WithdrawalMethod,
    val upiId: String = "",
    val accountHolderName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val status: WithdrawalStatus = WithdrawalStatus.PENDING,
    val rejectionReason: String = "",
    val transactionRef: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null
)

data class AdSettings(
    val id: Int = 1,
    val adsEnabled: Boolean = true,
    // Google AdMob Configuration Placeholders
    val admobAppId: String = "ca-app-pub-3940256099942544~3347511713", // Test AdMob ID
    val admobBannerUnitId: String = "ca-app-pub-3940256099942544/6300978111", // Test Banner
    val admobNativeUnitId: String = "ca-app-pub-3940256099942544/2247696110", // Test Native
    val admobInterstitialUnitId: String = "ca-app-pub-3940256099942544/1033173712",
    // Adsterra Configuration
    val adsterraPublisherId: String = "31386617",
    val adsterraBannerPlacementId: String = "31386617",
    val adsterraDirectLink: String = "https://www.profitableratecpmnetwork.com/inwm47tj?key=6287ae42321dd2648886f420e3b805eb",
    // Screen Placements
    val showOnHomeFeed: Boolean = true,
    val showOnWallet: Boolean = true,
    val showOnPostDetails: Boolean = true,
    val feedAdInterval: Int = 4, // Show native ad every 4 posts
    val lastUpdated: Long = System.currentTimeMillis()
)

data class RewardCycle(
    val id: String,
    val cycleName: String, // e.g. "September 2026 Cycle"
    val totalAdRevenue: Double, // e.g. ₹50,000
    val userPoolSharePercent: Double = 40.0, // 40%
    val platformSharePercent: Double = 60.0, // 60%
    val userPoolAmount: Double, // ₹20,000
    val platformShareAmount: Double, // ₹30,000
    val totalEligibleScore: Double,
    val distributedAt: Long = System.currentTimeMillis(),
    val eligibleCreatorsCount: Int = 0
)

enum class NotificationType {
    POST_APPROVED,
    POST_REJECTED,
    LIKE_RECEIVED,
    COMMENT_RECEIVED,
    REWARD_CREDITED,
    WITHDRAWAL_SUBMITTED,
    WITHDRAWAL_APPROVED,
    WITHDRAWAL_REJECTED,
    WITHDRAWAL_PAID
}

data class AppNotification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedEntityId: String = ""
)

enum class ReportReason {
    SPAM_OR_BOT,
    FAKE_ENGAGEMENT,
    UNSAFE_ILLEGAL_CONTENT,
    HARASSMENT,
    COPYRIGHT_VIOLATION,
    OTHER
}

enum class ReportStatus {
    PENDING,
    REVIEWED,
    CONTENT_REMOVED,
    DISMISSED
}

data class PostReport(
    val id: String,
    val postId: String,
    val reportedUserId: String,
    val reporterUserId: String,
    val reporterName: String,
    val reason: ReportReason,
    val comments: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

data class AdminOverviewStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val totalPosts: Int = 0,
    val totalPhotos: Int = 0,
    val totalVideos: Int = 0,
    val totalLikes: Int = 0,
    val totalComments: Int = 0,
    val totalAdRevenue: Double = 0.0,
    val userRewardPool: Double = 0.0,
    val platformShare: Double = 0.0,
    val pendingWithdrawalsCount: Int = 0,
    val pendingWithdrawalsAmount: Double = 0.0,
    val completedWithdrawalsCount: Int = 0,
    val totalPaidRewards: Double = 0.0,
    val reportedPostsCount: Int = 0,
    val suspendedUsersCount: Int = 0
)
