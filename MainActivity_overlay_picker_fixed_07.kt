package com.example.mastercut

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.TextureView
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.opengl.GLES20
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint as ComposePaint
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.C
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoFrameProcessingException
import androidx.media3.common.VideoSize
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.VideoCompositorSettings
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.audio.SpeedProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BaseGlShaderProgram
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram
import androidx.media3.effect.MatrixTransformation
import androidx.media3.effect.RgbMatrix
import androidx.media3.effect.OverlayEffect
import androidx.media3.common.OverlaySettings
import androidx.media3.effect.StaticOverlaySettings
import androidx.media3.effect.Presentation
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.effect.TextureOverlay
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.mastercut.ui.theme.MasterCutTheme
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds
import androidx.media3.common.util.Size as MediaSize

// ───────────────────────── Colors ─────────────────────────
private val BgDark = Color(0xFF121212)
private val PanelDark = Color(0xFF1A1A1A)
private val BarDark = Color(0xFF1C1C1C)
private val TrackBg = Color(0xFF111111)
private val Accent = Color(0xFFFF4757)
private val Stroke = Color(0xFF333333)
private val TextDim = Color(0xFF888888)

// ───────────────────────── Aspect Ratio Definitions ─────────────────────────
enum class AspectRatio(val label: String, val ratio: Float, val iconW: Int, val iconH: Int) {
    RATIO_16_9("16:9", 16f / 9f, 28, 16),
    RATIO_9_16("9:16", 9f / 16f, 16, 28),
    RATIO_1_1("1:1", 1f, 20, 20),
    RATIO_4_3("4:3", 4f / 3f, 24, 18),
    RATIO_3_4("3:4", 3f / 4f, 18, 24),
    RATIO_4_5("4:5", 4f / 5f, 18, 22),
    RATIO_2_35_1("2.35:1", 2.35f, 30, 13)
}

// ───────────────────────── Data model ─────────────────────────
enum class TrackCategory { VIDEO, OVERLAY, AUDIO }

// ট্রান্সিশনের ধরন (ক্লিপের শুরু / শেষ / দুই ক্লিপের মাঝে বসে)
enum class TransitionType(val label: String, val icon: String) {
    NONE("None", "🚫"),
    FADE("Fade", "🌑"),
    FLASH("Flash", "⚡"),
    DISSOLVE("Dissolve", "🌫️"),
    ZOOM_IN("Zoom In", "🔍"),
    ZOOM_OUT("Zoom Out", "🔭"),
    SLIDE_LEFT("Slide Left", "⬅️"),
    SLIDE_RIGHT("Slide Right", "➡️"),
    SLIDE_UP("Slide Up", "⬆️"),
    SLIDE_DOWN("Slide Down", "⬇️")
}

// ট্রান্সিশন কোথায় বসবে
enum class TransTarget(val label: String) {
    START("Clip start"),
    END("Clip end"),
    BOTH("Start + End"),
    JOIN_PREV("Between ← prev"),
    JOIN_NEXT("Between next →")
}

// ওভারলে ক্লিপের Blend Mode (নিচের লেয়ারের সাথে মেশানোর ধরন)
enum class ClipBlend(val label: String, val mode: BlendMode) {
    NORMAL("Normal", BlendMode.SrcOver),
    MULTIPLY("Multiply", BlendMode.Multiply),
    SCREEN("Screen", BlendMode.Screen),
    OVERLAY("Overlay", BlendMode.Overlay),
    DARKEN("Darken", BlendMode.Darken),
    LIGHTEN("Lighten", BlendMode.Lighten),
    ADD("Add", BlendMode.Plus),
    SOFT_LIGHT("Soft light", BlendMode.Softlight),
    HARD_LIGHT("Hard light", BlendMode.Hardlight),
    DIFFERENCE("Difference", BlendMode.Difference)
}

data class TrackInfo(
    val id: String,
    val label: String,
    val category: TrackCategory,
    val color: Color,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false
)

data class Clip(
    val id: Int,
    val name: String,
    val trackId: String,
    val startMs: Long,
    val durationMs: Long,
    val uri: String? = null,
    val inMs: Long = 0L,
    val textOverlay: String? = null,
    val isVideoOverlay: Boolean = false,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    // offsetX / offsetY এখন প্রিভিউ-উচ্চতার (preview height) ভগ্নাংশ। px নয়।
    // এতে প্রিভিউ ছোট-বড় হলেও (টাইমলাইন লুকালে) এক্সপোর্টে পজিশন একই থাকে।
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val volume: Float = 1.0f,
    val opacity: Float = 1.0f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val transIn: TransitionType = TransitionType.NONE,
    val transInMs: Long = 700L,
    val transOut: TransitionType = TransitionType.NONE,
    val transOutMs: Long = 700L,
    val adj: Adjust = Adjust(),
    val filter: FilterPreset = FilterPreset.NONE,
    val textStyle: TextStyleSpec = TextStyleSpec(),
    // প্লেব্যাক গতি (0.25x..4x)। durationMs = টাইমলাইনে ক্লিপের দৈর্ঘ্য; সোর্সের ব্যবহৃত অংশ = durationMs * speed
    val speed: Float = 1f,
    val blend: ClipBlend = ClipBlend.NORMAL,
    // সোর্স ফাইলের আসল দৈর্ঘ্য (ms)। ভিডিও/অডিও ক্লিপের জন্য > 0; ছবি/টেক্সটে 0 (সীমাহীন)
    val sourceDurationMs: Long = 0L,
    // ফাইল খুঁজে না পেলে true (সেভ হয় না; প্রতিবার লোডের সময় আবার যাচাই হয়)
    val missing: Boolean = false,
    // true হলে প্রিভিউতে সরানো / স্কেল / রোটেশন বন্ধ
    val locked: Boolean = false
) {
    val endMs: Long get() = startMs + durationMs
    /** সোর্স ফাইলের যতটা অংশ এই ক্লিপে ব্যবহার হচ্ছে (ms) */
    val sourceSpanMs: Long get() = Math.round(durationMs.toDouble() * speed)
    /** সোর্স-দৈর্ঘ্য জানা থাকলে এই গতিতে ক্লিপ সর্বোচ্চ কত ms লম্বা হতে পারে */
    fun maxDurationAt(spd: Float): Long =
        if (sourceDurationMs > 0L) Math.round((sourceDurationMs - inMs).coerceAtLeast(0L).toDouble() / spd).coerceAtLeast(MIN_CLIP_MS)
        else Long.MAX_VALUE
    val maxDurationMs: Long get() = maxDurationAt(speed)
    /** বাঁ দিকে বাড়াতে গেলে সোর্সের শুরু (inMs) পর্যন্তই যাওয়া যায় — টাইমলাইনের ms এ */
    val maxLeftExtendMs: Long
        get() = if (sourceDurationMs > 0L) Math.round(inMs.toDouble() / speed) else Long.MAX_VALUE
}

private const val MIN_CLIP_MS = 300L
private const val MIN_SPEED = 0.25f
private const val MAX_SPEED = 4f
private val SPEED_STEPS = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f, 4f)

private fun speedLabel(s: Float): String =
    if (s == s.toInt().toFloat()) "${s.toInt()}x" else "${s}x"

/** টাইমলাইনের playhead → ক্লিপের সোর্স-পজিশন (inMs থেকে মাপা), গতি ধরে */
private fun srcPosMs(c: Clip, playheadMs: Long): Long =
    Math.round((playheadMs - c.startMs).coerceAtLeast(0L).toDouble() * c.speed)
private const val BASE_DP_PER_SEC = 60f
private const val MIN_GAP_MS = 50L
private const val SNAP_DP = 12f
private const val MIN_ZOOM = 0.3f
private const val MAX_ZOOM = 6f
private const val MAX_TIMELINE_DP = 30000f
private const val MAX_UNDO = 50

// ওভারলে আইটেমের সাইজ — প্রিভিউ ও এক্সপোর্ট দুই জায়গায় একই অনুপাত (প্রিভিউ-উচ্চতার ভগ্নাংশ)
private const val IMG_BOX = 0.5f
private const val VID_W = 0.8f
private const val VID_H = 0.45f
private const val TEXT_SIZE = 0.09f
private const val TEXT_PAD_H = 0.045f
private const val TEXT_PAD_V = 0.022f
private const val TEXT_RADIUS = 0.025f

private val FADE_STEPS = listOf(0L, 500L, 1000L, 2000L)

data class Gap(val trackId: String, val startMs: Long, val endMs: Long) {
    val durationMs: Long get() = endMs - startMs
}

private fun defaultTracks() = listOf(
    TrackInfo("V1", "Video 1", TrackCategory.VIDEO, Color(0xFFFF4757)),
    TrackInfo("O1", "Overlay 1", TrackCategory.OVERLAY, Color(0xFF2ED573)),
    TrackInfo("A1", "Audio 1", TrackCategory.AUDIO, Color(0xFF1E90FF))
)

private fun shiftAfter(list: List<Clip>, trackId: String, fromMs: Long, byMs: Long): List<Clip> =
    list.map {
        if (it.trackId == trackId && it.startMs >= fromMs) it.copy(startMs = (it.startMs - byMs).coerceAtLeast(0L)) else it
    }

private fun resolveStart(
    snapPoints: List<Long>,
    blockers: List<Clip>,
    durationMs: Long,
    rawStartMs: Long,
    snapMs: Long
): Long {
    var start = rawStartMs.coerceAtLeast(0L)

    var bestStart = start
    var bestDist = snapMs + 1
    for (p in snapPoints) {
        val dStart = abs(start - p)
        if (dStart < bestDist) { bestDist = dStart; bestStart = p }
        val dEnd = abs(start + durationMs - p)
        if (dEnd < bestDist) { bestDist = dEnd; bestStart = p - durationMs }
    }
    if (bestDist <= snapMs) start = bestStart.coerceAtLeast(0L)

    fun overlaps(s: Long) = blockers.any { s < it.endMs && s + durationMs > it.startMs }

    if (overlaps(start)) {
        val candidates = buildList {
            add(0L)
            blockers.forEach { add(it.endMs); add(it.startMs - durationMs) }
        }.filter { it >= 0L && !overlaps(it) }
        start = candidates.minByOrNull { abs(it - rawStartMs) } ?: start
    }
    return start
}

private fun fadeGain(c: Clip, posMs: Long): Float {
    var g = 1f
    if (c.fadeInMs > 0 && posMs < c.fadeInMs) {
        g = minOf(g, posMs.coerceAtLeast(0L) / c.fadeInMs.toFloat())
    }
    val remain = c.durationMs - posMs
    if (c.fadeOutMs > 0 && remain < c.fadeOutMs) {
        g = minOf(g, remain.coerceAtLeast(0L) / c.fadeOutMs.toFloat())
    }
    return g
}

// ───────────────────────── Adjustments (colour) ─────────────────────────
/** প্রতিটি মান -1..1 (0 = অপরিবর্তিত) */
data class Adjust(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val temperature: Float = 0f,   // White balance: − ঠান্ডা (নীল), + গরম (হলুদ)
    val tint: Float = 0f,          // − সবুজ, + ম্যাজেন্টা
    val hue: Float = 0f,           // ±180°
    val vignette: Float = 0f,     // 0..1 — কোণা কালো
    val sharpen: Float = 0f,      // 0..1 — ধার বাড়ানো
    val grain: Float = 0f,        // 0..1 — মিহি ডিজিটাল দানা
    val filmGrain: Float = 0f     // 0..1 — বড়, নরম ফিল্মের দানা
) {
    val colorDefault: Boolean
        get() = brightness == 0f && contrast == 0f && saturation == 0f &&
                temperature == 0f && tint == 0f && hue == 0f
    val fxDefault: Boolean
        get() = vignette == 0f && sharpen == 0f && grain == 0f && filmGrain == 0f
    val isDefault: Boolean
        get() = colorDefault && fxDefault
}

// ───────────────────────── Filter presets ─────────────────────────
// ফিল্টার = তৈরি করা Adjust মানের সেট। ক্লিপের নিজের Adjust-এর সাথে যোগ হয়ে একসাথে কাজ করে (combineAdjust)।
enum class FilterPreset(val label: String, val icon: String, val adjust: Adjust) {
    NONE("None", "🚫", Adjust()),
    VINTAGE(
        "Vintage", "📷",
        Adjust(temperature = 0.35f, saturation = -0.25f, contrast = 0.08f, tint = 0.05f, vignette = 0.30f, filmGrain = 0.30f)
    ),
    CINEMATIC(
        "Cinematic", "🎬",
        Adjust(contrast = 0.30f, saturation = -0.20f, temperature = -0.20f, tint = -0.08f, vignette = 0.20f)
    ),
    B_AND_W("B&W", "⚫", Adjust(saturation = -1f, contrast = 0.15f)),
    WARM("Warm", "☀️", Adjust(temperature = 0.45f, saturation = 0.05f)),
    COOL("Cool", "❄️", Adjust(temperature = -0.45f, saturation = 0.05f)),
    FADE("Fade", "🌫️", Adjust(contrast = -0.30f, brightness = 0.10f, saturation = -0.15f)),
    VIVID("Vivid", "🌈", Adjust(saturation = 0.50f, contrast = 0.25f)),
    SEPIA("Sepia", "🟤", Adjust(temperature = 0.55f, saturation = -0.70f, contrast = 0.05f, brightness = 0.02f)),
    NOIR("Noir", "🖤", Adjust(saturation = -1f, contrast = 0.50f, brightness = -0.05f, vignette = 0.60f)),
    DREAMY("Dreamy", "✨", Adjust(brightness = 0.12f, contrast = -0.20f, saturation = 0.10f, grain = 0.15f)),
    RETRO("Retro", "📼", Adjust(temperature = 0.30f, saturation = -0.30f, contrast = 0.10f, filmGrain = 0.40f))
}

/** ফিল্টার + ক্লিপের নিজস্ব Adjust: মানগুলো যোগ করে সীমার ভেতরে রাখা। প্রিভিউ ও এক্সপোর্ট দুই জায়গায় এটাই ব্যবহার হয়। */
private fun combineAdjust(a: Adjust, b: Adjust): Adjust {
    if (a.isDefault) return b
    if (b.isDefault) return a
    return Adjust(
        brightness = (a.brightness + b.brightness).coerceIn(-1f, 1f),
        contrast = (a.contrast + b.contrast).coerceIn(-1f, 1f),
        saturation = (a.saturation + b.saturation).coerceIn(-1f, 1f),
        temperature = (a.temperature + b.temperature).coerceIn(-1f, 1f),
        tint = (a.tint + b.tint).coerceIn(-1f, 1f),
        hue = (a.hue + b.hue).coerceIn(-1f, 1f),
        vignette = (a.vignette + b.vignette).coerceIn(0f, 1f),
        sharpen = (a.sharpen + b.sharpen).coerceIn(0f, 1f),
        grain = (a.grain + b.grain).coerceIn(0f, 1f),
        filmGrain = (a.filmGrain + b.filmGrain).coerceIn(0f, 1f)
    )
}

// ───────────────────────── Text style ─────────────────────────
// টেক্সট ওভারলের চেহারার সব প্রপার্টি এখানে। প্রিভিউ (StyledTextOverlay) ও এক্সপোর্ট (renderOverlayBitmap)
// দুটোই এই একই ডেটা ও একই TextPx হিসাব থেকে আঁকে, তাই দুই জায়গায় মিলে যায়।
enum class TextFont(val label: String, val family: FontFamily, val face: String) {
    SANS("Sans", FontFamily.SansSerif, "sans-serif"),
    SERIF("Serif", FontFamily.Serif, "serif"),
    MONO("Mono", FontFamily.Monospace, "monospace"),
    SCRIPT("Script", FontFamily.Cursive, "cursive")
}

data class TextStyleSpec(
    val font: TextFont = TextFont.SANS,
    val customFont: String? = null,       // ইউজারের যোগ করা ফন্ট ফাইলের নাম (filesDir/fonts); থাকলে এটাই ব্যবহার হয়
    val size: Float = 1f,                 // ডিফল্ট আকারের গুণক (0.4..2.5)
    val color: Int = 0xFFFFFFFF.toInt(),
    val bold: Boolean = true,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val spacing: Float = 0f,              // অক্ষরের ফাঁক, em এককে (-0.05..0.5)
    val shadow: Boolean = false,
    val shadowColor: Int = 0xFF000000.toInt(),
    val shadowBlur: Float = 0.4f,         // 0..1
    val shadowDx: Float = 0.3f,           // -1..1
    val shadowDy: Float = 0.3f,           // -1..1
    val outline: Boolean = false,
    val outlineColor: Int = 0xFF000000.toInt(),
    val outlineWidth: Float = 0.4f,       // 0..1
    val bg: Boolean = true,
    val bgColor: Int = 0xFF000000.toInt(),
    val bgAlpha: Float = 0.6f,            // ডিফল্ট 0.6 = আগের 0x99
    val bgRadius: Float = 1f              // কোণের গোলতা গুণক (0..4)
)

// ───────────────────────── Custom fonts (.ttf / .otf) ─────────────────────────
// ইউজারের বাছাই করা ফন্ট অ্যাপের ভেতরে (filesDir/fonts) কপি হয়; প্রিভিউ ও এক্সপোর্ট দুটোই একই Typeface ব্যবহার করে।
private object CustomFonts {
    private val typefaces = ConcurrentHashMap<String, Typeface>()
    private val families = ConcurrentHashMap<String, FontFamily>()

    private fun dir(context: Context) = File(context.filesDir, "fonts").apply { mkdirs() }

    fun list(context: Context): List<String> =
        dir(context).listFiles { f -> f.isFile && f.extension.lowercase() in setOf("ttf", "otf") }
            ?.map { it.name }?.sorted() ?: emptyList()

    fun label(name: String): String = name.substringBeforeLast('.').replace('_', ' ')

    fun typeface(context: Context, name: String): Typeface? {
        typefaces[name]?.let { return it }
        val f = File(dir(context), name)
        if (!f.exists()) return null
        return try {
            Typeface.createFromFile(f).also { typefaces[name] = it }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun family(context: Context, name: String): FontFamily? {
        families[name]?.let { return it }
        val tf = typeface(context, name) ?: return null
        return FontFamily(tf).also { families[name] = it }
    }

    fun delete(context: Context, name: String) {
        try {
            File(dir(context), name).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        typefaces.remove(name)
        families.remove(name)
    }

    /** সফল হলে (ফাইলের নাম, null), ব্যর্থ হলে (null, কারণ) */
    fun import(context: Context, uri: Uri): Pair<String?, String?> {
        return try {
            var display: String? = null
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) display = c.getString(0)
            }
            val raw = display ?: uri.lastPathSegment ?: "font.ttf"
            val ext = raw.substringAfterLast('.', "").lowercase()
            if (ext != "ttf" && ext != "otf") return null to "Please choose a .ttf or .otf font file"

            val safe = raw.replace(Regex("[^A-Za-z0-9._-]"), "_").takeLast(60)
            val folder = dir(context)
            val tmp = File(folder, "$safe.tmp")
            val input = context.contentResolver.openInputStream(uri) ?: return null to "Could not read this file"
            input.use { i -> tmp.outputStream().use { o -> i.copyTo(o) } }

            // হেডার দেখে সত্যিকারের ফন্ট কিনা যাচাই (TrueType / OpenType)
            val head = ByteArray(4)
            tmp.inputStream().use { it.read(head) }
            val magic = String(head, Charsets.ISO_8859_1)
            val isTrueType = head[0] == 0.toByte() && head[1] == 1.toByte() && head[2] == 0.toByte() && head[3] == 0.toByte()
            if (!(isTrueType || magic == "OTTO" || magic == "true")) {
                tmp.delete()
                return null to "This file is not a valid font"
            }

            val dest = File(folder, safe)
            if (dest.exists()) dest.delete()
            if (!tmp.renameTo(dest)) {
                tmp.copyTo(dest, overwrite = true)
                tmp.delete()
            }
            typefaces.remove(safe)
            families.remove(safe)
            if (typeface(context, safe) == null) {
                dest.delete()
                return null to "Android could not load this font"
            }
            safe to null
        } catch (e: Exception) {
            e.printStackTrace()
            null to "Font import failed"
        }
    }
}

/** TextStyleSpec থেকে পিক্সেল মান — unit = প্রিভিউ/আউটপুট ফ্রেমের উচ্চতা। প্রিভিউ ও এক্সপোর্ট একই ফাংশন ব্যবহার করে। */
private class TextPx(s: TextStyleSpec, unit: Float) {
    val font = TEXT_SIZE * unit * s.size
    val padH = TEXT_PAD_H * unit * s.size
    val padV = TEXT_PAD_V * unit * s.size
    val radius = TEXT_RADIUS * unit * s.size * s.bgRadius
    val blur = (s.shadowBlur * 0.03f * unit).coerceAtLeast(0.5f)
    val dx = s.shadowDx * 0.02f * unit
    val dy = s.shadowDy * 0.02f * unit
    val stroke = (s.outlineWidth * 0.012f * unit).coerceAtLeast(0.5f)
    val bgArgb: Int = ((s.bgAlpha.coerceIn(0f, 1f) * 255f).roundToInt() shl 24) or (s.bgColor and 0xFFFFFF)
}

private fun textStyleToJson(s: TextStyleSpec): JSONObject = JSONObject().apply {
    put("font", s.font.name)
    if (s.customFont != null) put("customFont", s.customFont)
    put("size", s.size.toDouble())
    put("color", s.color)
    put("bold", s.bold)
    put("italic", s.italic)
    put("underline", s.underline)
    put("spacing", s.spacing.toDouble())
    put("shadow", s.shadow)
    put("shadowColor", s.shadowColor)
    put("shadowBlur", s.shadowBlur.toDouble())
    put("shadowDx", s.shadowDx.toDouble())
    put("shadowDy", s.shadowDy.toDouble())
    put("outline", s.outline)
    put("outlineColor", s.outlineColor)
    put("outlineWidth", s.outlineWidth.toDouble())
    put("bg", s.bg)
    put("bgColor", s.bgColor)
    put("bgAlpha", s.bgAlpha.toDouble())
    put("bgRadius", s.bgRadius.toDouble())
}

private fun textStyleFromJson(o: JSONObject): TextStyleSpec {
    val d = TextStyleSpec()
    val fontName = o.optString("font", d.font.name)
    return TextStyleSpec(
        font = TextFont.entries.firstOrNull { it.name == fontName } ?: d.font,
        customFont = o.optString("customFont", "").takeIf { it.isNotEmpty() },
        size = o.optDouble("size", d.size.toDouble()).toFloat(),
        color = o.optInt("color", d.color),
        bold = o.optBoolean("bold", d.bold),
        italic = o.optBoolean("italic", d.italic),
        underline = o.optBoolean("underline", d.underline),
        spacing = o.optDouble("spacing", d.spacing.toDouble()).toFloat(),
        shadow = o.optBoolean("shadow", d.shadow),
        shadowColor = o.optInt("shadowColor", d.shadowColor),
        shadowBlur = o.optDouble("shadowBlur", d.shadowBlur.toDouble()).toFloat(),
        shadowDx = o.optDouble("shadowDx", d.shadowDx.toDouble()).toFloat(),
        shadowDy = o.optDouble("shadowDy", d.shadowDy.toDouble()).toFloat(),
        outline = o.optBoolean("outline", d.outline),
        outlineColor = o.optInt("outlineColor", d.outlineColor),
        outlineWidth = o.optDouble("outlineWidth", d.outlineWidth.toDouble()).toFloat(),
        bg = o.optBoolean("bg", d.bg),
        bgColor = o.optInt("bgColor", d.bgColor),
        bgAlpha = o.optDouble("bgAlpha", d.bgAlpha.toDouble()).toFloat(),
        bgRadius = o.optDouble("bgRadius", d.bgRadius.toDouble()).toFloat()
    )
}

private val ADJ_IDENTITY_4x4 = floatArrayOf(
    1f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f,
    0f, 0f, 1f, 0f,
    0f, 0f, 0f, 1f
)

/** 4x5 (সারি-মেজর) ম্যাট্রিক্স গুণ: প্রথমে b, তারপর a */
private fun mulColor(a: FloatArray, b: FloatArray): FloatArray {
    val r = FloatArray(20)
    for (i in 0 until 4) {
        for (j in 0 until 5) {
            var sum = 0f
            for (k in 0 until 4) sum += a[i * 5 + k] * b[k * 5 + j]
            if (j == 4) sum += a[i * 5 + 4]
            r[i * 5 + j] = sum
        }
    }
    return r
}

private fun colorIdentity() = floatArrayOf(
    1f, 0f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f, 0f,
    0f, 0f, 1f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f
)

/** Adjust → RgbMatrix-এর 4x4 (কলাম-মেজর, শেষ কলামে ধ্রুবক) অ্যারে। প্রিভিউ ও এক্সপোর্ট দুই জায়গাতেই এটাই ব্যবহার হয়। */
private fun buildAdjust4x5(a: Adjust): FloatArray {
    if (a.colorDefault) return colorIdentity()

    // হোয়াইট ব্যালান্স (temperature) + tint
    val wb = colorIdentity().also {
        it[0] = 1f + 0.30f * a.temperature + 0.08f * a.tint     // R
        it[6] = 1f - 0.25f * a.tint                              // G
        it[12] = 1f - 0.30f * a.temperature + 0.08f * a.tint    // B
    }

    // hue
    val rad = Math.toRadians((a.hue * 180f).toDouble())
    val cs = cos(rad).toFloat()
    val sn = sin(rad).toFloat()
    val lr = 0.213f; val lg = 0.715f; val lb = 0.072f
    val hue = floatArrayOf(
        lr + cs * (1 - lr) + sn * (-lr), lg + cs * (-lg) + sn * (-lg), lb + cs * (-lb) + sn * (1 - lb), 0f, 0f,
        lr + cs * (-lr) + sn * 0.143f, lg + cs * (1 - lg) + sn * 0.140f, lb + cs * (-lb) + sn * (-0.283f), 0f, 0f,
        lr + cs * (-lr) + sn * (-(1 - lr)), lg + cs * (-lg) + sn * lg, lb + cs * (1 - lb) + sn * lb, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    // saturation
    val sat = 1f + a.saturation
    val ir = (1 - sat) * 0.2126f
    val ig = (1 - sat) * 0.7152f
    val ib = (1 - sat) * 0.0722f
    val satM = floatArrayOf(
        ir + sat, ig, ib, 0f, 0f,
        ir, ig + sat, ib, 0f, 0f,
        ir, ig, ib + sat, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    // contrast (0.5 কে কেন্দ্র ধরে)
    val k = 1f + 0.8f * a.contrast
    val off = 0.5f * (1f - k)
    val con = floatArrayOf(
        k, 0f, 0f, 0f, off,
        0f, k, 0f, 0f, off,
        0f, 0f, k, 0f, off,
        0f, 0f, 0f, 1f, 0f
    )

    // brightness
    val br = 0.4f * a.brightness
    val bri = floatArrayOf(
        1f, 0f, 0f, 0f, br,
        0f, 1f, 0f, 0f, br,
        0f, 0f, 1f, 0f, br,
        0f, 0f, 0f, 1f, 0f
    )

    // wb → hue → saturation → contrast → brightness
    return mulColor(bri, mulColor(con, mulColor(satM, mulColor(hue, wb))))
}

/** এক্সপোর্টের RgbMatrix: 4x4 কলাম-মেজর */
private fun buildAdjustMatrix(a: Adjust): FloatArray {
    if (a.colorDefault) return ADJ_IDENTITY_4x4
    val m = buildAdjust4x5(a)

    val out = FloatArray(16)
    for (i in 0 until 4) {
        for (j in 0 until 3) out[j * 4 + i] = m[i * 5 + j]
        out[12 + i] = m[i * 5 + 3] + m[i * 5 + 4]   // আলফা (=1) ধরে ধ্রুবক যোগ
    }
    return out
}

/** প্রিভিউর Compose ColorFilter: 4x5 সারি-মেজর, ধ্রুবক অংশ 0..255 স্কেলে (Android ColorMatrix-এর নিয়ম) */
private fun buildAdjustColorMatrix(a: Adjust): FloatArray {
    val m = buildAdjust4x5(a).copyOf()
    for (r in 0 until 4) m[r * 5 + 4] = m[r * 5 + 4] * 255f
    return m
}

// ───────────────────────── Transitions (math) ─────────────────────────
private const val FX_PANEL = "FX / Transition"
private const val FX_ADJUST_PANEL = "FX / Adjust"
private const val FX_FILTER_PANEL = "FX / Filter"
private const val FX_TEXT_PANEL = "FX / Text"
private const val FX_VIDEO_PANEL = "FX / Video"
private const val TRANS_DEFAULT_MS = 700L
private const val ADJ_MS = 150L              // দুই ক্লিপ "লাগোয়া" ধরার সীমা
private const val JUNCTION_PICK_MS = 800L    // প্লেহেড কাটার বিন্দুর কাছে আছে কিনা

/** একটি মুহূর্তে ট্রান্সিশনের প্রভাব। প্রিভিউ ও এক্সপোর্ট দুই জায়গায় একই হিসাব। */
private class TransFx(
    val darken: Float = 0f,   // 0..1 — কালো দিয়ে ঢাকা
    val whiten: Float = 0f,   // 0..1 — সাদা দিয়ে ঢাকা
    val scale: Float = 1f,
    val tx: Float = 0f,       // ফ্রেম-প্রস্থের ভগ্নাংশ (ডানে +)
    val ty: Float = 0f        // ফ্রেম-উচ্চতার ভগ্নাংশ (নিচে +)
) {
    val isIdentity: Boolean
        get() = darken == 0f && whiten == 0f && scale == 1f && tx == 0f && ty == 0f
}

private val NO_FX = TransFx()

/** p: 0 = পুরো লুকানো অবস্থা, 1 = স্বাভাবিক। intro = ক্লিপের শুরুতে বসানো। */
private fun fxFor(type: TransitionType, p: Float, intro: Boolean): TransFx {
    val q = p.coerceIn(0f, 1f)
    if (q >= 1f) return NO_FX
    val e = q * q * (3f - 2f * q)   // smoothstep
    val away = 1f - e
    return when (type) {
        TransitionType.NONE -> NO_FX
        TransitionType.FADE -> TransFx(darken = 1f - q)
        TransitionType.FLASH -> TransFx(whiten = 1f - q)
        TransitionType.DISSOLVE -> TransFx(darken = away)
        TransitionType.ZOOM_IN -> TransFx(scale = 0.15f + 0.85f * e)
        TransitionType.ZOOM_OUT -> TransFx(scale = 1f + 1.5f * away, darken = 1f - q)
        TransitionType.SLIDE_LEFT -> TransFx(tx = if (intro) away else -away)
        TransitionType.SLIDE_RIGHT -> TransFx(tx = if (intro) -away else away)
        TransitionType.SLIDE_UP -> TransFx(ty = if (intro) away else -away)
        TransitionType.SLIDE_DOWN -> TransFx(ty = if (intro) -away else away)
    }
}

/** localMs = ক্লিপের শুরু থেকে কত মিলিসেকেন্ড পেরিয়েছে */
private fun clipFx(c: Clip, localMs: Long): TransFx {
    var best = NO_FX
    var bestP = 1f
    if (c.transIn != TransitionType.NONE) {
        val d = minOf(c.transInMs, c.durationMs / 2)
        if (d > 0L && localMs < d) {
            val p = localMs.coerceAtLeast(0L) / d.toFloat()
            if (p < bestP) { bestP = p; best = fxFor(c.transIn, p, true) }
        }
    }
    if (c.transOut != TransitionType.NONE) {
        val d = minOf(c.transOutMs, c.durationMs / 2)
        val remain = c.durationMs - localMs
        if (d > 0L && remain < d) {
            val p = remain.coerceAtLeast(0L) / d.toFloat()
            if (p < bestP) { bestP = p; best = fxFor(c.transOut, p, false) }
        }
    }
    return best
}

/** OverlaySettings-এর অ্যাঙ্কর ±1 এর বেশি হতে পারে না; বাকিটা ওভারলে-অ্যাঙ্করে ভাগ করা হয়। */
private class Anchor(val bg: Float, val ov: Float)

private fun splitAnchor(total: Float, half: Float): Anchor {
    val bg = total.coerceIn(-1f, 1f)
    val ov = if (half > 0.0001f) ((bg - total) / half).coerceIn(-1f, 1f) else 0f
    return Anchor(bg, ov)
}

/** পুরো-ফ্রেম ইমেজ/টেক্সট ওভারলের জন্য ট্রান্সিশন সেটিংস */
@OptIn(UnstableApi::class)
private fun staticFxSettings(fx: TransFx, opacity: Float = 1f): OverlaySettings {
    val a = ((1f - fx.darken) * (1f - fx.whiten) * opacity).coerceIn(0f, 1f)
    val ax = splitAnchor(fx.tx * 2f, fx.scale)
    val ay = splitAnchor(-fx.ty * 2f, fx.scale)
    return StaticOverlaySettings.Builder()
        .setAlphaScale(a)
        .setScale(fx.scale, fx.scale)
        .setBackgroundFrameAnchor(ax.bg, ay.bg)
        .setOverlayFrameAnchor(ax.ov, ay.ov)
        .build()
}

// ───────────────────────── State holder ─────────────────────────
@Stable
class EditorState {
    var projectId by mutableStateOf("")
    var selectedRatio by mutableStateOf(AspectRatio.RATIO_16_9)
    var projectName by mutableStateOf("New Project")

    var tracks by mutableStateOf(defaultTracks())
        private set

    var activeTrackId by mutableStateOf("V1")

    var clips by mutableStateOf(emptyList<Clip>())
        private set
    var playheadMs by mutableLongStateOf(0L)
    var isPlaying by mutableStateOf(false)
    var selectedId by mutableStateOf<Int?>(null)
    var selectedGap by mutableStateOf<Gap?>(null)
    // টাইমলাইনে নির্বাচিত ট্রান্সিশন/ফেড চিহ্ন: (ক্লিপ id, শুরুর দিকে কিনা)
    var selectedTransition by mutableStateOf<Pair<Int, Boolean>?>(null)
    // দুই ক্লিপের জয়েন্টের "+" বাটন থেকে খুললে পরের ক্লিপের id (FX বাটন থেকে খুললে null)
    var transitionJoinClipId by mutableStateOf<Int?>(null)
    // ক্লিপের শুরু/শেষের "+" থেকে খুললে কোন দিক (START / END)
    var transitionPresetTarget by mutableStateOf<TransTarget?>(null)
    var rippleOn by mutableStateOf(false)
    // Drag/trim করার সময় Snap হওয়া টাইম-পয়েন্টের দৃশ্যমান Guide Line
    var snapGuideMs by mutableStateOf<Long?>(null)
    var playheadLocked by mutableStateOf(false)
    var navTracks by mutableStateOf(setOf("V1", "O1", "A1"))
    var jumpTick by mutableIntStateOf(0)
    var zoom by mutableFloatStateOf(1f)
    var status by mutableStateOf("Ready")
    var timelineVisible by mutableStateOf(true)
    var panel by mutableStateOf<String?>(null)
    // ওভারলে-ভিডিও নির্বাচিত থাকলে ডান প্যানেলে ভলিউমের বদলে Properties দেখাবে কিনা
    var showOverlayProps by mutableStateOf(false)

    // প্রিভিউ বক্সের উচ্চতা (px) — ওভারলের পজিশন/সাইজের একক
    var previewHeightPx by mutableIntStateOf(0)

    private val undoStack = mutableStateListOf<Pair<List<TrackInfo>, List<Clip>>>()
    private val redoStack = mutableStateListOf<Pair<List<TrackInfo>, List<Clip>>>()
    // Drag session snapshot: সব pointer update একই original timeline থেকে হিসাব করে।
    private var clipMoveBase: List<Clip>? = null
    private var clipMoveOriginal: Clip? = null

    var nextId = 100
        private set

    val canUndo get() = undoStack.isNotEmpty()
    val canRedo get() = redoStack.isNotEmpty()
    val selectedClip get() = clips.firstOrNull { it.id == selectedId }
    val totalMs: Long get() = maxOf(15000L, (clips.maxOfOrNull { it.endMs } ?: 0L) + 3000L)
    private val maxZoom: Float
        get() {
            val totalSec = (totalMs / 1000f).coerceAtLeast(1f)
            return (MAX_TIMELINE_DP / (BASE_DP_PER_SEC * totalSec)).coerceIn(MIN_ZOOM, MAX_ZOOM)
        }
    val dpPerSec: Float get() = BASE_DP_PER_SEC * zoom.coerceIn(MIN_ZOOM, maxZoom)

    val isAllMuted: Boolean
        get() = tracks.filter { it.category == TrackCategory.VIDEO || it.category == TrackCategory.AUDIO }.all { it.isMuted }

    fun toggleMuteAll() {
        beginChange()
        val willMute = !isAllMuted
        tracks = tracks.map {
            if (it.category == TrackCategory.VIDEO || it.category == TrackCategory.AUDIO) {
                it.copy(isMuted = willMute)
            } else it
        }
        status = if (willMute) "All audio muted" else "All audio unmuted"
    }

    fun toggleTrackMute(trackId: String) {
        beginChange()
        tracks = tracks.map {
            if (it.id == trackId) it.copy(isMuted = !it.isMuted) else it
        }
        val t = tracks.firstOrNull { it.id == trackId }
        status = if (t?.isMuted == true) "${t.label} Muted" else "${t?.label} Unmuted"
    }

    fun toggleTrackLock(trackId: String) {
        beginChange()
        tracks = tracks.map {
            if (it.id == trackId) it.copy(isLocked = !it.isLocked) else it
        }
        val t = tracks.firstOrNull { it.id == trackId }
        status = if (t?.isLocked == true) "${t.label} Locked" else "${t?.label} Unlocked"
    }

    /** নতুন ট্র্যাকের নম্বর = বিদ্যমান সর্বোচ্চ নম্বর + 1 (মুছে ফেলার পরেও ID ডুপ্লিকেট হয় না) */
    private fun nextTrackNo(prefix: String): Int =
        (tracks.filter { it.id.startsWith(prefix) }
            .mapNotNull { it.id.drop(prefix.length).toIntOrNull() }.maxOrNull() ?: 0) + 1

    fun updateClipVolume(clipId: Int, newVolume: Float) {
        clips = clips.map {
            if (it.id == clipId) it.copy(volume = newVolume.coerceIn(0f, 2f)) else it
        }
    }

    fun stepClipVolume(id: Int, delta: Float) {
        val current = clips.firstOrNull { it.id == id }?.volume ?: return
        updateClipVolume(id, (current + delta).coerceIn(0f, 2f))
    }
    fun toggleClipMute(clipId: Int) {
        beginChange()
        clips = clips.map {
            if (it.id == clipId) {
                val newVol = if (it.volume == 0f) 1.0f else 0f
                it.copy(volume = newVol)
            } else it
        }
        val c = clips.firstOrNull { it.id == clipId }
        status = if (c?.volume == 0f) "Clip muted" else "Clip unmuted"
    }

    /** ভিডিও, অডিও ও ওভারলে-ভিডিও ক্লিপে গতি বদলানো যায়; ছবি/টেক্সটে নয় */
    fun supportsSpeed(c: Clip): Boolean {
        val cat = categoryOf(c) ?: return false
        return c.uri != null && c.textOverlay == null &&
                (cat == TrackCategory.VIDEO || cat == TrackCategory.AUDIO ||
                        (cat == TrackCategory.OVERLAY && c.isVideoOverlay))
    }

    /**
     * গতি বদলালে সোর্সের একই অংশ নতুন গতিতে চলে, তাই টাইমলাইনে দৈর্ঘ্য বদলায়।
     * Ripple চালু থাকলে পরের ক্লিপগুলো সরে যায়; নইলে পরের ক্লিপে ধাক্কা লাগলে শেষ ছাঁটা হয়।
     */
    fun setClipSpeed(clipId: Int, rawSpeed: Float) {
        val c = clips.firstOrNull { it.id == clipId } ?: return
        if (!supportsSpeed(c)) { status = "Speed is not available for this clip"; return }
        val newSpeed = rawSpeed.coerceIn(MIN_SPEED, MAX_SPEED)
        if (newSpeed == c.speed) return
        var newDur = Math.round(c.sourceSpanMs.toDouble() / newSpeed).coerceAtLeast(MIN_CLIP_MS)
        newDur = minOf(newDur, c.maxDurationAt(newSpeed))   // সোর্সের বাইরে যাবে না
        var shiftBy = 0L
        var clamped = false
        if (rippleOn) {
            shiftBy = newDur - c.durationMs
        } else if (newDur > c.durationMs) {
            val nextStart = clips.filter { it.trackId == c.trackId && it.id != c.id && it.startMs >= c.endMs }
                .minOfOrNull { it.startMs }
            if (nextStart != null && c.startMs + newDur > nextStart) {
                newDur = (nextStart - c.startMs).coerceAtLeast(MIN_CLIP_MS)
                clamped = true
            }
        }
        beginChange()
        clips = clips.map {
            when {
                it.id == c.id -> it.copy(
                    speed = newSpeed,
                    durationMs = newDur,
                    fadeInMs = it.fadeInMs.coerceAtMost(newDur / 2),
                    fadeOutMs = it.fadeOutMs.coerceAtMost(newDur / 2)
                )
                shiftBy != 0L && it.trackId == c.trackId && it.startMs >= c.endMs ->
                    it.copy(startMs = (it.startMs + shiftBy).coerceAtLeast(0L))
                else -> it
            }
        }
        status = "Speed ${speedLabel(newSpeed)}" + if (clamped) " (end trimmed to fit next clip)" else ""
    }

    /** Blend Mode শুধু ওভারলে ট্র্যাকের ক্লিপে (ভিডিও/ছবি/টেক্সট) — নিচে মেশানোর মতো লেয়ার থাকে */
    fun canBlend(c: Clip): Boolean =
        categoryOf(c) == TrackCategory.OVERLAY && (c.uri != null || c.textOverlay != null)

    /** Video fx ট্যাবের লক্ষ্য: ভিডিও/ওভারলে-ভিডিও (Speed) অথবা যেকোনো ওভারলে (Blend) */
    fun canVideoFx(c: Clip): Boolean =
        supportsSpeed(c) && categoryOf(c) != TrackCategory.AUDIO

    fun setClipBlend(clipId: Int, blend: ClipBlend) {
        val c = clips.firstOrNull { it.id == clipId } ?: return
        if (!canBlend(c) || c.blend == blend) return
        beginChange()
        clips = clips.map { if (it.id == clipId) it.copy(blend = blend) else it }
        status = "Blend: ${blend.label}"
    }

    fun stepClipSpeed(clipId: Int, faster: Boolean) {
        val c = clips.firstOrNull { it.id == clipId } ?: return
        val target = if (faster) SPEED_STEPS.firstOrNull { it > c.speed + 0.001f } ?: MAX_SPEED
        else SPEED_STEPS.lastOrNull { it < c.speed - 0.001f } ?: MIN_SPEED
        setClipSpeed(clipId, target)
    }

    fun toggleFade(fadeIn: Boolean) {
        val c = selectedClip ?: return
        beginChange()
        val cur = if (fadeIn) c.fadeInMs else c.fadeOutMs
        // চালু থাকলে বন্ধ, বন্ধ থাকলে 1.0s
        val value = if (cur > 0L) 0L else 1000L.coerceAtMost(c.durationMs / 2)
        clips = clips.map {
            if (it.id == c.id) {
                if (fadeIn) it.copy(fadeInMs = value) else it.copy(fadeOutMs = value)
            } else it
        }
        status = (if (fadeIn) "Fade In: " else "Fade Out: ") +
                (if (value == 0L) "off" else String.format(Locale.getDefault(), "%.1fs", value / 1000f))
    }

    fun addVideoTrack() {
        beginChange()
        val count = nextTrackNo("V")
        val newTrack = TrackInfo("V$count", "Video $count", TrackCategory.VIDEO, Color(0xFFFF4757))
        tracks = listOf(newTrack) + tracks
        navTracks = navTracks + newTrack.id
        activeTrackId = newTrack.id
        status = "Added ${newTrack.label}"
    }

    fun addOverlayTrack() {
        beginChange()
        val count = nextTrackNo("O")
        val newTrack = TrackInfo("O$count", "Overlay $count", TrackCategory.OVERLAY, Color(0xFF2ED573))
        val lastOverlayIndex = tracks.indexOfLast { it.category == TrackCategory.OVERLAY }
        tracks = if (lastOverlayIndex != -1) {
            tracks.toMutableList().apply { add(lastOverlayIndex + 1, newTrack) }
        } else {
            tracks + newTrack
        }
        navTracks = navTracks + newTrack.id
        activeTrackId = newTrack.id
        status = "Added ${newTrack.label}"
    }

    fun addAudioTrack() {
        beginChange()
        val count = nextTrackNo("A")
        val newTrack = TrackInfo("A$count", "Audio $count", TrackCategory.AUDIO, Color(0xFF1E90FF))
        tracks = tracks + newTrack
        navTracks = navTracks + newTrack.id
        activeTrackId = newTrack.id
        status = "Added ${newTrack.label}"
    }

    fun deleteTrack(trackId: String) {
        if (trackId == "V1" || trackId == "O1" || trackId == "A1") {
            status = "Default track cannot be deleted"
            return
        }
        beginChange()
        val t = tracks.firstOrNull { it.id == trackId }
        tracks = tracks.filterNot { it.id == trackId }
        clips = clips.filterNot { it.trackId == trackId }
        navTracks = navTracks - trackId
        if (activeTrackId == trackId) {
            activeTrackId = tracks.firstOrNull()?.id ?: "V1"
        }
        if (selectedClip?.trackId == trackId) {
            selectedId = null
        }
        status = "Deleted ${t?.label ?: trackId}"
    }

    fun beginChange() {
        undoStack.add(tracks to clips)
        if (undoStack.size > MAX_UNDO) undoStack.removeAt(0)
        redoStack.clear()
    }

    private fun commit(newList: List<Clip>) {
        beginChange()
        clips = newList
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(tracks to clips)
        val (prevTracks, prevClips) = undoStack.removeAt(undoStack.lastIndex)
        tracks = prevTracks
        clips = prevClips
        if (selectedClip == null) selectedId = null
        selectedTransition = null
        selectedGap = null
        if (tracks.none { it.id == activeTrackId }) {
            activeTrackId = tracks.firstOrNull()?.id ?: "V1"
        }
        navTracks = navTracks.filter { id -> tracks.any { it.id == id } }.toSet() +
                tracks.map { it.id }.filter { it !in navTracks }
        status = "Undo"
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.add(tracks to clips)
        val (nextTracks, nextClips) = redoStack.removeAt(redoStack.lastIndex)
        tracks = nextTracks
        clips = nextClips
        if (selectedClip == null) selectedId = null
        selectedTransition = null
        selectedGap = null
        if (tracks.none { it.id == activeTrackId }) {
            activeTrackId = tracks.firstOrNull()?.id ?: "V1"
        }
        navTracks = navTracks.filter { id -> tracks.any { it.id == id } }.toSet() +
                tracks.map { it.id }.filter { it !in navTracks }
        status = "Redo"
    }

    fun selectClip(id: Int) {
        snapGuideMs = null
        selectedId = id
        selectedGap = null
        selectedTransition = null
        val c = clips.firstOrNull { it.id == id }
        if (c != null) {
            activeTrackId = c.trackId
            status = "${c.name}: ${(c.durationMs / 1000f)}s"
        }
    }

    fun clearSelection() {
        selectedId = null
        selectedGap = null
        selectedTransition = null
    }

    fun selectTransition(clipId: Int, isIn: Boolean) {
        val c = clips.firstOrNull { it.id == clipId } ?: return
        selectedId = clipId
        selectedGap = null
        selectedTransition = clipId to isIn
        activeTrackId = c.trackId
        status = "Transition selected - press Delete to remove"
    }

    /** নির্বাচিত ট্রান্সিশন (অডিও ক্লিপে ফেড) মুছে ফেলা; দুই ক্লিপের মাঝের হলে দুই পাশই */
    private fun removeSelectedTransition(clipId: Int, isIn: Boolean) {
        val c = clips.firstOrNull { it.id == clipId }
        selectedTransition = null
        if (c == null) return
        beginChange()
        if (categoryOf(c) == TrackCategory.AUDIO) {
            clips = clips.map {
                if (it.id != c.id) it
                else if (isIn) it.copy(fadeInMs = 0L) else it.copy(fadeOutMs = 0L)
            }
            status = "Fade deleted"
            return
        }
        val type = if (isIn) c.transIn else c.transOut
        val partner = if (isIn) prevAdjacent(c) else nextAdjacent(c)
        clips = clips.map { x ->
            when {
                x.id == c.id -> if (isIn) x.copy(transIn = TransitionType.NONE) else x.copy(transOut = TransitionType.NONE)
                partner != null && x.id == partner.id ->
                    if (isIn && x.transOut == type) x.copy(transOut = TransitionType.NONE)
                    else if (!isIn && x.transIn == type) x.copy(transIn = TransitionType.NONE)
                    else x
                else -> x
            }
        }
        status = "Transition deleted"
    }

    fun selectTrack(trackId: String) {
        activeTrackId = trackId
        val t = tracks.firstOrNull { it.id == trackId }
        status = "Selected: ${t?.label ?: trackId}"
    }

    fun ensureTrackFor(category: TrackCategory) {
        val cur = tracks.firstOrNull { it.id == activeTrackId }
        if (cur?.category == category) return
        tracks.firstOrNull { it.category == category }?.let { activeTrackId = it.id }
    }

    fun gapsOf(trackId: String): List<Gap> =
        clips.filter { it.trackId == trackId }
            .sortedBy { it.startMs }
            .zipWithNext()
            .mapNotNull { (a, b) ->
                if (b.startMs - a.endMs >= MIN_GAP_MS) Gap(trackId, a.endMs, b.startMs) else null
            }

    fun selectGap(gap: Gap) {
        selectedGap = gap
        selectedId = null
        selectedTransition = null
        status = String.format(Locale.getDefault(), "Gap (%.1fs)", gap.durationMs / 1000f)
    }

    fun toggleRipple() {
        rippleOn = !rippleOn
        status = if (rippleOn) "Ripple ON" else "Ripple OFF"
    }

    fun togglePlayheadLock() {
        playheadLocked = !playheadLocked
        status = if (playheadLocked) "Playhead Locked" else "Playhead Unlocked"
    }

    fun toggleNavTrack(trackId: String) {
        navTracks = if (trackId in navTracks) navTracks - trackId else navTracks + trackId
    }

    private fun editPoints(): List<Long> =
        (clips.filter { it.trackId in navTracks }.flatMap { listOf(it.startMs, it.endMs) } + 0L)
            .distinct()
            .sorted()

    fun jumpToNextEdit() {
        if (navTracks.isEmpty()) { status = "Select a track first"; return }
        val p = editPoints().firstOrNull { it > playheadMs + 1 }
        if (p == null) { status = "No next edit point"; return }
        isPlaying = false
        playheadMs = p
        jumpTick++
        status = "Jump: ${fmt(p)}"
    }

    fun jumpToPrevEdit() {
        if (navTracks.isEmpty()) { status = "Select a track first"; return }
        val p = editPoints().lastOrNull { it < playheadMs - 1 }
        if (p == null) { status = "No prev edit point"; return }
        isPlaying = false
        playheadMs = p
        jumpTick++
        status = "Jump: ${fmt(p)}"
    }

    /** Playhead কে timeline এর একদম শুরুতে (0:00) নিয়ে যায় */
    fun goToStart() {
        isPlaying = false
        playheadMs = 0L
        jumpTick++
        status = "Home: ${fmt(0L)}"
    }

    /** Playhead কে timeline এর একদম শেষে (সর্বশেষ ক্লিপের শেষ প্রান্তে) নিয়ে যায় */
    fun goToEnd() {
        isPlaying = false
        val end = clips.maxOfOrNull { it.endMs } ?: 0L
        playheadMs = end.coerceIn(0L, totalMs)
        jumpTick++
        status = "End: ${fmt(playheadMs)}"
    }

    private fun nearestSnap(valueMs: Long, candidates: List<Long>, snapMs: Long): Long? =
        candidates.distinct().minByOrNull { abs(it - valueMs) }?.takeIf { abs(it - valueMs) <= snapMs }

    fun clearSnapGuide() { snapGuideMs = null }

    fun beginClipMove(id: Int) {
        val source = clips.firstOrNull { it.id == id } ?: return
        clipMoveBase = clips.toList()
        clipMoveOriginal = source
    }

    fun finishClipMove() {
        clipMoveBase = null
        clipMoveOriginal = null
    }

    fun moveClip(id: Int, rawStartMs: Long, snapMs: Long, targetTrackId: String? = null) {
        val baseClips = clipMoveBase ?: clips.toList()
        val c = clipMoveOriginal?.takeIf { it.id == id }
            ?: baseClips.firstOrNull { it.id == id } ?: return
        val destinationTrackId = targetTrackId ?: c.trackId
        val destinationTrack = tracks.firstOrNull { it.id == destinationTrackId } ?: return
        // Video/Overlay/Audio ক্লিপ কেবল একই category-র unlocked Track-এ যাবে।
        if (destinationTrack.category != categoryOf(c) || destinationTrack.isLocked) return

        val rawStart = rawStartMs.coerceAtLeast(0L)
        val rawEnd = rawStart + c.durationMs
        val snapPoints = buildList {
            add(0L)
            add(playheadMs)
            baseClips.filter { it.id != id }.forEach {
                add(it.startMs)
                add(it.endMs)
            }
        }
        data class SnapCandidate(val startMs: Long, val guideMs: Long, val distanceMs: Long)
        val candidates = snapPoints.flatMap { point ->
            listOf(
                SnapCandidate(point.coerceAtLeast(0L), point.coerceAtLeast(0L), abs(rawStart - point)),
                SnapCandidate((point - c.durationMs).coerceAtLeast(0L), point.coerceAtLeast(0L), abs(rawEnd - point))
            )
        }
        val nearest = candidates.minByOrNull { it.distanceMs }
        val snappedStart = if (nearest != null && nearest.distanceMs <= snapMs) nearest.startMs else rawStart

        // অন্য ক্লিপগুলো যেখানে আছে সেখানেই থাকে (ব্যবহারকারী না ছুঁলে তাদের জায়গা/গ্যাপ বদলায় না)।
        // সরানো ক্লিপের পুরনো জায়গায় ফাঁকা (gap) থেকে যায়।
        val closed = baseClips.filter { it.trackId == destinationTrackId && it.id != id }
            .sortedBy { it.startMs }
        val insertionTime = snappedStart

        val insertAt = closed.indexOfFirst { insertionTime < it.startMs }
            .let { if (it < 0) closed.size else it }
        val previousEnd = if (insertAt > 0) closed[insertAt - 1].endMs else 0L
        val insertStart = maxOf(insertionTime, previousEnd)
        val moved = c.copy(trackId = destinationTrackId, startMs = insertStart)

        // Insertion-এর পরের clip-গুলিকে কেবল overlap হলে ডানদিকে সরাই।
        var cursor = moved.endMs
        val rebuiltTrack = buildList {
            addAll(closed.take(insertAt))
            add(moved)
            closed.drop(insertAt).forEach { next ->
                val adjusted = if (next.startMs < cursor) next.copy(startMs = cursor) else next
                add(adjusted)
                cursor = adjusted.endMs
            }
        }
        val rebuiltIds = rebuiltTrack.map { it.id }.toSet()
        clips = baseClips.filterNot { it.id in rebuiltIds || it.id == id } + rebuiltTrack
        activeTrackId = destinationTrackId
        snapGuideMs = if (nearest != null && nearest.distanceMs <= snapMs) {
            if (nearest.guideMs == nearest.startMs) moved.startMs else moved.endMs
        } else null
    }

    /** rawEndMs = আঙুল যেখানে আছে (snap ছাড়া, absolute)। ফেরত দেয় সীমার ভেতরে আনা raw মান। */
    fun resizeClipEnd(id: Int, rawEndMs: Long, snapMs: Long = 0L): Long {
        val c = clips.firstOrNull { it.id == id } ?: return rawEndMs
        // একই Track-এর পরের Clip-এর শুরু পর্যন্তই ডানদিকে বাড়ানো যাবে;
        // পাশাপাশি Clip থাকলে ছোট করা যাবে, কিন্তু অন্য Clip-এর উপর দিয়ে বাড়ানো যাবে না।
        val nextClipStart = clips.filter { it.trackId == c.trackId && it.id != id && it.startMs >= c.endMs }
            .minOfOrNull { it.startMs } ?: Long.MAX_VALUE
        // সোর্স ফাইলের শেষের বাইরে বাড়ানো যাবে না (নইলে এক্সপোর্টে পরের ক্লিপ আগে চলে আসে)
        val maxDuration = minOf((nextClipStart - c.startMs).coerceAtLeast(MIN_CLIP_MS), c.maxDurationMs.coerceAtLeast(MIN_CLIP_MS))
        val rawClamped = rawEndMs.coerceIn(c.startMs + MIN_CLIP_MS, c.startMs + maxDuration)
        var targetEnd = rawClamped
        val snapCandidates = buildList {
            add(playheadMs)
            clips.filter { it.id != id }.forEach { add(it.startMs); add(it.endMs) }
        }
        val snapped = nearestSnap(targetEnd, snapCandidates, snapMs)
        if (snapped != null) targetEnd = snapped
        val newDuration = (targetEnd - c.startMs).coerceIn(MIN_CLIP_MS, maxDuration)
        snapGuideMs = if (snapped != null) c.startMs + newDuration else null
        clips = clips.map { if (it.id == id) it.copy(durationMs = newDuration) else it }
        status = String.format(Locale.getDefault(), "%s: %.1fs", c.name, newDuration / 1000f)
        return rawClamped
    }

    /** rawStartMs = আঙুল যেখানে আছে (snap ছাড়া, absolute)। ফেরত দেয় সীমার ভেতরে আনা raw মান। */
    fun resizeClipStart(id: Int, rawStartMs: Long, snapMs: Long = 0L): Long {
        val c = clips.firstOrNull { it.id == id } ?: return rawStartMs
        val prevClipEnd = clips.filter { it.trackId == c.trackId && it.id != id && it.endMs <= c.startMs }
            .maxOfOrNull { it.endMs } ?: 0L
        // বাঁ দিকে বাড়ানো: আগের ক্লিপ, টাইমলাইনের 0 এবং সোর্সের শুরু (inMs = 0) — এর মধ্যে যেটা আগে আসে
        val maxLeftShift = minOf(c.startMs - prevClipEnd, c.maxLeftExtendMs)
        val rawClamped = rawStartMs.coerceIn(c.startMs - maxLeftShift, (c.endMs - MIN_CLIP_MS).coerceAtLeast(c.startMs - maxLeftShift))
        var targetStart = rawClamped
        val snapCandidates = buildList {
            add(0L); add(playheadMs)
            clips.filter { it.id != id }.forEach { add(it.startMs); add(it.endMs) }
        }
        val snapped = nearestSnap(targetStart, snapCandidates, snapMs)
        if (snapped != null) targetStart = snapped
        val shift = (targetStart - c.startMs).coerceIn(-maxLeftShift, (c.durationMs - MIN_CLIP_MS).coerceAtLeast(-maxLeftShift))
        val newStart = c.startMs + shift
        val newDuration = c.durationMs - shift
        snapGuideMs = if (snapped != null) newStart else null
        // ভিডিও/অডিওর ক্ষেত্রে বাঁ দিক থেকে ছাঁটলে সোর্সের শুরুর বিন্দুও সরে যায়
        val newIn = if (c.textOverlay == null) (c.inMs + Math.round(shift * c.speed.toDouble())).coerceAtLeast(0L) else c.inMs
        clips = clips.map { if (it.id == id) it.copy(startMs = newStart, durationMs = newDuration, inMs = newIn) else it }
        status = String.format(Locale.getDefault(), "%s: %.1fs", c.name, newDuration / 1000f)
        return rawClamped
    }

    fun addMediaToActiveTrack(
        uri: String,
        durationMs: Long,
        customName: String? = null,
        isVideoOverlay: Boolean = false,
        category: TrackCategory? = null,
        sourceDurationMs: Long = 0L
    ) {
        if (category != null) ensureTrackFor(category)
        val currentTrack = tracks.firstOrNull { it.id == activeTrackId } ?: tracks.first()
        val count = clips.count { it.trackId == currentTrack.id } + 1
        var start = playheadMs
        val sameTrack = clips.filter { it.trackId == currentTrack.id }
        if (sameTrack.any { start < it.endMs && start + durationMs > it.startMs }) {
            start = sameTrack.maxOf { it.endMs }
        }
        val name = customName ?: "${currentTrack.label} $count"
        val clip = Clip(
            id = nextId++,
            name = name,
            trackId = currentTrack.id,
            startMs = start,
            durationMs = durationMs,
            uri = uri,
            isVideoOverlay = isVideoOverlay,
            sourceDurationMs = sourceDurationMs
        )
        commit(clips + clip)
        selectedId = clip.id
        playheadMs = start
        status = "Added to ${currentTrack.label}"
    }

    fun addTextOverlay(text: String, durationMs: Long = 3000L) {
        val currentTrack = tracks.firstOrNull { it.id == activeTrackId && it.category == TrackCategory.OVERLAY }
            ?: tracks.firstOrNull { it.category == TrackCategory.OVERLAY }
            ?: tracks.first()
        activeTrackId = currentTrack.id

        var start = playheadMs
        val sameTrack = clips.filter { it.trackId == currentTrack.id }
        if (sameTrack.any { start < it.endMs && start + durationMs > it.startMs }) {
            start = sameTrack.maxOf { it.endMs }
        }
        val clip = Clip(
            id = nextId++,
            name = "Text: \"${text.take(8)}\"",
            trackId = currentTrack.id,
            startMs = start,
            durationMs = durationMs,
            textOverlay = text
        )
        commit(clips + clip)
        selectedId = clip.id
        status = "Added text overlay"
    }

    fun toggleClipLock(id: Int) {
        val clip = clips.firstOrNull { it.id == id } ?: return
        beginChange()
        clips = clips.map { if (it.id == id) it.copy(locked = !it.locked) else it }
        status = if (!clip.locked) "Clip locked" else "Clip unlocked"
    }

    fun updateClipTransform(id: Int, scaleChange: Float, panChangeUnits: Offset) {
        if (clips.firstOrNull { it.id == id }?.locked == true) return
        clips = clips.map {
            if (it.id == id) {
                it.copy(
                    scale = (it.scale * scaleChange).coerceIn(0.2f, 5.0f),
                    offsetX = it.offsetX + panChangeUnits.x,
                    offsetY = it.offsetY + panChangeUnits.y
                )
            } else it
        }
    }
    fun updateClipOpacity(id: Int, change: Float) {
        clips = clips.map {
            if (it.id == id) it.copy(opacity = (it.opacity + change).coerceIn(0f, 1f)) else it
        }
    }

    fun resetClipTransform(id: Int) {
        if (clips.firstOrNull { it.id == id }?.locked == true) {
            status = "Clip is locked"
            return
        }
        beginChange()
        clips = clips.map {
            if (it.id == id) it.copy(opacity = 1f, scale = 1f, rotation = 0f, offsetX = 0f, offsetY = 0f) else it
        }
        status = "Transform reset"
    }
    fun rotateSelectedClip(degrees: Float) {
        val sel = selectedClip ?: return
        if (sel.locked) {
            status = "Clip is locked"
            return
        }
        beginChange()
        val newRot = (sel.rotation + degrees) % 360f
        clips = clips.map { if (it.id == sel.id) it.copy(rotation = newRot) else it }
        status = "Rotated: ${newRot.toInt()}°"
    }

    // ── Transitions ──
    private fun categoryOf(c: Clip): TrackCategory? = tracks.firstOrNull { it.id == c.trackId }?.category

    fun canHaveTransition(c: Clip): Boolean {
        val cat = categoryOf(c)
        return cat == TrackCategory.VIDEO || cat == TrackCategory.OVERLAY
    }

    /** একই ট্র্যাকে ঠিক আগের লাগোয়া ক্লিপ */
    fun prevAdjacent(c: Clip): Clip? =
        clips.filter {
            it.trackId == c.trackId && it.id != c.id && it.startMs < c.startMs &&
                    abs(it.endMs - c.startMs) <= ADJ_MS
        }.maxByOrNull { it.endMs }

    /** একই ট্র্যাকে ঠিক পরের লাগোয়া ক্লিপ */
    fun nextAdjacent(c: Clip): Clip? =
        clips.filter {
            it.trackId == c.trackId && it.id != c.id && it.startMs > c.startMs &&
                    abs(c.endMs - it.startMs) <= ADJ_MS
        }.minByOrNull { it.startMs }

    /** নির্বাচিত ক্লিপ; না থাকলে প্লেহেডের কাছের কাট-পয়েন্ট; না থাকলে প্লেহেডের নিচের ক্লিপ */
    fun transitionTargetClip(): Clip? {
        val sel = selectedClip
        if (sel != null && canHaveTransition(sel)) return sel
        val cands = clips.filter { canHaveTransition(it) }
        cands.filter { abs(it.startMs - playheadMs) <= JUNCTION_PICK_MS && prevAdjacent(it) != null }
            .minByOrNull { abs(it.startMs - playheadMs) }
            ?.let { return it }
        return cands.firstOrNull { it.trackId == activeTrackId && playheadMs in it.startMs..it.endMs }
            ?: cands.firstOrNull { playheadMs in it.startMs..it.endMs }
    }

    // ── Adjustments ──
    fun canAdjust(c: Clip): Boolean {
        val cat = categoryOf(c)
        return cat == TrackCategory.VIDEO || (cat == TrackCategory.OVERLAY && c.isVideoOverlay)
    }

    // Adjust প্যানেলের খসড়া: Apply চাপার আগে শুধু প্রিভিউতে দেখা যায়, ক্লিপে বসে না
    var adjustDraft by mutableStateOf<Pair<Int, Adjust>?>(null)

    // Filter প্যানেলের খসড়া
    var filterDraft by mutableStateOf<Pair<Int, FilterPreset>?>(null)

    // Text প্যানেলের খসড়া
    var textDraft by mutableStateOf<Pair<Int, TextStyleSpec>?>(null)

    // টেক্সট এডিট ডায়ালগ খোলার জন্য ক্লিপ id; ফন্ট তালিকা বদলালে বাড়ে
    var textEditId by mutableStateOf<Int?>(null)
    var fontsVersion by mutableIntStateOf(0)

    fun setClipText(id: Int, newText: String) {
        val t = newText.trim()
        if (t.isEmpty()) return
        val c = clips.firstOrNull { it.id == id && it.textOverlay != null } ?: return
        if (c.textOverlay == t) return
        beginChange()
        val autoName = c.name.startsWith("Text: \"")
        clips = clips.map {
            if (it.id == id) it.copy(textOverlay = t, name = if (autoName) "Text: \"${t.take(8)}\"" else it.name) else it
        }
        status = "Text updated"
    }

    fun effectiveTextStyle(c: Clip): TextStyleSpec =
        textDraft?.takeIf { it.first == c.id }?.second ?: c.textStyle

    fun applyTextDraft() {
        val d = textDraft ?: return
        beginChange()
        clips = clips.map { if (it.id == d.first) it.copy(textStyle = d.second) else it }
        status = "Text style applied"
    }

    fun applyTextStyleToAll(spec: TextStyleSpec) {
        beginChange()
        clips = clips.map { if (it.textOverlay != null) it.copy(textStyle = spec) else it }
        status = "Text style applied to all text clips"
    }

    /** ক্লিপের নিজস্ব Adjust (খসড়াসহ) — Adjust স্লাইডারের মান */
    fun draftAdj(c: Clip): Adjust =
        adjustDraft?.takeIf { it.first == c.id }?.second ?: c.adj

    fun draftFilter(c: Clip): FilterPreset =
        filterDraft?.takeIf { it.first == c.id }?.second ?: c.filter

    fun hasPending(c: Clip): Boolean = draftAdj(c) != c.adj || draftFilter(c) != c.filter

    /** প্রিভিউতে যা দেখা যায়: Filter + Adjust একসাথে */
    fun effectiveAdj(c: Clip): Adjust = combineAdjust(draftFilter(c).adjust, draftAdj(c))

    /** বাছাই করা ক্লিপে Adjust ও Filter-এর অপেক্ষমাণ খসড়া দুটোই বসানো */
    fun applyDrafts() {
        val id = adjustDraft?.first ?: filterDraft?.first ?: return
        val c = clips.firstOrNull { it.id == id } ?: return
        val newAdj = draftAdj(c)
        val newFilter = draftFilter(c)
        beginChange()
        clips = clips.map { if (it.id == id) it.copy(adj = newAdj, filter = newFilter) else it }
        status = "Applied to ${c.name}"
    }

    /** সব ভিডিও ক্লিপে একই Adjust (বাছাই করা ক্লিপের অপেক্ষমাণ ফিল্টারও সাথে বসে) */
    fun applyAdjustToAll(adj: Adjust) {
        val pf = filterDraft
        beginChange()
        clips = clips.map { c ->
            if (!canAdjust(c)) c
            else {
                var n = c.copy(adj = adj)
                if (pf != null && pf.first == c.id) n = n.copy(filter = pf.second)
                n
            }
        }
        status = "Adjustment applied to all video clips"
    }

    /** সব ভিডিও ক্লিপে একই Filter (বাছাই করা ক্লিপের অপেক্ষমাণ Adjust-ও সাথে বসে) */
    fun applyFilterToAll(f: FilterPreset) {
        val pa = adjustDraft
        beginChange()
        clips = clips.map { c ->
            if (!canAdjust(c)) c
            else {
                var n = c.copy(filter = f)
                if (pa != null && pa.first == c.id) n = n.copy(adj = pa.second)
                n
            }
        }
        status = "Filter applied to all video clips"
    }

    /** একই ট্র্যাকের লাগোয়া ক্লিপ-জোড়া (টাইমলাইনে "+" বাটন বসে) */
    fun junctionsOf(trackId: String): List<Pair<Clip, Clip>> =
        clips.filter { it.trackId == trackId }
            .sortedBy { it.startMs }
            .zipWithNext()
            .filter { (a, b) -> abs(b.startMs - a.endMs) <= ADJ_MS }

    fun openEdgeTransition(clipId: Int, atStart: Boolean) {
        transitionJoinClipId = null
        transitionPresetTarget = if (atStart) TransTarget.START else TransTarget.END
        selectedId = clipId
        selectedGap = null
        selectedTransition = null
        panel = FX_PANEL
    }

    fun openJoinTransition(laterClipId: Int) {
        transitionPresetTarget = null
        transitionJoinClipId = laterClipId
        selectedId = laterClipId
        selectedGap = null
        selectedTransition = null
        panel = FX_PANEL
    }

    fun transitionTargets(c: Clip): List<TransTarget> = buildList {
        add(TransTarget.START)
        add(TransTarget.END)
        add(TransTarget.BOTH)
        if (prevAdjacent(c) != null) add(TransTarget.JOIN_PREV)
        if (nextAdjacent(c) != null) add(TransTarget.JOIN_NEXT)
    }

    fun defaultTransitionTarget(c: Clip): TransTarget {
        val nearStart = abs(playheadMs - c.startMs)
        val nearEnd = abs(playheadMs - c.endMs)
        if (prevAdjacent(c) != null && nearStart <= JUNCTION_PICK_MS && nearStart <= nearEnd) return TransTarget.JOIN_PREV
        if (nextAdjacent(c) != null && nearEnd <= JUNCTION_PICK_MS && nearEnd < nearStart) return TransTarget.JOIN_NEXT
        return if (nearStart <= nearEnd) TransTarget.START else TransTarget.END
    }

    fun applyTransition(clipId: Int, target: TransTarget, type: TransitionType, ms: Long) {
        val c = clips.firstOrNull { it.id == clipId } ?: return
        val prev = prevAdjacent(c)
        val next = nextAdjacent(c)
        if ((target == TransTarget.JOIN_PREV && prev == null) || (target == TransTarget.JOIN_NEXT && next == null)) {
            status = "No adjacent clip there"
            return
        }
        beginChange()
        val dur = if (type == TransitionType.NONE) 0L else ms
        clips = clips.map { x ->
            when {
                x.id == c.id -> when (target) {
                    TransTarget.START, TransTarget.JOIN_PREV -> x.copy(transIn = type, transInMs = dur)
                    TransTarget.END, TransTarget.JOIN_NEXT -> x.copy(transOut = type, transOutMs = dur)
                    TransTarget.BOTH -> x.copy(transIn = type, transInMs = dur, transOut = type, transOutMs = dur)
                }
                target == TransTarget.JOIN_PREV && prev != null && x.id == prev.id ->
                    x.copy(transOut = type, transOutMs = dur)
                target == TransTarget.JOIN_NEXT && next != null && x.id == next.id ->
                    x.copy(transIn = type, transInMs = dur)
                else -> x
            }
        }
        selectedId = c.id
        selectedGap = null
        status = if (type == TransitionType.NONE) "Transition removed"
        else String.format(Locale.getDefault(), "%s added (%.1fs)", type.label, dur / 1000f)
    }

    fun trimLeft() {
        val c = selectedClip ?: run { status = "Select a clip first"; return }
        if (playheadMs <= c.startMs || playheadMs >= c.endMs - MIN_CLIP_MS) {
            status = "Move playhead inside clip"; return
        }
        val cut = playheadMs - c.startMs
        commit(clips.map {
            when {
                it.id == c.id ->
                    if (rippleOn) it.copy(durationMs = c.endMs - playheadMs, inMs = c.inMs + Math.round(cut * c.speed.toDouble()), fadeInMs = 0L, transIn = TransitionType.NONE)
                    else it.copy(startMs = playheadMs, durationMs = c.endMs - playheadMs, inMs = c.inMs + Math.round(cut * c.speed.toDouble()), fadeInMs = 0L, transIn = TransitionType.NONE)
                rippleOn && it.trackId == c.trackId && it.startMs >= c.endMs ->
                    it.copy(startMs = it.startMs - cut)
                else -> it
            }
        })
        if (rippleOn) playheadMs = c.startMs
        status = "Trimmed Left: ${c.name}"
    }

    fun trimRight() {
        val c = selectedClip ?: run { status = "Select a clip first"; return }
        if (playheadMs <= c.startMs + MIN_CLIP_MS || playheadMs >= c.endMs) {
            status = "Move playhead inside clip"; return
        }
        val cut = c.endMs - playheadMs
        commit(clips.map {
            when {
                it.id == c.id -> it.copy(durationMs = playheadMs - c.startMs, fadeOutMs = 0L, transOut = TransitionType.NONE)
                rippleOn && it.trackId == c.trackId && it.startMs >= c.endMs ->
                    it.copy(startMs = it.startMs - cut)
                else -> it
            }
        })
        status = "Trimmed Right: ${c.name}"
    }

    fun split() {
        val c = selectedClip ?: run { status = "Select a clip first"; return }
        if (playheadMs <= c.startMs + MIN_CLIP_MS || playheadMs >= c.endMs - MIN_CLIP_MS) {
            status = "Move playhead inside clip"; return
        }
        val first = c.copy(durationMs = playheadMs - c.startMs, fadeOutMs = 0L, transOut = TransitionType.NONE)
        val second = c.copy(
            id = nextId++,
            name = c.name.trimEnd('*') + "*",
            startMs = playheadMs,
            durationMs = c.endMs - playheadMs,
            inMs = c.inMs + Math.round((playheadMs - c.startMs) * c.speed.toDouble()),
            fadeInMs = 0L,
            transIn = TransitionType.NONE
        )
        commit(clips.flatMap { if (it.id == c.id) listOf(first, second) else listOf(it) })
        selectedId = second.id
        status = "Split: ${c.name}"
    }

    fun deleteSelected() {
        val tr = selectedTransition
        if (tr != null) {
            removeSelectedTransition(tr.first, tr.second)
            return
        }
        val g = selectedGap
        if (g != null) {
            selectedGap = null
            if (g !in gapsOf(g.trackId)) { status = "Gap closed"; return }
            commit(shiftAfter(clips, g.trackId, g.endMs, g.durationMs))
            status = "Gap deleted"
            return
        }
        val c = selectedClip ?: run { status = "Select clip or gap"; return }
        val remaining = clips.filterNot { it.id == c.id }
        commit(if (rippleOn) shiftAfter(remaining, c.trackId, c.endMs, c.durationMs) else remaining)
        selectedId = null
        status = if (rippleOn) "Deleted (Ripple)" else "Deleted"
    }

    fun clearAllClips() {
        beginChange()
        clips = emptyList()
        selectedId = null
        selectedGap = null
        playheadMs = 0L
        status = "Project cleared"
    }

    fun restoreProject(
        id: String,
        savedTracks: List<TrackInfo>,
        savedClips: List<Clip>,
        savedNextId: Int,
        savedPlayheadMs: Long,
        savedZoom: Float,
        savedRipple: Boolean,
        savedRatio: AspectRatio,
        savedProjectName: String
    ) {
        isPlaying = false
        projectId = id
        tracks = savedTracks.ifEmpty { defaultTracks() }
        clips = savedClips
        nextId = maxOf(savedNextId, (savedClips.maxOfOrNull { it.id } ?: 0) + 1)
        playheadMs = savedPlayheadMs.coerceAtLeast(0L)
        zoom = savedZoom
        rippleOn = savedRipple
        selectedRatio = savedRatio
        projectName = savedProjectName
        undoStack.clear()
        redoStack.clear()
        selectedId = null
        selectedGap = null
        selectedTransition = null
        transitionJoinClipId = null
        panel = null
        navTracks = tracks.map { it.id }.toSet()
        activeTrackId = tracks.firstOrNull { it.id == "V1" }?.id ?: tracks.first().id
        status = "Ready"
    }

    fun newProject(ratio: AspectRatio, name: String) {
        isPlaying = false
        projectId = "p_" + System.currentTimeMillis()
        selectedRatio = ratio
        projectName = name
        clips = emptyList()
        tracks = defaultTracks()
        navTracks = tracks.map { it.id }.toSet()
        activeTrackId = "V1"
        undoStack.clear()
        redoStack.clear()
        selectedId = null
        selectedGap = null
        selectedTransition = null
        transitionJoinClipId = null
        playheadMs = 0L
        zoom = 1f
        panel = null
        nextId = 100
        status = "New project: $name"
    }

    fun seek(ms: Long) {
        playheadMs = ms.coerceIn(0L, totalMs)
    }

    fun changeZoom(value: Float) {
        zoom = value.coerceIn(MIN_ZOOM, maxZoom)
    }

    fun zoomBy(delta: Float) {
        changeZoom(zoom + delta)
        status = String.format(Locale.getDefault(), "Zoom: %.2fx", zoom)
    }
}

private fun fmt(ms: Long): String {
    val m = ms / 60000
    val s = (ms / 1000) % 60
    val t = (ms % 1000) / 100
    return String.format(Locale.getDefault(), "%02d:%02d.%d", m, s, t)
}

// ───────────────────────── Media helpers ─────────────────────────
private fun readDurationMs(context: Context, uri: Uri): Long {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val d = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        retriever.release()
        d
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

private fun readVideoAspect(context: Context, uriString: String?): Float {
    if (uriString == null) return 16f / 9f
    return try {
        val r = MediaMetadataRetriever()
        r.setDataSource(context, uriString.toUri())
        val w = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toFloatOrNull() ?: 0f
        val h = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toFloatOrNull() ?: 0f
        val rot = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
        r.release()
        if (w <= 0f || h <= 0f) 16f / 9f
        else if (rot == 90 || rot == 270) h / w else w / h
    } catch (e: Exception) {
        e.printStackTrace()
        16f / 9f
    }
}

private fun decodeBitmapSampled(context: Context, uriString: String, maxDim: Int): Bitmap? {
    return try {
        val uri = uriString.toUri()
        fun open() = if (uri.scheme == "file") {
            File(uri.path ?: "").inputStream()
        } else {
            context.contentResolver.openInputStream(uri)
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open()?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxDim || bounds.outHeight / (sample * 2) >= maxDim) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        open()?.use { BitmapFactory.decodeStream(it, null, opts) }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun importMedia(
    context: Context,
    state: EditorState,
    uri: Uri,
    isVideoOverlay: Boolean = false,
    category: TrackCategory
) {
    try {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val duration = readDurationMs(context, uri)
    if (duration <= 0L) {
        state.status = "Could not read this file"
        return
    }
    state.addMediaToActiveTrack(
        uri.toString(), duration,
        isVideoOverlay = isVideoOverlay, category = category,
        sourceDurationMs = duration
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun rememberSyncedPlayer(
    state: EditorState,
    trackCategory: TrackCategory,
    isOverlayVideo: Boolean = false,
    trackId: String? = null   // দিলে শুধু এই ট্র্যাকের ক্লিপ চলে (প্রতি ট্র্যাকে আলাদা প্লেয়ারের জন্য)
): ExoPlayer {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(player) { onDispose { player.release() } }

    // state.tracks ভেতরেই পড়া হচ্ছে, তাই নতুন ট্র্যাক যোগ হলেও এটি সঠিক থাকে।
    // ট্র্যাকের ক্রম অনুযায়ী ওপরের ট্র্যাক আগে বিবেচিত হয়।
    val active by remember {
        derivedStateOf {
            val pos = state.playheadMs
            state.tracks.filter { it.category == trackCategory && (trackId == null || it.id == trackId) }.firstNotNullOfOrNull { t ->
                state.clips.firstOrNull {
                    it.trackId == t.id && it.uri != null && !it.missing &&
                            (!isOverlayVideo || it.isVideoOverlay) &&
                            pos >= it.startMs && pos < it.endMs
                }
            }
        }
    }

    // ভলিউম + fade (প্রিভিউতে সর্বোচ্চ 100%, 100% এর উপরের অংশ শুধু এক্সপোর্টে কাজ করে)
    LaunchedEffect(Unit) {
        snapshotFlow {
            val c = active
            val track = state.tracks.firstOrNull { it.id == c?.trackId }
            if (c == null || track?.isMuted == true) 0f
            else (c.volume * fadeGain(c, state.playheadMs - c.startMs)).coerceIn(0f, 1f)
        }.collect { v -> player.volume = v }
    }

    // একই ট্র্যাকের ঠিক পরের লাগোয়া ক্লিপ — আগে থেকে লোড করে রাখা হয় যাতে কাটার সময় থেমে না যায়
    val nextClip by remember {
        derivedStateOf {
            val c = active
            if (c == null) null
            else state.clips.filter {
                it.trackId == c.trackId && it.uri != null && !it.missing &&
                        (!isOverlayVideo || it.isVideoOverlay) &&
                        it.startMs >= c.endMs && it.startMs - c.endMs <= ADJ_MS
            }.minByOrNull { it.startMs }
        }
    }

    // ক্লিপের শুরু (inMs) MediaItem-এর ক্লিপিংয়ে বসানো — তাই প্লেয়ারের পজিশন = ক্লিপের নিজস্ব সময়
    fun keyOf(x: Clip): String = "${x.id}_${x.inMs}"

    fun itemOf(x: Clip): MediaItem = MediaItem.Builder()
        .setUri(x.uri)
        .setMediaId(keyOf(x))
        .setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder().setStartPositionMs(x.inMs).build()
        )
        .build()

    // ক্লিপ বদলে পরের ক্লিপে নিজে থেকে এগোলে সেই ক্লিপের গতি বসানো
    DisposableEffect(player) {
        val l = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val id = mediaItem?.mediaId?.substringBefore('_')?.toIntOrNull() ?: return
                val sp = state.clips.firstOrNull { it.id == id }?.speed ?: return
                player.playbackParameters = PlaybackParameters(sp, 1f)
            }
        }
        player.addListener(l)
        onDispose { player.removeListener(l) }
    }

    LaunchedEffect(active?.id, active?.uri, active?.inMs, active?.speed, nextClip?.id, nextClip?.uri, nextClip?.inMs) {
        val c = active
        if (c?.uri == null) {
            player.pause()
            player.clearMediaItems()
            return@LaunchedEffect
        }
        val n = nextClip
        val pos = srcPosMs(c, state.playheadMs)
        player.playbackParameters = PlaybackParameters(c.speed, 1f)

        fun fixTail() {
            val idx = player.currentMediaItemIndex
            val wantId = n?.let { keyOf(it) }
            val hasTail = player.mediaItemCount > idx + 1
            val tailId = if (hasTail) player.getMediaItemAt(idx + 1).mediaId else null
            if (tailId == wantId) return
            if (hasTail) player.removeMediaItems(idx + 1, player.mediaItemCount)
            if (n != null) player.addMediaItem(itemOf(n))
        }

        val curId = player.currentMediaItem?.mediaId
        val idx = player.currentMediaItemIndex
        val nextLoadedId =
            if (player.mediaItemCount > idx + 1) player.getMediaItemAt(idx + 1).mediaId else null

        when {
            // এই ক্লিপই এখন চলছে (নিজে থেকে এগিয়ে এসেছে) — শুধু লেজ ঠিক করা
            curId == keyOf(c) -> {
                if (kotlin.math.abs(player.currentPosition - pos) > 300L) player.seekTo(pos)
                fixTail()
            }
            // এই ক্লিপ আগেই লোড করা ছিল — সোজা সেখানে লাফ (থামে না)
            nextLoadedId == keyOf(c) -> {
                player.seekTo(idx + 1, pos)
                if (player.currentMediaItemIndex > 0) player.removeMediaItems(0, player.currentMediaItemIndex)
                fixTail()
            }
            // নতুন করে লোড
            else -> {
                val items = listOfNotNull(itemOf(c), n?.let { itemOf(it) })
                player.setMediaItems(items, 0, pos)
                player.prepare()
            }
        }
        player.playWhenReady = state.isPlaying
    }

    LaunchedEffect(state.isPlaying) {
        val c = active ?: return@LaunchedEffect
        player.seekTo(srcPosMs(c, state.playheadMs))
        player.playWhenReady = state.isPlaying
    }

    LaunchedEffect(Unit) {
        snapshotFlow { state.playheadMs }.collect { ms ->
            val c = active
            if (c != null) {
                if (!state.isPlaying) {
                    player.seekTo(srcPosMs(c, ms))
                } else if (player.playbackState == Player.STATE_READY && player.playWhenReady) {
                    // প্লেহেড ও ExoPlayer আলাদা ঘড়িতে চলে — বেশি সরে গেলে প্লেয়ারকে প্লেহেডের জায়গায় টেনে আনি
                    val want = srcPosMs(c, ms)
                    if (kotlin.math.abs(player.currentPosition - want) > 600L) player.seekTo(want)
                }
            }
        }
    }

    return player
}

/** একটা ক্যাটাগরির প্রতিটি ট্র্যাকের জন্য আলাদা প্লেয়ার (ট্র্যাকের তালিকা বদলালেও key দিয়ে ঠিক থাকে) */
@Composable
private fun rememberTrackPlayers(
    state: EditorState,
    category: TrackCategory,
    isOverlayVideo: Boolean = false
): Map<String, ExoPlayer> {
    val result = LinkedHashMap<String, ExoPlayer>()
    state.tracks.filter { it.category == category }.forEach { t ->
        key(t.id) {
            result[t.id] = rememberSyncedPlayer(state, category, isOverlayVideo, trackId = t.id)
        }
    }
    return result
}

// ───────────────────────── Video surface (live colour + effects) ─────────────────────────
// প্রিভিউর দানা (grain) এর জন্য আগে থেকে বানানো noise ছবি: সাদা/কালো বিন্দু, স্বচ্ছতা র‍্যান্ডম
private fun makeNoiseBitmap(w: Int, h: Int, seed: Long, maxAlpha: Float): Bitmap {
    val r = java.util.Random(seed)
    val px = IntArray(w * h)
    for (i in px.indices) {
        val g = r.nextFloat() - 0.5f                    // -0.5..0.5
        val a = (abs(g) * 2f * maxAlpha * 255f).toInt().coerceIn(0, 255)
        px[i] = (a shl 24) or (if (g > 0f) 0x00FFFFFF else 0x00000000)
    }
    val bmp = createBitmap(w, h, Bitmap.Config.ARGB_8888)
    bmp.setPixels(px, 0, w, 0, 0, w, h)
    return bmp
}

private val FINE_NOISE: ImageBitmap by lazy { makeNoiseBitmap(480, 270, 11L, 0.35f).asImageBitmap() }
private val FILM_NOISE: ImageBitmap by lazy { makeNoiseBitmap(150, 85, 23L, 0.7f).asImageBitmap() }

private fun DrawScope.drawNoise(img: ImageBitmap, strength: Float, seed: Long, quality: FilterQuality) {
    val w = size.width
    val h = size.height
    val dw = w * 1.25f
    val dh = h * 1.25f
    val r = java.util.Random(seed)
    val ox = -r.nextFloat() * (dw - w)
    val oy = -r.nextFloat() * (dh - h)
    clipRect(0f, 0f, w, h) {
        drawImage(
            image = img,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(img.width, img.height),
            dstOffset = IntOffset(ox.roundToInt(), oy.roundToInt()),
            dstSize = IntSize(dw.roundToInt(), dh.roundToInt()),
            alpha = strength.coerceIn(0f, 1f),
            filterQuality = quality
        )
    }
}

// Sharpen (প্রিভিউ): Android 13+ এ AGSL শেডার দিয়ে। এক্সপোর্টে সব ফোনেই কাজ করে।
private const val SHARPEN_AGSL = """
uniform shader inputShader;
uniform float amount;
half4 main(float2 coord) {
    half4 c = inputShader.eval(coord);
    half3 n = inputShader.eval(coord + float2(1.0, 0.0)).rgb
            + inputShader.eval(coord - float2(1.0, 0.0)).rgb
            + inputShader.eval(coord + float2(0.0, 1.0)).rgb
            + inputShader.eval(coord - float2(0.0, 1.0)).rgb;
    half3 rgb = c.rgb * (1.0 + 4.0 * amount) - amount * n;
    return half4(clamp(rgb, 0.0, 1.0), c.a);
}
"""

/** TextureView-তে ভিডিও দেখায়; রং, vignette, sharpen, grain Compose স্তরে সরাসরি বসায় — তাই স্লাইডার টানলেই সঙ্গে সঙ্গে বদলায় */
@Composable
private fun VideoSurface(
    player: ExoPlayer,
    adj: Adjust,
    modifier: Modifier = Modifier,
    frameIndex: () -> Long = { 0L }
) {
    val context = LocalContext.current
    val tv = remember { TextureView(context) }
    var aspect by remember { mutableFloatStateOf(16f / 9f) }

    DisposableEffect(player, tv) {
        fun refresh(v: VideoSize) {
            if (v.width > 0 && v.height > 0) aspect = v.width * v.pixelWidthHeightRatio / v.height
        }
        refresh(player.videoSize)
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                refresh(videoSize)
            }
        }
        player.addListener(listener)
        player.setVideoTextureView(tv)
        onDispose {
            player.removeListener(listener)
            player.clearVideoTextureView(tv)
        }
    }

    val filter = remember(adj.brightness, adj.contrast, adj.saturation, adj.temperature, adj.tint, adj.hue) {
        if (adj.colorDefault) null else ColorFilter.colorMatrix(ColorMatrix(buildAdjustColorMatrix(adj)))
    }

    val sharpenEffect = remember(adj.sharpen) {
        if (adj.sharpen <= 0f || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            null
        } else {
            try {
                val sh = android.graphics.RuntimeShader(SHARPEN_AGSL)
                sh.setFloatUniform("amount", adj.sharpen * 0.5f)
                android.graphics.RenderEffect.createRuntimeShaderEffect(sh, "inputShader").asComposeRenderEffect()
            } catch (e: Throwable) {
                null
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { renderEffect = sharpenEffect },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { tv },
            modifier = Modifier
                .aspectRatio(aspect.coerceIn(0.1f, 10f))
                .drawWithContent {
                    val f = filter
                    if (f == null) {
                        drawContent()
                    } else {
                        drawIntoCanvas { canvas ->
                            val paint = ComposePaint().apply { colorFilter = f }
                            canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
                            drawContent()
                            canvas.restore()
                        }
                    }

                    val w = size.width
                    val h = size.height
                    if (adj.vignette > 0f) {
                        val a = (adj.vignette * 0.9f).coerceIn(0f, 1f)
                        drawRect(
                            brush = Brush.radialGradient(
                                0.00f to Color.Transparent,
                                0.35f to Color.Transparent,
                                0.50f to Color.Black.copy(alpha = a * 0.13f),
                                0.65f to Color.Black.copy(alpha = a * 0.44f),
                                0.80f to Color.Black.copy(alpha = a * 0.77f),
                                1.00f to Color.Black.copy(alpha = a),
                                center = Offset(w / 2f, h / 2f),
                                radius = kotlin.math.hypot(w, h) / 2f
                            )
                        )
                    }
                    if (adj.grain > 0f) {
                        drawNoise(FINE_NOISE, adj.grain, frameIndex(), FilterQuality.None)
                    }
                    if (adj.filmGrain > 0f) {
                        drawNoise(FILM_NOISE, adj.filmGrain, frameIndex() / 2L + 7919L, FilterQuality.Low)
                    }
                }
        )
    }
}

// ───────────────────────── Audio waveform ─────────────────────────
private const val PEAKS_PER_SEC = 50

private object WaveformCache {
    private val memory = ConcurrentHashMap<String, FloatArray>()
    private val locks = ConcurrentHashMap<String, Any>()

    fun peek(uri: String): FloatArray? = memory[uri]

    /** ব্যাকগ্রাউন্ড থ্রেডে চালাতে হবে। অডিও না থাকলে খালি অ্যারে ফেরত দেয়। */
    fun load(context: Context, uri: String, shouldContinue: () -> Boolean): FloatArray? {
        memory[uri]?.let { return it }
        val lock = locks.getOrPut(uri) { Any() }
        synchronized(lock) {
            memory[uri]?.let { return it }

            val dir = File(context.cacheDir, "waveforms").apply { mkdirs() }
            val cacheFile = File(dir, uri.hashCode().toUInt().toString(16) + ".bin")
            if (cacheFile.exists()) {
                try {
                    val bytes = cacheFile.readBytes()
                    val arr = FloatArray(bytes.size) { (bytes[it].toInt() and 0xFF) / 255f }
                    memory[uri] = arr
                    return arr
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val peaks = decodePeaks(context, uri, shouldContinue) ?: return null
            memory[uri] = peaks
            try {
                cacheFile.writeBytes(ByteArray(peaks.size) { (peaks[it].coerceIn(0f, 1f) * 255f).roundToInt().toByte() })
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return peaks
        }
    }

    private fun decodePeaks(context: Context, uriString: String, shouldContinue: () -> Boolean): FloatArray? {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            extractor.setDataSource(context, uriString.toUri(), null)
            var trackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                    trackIndex = i
                    format = f
                    break
                }
            }
            if (trackIndex < 0 || format == null) return FloatArray(0) // অডিও ট্র্যাক নেই

            extractor.selectTrack(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return FloatArray(0)
            val dec = MediaCodec.createDecoderByType(mime)
            codec = dec
            dec.configure(format, null, null, 0)
            dec.start()

            var sampleRate = if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) format.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 44100
            var channels = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 2
            var isFloat = false
            var framesPerBucket = (sampleRate / PEAKS_PER_SEC).coerceAtLeast(1)

            var arr = FloatArray(4096)
            var n = 0
            var framesInBucket = 0
            var bucketMax = 0f

            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false

            while (!outputDone) {
                if (!shouldContinue()) return null

                if (!inputDone) {
                    val inIdx = dec.dequeueInputBuffer(10_000)
                    if (inIdx >= 0) {
                        val buf = dec.getInputBuffer(inIdx)
                        val size = if (buf != null) extractor.readSampleData(buf, 0) else -1
                        if (size < 0) {
                            dec.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            dec.queueInputBuffer(inIdx, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outIdx = dec.dequeueOutputBuffer(info, 10_000)
                if (outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val of = dec.outputFormat
                    if (of.containsKey(MediaFormat.KEY_SAMPLE_RATE)) sampleRate = of.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    if (of.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) channels = of.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    framesPerBucket = (sampleRate / PEAKS_PER_SEC).coerceAtLeast(1)
                    isFloat = of.containsKey(MediaFormat.KEY_PCM_ENCODING) &&
                            of.getInteger(MediaFormat.KEY_PCM_ENCODING) == android.media.AudioFormat.ENCODING_PCM_FLOAT
                } else if (outIdx >= 0) {
                    val out = dec.getOutputBuffer(outIdx)
                    if (out != null && info.size > 0) {
                        out.position(info.offset)
                        out.limit(info.offset + info.size)
                        out.order(ByteOrder.nativeOrder())
                        val ch = channels.coerceAtLeast(1)
                        if (isFloat) {
                            val fb = out.asFloatBuffer()
                            while (fb.remaining() >= ch) {
                                var m = 0f
                                for (c in 0 until ch) {
                                    val v = abs(fb.get()).coerceAtMost(1f)
                                    if (v > m) m = v
                                }
                                if (m > bucketMax) bucketMax = m
                                if (++framesInBucket >= framesPerBucket) {
                                    if (n == arr.size) arr = arr.copyOf(n * 2)
                                    arr[n++] = bucketMax
                                    bucketMax = 0f
                                    framesInBucket = 0
                                }
                            }
                        } else {
                            val sb = out.asShortBuffer()
                            while (sb.remaining() >= ch) {
                                var m = 0f
                                for (c in 0 until ch) {
                                    val v = abs(sb.get().toInt()) / 32768f
                                    if (v > m) m = v
                                }
                                if (m > bucketMax) bucketMax = m
                                if (++framesInBucket >= framesPerBucket) {
                                    if (n == arr.size) arr = arr.copyOf(n * 2)
                                    arr[n++] = bucketMax
                                    bucketMax = 0f
                                    framesInBucket = 0
                                }
                            }
                        }
                    }
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                    dec.releaseOutputBuffer(outIdx, false)
                }
            }
            if (framesInBucket > 0) {
                if (n == arr.size) arr = arr.copyOf(n + 1)
                arr[n++] = bucketMax
            }
            return arr.copyOf(n)
        } catch (e: Exception) {
            e.printStackTrace()
            return FloatArray(0)
        } finally {
            try { codec?.stop() } catch (_: Exception) {}
            try { codec?.release() } catch (_: Exception) {}
            try { extractor.release() } catch (_: Exception) {}
        }
    }
}

@Composable
private fun rememberWaveform(uri: String?): FloatArray? {
    val context = LocalContext.current
    var peaks by remember(uri) { mutableStateOf(if (uri != null) WaveformCache.peek(uri) else null) }
    LaunchedEffect(uri) {
        if (uri != null && peaks == null) {
            peaks = withContext(Dispatchers.IO) {
                val ctx = currentCoroutineContext()
                WaveformCache.load(context, uri) { ctx.isActive }
            }
        }
    }
    return peaks
}

// ───────────────────────── Vignette / Sharpen / Grain (export, GL shader) ─────────────────────────
private const val FX_VERTEX_SHADER = """
attribute vec4 aFramePosition;
varying vec2 vTexSamplingCoord;
void main() {
  gl_Position = aFramePosition;
  vTexSamplingCoord = vec2(aFramePosition.x * 0.5 + 0.5, aFramePosition.y * 0.5 + 0.5);
}
"""

private const val FX_FRAGMENT_SHADER = """
precision highp float;
uniform sampler2D uTexSampler;
uniform vec2 uRes;
uniform float uVignette;
uniform float uSharpen;
uniform float uGrain;
uniform float uFilm;
uniform float uSeed;
uniform float uFilmSeed;
varying vec2 vTexSamplingCoord;

float hash(vec2 p) {
  p = fract(p * vec2(123.34, 456.21));
  p += dot(p, p + 45.32);
  return fract(p.x * p.y);
}

float vnoise(vec2 p) {
  vec2 i = floor(p);
  vec2 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  float a = hash(i);
  float b = hash(i + vec2(1.0, 0.0));
  float c = hash(i + vec2(0.0, 1.0));
  float d = hash(i + vec2(1.0, 1.0));
  return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

void main() {
  vec2 uv = vTexSamplingCoord;
  vec4 color = texture2D(uTexSampler, uv);
  vec3 rgb = color.rgb;

  if (uSharpen > 0.0) {
    vec2 t = 1.0 / uRes;
    vec3 n = texture2D(uTexSampler, uv + vec2(t.x, 0.0)).rgb
           + texture2D(uTexSampler, uv - vec2(t.x, 0.0)).rgb
           + texture2D(uTexSampler, uv + vec2(0.0, t.y)).rgb
           + texture2D(uTexSampler, uv - vec2(0.0, t.y)).rgb;
    float a = uSharpen * 0.5;
    rgb = rgb * (1.0 + 4.0 * a) - a * n;
  }

  if (uVignette > 0.0) {
    vec2 p = (uv - 0.5) * uRes;
    float r = length(p) / (0.5 * length(uRes));
    float v = smoothstep(0.35, 1.0, r);
    rgb *= 1.0 - uVignette * 0.9 * v;
  }

  if (uGrain > 0.0) {
    float g = hash(floor(uv * uRes) + vec2(uSeed * 13.7, uSeed * 7.3)) - 0.5;
    rgb += g * uGrain * 0.35;
  }

  if (uFilm > 0.0) {
    float cell = max(1.0, uRes.y / 180.0);
    float f = vnoise(uv * uRes / cell + vec2(uFilmSeed * 17.0, uFilmSeed * 5.0)) - 0.5;
    float lum = dot(rgb, vec3(0.299, 0.587, 0.114));
    float w = 1.0 - abs(lum * 2.0 - 1.0) * 0.6;
    rgb += f * uFilm * 0.6 * w;
  }

  gl_FragColor = vec4(clamp(rgb, 0.0, 1.0), color.a);
}
"""

@OptIn(UnstableApi::class)
private fun createFxProgram(): GlProgram {
    try {
        val p = GlProgram(FX_VERTEX_SHADER, FX_FRAGMENT_SHADER)
        p.setBufferAttribute(
            "aFramePosition",
            GlUtil.getNormalizedCoordinateBounds(),
            GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE
        )
        return p
    } catch (e: Exception) {
        throw VideoFrameProcessingException(e)
    }
}

@OptIn(UnstableApi::class)
private class AdjustFxShaderProgram(
    private val adj: Adjust,
    useHdr: Boolean
) : BaseGlShaderProgram(useHdr, 1) {
    private val program: GlProgram = createFxProgram()
    private var frameW = 1f
    private var frameH = 1f

    override fun configure(inputWidth: Int, inputHeight: Int): MediaSize {
        frameW = inputWidth.toFloat()
        frameH = inputHeight.toFloat()
        return MediaSize(inputWidth, inputHeight)
    }

    override fun drawFrame(inputTexId: Int, presentationTimeUs: Long) {
        try {
            program.use()
            program.setSamplerTexIdUniform("uTexSampler", inputTexId, 0)
            program.setFloatsUniform("uRes", floatArrayOf(frameW, frameH))
            program.setFloatUniform("uVignette", adj.vignette)
            program.setFloatUniform("uSharpen", adj.sharpen)
            program.setFloatUniform("uGrain", adj.grain)
            program.setFloatUniform("uFilm", adj.filmGrain)
            program.setFloatUniform("uSeed", ((presentationTimeUs / 16667L) % 997L).toFloat())
            program.setFloatUniform("uFilmSeed", ((presentationTimeUs / 41667L) % 997L).toFloat())
            program.bindAttributesAndUniforms()
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        } catch (e: Exception) {
            throw VideoFrameProcessingException(e)
        }
    }

    override fun release() {
        super.release()
        try {
            program.delete()
        } catch (e: Exception) {
            throw VideoFrameProcessingException(e)
        }
    }
}

@OptIn(UnstableApi::class)
private class AdjustFxEffect(private val adj: Adjust) : GlEffect {
    override fun toGlShaderProgram(context: Context, useHdr: Boolean): GlShaderProgram =
        AdjustFxShaderProgram(adj, useHdr)
}

// ───────────────────────── Audio gain + fade processor (export) ─────────────────────────
@OptIn(UnstableApi::class)
private class GainAudioProcessor(
    private val volume: Float,
    private val fadeInMs: Long,
    private val fadeOutMs: Long,
    private val durationMs: Long
) : BaseAudioProcessor() {
    private var sampleRate = 44100
    private var channels = 2
    private var isFloat = false
    private var framesDone = 0L

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        when (inputAudioFormat.encoding) {
            C.ENCODING_PCM_16BIT -> isFloat = false
            C.ENCODING_PCM_FLOAT -> isFloat = true
            else -> throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        sampleRate = inputAudioFormat.sampleRate.coerceAtLeast(1)
        channels = inputAudioFormat.channelCount.coerceAtLeast(1)
        framesDone = 0L
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val bytesPerSample = if (isFloat) 4 else 2
        val frameBytes = bytesPerSample * channels
        val frames = inputBuffer.remaining() / frameBytes
        if (frames == 0) {
            inputBuffer.position(inputBuffer.limit())
            return
        }
        val out = replaceOutputBuffer(frames * frameBytes)
        repeat(frames) {
            val tMs = framesDone * 1000.0 / sampleRate
            var fade = 1.0
            if (fadeInMs > 0 && tMs < fadeInMs) fade = minOf(fade, tMs / fadeInMs)
            val remain = durationMs - tMs
            if (fadeOutMs > 0 && remain < fadeOutMs) fade = minOf(fade, remain.coerceAtLeast(0.0) / fadeOutMs)
            val g = (volume * fade).toFloat()
            repeat(channels) {
                if (isFloat) {
                    out.putFloat(inputBuffer.getFloat() * g)
                } else {
                    val s = (inputBuffer.getShort() * g).toInt().coerceIn(-32768, 32767)
                    out.putShort(s.toShort())
                }
            }
            framesDone++
        }
        inputBuffer.position(inputBuffer.limit())
        out.flip()
    }
}

// ───────────────────────── Styled text (preview) ─────────────────────────
// renderOverlayBitmap()-এর সাথে একই TextStyleSpec / TextPx ব্যবহার করে।
@Composable
private fun StyledTextOverlay(text: String, spec: TextStyleSpec, unit: Float) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val family = remember(spec.font, spec.customFont) {
        spec.customFont?.let { CustomFonts.family(context, it) } ?: spec.font.family
    }
    val px = remember(spec, unit) { TextPx(spec, unit) }
    val shadowObj = if (spec.shadow) Shadow(Color(spec.shadowColor), Offset(px.dx, px.dy), px.blur) else null
    val base = TextStyle(
        fontFamily = family,
        fontWeight = if (spec.bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (spec.italic) FontStyle.Italic else FontStyle.Normal,
        letterSpacing = spec.spacing.em,
        fontSize = with(density) { px.font.toSp() }
    )
    val bgMod = if (spec.bg) {
        Modifier.background(Color(px.bgArgb), RoundedCornerShape(with(density) { px.radius.toDp() }))
    } else Modifier
    Box(
        modifier = bgMod.padding(
            horizontal = with(density) { px.padH.toDp() },
            vertical = with(density) { px.padV.toDp() }
        )
    ) {
        if (spec.outline) {
            Text(
                text = text,
                style = base.copy(
                    color = Color(spec.outlineColor),
                    drawStyle = DrawStroke(width = px.stroke, join = StrokeJoin.Round),
                    shadow = shadowObj
                ),
                maxLines = 1,
                softWrap = false
            )
        }
        Text(
            text = text,
            style = base.copy(
                color = Color(spec.color),
                textDecoration = if (spec.underline) TextDecoration.Underline else null,
                shadow = if (spec.outline) null else shadowObj
            ),
            maxLines = 1,
            softWrap = false
        )
    }
}

// ───────────────────────── Overlay bitmap rendering (export) ─────────────────────────
// পুরো আউটপুট ফ্রেমের সমান একটি ট্রান্সপারেন্ট বিটম্যাপে আইটেমটি
// প্রিভিউর মতো একই পজিশন / স্কেল / রোটেশনে আঁকা হয়।
private fun renderOverlayBitmap(context: Context, clip: Clip, outW: Int, outH: Int): Bitmap? {
    val unit = outH.toFloat()
    val bmp = createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bmp)

    canvas.save()
    canvas.translate(outW / 2f + clip.offsetX * unit, outH / 2f + clip.offsetY * unit)
    canvas.rotate(clip.rotation)
    canvas.scale(clip.scale, clip.scale)

    var ok = false
    if (clip.textOverlay != null) {
        val text = clip.textOverlay
        val spec = clip.textStyle
        val px = TextPx(spec, unit)
        val typefaceStyle = when {
            spec.bold && spec.italic -> Typeface.BOLD_ITALIC
            spec.bold -> Typeface.BOLD
            spec.italic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        // কাস্টম ফন্ট থাকলে সেটি যেমন আছে তেমনই (প্রিভিউর সাথে মেলাতে) — না থাকলে সিস্টেম ফন্ট
        val custom = spec.customFont?.let { CustomFonts.typeface(context, it) }
        val face = custom ?: Typeface.create(spec.font.face, typefaceStyle)
        fun newPaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = px.font
            typeface = face
            letterSpacing = spec.spacing
        }
        val fill = newPaint().apply {
            color = spec.color
            isUnderlineText = spec.underline
        }
        val fm = fill.fontMetrics
        val textW = fill.measureText(text)
        val textH = fm.descent - fm.ascent
        val rect = RectF(-textW / 2f - px.padH, -textH / 2f - px.padV, textW / 2f + px.padH, textH / 2f + px.padV)
        if (spec.bg) {
            val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = px.bgArgb }
            canvas.drawRoundRect(rect, px.radius, px.radius, bg)
        }
        val baseY = -textH / 2f - fm.ascent
        // নিচের স্তর: outline থাকলে সেটি (শ্যাডোসহ), না থাকলে শুধু fill-এ শ্যাডো
        if (spec.outline) {
            val sp = newPaint().apply {
                style = Paint.Style.STROKE
                strokeWidth = px.stroke
                strokeJoin = Paint.Join.ROUND
                color = spec.outlineColor
                if (spec.shadow) setShadowLayer(px.blur, px.dx, px.dy, spec.shadowColor)
            }
            canvas.drawText(text, -textW / 2f, baseY, sp)
        } else if (spec.shadow) {
            fill.setShadowLayer(px.blur, px.dx, px.dy, spec.shadowColor)
        }
        canvas.drawText(text, -textW / 2f, baseY, fill)
        ok = true
    } else if (clip.uri != null) {
        val src = decodeBitmapSampled(context, clip.uri, 2048)
        if (src != null) {
            val side = IMG_BOX * unit
            val s = minOf(side / src.width, side / src.height)
            val dw = src.width * s
            val dh = src.height * s
            val p = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
            canvas.drawBitmap(src, null, RectF(-dw / 2f, -dh / 2f, dw / 2f, dh / 2f), p)
            src.recycle()
            ok = true
        }
    }
    canvas.restore()
    if (!ok) {
        bmp.recycle()
        return null
    }
    return bmp
}

// ───────────────────────── Multi-layer Export (MP4) ─────────────────────────
private class SeqMeta(val isOverlay: Boolean, val clips: List<Clip>)

@OptIn(UnstableApi::class)
class ExportManager(private val context: Context, private val state: EditorState) {
    var running by mutableStateOf(false)
        private set
    var progress by mutableFloatStateOf(0f)
        private set

    private val mainHandler = Handler(Looper.getMainLooper())
    private val progressHolder = ProgressHolder()
    private var transformer: Transformer? = null
    private var currentFile: File? = null

    private val progressTicker = object : Runnable {
        override fun run() {
            val t = transformer ?: return
            if (t.getProgress(progressHolder) == Transformer.PROGRESS_STATE_AVAILABLE) {
                progress = progressHolder.progress / 100f
                state.status = "Exporting ${progressHolder.progress}%"
            }
            if (running) mainHandler.postDelayed(this, 300)
        }
    }

    fun start(outputHeight: Int) {
        if (running) return
        val trackById = state.tracks.associateBy { it.id }

        // ভিডিও/অডিও ট্র্যাকের ক্লিপ + ওভারলে ট্র্যাকের ভিডিও ক্লিপ
        val exportable = state.clips.filter { c ->
            val t = trackById[c.trackId] ?: return@filter false
            c.uri != null && !c.missing && when (t.category) {
                TrackCategory.VIDEO, TrackCategory.AUDIO -> true
                TrackCategory.OVERLAY -> c.isVideoOverlay
            }
        }
        if (exportable.isEmpty()) {
            state.status = "Nothing to export"
            return
        }
        state.isPlaying = false

        val t0 = exportable.minOf { it.startMs }
        val outputWidth = ((outputHeight * state.selectedRatio.ratio) / 2).toInt() * 2

        // সিকোয়েন্সের ক্রম: নিচের ভিডিও আগে, ওপরের পরে, তারপর ওভারলে ভিডিও, শেষে শুধু অডিও।
        // (ভিডিও সিকোয়েন্সগুলো তালিকার শুরুতে থাকে বলে compositor-এর inputId ঠিক থাকে)
        val sequences = mutableListOf<EditedMediaItemSequence>()
        val metas = mutableListOf<SeqMeta>()

        state.tracks.filter { it.category == TrackCategory.VIDEO }.reversed().forEach { track ->
            buildSequence(exportable, track, t0)?.let { sequences.add(it); metas.add(SeqMeta(false, emptyList())) }
        }
        state.tracks.filter { it.category == TrackCategory.OVERLAY }.reversed().forEach { track ->
            val cl = exportable.filter { it.trackId == track.id }
            buildSequence(exportable, track, t0)?.let { sequences.add(it); metas.add(SeqMeta(true, cl)) }
        }
        state.tracks.filter { it.category == TrackCategory.AUDIO }.forEach { track ->
            buildSequence(exportable, track, t0)?.let { sequences.add(it) }
        }
        if (sequences.isEmpty()) {
            state.status = "Nothing to export"
            return
        }

        // ইমেজ ও টেক্সট ওভারলে
        val overlayTrackOrder = state.tracks.filter { it.category == TrackCategory.OVERLAY }.map { it.id }
        val overlayTrackIds = overlayTrackOrder.toSet()
        // স্তরক্রম: নিচের ট্র্যাক আগে আঁকা, ওপরের ট্র্যাক পরে (উপরে); একই ট্র্যাকে id ধরে — প্রিভিউর সাথে একই নিয়ম
        val staticOverlayClips = state.clips.filter {
            it.trackId in overlayTrackIds && !it.isVideoOverlay && !it.missing && (it.uri != null || it.textOverlay != null)
        }.sortedWith(
            compareByDescending<Clip> { overlayTrackOrder.indexOf(it.trackId) }.thenBy { it.id }
        )

        val empty = createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val shownSettings = StaticOverlaySettings.Builder().build()
        val hiddenSettings = StaticOverlaySettings.Builder().setAlphaScale(0f).build()

        val textureOverlays = mutableListOf<TextureOverlay>()
        for (clip in staticOverlayClips) {
            var cached: Bitmap? = null
            var failed = false
            val ctx = context
            textureOverlays.add(object : BitmapOverlay() {
                private fun isActive(us: Long): Boolean {
                    val ms = t0 + us / 1000L
                    return ms >= clip.startMs && ms < clip.endMs
                }

                override fun getBitmap(presentationTimeUs: Long): Bitmap {
                    if (!isActive(presentationTimeUs) || failed) return empty
                    val existing = cached
                    if (existing != null) return existing
                    val made = renderOverlayBitmap(ctx, clip, outputWidth, outputHeight)
                    if (made == null) {
                        failed = true
                        return empty
                    }
                    cached = made
                    return made
                }

                override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
                    if (!isActive(presentationTimeUs) || failed) return hiddenSettings
                    val fx = clipFx(clip, t0 + presentationTimeUs / 1000L - clip.startMs)
                    return if (fx.isIdentity && clip.opacity >= 1f) shownSettings else staticFxSettings(fx, clip.opacity)
                }
            })
        }

        val compositionEffects = mutableListOf<Effect>()
        compositionEffects.add(
            Presentation.createForWidthAndHeight(outputWidth, outputHeight, Presentation.LAYOUT_SCALE_TO_FIT)
        )

        // ভিডিও ট্র্যাকের ট্রান্সিশন (ফেড / ফ্ল্যাশ / জুম / স্লাইড) — টাইমলাইনের সময় ধরে পুরো ফ্রেমে
        val videoTrackOrder = state.tracks.filter { it.category == TrackCategory.VIDEO }.map { it.id }
        val videoClipsTopFirst = exportable
            .filter { it.trackId in videoTrackOrder }
            .sortedBy { videoTrackOrder.indexOf(it.trackId) }
        if (videoClipsTopFirst.any { it.transIn != TransitionType.NONE || it.transOut != TransitionType.NONE }) {
            fun videoFxAt(us: Long): TransFx {
                val ms = t0 + us / 1000L
                val c = videoClipsTopFirst.firstOrNull { ms >= it.startMs && ms < it.endMs } ?: return NO_FX
                return clipFx(c, ms - c.startMs)
            }

            compositionEffects.add(object : MatrixTransformation {
                override fun getMatrix(presentationTimeUs: Long): Matrix {
                    val fx = videoFxAt(presentationTimeUs)
                    val m = Matrix()
                    if (fx.scale != 1f) m.postScale(fx.scale, fx.scale)
                    if (fx.tx != 0f || fx.ty != 0f) m.postTranslate(fx.tx * 2f, -fx.ty * 2f)
                    return m
                }
            })

            compositionEffects.add(object : RgbMatrix {
                override fun getMatrix(presentationTimeUs: Long, useHdr: Boolean): FloatArray {
                    val fx = videoFxAt(presentationTimeUs)
                    val k = (1f - fx.darken) * (1f - fx.whiten)
                    val w = fx.whiten
                    // 4x4 কলাম-মেজর: শেষ কলাম = ধ্রুবক যোগ (সাদা ফ্ল্যাশের জন্য)
                    return floatArrayOf(
                        k, 0f, 0f, 0f,
                        0f, k, 0f, 0f,
                        0f, 0f, k, 0f,
                        w, w, w, 1f
                    )
                }
            })
        }

        if (textureOverlays.isNotEmpty()) {
            compositionEffects.add(OverlayEffect(ImmutableList.copyOf(textureOverlays)))
        }

        val compositionBuilder = Composition.Builder(sequences)
            .setEffects(Effects(emptyList(), compositionEffects))

        // ওভারলে ভিডিওর পজিশন / সাইজ / রোটেশন (compositor)
        if (metas.any { it.isOverlay }) {
            val aspects = HashMap<Int, Float>()
            metas.filter { it.isOverlay }.flatMap { it.clips }.forEach { aspects[it.id] = readVideoAspect(context, it.uri) }

            compositionBuilder.setVideoCompositorSettings(object : VideoCompositorSettings {
                override fun getOutputSize(inputSizes: List<MediaSize>): MediaSize =
                    MediaSize(outputWidth, outputHeight)

                override fun getOverlaySettings(inputId: Int, presentationTimeUs: Long): OverlaySettings {
                    val meta = metas.getOrNull(inputId)
                    if (meta == null || !meta.isOverlay) return StaticOverlaySettings.Builder().build()
                    val ms = t0 + presentationTimeUs / 1000L
                    val clip = meta.clips.firstOrNull { ms >= it.startMs && ms < it.endMs }
                        ?: return hiddenSettings

                    val aspect = aspects[clip.id] ?: (16f / 9f)
                    val boxW = VID_W * outputHeight * clip.scale
                    val boxH = VID_H * outputHeight * clip.scale
                    val w: Float
                    val h: Float
                    if (aspect > boxW / boxH) { w = boxW; h = boxW / aspect } else { h = boxH; w = boxH * aspect }

                    val fx = clipFx(clip, ms - clip.startMs)
                    val sw = w * fx.scale / outputWidth
                    val sh = h * fx.scale / outputHeight
                    val axTotal = clip.offsetX * outputHeight / (outputWidth / 2f) + fx.tx * 2f
                    val ayTotal = -clip.offsetY * outputHeight / (outputHeight / 2f) - fx.ty * 2f
                    val ax = splitAnchor(axTotal, sw)
                    val ay = splitAnchor(ayTotal, sh)
                    val alpha = ((1f - fx.darken) * (1f - fx.whiten) * clip.opacity).coerceIn(0f, 1f)
                    return StaticOverlaySettings.Builder()
                        .setScale(sw, sh)
                        .setAlphaScale(alpha)
                        .setRotationDegrees(-clip.rotation)
                        .setBackgroundFrameAnchor(ax.bg, ay.bg)
                        .setOverlayFrameAnchor(ax.ov, ay.ov)
                        .build()
                }
            })
        }

        val composition = compositionBuilder.build()

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "MasterCut_$stamp.mp4")
        currentFile = file

        val listener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, result: ExportResult) {
                finishRunning()
                progress = 1f
                state.status = "Export done - saving..."
                saveToGallery(file)
            }

            override fun onError(composition: Composition, result: ExportResult, exception: ExportException) {
                finishRunning()
                exception.printStackTrace()
                state.status = "Export error: " + (exception.message ?: "unknown")
                file.delete()
            }
        }

        try {
            val t = Transformer.Builder(context).addListener(listener).build()
            transformer = t
            running = true
            progress = 0f
            state.status = "Exporting 0%"
            t.start(composition, file.absolutePath)
            mainHandler.postDelayed(progressTicker, 300)
        } catch (e: Exception) {
            e.printStackTrace()
            finishRunning()
            state.status = "Export error: " + (e.message ?: "unknown")
        }
    }

    fun cancel() {
        if (!running) return
        transformer?.cancel()
        finishRunning()
        currentFile?.delete()
        state.status = "Export cancelled"
    }

    private fun finishRunning() {
        running = false
        mainHandler.removeCallbacks(progressTicker)
        transformer = null
    }

    private fun buildSequence(clips: List<Clip>, track: TrackInfo, t0: Long): EditedMediaItemSequence? {
        val sorted = clips.filter { it.trackId == track.id }.sortedBy { it.startMs }
        if (sorted.isEmpty()) return null

        val trackTypes =
            if (track.category == TrackCategory.AUDIO) setOf(C.TRACK_TYPE_AUDIO)
            else setOf(C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_VIDEO)
        val builder = EditedMediaItemSequence.Builder(trackTypes)

        var cursor = t0
        var added = 0
        for (c in sorted) {
            // নীরব (mute / volume 0) অডিও-ট্র্যাক ক্লিপ: setRemoveVideo + setRemoveAudio একসাথে দিলে Media3 এরর দেয়।
            // তাই এটা বাদ দিই — cursor না সরালে পরের আসল ক্লিপের আগে গ্যাপ নিজে থেকেই বসে (শেষে গ্যাপ থাকে না)।
            if (track.category == TrackCategory.AUDIO && (track.isMuted || c.volume <= 0f)) continue
            val gapMs = c.startMs - cursor
            if (gapMs >= MIN_GAP_MS) builder.addGap(gapMs * 1000L)
            val mediaItem = MediaItem.Builder()
                .setUri(c.uri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(c.inMs)
                        .setEndPositionMs(c.inMs + c.sourceSpanMs)
                        .build()
                )
                .build()
            val editedBuilder = EditedMediaItem.Builder(mediaItem)
            if (track.category == TrackCategory.AUDIO) editedBuilder.setRemoveVideo(true)

            val finalVol = if (track.isMuted) 0f else c.volume
            val audioProcessors = mutableListOf<AudioProcessor>()
            if (finalVol <= 0f) {
                editedBuilder.setRemoveAudio(true)
            } else if (finalVol != 1f || c.fadeInMs > 0 || c.fadeOutMs > 0) {
                // প্রসেসর সোর্স-সময়ে চলে (গতি-প্রসেসরের আগে), তাই ফেড ও দৈর্ঘ্য speed দিয়ে গুণ করে সোর্স-সময়ে পাঠাই
                val sp = c.speed.toDouble()
                audioProcessors.add(
                    GainAudioProcessor(
                        finalVol,
                        Math.round(c.fadeInMs * sp),
                        Math.round(c.fadeOutMs * sp),
                        c.sourceSpanMs
                    )
                )
            }

            // ভিডিও ট্র্যাকের ক্লিপ ঘোরানো (প্রিভিউর রোটেশন এক্সপোর্টেও প্রয়োগ হবে)
            val videoEffects = mutableListOf<Effect>()
            if (track.category == TrackCategory.VIDEO && c.rotation % 360f != 0f) {
                videoEffects.add(
                    ScaleAndRotateTransformation.Builder().setRotationDegrees(-c.rotation).build()
                )
            }
            // রং-অ্যাডজাস্টমেন্ট (ভিডিও ও PiP ভিডিও ক্লিপ)
            val fxAdj = combineAdjust(c.filter.adjust, c.adj)
            if (track.category != TrackCategory.AUDIO && !fxAdj.colorDefault) {
                val adjMatrix = buildAdjustMatrix(fxAdj)
                videoEffects.add(object : RgbMatrix {
                    override fun getMatrix(presentationTimeUs: Long, useHdr: Boolean): FloatArray = adjMatrix
                })
            }
            // vignette / sharpen / grain / film grain (একটাই শেডার)
            if (track.category != TrackCategory.AUDIO && !fxAdj.fxDefault) {
                videoEffects.add(AdjustFxEffect(fxAdj))
            }
            // গতি: Media3 1.9+ এর setSpeed(SpeedProvider) — ভিডিও ও অডিও একসাথে (পিচ ঠিক রেখে) বদলায়।
            // SpeedProvider সেট করলে পুরনো speed-changing effect (SpeedChangeEffect/Sonic) ব্যবহার করা যায় না।
            if (c.speed != 1f) {
                val sp = c.speed
                editedBuilder.setSpeed(object : SpeedProvider {
                    override fun getSpeed(presentationTimeUs: Long): Float = sp
                    override fun getNextSpeedChangeTimeUs(timeUs: Long): Long = C.TIME_UNSET
                })
                // দ্রুত গতিতে আউটপুটের ফ্রেম-রেট যেন অতিরিক্ত না বাড়ে
                if (track.category != TrackCategory.AUDIO && sp > 1f) editedBuilder.setFrameRate(60)
            }
            if (audioProcessors.isNotEmpty() || videoEffects.isNotEmpty()) {
                editedBuilder.setEffects(Effects(audioProcessors, videoEffects))
            }

            builder.addItem(editedBuilder.build())
            added++
            cursor = c.endMs
        }
        // শুধু গ্যাপের (বা খালি) সিকোয়েন্স সমস্যা করে, তাই সেটা বাদ
        if (added == 0) return null
        return builder.build()
    }

    private fun saveToGallery(file: File) {
        Thread {
            val message = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val values = ContentValues().apply {
                        put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/MasterCut")
                        put(MediaStore.Video.Media.IS_PENDING, 1)
                    }
                    val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                    if (uri == null) {
                        "Failed to save gallery"
                    } else {
                        resolver.openOutputStream(uri)?.use { out ->
                            file.inputStream().use { it.copyTo(out) }
                        }
                        values.clear()
                        values.put(MediaStore.Video.Media.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                        "Saved: Movies/MasterCut"
                    }
                } else {
                    val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
                    val dest = File(dir, file.name)
                    file.copyTo(dest, overwrite = true)
                    "Saved: ${dest.absolutePath}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Save failed"
            } finally {
                file.delete()
            }
            mainHandler.post { state.status = message }
        }.start()
    }
}

// ───────────────────────── Project save / load (multi-project) ─────────────────────────
data class ProjectMeta(
    val id: String,
    val name: String,
    val ratio: AspectRatio,
    val clipCount: Int,
    val durationMs: Long,
    val modified: Long
)

object ProjectStorage {
    private const val DIR_NAME = "projects"
    private const val LEGACY_FILE = "project.json"

    /** লোডের সময় যে ক্লিপগুলোর ফাইল পাওয়া যায়নি তাদের নাম */
    var lastMissing: List<String> = emptyList()
        private set

    private fun dir(context: Context) = File(context.filesDir, DIR_NAME).apply { mkdirs() }
    private fun fileOf(context: Context, id: String) = File(dir(context), "$id.json")

    private fun migrateLegacy(context: Context) {
        val old = File(context.filesDir, LEGACY_FILE)
        if (!old.exists()) return
        try {
            val dest = fileOf(context, "legacy")
            if (!dest.exists()) old.copyTo(dest)
            old.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun list(context: Context): List<ProjectMeta> {
        migrateLegacy(context)
        val files = dir(context).listFiles { f -> f.extension == "json" } ?: return emptyList()
        return files.mapNotNull { f ->
            try {
                val root = JSONObject(f.readText())
                val arr = root.optJSONArray("clips") ?: JSONArray()
                var maxEnd = 0L
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    maxEnd = maxOf(maxEnd, o.optLong("startMs", 0L) + o.optLong("durationMs", 0L))
                }
                val ratio = try {
                    AspectRatio.valueOf(root.optString("aspectRatio", AspectRatio.RATIO_16_9.name))
                } catch (e: Exception) {
                    AspectRatio.RATIO_16_9
                }
                ProjectMeta(
                    id = f.nameWithoutExtension,
                    name = root.optString("projectName", "Project"),
                    ratio = ratio,
                    clipCount = arr.length(),
                    durationMs = maxOf(15000L, maxEnd + 3000L),
                    modified = f.lastModified()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }.sortedByDescending { it.modified }
    }

    fun delete(context: Context, id: String) {
        try {
            fileOf(context, id).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun save(context: Context, state: EditorState): Boolean {
        if (state.projectId.isBlank()) return false
        return try {
            val root = JSONObject()
            root.put("version", 7)
            root.put("projectName", state.projectName)
            root.put("aspectRatio", state.selectedRatio.name)
            root.put("nextId", state.nextId)
            root.put("playheadMs", state.playheadMs)
            root.put("zoom", state.zoom.toDouble())
            root.put("rippleOn", state.rippleOn)

            val tracksArr = JSONArray()
            state.tracks.forEach { t ->
                val o = JSONObject()
                o.put("id", t.id)
                o.put("label", t.label)
                o.put("category", t.category.name)
                o.put("isMuted", t.isMuted)
                o.put("isLocked", t.isLocked)
                tracksArr.put(o)
            }
            root.put("tracks", tracksArr)

            val arr = JSONArray()
            state.clips.forEach { c ->
                val o = JSONObject()
                o.put("id", c.id)
                o.put("name", c.name)
                o.put("trackId", c.trackId)
                o.put("startMs", c.startMs)
                o.put("durationMs", c.durationMs)
                o.put("inMs", c.inMs)
                o.put("sourceDurationMs", c.sourceDurationMs)
                if (c.uri != null) o.put("uri", c.uri)
                if (c.textOverlay != null) o.put("textOverlay", c.textOverlay)
                o.put("isVideoOverlay", c.isVideoOverlay)
                if (c.locked) o.put("locked", true)
                o.put("scale", c.scale.toDouble())
                o.put("rotation", c.rotation.toDouble())
                o.put("offsetX", c.offsetX.toDouble())
                o.put("offsetY", c.offsetY.toDouble())
                o.put("volume", c.volume.toDouble())
                o.put("opacity", c.opacity.toDouble())
                if (c.speed != 1f) o.put("speed", c.speed.toDouble())
                if (c.blend != ClipBlend.NORMAL) o.put("blend", c.blend.name)
                o.put("fadeInMs", c.fadeInMs)
                o.put("fadeOutMs", c.fadeOutMs)
                o.put("transIn", c.transIn.name)
                o.put("transInMs", c.transInMs)
                o.put("transOut", c.transOut.name)
                o.put("transOutMs", c.transOutMs)
                if (c.filter != FilterPreset.NONE) o.put("filter", c.filter.name)
                if (c.textOverlay != null && c.textStyle != TextStyleSpec()) o.put("textStyle", textStyleToJson(c.textStyle))
                if (!c.adj.isDefault) {
                    val ao = JSONObject()
                    ao.put("brightness", c.adj.brightness.toDouble())
                    ao.put("contrast", c.adj.contrast.toDouble())
                    ao.put("saturation", c.adj.saturation.toDouble())
                    ao.put("temperature", c.adj.temperature.toDouble())
                    ao.put("tint", c.adj.tint.toDouble())
                    ao.put("hue", c.adj.hue.toDouble())
                    ao.put("vignette", c.adj.vignette.toDouble())
                    ao.put("sharpen", c.adj.sharpen.toDouble())
                    ao.put("grain", c.adj.grain.toDouble())
                    ao.put("filmGrain", c.adj.filmGrain.toDouble())
                    o.put("adj", ao)
                }
                arr.put(o)
            }
            root.put("clips", arr)

            val dest = fileOf(context, state.projectId)
            val tmp = File(dest.parentFile, dest.name + ".tmp")
            tmp.writeText(root.toString())
            if (!tmp.renameTo(dest)) {
                dest.writeText(root.toString())
                tmp.delete()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** সফল হলে হারানো ক্লিপের সংখ্যা (0 = সব ঠিক), ব্যর্থ হলে -1 */
    fun load(context: Context, state: EditorState, id: String): Int {
        val file = fileOf(context, id)
        if (!file.exists()) return -1
        return try {
            val root = JSONObject(file.readText())
            val version = root.optInt("version", 1)

            val loadedTracks = mutableListOf<TrackInfo>()
            if (root.has("tracks")) {
                val tArr = root.getJSONArray("tracks")
                for (i in 0 until tArr.length()) {
                    val o = tArr.getJSONObject(i)
                    val cat = TrackCategory.valueOf(o.getString("category"))
                    val col = when (cat) {
                        TrackCategory.VIDEO -> Color(0xFFFF4757)
                        TrackCategory.OVERLAY -> Color(0xFF2ED573)
                        TrackCategory.AUDIO -> Color(0xFF1E90FF)
                    }
                    loadedTracks.add(
                        TrackInfo(
                            id = o.getString("id"),
                            label = o.getString("label"),
                            category = cat,
                            color = col,
                            isMuted = o.optBoolean("isMuted", false),
                            isLocked = o.optBoolean("isLocked", false)
                        )
                    )
                }
            }

            val arr = root.getJSONArray("clips")
            val loaded = mutableListOf<Clip>()
            val missingNames = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val uri = if (o.has("uri")) o.getString("uri") else null
                val textOverlay = if (o.has("textOverlay")) o.getString("textOverlay") else null

                // ফাইল না পেলে ক্লিপ মুছি না — "missing" চিহ্ন দিয়ে রেখে দিই, যাতে সাময়িক সমস্যায় (SD কার্ড, পারমিশন) কাজ না হারায়
                val isMissing = uri != null && !canOpen(context, uri)
                if (isMissing) missingNames.add(o.optString("name", "clip"))
                // সোর্স-দৈর্ঘ্য: সেভ করা থাকলে সেটা, নইলে (পুরনো প্রজেক্ট) ফাইল থেকে পড়ি
                val srcDur = if (o.has("sourceDurationMs")) o.optLong("sourceDurationMs", 0L)
                else if (uri != null && !isMissing && textOverlay == null) readDurationMs(context, uri.toUri())
                else 0L
                // পুরনো সংস্করণে (v<6) offset px ছিল, এখন প্রিভিউ-উচ্চতার ভগ্নাংশ — তাই রিসেট
                val oldOffsets = version < 6
                loaded.add(
                    Clip(
                        id = o.getInt("id"),
                        name = o.getString("name"),
                        trackId = o.optString("trackId", "V1"),
                        startMs = o.getLong("startMs"),
                        durationMs = o.getLong("durationMs"),
                        uri = uri,
                        inMs = o.optLong("inMs", 0L),
                        textOverlay = textOverlay,
                        isVideoOverlay = o.optBoolean("isVideoOverlay", false),
                        scale = o.optDouble("scale", 1.0).toFloat(),
                        rotation = o.optDouble("rotation", 0.0).toFloat(),
                        offsetX = if (oldOffsets) 0f else o.optDouble("offsetX", 0.0).toFloat(),
                        offsetY = if (oldOffsets) 0f else o.optDouble("offsetY", 0.0).toFloat(),
                        volume = o.optDouble("volume", 1.0).toFloat(),
                        opacity = o.optDouble("opacity", 1.0).toFloat(),
                        speed = o.optDouble("speed", 1.0).toFloat().coerceIn(MIN_SPEED, MAX_SPEED),
                        // Blend Mode এক্সপোর্টে নেই, তাই প্রিভিউ-এক্সপোর্ট না মেলা এড়াতে সবসময় Normal
                        blend = ClipBlend.NORMAL,
                        sourceDurationMs = srcDur,
                        missing = isMissing,
                        locked = o.optBoolean("locked", false),
                        fadeInMs = o.optLong("fadeInMs", 0L),
                        fadeOutMs = o.optLong("fadeOutMs", 0L),
                        transIn = parseTransition(o.optString("transIn", "NONE")),
                        transInMs = o.optLong("transInMs", TRANS_DEFAULT_MS),
                        transOut = parseTransition(o.optString("transOut", "NONE")),
                        transOutMs = o.optLong("transOutMs", TRANS_DEFAULT_MS),
                        adj = o.optJSONObject("adj")?.let { ao ->
                            Adjust(
                                brightness = ao.optDouble("brightness", 0.0).toFloat(),
                                contrast = ao.optDouble("contrast", 0.0).toFloat(),
                                saturation = ao.optDouble("saturation", 0.0).toFloat(),
                                temperature = ao.optDouble("temperature", 0.0).toFloat(),
                                tint = ao.optDouble("tint", 0.0).toFloat(),
                                hue = ao.optDouble("hue", 0.0).toFloat(),
                                vignette = ao.optDouble("vignette", 0.0).toFloat(),
                                sharpen = ao.optDouble("sharpen", 0.0).toFloat(),
                                grain = ao.optDouble("grain", 0.0).toFloat(),
                                filmGrain = ao.optDouble("filmGrain", 0.0).toFloat()
                            )
                        } ?: Adjust(),
                        filter = o.optString("filter", "NONE").let { n ->
                            FilterPreset.entries.firstOrNull { it.name == n } ?: FilterPreset.NONE
                        },
                        textStyle = o.optJSONObject("textStyle")?.let { textStyleFromJson(it) } ?: TextStyleSpec()
                    )
                )
            }
            val loadedRatio = try {
                AspectRatio.valueOf(root.optString("aspectRatio", AspectRatio.RATIO_16_9.name))
            } catch (e: Exception) {
                AspectRatio.RATIO_16_9
            }
            val loadedName = root.optString("projectName", "Project")

            state.restoreProject(
                id = id,
                savedTracks = loadedTracks,
                savedClips = loaded,
                savedNextId = root.optInt("nextId", 100),
                savedPlayheadMs = root.optLong("playheadMs", 0L),
                savedZoom = root.optDouble("zoom", 1.0).toFloat(),
                savedRipple = root.optBoolean("rippleOn", false),
                savedRatio = loadedRatio,
                savedProjectName = loadedName
            )
            lastMissing = missingNames
            missingNames.size
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    private fun parseTransition(name: String): TransitionType =
        TransitionType.entries.firstOrNull { it.name == name } ?: TransitionType.NONE

    private fun canOpen(context: Context, uriString: String): Boolean {
        return try {
            val uri = uriString.toUri()
            if (uri.scheme == "file") {
                File(uri.path ?: "").exists()
            } else {
                val fd = context.contentResolver.openAssetFileDescriptor(uri, "r")
                fd?.close()
                fd != null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// ───────────────────────── Activity & Main Router ─────────────────────────
class EditorViewModel(app: Application) : AndroidViewModel(app) {
    val editorState = EditorState()
    val exporter = ExportManager(app, editorState)

    override fun onCleared() {
        exporter.cancel()
        super.onCleared()
    }
}

enum class AppScreen { WELCOME, EDITOR }

class MainActivity : ComponentActivity() {
    private val vm: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep the display awake while this video-editing activity is in the foreground.
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()

        setContent {
            MasterCutTheme {
                // প্রজেক্ট খোলা থাকা অবস্থায় (রোটেশন/প্রসেস রিস্টার্ট বাদে) এডিটরেই থাকা
                var currentScreen by remember {
                    mutableStateOf(if (vm.editorState.projectId.isNotBlank()) AppScreen.EDITOR else AppScreen.WELCOME)
                }

                Surface(modifier = Modifier.fillMaxSize(), color = BgDark) {
                    if (currentScreen == AppScreen.WELCOME) {
                        WelcomeScreen(
                            state = vm.editorState,
                            onCreateNew = { ratio, name ->
                                vm.editorState.newProject(ratio, name)
                                ProjectStorage.save(this@MainActivity, vm.editorState)
                                currentScreen = AppScreen.EDITOR
                            },
                            onOpenProject = { id ->
                                val r = ProjectStorage.load(this@MainActivity, vm.editorState, id)
                                if (r < 0) {
                                    Toast.makeText(this@MainActivity, "Could not open project", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (r > 0) {
                                        val names = ProjectStorage.lastMissing.take(2).joinToString(", ")
                                        val more = if (r > 2) " +${r - 2} more" else ""
                                        vm.editorState.status = "Missing: $names$more"
                                    }
                                    currentScreen = AppScreen.EDITOR
                                }
                            }
                        )
                    } else {
                        EditorScreen(
                            state = vm.editorState,
                            exporter = vm.exporter,
                            onBackToWelcome = {
                                vm.editorState.isPlaying = false
                                ProjectStorage.save(this@MainActivity, vm.editorState)
                                currentScreen = AppScreen.WELCOME
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        ProjectStorage.save(this, vm.editorState)
    }

    // স্ট্যাটাস বার / নেভিগেশন বার লুকিয়ে পুরো স্ক্রিন ব্যবহার (ওপর/নিচ থেকে সোয়াইপ করলে সাময়িক দেখা যায়)
    private fun hideSystemBars() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }
}

// ───────────────────────── Welcome Screen ─────────────────────────
@Composable
fun WelcomeScreen(
    state: EditorState,
    onCreateNew: (AspectRatio, String) -> Unit,
    onOpenProject: (String) -> Unit
) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var projects by remember { mutableStateOf(ProjectStorage.list(context)) }
    var toDelete by remember { mutableStateOf<ProjectMeta?>(null) }

    if (showCreateDialog) {
        NewProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { ratio, name ->
                showCreateDialog = false
                onCreateNew(ratio, name)
            }
        )
    }

    toDelete?.let { meta ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete project?", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text("\"${meta.name}\" will be removed permanently.", color = TextDim, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        ProjectStorage.delete(context, meta.id)
                        if (state.projectId == meta.id) state.projectId = ""
                        projects = ProjectStorage.list(context)
                        toDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE84118))
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel", color = TextDim) } },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    Row(modifier = Modifier.fillMaxSize().background(BgDark).padding(16.dp)) {
        // বাম পাশে: New Project কার্ড
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.5.dp, Accent, RoundedCornerShape(16.dp))
                    .clickable { showCreateDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Create New Project", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Choose Aspect Ratio & Quality", color = TextDim, fontSize = 11.sp)
                }
            }
        }

        // ডান পাশে: সব সেভ করা প্রজেক্ট
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .background(PanelDark, RoundedCornerShape(16.dp))
                .border(1.dp, Stroke, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text("Recent Projects", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            if (projects.isNotEmpty()) {
                val dateFmt = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects, key = { it.id }) { meta ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF242424))
                                .clickable { onOpenProject(meta.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(meta.ratio.label, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(meta.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(
                                    "${meta.clipCount} clip(s) • ${fmt(meta.durationMs)} • ${dateFmt.format(Date(meta.modified))}",
                                    color = TextDim,
                                    fontSize = 10.sp
                                )
                            }
                            Text("Open ▸", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF))
                                    .clickable { toDelete = meta },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No saved projects yet.\nTap Create New Project to begin!", color = TextDim, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun NewProjectDialog(onDismiss: () -> Unit, onCreate: (AspectRatio, String) -> Unit) {
    var selectedRatio by remember { mutableStateOf(AspectRatio.RATIO_16_9) }
    var projectName by remember { mutableStateOf("Project " + SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Project Setup", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Stroke
                    )
                )

                Text("Select Aspect Ratio:", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatio.entries.forEach { item ->
                        val isSel = item == selectedRatio
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF2E2E2E) else Color(0xFF1A1A1A))
                                .border(1.5.dp, if (isSel) Accent else Stroke, RoundedCornerShape(8.dp))
                                .clickable { selectedRatio = item }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(item.iconW.dp, item.iconH.dp)
                                    .background(if (isSel) Accent else Color(0xFF555555), RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else TextDim
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(selectedRatio, projectName.ifBlank { "Untitled" }) },
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextDim) }
        },
        containerColor = Color(0xFF1E1E1E)
    )
}

// ───────────────────────── Editor Screen ─────────────────────────
@Composable
fun EditorScreen(state: EditorState, exporter: ExportManager, onBackToWelcome: () -> Unit) {
    val context = LocalContext.current
    var showTextDialog by remember { mutableStateOf(false) }

    val view = LocalView.current
    LaunchedEffect(exporter.running) { view.keepScreenOn = exporter.running }

    var pickedVideoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var previewVideoUri by remember { mutableStateOf<Uri?>(null) }
    var showMediaSelection by remember { mutableStateOf(false) }

    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(50)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            pickedVideoUris = uris.distinct()
            previewVideoUri = null
            showMediaSelection = true
        }
    }

    val overlayVideoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) importMedia(context, state, uri, isVideoOverlay = true, category = TrackCategory.OVERLAY)
    }

    fun addOverlayImage(uri: Uri) {
        val savedUri = try {
            val dir = File(context.filesDir, "overlays").apply { mkdirs() }
            val file = File(dir, "overlay_${System.currentTimeMillis()}.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            uri.toString()
        }
        state.addMediaToActiveTrack(savedUri, 3000L, customName = "Image", category = TrackCategory.OVERLAY)
    }

    // একটি Gallery picker থেকেই Image অথবা Video — দুটোই Overlay হিসেবে নেওয়া যাবে।
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri).orEmpty()
            if (mimeType.startsWith("video/")) {
                importMedia(context, state, uri, isVideoOverlay = true, category = TrackCategory.OVERLAY)
            } else {
                addOverlayImage(uri)
            }
        }
    }

    // একটি Files picker থেকেই Image অথবা Video নির্বাচন করা যাবে।
    val imageFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri).orEmpty()
            if (mimeType.startsWith("video/")) {
                importMedia(context, state, uri, isVideoOverlay = true, category = TrackCategory.OVERLAY)
            } else {
                addOverlayImage(uri)
            }
        }
    }

    // পুরোনো আলাদা Video picker আর দরকার নেই; callback compatibility-র জন্য
    // declaration রাখা হয়েছে, কিন্তু Overlay menu থেকে এটি ব্যবহার করা হয় না।
    val overlayVideoFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) importMedia(context, state, uri, isVideoOverlay = true, category = TrackCategory.OVERLAY)
    }

    val audioPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) importMedia(context, state, uri, category = TrackCategory.AUDIO)
    }
    val videoFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            pickedVideoUris = uris.distinct()
            previewVideoUri = null
            showMediaSelection = true
        }
    }

    val fontPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val (name, error) = CustomFonts.import(context, uri)
            if (name != null) {
                state.fontsVersion++
                // বাছাই করা টেক্সটের খসড়ায় সঙ্গে সঙ্গে নতুন ফন্ট বসানো
                val d = state.textDraft
                if (d != null) state.textDraft = d.first to d.second.copy(customFont = name)
                state.status = "Font added: ${CustomFonts.label(name)}"
            } else {
                state.status = error ?: "Font import failed"
                Toast.makeText(context, error ?: "Font import failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ভলিউম + fade (প্রিভিউতে সর্বোচ্চ 100%, 100% এর উপরের অংশ শুধু এক্সপোর্টে কাজ করে)
    if (showMediaSelection) {
        MediaSelectionDialog(
            uris = pickedVideoUris,
            previewUri = previewVideoUri,
            onPreview = { previewVideoUri = it },
            onBackToList = { previewVideoUri = null },
            onAddOne = { uri ->
                importMedia(context, state, uri, category = TrackCategory.VIDEO)
                pickedVideoUris = pickedVideoUris.filterNot { it == uri }
                previewVideoUri = null
                if (pickedVideoUris.isEmpty()) showMediaSelection = false
            },
            onAddAll = {
                val toAdd = pickedVideoUris.toList()
                toAdd.forEach { importMedia(context, state, it, category = TrackCategory.VIDEO) }
                pickedVideoUris = emptyList()
                previewVideoUri = null
                showMediaSelection = false
            },
            onClose = { showMediaSelection = false }
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            listOf(state.clips, state.tracks, state.zoom, state.rippleOn, state.projectName, state.selectedRatio)
        }
            .drop(1)
            .collectLatest {
                delay(500.milliseconds)
                ProjectStorage.save(context, state)
            }
    }

    LaunchedEffect(state.isPlaying) {
        if (!state.isPlaying) return@LaunchedEffect
        if (state.playheadMs >= state.totalMs) state.playheadMs = 0L
        var last = withFrameMillis { it }
        while (state.isPlaying) {
            val now = withFrameMillis { it }
            state.playheadMs += now - last
            last = now
            if (state.playheadMs >= state.totalMs) {
                state.playheadMs = state.totalMs
                state.isPlaying = false
            }
        }
    }

    // টেক্সট পরে সম্পাদনা
    state.textEditId?.let { editId ->
        val editClip = state.clips.firstOrNull { it.id == editId }
        if (editClip?.textOverlay != null) {
            TextInputDialog(
                title = "Edit Text",
                initial = editClip.textOverlay,
                confirmLabel = "Save",
                onDismiss = { state.textEditId = null },
                onConfirm = { t ->
                    state.setClipText(editId, t)
                    state.textEditId = null
                }
            )
        }
    }

    if (showTextDialog) {
        TextInputDialog(
            onDismiss = { showTextDialog = false },
            onConfirm = { text ->
                state.addTextOverlay(text)
                showTextDialog = false
            }
        )
    }

    if (state.panel == FX_PANEL) {
        TransitionDialog(state)
    }

    Column(modifier = Modifier.fillMaxSize().background(BgDark).safeDrawingPadding()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PanelDark)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SideActionBar(state, onHomeClick = onBackToWelcome)

                SideInfoPanel(state, modifier = Modifier.width(90.dp).fillMaxHeight())

                PreviewArea(
                    state = state,
                    exporter = exporter,
                    onPickVideo = {
                        videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    },
                    onPickVideoFile = { videoFilePicker.launch(arrayOf("video/*")) },
                    onPickAudio = { audioPicker.launch(arrayOf("audio/*")) },
                    onPickImage = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                    },
                    onPickOverlayVideo = {
                        overlayVideoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    },
                    onPickImageFile = { imageFilePicker.launch(arrayOf("image/*", "video/*")) },
                    onPickOverlayVideoFile = { overlayVideoFilePicker.launch(arrayOf("video/*")) },
                    onAddText = { showTextDialog = true },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                val selClip = state.selectedClip
                val track = state.tracks.firstOrNull { it.id == selClip?.trackId }
                val isAudio = track != null && (track.category == TrackCategory.AUDIO || track.category == TrackCategory.VIDEO || (track.category == TrackCategory.OVERLAY && selClip?.isVideoOverlay == true))

                // ওভারলে-ভিডিওতে ভলিউম ও Properties (Opacity/Scale/Rotate) দুটোই লাগে — তাই প্যানেলে টগল বাটন আছে
                val overlayVideoSel = selClip != null && track?.category == TrackCategory.OVERLAY && selClip.isVideoOverlay
                if (selClip != null && isAudio && !(overlayVideoSel && state.showOverlayProps)) {
                    VolumeAudioPanel(state, modifier = Modifier.padding(start = 8.dp).width(90.dp).fillMaxHeight())
                } else {
                    PropertiesPanel(state, modifier = Modifier.padding(start = 8.dp).width(90.dp).fillMaxHeight())
                }

                RightPanel(state, Modifier.fillMaxHeight().width(120.dp))
            }

            if (state.panel == FX_ADJUST_PANEL) {
                AdjustPanel(state, Modifier.align(Alignment.CenterEnd))
            } else if (state.panel == FX_FILTER_PANEL) {
                FilterPanel(state, Modifier.align(Alignment.CenterEnd))
            } else if (state.panel == FX_TEXT_PANEL) {
                TextPanel(state, Modifier.align(Alignment.CenterEnd), onPickFont = { fontPicker.launch(arrayOf("*/*")) })
            } else if (state.panel == FX_VIDEO_PANEL) {
                VideoFxPanel(state, Modifier.align(Alignment.CenterEnd))
            }

            // দুই প্যানেলের কোনোটিই খোলা না থাকলে না-বসানো খসড়া ফেলে দেওয়া (ট্যাব বদলালে খসড়া থাকে)
            LaunchedEffect(state.panel) {
                if (state.panel != FX_ADJUST_PANEL && state.panel != FX_FILTER_PANEL && state.panel != FX_TEXT_PANEL && state.panel != FX_VIDEO_PANEL) {
                    state.adjustDraft = null
                    state.filterDraft = null
                    state.textDraft = null
                }
            }
        }

        if (state.timelineVisible) {
            Timeline(state)
        }
    }
}

// ───────────────────────── FX / Transition pop-up ─────────────────────────
@Composable
private fun TransitionDialog(state: EditorState) {
    val joinId = remember { state.transitionJoinClipId }
    val preset = remember { state.transitionPresetTarget }
    val targetId = remember { joinId ?: state.transitionTargetClip()?.id }
    val clip = state.clips.firstOrNull { it.id == targetId }
    val close = {
        state.transitionJoinClipId = null
        state.transitionPresetTarget = null
        state.panel = null
    }

    var tgt by remember {
        mutableStateOf(
            if (joinId != null) TransTarget.JOIN_PREV
            else preset ?: clip?.let { state.defaultTransitionTarget(it) } ?: TransTarget.START
        )
    }
    // এখন এই জায়গায় যা বসানো আছে — সেটাই শুরুতে সিলেক্ট দেখাবে
    val existing = when (tgt) {
        TransTarget.START, TransTarget.JOIN_PREV, TransTarget.BOTH -> clip?.transIn ?: TransitionType.NONE
        TransTarget.END, TransTarget.JOIN_NEXT -> clip?.transOut ?: TransitionType.NONE
    }
    var sel by remember { mutableStateOf(if (existing != TransitionType.NONE) existing else TransitionType.FADE) }
    var durSec by remember {
        val ms = when (tgt) {
            TransTarget.START, TransTarget.JOIN_PREV, TransTarget.BOTH -> clip?.transInMs
            TransTarget.END, TransTarget.JOIN_NEXT -> clip?.transOutMs
        } ?: TRANS_DEFAULT_MS
        mutableFloatStateOf(if (ms > 0L) ms / 1000f else TRANS_DEFAULT_MS / 1000f)
    }

    Dialog(
        onDismissRequest = { close() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 760.dp)
                .heightIn(max = 340.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1E1E1E))
                .border(1.dp, Stroke, RoundedCornerShape(14.dp))
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .clickable { close() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text("Transition", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (clip != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Accent)
                            .clickable {
                                val maxS = transitionMaxSec(state, clip, tgt)
                                val ms = (durSec.coerceAtMost(maxS) * 1000f).toLong()
                                state.applyTransition(clip.id, tgt, sel, ms)
                                close()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(Modifier.size(28.dp))
                }
            }

            if (clip == null) {
                Text(
                    "Select a Video or Overlay clip on the timeline first\n(or tap the small + between two clips), then open FX again.",
                    color = TextDim,
                    fontSize = 12.sp
                )
            } else {
                val maxSec = transitionMaxSec(state, clip, tgt)
                val shown = durSec.coerceAtMost(maxSec)
                val targets = state.transitionTargets(clip)
                val trackLabel = state.tracks.firstOrNull { it.id == clip.trackId }?.label ?: ""
                val isOverlayClip = state.tracks.firstOrNull { it.id == clip.trackId }?.category == TrackCategory.OVERLAY

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // বাঁ পাশ: লাইভ প্রিভিউ (A → B)
                    Column(
                        modifier = Modifier.width(190.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TransitionPreview(sel, (shown * 1000f).toLong(), Modifier.fillMaxWidth())
                        Text(
                            if (joinId != null) "Between clips  •  $trackLabel" else "${clip.name}  •  $trackLabel",
                            color = TextDim,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                        Text(
                            if (isOverlayClip) "Overlay: fades make it transparent."
                            else "Video: Fade/Dissolve → black, Flash → white.",
                            color = Color(0xFF666666),
                            fontSize = 9.sp
                        )
                    }

                    // ডান পাশ: জায়গা, সময় (স্লাইডার), ইফেক্ট তালিকা
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (joinId == null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                targets.forEach { t -> ChipBtn(t.label, tgt == t) { tgt = t } }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF2A2A2A))
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⏱", fontSize = 14.sp)
                            Slider(
                                value = shown,
                                onValueChange = { durSec = it },
                                valueRange = 0.2f..maxSec,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Accent,
                                    inactiveTrackColor = Color(0xFF444444)
                                ),
                                modifier = Modifier.weight(1f).height(32.dp).padding(horizontal = 8.dp)
                            )
                            Text(
                                String.format(Locale.getDefault(), "%.1fs", shown),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TransitionType.entries.chunked(5).forEach { rowItems ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowItems.forEach { type ->
                                        TransitionCard(type, active = type == sel) { sel = type }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val TEXT_COLORS: List<Int> = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFFFF4757, 0xFFFF9F43, 0xFFFFD32A, 0xFF2ED573,
    0xFF1ABC9C, 0xFF1E90FF, 0xFF8E44AD, 0xFFFF6B9D, 0xFF888888, 0xFFD4AF37
).map { it.toInt() }

@Composable
private fun MiniChip(text: String, selected: Boolean, weight: FontWeight = FontWeight.Bold, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Accent else Color(0xFF2B2B2B))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 10.sp, color = Color.White, fontWeight = weight, maxLines = 1)
    }
}

@Composable
private fun ColorRow(selected: Int, onPick: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TEXT_COLORS.forEach { argb ->
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(argb))
                    .border(if (argb == selected) 2.dp else 1.dp, if (argb == selected) Accent else Color(0xFF666666), CircleShape)
                    .clickable { onPick(argb) }
            )
        }
    }
}

@Composable
private fun TextSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    display: String,
    onChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 10.sp, modifier = Modifier.width(62.dp), maxLines = 1)
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Accent,
                inactiveTrackColor = Color(0xFF444444)
            ),
            modifier = Modifier.weight(1f).height(24.dp)
        )
        Text(
            display,
            color = Accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(30.dp)
        )
    }
}

@Composable
private fun TextSectionTitle(text: String) {
    Text(text, color = TextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
}

/** Text প্যানেলের লক্ষ্য: বাছাই করা টেক্সট ক্লিপ, না থাকলে প্লেহেডের নিচের টেক্সট ক্লিপ */
@Composable
private fun rememberTextTarget(state: EditorState): Clip? {
    val c: Clip? = state.selectedClip?.takeIf { it.textOverlay != null }
        ?: state.clips.firstOrNull {
            it.textOverlay != null && state.playheadMs >= it.startMs && state.playheadMs < it.endMs
        }
    LaunchedEffect(c?.id) {
        if (c == null) {
            state.textDraft = null
        } else {
            if (state.textDraft?.first != c.id) state.textDraft = c.id to c.textStyle
            if (state.playheadMs < c.startMs || state.playheadMs >= c.endMs) {
                state.isPlaying = false
                state.seek(c.startMs + minOf(c.durationMs / 2, 1000L))
            }
        }
    }
    return c
}

/** ডান প্রান্তে লাগানো Text প্যানেল — ফন্ট, রঙ, স্টাইল, শ্যাডো, আউটলাইন, ব্যাকগ্রাউন্ড। প্রিভিউতে সঙ্গে সঙ্গে দেখা যায়। */
@Composable
private fun TextPanel(state: EditorState, modifier: Modifier, onPickFont: () -> Unit) {
    val context = LocalContext.current
    val c = rememberTextTarget(state)
    val spec = c?.let { state.effectiveTextStyle(it) } ?: TextStyleSpec()
    val dirty = c != null && spec != c.textStyle
    val shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)

    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(Color(0xF2191919), shape)
            .border(1.dp, Stroke, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FxTabs(state, 2, Modifier.weight(1f))
            FxCloseButton(state)
        }

        if (c == null) {
            Text(
                "Select a text overlay on the timeline (add one from the Text menu) — the preview updates live as you style it.",
                color = TextDim,
                fontSize = 11.sp
            )
        } else {
            Text(
                text = c.name + if (dirty) "  •  not applied" else "",
                color = if (dirty) Color(0xFFFFD32A) else TextDim,
                fontSize = 10.sp,
                maxLines = 1
            )
            MiniChip("✎ Edit text", false) { state.textEditId = c.id }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                fun set(n: TextStyleSpec) { state.textDraft = c.id to n }

                TextSectionTitle("FONT")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextFont.entries.forEach { f ->
                        MiniChip(f.label, spec.customFont == null && spec.font == f) { set(spec.copy(font = f, customFont = null)) }
                    }
                }
                val myFonts = remember(state.fontsVersion) { CustomFonts.list(context) }
                if (myFonts.isNotEmpty()) {
                    Text("My fonts", color = TextDim, fontSize = 9.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        myFonts.forEach { name ->
                            MiniChip(CustomFonts.label(name), spec.customFont == name) { set(spec.copy(customFont = name)) }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniChip("+ Add font", false) { onPickFont() }
                    if (spec.customFont != null) {
                        MiniChip("Delete font", false) {
                            spec.customFont?.let { gone ->
                                CustomFonts.delete(context, gone)
                                state.fontsVersion++
                                set(spec.copy(customFont = null))
                                state.status = "Font deleted: ${CustomFonts.label(gone)}"
                            }
                        }
                    }
                }
                if (spec.customFont != null) {
                    Text("Custom font uses its own weight — B / I apply to built-in fonts only.", color = Color(0xFF666666), fontSize = 9.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniChip("B", spec.bold) { set(spec.copy(bold = !spec.bold)) }
                    MiniChip("I", spec.italic) { set(spec.copy(italic = !spec.italic)) }
                    MiniChip("U", spec.underline) { set(spec.copy(underline = !spec.underline)) }
                }
                TextSlider("Size", spec.size, 0.4f..2.5f, "${(spec.size * 100).roundToInt()}%") { set(spec.copy(size = it)) }
                TextSlider("Spacing", spec.spacing, -0.05f..0.5f, "${(spec.spacing * 100).roundToInt()}") { set(spec.copy(spacing = it)) }

                TextSectionTitle("COLOR")
                ColorRow(spec.color) { set(spec.copy(color = it)) }

                TextSectionTitle("SHADOW")
                MiniChip(if (spec.shadow) "Shadow: ON" else "Shadow: OFF", spec.shadow) { set(spec.copy(shadow = !spec.shadow)) }
                if (spec.shadow) {
                    ColorRow(spec.shadowColor) { set(spec.copy(shadowColor = it)) }
                    TextSlider("Blur", spec.shadowBlur, 0f..1f, "${(spec.shadowBlur * 100).roundToInt()}") { set(spec.copy(shadowBlur = it)) }
                    TextSlider("Offset X", spec.shadowDx, -1f..1f, "${(spec.shadowDx * 100).roundToInt()}") { set(spec.copy(shadowDx = it)) }
                    TextSlider("Offset Y", spec.shadowDy, -1f..1f, "${(spec.shadowDy * 100).roundToInt()}") { set(spec.copy(shadowDy = it)) }
                }

                TextSectionTitle("OUTLINE")
                MiniChip(if (spec.outline) "Outline: ON" else "Outline: OFF", spec.outline) { set(spec.copy(outline = !spec.outline)) }
                if (spec.outline) {
                    ColorRow(spec.outlineColor) { set(spec.copy(outlineColor = it)) }
                    TextSlider("Width", spec.outlineWidth, 0f..1f, "${(spec.outlineWidth * 100).roundToInt()}") { set(spec.copy(outlineWidth = it)) }
                }

                TextSectionTitle("BACKGROUND")
                MiniChip(if (spec.bg) "Background: ON" else "Background: OFF", spec.bg) { set(spec.copy(bg = !spec.bg)) }
                if (spec.bg) {
                    ColorRow(spec.bgColor) { set(spec.copy(bgColor = it)) }
                    TextSlider("Opacity", spec.bgAlpha, 0f..1f, "${(spec.bgAlpha * 100).roundToInt()}") { set(spec.copy(bgAlpha = it)) }
                    TextSlider("Corners", spec.bgRadius, 0f..4f, "${(spec.bgRadius * 100).roundToInt()}") { set(spec.copy(bgRadius = it)) }
                }

                Text(
                    "Reset style",
                    color = Accent,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable { state.textDraft = c.id to TextStyleSpec() }
                        .padding(vertical = 6.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PillBtn("Apply") {
                    state.applyTextDraft()
                    state.panel = null
                }
                PillBtn("Apply all", Color(0xFF2B6CB0)) {
                    state.applyTextStyleToAll(spec)
                    state.panel = null
                }
            }
        }
    }
}

/** Adjust ও Filter প্যানেলের লক্ষ্য ক্লিপ; ক্লিপ বদলালে দুই খসড়াই নতুন করে শুরু, প্লেহেড ক্লিপের বাইরে থাকলে ভেতরে এনে প্রিভিউতে দেখানো */
@Composable
private fun rememberFxTarget(state: EditorState): Clip? {
    val c: Clip? = state.selectedClip?.takeIf { state.canAdjust(it) }
        ?: state.clips.firstOrNull {
            state.canAdjust(it) && state.playheadMs >= it.startMs && state.playheadMs < it.endMs
        }
    LaunchedEffect(c?.id) {
        if (c == null) {
            state.adjustDraft = null
            state.filterDraft = null
        } else {
            if (state.adjustDraft?.first != c.id) state.adjustDraft = c.id to c.adj
            if (state.filterDraft?.first != c.id) state.filterDraft = c.id to c.filter
            if (state.playheadMs < c.startMs || state.playheadMs >= c.endMs) {
                state.isPlaying = false
                state.seek(c.startMs + minOf(c.durationMs / 2, 1000L))
            }
        }
    }
    return c
}

/** Adjust | Filter ট্যাব-চিপ */
@Composable
private fun FxTabs(state: EditorState, tab: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MiniChip("Adjust", tab == 0) { state.panel = FX_ADJUST_PANEL }
        MiniChip("Filter", tab == 1) { state.panel = FX_FILTER_PANEL }
        MiniChip("Text", tab == 2) { state.panel = FX_TEXT_PANEL }
        MiniChip("Video fx", tab == 3) { state.panel = FX_VIDEO_PANEL }
    }
}

@Composable
private fun FxCloseButton(state: EditorState) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Color(0x33FFFFFF))
            .clickable { state.panel = null },
        contentAlignment = Alignment.Center
    ) {
        Text("✕", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

/** ডান প্রান্তে লাগানো Filter প্যানেল — ১২টি প্রিসেটের গ্রিড। Adjust-এর সাথে একসাথে কাজ করে। */
@Composable
private fun FilterPanel(state: EditorState, modifier: Modifier) {
    val c = rememberFxTarget(state)
    val selected = c?.let { state.draftFilter(it) } ?: FilterPreset.NONE
    val dirty = c != null && state.hasPending(c)
    val shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)

    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(Color(0xF2191919), shape)
            .border(1.dp, Stroke, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FxTabs(state, 1, Modifier.weight(1f))
            FxCloseButton(state)
        }

        if (c == null) {
            Text(
                "Select a video clip (Video track or Overlay video) on the timeline — the preview updates live as you pick a filter.",
                color = TextDim,
                fontSize = 11.sp
            )
        } else {
            Text(
                text = c.name + if (dirty) "  •  not applied" else "",
                color = if (dirty) Color(0xFFFFD32A) else TextDim,
                fontSize = 10.sp,
                maxLines = 1
            )

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterPreset.entries.chunked(3).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowItems.forEach { p ->
                            Box(modifier = Modifier.weight(1f)) {
                                FilterCard(p, p == selected) { state.filterDraft = c.id to p }
                            }
                        }
                        repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                Text(
                    "Filter works together with Adjust.",
                    color = Color(0xFF666666),
                    fontSize = 9.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PillBtn("Apply") {
                    state.applyDrafts()
                    state.panel = null
                }
                PillBtn("Apply all", Color(0xFF2B6CB0)) {
                    state.applyFilterToAll(selected)
                    state.panel = null
                }
            }
        }
    }
}

@Composable
private fun FilterCard(preset: FilterPreset, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) Color(0xFF3A2024) else Color(0xFF2A2A2A))
            .border(1.5.dp, if (active) Accent else Stroke, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(preset.icon, fontSize = 22.sp)
        Text(
            preset.label,
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

/** ডান প্রান্তে লাগানো Adjust প্যানেল — প্রিভিউ ঢাকে না। স্লাইডার টানলে প্রিভিউতে সঙ্গে সঙ্গে দেখা যায়; Apply / Apply all চাপলে তবেই ক্লিপে বসে। */
@Composable
private fun AdjustPanel(state: EditorState, modifier: Modifier) {
    val c = rememberFxTarget(state)
    val draft = c?.let { state.draftAdj(it) } ?: Adjust()
    val dirty = c != null && state.hasPending(c)
    val shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)

    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(Color(0xF2191919), shape)
            .border(1.dp, Stroke, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FxTabs(state, 0, Modifier.weight(1f))
            FxCloseButton(state)
        }

        if (c == null) {
            Text(
                "Select a video clip (Video track or Overlay video) on the timeline — the preview updates live as you adjust.",
                color = TextDim,
                fontSize = 11.sp
            )
        } else {
            Text(
                text = c.name + if (dirty) "  •  not applied" else "",
                color = if (dirty) Color(0xFFFFD32A) else TextDim,
                fontSize = 10.sp,
                maxLines = 1
            )

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                fun setDraft(a: Adjust) { state.adjustDraft = c.id to a }
                AdjustSlider("Brightness", draft.brightness) { setDraft(draft.copy(brightness = it)) }
                AdjustSlider("Contrast", draft.contrast) { setDraft(draft.copy(contrast = it)) }
                AdjustSlider("Saturation", draft.saturation) { setDraft(draft.copy(saturation = it)) }
                AdjustSlider("White bal.", draft.temperature) { setDraft(draft.copy(temperature = it)) }
                AdjustSlider("Tint", draft.tint) { setDraft(draft.copy(tint = it)) }
                AdjustSlider("Hue", draft.hue) { setDraft(draft.copy(hue = it)) }
                Text("EFFECTS", color = TextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                AdjustSlider("Vignette", draft.vignette, 0f..1f) { setDraft(draft.copy(vignette = it)) }
                AdjustSlider("Sharpen", draft.sharpen, 0f..1f) { setDraft(draft.copy(sharpen = it)) }
                AdjustSlider("Grain", draft.grain, 0f..1f) { setDraft(draft.copy(grain = it)) }
                AdjustSlider("Film grain", draft.filmGrain, 0f..1f) { setDraft(draft.copy(filmGrain = it)) }
                Text(
                    "Reset all",
                    color = Accent,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable { state.adjustDraft = c.id to Adjust() }
                        .padding(vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PillBtn("Apply") {
                    state.applyDrafts()
                    state.panel = null
                }
                PillBtn("Apply all", Color(0xFF2B6CB0)) {
                    state.applyAdjustToAll(draft)
                    state.panel = null
                }
            }
        }
    }
}

/** FX ▸ Video fx — Speed। বদল সঙ্গে সঙ্গে ক্লিপে বসে (Undo করা যায়)। */
@Composable
private fun VideoFxPanel(state: EditorState, modifier: Modifier) {
    val c: Clip? = state.selectedClip?.takeIf { state.canVideoFx(it) || state.canBlend(it) }
        ?: state.clips.firstOrNull {
            (state.canVideoFx(it) || state.canBlend(it)) &&
                    state.playheadMs >= it.startMs && state.playheadMs < it.endMs
        }
    val shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)

    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(Color(0xF2191919), shape)
            .border(1.dp, Stroke, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FxTabs(state, 3, Modifier.weight(1f))
            FxCloseButton(state)
        }

        if (c == null) {
            Text(
                "Select a video clip (Video track or Overlay video) on the timeline to change its speed.",
                color = TextDim,
                fontSize = 11.sp
            )
        } else {
            Text(c.name, color = TextDim, fontSize = 10.sp, maxLines = 1)

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (state.supportsSpeed(c)) {
                    TextSectionTitle("SPEED")
                    SPEED_STEPS.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { sp ->
                                MiniChip(speedLabel(sp), c.speed == sp) { state.setClipSpeed(c.id, sp) }
                            }
                        }
                    }
                    Text(
                        String.format(Locale.getDefault(), "Clip length: %.1fs", c.durationMs / 1000f),
                        color = TextDim,
                        fontSize = 10.sp
                    )
                }

                if (state.canBlend(c)) {
                    TextSectionTitle("BLEND MODE")
                    ClipBlend.entries.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { blend ->
                                MiniChip(blend.label, c.blend == blend) {
                                    state.setClipBlend(c.id, blend)
                                }
                            }
                        }
                    }
                }

                // Reverse export support is not available.
            }
        }
    }
}

@Composable
private fun AdjustSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float> = -1f..1f,
    onChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 10.sp, modifier = Modifier.width(62.dp), maxLines = 1)
        Slider(
            value = value,
            onValueChange = { onChange(if (kotlin.math.abs(it) < 0.03f) 0f else it) },
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Accent,
                inactiveTrackColor = Color(0xFF444444)
            ),
            modifier = Modifier.weight(1f).height(24.dp)
        )
        Text(
            (value * 100).roundToInt().toString(),
            color = if (value == 0f) TextDim else Accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier
                .width(30.dp)
                .clickable { onChange(0f) }
        )
    }
}

/** স্লাইডারের সর্বোচ্চ সময়: যে ক্লিপগুলো প্রভাবিত হবে তাদের ছোটটির অর্ধেক */
private fun transitionMaxSec(state: EditorState, clip: Clip, tgt: TransTarget): Float {
    var minDur = clip.durationMs
    if (tgt == TransTarget.JOIN_PREV) state.prevAdjacent(clip)?.let { minDur = minOf(minDur, it.durationMs) }
    if (tgt == TransTarget.JOIN_NEXT) state.nextAdjacent(clip)?.let { minDur = minOf(minDur, it.durationMs) }
    return (minDur / 2000f).coerceIn(0.3f, 3f)
}

/** A ক্লিপ থেকে B ক্লিপে যাওয়ার লুপ-প্রিভিউ (প্রিভিউ ও এক্সপোর্টের একই হিসাব ব্যবহার করে) */
@Composable
private fun TransitionPreview(type: TransitionType, ms: Long, modifier: Modifier) {
    val d = ms.coerceAtLeast(100L)
    val hold = 700L
    val total = hold * 2 + d * 2
    var t by remember { mutableLongStateOf(0L) }
    LaunchedEffect(type, d) {
        val start = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            t = (now - start) % total
        }
    }
    val isA = t < hold + d
    val fx = when {
        type == TransitionType.NONE -> NO_FX
        t < hold -> NO_FX
        t < hold + d -> fxFor(type, 1f - (t - hold) / d.toFloat(), false)
        t < hold + 2 * d -> fxFor(type, (t - hold - d) / d.toFloat(), true)
        else -> NO_FX
    }

    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(1.dp, Stroke, RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = fx.scale
                    scaleY = fx.scale
                    translationX = fx.tx * size.width
                    translationY = fx.ty * size.height
                }
                .background(
                    Brush.linearGradient(
                        if (isA) listOf(Color(0xFF1E90FF), Color(0xFF6C5CE7))
                        else listOf(Color(0xFFFF9F43), Color(0xFFEE5253))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isA) "A" else "B", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
        }
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = fx.darken }.background(Color.Black))
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = fx.whiten }.background(Color.White))
    }
}

@Composable
private fun ChipBtn(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Accent else Color(0xFF2B2B2B))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun TransitionCard(type: TransitionType, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) Color(0xFF3A2024) else Color(0xFF2A2A2A))
            .border(1.5.dp, if (active) Accent else Stroke, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(type.icon, fontSize = 22.sp)
        Text(
            if (type == TransitionType.NONE) "Remove" else type.label,
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * বাটন চাপলেই একবার onStep; ৩৫০ms ধরে রাখলে বারবার চলে; ছেড়ে দিলেই থামে।
 * onStart একবারই চলে, তাই একটি press-and-hold-এ Undo entry-ও একটি হয়।
 */
@Composable
private fun Modifier.holdRepeat(onStart: () -> Unit, onStep: () -> Unit): Modifier {
    val scope = rememberCoroutineScope()
    val startNow by rememberUpdatedState(onStart)
    val stepNow by rememberUpdatedState(onStep)
    return this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            startNow()
            stepNow()
            val job = scope.launch {
                delay(350)
                while (true) {
                    stepNow()
                    delay(70)
                }
            }
            try {
                waitForUpOrCancellation()
            } finally {
                job.cancel()
            }
        }
    }
}

// ───────────────────────── Dedicated Volume & Audio Panel ─────────────────────────
@Composable
private fun VolumeAudioPanel(state: EditorState, modifier: Modifier) {
    val scrollState = rememberScrollState()
    val sel = state.selectedClip
    val currentTrack = state.tracks.firstOrNull { it.id == sel?.trackId }
    val hasAudio = currentTrack != null && (
            currentTrack.category == TrackCategory.VIDEO ||
                    currentTrack.category == TrackCategory.AUDIO ||
                    (currentTrack.category == TrackCategory.OVERLAY && sel?.isVideoOverlay == true)
            )

    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
            .background(Color(0xFF141414), RoundedCornerShape(8.dp))
            .border(1.dp, Stroke, RoundedCornerShape(8.dp))
            .padding(6.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // হেডার: Clip Mute ও Master Mute বাটন
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (sel != null && hasAudio) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (sel.volume == 0f) Color(0xFFE84118) else Color(0xFF2B2B2B))
                        .border(1.dp, Stroke, CircleShape)
                        .clickable { state.toggleClipMute(sel.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (sel.volume == 0f) "🎧‍✕" else "🎧",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (state.isAllMuted) Color(0xFFE84118) else Color(0xFF2B2B2B))
                    .border(1.dp, Stroke, CircleShape)
                    .clickable { state.toggleMuteAll() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.isAllMuted) "🔇" else "🔊",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        HorizontalDivider(color = Stroke, thickness = 0.5.dp)

        if (sel != null && hasAudio) {
            if (sel.isVideoOverlay && currentTrack?.category == TrackCategory.OVERLAY) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2B6CB0))
                        .clickable { state.showOverlayProps = true }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) { Text("⚙ Props", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
            }
            Text(
                text = "${(sel.volume * 100).toInt()}%",
                color = Color(0xFF1E90FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            // উল্লম্ব ভলিউম কন্ট্রোল: + উপরে, − নিচে
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(104.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, Stroke, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .holdRepeat(
                                onStart = { state.beginChange() },
                                onStep = { state.stepClipVolume(sel.id, 0.1f) }
                            ),
                        contentAlignment = Alignment.Center
                    ) { Text("+", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold) }

                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .weight(1f)
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF333333))
                    ) {
                        // ভলিউম বার (নিচ থেকে উপরে ভরে)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(sel.volume / 2f) // ০-২০% → ২.০x
                                .align(Alignment.BottomCenter)
                                .background(Color(0xFF1E90FF))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .holdRepeat(
                                onStart = { state.beginChange() },
                                onStep = { state.stepClipVolume(sel.id, -0.1f) }
                            ),
                        contentAlignment = Alignment.Center
                    ) { Text("−", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }

            // Quick reset বাটন
            Text(
                text = "⟲ 100%",
                color = TextDim,
                fontSize = 9.sp,
                modifier = Modifier
                    .clickable {
                        state.beginChange()
                        state.updateClipVolume(sel.id, 1.0f)
                    }
                    .padding(4.dp)
            )

            HorizontalDivider(color = Stroke, thickness = 0.5.dp)

            Text("FADE", color = TextDim, fontSize = 8.sp, fontWeight = FontWeight.Bold)

            // ফেড কন্ট্রোল — উল্লম্ব
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val fadeInText = if (sel.fadeInMs == 0L) "In: off" else "In: ${(sel.fadeInMs / 1000f)}s"
                val fadeOutText = if (sel.fadeOutMs == 0L) "Out: off" else "Out: ${(sel.fadeOutMs / 1000f)}s"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (sel.fadeInMs > 0) Color(0xFF1E90FF) else Color(0xFF222222))
                        .clickable { state.toggleFade(true) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏪ $fadeInText", fontSize = 8.sp, color = Color.White, maxLines = 1)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (sel.fadeOutMs > 0) Color(0xFF1E90FF) else Color(0xFF222222))
                        .clickable { state.toggleFade(false) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$fadeOutText ⏩", fontSize = 8.sp, color = Color.White, maxLines = 1)
                }
            }

        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No audio", color = Color(0xFF555555), fontSize = 10.sp)
                Text("clip selected", color = Color(0xFF555555), fontSize = 10.sp)
            }
        }
    }
}

// ───────────────────────── Side Info Panel ─────────────────────────
@Composable
private fun SideInfoPanel(state: EditorState, modifier: Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
            .background(Color(0xFF141414), RoundedCornerShape(8.dp))
            .border(1.dp, Stroke, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("TIME (${state.selectedRatio.label})", color = TextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(
                text = fmt(state.playheadMs),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total: ${fmt(state.totalMs)}",
                color = Color(0xFFAAAAAA),
                fontSize = 10.sp
            )
        }

        HorizontalDivider(color = Stroke, thickness = 0.5.dp)

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("ACTIVE TRACK", color = TextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            val current = state.tracks.firstOrNull { it.id == state.activeTrackId }
            Text(
                text = (current?.label ?: "None") + if (current?.isMuted == true) " (Muted)" else "",
                color = current?.color ?: Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = Stroke, thickness = 0.5.dp)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("PROPERTY", color = TextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            val sel = state.selectedClip
            if (sel != null) {
                Text(
                    text = sel.name,
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Dur: ${(sel.durationMs / 1000f)}s",
                    color = Color.White,
                    fontSize = 10.sp
                )
                if (sel.transIn != TransitionType.NONE) {
                    Text(
                        text = "In: ${sel.transIn.icon} ${sel.transIn.label}",
                        color = Color(0xFFFFD32A),
                        fontSize = 10.sp
                    )
                }
                if (sel.transOut != TransitionType.NONE) {
                    Text(
                        text = "Out: ${sel.transOut.icon} ${sel.transOut.label}",
                        color = Color(0xFFFFD32A),
                        fontSize = 10.sp
                    )
                }
            } else {
                Text("No clip selected", color = Color(0xFF555555), fontSize = 10.sp)
            }
        }

        HorizontalDivider(color = Stroke, thickness = 0.5.dp)

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("STATUS", color = TextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(
                text = state.status,
                color = Color.White,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TextInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String = "Add Text Overlay",
    initial: String = "",
    confirmLabel: String = "Add"
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Color.White, fontSize = 16.sp) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Type here...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Stroke
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextDim)
            }
        },
        containerColor = Color(0xFF222222)
    )
}

// ───────────────────────── Left action bar ─────────────────────────
@Composable
private fun SideActionBar(state: EditorState, onHomeClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RoundBtn("🏠") { onHomeClick() }
        RoundBtn("↶", enabled = state.canUndo) { state.undo() }
        RoundBtn("↷", enabled = state.canRedo) { state.redo() }
        RoundBtn(if (state.timelineVisible) "▾" else "▴") {
            state.timelineVisible = !state.timelineVisible
            state.status = if (state.timelineVisible) "Timeline shown" else "Timeline hidden"
        }
        RoundBtn("+") { state.zoomBy(0.25f) }
        RoundBtn("−") { state.zoomBy(-0.25f) }
    }
}

@Composable
private fun RoundBtn(label: String, size: Int = 30, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFF242424))
            .border(1.dp, Color(0xFF444444), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else Color(0xFF555555)
        )
    }
}

// ───────────────────────── Preview Area ─────────────────────────
@Composable
private fun PreviewArea(
    state: EditorState,
    exporter: ExportManager,
    onPickVideo: () -> Unit,
    onPickVideoFile: () -> Unit,
    onPickAudio: () -> Unit,
    onPickImage: () -> Unit,
    onPickOverlayVideo: () -> Unit,
    onPickImageFile: () -> Unit,
    onPickOverlayVideoFile: () -> Unit,
    onAddText: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val videoPlayer = rememberSyncedPlayer(state, TrackCategory.VIDEO)
    // প্রতিটি ওভারলে ট্র্যাকের নিজস্ব ভিডিও প্লেয়ার — দুটো ওভারলে ভিডিও একসাথে থাকলেও একে অন্যের প্লেয়ার কাড়ে না
    val overlayPlayers = rememberTrackPlayers(state, TrackCategory.OVERLAY, isOverlayVideo = true)
    // প্রতিটি অডিও ট্র্যাকের নিজস্ব প্লেয়ার — একাধিক অডিও ট্র্যাক একসাথে শোনা যায় (এক্সপোর্টের মতোই)
    rememberTrackPlayers(state, TrackCategory.AUDIO)

    val overlayTrackIds = remember(state.tracks) {
        state.tracks.filter { it.category == TrackCategory.OVERLAY }.map { it.id }.toSet()
    }

    // স্তরক্রম: নিচের ট্র্যাক আগে আঁকা, ওপরের ট্র্যাক পরে (উপরে), একই ট্র্যাকে id ধরে — এক্সপোর্টের সাথে একই নিয়ম।
    // state.clips-এর ক্রম বদলালেও (ক্লিপ সরালে) স্তরক্রম বদলায় না।
    val currentOverlays by remember(overlayTrackIds) {
        derivedStateOf {
            val order = state.tracks.filter { it.category == TrackCategory.OVERLAY }.map { it.id }
            state.clips.filter {
                it.trackId in overlayTrackIds &&
                        state.playheadMs >= it.startMs && state.playheadMs < it.endMs
            }.sortedWith(compareByDescending<Clip> { order.indexOf(it.trackId) }.thenBy { it.id })
        }
    }

    val activeVideoClip by remember {
        derivedStateOf {
            val pos = state.playheadMs
            state.tracks.filter { it.category == TrackCategory.VIDEO }.firstNotNullOfOrNull { t ->
                state.clips.firstOrNull {
                    it.trackId == t.id && it.uri != null && !it.missing && pos >= it.startMs && pos < it.endMs
                }
            }
        }
    }
    val activeVideoRotation by remember { derivedStateOf { activeVideoClip?.rotation ?: 0f } }

    val unit = state.previewHeightPx.toFloat()
    fun u2dp(v: Float): Dp = with(density) { v.toDp() }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .aspectRatio(state.selectedRatio.ratio)
                .onSizeChanged { state.previewHeightPx = it.height }
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(1.dp, Stroke, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            val ratio = state.selectedRatio.ratio
            val quarterTurn = abs(((activeVideoRotation % 180f) + 180f) % 180f - 90f) < 1f
            val fitScale = if (quarterTurn) minOf(ratio, 1f / ratio) else 1f

            val videoFxNow: () -> TransFx = {
                val vc = activeVideoClip
                if (vc == null) NO_FX else clipFx(vc, state.playheadMs - vc.startMs)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .pointerInput(state.selectedId, unit) {
                        awaitEachGesture {
                            awaitFirstDown(
                                requireUnconsumed = false,
                                pass = PointerEventPass.Initial
                            )
                            var begun = false
                            do {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val pressedCount = event.changes.count { it.pressed }
                                val selectedOverlay = state.selectedClip?.takeIf { state.canBlend(it) && !it.locked }
                                if (pressedCount >= 2 && selectedOverlay != null) {
                                    val zoomChange = event.calculateZoom()
                                    val pan = event.calculatePan()
                                    if (zoomChange != 1f || pan != Offset.Zero) {
                                        if (!begun) {
                                            state.beginChange()
                                            begun = true
                                        }
                                        val rad = Math.toRadians(selectedOverlay.rotation.toDouble())
                                        val cosA = cos(rad).toFloat()
                                        val sinA = sin(rad).toFloat()
                                        val realPanX = (pan.x * cosA - pan.y * sinA) * selectedOverlay.scale
                                        val realPanY = (pan.x * sinA + pan.y * cosA) * selectedOverlay.scale
                                        state.updateClipTransform(
                                            selectedOverlay.id,
                                            zoomChange,
                                            Offset(realPanX / unit, realPanY / unit)
                                        )
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                    // Blend Mode নিচের লেয়ারের পিক্সেলের সাথে মেশে, তাই এই স্তরকে আলাদা অফস্ক্রিন বাফারে আঁকা হয়
                    .then(
                        if (currentOverlays.any { it.blend != ClipBlend.NORMAL })
                            Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                val mainAdj = activeVideoClip?.let { state.effectiveAdj(it) } ?: Adjust()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val fx = videoFxNow()
                            rotationZ = activeVideoRotation
                            scaleX = fitScale * fx.scale
                            scaleY = fitScale * fx.scale
                            translationX = fx.tx * size.width
                            translationY = fx.ty * size.height
                        }
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    VideoSurface(videoPlayer, mainAdj, frameIndex = { state.playheadMs / 40L })
                    // গ্যাপের সময় আগের ফ্রেম আটকে না থেকে কালো দেখাবে
                    if (activeVideoClip == null) {
                        Box(Modifier.fillMaxSize().background(Color.Black))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = videoFxNow().darken }
                        .background(Color.Black)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = videoFxNow().whiten }
                        .background(Color.White)
                )

                // ব্যাকগ্রাউন্ড ট্যাপ (টাইমলাইন টগল এবং ডিসিলেক্ট)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { state.clearSelection() },
                                onDoubleTap = {
                                    state.timelineVisible = !state.timelineVisible
                                    state.status = if (state.timelineVisible) "Timeline shown" else "Big preview mode"
                                }
                            )
                        }
                )

                if (unit > 0f) {
                    currentOverlays.forEach { overlay ->
                        key(overlay.id) {
                            val isSelected = state.selectedId == overlay.id
                            val latestOverlay by rememberUpdatedState(overlay)
                            val latestUnit by rememberUpdatedState(unit)

                            Box(
                                modifier = Modifier
                                    .offset {
                                        val fx = clipFx(overlay, state.playheadMs - overlay.startMs)
                                        IntOffset(
                                            (overlay.offsetX * unit + fx.tx * unit * ratio).roundToInt(),
                                            (overlay.offsetY * unit + fx.ty * unit).roundToInt()
                                        )
                                    }
                                    .then(
                                        if (overlay.blend == ClipBlend.NORMAL) Modifier
                                        else Modifier.drawWithContent {
                                            drawIntoCanvas { canvas ->
                                                val paint = ComposePaint().apply { blendMode = overlay.blend.mode }
                                                canvas.saveLayer(Rect(-100000f, -100000f, 100000f, 100000f), paint)
                                                drawContent()
                                                canvas.restore()
                                            }
                                        }
                                    )
                                    .graphicsLayer {
                                        val fx = clipFx(overlay, state.playheadMs - overlay.startMs)
                                        val s = overlay.scale * fx.scale
                                        scaleX = s
                                        scaleY = s
                                        rotationZ = overlay.rotation
                                        alpha = (((1f - fx.darken) * (1f - fx.whiten)) * overlay.opacity).coerceIn(0f, 1f)
                                    }
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, Color(0xFF1E90FF), RoundedCornerShape(6.dp))
                                        else Modifier
                                    )
                                    .pointerInput(overlay.id) {
                                        awaitEachGesture {
                                            awaitFirstDown(requireUnconsumed = false)
                                            state.selectClip(latestOverlay.id)
                                            var begun = false
                                            do {
                                                val event = awaitPointerEvent()
                                                val zoomChange = event.calculateZoom()
                                                val pan = event.calculatePan()
                                                if (!latestOverlay.locked &&
                                                    event.changes.count { it.pressed } == 1 &&
                                                    (zoomChange != 1f || pan != Offset.Zero)) {
                                                    if (!begun) {
                                                        state.beginChange()
                                                        begun = true
                                                    }
                                                    val o = latestOverlay
                                                    val rad = Math.toRadians(o.rotation.toDouble())
                                                    val cosA = cos(rad).toFloat()
                                                    val sinA = sin(rad).toFloat()
                                                    val realPanX = (pan.x * cosA - pan.y * sinA) * o.scale
                                                    val realPanY = (pan.x * sinA + pan.y * cosA) * o.scale
                                                    val uPx = latestUnit
                                                    state.updateClipTransform(
                                                        o.id,
                                                        zoomChange,
                                                        Offset(realPanX / uPx, realPanY / uPx)
                                                    )
                                                    event.changes.forEach { it.consume() }
                                                }
                                            } while (event.changes.any { it.pressed })
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (overlay.isVideoOverlay) {
                                    val ovPlayer = overlayPlayers[overlay.trackId]
                                    if (ovPlayer != null && !overlay.missing) {
                                        VideoSurface(
                                            ovPlayer,
                                            state.effectiveAdj(overlay),
                                            modifier = Modifier
                                                .size(u2dp(VID_W * unit), u2dp(VID_H * unit))
                                                .clip(RoundedCornerShape(4.dp)),
                                            frameIndex = { state.playheadMs / 40L }
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(u2dp(VID_W * unit), u2dp(VID_H * unit))
                                                .background(Color(0x66000000), RoundedCornerShape(4.dp))
                                        )
                                    }
                                } else if (overlay.uri != null) {
                                    // বড় ছবি মেইন থ্রেডে ডিকোড করলে জার্ক হয় — তাই ব্যাকগ্রাউন্ডে
                                    val bitmapState = produceState<Bitmap?>(initialValue = null, overlay.uri) {
                                        value = withContext(Dispatchers.IO) {
                                            decodeBitmapSampled(context, overlay.uri, 1024)
                                        }
                                    }
                                    val bitmap = bitmapState.value
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Overlay Image",
                                            modifier = Modifier.size(u2dp(IMG_BOX * unit)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                } else if (overlay.textOverlay != null) {
                                    StyledTextOverlay(overlay.textOverlay, state.effectiveTextStyle(overlay), unit)
                                }
                            }
                        }
                    }
                }

                if (state.clips.isEmpty()) {
                    Text(
                        text = "Tap Media ▸ Add video",
                        color = Color(0xFF666666),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            state.panel?.let { p ->
                if (p != FX_PANEL && p != FX_ADJUST_PANEL && p != FX_FILTER_PANEL && p != FX_TEXT_PANEL && p != FX_VIDEO_PANEL) PanelOverlay(
                    state = state,
                    exporter = exporter,
                    panel = p,
                    onPickVideo = onPickVideo,
                    onPickVideoFile = onPickVideoFile,
                    onPickAudio = onPickAudio,
                    onPickImage = onPickImage,
                    onPickOverlayVideo = onPickOverlayVideo,
                    onPickImageFile = onPickImageFile,
                    onPickOverlayVideoFile = onPickOverlayVideoFile,
                    onAddText = onAddText,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun MediaSelectionDialog(
    uris: List<Uri>,
    previewUri: Uri?,
    onPreview: (Uri) -> Unit,
    onBackToList: () -> Unit,
    onAddOne: (Uri) -> Unit,
    onAddAll: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(player) { onDispose { player.release() } }

    LaunchedEffect(previewUri) {
        player.stop()
        player.clearMediaItems()
        if (previewUri != null) {
            player.setMediaItem(MediaItem.fromUri(previewUri))
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.prepare()
            player.playWhenReady = true
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp)
                .background(Color(0xFF171717), RoundedCornerShape(16.dp))
                .border(1.dp, Stroke, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (previewUri != null) {
                Text("Video Preview", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                VideoSurface(
                    player = player,
                    adj = Adjust(),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .background(Color.Black, RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onBackToList,
                        modifier = Modifier.weight(1f)
                    ) { Text("Back") }
                    Button(
                        onClick = { onAddOne(previewUri) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) { Text("Add to Timeline") }
                }
            } else {
                Text("Select videos (${uris.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Tap a clip to preview it. Your selection stays here when you go back.",
                    color = TextDim, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                    items(uris, key = { it.toString() }) { uri ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 5.dp)
                                .background(Color(0xFF252525), RoundedCornerShape(8.dp))
                                .clickable { onPreview(uri) }.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(uri.lastPathSegment ?: "Video", color = Color.White,
                                modifier = Modifier.weight(1f), maxLines = 2)
                            Text("Preview  ›", color = Accent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Back") }
                    Button(
                        onClick = onAddAll,
                        enabled = uris.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) { Text("Add all to Timeline") }
                }
            }
        }
    }
}

@Composable
private fun PanelOverlay(
    state: EditorState,
    exporter: ExportManager,
    panel: String,
    onPickVideo: () -> Unit,
    onPickVideoFile: () -> Unit,
    onPickAudio: () -> Unit,
    onPickImage: () -> Unit,
    onPickOverlayVideo: () -> Unit,
    onPickImageFile: () -> Unit,
    onPickOverlayVideoFile: () -> Unit,
    onAddText: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .padding(8.dp)
            .background(Color(0xEE1E1E1E), RoundedCornerShape(10.dp))
            .border(1.dp, Stroke, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(panel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        when (panel) {
            "Media" -> {
                Text("Adds to a Video track", color = TextDim, fontSize = 10.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillBtn("Gallery") { state.panel = null; onPickVideo() }
                    PillBtn("Files") { state.panel = null; onPickVideoFile() }
                }
            }
            "Overlay" -> {
                Text("Add Overlay from:", color = TextDim, fontSize = 10.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PillBtn("🖼 Gallery") { state.panel = null; onPickImage() }
                    PillBtn("📁 Files") { state.panel = null; onPickImageFile() }
                }
                PillBtn("✍ Text") { state.panel = null; onAddText() }
            }
            "Project" -> {
                Text(state.projectName, color = TextDim, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillBtn("Save now") {
                        val ok = ProjectStorage.save(context, state)
                        state.status = if (ok) "Project saved" else "Save failed"
                        state.panel = null
                    }
                    PillBtn("Clear all") { state.panel = "Clear project?" }
                }
                Text("Your work is also saved automatically", color = TextDim, fontSize = 10.sp)
            }
            "Clear project?" -> {
                Text("Remove all clips from this project?\n(You can undo this.)", color = TextDim, fontSize = 11.sp, textAlign = TextAlign.Center)
                PillBtn("Yes, clear") {
                    state.clearAllClips()
                    state.panel = null
                }
            }
            "Audio" -> {
                Text("Adds to an Audio track", color = TextDim, fontSize = 10.sp)
                PillBtn("+ Add audio file") { state.panel = null; onPickAudio() }
            }
            "Export" -> {
                if (exporter.running) {
                    Text(
                        "Exporting ${(exporter.progress * 100).toInt()}% - keep the app open",
                        color = TextDim,
                        fontSize = 11.sp
                    )
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF333333))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(exporter.progress.coerceIn(0f, 1f))
                                .background(Accent)
                        )
                    }
                    PillBtn("Cancel export", Color(0xFFE84118)) { exporter.cancel() }
                } else {
                    Text("Save MP4 (${state.selectedRatio.label}):", color = TextDim, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PillBtn("480p") { exporter.start(480) }
                        PillBtn("720p") { exporter.start(720) }
                        PillBtn("1080p") { exporter.start(1080) }
                    }
                }
            }
            else -> Text("Effects coming soon", color = TextDim, fontSize = 11.sp)
        }
        PillBtn("Close", Color(0xFF333333)) { state.panel = null }
    }
}

@Composable
private fun PillBtn(text: String, color: Color = Accent, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ───────────────────────── Tool wheel ─────────────────────────
@Composable
private fun RightPanel(state: EditorState, modifier: Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val wheelSize = (maxHeight - 86.dp).coerceIn(90.dp, 124.dp)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolWheel(state, Modifier.size(wheelSize))
            TransportControls(state)
        }
    }
}

@Composable
private fun TransportControls(state: EditorState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(fmt(state.playheadMs), color = Color.White, fontSize = 11.sp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EdgeButton(forward = false, buttonSize = 34.dp) { state.jumpToPrevEdit() }
            PlayButton(state.isPlaying, buttonSize = 40.dp) { state.isPlaying = !state.isPlaying }
            EdgeButton(forward = true, buttonSize = 34.dp) { state.jumpToNextEdit() }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeEndButton("Home") { state.goToStart() }
            HomeEndButton("End") { state.goToEnd() }
        }
    }
}

@Composable
private fun HomeEndButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(24.dp)
            .widthIn(min = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2B2B2B))
            .border(1.dp, Color(0xFF444444), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ToolWheel(state: EditorState, modifier: Modifier) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val d = minOf(maxHeight, maxWidth, 190.dp)
        Box(
            modifier = Modifier
                .size(d)
                .background(Color(0xFF222222), CircleShape)
                .border(3.dp, Stroke, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            WheelBtn("🎬", Modifier.align(Alignment.TopCenter).offset(y = 4.dp)) { state.panel = "Media" }
            WheelBtn("🖼️", Modifier.align(Alignment.CenterStart).offset(x = 4.dp)) { state.panel = "Overlay" }
            WheelBtn("🎵", Modifier.align(Alignment.CenterEnd).offset(x = (-4).dp)) { state.panel = "Audio" }
            WheelBtn("📤", Modifier.align(Alignment.BottomCenter).offset(y = (-4).dp)) { state.panel = "Export" }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Accent, CircleShape)
                    .border(1.5.dp, Stroke, CircleShape)
                    .clickable {
                        state.transitionJoinClipId = null
                        state.panel = FX_ADJUST_PANEL
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("FX", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WheelBtn(icon: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF2B2B2B))
            .border(1.dp, Color(0xFF444444), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = 18.sp, color = Color.White)
    }
}

// ───────────────────────── Timeline ─────────────────────────
private val RulerHeight = 14.dp
private val TransitionLaneHeight = 12.dp
private val TrackHeight = 40.dp
private val ToolbarHeight = 28.dp

@Composable
private fun Timeline(state: EditorState) {
    // Toolbar + Ruler + ৩টি Track + সামান্য মার্জিন
    val visibleTimelineHeight = ToolbarHeight + RulerHeight + TrackHeight * 3 + 2.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(visibleTimelineHeight)
            .background(Color(0xFF141414))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { state.clearSelection() })
            }
    ) {
        TimelineToolbar(state)

        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val verticalTrackScroll = rememberScrollState()

            Column(
                modifier = Modifier
                    .width(124.dp)
                    .fillMaxHeight()
                    .background(BarDark)
            ) {
                Spacer(Modifier.height(RulerHeight))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(verticalTrackScroll)
                ) {
                    state.tracks.forEach { track ->
                        val isDefaultTrack = track.id == "V1" || track.id == "O1" || track.id == "A1"
                        TrackHeader(
                            track = track,
                            isActive = track.id == state.activeTrackId,
                            selected = track.id in state.navTracks,
                            isDeletable = !isDefaultTrack,
                            onClick = { state.selectTrack(track.id) },
                            onToggleNav = { state.toggleNavTrack(track.id) },
                            onToggleMute = { state.toggleTrackMute(track.id) },
                            onToggleLock = { state.toggleTrackLock(track.id) },
                            onDelete = { state.deleteTrack(track.id) }
                        )
                    }
                }
            }

            val scroll = rememberScrollState()
            val dpPerSec = state.dpPerSec
            val contentWidth = ((state.totalMs / 1000f) * dpPerSec).dp
            val locked = state.playheadLocked
            val density = LocalDensity.current.density
            var viewportPx by remember { mutableIntStateOf(0) }
            val halfDp = (viewportPx / 2f / density).dp
            var synced by remember { mutableStateOf(false) }
            // কোড নিজে যে px-এ স্ক্রল করেছে সেটা মনে রাখি, যাতে সেটাকে ইউজারের স্ক্রল ভেবে প্লেহেড পেছনে টেনে না আনে
            val progPx = remember { intArrayOf(-1) }

            LaunchedEffect(locked, dpPerSec, viewportPx) {
                synced = false
                if (locked && viewportPx > 0) {
                    withFrameNanos { }
                    withFrameNanos { }
                    val first = ((state.playheadMs / 1000f) * dpPerSec * density).roundToInt()
                    progPx[0] = first.coerceIn(0, scroll.maxValue)
                    scroll.scrollTo(first)
                    synced = true
                }
            }

            LaunchedEffect(locked, dpPerSec) {
                if (!locked) return@LaunchedEffect
                val msPerPx = 1000f / (dpPerSec * density)
                snapshotFlow { scroll.value }.collect { px ->
                    if (synced && px != progPx[0]) {
                        val ms = (px * msPerPx).toLong()
                        if (abs(ms - state.playheadMs) > msPerPx) state.seek(ms)
                    }
                }
            }

            LaunchedEffect(locked, dpPerSec) {
                if (!locked) return@LaunchedEffect
                snapshotFlow { state.playheadMs }.collect { ms ->
                    if (synced && !scroll.isScrollInProgress) {
                        val target = ((ms / 1000f) * dpPerSec * density).coerceIn(0f, scroll.maxValue.toFloat())
                        if (abs(target - scroll.value) > 1.5f) {
                            val t = target.roundToInt()
                            progPx[0] = t
                            scroll.scrollTo(t)
                        }
                    }
                }
            }

            LaunchedEffect(state.jumpTick) {
                if (state.jumpTick > 0 && !locked) {
                    val target = ((state.playheadMs / 1000f) * dpPerSec * density).roundToInt() - (viewportPx / 2)
                    scroll.animateScrollTo(target.coerceAtLeast(0))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .onSizeChanged { viewportPx = it.width }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            do {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.size >= 2) {
                                    val zoomChange = event.calculateZoom()
                                    if (zoomChange != 1f) {
                                        state.changeZoom(state.zoom * zoomChange)
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                    .background(TrackBg)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { state.clearSelection() })
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize().horizontalScroll(scroll)) {
                    Row(modifier = Modifier.fillMaxHeight()) {
                        if (locked) Spacer(Modifier.width(halfDp))
                        Column(modifier = Modifier.width(contentWidth).fillMaxHeight()) {
                            Ruler(state, contentWidth)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(verticalTrackScroll)
                            ) {
                                state.tracks.forEach { track ->
                                    TrackRow {
                                        state.gapsOf(track.id).forEach { gap ->
                                            GapBlock(state, gap)
                                        }
                                        // নির্বাচিত Clip-টি সর্বশেষে আঁকা হবে, যাতে পাশাপাশি থাকা
                                        // পরের Clip তার Trim Handle-এর Touch Area ঢেকে না ফেলে।
                                        state.clips
                                            .filter { it.trackId == track.id }
                                            .sortedBy { it.id == state.selectedId }
                                            .forEach { clip ->
                                                // প্রতিটি ক্লিপের Compose identity স্থির রাখি।
                                                // Selection/drag-এর সময় sorting বদলালেও gesture state অন্য ক্লিপে যাবে না।
                                                key(clip.id) {
                                                    val hasAudioLane = clip.uri != null && clip.textOverlay == null &&
                                                            (track.category != TrackCategory.OVERLAY || clip.isVideoOverlay)
                                                    TimelineClip(state, clip, track.color, showWave = hasAudioLane, muted = track.isMuted, editable = !track.isLocked)
                                                }
                                            }
                                        // Transition "+" বাটন: ক্লিপের উপরে (zIndex বেশি), আলাদা strip ছাড়া
                                        TransitionButtons(state, track)
                                    }
                                }
                            }
                        }
                        if (locked) Spacer(Modifier.width(halfDp))
                    }
                    state.snapGuideMs?.let { guideMs ->
                        Box(
                            modifier = Modifier
                                .offset(x = ((guideMs / 1000f) * dpPerSec).dp + (if (locked) halfDp else 0.dp))
                                .width(1.5.dp)
                                .fillMaxHeight()
                                .background(Color(0xFFFFD54F))
                        )
                    }
                    if (!locked) Playhead(state)
                }
                if (locked) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Accent)
                    )
                }
            }
        }
    }
}
//TIMELINE TOOLBAR ======**********--============
@Composable
private fun TimelineToolbar(state: EditorState) {
    var showLayerMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ToolbarHeight)
            .background(BarDark)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add Layer বাটন
            Box {
                ToolBtn("+ Add Layer", active = showLayerMenu) { showLayerMenu = !showLayerMenu }

                // ড্রপডাউন মেনু
                DropdownMenu(
                    expanded = showLayerMenu,
                    onDismissRequest = { showLayerMenu = false },
                    modifier = Modifier.background(Color(0xFF2B2B2B))
                ) {
                    DropdownMenuItem(
                        text = { Text("Video Layer", color = Color.White, fontSize = 13.sp) },
                        onClick = {
                            state.addVideoTrack()
                            showLayerMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Overlay Layer", color = Color.White, fontSize = 13.sp) },
                        onClick = {
                            state.addOverlayTrack()
                            showLayerMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Audio Layer", color = Color.White, fontSize = 13.sp) },
                        onClick = {
                            state.addAudioTrack()
                            showLayerMenu = false
                        }
                    )
                }
            }

            ToolBtn("Trim L") { state.trimLeft() }
            ToolBtn("Trim R") { state.trimRight() }
            ToolBtn("Split") { state.split() }
            ToolBtn("Delete") { state.deleteSelected() }

            val sel = state.selectedClip
            val selCategory = state.tracks.firstOrNull { it.id == sel?.trackId }?.category
            if (sel != null && selCategory != null && selCategory != TrackCategory.AUDIO) {
                ToolBtn("⟲ -90°") { state.rotateSelectedClip(-90f) }
                ToolBtn("⟳ +90°") { state.rotateSelectedClip(90f) }
            }

            ToolBtn("Ripple", active = state.rippleOn) { state.toggleRipple() }
            ToolBtn("Lock", active = state.playheadLocked) { state.togglePlayheadLock() }
            ToolBtn("Project") { state.panel = "Project" }
        }
    }
}

@Composable
private fun ToolBtn(text: String, active: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) Accent else Color(0xFF2B2B2B))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 11.sp, color = Color.White)
    }
}

@Composable
private fun PlayButton(isPlaying: Boolean, buttonSize: Dp = 30.dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(if (isPlaying) Color(0xFFE84118) else Accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(buttonSize * 0.45f)) {
            if (isPlaying) {
                val w = size.width / 3f
                drawRect(Color.White, topLeft = Offset(0f, 0f), size = Size(w, size.height))
                drawRect(Color.White, topLeft = Offset(w * 2, 0f), size = Size(w, size.height))
            } else {
                val p = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(p, Color.White)
            }
        }
    }
}

@Composable
private fun EdgeButton(forward: Boolean, buttonSize: Dp = 30.dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(Color(0xFF2B2B2B))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(buttonSize * 0.4f)) {
            val w = size.width
            val h = size.height
            val barW = w * 0.18f
            val tri = Path().apply {
                if (forward) {
                    moveTo(0f, 0f); lineTo(w * 0.78f, h / 2f); lineTo(0f, h)
                } else {
                    moveTo(w, 0f); lineTo(w * 0.22f, h / 2f); lineTo(w, h)
                }
                close()
            }
            drawPath(tri, Color.White)
            if (forward) {
                drawRect(Color.White, topLeft = Offset(w - barW, 0f), size = Size(barW, h))
            } else {
                drawRect(Color.White, topLeft = Offset(0f, 0f), size = Size(barW, h))
            }
        }
    }
}

@Composable
private fun TrackHeader(
    track: TrackInfo,
    extraHeight: Dp = 0.dp,
    isActive: Boolean,
    selected: Boolean,
    isDeletable: Boolean,
    onClick: () -> Unit,
    onToggleNav: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleLock: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TrackHeight + extraHeight)
            .background(if (isActive) track.color.copy(alpha = 0.28f) else Color.Transparent)
            .border(
                width = if (isActive) 1.dp else 0.5.dp,
                color = if (isActive) track.color else Color(0xFF222222)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (track.isMuted) Color(0xFFE84118) else Color(0xFF333333))
                        .clickable(onClick = onToggleMute),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (track.isLocked) Color(0xFFB7791F) else Color(0xFF333333))
                        .clickable(onClick = onToggleLock),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (track.isLocked) "L" else "U", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = if (selected) "●" else "○",
                    fontSize = 10.sp,
                    color = track.color,
                    modifier = Modifier.clickable(onClick = onToggleNav)
                )

                Text(
                    text = track.label,
                    fontSize = 9.sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (isActive) Color.White else Color(0xFF888888),
                    maxLines = 1
                )
            }

            if (isDeletable) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackRow(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TrackHeight)
            .border(0.5.dp, Color(0xFF222222)),
        contentAlignment = Alignment.CenterStart,
        content = content
    )
}

@Composable
private fun Ruler(state: EditorState, width: Dp) {
    val dpPerSec = state.dpPerSec
    val totalSec = (state.totalMs / 1000f).toInt().coerceAtLeast(1)
    val majorStep = when {
        dpPerSec >= 90f -> 1
        dpPerSec >= 45f -> 2
        dpPerSec >= 22f -> 5
        dpPerSec >= 10f -> 10
        else -> 30
    }
    val minorStep = when {
        majorStep == 1 -> 0.25f
        majorStep == 2 -> 0.5f
        majorStep == 5 -> 1f
        majorStep == 10 -> 2f
        else -> 5f
    }
    Box(
        modifier = Modifier
            .width(width)
            .height(RulerHeight)
            .background(Color(0xFF191D26))
            .pointerInput(dpPerSec, state.playheadLocked) {
                if (state.playheadLocked) return@pointerInput
                detectTapGestures { pos ->
                    state.seek((pos.x / density / dpPerSec * 1000).toLong())
                }
            }
            .pointerInput(dpPerSec, state.playheadLocked) {
                if (state.playheadLocked) return@pointerInput
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    state.seek((change.position.x / density / dpPerSec * 1000).toLong())
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var t = 0f
            while (t <= totalSec) {
                val x = t * dpPerSec.dp.toPx()
                val isMajor = (t % majorStep.toFloat()) < 0.001f
                val tickHeight = if (isMajor) size.height * 0.72f else size.height * 0.32f
                drawLine(
                    color = if (isMajor) Color(0xFF9AA4B2) else Color(0xFF596273),
                    start = Offset(x, size.height),
                    end = Offset(x, size.height - tickHeight),
                    strokeWidth = if (isMajor) 1.2.dp.toPx() else 0.7.dp.toPx()
                )
                t += minorStep
            }
        }
        for (sec in 0..totalSec step majorStep) {
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60),
                color = Color(0xFFD0D6E0),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier
                    .offset(x = (sec * dpPerSec).dp + 3.dp, y = 0.dp)
                    .align(Alignment.TopStart)
            )
        }
    }
}

@Composable
private fun TimelineClip(state: EditorState, clip: Clip, trackColor: Color, showWave: Boolean, muted: Boolean, editable: Boolean = true) {
    val dpPerSec = state.dpPerSec
    val selected = state.selectedId == clip.id
    val haptic = LocalHapticFeedback.current
    var isDragging by remember(clip.id) { mutableStateOf(false) }
    // ড্র্যাগের সময় আঙুলের হুবহু অবস্থান (ms) — ক্লিপ এই জায়গায় আঁকা হয়, তাই আঙুলের সাথে ১:১ চলে
    var dragRawMs by remember(clip.id) { mutableFloatStateOf(0f) }
    val widthDp = ((clip.durationMs / 1000f) * dpPerSec).dp
    val xDp = ((clip.startMs / 1000f) * dpPerSec).dp
    // অন্য ক্লিপগুলো সরে যাওয়ার সময় নরম স্প্রিং অ্যানিমেশন (আগে 1800 ছিল, এখন 800 — বেশি স্মুথ)।
    // যে ক্লিপটি টানা হচ্ছে সেটি সরাসরি xDp ব্যবহার করে, তাই আঙুলের সাথে সাথে চলে।
    val animatedXDp by animateDpAsState(
        targetValue = xDp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 800f
        ),
        label = "clipX"
    )
    val latestClip by rememberUpdatedState(clip)
    // নির্বাচিত ক্লিপ "ভেসে ওঠে": লম্বা, ছায়াসহ, উজ্জ্বল; ডিসিলেক্ট করলে আগের ছোট আকারে ফেরে
    val clipH by animateDpAsState(if (selected) TrackHeight - 2.dp else TrackHeight - 6.dp, label = "clipH")
    // TrackRow ক্লিপকে আগেই ভার্টিক্যালি মাঝে বসায় — তাই এখানে আলাদা y-offset দেওয়া হয় না (নইলে ক্লিপ নিচে সরে যায়)

    // সিলেক্ট থাকলে দুই পাশে এই চওড়ার ট্রিম হ্যান্ডেল বসে — এই অংশ থেকে মুভ শুরু হবে না
    val handleW = 20.dp

    Box(
        modifier = Modifier
            .offset {
                val shownX = if (isDragging) ((dragRawMs.coerceAtLeast(0f) / 1000f) * dpPerSec).dp else animatedXDp
                IntOffset(shownX.roundToPx(), 0)
            }
            // নির্বাচিত Clip-কে একই Track-এর অন্য Clip-এর উপরে রাখে।
            .zIndex(if (selected) 2f else 0f)
            .width(widthDp)
            .height(clipH)
            .shadow(if (selected) 10.dp else 0.dp, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (clip.missing) Color(0xFF555555)
                else trackColor.copy(alpha = if (selected) 1f else 0.78f)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (clip.missing) Color(0xFFFFC312)
                else if (selected) Color.White else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            // শুধু ট্যাপ করলে সিলেক্ট হয় — মুভ হয় না
            .clickable(enabled = editable) {
                state.selectClip(clip.id)
            }
    ) {
        // ── "বডি" লেয়ার: শুধু এখান থেকেই লং-প্রেস করে মুভ শুরু করা যায় ──
        // সিলেক্ট থাকলে দুই পাশের হ্যান্ডেল এলাকা বাদ থাকে, তাই কিনারা ধরলে ক্লিপ নড়ে না।
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = if (selected) handleW else 0.dp)
                .pointerInput(clip.id, dpPerSec, editable) {
                    if (!editable) return@pointerInput
                    var rawStart = 0L
                    var dragY = 0f
                    var sourceTrackIndex = -1
                    var changed = false
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            rawStart = latestClip.startMs
                            dragRawMs = rawStart.toFloat()
                            dragY = 0f
                            sourceTrackIndex = state.tracks.indexOfFirst { it.id == latestClip.trackId }
                            changed = false
                            state.selectClip(clip.id)
                            state.beginClipMove(clip.id)
                            isDragging = true
                            // চেপে ধরার সময় পূর্ণ হলেই একবার হালকা ভাইব্রেশন
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDragEnd = {
                            isDragging = false
                            state.finishClipMove()
                            state.clearSnapGuide()
                        },
                        onDragCancel = {
                            isDragging = false
                            state.finishClipMove()
                            state.clearSnapGuide()
                        },
                        onDrag = { change, drag ->
                            change.consume()
                            if (!changed) {
                                state.beginChange()
                                changed = true
                            }
                            // দশমিকসহ জমা রাখি যাতে আঙুলের সাথে একটুও পিছিয়ে না পড়ে
                            dragRawMs += drag.x / density / dpPerSec * 1000f
                            rawStart = dragRawMs.toLong()
                            dragY += drag.y
                            val snapMs = (SNAP_DP / dpPerSec * 1000).toLong()
                            val rowHeightPx = TrackHeight.toPx().coerceAtLeast(1f)
                            val targetIndex = (sourceTrackIndex + (dragY / rowHeightPx).roundToInt())
                                .coerceIn(0, state.tracks.lastIndex.coerceAtLeast(0))
                            val sourceCategory = state.tracks.firstOrNull { it.id == latestClip.trackId }?.category
                            val targetTrack = state.tracks.getOrNull(targetIndex)
                            val targetTrackId = targetTrack?.takeIf {
                                it.category == sourceCategory && !it.isLocked
                            }?.id ?: latestClip.trackId
                            state.moveClip(clip.id, rawStart, snapMs, targetTrackId)
                        }
                    )
                }
        )

        if (showWave) {
            val peaks = rememberWaveform(clip.uri)
            if (peaks != null && peaks.isNotEmpty()) {
                val vol = if (muted) 0f else clip.volume
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    if (w <= 0f || h <= 0f) return@Canvas
                    val stepPx = 3.dp.toPx()
                    val barPx = 2.dp.toPx()
                    val firstPeak = clip.inMs / 1000f * PEAKS_PER_SEC
                    val peaksPerPx = (clip.sourceSpanMs / 1000f * PEAKS_PER_SEC) / w
                    val color = Color.White.copy(alpha = if (muted) 0.25f else 0.55f)
                    var x = 0f
                    while (x < w) {
                        val i0 = (firstPeak + x * peaksPerPx).toInt().coerceIn(0, peaks.size - 1)
                        val i1 = (firstPeak + (x + stepPx) * peaksPerPx).toInt().coerceIn(i0, peaks.size - 1)
                        var m = 0f
                        for (i in i0..i1) if (peaks[i] > m) m = peaks[i]
                        // হালকা বুস্ট (নিচু শব্দও দেখা যাবে) + ক্লিপের ভলিউম অনুযায়ী উচ্চতা
                        val level = (Math.pow(m.toDouble(), 0.6).toFloat() * (if (muted) 1f else vol)).coerceIn(0f, 1f)
                        val barH = (level * h * 0.9f).coerceAtLeast(1f)
                        drawRect(color, topLeft = Offset(x, (h - barH) / 2f), size = Size(barPx, barH))
                        x += stepPx
                    }
                }
            }
        }

        // ট্রান্সিশনের চিহ্ন: ক্লিপের শুরু/শেষে হালকা সবুজ অংশ। চাপলে নির্বাচিত হয়, Delete চাপলে মুছে যায়।
        // অডিও ক্লিপে একই চিহ্ন দিয়ে ফেড (In/Out) দেখানো হয়।
        val isAudioClip = state.tracks.firstOrNull { it.id == clip.trackId }?.category == TrackCategory.AUDIO
        val inMsShown = if (isAudioClip) clip.fadeInMs else if (clip.transIn != TransitionType.NONE) clip.transInMs else 0L
        val outMsShown = if (isAudioClip) clip.fadeOutMs else if (clip.transOut != TransitionType.NONE) clip.transOutMs else 0L
        val inIcon = if (isAudioClip) "🔉" else clip.transIn.icon
        val outIcon = if (isAudioClip) "🔉" else clip.transOut.icon

        if (inMsShown > 0L) {
            val mw = ((minOf(inMsShown, clip.durationMs / 2) / 1000f) * dpPerSec).dp.coerceAtLeast(26.dp)
            val sel = state.selectedTransition == (clip.id to true)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(mw)
                    .fillMaxHeight()
                    .background(Color(0xFF2ED573).copy(alpha = if (sel) 0.8f else 0.38f))
                    .border(if (sel) 2.dp else 0.5.dp, Color(0xFF2ED573))
                    .clickable { state.selectTransition(clip.id, true) },
                contentAlignment = Alignment.Center
            ) {
                Text(inIcon, fontSize = 9.sp, maxLines = 1)
            }
        }
        if (outMsShown > 0L) {
            val mw = ((minOf(outMsShown, clip.durationMs / 2) / 1000f) * dpPerSec).dp.coerceAtLeast(26.dp)
            val sel = state.selectedTransition == (clip.id to false)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(mw)
                    .fillMaxHeight()
                    .background(Color(0xFF2ED573).copy(alpha = if (sel) 0.8f else 0.38f))
                    .border(if (sel) 2.dp else 0.5.dp, Color(0xFF2ED573))
                    .clickable { state.selectTransition(clip.id, false) },
                contentAlignment = Alignment.Center
            ) {
                Text(outIcon, fontSize = 9.sp, maxLines = 1)
            }
        }

        Text(
            (if (clip.missing) "⚠ " else "") +
                    (if (clip.speed != 1f) "${clip.name} · ${speedLabel(clip.speed)}" else clip.name),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = if (selected) 22.dp else 6.dp)
        )

        // ── ট্রিম হ্যান্ডেল (সিলেক্ট থাকলেই দেখা যায়) — এগুলো বডি লেয়ারের উপরে আঁকা,
        // তাই কিনারায় টাচ করলে শুধু ট্রিম হয়, মুভ হয় না ──
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(handleW)
                    .fillMaxHeight()
                    .background(Color(0xFF202020))
                    .border(1.5.dp, Color.White, RoundedCornerShape(2.dp))
                    .pointerInput(clip.id, dpPerSec) {
                        // আঙুলের মোট সরণ জমা রাখি; snap শুধু চূড়ান্ত অবস্থানে প্রযোজ্য।
                        var rawStart = 0L
                        detectHorizontalDragGestures(
                            onDragStart = { rawStart = latestClip.startMs; state.beginChange() },
                            onDragEnd = { state.clearSnapGuide() },
                            onDragCancel = { state.clearSnapGuide() },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                rawStart += (dragAmount / density / dpPerSec * 1000).toLong()
                                val snapMs = (SNAP_DP / dpPerSec * 1000).toLong()
                                rawStart = state.resizeClipStart(clip.id, rawStart, snapMs)
                            }
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(handleW)
                    .fillMaxHeight()
                    .background(Color(0xFF202020))
                    .border(1.5.dp, Color.White, RoundedCornerShape(2.dp))
                    .pointerInput(clip.id, dpPerSec) {
                        // আঙুলের মোট সরণ জমা রাখি; snap শুধু চূড়ান্ত অবস্থানে প্রযোজ্য।
                        var rawEnd = 0L
                        detectHorizontalDragGestures(
                            onDragStart = { rawEnd = latestClip.endMs; state.beginChange() },
                            onDragEnd = { state.clearSnapGuide() },
                            onDragCancel = { state.clearSnapGuide() },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                rawEnd += (dragAmount / density / dpPerSec * 1000).toLong()
                                val snapMs = (SNAP_DP / dpPerSec * 1000).toLong()
                                rawEnd = state.resizeClipEnd(clip.id, rawEnd, snapMs)
                            }
                        )
                    }
            )
        }
    }
}

/** ট্র্যাকের ভেতরেই (BoxScope) জোড়া ও কিনারার Transition "+" বাটন বসায় */
@Composable
private fun TransitionButtons(state: EditorState, track: TrackInfo) {
    if (track.category == TrackCategory.AUDIO || track.isLocked) return
    state.junctionsOf(track.id).forEach { (a, b) ->
        JoinButton(state, a, b)
    }
    val selected = state.selectedClip
    if (selected != null && selected.trackId == track.id && state.canHaveTransition(selected)) {
        if (state.prevAdjacent(selected) == null) EdgeTransButton(state, selected, true)
        if (state.nextAdjacent(selected) == null) EdgeTransButton(state, selected, false)
    }
}

/** ক্লিপের শুরু বা শেষে Transition control lane-এ ছোট + বাটন */
@Composable
private fun EdgeTransButton(state: EditorState, clip: Clip, atStart: Boolean) {
    val dpPerSec = state.dpPerSec
    val xDp = if (atStart) {
        (((clip.startMs / 1000f) * dpPerSec).dp - 17.dp).coerceAtLeast(0.dp)
    } else {
        ((clip.endMs / 1000f) * dpPerSec).dp + 3.dp
    }
    Box(
        modifier = Modifier
            .offset { IntOffset(xDp.roundToPx(), 0) }
            .zIndex(3f)
            .size(14.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFFE0E6EF))
            .border(1.dp, Color(0xFF657080), RoundedCornerShape(3.dp))
            .clickable { state.openEdgeTransition(clip.id, atStart) },
        contentAlignment = Alignment.Center
    ) {
        Text("+", fontSize = 12.sp, lineHeight = 12.sp, color = Color(0xFF151922), fontWeight = FontWeight.Bold)
    }
}

/** দুই লাগোয়া ক্লিপের মাঝে Transition control lane-এ ছোট + বাটন */
@Composable
private fun JoinButton(state: EditorState, a: Clip, b: Clip) {
    val dpPerSec = state.dpPerSec
    val xDp = (((a.endMs + b.startMs) / 2f / 1000f) * dpPerSec).dp - 7.dp
    val has = a.transOut != TransitionType.NONE || b.transIn != TransitionType.NONE
    Box(
        modifier = Modifier
            .offset { IntOffset(xDp.roundToPx(), 0) }
            .zIndex(3f)
            .size(14.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (has) Color(0xFF2ED573) else Color(0xFFE0E6EF))
            .border(1.dp, Color(0xFF657080), RoundedCornerShape(3.dp))
            .clickable { state.openJoinTransition(b.id) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (has) "✦" else "+",
            fontSize = 11.sp,
            lineHeight = 11.sp,
            color = Color(0xFF151922),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GapBlock(state: EditorState, gap: Gap) {
    val dpPerSec = state.dpPerSec
    val selected = state.selectedGap == gap
    val xDp = ((gap.startMs / 1000f) * dpPerSec).dp
    val widthDp = ((gap.durationMs / 1000f) * dpPerSec).dp
    Box(
        modifier = Modifier
            .offset { IntOffset(xDp.roundToPx(), 0) }
            .width(widthDp)
            .height(TrackHeight - 6.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) Color(0x66FFD32A) else Color(0x22FFFFFF))
            .border(
                1.dp,
                if (selected) Color(0xFFFFD32A) else Color(0x55FFFFFF),
                RoundedCornerShape(4.dp)
            )
            .clickable { state.selectGap(gap) },
        contentAlignment = Alignment.Center
    ) {
        if (widthDp > 30.dp) {
            Text("gap", fontSize = 9.sp, color = Color(0xFFCCCCCC), maxLines = 1)
        }
    }
}

@Composable
private fun Playhead(state: EditorState) {
    val xDp = ((state.playheadMs / 1000f) * state.dpPerSec).dp
    Box(
        modifier = Modifier
            .offset { IntOffset(xDp.roundToPx(), 0) }
            .width(2.dp)
            .fillMaxHeight()
            .background(Accent)
    )
}


@Composable
private fun PropertiesPanel(state: EditorState, modifier: Modifier) {
    val sel = state.selectedClip
    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
            .background(Color(0xFF141414), RoundedCornerShape(8.dp))
            .border(1.dp, Stroke, RoundedCornerShape(8.dp))
            .padding(6.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PROPERTIES", color = TextDim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        HorizontalDivider(color = Stroke, thickness = 0.5.dp)

        if (sel != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (sel.locked) Color(0xFFB7791F) else Color(0xFF2B2B2B))
                    .clickable { state.toggleClipLock(sel.id) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (sel.locked) "🔒 Locked" else "🔓 Lock",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(color = Stroke, thickness = 0.5.dp)

            if (sel.isVideoOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E90FF))
                        .clickable { state.showOverlayProps = false }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) { Text("🔊 Audio", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                HorizontalDivider(color = Stroke, thickness = 0.5.dp)
            }
            if (sel.textOverlay != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2B6CB0))
                        .clickable { state.textEditId = sel.id }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) { Text("✎ Edit text", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                HorizontalDivider(color = Stroke, thickness = 0.5.dp)
            }
            Text("Opacity", color = TextDim, fontSize = 8.sp)
            Text("${(sel.opacity * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF2B2B2B)).clickable {
                        state.beginChange()
                        state.updateClipOpacity(sel.id, -0.1f)
                    },
                    contentAlignment = Alignment.Center
                ) { Text("−", fontSize = 16.sp, color = Color.White) }

                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF2B2B2B)).clickable {
                        state.beginChange()
                        state.updateClipOpacity(sel.id, 0.1f)
                    },
                    contentAlignment = Alignment.Center
                ) { Text("+", fontSize = 16.sp, color = Color.White) }
            }

            HorizontalDivider(color = Stroke, thickness = 0.5.dp)

            Text("Rotate", color = TextDim, fontSize = 8.sp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFF2B2B2B)).clickable { state.rotateSelectedClip(-90f) }.padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) { Text("⟲ -90°", fontSize = 10.sp, color = Color.White) }

                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFF2B2B2B)).clickable { state.rotateSelectedClip(90f) }.padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) { Text("⟳ +90°", fontSize = 10.sp, color = Color.White) }
            }

            HorizontalDivider(color = Stroke, thickness = 0.5.dp)

            Text("Scale", color = TextDim, fontSize = 8.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF2B2B2B)).clickable {
                        state.beginChange()
                        state.updateClipTransform(sel.id, 0.9f, Offset.Zero)
                    },
                    contentAlignment = Alignment.Center
                ) { Text("−", fontSize = 16.sp, color = Color.White) }

                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF2B2B2B)).clickable {
                        state.beginChange()
                        state.updateClipTransform(sel.id, 1.1f, Offset.Zero)
                    },
                    contentAlignment = Alignment.Center
                ) { Text("+", fontSize = 16.sp, color = Color.White) }
            }

            HorizontalDivider(color = Stroke, thickness = 0.5.dp)

            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFFFF4757)).clickable { state.resetClipTransform(sel.id) }.padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) { Text("Reset", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }

        } else {
            Text("Select a clip\nto edit\nproperties", color = Color(0xFF555555), fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}