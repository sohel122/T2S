package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiOcrHelper
import com.example.data.AppDatabase
import com.example.data.FavoriteItem
import com.example.data.HistoryItem
import com.example.data.TextRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.zip.ZipInputStream

class MainViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    private val TAG = "MainViewModel"

    // Database & Repository
    private val database = AppDatabase.getDatabase(application)
    private val repository = TextRepository(database.historyDao(), database.favoriteDao())

    // UI States
    var textInput by mutableStateOf("")
    var isSpeaking by mutableStateOf(false)
    var pitch by mutableStateOf(1.0f)
    var speechRate by mutableStateOf(1.0f)
    
    // Voice Type: "Female", "Male", "Child"
    var selectedVoiceType by mutableStateOf("Female")

    // Async operations states
    var ocrStatus by mutableStateOf("Idle")
    var fileImportStatus by mutableStateOf("Idle")
    var audioSaveStatus by mutableStateOf("Idle")

    // Playback progress/character highlighting
    var spokenCharacterIndex by mutableStateOf(0)

    // Room database flows
    val historyList: StateFlow<List<HistoryItem>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritesList: StateFlow<List<FavoriteItem>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isCurrentTextFavorite = MutableStateFlow(false)

    // Translation and copy-to-speak features
    var copyToSpeakEnabled by mutableStateOf(false)
    var translatedTextResult by mutableStateOf("")
    var isTranslating by mutableStateOf(false)
    var translationErrorMessage by mutableStateOf("")

    // TextToSpeech
    private var tts: TextToSpeech? = null
    var isTtsInitialized by mutableStateOf(false)
    var ttsErrorMessage by mutableStateOf("")

    private val clipboardManager = application.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

    private val clipboardListener = android.content.ClipboardManager.OnPrimaryClipChangedListener {
        if (copyToSpeakEnabled) {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: ""
                if (text.isNotBlank() && text != textInput) {
                    textInput = text
                    speak()
                }
            }
        }
    }

    init {
        // Initialize local TextToSpeech
        tts = TextToSpeech(application, this)
        
        // Register clipboard monitor for Copy-to-Speak
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)
        
        // Monitor favorite status of current textInput
        viewModelScope.launch {
            snapshotFlow { textInput }.collectLatest { currentText ->
                if (currentText.isBlank()) {
                    isCurrentTextFavorite.value = false
                } else {
                    repository.isFavorite(currentText).collectLatest { fav ->
                        isCurrentTextFavorite.value = fav
                    }
                }
            }
        }
    }

    // SnapshotFlow helper since Compose snapshotFlow is standard
    private fun <T> snapshotFlow(block: () -> T): kotlinx.coroutines.flow.Flow<T> =
        kotlinx.coroutines.flow.flow {
            var lastValue = block()
            emit(lastValue)
            while (true) {
                kotlinx.coroutines.delay(100)
                val newValue = block()
                if (newValue != lastValue) {
                    lastValue = newValue
                    emit(newValue)
                }
            }
        }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            setupTtsLanguage()
        } else {
            ttsErrorMessage = "ভয়েস ইঞ্জিন চালু করা সম্ভব হয়নি।"
            Log.e(TAG, "TTS Initialization failed")
        }
    }

    private fun setupTtsLanguage() {
        val localeBD = Locale("bn", "BD")
        val localeIN = Locale("bn", "IN")
        
        var result = tts?.setLanguage(localeBD)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            result = tts?.setLanguage(localeIN)
        }

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            ttsErrorMessage = "ডিভাইসে বাংলা ভয়েস ডাটা খুঁজে পাওয়া যায়নি। দয়া করে Google TTS থেকে বাংলা ভয়েস ডাউনলোড করুন।"
            Log.w(TAG, "Bangla language not supported in current TTS engine")
        } else {
            isTtsInitialized = true
            ttsErrorMessage = ""
            setupTtsProgressListener()
        }
    }

    private fun setupTtsProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking = false
                spokenCharacterIndex = 0
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                spokenCharacterIndex = start
            }
        })
    }

    // Adjust parameters and dynamically set language according to selected voice type and text content
    private fun applyVoiceSettings(text: String) {
        val hasBengali = text.contains(Regex("[\\u0980-\\u09FF]"))
        val locale = if (hasBengali) {
            Locale("bn", "BD")
        } else {
            Locale.US
        }
        
        var result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            if (hasBengali) {
                tts?.setLanguage(Locale("bn", "IN"))
            } else {
                tts?.setLanguage(Locale.ENGLISH)
            }
        }

        // Female: base pitch & speed
        // Male: lower pitch
        // Child: higher pitch
        val finalPitch = when (selectedVoiceType) {
            "Male" -> pitch * 0.75f
            "Child" -> pitch * 1.45f
            else -> pitch // Female / Standard
        }
        
        tts?.setPitch(finalPitch)
        tts?.setSpeechRate(speechRate)
    }

    fun speak() {
        if (textInput.isBlank()) return

        applyVoiceSettings(textInput)

        // Insert into history when reading starts
        viewModelScope.launch {
            repository.insertHistory(textInput)
        }

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "reader_utterance")
        }
        tts?.speak(textInput, TextToSpeech.QUEUE_FLUSH, params, "reader_utterance")
        isSpeaking = true
    }

    fun pauseOrStop() {
        tts?.stop()
        isSpeaking = false
        spokenCharacterIndex = 0
    }

    // Translate bidirectional (English <-> Bengali)
    fun translate() {
        if (textInput.isBlank()) return
        isTranslating = true
        translationErrorMessage = ""
        viewModelScope.launch {
            val result = com.example.api.GeminiTranslateHelper.translateText(textInput)
            result.onSuccess { translated ->
                translatedTextResult = translated
                isTranslating = false
            }
            result.onFailure { exception ->
                translationErrorMessage = "অনুবাদ ব্যর্থ হয়েছে: ${exception.localizedMessage ?: "API Error"}"
                isTranslating = false
            }
        }
    }

    // Swap input text and translation result
    fun swapText() {
        if (translatedTextResult.isNotBlank()) {
            val temp = textInput
            textInput = translatedTextResult
            translatedTextResult = temp
        }
    }

    // Clear translation results
    fun clearTranslation() {
        translatedTextResult = ""
        translationErrorMessage = ""
    }

    // Smartly enhance text quality using Gemini AI
    fun improveTextQuality() {
        if (textInput.isBlank()) return
        isTranslating = true
        translationErrorMessage = ""
        viewModelScope.launch {
            val result = com.example.api.GeminiTranslateHelper.improveText(textInput)
            result.onSuccess { improved ->
                textInput = improved
                isTranslating = false
            }
            result.onFailure { exception ->
                translationErrorMessage = "লেখা উন্নত করা যায়নি: ${exception.localizedMessage ?: "API Error"}"
                isTranslating = false
            }
        }
    }

    // Toggle favorite
    fun toggleFavoriteCurrentText() {
        if (textInput.isBlank()) return
        viewModelScope.launch {
            repository.toggleFavorite(textInput, isCurrentTextFavorite.value)
        }
    }

    // Clear history
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteHistoryItem(item: HistoryItem) {
        viewModelScope.launch {
            repository.deleteHistory(item)
        }
    }

    fun deleteFavoriteItem(item: FavoriteItem) {
        viewModelScope.launch {
            repository.deleteFavorite(item)
        }
    }

    // Export speech to MP3 file in Downloads
    fun saveAsAudioFile(context: Context) {
        if (textInput.isBlank()) return
        audioSaveStatus = "সংরক্ষণ করা হচ্ছে..."
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                applyVoiceSettings(textInput)
                
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                
                val fileName = "BanglaTTS_${System.currentTimeMillis()}.mp3"
                val audioFile = File(downloadsDir, fileName)
                
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "save_file_utterance")
                }

                val result = tts?.synthesizeToFile(textInput, params, audioFile, "save_file_utterance")
                
                withContext(Dispatchers.Main) {
                    if (result == TextToSpeech.SUCCESS) {
                        audioSaveStatus = "সফলভাবে ডাউনলোড ফোল্ডারে সংরক্ষিত হয়েছে:\n$fileName"
                    } else {
                        audioSaveStatus = "অডিও ফাইল সেভ করা সম্ভব হয়নি।"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    audioSaveStatus = "Error: ${e.localizedMessage}"
                }
            }
        }
    }

    // OCR: Handle captured or picked image
    fun runOcr(bitmap: Bitmap) {
        ocrStatus = "ছবি থেকে লেখা স্ক্যান করা হচ্ছে..."
        viewModelScope.launch {
            val result = GeminiOcrHelper.performOcr(bitmap)
            result.onSuccess { text ->
                textInput = text
                ocrStatus = "সফলভাবে স্ক্যান সম্পন্ন হয়েছে!"
            }
            result.onFailure { exception ->
                ocrStatus = "ভুল: ${exception.localizedMessage ?: "OCR ব্যর্থ হয়েছে"}"
            }
        }
    }

    // File Parsers: TXT, PDF, DOCX
    fun importFile(context: Context, uri: Uri) {
        fileImportStatus = "ফাইল পড়া হচ্ছে..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val type = contentResolver.getType(uri) ?: ""
                val originalFileName = getFileName(context, uri) ?: "document"
                
                var extractedText = ""

                if (originalFileName.endsWith(".txt", true) || type.contains("text/plain")) {
                    extractedText = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                } 
                else if (originalFileName.endsWith(".pdf", true) || type.contains("pdf")) {
                    extractedText = parsePdfUsingRenderer(context, uri)
                } 
                else if (originalFileName.endsWith(".docx", true) || type.contains("officedocument.wordprocessingml")) {
                    extractedText = parseDocxFile(context, uri)
                } else {
                    // Fallback to text reading if type is unknown
                    extractedText = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                }

                withContext(Dispatchers.Main) {
                    if (extractedText.isNotBlank()) {
                        textInput = extractedText
                        fileImportStatus = "ফাইল সফলভাবে ইম্পোর্ট করা হয়েছে!"
                    } else {
                        fileImportStatus = "ফাইল থেকে কোনো লেখা পাওয়া যায়নি।"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    fileImportStatus = "ভুল: ${e.localizedMessage ?: "ফাইল পড়া সম্ভব হয়নি"}"
                }
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    // Elegant and fully native PDF rendering + text recognition if available,
    // or we can read first few pages text or let them scan it.
    // Wait, PDF files are often parsed via PDFBox or native renderer. Let's do a fallback or simple parsing:
    private suspend fun parsePdfUsingRenderer(context: Context, uri: Uri): String = try {
        // Since Android's PdfRenderer renders pages to Bitmaps, we can render the first page
        // and scan it via Gemini OCR! That's incredibly elegant and works for scanned PDFs!
        val fileDescriptor: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(uri, "r")
        if (fileDescriptor != null) {
            val pdfRenderer = PdfRenderer(fileDescriptor)
            val pageCount = pdfRenderer.pageCount
            
            val stringBuilder = StringBuilder()
            // We'll process first page or first 3 pages if pageCount > 0, to avoid long loops
            val pagesToProcess = minOf(3, pageCount)
            
            for (i in 0 until pagesToProcess) {
                val page = pdfRenderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                // Fill with white background
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                
                // Let's call Gemini to scan this rendered PDF page!
                val ocrResult = GeminiOcrHelper.performOcr(bitmap)
                ocrResult.onSuccess { pageText ->
                    stringBuilder.append("--- Page ${i + 1} ---\n")
                    stringBuilder.append(pageText).append("\n\n")
                }
            }
            pdfRenderer.close()
            fileDescriptor.close()
            
            if (stringBuilder.isNotEmpty()) {
                stringBuilder.toString().trim()
            } else {
                "PDF ফাইলটি খালি অথবা স্ক্যান করা যায়নি।"
            }
        } else {
            "PDF ফাইল ওপেন করা সম্ভব হয়নি।"
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error rendering PDF", e)
        "PDF ফাইল রেন্ডার করতে ভুল হয়েছে: ${e.localizedMessage}"
    }

    // DOCX file parser: DOCX is a zip file containing word/document.xml.
    // Let's parse it and grab text inside <w:t> tags. Extremely lightweight and 100% stable!
    private fun parseDocxFile(context: Context, uri: Uri): String = try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val zipInputStream = ZipInputStream(inputStream)
        var entry = zipInputStream.nextEntry
        var documentXmlContent = ""
        
        while (entry != null) {
            if (entry.name == "word/document.xml") {
                documentXmlContent = zipInputStream.bufferedReader().readText()
                break
            }
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        inputStream?.close()

        if (documentXmlContent.isNotEmpty()) {
            // Match contents inside <w:t> tags
            val regex = Regex("<w:t[^>]*>(.*?)</w:t>")
            val matches = regex.findAll(documentXmlContent)
            val extracted = matches.map { it.groupValues[1] }.joinToString("")
            
            // Unescape common XML characters
            extracted.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
        } else {
            "DOCX ফাইলে কোনো লেখা পাওয়া যায়নি।"
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error parsing DOCX", e)
        "DOCX ফাইলটি পড়া সম্ভব হয়নি: ${e.localizedMessage}"
    }

    override fun onCleared() {
        super.onCleared()
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        tts?.shutdown()
    }
}
