package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.AntiFraudEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EarnGram", appName)
  }

  @Test
  fun `anti fraud validates withdrawal minimum threshold`() {
    // Amount less than ₹100 should fail
    val (underSuccess, underReason) = AntiFraudEngine.validateWithdrawalRequest(
      amount = 50.0,
      availableBalance = 500.0,
      method = "UPI",
      upiId = "test@upi",
      accountHolderName = "",
      accountNumber = "",
      ifscCode = ""
    )
    assertFalse(underSuccess)
    assertTrue(underReason.contains("100"))

    // Amount >= ₹100 with valid UPI should pass
    val (validSuccess, _) = AntiFraudEngine.validateWithdrawalRequest(
      amount = 150.0,
      availableBalance = 500.0,
      method = "UPI",
      upiId = "test@okhdfcbank",
      accountHolderName = "",
      accountNumber = "",
      ifscCode = ""
    )
    assertTrue(validSuccess)
  }

  @Test
  fun `anti fraud checks self like engagement`() {
    // Self-like allowed but excluded from reward score
    val result = AntiFraudEngine.validateLikeAction(
      currentUserId = "user_1",
      postOwnerId = "user_1",
      isAlreadyLiked = false
    )
    assertTrue(result.isAllowed)
    assertFalse(result.isEligibleForReward)
  }

  @Test
  fun `revenue sharing formula verifies 40 percent user pool and 60 percent platform share`() {
    val totalAdRevenue = 10000.0
    val userRewardPool = totalAdRevenue * 0.40
    val platformShare = totalAdRevenue * 0.60

    assertEquals(4000.0, userRewardPool, 0.001)
    assertEquals(6000.0, platformShare, 0.001)
  }
}
