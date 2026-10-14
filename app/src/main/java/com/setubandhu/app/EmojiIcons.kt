package com.setubandhu.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

// ---------- ইমোজির বদলে 3D ছবি ----------
// লেখার ভেতরের ইমোজি (যেমন "📞 কল") এই তালিকা অনুযায়ী ছবি হয়ে দেখা যায়।
// নতুন ছবি যোগ করতে res/drawable-nodpi/ এ png রেখে নিচে একটি লাইন যোগ করুন।
// ছবি না পেলে সাধারণ ইমোজিই দেখাবে।

// চাবিতে VS16 (FE0F) চিহ্ন বাদ দেওয়া আছে।
val emojiIconNames: Map<String, String> = mapOf(
    "📞" to "ic_act_call",
    "➕" to "ic_act_add_contact",
    "🔊" to "ic_act_sound_on",
    "⏹" to "ic_act_sound_off",
    "🟦" to "ic_act_imo",
    "🗺" to "ic_act_map",
    "🎯" to "ic_act_target",
    "🕒" to "ic_act_clock",
    "🎉" to "ic_act_birthday",
    "🗑" to "ic_act_delete",
    "📂" to "ic_act_folder",
    "⚠" to "ic_act_warning",
    "🤵" to "ic_act_formal",
    "⬜" to "ic_act_whitebg",
    "🛂" to "ic_act_passport"
)

// ক্যাটাগরির নামের পাশের আইকন
val categoryIconNames: Map<String, String> = mapOf(
    "উপদেশ" to "ic_cat_advice",
    "ড্রাইভার ও নামাজ" to "ic_cat_driver",
    "নেটওয়ার্ক ও সংযোগ" to "ic_cat_network",
    "ডিসপ্লে ও সাউন্ড" to "ic_cat_display",
    "অ্যাপস ও স্টোরেজ" to "ic_cat_apps",
    "সিস্টেম ও সাধারণ" to "ic_cat_system",
    "সিকিউরিটি ও প্রাইভেসি" to "ic_cat_security",
    "নোটিফিকেশন ও অ্যালার্ট" to "ic_cat_notification",
    "পাওয়ার ও রিসেট" to "ic_cat_power",
    "প্রিয় সেটিংস" to "ic_cat_fav"
)

@Composable
fun ETxt(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    val context = LocalContext.current
    val clean = text.replace("\uFE0F", "")
    val built = remember(clean) {
        val inline = mutableMapOf<String, InlineTextContent>()
        val annotated = buildAnnotatedString {
            var i = 0
            while (i < clean.length) {
                var matched: String? = null
                for (k in emojiIconNames.keys) {
                    if (clean.startsWith(k, i)) { matched = k; break }
                }
                val resId = if (matched == null) 0
                else context.resources.getIdentifier(emojiIconNames[matched], "drawable", context.packageName)
                if (matched != null && resId != 0) {
                    appendInlineContent(matched, "•")
                    inline[matched] = InlineTextContent(
                        Placeholder(1.35.em, 1.35.em, PlaceholderVerticalAlign.TextCenter)
                    ) {
                        Image(painter = painterResource(id = resId), contentDescription = null, modifier = Modifier.fillMaxSize())
                    }
                    i += matched.length
                } else {
                    append(clean[i])
                    i++
                }
            }
        }
        annotated to inline
    }
    Text(
        text = built.first,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        inlineContent = built.second
    )
}

// ক্যাটাগরির শিরোনামের পাশে ছবি (না থাকলে আগের রঙিন গোল বিন্দু)
@Composable
fun CategoryBadge(title: String, accent: Color) {
    val context = LocalContext.current
    val resId = remember(title) {
        val n = categoryIconNames[title]
        if (n == null) 0 else context.resources.getIdentifier(n, "drawable", context.packageName)
    }
    if (resId != 0) {
        Image(painter = painterResource(id = resId), contentDescription = null, modifier = Modifier.size(24.dp))
    } else {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(9.dp).clip(CircleShape).background(accent)
        )
    }
}
