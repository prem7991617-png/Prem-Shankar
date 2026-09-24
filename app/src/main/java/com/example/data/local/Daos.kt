package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isSuspended = :isSuspended WHERE id = :userId")
    suspend fun setSuspension(userId: String, isSuspended: Boolean)

    @Query("SELECT COUNT(*) FROM users")
    fun getUsersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE isSuspended = 1")
    fun getSuspendedCount(): Flow<Int>
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE status = 'APPROVED' ORDER BY createdAt DESC")
    fun getApprovedPostsLatest(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE status = 'APPROVED' ORDER BY (likesCount * 2 + commentsCount * 3 + viewsCount) DESC")
    fun getApprovedPostsPopular(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUserId(userId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingModerationPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: String): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("UPDATE posts SET status = :status WHERE id = :postId")
    suspend fun updatePostStatus(postId: String, status: String)

    @Query("UPDATE posts SET likesCount = likesCount + :delta WHERE id = :postId")
    suspend fun updateLikesCount(postId: String, delta: Int)

    @Query("UPDATE posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentsCount(postId: String)

    @Query("UPDATE posts SET viewsCount = viewsCount + 1 WHERE id = :postId")
    suspend fun incrementViewsCount(postId: String)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("SELECT COUNT(*) FROM posts")
    fun getTotalPostsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE mediaType = 'PHOTO'")
    fun getTotalPhotosCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE mediaType = 'VIDEO'")
    fun getTotalVideosCount(): Flow<Int>

    @Query("SELECT SUM(likesCount) FROM posts")
    fun getTotalLikesCount(): Flow<Int?>

    @Query("SELECT SUM(commentsCount) FROM posts")
    fun getTotalCommentsCount(): Flow<Int?>
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)
}

@Dao
interface LikeDao {
    @Query("SELECT * FROM likes WHERE postId = :postId AND userId = :userId LIMIT 1")
    suspend fun getLike(postId: String, userId: String): LikeEntity?

    @Query("SELECT postId FROM likes WHERE userId = :userId")
    fun getUserLikedPostIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    @Query("DELETE FROM likes WHERE postId = :postId AND userId = :userId")
    suspend fun deleteLike(postId: String, userId: String)

    @Query("SELECT COUNT(*) FROM likes WHERE userId = :userId AND timestamp > :recentThreshold")
    suspend fun getRecentLikesCount(userId: String, recentThreshold: Long): Int
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE userId = :userId")
    fun getWalletByUserId(userId: String): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun getWalletSnapshot(userId: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWallet(wallet: WalletEntity)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'REWARD_CREDIT'")
    fun getTotalPaidRewards(): Flow<Double?>
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWithdrawalsForUser(userId: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY createdAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE id = :id")
    suspend fun getWithdrawalById(id: String): WithdrawalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT SUM(amount) FROM withdrawals WHERE status = 'PENDING'")
    fun getPendingAmount(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'PAID'")
    fun getPaidCount(): Flow<Int>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)

    @Query("SELECT COUNT(*) FROM reports WHERE status = 'PENDING'")
    fun getPendingReportsCount(): Flow<Int>
}

@Dao
interface AdminDao {
    @Query("SELECT * FROM ad_settings WHERE id = 1")
    fun getAdSettings(): Flow<AdSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdSettings(settings: AdSettingsEntity)

    @Query("SELECT * FROM reward_cycles ORDER BY distributedAt DESC")
    fun getAllRewardCycles(): Flow<List<RewardCycleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardCycle(cycle: RewardCycleEntity)
}
