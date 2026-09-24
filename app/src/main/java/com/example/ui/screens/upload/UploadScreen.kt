package com.example.ui.screens.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaType
import com.example.data.repository.EarnGramRepository
import com.example.util.LocalLanguageState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    repository: EarnGramRepository,
    onUploadSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val langState = LocalLanguageState.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedMediaUri by remember { mutableStateOf<String?>(null) }
    var mediaType by remember { mutableStateOf(MediaType.PHOTO) }
    var caption by remember { mutableStateOf("") }
    var videoDurationSec by remember { mutableIntStateOf(30) }
    var isUploading by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var uploadSuccessBanner by remember { mutableStateOf(false) }

    // Modern Android Photo Picker (zero permission required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
            validationError = null
        }
    }

    // Curated high quality presets for testing & immediate posting
    val samplePhotos = listOf(
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&q=80" to "Portrait Lifestyle",
        "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800&q=80" to "Mountain Explorer",
        "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&q=80" to "Vibrant Concert",
        "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&q=80" to "Wellness Spa"
    )

    val sampleVideos = listOf(
        "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80" to "Northern Lights Time-lapse (24s)",
        "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=800&q=80" to "Nightlife Groove Dance (45s)",
        "https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=800&q=80" to "High Energy Gym Reel (30s)"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = langState.t("Create & Upload", "नया पोस्ट अपलोड करें"),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success alert if just uploaded
            AnimatedVisibility(visible = uploadSuccessBanner) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = langState.t(
                                "Post submitted! Status: UNDER MODERATION. It will be reviewed and published to the feed shortly.",
                                "पोस्ट सबमिट हो गई! स्थिति: समीक्षाधीन। यह जल्द ही स्वीकृत होकर फ़ीड में प्रकाशित होगी।"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Media Type Selector (Photo vs Video)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = (mediaType == MediaType.PHOTO),
                    onClick = { mediaType = MediaType.PHOTO },
                    label = { Text(langState.t("Photo Upload", "फोटो अपलोड")) },
                    leadingIcon = {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("upload_type_photo")
                )

                FilterChip(
                    selected = (mediaType == MediaType.VIDEO),
                    onClick = { mediaType = MediaType.VIDEO },
                    label = { Text(langState.t("Video / Reel", "वीडियो / रील")) },
                    leadingIcon = {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("upload_type_video")
                )
            }

            // Media Preview or Picker Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        // Launch system photo/video picker
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                if (mediaType == MediaType.PHOTO) {
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                } else {
                                    ActivityResultContracts.PickVisualMedia.VideoOnly
                                }
                            )
                        )
                    }
                    .testTag("media_picker_box"),
                contentAlignment = Alignment.Center
            ) {
                if (selectedMediaUri != null) {
                    AsyncImage(
                        model = selectedMediaUri,
                        contentDescription = "Selected media preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay with change button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change media",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (mediaType == MediaType.VIDEO) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VIDEO PREVIEW (${videoDurationSec}s)",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (mediaType == MediaType.PHOTO) Icons.Default.AddPhotoAlternate else Icons.Default.VideoCall,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = langState.t(
                                "Tap to select ${if (mediaType == MediaType.PHOTO) "Photo" else "Video"} from Device",
                                "डिवाइस से ${if (mediaType == MediaType.PHOTO) "फ़ोटो" else "वीडियो"} चुनने के लिए टैप करें"
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = langState.t(
                                "Photo limit: 10MB (.jpg, .png) • Video limit: 50MB (.mp4)",
                                "फ़ोटो सीमा: 10MB (.jpg, .png) • वीडियो सीमा: 50MB (.mp4)"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Presets Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = langState.t("Or pick a ready-to-test sample asset:", "या तैयार सैंपल मीडिया चुनें:"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = if (mediaType == MediaType.PHOTO) samplePhotos else sampleVideos
                    presets.take(3).forEachIndexed { idx, (url, title) ->
                        OutlinedButton(
                            onClick = {
                                selectedMediaUri = url
                                validationError = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Sample ${idx + 1}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Caption Text Field
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text(langState.t("Caption & Hashtags", "कैप्शन और हैशटैग्स")) },
                placeholder = {
                    Text(langState.t("Describe your creative moment... #travel #creators", "अपनी रचनात्मकता का वर्णन करें... #travel #vlog"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_caption_input"),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            // Video Duration slider if Video
            if (mediaType == MediaType.VIDEO) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${langState.t("Video Length", "वीडियो की अवधि")}: $videoDurationSec ${langState.t("seconds", "सेकंड")}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = videoDurationSec.toFloat(),
                        onValueChange = { videoDurationSec = it.toInt() },
                        valueRange = 10f..60f,
                        steps = 5,
                        modifier = Modifier.testTag("video_duration_slider")
                    )
                }
            }

            // Community Guidelines and Anti-Fraud Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = langState.t("Community Guidelines & Safety", "सामुदायिक दिशानिर्देश और सुरक्षा"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = langState.t(
                                "No copyright violations, adult, or illegal content. Fake engagement is detected and disqualified from ad revenue.",
                                "कॉपीराइट उल्लंघन, वयस्क या अवैध सामग्री निषिद्ध है। फर्जी जुड़ाव को विज्ञापन राजस्व से अयोग्य घोषित किया जाता है।"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Error message if any
            validationError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (selectedMediaUri == null) {
                        validationError = langState.t("Please choose a photo or video first.", "कृपया पहले एक फ़ोटो या वीडियो चुनें।")
                        return@Button
                    }
                    if (caption.trim().isEmpty()) {
                        validationError = langState.t("Please add a short caption.", "कृपया एक छोटा कैप्शन लिखें।")
                        return@Button
                    }

                    isUploading = true
                    validationError = null
                    coroutineScope.launch {
                        val result = repository.uploadPost(
                            mediaUrl = selectedMediaUri!!,
                            mediaType = mediaType,
                            caption = caption,
                            videoDurationSec = if (mediaType == MediaType.VIDEO) videoDurationSec else 0
                        )
                        isUploading = false
                        if (result.isSuccess) {
                            uploadSuccessBanner = true
                            selectedMediaUri = null
                            caption = ""
                            onUploadSuccess()
                        } else {
                            validationError = result.exceptionOrNull()?.message ?: "Upload failed"
                        }
                    }
                },
                enabled = !isUploading && selectedMediaUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_upload_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Text(
                            text = langState.t("Submit for Moderation", "समीक्षा हेतु सबमिट करें"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
