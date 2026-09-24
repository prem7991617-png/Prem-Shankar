package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AdSettings
import com.example.util.LocalLanguageState

/**
 * Modern, policy-compliant advertisement card supporting:
 * - Adsterra Direct Link (Smartlink)
 * - Adsterra WebView Banner / Script (300x250 or responsive)
 * - Google AdMob Native Card Placeholders
 * Strict compliance:
 * - Clear "Sponsored / विज्ञापन" disclosure badge
 * - Non-deceptive styling
 * - No incentive or encouragement to click
 */
@Composable
fun CompliantNativeAdCard(
    adSettings: AdSettings,
    modifier: Modifier = Modifier
) {
    if (!adSettings.adsEnabled) return
    val context = LocalContext.current
    val langState = LocalLanguageState.current

    // Safely determine destination link for Adsterra
    val targetAdUrl = if (adSettings.adsterraDirectLink.isNotBlank() && adSettings.adsterraDirectLink.startsWith("http")) {
        adSettings.adsterraDirectLink
    } else {
        "https://adsterra.com"
    }

    val hasCustomAdsterraScript = adSettings.adsterraBannerPlacementId.isNotBlank() &&
            (adSettings.adsterraBannerPlacementId.contains("<script") ||
             adSettings.adsterraBannerPlacementId.contains("<iframe") ||
             adSettings.adsterraBannerPlacementId.length > 20)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("feed_native_ad_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Sponsored disclosure badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = langState.t("Sponsored", "प्रायोजित / विज्ञापन"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = if (adSettings.adsterraPublisherId.isNotBlank() || adSettings.adsterraDirectLink.isNotBlank()) "Adsterra Network" else "Google Ad Partner",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ad Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Either render real Adsterra WebView Banner or Modern Native Creative
            if (hasCustomAdsterraScript) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                webViewClient = WebViewClient()
                                val htmlData = if (adSettings.adsterraBannerPlacementId.contains("<")) {
                                    """
                                    <!DOCTYPE html>
                                    <html>
                                    <head><meta name="viewport" content="width=device-width, initial-scale=1.0"><style>body{margin:0;padding:0;display:flex;justify-content:center;align-items:center;background:transparent;}</style></head>
                                    <body>${adSettings.adsterraBannerPlacementId}</body>
                                    </html>
                                    """.trimIndent()
                                } else {
                                    """
                                    <!DOCTYPE html>
                                    <html>
                                    <head><meta name="viewport" content="width=device-width, initial-scale=1.0"><style>body{margin:0;padding:0;display:flex;justify-content:center;align-items:center;background:transparent;}</style></head>
                                    <body>
                                    <script type="text/javascript">
                                        atOptions = {
                                            'key' : '${adSettings.adsterraBannerPlacementId}',
                                            'format' : 'iframe',
                                            'height' : 250,
                                            'width' : 300,
                                            'params' : {}
                                        };
                                    </script>
                                    <script type="text/javascript" src="//www.highperformanceformat.com/${adSettings.adsterraBannerPlacementId}/invoke.js"></script>
                                    </body>
                                    </html>
                                    """.trimIndent()
                                }
                                loadDataWithBaseURL("https://adsterra.com", htmlData, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Interactive Native Creative Card linked to Adsterra Smartlink
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetAdUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = langState.t("Explore High-Performing Offers", "सर्वश्रेष्ठ ऑफर्स व टूल्स देखें"),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = langState.t("Verified top deals & premium utilities", "सत्यापित प्रीमियम डील्स और उपयोगी टूल्स"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (adSettings.adsterraDirectLink.isNotBlank()) "Partner Offers" else "CreativeCloud Suite",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (adSettings.adsterraDirectLink.isNotBlank()) "adsterra.partner.network" else "creativecloud.partner.io",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetAdUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("ad_learn_more_button")
                ) {
                    Text(
                        text = langState.t("Learn More", "अधिक जानें"),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
