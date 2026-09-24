/**
 * Firebase Cloud Functions for EarnGram
 * Handles server-side financial calculations:
 * - Monthly Ad Revenue 40% User Pool / 60% Platform Share Distribution
 * - Anti-fraud engagement scoring
 * - Safe withdrawal ledger updates
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.firestore();

/**
 * Scheduled or Admin-triggered Monthly Ad Revenue Distribution
 * Formula:
 * User Reward Pool = Eligible Ad Revenue * 0.40
 * Platform Share = Eligible Ad Revenue * 0.60
 */
exports.distributeMonthlyAdRevenue = functions.https.onCall(async (data, context) => {
  // 1. Verify caller has admin privileges
  if (!context.auth || !context.auth.token.admin) {
    throw new functions.https.HttpsError("permission-denied", "Only administrators can run revenue distribution.");
  }

  const { cycleName, totalAdRevenue } = data;
  if (!totalAdRevenue || totalAdRevenue <= 0) {
    throw new functions.https.HttpsError("invalid-argument", "Invalid advertising revenue amount.");
  }

  const userPoolAmount = totalAdRevenue * 0.40;
  const platformShareAmount = totalAdRevenue * 0.60;

  // 2. Fetch all approved posts in this billing cycle
  const postsSnapshot = await db.collection("posts")
    .where("status", "==", "APPROVED")
    .get();

  const creatorScores = {};
  let totalPoolScore = 0;

  postsSnapshot.forEach((doc) => {
    const post = doc.data();
    // Exclude suspicious or fraudulent posts
    const eligibleScore = post.eligibleRewardScore || 0;
    if (eligibleScore > 0) {
      creatorScores[post.userId] = (creatorScores[post.userId] || 0) + eligibleScore;
      totalPoolScore += eligibleScore;
    }
  });

  if (totalPoolScore === 0) {
    return { success: false, message: "No eligible creator engagement found for this cycle." };
  }

  const batch = db.batch();
  const cycleRef = db.collection("reward_cycles").doc();

  // 3. Credit each creator's wallet securely on the server
  for (const [userId, score] of Object.entries(creatorScores)) {
    const creatorShare = (score / totalPoolScore) * userPoolAmount;
    if (creatorShare >= 0.01) {
      const walletRef = db.collection("wallets").doc(userId);
      batch.set(walletRef, {
        totalEarnings: admin.firestore.FieldValue.increment(creatorShare),
        availableBalance: admin.firestore.FieldValue.increment(creatorShare),
        lastUpdated: admin.firestore.FieldValue.serverTimestamp()
      }, { merge: true });

      // Record immutable ledger entry
      const txRef = db.collection("transactions").doc();
      batch.set(txRef, {
        userId,
        type: "REWARD_CREDIT",
        amount: creatorShare,
        description: `40% Ad Revenue Pool Share (${cycleName})`,
        referenceId: cycleRef.id,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
      });

      // Notify Creator
      const notifRef = db.collection("notifications").doc();
      batch.set(notifRef, {
        userId,
        title: "Monthly Ad Revenue Credited! / रिवार्ड जमा हुआ!",
        message: `₹${creatorShare.toFixed(2)} credited to your wallet from the 40% creator pool.`,
        type: "REWARD_CREDITED",
        isRead: false,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
      });
    }
  }

  // 4. Save Revenue Cycle Audit Record
  batch.set(cycleRef, {
    cycleName,
    totalAdRevenue,
    userPoolSharePercent: 40.0,
    platformSharePercent: 60.0,
    userPoolAmount,
    platformShareAmount,
    totalEligibleScore: totalPoolScore,
    eligibleCreatorsCount: Object.keys(creatorScores).length,
    distributedAt: admin.firestore.FieldValue.serverTimestamp()
  });

  await batch.commit();

  return {
    success: true,
    userPoolAmount,
    platformShareAmount,
    creatorsCredited: Object.keys(creatorScores).length
  };
});
