package com.setubandhu.app

import android.app.DatePickerDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

private const val DAY_MS = 86400000L
private const val KEY_BIRTH = "birth"
private val MutedColor = Color(0xFF666666)

// ---------- মাস ও বারের নাম (তিন ভাষায়) ----------
private val gMonthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
private val gMonthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
private val gMonthsAr = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
private val gMonthsShortBn = listOf("জানু", "ফেব্রু", "মার্চ", "এপ্রি", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")
private val gMonthsShortEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private val hMonthsBn = listOf("মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শা'বান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ")
private val hMonthsEn = listOf("Muharram", "Safar", "Rabi' al-awwal", "Rabi' al-thani", "Jumada al-awwal", "Jumada al-thani", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah")
private val hMonthsAr = listOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")

private val wdShortBn = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহ", "শুক্র", "শনি")
private val wdShortEn = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
private val wdShortAr = listOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
private val wdFullBn = listOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")
private val wdFullEn = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
private val wdFullAr = listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")

fun gMonthName(m0: Int): String {
    val i = m0.coerceIn(0, 11)
    return l3(gMonthsBn[i], gMonthsEn[i], gMonthsAr[i])
}

fun gMonthShort(m0: Int): String {
    val i = m0.coerceIn(0, 11)
    return l3(gMonthsShortBn[i], gMonthsShortEn[i], gMonthsAr[i])
}

fun hMonthName(m1: Int): String {
    val i = (m1 - 1).coerceIn(0, 11)
    return l3(hMonthsBn[i], hMonthsEn[i], hMonthsAr[i])
}

// i: 0 = রবিবার ... 6 = শনিবার
fun weekdayShort(i: Int): String {
    val k = i.coerceIn(0, 6)
    return l3(wdShortBn[k], wdShortEn[k], wdShortAr[k])
}

fun weekdayFull(i: Int): String {
    val k = i.coerceIn(0, 6)
    return l3(wdFullBn[k], wdFullEn[k], wdFullAr[k])
}

// ---------- হিজরি তারিখ (উম্মুল কুরা, সৌদি সরকারি ক্যালেন্ডার) ----------
// ফেরত: (বছর, মাস ১-১২, দিন)। কোনো কারণে হিসাব না হলে null।
fun hijriOf(millis: Long): Triple<Int, Int, Int>? = try {
    val c = android.icu.util.IslamicCalendar()
    c.calculationType = android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
    c.timeZone = android.icu.util.TimeZone.getTimeZone("Asia/Riyadh")
    c.timeInMillis = millis
    Triple(
        c.get(android.icu.util.Calendar.YEAR),
        c.get(android.icu.util.Calendar.MONTH) + 1,
        c.get(android.icu.util.Calendar.DAY_OF_MONTH)
    )
} catch (e: Throwable) {
    null
}

// হিজরি বছরের ১ রমজানের দুপুর ১২টা (সৌদি সময়) মিলিসেকেন্ডে
fun ramadanStartMillis(hYear: Int): Long? = try {
    val c = android.icu.util.IslamicCalendar()
    c.calculationType = android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
    c.timeZone = android.icu.util.TimeZone.getTimeZone("Asia/Riyadh")
    c.set(android.icu.util.Calendar.YEAR, hYear)
    c.set(android.icu.util.Calendar.MONTH, 8)
    c.set(android.icu.util.Calendar.DAY_OF_MONTH, 1)
    c.set(android.icu.util.Calendar.HOUR_OF_DAY, 12)
    c.set(android.icu.util.Calendar.MINUTE, 0)
    c.set(android.icu.util.Calendar.SECOND, 0)
    c.set(android.icu.util.Calendar.MILLISECOND, 0)
    c.timeInMillis
} catch (e: Throwable) {
    null
}

private fun riyadhCal(millis: Long): Calendar =
    Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh")).apply { timeInMillis = millis }

private fun riyadhNoon(y: Int, m0: Int, d: Int): Long =
    Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh")).apply {
        clear()
        set(y, m0, d, 12, 0, 0)
    }.timeInMillis

private fun hijriRangeText(a: Triple<Int, Int, Int>?, b: Triple<Int, Int, Int>?): String {
    if (a == null || b == null) return ""
    return if (a.first == b.first && a.second == b.second) {
        hMonthName(a.second) + " " + bn(a.first)
    } else if (a.first == b.first) {
        hMonthName(a.second) + " – " + hMonthName(b.second) + " " + bn(a.first)
    } else {
        hMonthName(a.second) + " " + bn(a.first) + " – " + hMonthName(b.second) + " " + bn(b.first)
    }
}

// ---------- ইফতারের সময়সূচি ----------
class IftarDay(val number: Int, val m0: Int, val d: Int, val isToday: Boolean, val times: PrayerTimes)

@Composable
fun IftarDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var cityIndex by remember { mutableStateOf(loadCityIndex(context)) }
    var menuOpen by remember { mutableStateOf(false) }
    var bdTime by remember { mutableStateOf(false) }

    val now = remember { System.currentTimeMillis() }
    val city = saudiCities[cityIndex]
    val hijri = remember { hijriOf(now) }
    val hy = hijri?.first ?: 0
    val hm = hijri?.second ?: 0
    val hd = hijri?.third ?: 0
    val inRamadan = hm == 9
    val hYear = if (hm > 9) hy + 1 else hy
    val startMs: Long? = remember(hYear) { if (hYear > 0) ramadanStartMillis(hYear) else null }

    val days: List<IftarDay> = remember(startMs, cityIndex) {
        val list = mutableListOf<IftarDay>()
        val s = startMs
        if (s != null) {
            val tc = riyadhCal(now)
            val ty = tc.get(Calendar.YEAR)
            val tm0 = tc.get(Calendar.MONTH)
            val td = tc.get(Calendar.DAY_OF_MONTH)
            for (i in 0 until 30) {
                val ms = s + i * DAY_MS
                val h = hijriOf(ms)
                if (i > 0 && (h == null || h.second != 9)) break
                val c = riyadhCal(ms)
                val y = c.get(Calendar.YEAR)
                val m0 = c.get(Calendar.MONTH)
                val d = c.get(Calendar.DAY_OF_MONTH)
                val t = computePrayerTimes(y, m0 + 1, d, city.lat, city.lng)
                list.add(IftarDay(i + 1, m0, d, y == ty && m0 == tm0 && d == td, t))
            }
        }
        list
    }

    val today = days.firstOrNull { it.isToday }
    val off = if (bdTime) 3.0 else 0.0
    val modeName = if (bdTime) tr("বাংলাদেশের সময়") else tr("সৌদির সময়")
    val nowCal = riyadhCal(now)
    val nowH = nowCal.get(Calendar.HOUR_OF_DAY) + nowCal.get(Calendar.MINUTE) / 60.0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("বন্ধ করুন"), color = accent, fontWeight = FontWeight.Bold) } },
        title = { Text(tr("🌙  ইফতারের সময়সূচি"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    OutlinedButton(onClick = { menuOpen = true }) { Text("📍 ${tr(city.bn)}  ▾") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        saudiCities.forEachIndexed { index, c ->
                            DropdownMenuItem(text = { Text(tr(c.bn)) }, onClick = {
                                cityIndex = index
                                saveCityIndex(context, index)
                                menuOpen = false
                            })
                        }
                    }
                }
                OutlinedButton(onClick = { bdTime = !bdTime }, modifier = Modifier.fillMaxWidth()) {
                    ETxt("🕒 " + tr("এখন দেখাচ্ছে") + ": " + modeName + "  ⇄")
                }

                if (inRamadan) {
                    Text(
                        l3("রমজান ${bn(hYear)} — আজ ${bn(hd)} তম রোজা", "Ramadan $hYear — day $hd today", "رمضان ${bn(hYear)} — اليوم الصيام رقم ${bn(hd)}"),
                        fontWeight = FontWeight.Bold, color = accent, fontSize = 14.sp
                    )
                    if (today != null) {
                        val leftMin = ((today.times.maghrib - nowH) * 60).roundToInt()
                        Text(tr("সেহরি শেষ") + ": " + fmtTime(today.times.fajr + off), fontWeight = FontWeight.Medium)
                        Text(tr("ইফতার") + ": " + fmtTime(today.times.maghrib + off), fontWeight = FontWeight.Medium)
                        Text(
                            if (leftMin > 0) tr("ইফতার") + " " + remainingText(leftMin)
                            else l3("আজকের ইফতারের সময় হয়ে গেছে", "Today's iftar time has passed", "انتهى وقت إفطار اليوم"),
                            fontSize = 13.sp, color = accent
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionButton(tr("⏰ ইফতার অ্যালার্ম"), accent, Modifier.weight(1f)) { setAlarmFor(context, today.times.maghrib, tr("ইফতার")) }
                            ActionButton(tr("⏰ সেহরি অ্যালার্ম"), accent, Modifier.weight(1f)) { setAlarmFor(context, today.times.fajr, tr("সেহরি শেষ")) }
                        }
                    }
                } else if (startMs != null) {
                    val tc = riyadhCal(now)
                    val todayNoon = riyadhNoon(tc.get(Calendar.YEAR), tc.get(Calendar.MONTH), tc.get(Calendar.DAY_OF_MONTH))
                    val toStart = Math.round((startMs - todayNoon) / 86400000.0).toInt().coerceAtLeast(0)
                    Text(
                        l3(
                            "রমজান ${bn(hYear)} শুরু হতে আর ${bn(toStart)} দিন বাকি (আনুমানিক)",
                            "Ramadan $hYear starts in $toStart days (approx.)",
                            "يبدأ رمضان ${bn(hYear)} بعد ${bn(toStart)} يوم (تقريباً)"
                        ),
                        fontWeight = FontWeight.Bold, color = accent, fontSize = 14.sp
                    )
                }

                if (days.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(tr("রোজা"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedColor, modifier = Modifier.weight(0.7f))
                        Text(tr("তারিখ"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedColor, modifier = Modifier.weight(1.3f))
                        Text(tr("সেহরি শেষ"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedColor, modifier = Modifier.weight(1.4f))
                        Text(tr("ইফতার"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedColor, modifier = Modifier.weight(1.4f))
                    }
                    days.forEach { day ->
                        val rowBg = if (day.isToday) accent.copy(alpha = 0.14f) else Color.Transparent
                        Row(
                            modifier = Modifier.fillMaxWidth().background(rowBg, RoundedCornerShape(8.dp)).padding(vertical = 5.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bn(day.number), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                            Text(bn(day.d) + " " + gMonthShort(day.m0), fontSize = 11.sp, modifier = Modifier.weight(1.3f))
                            Text(fmtTime(day.times.fajr + off), fontSize = 11.sp, modifier = Modifier.weight(1.4f))
                            Text(fmtTime(day.times.maghrib + off), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.4f))
                        }
                    }
                }
                Text(
                    tr("সেহরির শেষ সময় ফজরের শুরুর সাথে মিলিয়ে ধরা হয়েছে। সময়গুলো হিসাব করে বের করা (উম্মুল কুরা পদ্ধতি) এবং রমজানের শুরু চাঁদ দেখার ওপর একদিন আগে-পরে হতে পারে। মসজিদের সময়ের সাথে মিলিয়ে নিন।"),
                    fontSize = 11.sp, color = MutedColor
                )
            }
        }
    )
}

// ---------- ক্যালেন্ডার (ইংরেজি + হিজরি) ----------
fun openPhoneCalendar(context: Context) {
    try {
        val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
        ContentUris.appendId(builder, System.currentTimeMillis())
        context.startActivity(Intent(Intent.ACTION_VIEW).setData(builder.build()))
    } catch (e: Exception) {
        Toast.makeText(context, tr("ক্যালেন্ডার অ্যাপ পাওয়া যায়নি"), Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CalendarDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val todayCal = remember { Calendar.getInstance() }
    val ty = todayCal.get(Calendar.YEAR)
    val tm0 = todayCal.get(Calendar.MONTH)
    val td = todayCal.get(Calendar.DAY_OF_MONTH)
    var year by remember { mutableStateOf(ty) }
    var month0 by remember { mutableStateOf(tm0) }

    val first = Calendar.getInstance().apply {
        clear()
        set(year, month0, 1, 12, 0, 0)
    }
    val firstDow = first.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
    val hijriDays: List<Triple<Int, Int, Int>?> = remember(year, month0) {
        (1..daysInMonth).map { d -> hijriOf(riyadhNoon(year, month0, d)) }
    }
    val rows = (firstDow + daysInMonth + 6) / 7
    val rtl = appLang == AppLang.AR
    val prevArrow = if (rtl) "▶" else "◀"
    val nextArrow = if (rtl) "◀" else "▶"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("বন্ধ করুন"), color = accent, fontWeight = FontWeight.Bold) } },
        title = { Text(tr("📅  ক্যালেন্ডার"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        if (month0 == 0) { month0 = 11; year -= 1 } else month0 -= 1
                    }) { Text(prevArrow, color = accent, fontSize = 18.sp) }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(gMonthName(month0) + " " + bn(year), fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                        Text(hijriRangeText(hijriDays.firstOrNull(), hijriDays.lastOrNull()), fontSize = 11.sp, color = MutedColor, textAlign = TextAlign.Center)
                    }
                    TextButton(onClick = {
                        if (month0 == 11) { month0 = 0; year += 1 } else month0 += 1
                    }) { Text(nextArrow, color = accent, fontSize = 18.sp) }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        Text(
                            weekdayShort(c),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (c == 5) Color(0xFFC62828) else MutedColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0 until 7) {
                            val dayNum = r * 7 + c - firstDow + 1
                            if (dayNum < 1 || dayNum > daysInMonth) {
                                Spacer(modifier = Modifier.weight(1f).height(40.dp))
                            } else {
                                val isToday = dayNum == td && month0 == tm0 && year == ty
                                val hijriDay = hijriDays.getOrNull(dayNum - 1)?.third
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .background(if (isToday) accent else Color.Transparent, RoundedCornerShape(10.dp))
                                ) {
                                    Text(
                                        bn(dayNum),
                                        fontSize = 14.sp,
                                        fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isToday) Color.White else if (c == 5) Color(0xFFC62828) else Color(0xFF1C2733)
                                    )
                                    Text(
                                        if (hijriDay != null) bn(hijriDay) else "",
                                        fontSize = 9.sp,
                                        color = if (isToday) Color.White else MutedColor
                                    )
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    OutlinedButton(onClick = { year = ty; month0 = tm0 }, modifier = Modifier.weight(1f)) { Text(tr("আজ")) }
                }
                Button(
                    onClick = { openPhoneCalendar(context) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(tr("📲 ফোনের ক্যালেন্ডার খুলুন"), color = Color.White, fontWeight = FontWeight.Bold) }
                Text(
                    tr("ইংরেজি তারিখের নিচে ছোট সংখ্যাটি হিজরি তারিখ (উম্মুল কুরা)। চাঁদ দেখার ওপর হিজরি তারিখ একদিন আগে-পরে হতে পারে।"),
                    fontSize = 11.sp, color = MutedColor
                )
            }
        }
    )
}

// ---------- বয়স ক্যালকুলেটর ----------
fun parseDate(s: String): Triple<Int, Int, Int>? = try {
    val p = s.split("-").map { it.toInt() }
    Triple(p[0], p[1], p[2])
} catch (e: Exception) {
    null
}

// ফেরত: (বছর, মাস, দিন)
fun ageBetween(by: Int, bm: Int, bd: Int, ty: Int, tm: Int, td: Int): Triple<Int, Int, Int> {
    var y = ty - by
    var m = tm - bm
    var d = td - bd
    if (d < 0) {
        val prev = Calendar.getInstance().apply {
            clear()
            set(ty, tm - 2, 1)
        }
        d += prev.getActualMaximum(Calendar.DAY_OF_MONTH)
        m -= 1
    }
    if (m < 0) {
        m += 12
        y -= 1
    }
    return Triple(y, m, d)
}

@Composable
fun AgeCalculatorDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE) }
    var birth by remember { mutableStateOf(prefs.getString(KEY_BIRTH, "") ?: "") }

    fun pickDate() {
        val now = Calendar.getInstance()
        val p = parseDate(birth)
        val iy = if (p != null) p.first else now.get(Calendar.YEAR) - 25
        val im = if (p != null) p.second - 1 else now.get(Calendar.MONTH)
        val id = if (p != null) p.third else now.get(Calendar.DAY_OF_MONTH)
        val dlg = DatePickerDialog(context, { _, y, m, d ->
            val s = "%04d-%02d-%02d".format(Locale.US, y, m + 1, d)
            birth = s
            prefs.edit().putString(KEY_BIRTH, s).apply()
        }, iy, im, id)
        dlg.datePicker.maxDate = System.currentTimeMillis()
        dlg.show()
    }

    val b = parseDate(birth)
    val today = Calendar.getInstance()
    val ty = today.get(Calendar.YEAR)
    val tm = today.get(Calendar.MONTH) + 1
    val td = today.get(Calendar.DAY_OF_MONTH)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("বন্ধ করুন"), color = accent, fontWeight = FontWeight.Bold) } },
        title = { Text(tr("🎂  বয়স ক্যালকুলেটর"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tr("আপনার জন্মতারিখ বেছে নিন, আজকের হিসাবে বয়স দেখাবে।"), fontSize = 13.sp)
                Button(
                    onClick = { pickDate() },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(tr("📅 জন্মতারিখ বেছে নিন"), color = Color.White, fontWeight = FontWeight.Bold) }

                if (b != null) {
                    val age = ageBetween(b.first, b.second, b.third, ty, tm, td)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(age.first to tr("বছর"), age.second to tr("মাস"), age.third to tr("দিন")).forEach { (value, label) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(bn(value), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = accent)
                                Text(label, fontSize = 12.sp, color = MutedColor)
                            }
                        }
                    }

                    val birthCal = Calendar.getInstance().apply {
                        clear()
                        set(b.first, b.second - 1, b.third, 0, 0, 0)
                    }
                    val todayMid = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val totalDays = Math.round((todayMid.timeInMillis - birthCal.timeInMillis) / 86400000.0).toInt()
                    val next = Calendar.getInstance().apply {
                        clear()
                        set(ty, b.second - 1, b.third, 0, 0, 0)
                    }
                    if (next.timeInMillis < todayMid.timeInMillis) next.add(Calendar.YEAR, 1)
                    val daysToNext = Math.round((next.timeInMillis - todayMid.timeInMillis) / 86400000.0).toInt()

                    Text(tr("জন্মতারিখ") + ": " + bn(b.third) + " " + gMonthName(b.second - 1) + " " + bn(b.first), fontSize = 14.sp)
                    Text(tr("জন্মবার") + ": " + weekdayFull(birthCal.get(Calendar.DAY_OF_WEEK) - 1), fontSize = 14.sp)
                    Text(tr("আজ পর্যন্ত মোট দিন") + ": " + bn(totalDays), fontSize = 14.sp)
                    if (daysToNext == 0) {
                        ETxt(tr("🎉 আজ আপনার জন্মদিন! শুভ জন্মদিন!"), fontWeight = FontWeight.Bold, color = accent, fontSize = 14.sp)
                    } else {
                        Text(
                            tr("পরের জন্মদিন") + ": " + l3("আর ${bn(daysToNext)} দিন", "in $daysToNext days", "بعد ${bn(daysToNext)} يوم"),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    )
}

// ---------- ভাষা সেটিংস (অ্যাপের ভাষা) ----------
@Composable
fun LanguageDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("বন্ধ করুন"), color = accent, fontWeight = FontWeight.Bold) } },
        title = { Text(tr("🌐  ভাষা সেটিংস"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tr("অ্যাপের ভাষা বেছে নিন। বেছে নেওয়া ভাষা মনে থাকবে।"), fontSize = 13.sp)
                AppLang.values().forEach { lang ->
                    if (appLang == lang) {
                        Button(
                            onClick = { saveLang(context, lang) },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("✓  " + lang.nativeName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    } else {
                        OutlinedButton(
                            onClick = { saveLang(context, lang) },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(lang.nativeName, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    }
                }
                Text(tr("আরবি বেছে নিলে পুরো অ্যাপ ডান থেকে বামে চলবে।"), fontSize = 11.sp, color = MutedColor)
            }
        }
    )
}
