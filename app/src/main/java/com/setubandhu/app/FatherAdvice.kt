package com.setubandhu.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ---------- বাবার উপদেশ ----------
// লেখা বদলাতে চাইলে নিচের adviceSections তালিকা ও ADVICE_SALAM / ADVICE_CLOSING সম্পাদনা করুন।

private val AdviceDeep = Color(0xFF0B3D2E)
private val AdviceGreen = Color(0xFF14664B)
private val AdviceGold = Color(0xFFC9A227)
private val AdviceCream = Color(0xFFFFF8E7)
private val AdviceInk = Color(0xFF2B2B2B)

private class AdviceSection(val title: String, val emoji: String, val color: Color, val items: List<String>)

private const val ADVICE_TITLE = "বাবার উপদেশ"
private const val ADVICE_SUBTITLE = "প্রিয় সন্তানের জন্য বাবার রেখে যাওয়া আমানত"
private const val ADVICE_SALAM = "প্রিয় বাবা আমার,\n\nআসসালামু আলাইকুম। আমি যদি বেঁচে না থাকি, এই কথাগুলো আমার পক্ষ থেকে তোমার কাছে আমানত। আমি তোমাদের সবাইকে সুখী করতে পারিনি। আমার সেই অপূর্ণতা তোমার জীবনের শিক্ষা হোক, তুমি যেন আমার চেয়ে ভালো মানুষ হও।"
private const val ADVICE_CLOSING_DUA = "আল্লাহ তোমাকে হেফাজত করুন, রিজিকে বরকত দিন, দুনিয়া ও আখিরাতে কামিয়াব করুন। আমীন।"
private const val ADVICE_SIGN = "ইতি, তোমার বাবা"

private val adviceSections: List<AdviceSection> = listOf(
    AdviceSection("দ্বীন ও ঈমান", "🕌", Color(0xFF14664B), listOf(
        "আল্লাহকে সবকিছুর আগে রাখবে। তিনিই রিজিকদাতা, মালিক ও রক্ষাকারী।",
        "পাঁচ ওয়াক্ত নামাজ সময়মতো পড়বে, কাজের চাপেও ছাড়বে না। যে নামাজ ধরে রাখে, আল্লাহ তার জীবন ধরে রাখেন।",
        "প্রতিদিন অন্তত কিছুটা কুরআন তিলাওয়াত করবে, অর্থ বোঝার চেষ্টা করবে। সকাল-সন্ধ্যার জিকির ও সফরের দোয়া পড়বে।",
        "রমজানের রোজা রাখবে, সামর্থ্য থাকলে যাকাত দেবে, সুযোগ হলে হজ-ওমরাহ করবে। সৌদিতে থাকা তোমার জন্য বড় সুযোগ।",
        "ভুল হলে তওবা করবে। গুনাহ ছোট ভেবে অবহেলা করবে না, আবার নিরাশও হবে না।"
    )),
    AdviceSection("হালাল উপার্জন ও আমানত", "🚗", Color(0xFF1565C0), listOf(
        "শুধু হালাল রিজিক খাবে ও খাওয়াবে। হারামে বরকত নেই।",
        "মালিকের গাড়ি, টাকা ও বিশ্বাস তোমার কাছে আমানত। কোনো হিসাবে ফাঁকি দেবে না। সততাই প্রবাসে তোমার সবচেয়ে বড় পুঁজি।",
        "কাজে আন্তরিক থাকবে। ভালো কর্মচারীকে মালিক ছাড়তে চায় না।",
        "নিজের দক্ষতা বাড়াবে: ভালো ড্রাইভিং, আরবি ভাষা, ইকামা ও লাইসেন্সের কাগজপত্র ঠিক রাখা। কাজ যত শিখবে, আয় তত বাড়বে।"
    )),
    AdviceSection("টাকার সঠিক ব্যবহার", "💰", Color(0xFFB8860B), listOf(
        "টাকার মূল্য বোঝো। এক টাকা কামাই করতে কত কষ্ট, তা মনে রাখবে। টাকা ধরে রাখার অভ্যাস গড়বে, সেটা যেন তোমার জীবনের শৃঙ্খলা হয়।",
        "আয় হাতে আসামাত্র আগে সঞ্চয় আলাদা করবে। আয়ের অন্তত এক-চতুর্থাংশ বা যতটা পারো, প্রতি মাসে জমাবে। বাকিটা দিয়ে খরচ চালাবে।",
        "কুরআন বলেছে, হাত গলায় বেঁধে রেখো না (কৃপণ হোয়ো না), আবার একেবারে খুলেও দিয়ো না (অপচয় করো না)। মধ্যপন্থা মেনো।",
        "অপ্রয়োজনীয় শখ, দামি মোবাইল, বাহারি পোশাক ও বন্ধুদের সামনে দেখানোর খরচ থেকে বাঁচবে।",
        "ঋণ থেকে দূরে থাকবে। ঋণ নিলে দিনের চিন্তা, রাতের অশান্তি। নিতেই হলে লিখে রাখবে ও আগে শোধ করবে।",
        "জরুরি অবস্থার জন্য আলাদা একটা তহবিল রাখবে, অন্তত ৪-৬ মাসের খরচ। অসুখ, চাকরি হারানো বা দুর্ঘটনা বলে কয়ে আসে না।",
        "দেশে টাকা পাঠাবে বৈধ পথে (ব্যাংক বা অনুমোদিত মাধ্যমে)। সঞ্চয়ের একটা অংশ জমি, ছোট ব্যবসা বা এমন কিছুতে লাগাবে যা থেকে ভবিষ্যতে আয় আসে। কারও লোভনীয় প্রস্তাবে না বুঝে টাকা ঢালবে না। সুদ ও জুয়া থেকে সম্পূর্ণ দূরে থাকবে।",
        "কাউকে অতিরিক্ত বিশ্বাস করে টাকা দেবে না। বন্ধুকে ধার দিলে ধরে নিও টাকাটা ফেরত নাও আসতে পারে, তাই যা না পেলেও চলে তাই দেবে।",
        "টাকা তোমার সেবক হবে, তুমি টাকার গোলাম হবে না। টাকাকে মনিব বানালে মনের শান্তি হারাবে।"
    )),
    AdviceSection("মা, ভাই ও পরিবার", "👨‍👩‍👦", Color(0xFFAD1457), listOf(
        "তোমার মা তোমার জান্নাতের পথ। তাঁর খেদমত করবে, নরম গলায় কথা বলবে, কখনো বিরক্তি দেখাবে না। তাঁর দোয়া নেবে।",
        "ছোট ভাইয়ের প্রতি বাবার মতো স্নেহ রাখবে। তাকে পড়াশোনা, ভালো চরিত্র ও দ্বীন শেখাবে। তার হাতে একটা দক্ষতা তুলে দিও, যেন সে নিজের পায়ে দাঁড়াতে পারে।",
        "হাদিসে আছে, তোমাদের মধ্যে সেই সবচেয়ে ভালো, যে নিজের পরিবারের কাছে ভালো। পরিবারকে টাকার পাশাপাশি সময় ও ভালোবাসাও দেবে। ফোনে নিয়মিত কথা বলবে।",
        "আমি আর তোমার মা আলাদা থাকি, কিন্তু আমরা দুজনই তোমার মা-বাবা। কারও প্রতি বিদ্বেষ রাখবে না। একজনের কথা অন্যজনের কাছে মন্দভাবে বলবে না। আমার ভুল থাকলে ক্ষমা করে দিও।"
    )),
    AdviceSection("চরিত্র ও মানবিকতা", "🌿", Color(0xFF2E7D32), listOf(
        "কোমল হৃদয়ের মানুষ হবে। গরিব, এতিম ও অসহায়ের পাশে দাঁড়াবে। তোমার আয়ের একটা অংশ সদকা করবে। সদকা সম্পদ কমায় না।",
        "সত্য বলবে, ওয়াদা রাখবে, কাউকে ঠকাবে না, গীবত করবে না।",
        "রাগ নিয়ন্ত্রণ করবে। রাগের মাথায় কথা বা সিদ্ধান্ত নিলে পরে আফসোস হয়। রাগ উঠলে চুপ থাকো, ওযু করো।",
        "নম্র ভাষায় মানুষের সাথে কথা বলবে। সৌদির মানুষ, সহকর্মী ও প্রবাসী ভাইদের সাথে ভালো আচরণ করবে। দেশ ও প্রবাসের আইন মেনে চলবে।",
        "নেশা, জুয়া, অশ্লীলতা ও খারাপ সঙ্গ থেকে দূরে থাকবে। প্রবাসের একাকিত্বে মন খারাপ হলে নামাজ, কুরআন ও পরিবারের সাথে কথা বলে মন শক্ত করবে।",
        "সুখে-দুঃখে আল্লাহর শোকর করবে, কষ্টে সবর করবে।"
    )),
    AdviceSection("সমাজে প্রতিষ্ঠা ও ভবিষ্যৎ", "🏡", Color(0xFF6A1B9A), listOf(
        "ধাপে ধাপে লক্ষ্য ঠিক করবে: ঋণমুক্ত থাকা, জমি বা নিজের ঘর, দেশে ছোট ব্যবসা বা নিজের গাড়ি। লক্ষ্য লিখে রাখবে।",
        "উপযুক্ত সময়ে দ্বীনদার, ভালো চরিত্রের পাত্রী দেখে বিয়ে করবে। বিয়ের আগে ইস্তিখারা করবে, বড়দের পরামর্শ নেবে। স্ত্রীর সাথে নম্র থাকবে। আমাদের মতো সংসার ভাঙতে দিও না।",
        "সন্তান হলে তাদের দ্বীন শেখাবে, সময় দেবে। যা আমি তোমাকে বলতে পারিনি, তুমি তাদের বলবে।",
        "সমাজে সম্মান আসে চরিত্র ও সততা থেকে, টাকার পরিমাণ থেকে নয়। মানুষ তোমাকে বলুক, \"ও ভালো মানুষ, দায়িত্বশীল ও দ্বীনদার।\""
    )),
    AdviceSection("শেষ কথা", "🤲", Color(0xFF00695C), listOf(
        "আমার জন্য দোয়া করবে। কুরআন পড়ে সওয়াব পাঠাবে, আমার পক্ষ থেকে সদকা দেবে। সৎ সন্তানের দোয়া মৃত্যুর পরও বাবার কাজে লাগে।",
        "আমি তোমাকে ভালোবাসি। আমার কোনো ভুল বা কমতি থাকলে আল্লাহর ওয়াস্তে মাফ করে দিও।"
    ))
)

private fun adviceFullText(): String {
    val sb = StringBuilder()
    sb.append(ADVICE_TITLE).append("\n\n").append(ADVICE_SALAM).append("\n\n")
    var n = 0
    for (s in adviceSections) {
        sb.append(s.emoji).append(" ").append(s.title).append("\n")
        for (item in s.items) {
            n++
            sb.append(bn(n)).append(". ").append(item).append("\n")
        }
        sb.append("\n")
    }
    sb.append(ADVICE_CLOSING_DUA).append("\n\n").append(ADVICE_SIGN)
    return sb.toString()
}

@Composable
fun FatherAdviceDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = AdviceCream) {
            Column(modifier = Modifier.fillMaxSize()) {
                // উপরের বার
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdviceDeep)
                        .padding(top = 24.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(tr("← ফিরে যান"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText(ADVICE_TITLE, adviceFullText()))
                        Toast.makeText(context, "কপি হয়েছে ✅", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("📋 কপি", color = AdviceGold, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, adviceFullText())
                        }
                        val chooser = Intent.createChooser(send, null)
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                    }) {
                        Text("📤 শেয়ার", color = AdviceGold, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // হিরো অংশ
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(listOf(AdviceDeep, AdviceGreen)),
                                RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                color = AdviceGold,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("✦  ✦  ✦", color = AdviceGold.copy(alpha = 0.8f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("📜", fontSize = 40.sp)
                            Text(
                                ADVICE_TITLE,
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                ADVICE_SUBTITLE,
                                color = Color(0xFFE8F5E9),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // সম্বোধন
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, AdviceGold),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                ADVICE_SALAM,
                                modifier = Modifier.padding(18.dp),
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                color = AdviceInk
                            )
                        }

                        var counter = 0
                        for (section in adviceSections) {
                            Spacer(modifier = Modifier.height(6.dp))
                            // সেকশনের শিরোনাম
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.horizontalGradient(listOf(section.color, section.color.copy(alpha = 0.75f))))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(section.emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    section.title,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            for (item in section.items) {
                                counter++
                                val number = counter
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, section.color.copy(alpha = 0.35f)),
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Brush.radialGradient(listOf(Color(0xFFFFE082), AdviceGold))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                bn(number),
                                                color = AdviceDeep,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            item,
                                            modifier = Modifier.weight(1f),
                                            fontSize = 15.sp,
                                            lineHeight = 24.sp,
                                            color = AdviceInk
                                        )
                                    }
                                }
                            }
                        }

                        // সমাপ্তি
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Brush.verticalGradient(listOf(AdviceGreen, AdviceDeep)))
                                .padding(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🤲", fontSize = 34.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    ADVICE_CLOSING_DUA,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    lineHeight = 26.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("✦  ✦  ✦", color = AdviceGold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    ADVICE_SIGN,
                                    color = AdviceGold,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
