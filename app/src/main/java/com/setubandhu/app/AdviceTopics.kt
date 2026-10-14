package com.setubandhu.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ---------- বিষয়ভিত্তিক উপদেশ (বাটন + বিস্তারিত পাতা) ----------
// লেখা বদলাতে চাইলে নিচের adviceTopics তালিকা সম্পাদনা করুন।

private val TopicDeep = Color(0xFF0B3D2E)
private val TopicGreen = Color(0xFF14664B)
private val TopicGold = Color(0xFFC9A227)
private val TopicCream = Color(0xFFFFF8E7)
private val TopicInk = Color(0xFF2B2B2B)

class AdviceBlock(val heading: String, val items: List<String>)

class AdviceTopic(
    val title: String,
    val emoji: String,
    val color: Color,
    val intro: String,
    val blocks: List<AdviceBlock>,
    val duaArabic: String? = null,
    val duaMeaning: String? = null
)

private const val TOPIC_SIGN = "ইতি, তোমার বাবা"

val adviceTopics: List<AdviceTopic> = listOf(
    AdviceTopic(
        title = "মালিকের সাথে আচরণ",
        emoji = "🤝",
        color = Color(0xFF1565C0),
        intro = "মালিক তোমার রিজিকের একটি মাধ্যম। তাঁর সাথে তোমার আচরণই ঠিক করে দেবে তুমি কতদিন কাজে টিকবে এবং কতটা সম্মান পাবে।",
        blocks = listOf(
            AdviceBlock("কথা ও ব্যবহার", listOf(
                "সালাম দিয়ে কথা শুরু করবে। নম্র ও স্পষ্ট ভাষায় কথা বলবে। মালিকের সামনে গলা চড়াবে না, তর্ক করবে না। মতভেদ হলে শান্ত সময়ে ভদ্রভাবে নিজের কথা জানাবে।",
                "আদেশ মন দিয়ে শুনে বুঝে নেবে। না বুঝলে ভুল করার চেয়ে বিনয়ের সাথে আবার জিজ্ঞেস করে নেওয়া ভালো।",
                "সহজ আরবি শব্দ শিখে ব্যবহার করবে, যেমন ‘আবশির’ (ঠিক আছে, করছি), ‘ইনশাআল্লাহ’, ‘শুকরান’। মালিক খুশি হন, কাজও সহজ হয়।",
                "মালিকের সাথে কখনো মিথ্যা বলবে না। ভুল হলে নিজে থেকেই স্বীকার করবে। ভুল লুকালে সেটা বড় হয়ে ফিরে আসে।"
            )),
            AdviceBlock("আমানত ও সততা", listOf(
                "গাড়ি, চাবি, পেট্রলের টাকা বা কার্ড, সবই তোমার কাছে আমানত। একটি রিয়ালও হিসাবের বাইরে নেবে না।",
                "মালিকের অনুমতি ছাড়া গাড়ি নিজের কাজে চালাবে না, অন্য কাউকে চালাতে দেবে না, বাইরের লোক গাড়িতে তুলবে না।",
                "মালিকের ঘরের কথা, পরিবারের কথা ও ব্যবসার কথা অন্য কারও কাছে বলবে না। এটাও আমানত।",
                "খরচের রসিদ রাখবে এবং ছোট একটি খাতায় হিসাব লিখে রাখবে। হিসাব পরিষ্কার থাকলে কেউ সন্দেহ করতে পারে না।"
            )),
            AdviceBlock("সময় ও দায়িত্ব", listOf(
                "নির্ধারিত সময়ের আগে হাজির থাকবে। কোনো কারণে দেরি হলে আগেই জানিয়ে দেবে।",
                "নামাজের সময় হলে মালিককে বিনয়ের সাথে জানিয়ে নামাজের সুযোগ করে নেবে। ভালোভাবে বললে প্রায় সবাই বুঝেন।",
                "বললে যেটুকু করার কথা, তার চেয়ে একটু বেশি যত্ন নিয়ে করবে। গাড়ি পরিষ্কার, কাগজপত্র তৈরি, সময়মতো হাজির, এই তিনটিতেই মালিকের আস্থা তৈরি হয়।"
            )),
            AdviceBlock("নিজের অধিকার ও সীমা", listOf(
                "চুক্তি অনুযায়ী বেতন, থাকা-খাওয়া ও ছুটির কথা চুক্তির কাগজে দেখে নেবে। পাওনা বিনয়ের সাথে সময়মতো চাইবে।",
                "হারাম বা বেআইনি কাজের আদেশ এলে ভদ্রভাবে না বলবে। হাদিসে আছে, স্রষ্টার অবাধ্যতায় সৃষ্টির আনুগত্য নেই।",
                "কাজ ছেড়ে পালিয়ে যাবে না (হুরুব)। সমস্যা হলে শ্রম অফিস ও বাংলাদেশ দূতাবাসের মাধ্যমে বৈধ পথে সমাধান খুঁজবে।",
                "নিজের পাসপোর্ট ও ইকামার ফটোকপি সবসময় নিজের কাছে এবং দেশে পরিবারের কাছেও রাখবে।"
            ))
        )
    ),
    AdviceTopic(
        title = "গাড়ি চালানোর কানুন",
        emoji = "🚗",
        color = Color(0xFFE65100),
        intro = "গাড়ি চালানো শুধু পেশা নয়, মানুষের জীবনের দায়িত্বও। তোমার এক মুহূর্তের অসাবধানতা একটি পরিবারকে শেষ করে দিতে পারে।",
        blocks = listOf(
            AdviceBlock("রওনা হওয়ার আগে", listOf(
                "বিসমিল্লাহ বলে গাড়িতে উঠবে এবং সফরের দোয়া পড়বে (নিচে দেওয়া আছে)।",
                "ইকামা, ড্রাইভিং লাইসেন্স, গাড়ির ইস্তিমারা (রেজিস্ট্রেশন) ও বীমার কাগজ সবসময় সাথে রাখবে। মেয়াদ কবে শেষ হবে আগে থেকে দেখে রাখবে।",
                "সিটবেল্ট বাঁধবে, আয়না ঠিক করবে, লাইট, ব্রেক, টায়ার ও তেলের অবস্থা একবার দেখে নেবে।"
            )),
            AdviceBlock("রাস্তায় চলার নিয়ম", listOf(
                "গতিসীমা মানবে। সাহার (স্পিড ক্যামেরা) প্রায় সর্বত্র আছে। জরিমানা মালিক ও তোমার দুজনেরই ক্ষতি।",
                "গাড়ি চলা অবস্থায় মোবাইল ধরবে না: ফোন, মেসেজ, ভিডিও সব বন্ধ। জরুরি হলে নিরাপদ জায়গায় থেমে কথা বলবে।",
                "লাল বাতি, লেন, ওভারটেক ও ইউ-টার্নের নিয়ম মানবে। সিগন্যাল না দিয়ে লেন বদলাবে না।",
                "সামনের গাড়ির সাথে নিরাপদ দূরত্ব রাখবে। হাইওয়েতে সেই দূরত্ব আরও বেশি রাখবে।",
                "রাগ করে গাড়ি চালাবে না। কেউ ভুল করলে ছাড় দাও। রাস্তায় জেতার চেয়ে নিরাপদে পৌঁছানো বড় কথা।",
                "ক্লান্ত বা ঘুম পেলে গাড়ি চালাবে না। দুই-আড়াই ঘণ্টা পর পর থেমে বিশ্রাম নেবে, মুখে পানি দেবে।",
                "ধুলোঝড়, বৃষ্টি বা কুয়াশায় গতি কমাবে ও লাইট জ্বালাবে। হঠাৎ বৃষ্টিতে ওয়াদি (নিচু খাল) দিয়ে পানি নামে, জলস্রোতের মধ্যে গাড়ি নামাবে না।",
                "মরু হাইওয়েতে রাতে উট বা পশু হঠাৎ রাস্তায় চলে আসতে পারে। রাতে গতি কমিয়ে সতর্ক থাকবে।",
                "নেশা বা ঘুমের ওষুধের প্রভাবে গাড়ি চালানো হারাম এবং কঠিন অপরাধ।"
            )),
            AdviceBlock("যাত্রীর সাথে আদব", listOf(
                "যাত্রী উঠলে ও নামলে সালাম ও ভদ্র ব্যবহার করবে। প্রয়োজনে দরজা খুলে দেবে ও মালপত্র তুলতে সাহায্য করবে।",
                "শিশু থাকলে সিটবেল্ট বা শিশু-আসন ঠিক আছে কি না নিশ্চিত করবে। শিশুদের সামনে বসতে দেবে না।",
                "গাড়ি ভেতরে-বাইরে পরিষ্কার রাখবে। নিজেও পরিষ্কার পোশাকে ও পরিচ্ছন্ন থাকবে।",
                "যাত্রীদের কথা কান পেতে শুনবে না। গাড়িতে যা দেখেছ বা শুনেছ, বাইরে কাউকে বলবে না।"
            )),
            AdviceBlock("দুর্ঘটনা হলে", listOf(
                "মাথা ঠান্ডা রাখবে। সবার আগে দেখবে কেউ আহত হয়েছে কি না। জরুরি নম্বর ৯১১ (এলাকাভেদে অ্যাম্বুলেন্স ৯৯৭, ট্রাফিক ৯৯৩)।",
                "ঘটনাস্থল ছেড়ে পালাবে না। ট্রাফিক পুলিশ বা নাজমের রিপোর্ট ছাড়া চলে যাবে না। নিয়ম বদলাতে পারে, তাই মালিকের কাছে আগেই জেনে রাখবে।",
                "মালিককে সাথে সাথে জানাবে। নিজে নিজে দোষ স্বীকার বা অস্বীকার করে মীমাংসা করতে যাবে না।",
                "সম্ভব হলে গাড়ির অবস্থান, ক্ষতি ও কাগজপত্রের ছবি তুলে রাখবে।"
            ))
        ),
        duaArabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
        duaMeaning = "পবিত্র সেই সত্তা, যিনি এই বাহনকে আমাদের অনুগত করে দিয়েছেন। আমরা একে বশ করতে পারতাম না। আমরা অবশ্যই আমাদের রবের কাছে ফিরে যাব।"
    ),
    AdviceTopic(
        title = "গাড়ির রক্ষণাবেক্ষণ",
        emoji = "🔧",
        color = Color(0xFF37474F),
        intro = "গাড়ি মালিকের আমানত এবং তোমার রুটি-রুজির হাতিয়ার। যত্ন নিলে গাড়ি তোমাকে বিপদ থেকে বাঁচাবে, অবহেলা করলে বিপদে ফেলবে।",
        blocks = listOf(
            AdviceBlock("প্রতিদিন", listOf(
                "সকালে রওনার আগে গাড়ির চারপাশ একবার ঘুরে দেখবে: টায়ার, লাইট, গাড়ির নিচে তেল বা পানি ঝরছে কি না।",
                "ড্যাশবোর্ডে কোনো সতর্ক বাতি জ্বললে অবহেলা করবে না। মালিককে জানাবে।",
                "জ্বালানি অর্ধেকের নিচে নামার আগেই ভরে নেবে। মরু পথে তেল শেষ হওয়া বিপজ্জনক।",
                "ভেতর-বাইরে মুছে পরিষ্কার রাখবে। ধুলো জমতে দেবে না।"
            )),
            AdviceBlock("সপ্তাহে ও মাসে", listOf(
                "টায়ারের হাওয়া ঠান্ডা অবস্থায় (সকালে) মাপবে। গরমে বেশি বা কম হাওয়ায় টায়ার ফেটে যায়। কত হাওয়া লাগবে তা গাড়ির দরজার পাশের স্টিকারে বা ম্যানুয়ালে লেখা থাকে।",
                "স্পেয়ার টায়ার, জ্যাক ও চাকা খোলার যন্ত্র ঠিক আছে কি না দেখবে।",
                "ইঞ্জিন অয়েল (ডিপস্টিক দিয়ে), রেডিয়েটরের কুল্যান্ট, ব্রেক ফ্লুইড ও ওয়াইপারের পানি দেখবে।",
                "ইঞ্জিন গরম থাকলে রেডিয়েটরের ক্যাপ খুলবে না। গরম বাষ্পে মারাত্মক পুড়ে যেতে পারে।",
                "ব্যাটারির টার্মিনাল পরিষ্কার রাখবে। সৌদির গরমে ব্যাটারি দ্রুত দুর্বল হয়।",
                "এসি ঠিকমতো ঠান্ডা না করলে এসি ফিল্টার ও গ্যাস দেখাবে।"
            )),
            AdviceBlock("নির্দিষ্ট সময় পর পর", listOf(
                "তেল ও ফিল্টার বদলানো এবং সার্ভিসিং প্রস্তুতকারকের নির্দেশনা অনুযায়ী করবে। কত কিলোমিটার পর পর করতে হবে, মালিকের সাথে কথা বলে ঠিক করবে।",
                "ব্রেক প্যাড ও টায়ারের ক্ষয় নিয়মিত পরীক্ষা করবে। ব্রেকে শব্দ হলে দেরি করবে না।",
                "ফাহস (বার্ষিক পরিদর্শন), ইস্তিমারা ও বীমার মেয়াদের তারিখ লিখে রাখবে, যাতে মেয়াদ শেষ হওয়ার আগেই নবায়ন হয়।",
                "মালিকের অনুমতি ছাড়া মেরামত বা যন্ত্রাংশ কেনা করবে না। রসিদ রাখবে।",
                "মেকানিক বা দোকান থেকে গোপন কমিশন নেবে না। এটা খিয়ানত।"
            )),
            AdviceBlock("গাড়িতে যা থাকা ভালো", listOf(
                "অগ্নিনির্বাপক যন্ত্র, প্রাথমিক চিকিৎসার বাক্স, সতর্ক ত্রিভুজ, জাম্পার কেবল, টর্চ।",
                "মরু পথের জন্য খাবার পানির বোতল ও সাথে মোবাইল চার্জার।"
            )),
            AdviceBlock("গরমের সতর্কতা", listOf(
                "রোদে বন্ধ গাড়ির ভেতরে শিশু বা পশুকে কখনো রেখে যাবে না। কয়েক মিনিটেই প্রাণ যেতে পারে।",
                "রোদে রাখা গাড়িতে লাইটার, স্প্রে ক্যান বা পানির বোতল ফেলে রাখবে না।",
                "ছোট একটি খাতায় সার্ভিসের তারিখ, কিলোমিটার ও খরচ লিখে রাখবে। এতে মালিক ও তুমি দুজনেই নিশ্চিন্ত থাকবে।"
            ))
        )
    ),
    AdviceTopic(
        title = "মায়ের সাথে আচরণ",
        emoji = "👩",
        color = Color(0xFFAD1457),
        intro = "এক ব্যক্তি রাসূল (সা.)-কে জিজ্ঞেস করল, আমার সদ্ব্যবহার পাওয়ার সবচেয়ে বেশি হকদার কে? তিনি তিনবার বললেন, তোমার মা। চতুর্থবার বললেন, তারপর তোমার বাবা।",
        blocks = listOf(
            AdviceBlock("কথা ও মন", listOf(
                "কুরআনে মা-বাবাকে ‘উফ’ পর্যন্ত বলতে নিষেধ করা হয়েছে (সূরা বনী ইসরাইল ২৩)। মা বিরক্ত করলেও বা বয়সের কারণে ভুল করলেও ধৈর্য ধরবে।",
                "মায়ের সাথে নরম গলায় কথা বলবে, তাঁর কথা মন দিয়ে শুনবে, মাঝপথে থামাবে না।",
                "মা কিছু চাইলে সম্ভব হলে সাথে সাথে দেবে। সম্ভব না হলে নরমভাবে বুঝিয়ে বলবে, কখনো ধমক দেবে না।"
            )),
            AdviceBlock("দূর থেকে সেবা", listOf(
                "প্রতিদিন না পারলেও সপ্তাহে কয়েকবার ফোন বা ভিডিও কল করবে। মা তোমার মুখ দেখে আর ‘ভালো আছি’ শুনে শান্তি পান।",
                "টাকা পাঠানোর সময় মায়ের জন্য আলাদা একটা অংশ রাখবে, যেটা তিনি নিজের মতো খরচ করতে পারেন।",
                "ঈদ ও রমজানের আগে মায়ের জন্য কিছু উপহার পাঠাবে। দামি কিছু না, মনে রাখাটাই বড়।",
                "মায়ের অসুখে চিকিৎসা ও ওষুধ ঠিকমতো চলছে কি না নিয়মিত খোঁজ নেবে। দেশে কাছের কাউকে দায়িত্ব দিয়ে রাখবে।"
            )),
            AdviceBlock("মনে রাখবে", listOf(
                "মা তোমাকে কত কষ্টে বড় করেছেন, সেই ঋণ শোধ হয় না। তাই তাঁর সামান্য কষ্টও বড় করে দেখবে।",
                "বাবা-মা আলাদা থাকলেও দুজনই তোমার মা-বাবা। একজনের কথা অন্যজনের কাছে মন্দভাবে বলবে না।",
                "সুযোগ হলে মাকে ওমরাহ বা হজে নিয়ে যাওয়া সবচেয়ে বড় উপহার।",
                "মায়ের দোয়া নিয়ে সফরে বের হবে। মায়ের দোয়া কোনো বিপদে ফেরে না।",
                "রোজ মায়ের জন্য দোয়া করবে (নিচে দেওয়া আছে)।"
            ))
        ),
        duaArabic = "رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
        duaMeaning = "হে আমার রব, তাঁদের উভয়ের প্রতি দয়া করুন, যেমন তাঁরা ছোটবেলায় আমাকে লালন-পালন করেছেন।"
    ),
    AdviceTopic(
        title = "ছোট ভাইয়ের সাথে আচরণ",
        emoji = "👦",
        color = Color(0xFF00838F),
        intro = "রাসূল (সা.) বলেছেন, যে ছোটদের স্নেহ করে না এবং বড়দের সম্মান জানায় না, সে আমাদের দলভুক্ত নয়। তুমি বড় ভাই, তাই তোমার দায়িত্ব বাবার ছায়ার মতো।",
        blocks = listOf(
            AdviceBlock("স্নেহ ও দায়িত্ব", listOf(
                "ছোট ভাইকে সময় দেবে। ফোনে তার পড়াশোনা, বন্ধুবান্ধব ও দিনকালের খোঁজ নেবে। সে যেন বোঝে, তুমি দূরে থেকেও তার পাশে আছো।",
                "ভুল করলে সবার সামনে অপমান করবে না। একান্তে নরম ভাষায় বোঝাবে।",
                "আদর ও শাসন দুটোই দরকার। শুধু শাসন করলে সে তোমার কাছ থেকে দূরে সরে যাবে, শুধু আদর করলে নষ্ট হবে।",
                "তুমি তার বন্ধুর মতো হও, যাতে ভালো-মন্দ সব কথা সে তোমাকে বলতে পারে।"
            )),
            AdviceBlock("শিক্ষা ও চরিত্র", listOf(
                "তাকে নামাজ, কুরআন ও আদব শেখাবে। দ্বীন শেখানো সবচেয়ে বড় উপহার।",
                "পড়াশোনার খরচ যথাসাধ্য চালাবে। পড়ায় মন না থাকলে তার হাতে একটি দক্ষতা তুলে দেবে, যেমন কম্পিউটার, মোবাইল সার্ভিসিং, ইলেকট্রিক কাজ বা ড্রাইভিং, যাতে সে নিজের পায়ে দাঁড়াতে পারে।",
                "খারাপ সঙ্গ, নেশা ও মোবাইলের অপব্যবহার নিয়ে সতর্ক থাকবে। তাকে ধমক না দিয়ে বুঝিয়ে বলবে।"
            )),
            AdviceBlock("টাকা ও উপহার", listOf(
                "তার হাতখরচ ঠিকমতো দেবে, কিন্তু অপচয়ের অভ্যাস করতে দেবে না।",
                "ছোটবেলা থেকে তাকে টাকার মূল্য ও সঞ্চয়ের অভ্যাস শেখাবে।",
                "মাঝেমধ্যে ছোট উপহার পাঠাবে। এতে সম্পর্ক গভীর হয়।"
            )),
            AdviceBlock("ভ্রাতৃত্বের বন্ধন", listOf(
                "টাকা বা সম্পত্তি নিয়ে ভাইয়ের সাথে ঝগড়া করবে না। ভাইয়ের সম্পর্ক সবকিছুর চেয়ে দামি।",
                "অন্যের সাথে তুলনা করবে না। তার সাফল্যে খুশি হবে, ব্যর্থতায় পাশে দাঁড়াবে।",
                "তাকে সৌদিতে আনার চিন্তা থাকলে শুধু বৈধ ভিসা ও বৈধ পথে আনবে। দালালের লোভনীয় প্রস্তাবে প্রতারিত হবে না।",
                "মায়ের খেদমতে ভাইকে সাথে রাখবে। দুজন মিলে দায়িত্ব নিলে মা বেশি শান্তি পান।"
            ))
        )
    ),
    AdviceTopic(
        title = "বাবার সাথে আচরণ",
        emoji = "👴",
        color = Color(0xFF5D4037),
        intro = "সিয়াম, বাবা তোমার কাছে কিছুই চায় না। এই পাতার প্রতিটি কথা বাবা বুকের ভেতর থেকে লিখেছে। পড়তে গিয়ে চোখ ভিজে গেলে লজ্জা পেয়ো না। লিখতে গিয়ে তোমার বাবারও চোখ ভিজেছিল।",
        blocks = listOf(
            AdviceBlock("বাবা তোমার কাছে যা চায় না", listOf(
                "আমি তোমার কাছে টাকা চাই না, এক রিয়ালও না। তুমি পরদেশে রোদে পুড়ে স্টিয়ারিং ধরে যা কামাও, সেটা তোমার ঘাম। সেই ঘামের হক তোমার মা আর তোমার ভাইয়ের।",
                "বাবা হয়ে ছেলের কাছে হাত পাতলে আমি ছোট হয়ে যাব। আমাকে ছোট কোরো না। আমার সম্মান রাখতে চাইলে যা পাঠাবে, মায়ের কাছেই পাঠাবে।",
                "আমার জন্য কোনো চিন্তা কোরো না। আল্লাহ আছেন, তিনিই যথেষ্ট। তোমরা ভালো থাকলেই আমি ভালো আছি।",
                "দূরে আছ বলে বাবার জন্য দায়িত্ব নেওয়ার বোঝা মাথায় নিয়ো না। বাবার দায়িত্ব ছিল তোমাদের জন্য। আজও সেই দায়িত্বের জায়গা থেকেই কথা বলছি।"
            )),
            AdviceBlock("বাবার একটাই আমানত: তোমার মা", listOf(
                "তোমার মা নুরজাহান আমার জীবনের সবচেয়ে প্রিয় মানুষ। আমাদের পথ আলাদা হয়ে গেছে, কিন্তু তাঁর জন্য আমার ভালোবাসা আর দোয়া একটুও কমেনি।",
                "আমার একটাই চাওয়া, সেটা তোমার কাছে আমানত: তোমার মা যেন ভালো থাকেন। তাঁর মুখে যেন হাসি থাকে, চোখে যেন কখনো পানি না আসে।",
                "তাঁর ওষুধ, খাওয়া, ঈদের কাপড়, ছোট ছোট শখের খোঁজ নিও। না চাইতেই বুঝে নিও কী লাগবে। মা মুখ ফুটে কিছু বলবেন না, সন্তানকে কষ্ট দিতে চান না কোনো মা।",
                "তোমার একটা ফোন, ‘মা, কেমন আছো? খেয়েছ?’ তাঁর কাছে দুনিয়ার সব উপহারের চেয়ে বড়। প্রতিদিন না পারলেও যতবার পারো, কোরো।",
                "আমাদের আলাদা হওয়ার কষ্ট যদি তোমার মনে জমে থাকে, সেটা কারও ওপর ঝেড়ো না। না মায়ের ওপর, না আমার ওপর। কারও কথা কারও কাছে মন্দভাবে বোলো না।",
                "মায়ের কোনো কষ্ট দেখলে সবার আগে ছুটে যেও। মায়ের দোয়া তোমার সবচেয়ে বড় পাহারাদার।"
            )),
            AdviceBlock("ছোট ভাই ও তোমরা তিনজন", listOf(
                "তোমার ছোট ভাইয়ের মনেও বাবার অভাব লাগবে। তুমি তার বড় ভাই, আর অনেক সময় বাবার জায়গাটাও তোমাকে নিতে হবে। তার কাঁধে হাত রেখে বোলো, ‘আমি আছি।’",
                "তাকে পড়াশোনা, নামাজ আর ভালো চরিত্রের পথে রেখো। তার হাতে এমন কিছু তুলে দিও, যাতে সে একদিন নিজের পায়ে দাঁড়াতে পারে।",
                "তোমরা তিনজন, মা, তুমি আর তোমার ভাই, মিলে একটা পরিবার। নিজেদের মধ্যে অভিমান, ঝগড়া আর দূরত্ব আসতে দিও না।",
                "নিজের জন্যও কিছু রেখো। ঠিকমতো খেও, ঘুমিও, কিছু সঞ্চয় করো। তুমি ভালো থাকলেই মা আর ভাই ভালো থাকবে। তুমি ভেঙে পড়লে ওদের আশ্রয় কে হবে?"
            )),
            AdviceBlock("বাবার ছোট্ট চাওয়া", listOf(
                "কখনো কখনো ফোন করে শুধু বোলো, ‘বাবা, কেমন আছো?’ এটুকুই আমার সবচেয়ে বড় সম্পদ। টাকা নয়, এই একটা কথা।",
                "ফোন করতে না পারলেও রাগ করব না। আমি জানি তুমি কাজে ব্যস্ত। বাবা সবসময় তোমার জন্য দোয়া করে।",
                "সেজদায় আমার জন্য একটু দোয়া কোরো। আমিও প্রতি নামাজে তোমার জন্য করি: তুমি যেন নিরাপদে থাকো, সৎ থাকো, আর একদিন ফিরে এসে মায়ের কোলে মাথা রাখতে পারো।",
                "যেদিন তুমি নিজে বাবা হবে, সেদিন বুঝবে বাবার বুকের ভেতরটা কেমন হয়।",
                "আমি যদি কখনো কোনো ভুল করে থাকি, বাবা বলে আল্লাহর ওয়াস্তে ক্ষমা করে দিও। আমি তোমাকে অনেক ভালোবাসি, সিয়াম।"
            ))
        ),
        duaArabic = "رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ",
        duaMeaning = "হে আমাদের রব, হিসাবের দিনে আমাকে, আমার মা-বাবাকে ও সব মুমিনকে ক্ষমা করুন।"
    ),
    AdviceTopic(
        title = "কেমন স্ত্রী হবে",
        emoji = "💍",
        color = Color(0xFFC2185B),
        intro = "রাসূল (সা.) বলেছেন, সাধারণত চার কারণে নারীকে বিয়ে করা হয়: সম্পদ, বংশ, সৌন্দর্য ও দ্বীন। তুমি দ্বীনদার নারীকে প্রাধান্য দাও, তাহলেই সফল হবে।",
        blocks = listOf(
            AdviceBlock("কেমন পাত্রী খুঁজবে", listOf(
                "দ্বীনদার ও নামাজি। নামাজ ও পর্দার ব্যাপারে যত্নবান। যে আল্লাহকে ভয় করে, সে স্বামীর অনুপস্থিতিতেও আমানত রক্ষা করে।",
                "চরিত্রবতী, লজ্জাশীলা ও নম্র স্বভাবের। কথায় ও আচরণে ভদ্র।",
                "ধৈর্যশীলা ও কৃতজ্ঞ। প্রবাসীর স্ত্রীকে অনেক সময় একা সংসার সামলাতে হয়, তাই সবর জরুরি।",
                "পরিবার ভালো কি না দেখবে। মেয়ের মা-বাবার আচরণ ও পরিবেশ থেকে মেয়ের শিক্ষা বোঝা যায়।",
                "সংসারে বুদ্ধিমতী ও হিসাবি। খরচে মধ্যপন্থা মেনে চলে।"
            )),
            AdviceBlock("কীভাবে খুঁজবে", listOf(
                "বিয়ের কথা এগোতে হবে পরিবারের মাধ্যমে, সম্মানের সাথে। লুকিয়ে প্রেম বা অবৈধ সম্পর্ক করে বিয়ের কথা ভাববে না।",
                "মা-বাবা ও বিশ্বস্ত আত্মীয়দের দিয়ে মেয়ে ও পরিবারের খোঁজখবর নেবে। মাকে সাথে নিয়ে দেখবে।",
                "বিয়ের আগে ইস্তিখারা করবে ও আল্লাহর কাছে কল্যাণ চাইবে।",
                "যৌতুক নেবে না। মোহর সাধ্যের মধ্যে ঠিক করবে ও খুশিমনে আদায় করবে। বিয়েতে অপচয় ও লোক দেখানো খরচ এড়াবে।"
            )),
            AdviceBlock("যা এড়িয়ে চলবে", listOf(
                "শুধু রূপ বা টাকা দেখে সিদ্ধান্ত নেবে না।",
                "ফোন বা অনলাইনে পরিচয় হয়ে ‘বিয়ে করব’ বলে টাকা চাওয়া মানুষ থেকে সাবধান। প্রবাসী ছেলের টাকার লোভে প্রস্তাব এলে ভালো করে যাচাই করবে।",
                "তাড়াহুড়ো করে বিয়ে করবে না, আবার অযথা দেরিও করবে না। দুই দিকেই ক্ষতি।"
            )),
            AdviceBlock("বিয়ের পর স্ত্রীর সাথে", listOf(
                "রাসূল (সা.) বলেছেন, তোমাদের মধ্যে সেই সবচেয়ে ভালো, যে তার স্ত্রীর কাছে সবচেয়ে ভালো।",
                "স্ত্রীর সাথে নম্র থাকবে। রাগের মাথায় চেঁচামেচি করবে না, হাত তুলবে না, অপমান করবে না।",
                "প্রবাসে থাকলে নিয়মিত ফোন করবে। বেশিদিন স্ত্রী থেকে দূরে থাকা ঠিক নয়।",
                "মা ও স্ত্রীর মধ্যে ইনসাফ করবে। একজনকে অন্যজনের বিরুদ্ধে ব্যবহার করবে না।",
                "আমাদের মতো সংসার ভাঙতে দিও না। ঝগড়া হলে আগে তুমি সরে এসো, ঠান্ডা হয়ে কথা বলো।"
            ))
        )
    ),
    AdviceTopic(
        title = "কেমন লোকের সাথে চলবে",
        emoji = "👥",
        color = Color(0xFF00695C),
        intro = "রাসূল (সা.) বলেছেন, মানুষ তার বন্ধুর দ্বীনের ওপর চলে। তাই কার সাথে বন্ধুত্ব করছ, ভেবে দেখো। সৎসঙ্গ আতর বিক্রেতার মতো: কিছু না দিলেও সুবাস পাবে।",
        blocks = listOf(
            AdviceBlock("যাদের সাথে চলবে", listOf(
                "নামাজি, সৎ ও পরিশ্রমী প্রবাসী ভাই। যারা হালাল পথে আয় করে ও দেশে টাকা পাঠায়।",
                "অভিজ্ঞ ড্রাইভার ও বয়স্ক মানুষ। তাঁদের কাছে কাজ ও জীবন শিখবে।",
                "যারা ঋণমুক্ত থাকে ও সঞ্চয় করে। তাদের অভ্যাস তোমার ভালো লাগবে।",
                "যারা তোমার ভুল সামনে বলে, পেছনে নিন্দা করে না। এরাই আসল বন্ধু।",
                "আলেম ও দ্বীনদার মানুষের মজলিসে সুযোগ পেলে বসবে।"
            )),
            AdviceBlock("যাদের থেকে দূরে থাকবে", listOf(
                "যারা নেশা, জুয়া, অশ্লীলতা ও গীবতে জড়িত।",
                "যারা বলে ‘ছোট একটা বেআইনি কাজে অনেক টাকা’। বৈধ পথের বাইরে কোনো আয়ে জড়াবে না। শাস্তি ও লজ্জা দুটোই ভারী।",
                "যারা মালিকের নিন্দা করে বা ঠকাতে প্ররোচিত করে।",
                "যারা বারবার ধার চায় বা তাড়াতাড়ি বড়লোক হওয়ার লোভ দেখায়।",
                "যারা কাজ ছেড়ে পালানোর (হুরুব) পরামর্শ দেয়।"
            )),
            AdviceBlock("প্রবাসে সতর্কতা", listOf(
                "অচেনা কারও প্যাকেট বা ব্যাগ গাড়িতে বহন করবে না। ভেতরে মাদক বা অবৈধ জিনিস থাকতে পারে, শাস্তি ভয়ংকর।",
                "অচেনা কাউকে ইকামা, পাসপোর্ট, ব্যাংক কার্ড, পিন বা ওটিপি দেবে না। ফোনে ‘বিকাশ/ব্যাংক থেকে বলছি’ বলে চাইলেও না।",
                "কারও কথায় নিজের ইকামা বা নাম ব্যবহার করে ব্যবসা বা লেনদেন করতে দেবে না।",
                "সহকর্মী ও প্রবাসী ভাইদের সাথে রাজনীতি, এলাকা বা মত নিয়ে অকারণ বিতর্ক করবে না।"
            )),
            AdviceBlock("তোমার আচরণ", listOf(
                "সবার সাথে সালাম ও হাসিমুখে কথা বলবে। সবার সাথে ভদ্রতা, কিন্তু গভীর বন্ধুত্ব বেছে নিয়ে।",
                "মানুষের উপকার করবে, কিন্তু বদলে কিছু চাইবে না।",
                "ঝগড়া হলে সরে আসবে। রাগের মাথায় কিছু বললে পরে ক্ষমা চাইবে।",
                "একা থাকলে মন খারাপ হয়, এটা স্বাভাবিক। সেই সময় ভালো বন্ধুর সাথে বসবে, ফোনে পরিবারের সাথে কথা বলবে, কুরআন পড়বে।"
            ))
        )
    ),
    AdviceTopic(
        title = "কফিলের পরিবারের সাথে ব্যবহার",
        emoji = "🏠",
        color = Color(0xFF4527A0),
        intro = "কফিলের পরিবারের সদস্যরা তোমার কাজের পরিবেশেরই অংশ। তাদের সাথে সম্মান, দূরত্ব ও আমানত রক্ষা করলে তুমি যেমন নিরাপদ থাকবে, মালিকের আস্থাও পাবে।",
        blocks = listOf(
            AdviceBlock("সাধারণ আদব", listOf(
                "কফিলের মা, স্ত্রী, বোন, মেয়ে, ছেলে সবার সাথে সম্মানের সাথে কথা বলবে। সালাম দেবে, সংক্ষেপে ও প্রয়োজনের কথা বলবে।",
                "বড় ও বয়স্কদের বিশেষ সম্মান করবে। তাঁদের কথার প্রতিবাদ করবে না।",
                "অকারণ গল্প, উপদেশ বা নিজের ব্যক্তিগত বিষয় নিয়ে আলোচনা করবে না।"
            )),
            AdviceBlock("নারী সদস্যদের সাথে", listOf(
                "দৃষ্টি নিচু রাখবে (সূরা নূর ৩০)। প্রয়োজনের বাইরে কথা বলবে না।",
                "মালিক যে নিয়মে গাড়িতে বসার ব্যবস্থা ঠিক করেছেন তা মানবে। যাত্রী নারী হলে সাধারণত পেছনের সিটে বসতে দেবে এবং গাড়ির আয়নায় তাকাবে না।",
                "মালিকের পরিবারের কারও ছবি বা ভিডিও তুলবে না। তাঁদের ফোন নম্বর, সোশ্যাল মিডিয়া বা ব্যক্তিগত কিছু চাইবে না, নিজেরটাও দেবে না।",
                "কোনো অস্বস্তিকর পরিস্থিতি হলে ভদ্রভাবে সরে আসবে এবং দরকারে মালিককে জানাবে। কখনো একান্ত নির্জনতায় (খালওয়াত) জড়াবে না।"
            )),
            AdviceBlock("শিশু ও বৃদ্ধদের সাথে", listOf(
                "শিশুদের সিটবেল্ট ঠিক আছে কি না আগে দেখবে। ধীরে ও নিরাপদে চালাবে।",
                "স্কুলে আনা-নেওয়ায় সময়মতো থাকবে। শিশুকে কখনো একা রেখে যাবে না।",
                "বৃদ্ধ বা অসুস্থ যাত্রীকে উঠতে-নামতে সাহায্য করবে। ঝাঁকুনি এড়িয়ে ধীরে চালাবে।"
            )),
            AdviceBlock("আমানত রক্ষা", listOf(
                "পরিবারের কে কোথায় যায়, কখন, কার সাথে, কাউকে বলবে না। এটা আমানত।",
                "একজনের কথা অন্যজনের কাছে লাগাবে না। পারিবারিক বিবাদে নিরপেক্ষ থাকবে, কোনো পক্ষ নেবে না।",
                "কেউ মালিকের অজান্তে অস্বাভাবিক কোথাও যেতে বললে নম্রভাবে মালিককে জানাবে।"
            )),
            AdviceBlock("কাজ ও ধৈর্য", listOf(
                "পরিবারের সদস্যদের ফরমাইশ মালিকের নির্দেশ অনুযায়ী পালন করবে। অতিরিক্ত বা অনুচিত হলে নম্রভাবে মালিককে জানাবে।",
                "কেউ কটু কথা বললে চুপ থাকো বা ‘ইনশাআল্লাহ’ বলে এড়িয়ে যাও। সবর করো।",
                "তবে মারধর বা গুরুতর অপমান হলে সহ্য করার দরকার নেই। বৈধ পথে (শ্রম অফিস, দূতাবাস) প্রতিকার চাইবে।"
            ))
        )
    )
)

private fun topicFullText(t: AdviceTopic): String {
    val sb = StringBuilder()
    sb.append(t.emoji).append(" ").append(t.title).append("\n\n").append(t.intro).append("\n\n")
    var n = 0
    for (b in t.blocks) {
        sb.append(b.heading).append("\n")
        for (item in b.items) {
            n++
            sb.append(bn(n)).append(". ").append(item).append("\n")
        }
        sb.append("\n")
    }
    if (t.duaArabic != null) {
        sb.append(t.duaArabic).append("\n")
        if (t.duaMeaning != null) sb.append(t.duaMeaning).append("\n")
        sb.append("\n")
    }
    sb.append(TOPIC_SIGN)
    return sb.toString()
}

// ---------- বাটনের সারি ----------
@Composable
fun AdviceTopicGrid(onOpen: (AdviceTopic) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            tr("বিষয় অনুযায়ী উপদেশ"),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TopicDeep
        )
        Text(
            tr("যে বিষয়ে জানতে চান, সেই বাটনে চাপ দিন"),
            fontSize = 13.sp,
            color = TopicInk.copy(alpha = 0.7f)
        )
        for (rowTopics in adviceTopics.chunked(2)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (topic in rowTopics) {
                    val ctx = LocalContext.current
                    val iconId = remember(topic.title) {
                        val n = iconNames[topic.title]
                        if (n == null) 0 else ctx.resources.getIdentifier(n, "drawable", ctx.packageName)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.5.dp, topic.color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable { onOpen(topic) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (iconId != 0) {
                                Image(
                                    painter = painterResource(id = iconId),
                                    contentDescription = tr(topic.title),
                                    modifier = Modifier.size(84.dp)
                                )
                            } else {
                                Text(topic.emoji, fontSize = 40.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                tr(topic.title),
                                color = topic.color,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
                if (rowTopics.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// ---------- বিস্তারিত পাতা ----------
@Composable
fun AdviceTopicDialog(topic: AdviceTopic, onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = TopicCream) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TopicDeep)
                        .padding(top = 24.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(tr("← ফিরে যান"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText(topic.title, topicFullText(topic)))
                        Toast.makeText(context, "কপি হয়েছে ✅", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("📋 কপি", color = TopicGold, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, topicFullText(topic))
                        }
                        val chooser = Intent.createChooser(send, null)
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                    }) {
                        Text("📤 শেয়ার", color = TopicGold, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(listOf(topic.color, TopicDeep)),
                                RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(topic.emoji, fontSize = 44.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                tr(topic.title),
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("✦  ✦  ✦", color = TopicGold, fontSize = 14.sp)
                        }
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, TopicGold),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                topic.intro,
                                modifier = Modifier.padding(18.dp),
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                color = TopicInk
                            )
                        }

                        var counter = 0
                        for (block in topic.blocks) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(topic.color, topic.color.copy(alpha = 0.75f))
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    block.heading,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            for (item in block.items) {
                                counter++
                                val number = counter
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, topic.color.copy(alpha = 0.35f)),
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
                                                .background(
                                                    Brush.radialGradient(listOf(Color(0xFFFFE082), TopicGold))
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                bn(number),
                                                color = TopicDeep,
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
                                            color = TopicInk
                                        )
                                    }
                                }
                            }
                        }

                        if (topic.duaArabic != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Brush.verticalGradient(listOf(TopicGreen, TopicDeep)))
                                    .padding(22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🤲", fontSize = 30.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        topic.duaArabic,
                                        color = TopicGold,
                                        fontSize = 22.sp,
                                        lineHeight = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    if (topic.duaMeaning != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            topic.duaMeaning,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            lineHeight = 24.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            TOPIC_SIGN,
                            modifier = Modifier.fillMaxWidth(),
                            color = TopicDeep,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
