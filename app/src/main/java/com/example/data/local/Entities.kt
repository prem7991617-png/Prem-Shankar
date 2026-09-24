package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MediaType
import com.example.data.model.NotificationType
import com.example.data.model.PostStatus
import com.example.data.model.ReportReason
import com.example.data.model.ReportStatus
import com.example.data.model.TransactionType
import com.example.data.model.WithdrawalMethod
import com.example.data.model.WithdrawalStatus

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val mediaUrl: String,
    val mediaType: String, // PHOTO, VIDEO
    val videoDurationSec: Int = 0,
    val caption: String,
    val tagsString: String = "",
    val status: String, // PENDING, APPROVED, REJECTED
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val viewsCount: Int = 0,
    val eligibleRewardScore: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "likes")
data class LikeEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuspicious: Boolean = false
)

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val userId: String,
    val totalEarnings: Double = 0.0,
    val availableBalance: Double = 0.0,
    val pendingRewards: Double = 0.0,
    val withdrawnAmount: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // REWARD_CREDIT, WITHDRAWAL_DEBIT, WITHDRAWAL_REFUND, BONUS
    val amount: Double,
    val description: String,
    val referenceId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val amount: Double,
    val method: String, // UPI, BANK_ACCOUNT
    val upiId: String = "",
    val accountHolderName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val status: String, // PENDING, APPROVED, PROCESSING, PAID, REJECTED
    val rejectionReason: String = "",
    val transactionRef: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedEntityId: String = ""
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val reportedUserId: String,
    val reporterUserId: String,
    val reporterName: String,
    val reason: String,
    val comments: String = "",
    val status: String, // PENDING, REVIEWED, CONTENT_REMOVED, DISMISSED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ad_settings")
data class AdSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val adsEnabled: Boolean = true,
    val admobAppId: String = "ca-app-pub-3940256099942544~3347511713",
    val admobBannerUnitId: String = "ca-app-pub-3940256099942544/6300978111",
    val admobNativeUnitId: String = "ca-app-pub-3940256099942544/2247696110",
    val admobInterstitialUnitId: String = "ca-app-pub-3940256099942544/1033173712",
    val adsterraPublisherId: String = "31386617",
    val adsterraBannerPlacementId: String = "31386617",
    val adsterraDirectLink: String = "https://www.profitableratecpmnetwork.com/inwm47tj?key=6287ae42321dd2648886f420e3b805eb",
    val showOnHomeFeed: Boolean = true,
    val showOnWallet: Boolean = true,
    val showOnPostDetails: Boolean = true,
    val feedAdInterval: Int = 4,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "reward_cycles")
data class RewardCycleEntity(
    @PrimaryKey val id: String,
    val cycleName: String,
    val totalAdRevenue: Double,
    val userPoolSharePercent: Double = 40.0,
    val platformSharePercent: Double = 60.0,
    val userPoolAmount: Double,
    val platformShareAmount: Double,
    val totalEligibleScore: Double,
    val distributedAt: Long = System.currentTimeMillis(),
    val eligibleCreatorsCount: Int = 0
)
