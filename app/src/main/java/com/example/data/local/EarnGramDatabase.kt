package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        CommentEntity::class,
        LikeEntity::class,
        WalletEntity::class,
        TransactionEntity::class,
        WithdrawalEntity::class,
        NotificationEntity::class,
        ReportEntity::class,
        AdSettingsEntity::class,
        RewardCycleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EarnGramDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun likeDao(): LikeDao
    abstract fun walletDao(): WalletDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reportDao(): ReportDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: EarnGramDatabase? = null

        fun getDatabase(context: Context): EarnGramDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EarnGramDatabase::class.java,
                    "earngram_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: EarnGramDatabase) {
            // Default primary user (Logged in creator)
            val currentUserId = "user_primary_101"
            val currentUser = UserEntity(
                id = currentUserId,
                phone = "+91 98765 43210",
                name = "Aman Sharma",
                username = "amansharma_creates",
                profilePicUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80",
                bio = "Tech & Travel Creator | Creating daily shorts & visuals | Monetizing with EarnGram",
                isVerified = true,
                isSuspended = false,
                primaryMobileVerified = true,
                createdAt = System.currentTimeMillis() - 86400000L * 30
            )
            db.userDao().insertUser(currentUser)

            // Other creators
            val creator2 = UserEntity(
                id = "user_creator_202",
                phone = "+91 91234 56789",
                name = "Priya Verma",
                username = "priya_travels",
                profilePicUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&q=80",
                bio = "Explorer & Photographer. Sharing the beauty of India.",
                isVerified = true,
                isSuspended = false,
                primaryMobileVerified = true
            )
            val creator3 = UserEntity(
                id = "user_creator_303",
                phone = "+91 99887 76655",
                name = "Rohit Dance Studio",
                username = "rohit_moves",
                profilePicUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&q=80",
                bio = "Dance Choreographer & Fitness Reel Creator",
                isVerified = true,
                isSuspended = false,
                primaryMobileVerified = true
            )
            db.userDao().insertUser(creator2)
            db.userDao().insertUser(creator3)

            // Initial Posts (Photos & Videos)
            val post1 = PostEntity(
                id = "post_101",
                userId = "user_creator_202",
                userName = "Priya Verma",
                userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&q=80",
                mediaType = "PHOTO",
                caption = "Sunset at the valley of flowers! Nature's golden hour never fails to inspire. What do you think? #nature #travelindia #wanderlust",
                tagsString = "nature,travel,photography",
                status = "APPROVED",
                likesCount = 124,
                commentsCount = 18,
                sharesCount = 14,
                viewsCount = 1420,
                eligibleRewardScore = 286.0,
                createdAt = System.currentTimeMillis() - 3600000L * 4
            )

            val post2 = PostEntity(
                id = "post_102",
                userId = "user_creator_303",
                userName = "Rohit Dance Studio",
                userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=800&q=80",
                mediaType = "VIDEO",
                videoDurationSec = 45,
                caption = "New dance choreography to viral beats! Turn sound ON and groove with us! Drop a comment if you want the full tutorial! #dance #reels #groove",
                tagsString = "dance,fitness,choreography",
                status = "APPROVED",
                likesCount = 389,
                commentsCount = 42,
                sharesCount = 89,
                viewsCount = 4850,
                eligibleRewardScore = 912.0,
                createdAt = System.currentTimeMillis() - 3600000L * 8
            )

            val post3 = PostEntity(
                id = "post_103",
                userId = currentUserId,
                userName = "Aman Sharma",
                userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?w=800&q=80",
                mediaType = "PHOTO",
                caption = "My new creative studio setup complete! Monitored audio + dual screen workflow for editing 4K reels. #contentcreator #workspace #tech",
                tagsString = "tech,creator,studio",
                status = "APPROVED",
                likesCount = 98,
                commentsCount = 12,
                sharesCount = 8,
                viewsCount = 1120,
                eligibleRewardScore = 224.0,
                createdAt = System.currentTimeMillis() - 3600000L * 14
            )

            val post4 = PostEntity(
                id = "post_104",
                userId = "user_creator_202",
                userName = "Priya Verma",
                userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=80",
                mediaType = "VIDEO",
                videoDurationSec = 30,
                caption = "Morning sea waves at Gokarna beach! Pure serenity. Listening to nature heals the soul. #beach #peace #karnataka",
                tagsString = "travel,beach,peace",
                status = "APPROVED",
                likesCount = 215,
                commentsCount = 27,
                sharesCount = 32,
                viewsCount = 2600,
                eligibleRewardScore = 520.0,
                createdAt = System.currentTimeMillis() - 3600000L * 22
            )

            db.postDao().insertPost(post1)
            db.postDao().insertPost(post2)
            db.postDao().insertPost(post3)
            db.postDao().insertPost(post4)

            // Initial Comments
            db.commentDao().insertComment(
                CommentEntity(
                    id = "c_1",
                    postId = "post_101",
                    userId = currentUserId,
                    userName = "Aman Sharma",
                    userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80",
                    text = "Breathtaking shot! Which camera did you use?",
                    timestamp = System.currentTimeMillis() - 3600000L * 3
                )
            )
            db.commentDao().insertComment(
                CommentEntity(
                    id = "c_2",
                    postId = "post_101",
                    userId = "user_creator_202",
                    userName = "Priya Verma",
                    userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&q=80",
                    text = "Thanks Aman! Shot on Sony A7IV with 24-70mm lens!",
                    timestamp = System.currentTimeMillis() - 3600000L * 2
                )
            )

            // Initial Wallet for current user (Total earnings: ₹1,450.00, Available: ₹650.00, Pending: ₹150.00, Withdrawn: ₹650.00)
            val wallet = WalletEntity(
                userId = currentUserId,
                totalEarnings = 1450.0,
                availableBalance = 650.0,
                pendingRewards = 150.0,
                withdrawnAmount = 650.0,
                lastUpdated = System.currentTimeMillis()
            )
            db.walletDao().insertOrUpdateWallet(wallet)

            // Initial Transactions
            db.walletDao().insertTransaction(
                TransactionEntity(
                    id = "tx_01",
                    userId = currentUserId,
                    type = "REWARD_CREDIT",
                    amount = 450.0,
                    description = "Monthly Ad Revenue Share (40% Pool Distribution - August)",
                    referenceId = "RC-2026-08",
                    timestamp = System.currentTimeMillis() - 86400000L * 20
                )
            )
            db.walletDao().insertTransaction(
                TransactionEntity(
                    id = "tx_02",
                    userId = currentUserId,
                    type = "WITHDRAWAL_DEBIT",
                    amount = 650.0,
                    description = "Withdrawal to UPI ID (amansharma@okaxis)",
                    referenceId = "WD-847291",
                    timestamp = System.currentTimeMillis() - 86400000L * 10
                )
            )
            db.walletDao().insertTransaction(
                TransactionEntity(
                    id = "tx_03",
                    userId = currentUserId,
                    type = "REWARD_CREDIT",
                    amount = 1000.0,
                    description = "Eligible Creator Engagement Share (High-quality Video/Photo)",
                    referenceId = "ENG-9021",
                    timestamp = System.currentTimeMillis() - 86400000L * 2
                )
            )

            // Initial Withdrawal History
            db.withdrawalDao().insertWithdrawal(
                WithdrawalEntity(
                    id = "WD-847291",
                    userId = currentUserId,
                    userName = "Aman Sharma",
                    userPhone = "+91 98765 43210",
                    amount = 650.0,
                    method = "UPI",
                    upiId = "amansharma@okaxis",
                    status = "PAID",
                    transactionRef = "UPI/260902381920/AXIS",
                    createdAt = System.currentTimeMillis() - 86400000L * 10,
                    processedAt = System.currentTimeMillis() - 86400000L * 9
                )
            )
            db.withdrawalDao().insertWithdrawal(
                WithdrawalEntity(
                    id = "WD-994102",
                    userId = "user_creator_202",
                    userName = "Priya Verma",
                    userPhone = "+91 91234 56789",
                    amount = 1200.0,
                    method = "BANK_ACCOUNT",
                    accountHolderName = "Priya Verma",
                    accountNumber = "918237465012",
                    ifscCode = "HDFC0001234",
                    status = "PENDING",
                    createdAt = System.currentTimeMillis() - 3600000L * 5
                )
            )

            // Default Ad Settings
            db.adminDao().saveAdSettings(
                AdSettingsEntity(
                    id = 1,
                    adsEnabled = true,
                    admobAppId = "ca-app-pub-3940256099942544~3347511713",
                    admobBannerUnitId = "ca-app-pub-3940256099942544/6300978111",
                    admobNativeUnitId = "ca-app-pub-3940256099942544/2247696110",
                    admobInterstitialUnitId = "ca-app-pub-3940256099942544/1033173712",
                    adsterraPublisherId = "31386617",
                    adsterraBannerPlacementId = "31386617",
                    adsterraDirectLink = "https://www.profitableratecpmnetwork.com/inwm47tj?key=6287ae42321dd2648886f420e3b805eb",
                    showOnHomeFeed = true,
                    showOnWallet = true,
                    showOnPostDetails = true,
                    feedAdInterval = 3
                )
            )

            // Default Reward Cycles history (40% Pool vs 60% Platform Share)
            db.adminDao().insertRewardCycle(
                RewardCycleEntity(
                    id = "RC-2026-08",
                    cycleName = "August 2026 Revenue Pool",
                    totalAdRevenue = 50000.0,
                    userPoolSharePercent = 40.0,
                    platformSharePercent = 60.0,
                    userPoolAmount = 20000.0,
                    platformShareAmount = 30000.0,
                    totalEligibleScore = 14200.0,
                    distributedAt = System.currentTimeMillis() - 86400000L * 20,
                    eligibleCreatorsCount = 142
                )
            )

            // Notifications
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_1",
                    userId = currentUserId,
                    title = "Reward Credited! / रिवार्ड जमा हुआ!",
                    message = "₹1,000.00 credited to your wallet for high eligible post engagement. Keep creating!",
                    type = "REWARD_CREDITED",
                    timestamp = System.currentTimeMillis() - 86400000L * 2,
                    isRead = false,
                    relatedEntityId = "ENG-9021"
                )
            )
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_2",
                    userId = currentUserId,
                    title = "Withdrawal Paid / निकासी सफल",
                    message = "Your withdrawal request of ₹650.00 via UPI (amansharma@okaxis) has been paid successfully.",
                    type = "WITHDRAWAL_PAID",
                    timestamp = System.currentTimeMillis() - 86400000L * 9,
                    isRead = true,
                    relatedEntityId = "WD-847291"
                )
            )
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_3",
                    userId = currentUserId,
                    title = "Post Approved / पोस्ट स्वीकृत",
                    message = "Your photo 'My new creative studio setup...' passed moderation and is now live on the feed!",
                    type = "POST_APPROVED",
                    timestamp = System.currentTimeMillis() - 3600000L * 13,
                    isRead = true,
                    relatedEntityId = "post_103"
                )
            )
        }
    }
}
