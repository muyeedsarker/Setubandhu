package com.setubandhu.app

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

// ছবি এডিট: ছবি বাছলেই অটো ক্লিয়ার (লেভেল, আলো, রং, শার্প, নয়েজ কমানো)। কোনো ইন্টারনেট বা বাইরের লাইব্রেরি লাগে না।
// "পোশাক পরিপাটি" = ছবির নিচের অংশের (পোশাকের) ভাঁজ/খসখসে ভাব নরম করা। এটা AI দিয়ে নতুন পোশাক পরানো নয়।
data class EditParams(
    val auto: Boolean = true,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val sharpness: Float = 0f,
    val fabric: Float = 0.5f,
    val rotation: Int = 0,
    val flip: Boolean = false,
    val crop: Int = 0 // 0 মূল, 1 = ১:১, 2 = পাসপোর্ট ৭:৯, 3 = ৩:৪
)

fun decodeScaled(context: Context, uri: Uri, maxSide: Int): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var s = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / s > maxSide * 2) s *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = s }
        var bmp = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return null
        val orient = try {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) { ExifInterface.ORIENTATION_NORMAL }
        val m = Matrix()
        when (orient) {
            ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
        }
        val f = maxSide.toFloat() / maxOf(bmp.width, bmp.height)
        if (f < 1f) m.postScale(f, f)
        if (!m.isIdentity) bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
        bmp
    } catch (e: Exception) { null } catch (e: OutOfMemoryError) { null }
}

private fun channel(c: Int, shift: Int) = (c shr shift) and 255
private fun pack(r: Float, g: Float, b: Float): Int =
    (0xFF shl 24) or (r.roundToInt().coerceIn(0, 255) shl 16) or (g.roundToInt().coerceIn(0, 255) shl 8) or b.roundToInt().coerceIn(0, 255)

private fun smooth(px: IntArray, w: Int, h: Int, y0: Int, y1: Int, strength: Float, sigma: Float) {
    val src = px.copyOf()
    val inv = -1f / (2f * sigma * sigma)
    for (y in 0 until h) {
        val rw = if (y1 <= y0) 1f else ((y - y0).toFloat() / (y1 - y0)).coerceIn(0f, 1f)
        if (rw <= 0f) continue
        val k = strength * rw
        for (x in 0 until w) {
            val c = src[y * w + x]
            val r0 = channel(c, 16); val g0 = channel(c, 8); val b0 = channel(c, 0)
            var sr = 0f; var sg = 0f; var sb = 0f; var sw = 0f
            var dy = -4
            while (dy <= 4) {
                val yy = (y + dy).coerceIn(0, h - 1)
                var dx = -4
                while (dx <= 4) {
                    val q = src[yy * w + (x + dx).coerceIn(0, w - 1)]
                    val r = channel(q, 16); val g = channel(q, 8); val b = channel(q, 0)
                    val d = ((r - r0) * (r - r0) + (g - g0) * (g - g0) + (b - b0) * (b - b0)).toFloat()
                    val wt = exp(d * inv)
                    sr += r * wt; sg += g * wt; sb += b * wt; sw += wt
                    dx += 2
                }
                dy += 2
            }
            px[y * w + x] = pack(r0 + (sr / sw - r0) * k, g0 + (sg / sw - g0) * k, b0 + (sb / sw - b0) * k)
        }
    }
}

private fun unsharp(px: IntArray, w: Int, h: Int, amt: Float) {
    val src = px.copyOf()
    for (y in 0 until h) for (x in 0 until w) {
        var sr = 0; var sg = 0; var sb = 0
        for (dy in -1..1) for (dx in -1..1) {
            val q = src[(y + dy).coerceIn(0, h - 1) * w + (x + dx).coerceIn(0, w - 1)]
            sr += channel(q, 16); sg += channel(q, 8); sb += channel(q, 0)
        }
        val c = src[y * w + x]
        val r = channel(c, 16); val g = channel(c, 8); val b = channel(c, 0)
        px[y * w + x] = pack(r + (r - sr / 9f) * amt, g + (g - sg / 9f) * amt, b + (b - sb / 9f) * amt)
    }
}

fun processImage(src: Bitmap, p: EditParams): Bitmap {
    val w = src.width; val h = src.height
    val px = IntArray(w * h)
    src.getPixels(px, 0, w, 0, 0, w, h)
    var lo = 0; var hi = 255; var gamma = 1f
    if (p.auto) {
        val hist = IntArray(256); var sum = 0L
        for (c in px) {
            val l = (channel(c, 16) * 299 + channel(c, 8) * 587 + channel(c, 0) * 114) / 1000
            hist[l]++; sum += l
        }
        val n = px.size; val cut = (n * 0.005).toInt(); var acc = 0
        for (i in 0..255) { acc += hist[i]; if (acc > cut) { lo = i; break } }
        acc = 0
        for (i in 255 downTo 0) { acc += hist[i]; if (acc > cut) { hi = i; break } }
        if (hi - lo < 60) { lo = 0; hi = 255 }
        val m = (((sum.toFloat() / n) - lo) / (hi - lo)).coerceIn(0.08f, 0.92f)
        gamma = (ln(0.46f) / ln(m)).coerceIn(0.65f, 1.4f)
    }
    val lut = IntArray(256) { c ->
        var x = ((c - lo) / (hi - lo).toFloat()).coerceIn(0f, 1f).pow(gamma)
        x = (x - 0.5f) * p.contrast + 0.5f + p.brightness / 255f
        (x * 255f).roundToInt().coerceIn(0, 255)
    }
    val sat = p.saturation * (if (p.auto) 1.08f else 1f)
    for (i in px.indices) {
        val c = px[i]
        val r = lut[channel(c, 16)]; val g = lut[channel(c, 8)]; val b = lut[channel(c, 0)]
        val l = 0.299f * r + 0.587f * g + 0.114f * b
        px[i] = pack(l + (r - l) * sat, l + (g - l) * sat, l + (b - l) * sat)
    }
    if (p.auto) smooth(px, w, h, 0, 0, 0.35f, 22f)
    if (p.fabric > 0f) smooth(px, w, h, (h * 0.38f).toInt(), (h * 0.55f).toInt(), p.fabric, 18f + 30f * p.fabric)
    val amt = p.sharpness + if (p.auto) 0.5f else 0f
    if (amt > 0f) unsharp(px, w, h, amt)
    var out = Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888)
    if (p.crop > 0) {
        val ratio = when (p.crop) { 1 -> 1f; 2 -> 7f / 9f; else -> 3f / 4f }
        var cw = w; var ch = (w / ratio).toInt()
        if (ch > h) { ch = h; cw = (h * ratio).toInt() }
        out = Bitmap.createBitmap(out, (w - cw) / 2, ((h - ch) * 0.3f).toInt(), cw, ch)
    }
    if (p.rotation != 0 || p.flip) {
        val m = Matrix()
        if (p.flip) m.postScale(-1f, 1f)
        m.postRotate(p.rotation.toFloat())
        out = Bitmap.createBitmap(out, 0, 0, out.width, out.height, m, true)
    }
    return out
}

fun saveToGallery(context: Context, bmp: Bitmap): Uri? {
    val cv = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "Setubandhu_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= 29) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Setubandhu")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv) ?: return null
    return try {
        context.contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        if (Build.VERSION.SDK_INT >= 29) {
            cv.clear(); cv.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, cv, null, null)
        }
        uri
    } catch (e: Exception) {
        context.contentResolver.delete(uri, null, null); null
    }
}

@Composable
private fun EditSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, accent: Color, onChange: (Float) -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Slider(value = value, onValueChange = onChange, valueRange = range, colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent))
    }
}

@Composable
private fun ChipBtn(text: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (selected) accent else Color(0xFFE7EEF8), contentColor = if (selected) Color.White else TextPrimary),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) { ETxt(text, fontSize = 12.sp) }
}

@Composable
fun ImageEditDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var base by remember { mutableStateOf<Bitmap?>(null) }
    var p by remember { mutableStateOf(EditParams()) }
    var result by remember { mutableStateOf<Bitmap?>(null) }
    var busy by remember { mutableStateOf(false) }
    var showOrig by remember { mutableStateOf(false) }
    var aiBusy by remember { mutableStateOf(false) }
    var aiPrompt by remember { mutableStateOf("") }
    var prevBase by remember { mutableStateOf<Bitmap?>(null) }
    var showKey by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            val b = withContext(Dispatchers.IO) { decodeScaled(context, uri, 1600) }
            if (b == null) { busy = false; Toast.makeText(context, l3("ছবি খোলা যায়নি", "Could not open photo", "تعذر فتح الصورة"), Toast.LENGTH_SHORT).show() }
            else { base = b; p = EditParams() }
        }
    }
    LaunchedEffect(base, p) {
        val b = base ?: return@LaunchedEffect
        busy = true
        delay(250)
        result = withContext(Dispatchers.Default) { processImage(b, p) }
        busy = false
    }
    fun save(addAlbum: Boolean) {
        val r = result ?: return
        scope.launch {
            val uri = withContext(Dispatchers.IO) { saveToGallery(context, r) }
            if (uri == null) { Toast.makeText(context, l3("সেভ করা যায়নি", "Could not save", "تعذر الحفظ"), Toast.LENGTH_SHORT).show(); return@launch }
            if (addAlbum) withContext(Dispatchers.IO) { addToAlbum(context, uri) }
            Toast.makeText(context, l3("সেভ হয়েছে ✅", "Saved ✅", "تم الحفظ ✅"), Toast.LENGTH_SHORT).show()
        }
    }

    fun runAi(prompt: String) {
        val b = base ?: return
        if (prompt.isBlank() || aiBusy) return
        val key = loadAiKey(context)
        if (key.isBlank()) { showKey = true; return }
        scope.launch {
            aiBusy = true
            val (bmp, err) = withContext(Dispatchers.IO) { aiEdit(b, prompt, key, loadAiModel(context)) }
            aiBusy = false
            if (bmp != null) { prevBase = b; base = scaleDown(bmp, 1600); p = EditParams(auto = false, fabric = 0f) }
            else Toast.makeText(context, "AI: " + (err ?: ""), Toast.LENGTH_LONG).show()
        }
    }
    if (showKey) AiKeyDialog(accent) { showKey = false }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(accent).padding(top = 24.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text(tr("← ফিরে যান"), color = Color.White, fontWeight = FontWeight.Bold) }
                    Text(l3("✨ ছবি এডিট", "✨ Photo Editor", "✨ تحرير الصور"), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    TextButton(onClick = { picker.launch("image/*") }) { ETxt(l3("📂 ছবি", "📂 Photo", "📂 صورة"), color = Color.White, fontWeight = FontWeight.Bold) }
                }
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF111111)).pointerInput(Unit) {
                        detectTapGestures(onPress = { showOrig = true; tryAwaitRelease(); showOrig = false })
                    },
                    contentAlignment = Alignment.Center
                ) {
                    val shown = if (showOrig) base else result
                    if (shown == null) {
                        Button(onClick = { picker.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = accent)) {
                            ETxt(l3("📂 ছবি বেছে নিন", "📂 Choose a photo", "📂 اختر صورة"), fontSize = 16.sp)
                        }
                    } else {
                        Image(bitmap = shown.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        Text(
                            if (showOrig) l3("আগের ছবি", "Original", "الأصلية") else l3("এডিট করা (চেপে ধরলে আগের ছবি)", "Edited (hold to see original)", "معدلة (اضغط مطولاً للأصلية)"),
                            color = Color.White, fontSize = 11.sp, modifier = Modifier.align(Alignment.TopCenter).padding(6.dp).background(Color(0x88000000), RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    if (busy || aiBusy) CircularProgressIndicator(color = Color.White)
                }
                if (base != null) {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(l3("🤖 AI এডিট (ইন্টারনেট লাগে)", "🤖 AI edit (needs internet)", "🤖 تعديل بالذكاء الاصطناعي (يحتاج إنترنت)"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accent)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            aiPresets.forEach { ps -> ChipBtn(ps.label(), false, accent) { runAi(ps.prompt) } }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = aiPrompt, onValueChange = { aiPrompt = it }, singleLine = true, modifier = Modifier.weight(1f),
                                placeholder = { Text(l3("নিজের নির্দেশ লিখুন…", "Type your own instruction…", "اكتب تعليماتك…"), fontSize = 12.sp) }
                            )
                            Button(onClick = { runAi(aiPrompt + " " + "Keep the person's face and identity exactly the same.") }, colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text("🤖", fontSize = 16.sp) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (prevBase != null) ChipBtn(l3("↩ AI আগের", "↩ Undo AI", "↩ تراجع"), false, accent) { base = prevBase; prevBase = null; p = EditParams() }
                            ChipBtn(l3("⚙️ API কী", "⚙️ API key", "⚙️ مفتاح API"), false, accent) { showKey = true }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            ChipBtn(l3("✨ অটো ক্লিয়ার", "✨ Auto clear", "✨ تحسين تلقائي"), p.auto, accent) { p = p.copy(auto = !p.auto) }
                            ChipBtn("↻", false, accent) { p = p.copy(rotation = (p.rotation + 90) % 360) }
                            ChipBtn("⇋", p.flip, accent) { p = p.copy(flip = !p.flip) }
                            ChipBtn(l3("রিসেট", "Reset", "إعادة"), false, accent) { p = EditParams() }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(l3("ক্রপ", "Crop", "قص"), fontSize = 12.sp, color = TextMuted)
                            ChipBtn(l3("মূল", "Original", "أصلي"), p.crop == 0, accent) { p = p.copy(crop = 0) }
                            ChipBtn("1:1", p.crop == 1, accent) { p = p.copy(crop = 1) }
                            ChipBtn(l3("পাসপোর্ট", "Passport", "جواز"), p.crop == 2, accent) { p = p.copy(crop = 2) }
                            ChipBtn("3:4", p.crop == 3, accent) { p = p.copy(crop = 3) }
                        }
                        EditSlider(l3("👔 পোশাক পরিপাটি (ভাঁজ নরম)", "👔 Tidy clothes (soften wrinkles)", "👔 ترتيب الملابس (تنعيم التجاعيد)"), p.fabric, 0f..1f, accent) { p = p.copy(fabric = it) }
                        EditSlider(l3("আলো", "Brightness", "السطوع"), p.brightness, -60f..60f, accent) { p = p.copy(brightness = it) }
                        EditSlider(l3("কনট্রাস্ট", "Contrast", "التباين"), p.contrast, 0.7f..1.4f, accent) { p = p.copy(contrast = it) }
                        EditSlider(l3("রং", "Colour", "الألوان"), p.saturation, 0f..2f, accent) { p = p.copy(saturation = it) }
                        EditSlider(l3("শার্পনেস", "Sharpness", "الحدة"), p.sharpness, 0f..2f, accent) { p = p.copy(sharpness = it) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { save(false) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text(l3("💾 গ্যালারিতে সেভ", "💾 Save to gallery", "💾 حفظ في المعرض"), fontSize = 13.sp) }
                            OutlinedButton(onClick = { save(true) }, modifier = Modifier.weight(1f)) { Text(l3("🖼️ অ্যালবামেও", "🖼️ + Album", "🖼️ + الألبوم"), fontSize = 13.sp) }
                        }
                    }
                }
            }
        }
    }
}

