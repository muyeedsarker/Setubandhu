package com.setubandhu.app

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp

// ফোন নম্বরের দেশের কোড। নম্বর + বা 00 দিয়ে শুরু হলে সেটাই থাকে; না হলে শুরুর 0 বাদ দিয়ে বেছে নেওয়া কোড বসে।
data class Country(val flag: String, val bn: String, val en: String, val ar: String, val code: String) {
    fun name() = l3(bn, en, ar)
}

val countries = listOf(
    Country("🇸🇦", "সৌদি আরব", "Saudi Arabia", "السعودية", "+966"),
    Country("🇧🇩", "বাংলাদেশ", "Bangladesh", "بنغلاديش", "+880"),
    Country("🇦🇪", "সংযুক্ত আরব আমিরাত", "UAE", "الإمارات", "+971"),
    Country("🇰🇼", "কুয়েত", "Kuwait", "الكويت", "+965"),
    Country("🇶🇦", "কাতার", "Qatar", "قطر", "+974"),
    Country("🇴🇲", "ওমান", "Oman", "عُمان", "+968"),
    Country("🇧🇭", "বাহরাইন", "Bahrain", "البحرين", "+973"),
    Country("🇮🇳", "ভারত", "India", "الهند", "+91"),
    Country("🇵🇰", "পাকিস্তান", "Pakistan", "باكستان", "+92"),
    Country("🇳🇵", "নেপাল", "Nepal", "نيبال", "+977"),
    Country("🇱🇰", "শ্রীলঙ্কা", "Sri Lanka", "سريلانكا", "+94"),
    Country("🇲🇾", "মালয়েশিয়া", "Malaysia", "ماليزيا", "+60"),
    Country("🇪🇬", "মিশর", "Egypt", "مصر", "+20")
)

private const val CC_PREFS = "setubandhu_cc"

fun loadLastCode(c: Context): String = c.getSharedPreferences(CC_PREFS, Context.MODE_PRIVATE).getString("last", "+880") ?: "+880"
fun saveLastCode(c: Context, code: String) { c.getSharedPreferences(CC_PREFS, Context.MODE_PRIVATE).edit().putString("last", code).apply() }

fun applyCountryCode(code: String, raw: String): String {
    val t = raw.trim().filter { it.isDigit() || it == '+' }
    if (t.isBlank()) return ""
    if (t.startsWith("+")) return t
    if (t.startsWith("00")) return "+" + t.drop(2)
    val cc = code.removePrefix("+")
    if (t.startsWith(cc) && t.length > cc.length + 6) return "+$t" // কোডসহ লেখা (যেমন 8801…)
    return code + t.trimStart('0')
}

@Composable
fun CountryCodePicker(selected: String, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val cur = countries.find { it.code == selected }
    Box {
        OutlinedButton(onClick = { open = true }) { Text("${cur?.flag ?: "🌐"} $selected ▾", fontSize = 14.sp) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            countries.forEach { c ->
                DropdownMenuItem(text = { Text("${c.flag} ${c.name()}  ${c.code}") }, onClick = { onSelect(c.code); open = false })
            }
        }
    }
}
