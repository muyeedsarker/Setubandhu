package com.setubandhu.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

// AI ছবি এডিট (Google Gemini ইমেজ মডেল)। ইন্টারনেট ও নিজের API কী লাগে। কী শুধু এই ফোনেই সেভ থাকে।
// ছবি এডিটের সময় ছবিটা Google-এর সার্ভারে যায়।
private const val AI_PREFS = "setubandhu_ai"
const val DEFAULT_AI_MODEL = "gemini-2.5-flash-image"

fun loadAiKey(c: Context): String = c.getSharedPreferences(AI_PREFS, Context.MODE_PRIVATE).getString("key", "") ?: ""
fun loadAiModel(c: Context): String =
    (c.getSharedPreferences(AI_PREFS, Context.MODE_PRIVATE).getString("model", DEFAULT_AI_MODEL) ?: DEFAULT_AI_MODEL).ifBlank { DEFAULT_AI_MODEL }
fun saveAiSettings(c: Context, key: String, model: String) {
    c.getSharedPreferences(AI_PREFS, Context.MODE_PRIVATE).edit().putString("key", key.trim()).putString("model", model.trim()).apply()
}

private const val KEEP = " Keep the person's face, identity, skin tone, hair, expression, pose and body proportions exactly the same. Do not add or remove people. The result must look like a natural, realistic photograph."

data class AiPreset(val bn: String, val en: String, val ar: String, val prompt: String) {
    fun label() = l3(bn, en, ar)
}

val aiPresets = listOf(
    AiPreset("👔 পোশাক পরিপাটি", "👔 Tidy clothes", "👔 ترتيب الملابس",
        "Make the person's clothes neat and tidy: remove wrinkles and creases, straighten the collar, smooth the fabric, fix the fit and remove stains. Keep the same clothing style and colours.$KEEP"),
    AiPreset("🤵 ফরমাল পোশাক", "🤵 Formal outfit", "🤵 ملابس رسمية",
        "Change the person's clothing to a clean, well-fitted, neatly ironed formal white shirt with a dark suit jacket.$KEEP"),
    AiPreset("⬜ সাদা ব্যাকগ্রাউন্ড", "⬜ White background", "⬜ خلفية بيضاء",
        "Replace the background with a plain, clean white studio background suitable for a passport photo. Keep the person's edges and hair clean.$KEEP"),
    AiPreset("✨ HD ক্লিয়ার", "✨ HD clear", "✨ وضوح عالٍ",
        "Enhance this photo: make it sharp, clear and high quality, remove noise and blur, and balance lighting and colours naturally.$KEEP"),
    AiPreset("🛂 পাসপোর্ট রেডি", "🛂 Passport ready", "🛂 جاهز للجواز",
        "Turn this into a clean passport-style portrait: neat formal clothes without wrinkles, plain white background, even soft lighting, sharp and clear, head and shoulders centred, neutral look.$KEEP")
)

fun scaleDown(b: Bitmap, maxSide: Int): Bitmap {
    val f = maxSide.toFloat() / maxOf(b.width, b.height)
    return if (f >= 1f) b else Bitmap.createScaledBitmap(b, (b.width * f).toInt(), (b.height * f).toInt(), true)
}

// সফল হলে (ছবি, null), ব্যর্থ হলে (null, কারণ)
fun aiEdit(src: Bitmap, prompt: String, key: String, model: String): Pair<Bitmap?, String?> {
    return try {
        val bos = ByteArrayOutputStream()
        scaleDown(src, 1024).compress(Bitmap.CompressFormat.JPEG, 90, bos)
        val b64 = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
        val parts = JSONArray()
            .put(JSONObject().put("text", prompt))
            .put(JSONObject().put("inline_data", JSONObject().put("mime_type", "image/jpeg").put("data", b64)))
        val body = JSONObject().put("contents", JSONArray().put(JSONObject().put("parts", parts)))
        val conn = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("x-goog-api-key", key)
        conn.connectTimeout = 20000
        conn.readTimeout = 120000
        conn.doOutput = true
        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        val code = conn.responseCode
        val text = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) {
            val msg = try { JSONObject(text).getJSONObject("error").getString("message") } catch (e: Exception) { "HTTP $code" }
            return Pair(null, msg)
        }
        val ps = JSONObject(text).optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
        var note: String? = null
        if (ps != null) for (i in 0 until ps.length()) {
            val p = ps.getJSONObject(i)
            val d = p.optJSONObject("inlineData") ?: p.optJSONObject("inline_data")
            if (d != null) {
                val bytes = Base64.decode(d.getString("data"), Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) return Pair(bmp, null)
            } else if (p.has("text")) note = p.getString("text")
        }
        Pair(null, note ?: l3("AI কোনো ছবি ফেরত দেয়নি", "AI returned no image", "لم يرجع الذكاء الاصطناعي صورة"))
    } catch (e: java.io.IOException) {
        Pair(null, l3("ইন্টারনেট সংযোগ নেই বা ধীর", "No or slow internet connection", "لا يوجد اتصال بالإنترنت"))
    } catch (e: Exception) {
        Pair(null, e.message ?: "error")
    }
}

@Composable
fun AiKeyDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var key by remember { mutableStateOf(loadAiKey(context)) }
    var model by remember { mutableStateOf(loadAiModel(context)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { saveAiSettings(context, key, model); onDismiss() }) {
                Text(l3("সেভ", "Save", "حفظ"), color = accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(l3("বাতিল", "Cancel", "إلغاء")) } },
        title = { Text("🤖 AI " + l3("সেটিংস", "Settings", "الإعدادات"), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(l3(
                    "Google AI Studio (aistudio.google.com/apikey) থেকে নিজের API কী নিয়ে এখানে বসান। কী শুধু এই ফোনে সেভ থাকে। এডিটের সময় ছবিটা Google-এর সার্ভারে যায়। ব্যবহার অনুযায়ী খরচ লাগতে পারে।",
                    "Get your own API key from Google AI Studio (aistudio.google.com/apikey) and paste it here. It is stored only on this phone. The photo is sent to Google's servers when editing. Usage charges may apply.",
                    "احصل على مفتاح API من Google AI Studio والصقه هنا. يُحفظ على هذا الهاتف فقط. تُرسل الصورة إلى خوادم Google أثناء التعديل. قد تُطبق رسوم الاستخدام."
                ), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = key, onValueChange = { key = it }, label = { Text("API key") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}
