package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import com.example.data.HistoryItem
import com.example.data.FavoriteItem
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("Reader") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        // Letter "ব" inside a rounded dark-purple box
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6750A4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ব",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Bangla TTS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1D1B1E)
                            )
                            Text(
                                text = "SMART READER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFDF8F6),
                    titleContentColor = Color(0xFF1D1B1E)
                ),
                actions = {
                    // Profile avatar placeholder on the right
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color(0xFFEADDFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 16.sp,
                            color = Color(0xFF21005D)
                        )
                    }
                    IconButton(
                        onClick = { currentTab = "Settings" },
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "সেটিংস",
                            tint = Color(0xFF6750A4)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3EDF7),
                windowInsets = WindowInsets.navigationBars
            ) {
                val navBarItemColors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF21005D),
                    selectedTextColor = Color(0xFF21005D),
                    indicatorColor = Color(0xFFEADDFF),
                    unselectedIconColor = Color(0xFF49454F),
                    unselectedTextColor = Color(0xFF49454F)
                )

                NavigationBarItem(
                    selected = currentTab == "Reader",
                    onClick = { currentTab = "Reader" },
                    icon = { Icon(Icons.Default.VolumeUp, contentDescription = "পড়ুন") },
                    label = { Text("পড়ুন", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = navBarItemColors,
                    modifier = Modifier.testTag("nav_reader")
                )
                NavigationBarItem(
                    selected = currentTab == "OCR",
                    onClick = { currentTab = "OCR" },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = "স্ক্যান") },
                    label = { Text("স্ক্যান", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = navBarItemColors,
                    modifier = Modifier.testTag("nav_ocr")
                )
                NavigationBarItem(
                    selected = currentTab == "History",
                    onClick = { currentTab = "History" },
                    icon = { Icon(Icons.Default.History, contentDescription = "ইতিহাস") },
                    label = { Text("ইতিহাস", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = navBarItemColors,
                    modifier = Modifier.testTag("nav_history")
                )
                NavigationBarItem(
                    selected = currentTab == "Favorites",
                    onClick = { currentTab = "Favorites" },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "প্রিয়") },
                    label = { Text("প্রিয়", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = navBarItemColors,
                    modifier = Modifier.testTag("nav_favorites")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFDF8F6))
        ) {
            when (currentTab) {
                "Reader" -> ReaderScreen(viewModel = viewModel, onNavigateToOcr = { currentTab = "OCR" })
                "OCR" -> OcrScreen(viewModel = viewModel)
                "History" -> HistoryScreen(viewModel = viewModel)
                "Favorites" -> FavoritesScreen(viewModel = viewModel)
                "Settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ReaderScreen(viewModel: MainViewModel, onNavigateToOcr: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Launchers for importing documents
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importFile(context, it) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header banner (Sleek minimalist style)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEADDFF).copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6750A4).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "স্মার্ট বাংলা ভয়েস রিডার 🎙️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF21005D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "যেকোনো বাংলা লেখা টাইপ করুন, ফাইল ইম্পোর্ট করুন অথবা ক্যামেরা দিয়ে স্ক্যান করে শুনে নিন চমৎকার কণ্ঠে!",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF49454F)
                    )
                }
            }
        }

        // Copy-to-Speak (কপি-টু-স্পিক) Switch Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.copyToSpeakEnabled) Color(0xFFE8DEF8) else Color(0xFFF7F2FA)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (viewModel.copyToSpeakEnabled) Color(0xFF6750A4).copy(alpha = 0.4f) else Color(0xFFE7E0EC)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📋",
                            fontSize = 22.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "কপি-টু-স্পিক (Copy-to-Speak)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1D1B1E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "চালু থাকলে যেকোনো অ্যাপ থেকে লেখা কপি করলেই রিডার তা সাথে সাথে পড়ে শোনাবে!",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                    }
                    androidx.compose.material3.Switch(
                        checked = viewModel.copyToSpeakEnabled,
                        onCheckedChange = { viewModel.copyToSpeakEnabled = it },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF6750A4),
                            uncheckedThumbColor = Color(0xFF49454F),
                            uncheckedTrackColor = Color(0xFFF4EFF4)
                        ),
                        modifier = Modifier.testTag("switch_copy_to_speak")
                    )
                }
            }
        }

        // Text input card (24.dp rounded, white container with shadow)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বাংলা লিখুন বা পেস্ট করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1D1B1E)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Paste Button
                            Button(
                                onClick = {
                                    clipboardManager.getText()?.let {
                                        viewModel.textInput = it.text
                                    }
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_paste"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF4EFF4),
                                    contentColor = Color(0xFF49454F)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                            ) {
                                Text("পেস্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Clear Button
                            Button(
                                onClick = { viewModel.textInput = "" },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_clear"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF4EFF4),
                                    contentColor = Color(0xFF49454F)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                            ) {
                                Text("মুছুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Editor
                    OutlinedTextField(
                        value = viewModel.textInput,
                        onValueChange = { viewModel.textInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp, max = 220.dp)
                            .testTag("input_text"),
                        placeholder = {
                            Text(
                                "এখানে বাংলা লিখতে পারেন অথবা কোনো ফাইল ওপেন করতে পারেন...",
                                fontSize = 14.sp,
                                color = Color(0xFF938F99)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        ),
                        maxLines = 10
                    )

                    // Divider line
                    androidx.compose.material3.HorizontalDivider(
                        color = Color(0xFFF4EFF4),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Spoken character count and favorite action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${viewModel.textInput.length} টি অক্ষর",
                            fontSize = 10.sp,
                            color = Color(0xFF938F99),
                            fontWeight = FontWeight.Bold
                        )

                        // Favorite toggle
                        IconButton(
                            onClick = { viewModel.toggleFavoriteCurrentText() },
                            modifier = Modifier.size(36.dp).testTag("btn_fav")
                        ) {
                            Icon(
                                imageVector = if (viewModel.isCurrentTextFavorite.collectAsStateWithLifecycle().value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "প্রিয় তালিকায় সংরক্ষণ",
                                tint = if (viewModel.isCurrentTextFavorite.collectAsStateWithLifecycle().value) Color(0xFF6750A4) else Color(0xFF938F99),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // AI Assistant Tools row (Translation & Text Enhancer)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DEF8))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🤖 এআই অ্যাসিস্ট্যান্ট (AI Tools)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF6750A4),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Translate Button (Bidirectional)
                        Button(
                            onClick = { viewModel.translate() },
                            enabled = viewModel.textInput.isNotBlank() && !viewModel.isTranslating,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_translate"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6750A4),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE7E0EC),
                                disabledContentColor = Color(0xFF938F99)
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (viewModel.textInput.contains(Regex("[\\u0980-\\u09FF]"))) "বাংলা ➔ English" else "English ➔ বাংলা",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Improve Text Button
                        Button(
                            onClick = { viewModel.improveTextQuality() },
                            enabled = viewModel.textInput.isNotBlank() && !viewModel.isTranslating,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_improve"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE8DEF8),
                                contentColor = Color(0xFF1D192B),
                                disabledContainerColor = Color(0xFFE7E0EC),
                                disabledContentColor = Color(0xFF938F99)
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("লেখা উন্নত করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Loader / Error Indicator
                    if (viewModel.isTranslating) {
                        Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF6750A4)
                        )
                    }

                    if (viewModel.translationErrorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.translationErrorMessage,
                            fontSize = 11.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Translation Result Display Card
        if (viewModel.translatedTextResult.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF6750A4).copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌐 অনূদিত লেখা (Translation Result)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF6750A4)
                            )
                            IconButton(
                                onClick = { viewModel.clearTranslation() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "বন্ধ করুন",
                                    tint = Color(0xFF49454F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = viewModel.translatedTextResult,
                            fontSize = 14.sp,
                            color = Color(0xFF1D1B1E),
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        androidx.compose.material3.HorizontalDivider(color = Color(0xFFF4EFF4), thickness = 1.dp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Play/TTS Button for translation
                            Button(
                                onClick = {
                                    // Speak translation text
                                    val tempInput = viewModel.textInput
                                    viewModel.textInput = viewModel.translatedTextResult
                                    viewModel.speak()
                                    // Restore original input
                                    viewModel.textInput = tempInput
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("btn_play_translated"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF6750A4),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("শুনুন (Speak)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Swap Button to replace the main input with translation
                            Button(
                                onClick = { viewModel.swapText() },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(38.dp)
                                    .testTag("btn_swap_translation"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF4EFF4),
                                    contentColor = Color(0xFF6750A4)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("মূল ইনপুটে নিন (Swap)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Copy Translation Button
                            Button(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(viewModel.translatedTextResult))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("btn_copy_translated"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF4EFF4),
                                    contentColor = Color(0xFF49454F)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("কপি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Voice selection section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ভয়েস নির্বাচন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1D1B1E),
                    modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Female Chip
                    FilterChip(
                        selected = viewModel.selectedVoiceType == "Female",
                        onClick = { viewModel.selectedVoiceType = "Female" },
                        label = { Text("👩 নারী কণ্ঠ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEADDFF),
                            selectedLabelColor = Color(0xFF21005D),
                            containerColor = Color.White,
                            labelColor = Color(0xFF49454F)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = viewModel.selectedVoiceType == "Female",
                            selectedBorderColor = Color(0xFF6750A4),
                            borderColor = Color(0xFFCAC4D0),
                            selectedBorderWidth = 1.dp,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("voice_female")
                    )

                    // Male Chip
                    FilterChip(
                        selected = viewModel.selectedVoiceType == "Male",
                        onClick = { viewModel.selectedVoiceType = "Male" },
                        label = { Text("👨 পুরুষ কণ্ঠ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEADDFF),
                            selectedLabelColor = Color(0xFF21005D),
                            containerColor = Color.White,
                            labelColor = Color(0xFF49454F)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = viewModel.selectedVoiceType == "Male",
                            selectedBorderColor = Color(0xFF6750A4),
                            borderColor = Color(0xFFCAC4D0),
                            selectedBorderWidth = 1.dp,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("voice_male")
                    )

                    // Child Chip
                    FilterChip(
                        selected = viewModel.selectedVoiceType == "Child",
                        onClick = { viewModel.selectedVoiceType = "Child" },
                        label = { Text("👶 শিশু কণ্ঠ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEADDFF),
                            selectedLabelColor = Color(0xFF21005D),
                            containerColor = Color.White,
                            labelColor = Color(0xFF49454F)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = viewModel.selectedVoiceType == "Child",
                            selectedBorderColor = Color(0xFF6750A4),
                            borderColor = Color(0xFFCAC4D0),
                            selectedBorderWidth = 1.dp,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("voice_child")
                    )
                }
            }
        }

        // Sliders (Pitch & Speed in 2 Columns grid)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Speed Slider Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE7E0EC))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("গতি (Speed)", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", viewModel.speechRate)}x",
                                fontSize = 11.sp,
                                color = Color(0xFF6750A4),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = viewModel.speechRate,
                            onValueChange = { viewModel.speechRate = it },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6750A4),
                                activeTrackColor = Color(0xFF6750A4),
                                inactiveTrackColor = Color(0xFFE7E0EC)
                            ),
                            modifier = Modifier.testTag("slider_speed")
                        )
                    }
                }

                // Pitch Slider Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE7E0EC))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("পিচ (Pitch)", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.SemiBold)
                            Text(
                                text = when {
                                    viewModel.pitch < 0.8f -> "নিচু"
                                    viewModel.pitch > 1.3f -> "উঁচু"
                                    else -> "স্বাভাবিক"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF6750A4),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = viewModel.pitch,
                            onValueChange = { viewModel.pitch = it },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6750A4),
                                activeTrackColor = Color(0xFF6750A4),
                                inactiveTrackColor = Color(0xFFE7E0EC)
                            ),
                            modifier = Modifier.testTag("slider_pitch")
                        )
                    }
                }
            }
        }

        // Action triggers block: File import, play, and OCR scanner launcher
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // File import & audio download side-by-side with Play/Stop button in center
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Action: File Import
                    Button(
                        onClick = { filePickerLauncher.launch("text/*, application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("btn_file_import"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8DEF8),
                            contentColor = Color(0xFF1D192B)
                        )
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ইম্পোর্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Center Action: Play/Stop (Primary FAB)
                    Button(
                        onClick = {
                            if (viewModel.isSpeaking) {
                                viewModel.pauseOrStop()
                            } else {
                                viewModel.speak()
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("btn_play_stop"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6750A4),
                            contentColor = Color.White
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (viewModel.isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (viewModel.isSpeaking) "থামুন" else "ভয়েসে শুনুন",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Right Action: Audio Download
                    Button(
                        onClick = { viewModel.saveAsAudioFile(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("btn_audio_save"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8DEF8),
                            contentColor = Color(0xFF1D192B)
                        )
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ডাউনলোড", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // File Import and Audio Save status display
                AnimatedVisibility(visible = viewModel.fileImportStatus != "Idle") {
                    StatusCard(message = viewModel.fileImportStatus) {
                        viewModel.fileImportStatus = "Idle"
                    }
                }

                AnimatedVisibility(visible = viewModel.audioSaveStatus != "Idle") {
                    StatusCard(message = viewModel.audioSaveStatus) {
                        viewModel.audioSaveStatus = "Idle"
                    }
                }

                // Warning / Device setup info if TTS error exists
                AnimatedVisibility(visible = viewModel.ttsErrorMessage.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(viewModel.ttsErrorMessage, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Clear, contentDescription = "dismiss", modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun OcrScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    // Request CAMERA permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Launchers for Camera capture & Gallery picker
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { viewModel.runOcr(it) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                // Convert Uri to Bitmap
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bitmap?.let { b -> viewModel.runOcr(b) }
            } catch (e: Exception) {
                viewModel.ocrStatus = "ছবি লোড করতে ব্যর্থ: ${e.localizedMessage}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // OCR Hero Illustration
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ক্যামেরা স্ক্যানার (OCR)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "যেকোনো বাংলা বই বা সাইনবোর্ডের ছবি তুলুন অথবা গ্যালারি থেকে ছবি সিলেক্ট করুন। কৃত্রিম বুদ্ধিমত্তা ছবির লেখাগুলোকে স্বয়ংক্রিয়ভাবে টেক্সটে রূপান্তর করবে।",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera Button
            Button(
                onClick = {
                    if (hasCameraPermission) {
                        cameraLauncher.launch()
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("btn_ocr_camera"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ছবি তুলুন", fontWeight = FontWeight.SemiBold)
            }

            // Gallery Button
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("btn_ocr_gallery"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("গ্যালারি", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // OCR Result / Processing State Card
        AnimatedVisibility(visible = viewModel.ocrStatus != "Idle") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "স্ক্যানিং স্ট্যাটাস",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { viewModel.ocrStatus = "Idle" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.ocrStatus,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val historyItems by viewModel.historyList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "আগে পড়া লেখাগুলো (History)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            if (historyItems.isNotEmpty()) {
                Text(
                    text = "সব মুছুন",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { viewModel.clearAllHistory() }
                        .padding(8.dp)
                        .testTag("btn_clear_history")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (historyItems.isEmpty()) {
            EmptyStateView(
                title = "ইতিহাস খালি!",
                description = "আপনার পড়া বাক্য বা টেক্সটগুলো স্বয়ংক্রিয়ভাবে এখানে সংরক্ষিত হয়ে যাবে।",
                icon = Icons.Default.History
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = historyItems) { item ->
                    HistoryItemCard(
                        item = item,
                        onLoad = { viewModel.textInput = item.text },
                        onDelete = { viewModel.deleteHistoryItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryItem,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = remember(item.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLoad),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (item.text.length > 80) "${item.text.take(80)}..." else item.text,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "মুছুন",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FavoritesScreen(viewModel: MainViewModel) {
    val favoritesItems by viewModel.favoritesList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "প্রিয় বাক্যসমূহ (Favorites)",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (favoritesItems.isEmpty()) {
            EmptyStateView(
                title = "কোনো প্রিয় বাক্য নেই!",
                description = "পড়ার স্ক্রিনে থাকা হার্ট বাটনটি চেপে যেকোনো বড় লেখা বা প্রয়োজনীয় বাক্য সংরক্ষণ করে রাখতে পারেন।",
                icon = Icons.Default.Favorite
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = favoritesItems) { item ->
                    FavoriteItemCard(
                        item = item,
                        onLoad = { viewModel.textInput = item.text },
                        onDelete = { viewModel.deleteFavoriteItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteItemCard(
    item: FavoriteItem,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLoad),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.text,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "মুছুন",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "অ্যাপ সেটিংস (Settings)",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "তথ্য ও নির্দেশনা",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "১. এই অ্যাপটি আপনার মোবাইলের ডিফল্ট Google Text-to-Speech ইঞ্জিন ব্যবহার করে কাজ করে।\n" +
                            "২. বাংলা রিডিং পরিষ্কার না শোনা গেলে আপনার ফোন সেটিংস থেকে 'Language & Input' -> 'Text-to-speech output' এ গিয়ে Google Text-to-speech সিলেক্ট করুন এবং বাংলা কণ্ঠস্বর চালু করুন।\n" +
                            "৩. কৃত্রিম বুদ্ধিমত্তা চালিত ক্যামেরা OCR ব্যবহারের জন্য ইন্টারনেট কানেকশন প্রয়োজন।",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // App details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Bangla TTS (বাংলা ভয়েস রিডার)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("ভার্সন: ১.০.০", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("ডেভেলপমেন্ট: AI Studio Build", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
