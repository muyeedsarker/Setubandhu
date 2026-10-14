package com.setubandhu.app

import android.Manifest
import android.app.DatePickerDialog
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

// ---------- রঙের তালিকা (প্রফেশনাল নেভি-টিল থিম) ----------
val BrandPrimary = Color(0xFF1E3A5F)
val BrandDark = Color(0xFF142B47)
val BrandTeal = Color(0xFF0E8F83)
val SurfaceBg = Color(0xFFF3F5F9)
val CardBorder = Color(0xFFE2E7EF)
val TextPrimary = Color(0xFF1C2733)
val TextMuted = Color(0xFF5B6777)

const val ACTION_SHARE_LOCATION = "app.share_location"
const val ACTION_PRAYER = "app.prayer_times"
const val ACTION_ADHAN = "app.adhan"
const val ACTION_REMINDERS = "app.reminders"
const val ACTION_EMERGENCY = "app.emergency"
const val ACTION_OWNER = "app.owner"
const val ACTION_LIVE_LOCATION = "app.live_location"
const val ACTION_ALBUM = "app.album"
const val ACTION_IFTAR = "app.iftar"
const val ACTION_CALENDAR = "app.calendar"
const val ACTION_AGE = "app.age"
const val ACTION_LANGUAGE = "app.language"
const val ACTION_IMAGE_EDIT = "app.image_edit"
const val ACTION_ADVICE = "app.advice"
const val ACTION_ADVICE_TOPIC = "app.advice_topic."

// ৩ডি আইকনের ছবি: app/src/main/res/drawable-nodpi/ic_*.png। ছবি না থাকলে ইমোজি দেখানো হবে (যেমন স্ক্রিনশট)।
val iconNames: Map<String, String> = mapOf(
    "ওয়াইফাই আইপি" to "ic_wifi_ip",
    "রিডিং মোড" to "ic_reading_mode",
    "অ্যাপ তথ্য" to "ic_app_info",
    "অ্যাপ আনইনস্টল" to "ic_app_uninstall",
    "বাবার উপদেশ" to "ic_father_advice",
    "মালিকের সাথে আচরণ" to "ic_behave_owner",
    "গাড়ি চালানোর কানুন" to "ic_driving_rules",
    "গাড়ির রক্ষণাবেক্ষণ" to "ic_car_care",
    "কেমন স্ত্রী হবে" to "ic_choose_wife",
    "কেমন লোকের সাথে চলবে" to "ic_good_company",
    "কফিলের পরিবারের সাথে ব্যবহার" to "ic_kafeel_family",
    "বাবার সাথে আচরণ" to "ic_behave_father",
    "মায়ের সাথে আচরণ" to "ic_behave_mother",
    "ছোট ভাইয়ের সাথে আচরণ" to "ic_behave_brother",
    "লাইভ লোকেশন" to "ic_live_location",
    "লোকেশন পাঠান" to "ic_share_location",
    "নামাজের সময়" to "ic_prayer",
    "আযান" to "ic_adhan",
    "রিমাইন্ডার" to "ic_reminder",
    "জরুরি নম্বর" to "ic_emergency_numbers",
    "মালিক" to "ic_owner",
    "ওয়াইফাই" to "ic_wifi",
    "ব্লুটুথ" to "ic_bluetooth",
    "ফ্লাইট মোড" to "ic_airplane",
    "মোবাইল নেটওয়ার্ক" to "ic_mobile_network",
    "ভিপিএন" to "ic_vpn",
    "এপিএন" to "ic_apn",
    "নেটওয়ার্ক অপারেটর" to "ic_operator",
    "রোমিং" to "ic_roaming",
    "এনএফসি" to "ic_nfc",
    "হটস্পট ও টিথারিং" to "ic_hotspot",
    "সিম কার্ড" to "ic_sim",
    "ডিসপ্লে" to "ic_display",
    "সাউন্ড" to "ic_sound",
    "উজ্জ্বলতা" to "ic_brightness",
    "স্ক্রিন টাইমআউট" to "ic_screen_timeout",
    "স্ক্রিন সেভার" to "ic_screensaver",
    "ক্যাপশন" to "ic_caption",
    "হোম অ্যাপ" to "ic_home_app",
    "ডিফল্ট অ্যাপস" to "ic_default_apps",
    "অ্যাপ ম্যানেজ" to "ic_app_manage",
    "অ্যাপ পারমিশন" to "ic_app_permission",
    "স্টোরেজ" to "ic_storage",
    "ইউসেজ অ্যাক্সেস" to "ic_usage",
    "প্রিন্ট" to "ic_print",
    "কীবোর্ড" to "ic_keyboard",
    "সাধারণ সেটিংস" to "ic_settings",
    "তারিখ ও সময়" to "ic_date_time",
    "ফোনের ভাষা" to "ic_language",
    "অ্যালবাম" to "ic_screensaver",
    "সিঙ্ক" to "ic_sync",
    "ডিভাইস তথ্য" to "ic_device_info",
    "ডেভেলপার অপশন" to "ic_developer",
    "সিস্টেম সেটিংস পরিবর্তন" to "ic_write_settings",
    "অ্যাকাউন্ট যোগ" to "ic_add_account",
    "সিকিউরিটি" to "ic_security",
    "প্রাইভেসি" to "ic_privacy",
    "ফিঙ্গারপ্রিন্ট/স্ক্রিন লক" to "ic_fingerprint",
    "অ্যাক্সেসিবিলিটি" to "ic_accessibility",
    "স্ক্রিন ওভারলে" to "ic_overlay",
    "নোটিফিকেশন অ্যাক্সেস" to "ic_notif_access",
    "নোটিফিকেশন" to "ic_notification",
    "ডু নট ডিস্টার্ব" to "ic_dnd",
    "ইমার্জেন্সি অ্যালার্ট" to "ic_emergency_alert",
    "লোকেশন" to "ic_location",
    "ব্যাটারি সেভার" to "ic_battery_saver",
    "ফ্যাক্টরি রিসেট" to "ic_factory_reset",
    "পাওয়ার অফ" to "ic_power_off",
    "রিস্টার্ট" to "ic_restart",
    "ছবি এডিট" to "ic_image_edit",
    "ইফতারের সময়সূচি" to "ic_iftar",
    "ক্যালেন্ডার" to "ic_calendar",
    "বয়স ক্যালকুলেটর" to "ic_age",
    "ভাষা সেটিংস" to "ic_app_language",
    "স্ক্রিনশট" to "ic_screenshot"
)

data class SettingItem(
    val label: String,
    val emoji: String,
    val action: String? = null,
    val helpText: String? = null,
    val keywords: String = ""
)

data class Category(
    val title: String,
    val emoji: String,
    val accent: Color,
    val items: List<SettingItem>
)

val categories = listOf(
    Category("উপদেশ", "📜", Color(0xFF14664B), listOf(
        SettingItem("বাবার উপদেশ", "📜", ACTION_ADVICE, keywords = "father advice upadesh wasiyat baba bapa nasihat বাবা বাবার উপদেশ নসিহত ওসিয়ত وصية"),
        SettingItem("মালিকের সাথে আচরণ", "🤝", ACTION_ADVICE_TOPIC + "0", keywords = "owner boss employer kafeel sponsor behaviour মালিক কফিল আচরণ"),
        SettingItem("গাড়ি চালানোর কানুন", "🚗", ACTION_ADVICE_TOPIC + "1", keywords = "driving rules traffic car গাড়ি চালানো কানুন নিয়ম"),
        SettingItem("গাড়ির রক্ষণাবেক্ষণ", "🔧", ACTION_ADVICE_TOPIC + "2", keywords = "car maintenance service repair গাড়ি রক্ষণাবেক্ষণ সার্ভিস"),
        SettingItem("মায়ের সাথে আচরণ", "👩", ACTION_ADVICE_TOPIC + "3", keywords = "mother mom maa behaviour মা মায়ের আচরণ"),
        SettingItem("ছোট ভাইয়ের সাথে আচরণ", "👦", ACTION_ADVICE_TOPIC + "4", keywords = "younger brother behaviour ভাই ছোট ভাই আচরণ"),
        SettingItem("বাবার সাথে আচরণ", "👴", ACTION_ADVICE_TOPIC + "5", keywords = "father baba behaviour বাবা আচরণ"),
        SettingItem("কেমন স্ত্রী হবে", "💍", ACTION_ADVICE_TOPIC + "6", keywords = "wife marriage bride স্ত্রী বিয়ে পাত্রী"),
        SettingItem("কেমন লোকের সাথে চলবে", "👥", ACTION_ADVICE_TOPIC + "7", keywords = "friends company people বন্ধু সঙ্গ লোক"),
        SettingItem("কফিলের পরিবারের সাথে ব্যবহার", "🏠", ACTION_ADVICE_TOPIC + "8", keywords = "kafeel family behaviour কফিল পরিবার ব্যবহার"),
    )),
    Category("ড্রাইভার ও নামাজ", "🚗", Color(0xFF2E7D32), listOf(
        SettingItem("লাইভ লোকেশন", "🚗", ACTION_LIVE_LOCATION, keywords = "live location gps car map speed লাইভ লোকেশন গাড়ি"),
        SettingItem("অ্যালবাম", "🖼️", ACTION_ALBUM, keywords = "album photo gallery picture ছবি অ্যালবাম"),
        SettingItem("ছবি এডিট", "✨", ACTION_IMAGE_EDIT, keywords = "photo edit image enhance clear blur sharpen passport clothes ছবি এডিট ক্লিয়ার পোশাক تحرير صورة"),
        SettingItem("লোকেশন পাঠান", "📤", ACTION_SHARE_LOCATION, keywords = "location share send forward gps map লোকেশন ফরওয়ার্ড শেয়ার"),
        SettingItem("নামাজের সময়", "🕌", ACTION_PRAYER, keywords = "prayer namaz salat time নামাজ সময় ওয়াক্ত"),
        SettingItem("আযান", "🔊", ACTION_ADHAN, keywords = "azan adhan আজান আযান"),
        SettingItem("রিমাইন্ডার", "⏰", ACTION_REMINDERS, keywords = "reminder iqama license insurance istimara ইকামা লাইসেন্স ইনসুরেন্স মেয়াদ"),
        SettingItem("জরুরি নম্বর", "🚨", ACTION_EMERGENCY, keywords = "emergency police ambulance traffic 999 997 998 993 জরুরি পুলিশ"),
        SettingItem("মালিক", "👔", ACTION_OWNER, keywords = "owner boss employer kafeel sponsor মালিক কফিল বস"),
        SettingItem("ইফতারের সময়সূচি", "🌙", ACTION_IFTAR, keywords = "iftar sehri suhoor ramadan ramzan roza fasting ইফতার সেহরি রমজান রোজা إفطار رمضان"),
        SettingItem("ক্যালেন্ডার", "📅", ACTION_CALENDAR, keywords = "calendar date hijri month ক্যালেন্ডার তারিখ হিজরি تقويم"),
        SettingItem("বয়স ক্যালকুলেটর", "🎂", ACTION_AGE, keywords = "age calculator birthday birth date বয়স জন্মতারিখ জন্মদিন عمر"),
        SettingItem("ভাষা সেটিংস", "🌐", ACTION_LANGUAGE, keywords = "app language arabic english bangla bengali ভাষা আরবি ইংরেজি বাংলা لغة عربي")
    )),
    Category("নেটওয়ার্ক ও সংযোগ", "🌐", Color(0xFF2979FF), listOf(
        SettingItem("ওয়াইফাই", "📶", Settings.ACTION_WIFI_SETTINGS, keywords = "wifi wi-fi internet"),
        SettingItem("ব্লুটুথ", "🔵", Settings.ACTION_BLUETOOTH_SETTINGS, keywords = "bluetooth"),
        SettingItem("ফ্লাইট মোড", "✈️", Settings.ACTION_AIRPLANE_MODE_SETTINGS, keywords = "airplane flight"),
        SettingItem("মোবাইল নেটওয়ার্ক", "🌐", Settings.ACTION_WIRELESS_SETTINGS, keywords = "mobile data network sim"),
        SettingItem("ভিপিএন", "🌍", Settings.ACTION_VPN_SETTINGS, keywords = "vpn"),
        SettingItem("এপিএন", "📡", "android.settings.APN_SETTINGS", keywords = "apn internet"),
        SettingItem("ওয়াইফাই আইপি", "🔍", "android.settings.WIFI_IP_SETTINGS", keywords = "wifi ip address static ওয়াইফাই আইপি ঠিকানা"),
        SettingItem("নেটওয়ার্ক অপারেটর", "🛰️", "android.settings.NETWORK_OPERATOR_SETTINGS", keywords = "operator carrier"),
        SettingItem("রোমিং", "📥", "android.settings.DATA_ROAMING_SETTINGS", keywords = "roaming"),
        SettingItem("এনএফসি", "📲", "android.settings.NFC_SETTINGS", keywords = "nfc"),
        SettingItem("হটস্পট ও টিথারিং", "📤", "android.settings.TETHER_SETTINGS", keywords = "hotspot tethering"),
        SettingItem("সিম কার্ড", "📇", "android.settings.MANAGE_ALL_SIM_PROFILES_SETTINGS", keywords = "sim")
    )),
    Category("ডিসপ্লে ও সাউন্ড", "🎨", Color(0xFF7C4DFF), listOf(
        SettingItem("ডিসপ্লে", "🖥️", Settings.ACTION_DISPLAY_SETTINGS, keywords = "display screen"),
        SettingItem("সাউন্ড", "🔊", Settings.ACTION_SOUND_SETTINGS, keywords = "sound volume ringtone"),
        SettingItem("উজ্জ্বলতা", "🔆", Settings.ACTION_DISPLAY_SETTINGS, keywords = "brightness"),
        SettingItem("স্ক্রিন টাইমআউট", "⏱️", Settings.ACTION_DISPLAY_SETTINGS, keywords = "timeout sleep screen off"),
        SettingItem("স্ক্রিন সেভার", "🖼️", "android.settings.DREAM_SETTINGS", keywords = "screensaver daydream"),
        SettingItem("ক্যাপশন", "💬", Settings.ACTION_CAPTIONING_SETTINGS, keywords = "caption subtitle"),
        SettingItem("রিডিং মোড", "📖", Settings.ACTION_DISPLAY_SETTINGS, keywords = "reading mode night eye comfort রিডিং মোড পড়া"),
        SettingItem("হোম অ্যাপ", "🏠", "android.settings.HOME_SETTINGS", keywords = "home launcher")
    )),
    Category("অ্যাপস ও স্টোরেজ", "📱", Color(0xFFFF9100), listOf(
        SettingItem("ডিফল্ট অ্যাপস", "⚏", "android.settings.MANAGE_DEFAULT_APPS_SETTINGS", keywords = "default apps"),
        SettingItem("অ্যাপ ম্যানেজ", "📱", Settings.ACTION_APPLICATION_SETTINGS, keywords = "app manage uninstall info"),
        SettingItem("অ্যাপ তথ্য", "🤖", "android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS", keywords = "app info details অ্যাপ তথ্য"),
        SettingItem("অ্যাপ আনইনস্টল", "🗑️", "android.settings.MANAGE_APPLICATIONS_SETTINGS", keywords = "uninstall remove delete app অ্যাপ আনইনস্টল মুছুন"),
        SettingItem("অ্যাপ পারমিশন", "🔑", "android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS", keywords = "permission all apps"),
        SettingItem("স্টোরেজ", "💾", "android.settings.INTERNAL_STORAGE_SETTINGS", keywords = "storage memory space"),
        SettingItem("ইউসেজ অ্যাক্সেস", "📊", "android.settings.USAGE_ACCESS_SETTINGS", keywords = "usage access"),
        SettingItem("প্রিন্ট", "🖨️", Settings.ACTION_PRINT_SETTINGS, keywords = "print"),
        SettingItem("কীবোর্ড", "⌨️", Settings.ACTION_INPUT_METHOD_SETTINGS, keywords = "keyboard input")
    )),
    Category("সিস্টেম ও সাধারণ", "⚙️", Color(0xFF00BFA5), listOf(
        SettingItem("সাধারণ সেটিংস", "⚙️", Settings.ACTION_SETTINGS, keywords = "settings general"),
        SettingItem("তারিখ ও সময়", "🕒", Settings.ACTION_DATE_SETTINGS, keywords = "date time clock"),
        SettingItem("ফোনের ভাষা", "🗣️", Settings.ACTION_LOCALE_SETTINGS, keywords = "language locale phone language ভাষা"),
        SettingItem("সিঙ্ক", "🔄", "android.settings.SYNC_SETTINGS", keywords = "sync"),
        SettingItem("ডিভাইস তথ্য", "ℹ️", "android.settings.DEVICE_INFO_SETTINGS", keywords = "about phone device info"),
        SettingItem("ডেভেলপার অপশন", "👨‍💻", "android.settings.APPLICATION_DEVELOPMENT_SETTINGS", keywords = "developer options usb debugging"),
        SettingItem("সিস্টেম সেটিংস পরিবর্তন", "✍️", "android.settings.action.MANAGE_WRITE_SETTINGS", keywords = "write settings"),
        SettingItem("অ্যাকাউন্ট যোগ", "📧", Settings.ACTION_ADD_ACCOUNT, keywords = "account google add")
    )),
    Category("সিকিউরিটি ও প্রাইভেসি", "🔒", Color(0xFFFF5252), listOf(
        SettingItem("সিকিউরিটি", "🔒", Settings.ACTION_SECURITY_SETTINGS, keywords = "security"),
        SettingItem("প্রাইভেসি", "🛡️", "android.settings.PRIVACY_SETTINGS", keywords = "privacy"),
        SettingItem("ফিঙ্গারপ্রিন্ট/স্ক্রিন লক", "🔐", Settings.ACTION_SECURITY_SETTINGS, keywords = "fingerprint screen lock password pin"),
        SettingItem("অ্যাক্সেসিবিলিটি", "🧍", Settings.ACTION_ACCESSIBILITY_SETTINGS, keywords = "accessibility"),
        SettingItem("স্ক্রিন ওভারলে", "🪟", "android.settings.action.MANAGE_OVERLAY_PERMISSION", keywords = "overlay draw over apps"),
        SettingItem("নোটিফিকেশন অ্যাক্সেস", "👂", "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", keywords = "notification access listener")
    )),
    Category("নোটিফিকেশন ও অ্যালার্ট", "🔔", Color(0xFFFF4081), listOf(
        SettingItem("নোটিফিকেশন", "🔔", "android.settings.NOTIFICATION_SETTINGS", keywords = "notification"),
        SettingItem("ডু নট ডিস্টার্ব", "🤚", "android.settings.ZEN_MODE_SETTINGS", keywords = "dnd do not disturb silent"),
        SettingItem("ইমার্জেন্সি অ্যালার্ট", "🚨", "android.settings.CELL_BROADCAST_SETTINGS", keywords = "emergency alert"),
        SettingItem("লোকেশন", "📍", Settings.ACTION_LOCATION_SOURCE_SETTINGS, keywords = "location gps"),
        SettingItem("ব্যাটারি সেভার", "🔋", "android.settings.BATTERY_SAVER_SETTINGS", keywords = "battery saver power")
    )),
    Category("পাওয়ার ও রিসেট", "🔌", Color(0xFF546E7A), listOf(
        SettingItem("ফ্যাক্টরি রিসেট", "♻️", helpText = "নিরাপত্তার কারণে এই কাজটা কোনো সাধারণ অ্যাপ সরাসরি করতে পারে না। নিজে করতে হলে:\n\n১. Settings খুলুন\n২. System (বা General Management) এ যান\n৩. Reset options ট্যাপ করুন\n৪. Factory data reset বেছে নিন\n\n⚠️ এতে ফোনের সব ডেটা মুছে যাবে, তাই আগে ব্যাকআপ নিন।", keywords = "factory reset erase"),
        SettingItem("পাওয়ার অফ", "🔴", helpText = "ফোনের ডান পাশের Power বাটন ৩-৫ সেকেন্ড চেপে ধরুন। একটা মেনু আসবে, সেখানে \"Power off\" লেখায় ট্যাপ করুন।", keywords = "power off shutdown"),
        SettingItem("রিস্টার্ট", "🔁", helpText = "Power বাটন ৩-৫ সেকেন্ড চেপে ধরুন। মেনু থেকে \"Restart\" বা \"পুনরায় চালু করুন\" অপশনে ট্যাপ করুন।", keywords = "restart reboot"),
        SettingItem("স্ক্রিনশট", "📸", helpText = "Power বাটন আর Volume Down (নিচের ভলিউম) বাটন একসাথে ২-৩ সেকেন্ড চেপে ধরুন। স্ক্রিনশট Gallery/Photos অ্যাপে সেভ হয়ে যাবে।", keywords = "screenshot capture")
    ))
)

// প্রিয় সেটিংসের জন্য আলাদা পেজ (সবসময় প্রথম পেজ)
val favCategory = Category("প্রিয় সেটিংস", "⭐", Color(0xFFFFB300), emptyList())
val pageCount = categories.size + 1

fun pageCategory(index: Int): Category = if (index == 0) favCategory else categories[index - 1]

val allItems: List<SettingItem> = categories.flatMap { it.items }
val itemAccent: Map<String, Color> = categories.flatMap { c -> c.items.map { it.label to c.accent } }.toMap()

fun matchesQuery(item: SettingItem, query: String): Boolean {
    val q = query.trim()
    if (q.isEmpty()) return true
    return item.label.contains(q, ignoreCase = true) || tr(item.label).contains(q, ignoreCase = true) || item.keywords.contains(q, ignoreCase = true)
}

private const val PREFS_NAME = "setubandhu"
private const val KEY_FAVS = "favs"

fun loadFavs(context: Context): List<String> =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_FAVS, "")
        ?.split("\n")
        ?.filter { it.isNotBlank() }
        ?.map { if (it == "ভাষা") "ফোনের ভাষা" else it }
        ?: emptyList()

fun saveFavs(context: Context, favs: List<String>) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_FAVS, favs.joinToString("\n"))
        .apply()
}

fun openSetting(context: Context, action: String) {
    try {
        context.startActivity(Intent(action))
    } catch (e: Exception) {
        try {
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        } catch (e2: Exception) {
            Toast.makeText(context, tr("এই সেটিং এই ফোনে পাওয়া যায়নি"), Toast.LENGTH_SHORT).show()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = BrandDark.toArgb()
        window.navigationBarColor = Color.White.toArgb()
        loadLang(this)
        setContent {
            var showSplash by rememberSaveable { mutableStateOf(true) }
            val layoutDir = if (appLang == AppLang.AR) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                MaterialTheme(
                    colorScheme = lightColorScheme(
                        primary = BrandPrimary,
                        onPrimary = Color.White,
                        secondary = BrandTeal,
                        background = SurfaceBg,
                        surface = Color.White,
                        onSurface = TextPrimary,
                        outline = CardBorder
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SetuBondhuApp()
                        AnimatedVisibility(visible = showSplash, exit = fadeOut(tween(500))) {
                            PhotoSplash(onFinished = { showSplash = false })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetuBondhuApp() {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val currentCategory = pageCategory(pagerState.currentPage)

    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val favs = remember { mutableStateListOf<String>().also { it.addAll(loadFavs(context)) } }
    val toggleFav: (SettingItem) -> Unit = { item ->
        if (favs.contains(item.label)) {
            favs.remove(item.label)
            Toast.makeText(context, tr("প্রিয় থেকে সরানো হয়েছে"), Toast.LENGTH_SHORT).show()
        } else {
            favs.add(item.label)
            Toast.makeText(context, tr("প্রিয়তে যোগ হয়েছে ⭐"), Toast.LENGTH_SHORT).show()
        }
        saveFavs(context, favs)
    }

    BackHandler(enabled = searching) {
        searching = false
        query = ""
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(BrandDark, BrandPrimary)))
                    .padding(top = 18.dp, bottom = 14.dp)
            ) {
                if (searching) {
                    SearchBar(query = query, onQueryChange = { query = it }, onClose = { searching = false; query = "" })
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(tr("সেতুবন্ধু"), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Text("🔍", fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp).clickable { searching = true })
                    }
                    Spacer(Modifier.height(6.dp))
                    AnimatedContent(targetState = currentCategory, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "category-title") { cat ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            CategoryBadge(cat.title, cat.accent)
                            Spacer(Modifier.width(8.dp))
                            Text(tr(cat.title), color = Color(0xFFD6E4F5), fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!searching) BottomNavBar(pagerState = pagerState, accent = BrandPrimary, scope = scope)
        },
        containerColor = SurfaceBg
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (searching) {
                val results = allItems.filter { matchesQuery(it, query) }
                ItemGrid(list = results, favs = favs, onToggleFav = toggleFav, emptyHint = tr("কোনো সেটিং পাওয়া যায়নি"))
            } else {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    if (page == 0) {
                        val favItems = favs.mapNotNull { label -> allItems.firstOrNull { it.label == label } }
                        ItemGrid(
                            list = favItems,
                            favs = favs,
                            onToggleFav = toggleFav,
                            emptyHint = tr("কোনো সেটিংয়ে চেপে ধরে রাখলে সেটা এখানে প্রিয় হিসেবে যোগ হবে ⭐")
                        )
                    } else {
                        ItemGrid(list = categories[page - 1].items, favs = favs, onToggleFav = toggleFav)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(tr("সেটিং খুঁজুন (বাংলা বা English)")) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f).focusRequester(focusRequester)
        )
        TextButton(onClick = onClose) { Text(tr("বাতিল"), color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun ItemGrid(
    list: List<SettingItem>,
    favs: List<String>,
    onToggleFav: (SettingItem) -> Unit,
    emptyHint: String? = null
) {
    if (list.isEmpty() && emptyHint != null) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(emptyHint, textAlign = TextAlign.Center, color = Color(0xFF555555), fontSize = 15.sp)
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 78.dp),
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(list, key = { it.label }) { item ->
            SettingCard(
                item = item,
                accent = BrandPrimary,
                isFav = favs.contains(item.label),
                onToggleFav = { onToggleFav(item) }
            )
        }
    }
}

@Composable
fun animateColorLerp(target: Color): State<Color> = androidx.compose.animation.animateColorAsState(targetValue = lerp(target, Color.White, 0.88f), label = "bg-color")

@Composable
fun BottomNavBar(pagerState: androidx.compose.foundation.pager.PagerState, accent: Color, scope: kotlinx.coroutines.CoroutineScope) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 10.dp), horizontalArrangement = Arrangement.Center) {
            for (index in 0 until pageCount) {
                val selected = pagerState.currentPage == index
                val width by animateDpAsState(if (selected) 20.dp else 8.dp, label = "dot-width")
                Box(modifier = Modifier.padding(horizontal = 3.dp).height(8.dp).width(width).clip(RoundedCornerShape(50)).background(if (selected) accent else Color(0xFFD5DBE5)))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            val hasPrev = pagerState.currentPage > 0
            val hasNext = pagerState.currentPage < pageCount - 1
            OutlinedButton(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                enabled = hasPrev,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (hasPrev) accent else CardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accent, disabledContentColor = Color(0xFFB4BDC9)),
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) { Text(tr("← আগের"), fontWeight = FontWeight.SemiBold) }
            Button(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                enabled = hasNext,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White, disabledContainerColor = Color(0xFFE6EAF0), disabledContentColor = Color(0xFF9AA5B4)),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) { Text(tr("পরের →"), fontWeight = FontWeight.SemiBold) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingCard(item: SettingItem, accent: Color, isFav: Boolean, onToggleFav: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showHelp by remember { mutableStateOf(false) }
    var appDialog by remember { mutableStateOf<String?>(null) }
    val pickAdhan: () -> Unit = if (item.action == ACTION_ADHAN) rememberAdhanPicker() else ({ })
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.88f else 1f, animationSpec = spring(dampingRatio = 0.4f), label = "press-scale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (item.helpText != null) showHelp = true
                    else if (item.action == ACTION_ADHAN) { if (AdhanPlayer.hasSource(context)) AdhanPlayer.toggle(context) else pickAdhan() }
                    else if (item.action != null && item.action.startsWith("app.")) appDialog = item.action
                    else if (item.action != null) openSetting(context, item.action)
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleFav()
                }
            )
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        val iconId = remember(item.label) {
            val name = iconNames[item.label]
            if (name == null) 0 else context.resources.getIdentifier(name, "drawable", context.packageName)
        }
        if (iconId != 0) {
            Box(modifier = Modifier.size(66.dp), contentAlignment = Alignment.Center) {
                Image(painter = painterResource(id = iconId), contentDescription = tr(item.label), modifier = Modifier.size(58.dp))
                if (isFav) {
                    Text(text = "⭐", fontSize = 13.sp, modifier = Modifier.align(Alignment.TopEnd))
                }
            }
        } else {
            Box(modifier = Modifier.size(58.dp).clip(CircleShape).background(Brush.radialGradient(listOf(accent.copy(alpha = 0.14f), accent.copy(alpha = 0.06f)))), contentAlignment = Alignment.Center) {
                Text(text = item.emoji, fontSize = 24.sp)
                if (isFav) {
                    Text(text = "⭐", fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = tr(item.label), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = TextPrimary, minLines = 2, maxLines = 2)
    }

    when (appDialog) {
        ACTION_SHARE_LOCATION -> LocationShareDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_PRAYER -> PrayerTimesDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_REMINDERS -> RemindersDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_EMERGENCY -> EmergencyDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_OWNER -> OwnerDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_LIVE_LOCATION -> LiveLocationDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_ALBUM -> AlbumDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_IFTAR -> IftarDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_CALENDAR -> CalendarDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_AGE -> AgeCalculatorDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_LANGUAGE -> LanguageDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_IMAGE_EDIT -> ImageEditDialog(accent = accent, onDismiss = { appDialog = null })
        ACTION_ADVICE -> FatherAdviceDialog(accent = accent, onDismiss = { appDialog = null })
        else -> {
            val code = appDialog
            if (code != null && code.startsWith(ACTION_ADVICE_TOPIC)) {
                val topic = adviceTopics.getOrNull(code.removePrefix(ACTION_ADVICE_TOPIC).toIntOrNull() ?: -1)
                if (topic != null) AdviceTopicDialog(topic = topic, onDismiss = { appDialog = null })
            }
        }
    }

    if (showHelp && item.helpText != null) {
        AlertDialog(onDismissRequest = { showHelp = false }, confirmButton = { TextButton(onClick = { showHelp = false }) { Text(tr("বুঝেছি"), color = accent, fontWeight = FontWeight.Bold) } }, title = { Text("${item.emoji}  ${tr(item.label)}", fontWeight = FontWeight.Bold) }, text = { ETxt(tr(item.helpText), fontSize = 14.sp) })
    }
}


// অ্যাপ খোলার সময় নিজের ছবি দেখানোর স্প্ল্যাশ স্ক্রিন।
// ছবির ফাইল: app/src/main/res/drawable-nodpi/my_photo.png (বা .jpg) — ছবি না থাকলে স্প্ল্যাশ নিজে থেকেই বাদ যাবে।
private const val SPLASH_PHOTO_NAME = "my_photo"
private const val SPLASH_DURATION_MS = 3000L
private const val SPLASH_NAME = "মমিনুল ইসলাম (সিয়াম)"

fun decodePhoto(context: Context, resId: Int, maxSide: Int): ImageBitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeResource(context.resources, resId, bounds)
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    BitmapFactory.decodeResource(context.resources, resId, opts)?.asImageBitmap()
} catch (e: Exception) {
    null
}

@Composable
fun PhotoSplash(onFinished: () -> Unit) {
    val context = LocalContext.current
    val resId = remember { context.resources.getIdentifier(SPLASH_PHOTO_NAME, "drawable", context.packageName) }
    var photo by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(resId) {
        if (resId != 0) photo = withContext(Dispatchers.Default) { decodePhoto(context, resId, 2048) }
        if (photo == null) onFinished() else { delay(SPLASH_DURATION_MS); onFinished() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (photo != null && photo!!.width.toFloat() / photo!!.height > 0.9f) Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF42A5F5))) else Brush.verticalGradient(listOf(Color.Black, Color.Black)))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onFinished() }
    ) {
        val img = photo
        if (img != null) {
            // লম্বা (পোর্ট্রেট) ছবি পুরো স্ক্রিনে ক্রপ হয়; চৌকো/গোল ছবি কাটা পড়ে না, পুরোটা দেখায়।
            val squareish = img.width.toFloat() / img.height > 0.9f
            Image(bitmap = img, contentDescription = null, contentScale = if (squareish) ContentScale.Fit else ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
                    .padding(start = 24.dp, end = 24.dp, top = 90.dp, bottom = 56.dp)
            ) {
                Text(SPLASH_NAME, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ---------- লোকেশন ফরওয়ার্ড ----------
// ব্যবহারকারী নিজে বাটন চাপলে তবেই বর্তমান লোকেশন নেওয়া হয়। কোনো ব্যাকগ্রাউন্ড ট্র্যাকিং বা নিজে নিজে পাঠানো নেই।

private const val KEY_CONTACT = "contact"

fun loadContact(context: Context): String =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_CONTACT, "") ?: ""

fun saveContact(context: Context, number: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_CONTACT, number).apply()
}

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
fun fetchLocation(context: Context, onResult: (Location?) -> Unit) {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).filter {
        try { lm.isProviderEnabled(it) } catch (e: Exception) { false }
    }
    if (providers.isEmpty()) { onResult(null); return }

    val handler = Handler(Looper.getMainLooper())
    val listeners = mutableListOf<LocationListener>()
    var done = false

    fun finish(loc: Location?) {
        if (done) return
        done = true
        handler.removeCallbacksAndMessages(null)
        listeners.forEach { try { lm.removeUpdates(it) } catch (e: Exception) { } }
        onResult(loc)
    }

    providers.forEach { provider ->
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) { finish(location) }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        listeners.add(listener)
        try {
            lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
        } catch (e: Exception) { }
    }

    // ১৫ সেকেন্ডে নতুন লোকেশন না পেলে সবচেয়ে সাম্প্রতিক জানা লোকেশন ব্যবহার করা হবে
    handler.postDelayed({
        val last = providers.mapNotNull { try { lm.getLastKnownLocation(it) } catch (e: Exception) { null } }
            .maxByOrNull { it.time }
        finish(last)
    }, 15000L)
}

fun locationMessage(loc: Location): String =
    tr("আমার বর্তমান লোকেশন") + ":\nhttps://maps.google.com/?q=${loc.latitude},${loc.longitude}"

fun shareLocationText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        context.startActivity(Intent.createChooser(send, tr("লোকেশন পাঠান")))
    } catch (e: Exception) {
        Toast.makeText(context, tr("শেয়ার করার অ্যাপ পাওয়া যায়নি"), Toast.LENGTH_SHORT).show()
    }
}

fun smsLocationText(context: Context, number: String, text: String) {
    val sms = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(number))).apply {
        putExtra("sms_body", text)
    }
    try {
        context.startActivity(sms)
    } catch (e: Exception) {
        Toast.makeText(context, tr("মেসেজ অ্যাপ পাওয়া যায়নি"), Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun LocationShareDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var number by remember { mutableStateOf(loadContact(context)) }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf("share") }

    fun start(m: String) {
        mode = m
        busy = true
        status = tr("লোকেশন খোঁজা হচ্ছে… (খোলা জায়গায় দ্রুত পাওয়া যায়)")
        fetchLocation(context) { loc ->
            busy = false
            if (loc == null) {
                status = tr("লোকেশন পাওয়া যায়নি। ফোনের লোকেশন (GPS) চালু আছে কিনা দেখুন।")
            } else {
                status = null
                val text = locationMessage(loc)
                if (m == "sms") smsLocationText(context, number.trim(), text) else shareLocationText(context, text)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) start(mode) else status = tr("পারমিশন না দিলে লোকেশন পাঠানো যাবে না।")
    }

    fun request(m: String) {
        mode = m
        if (m == "sms") saveContact(context, number.trim())
        if (hasLocationPermission(context)) start(m)
        else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("বন্ধ করুন"), color = accent, fontWeight = FontWeight.Bold) } },
        title = { Text(tr("📍  লোকেশন পাঠান"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tr("আপনার বর্তমান লোকেশনের গুগল ম্যাপ লিংক তৈরি হবে। পাঠানোর আগে আপনি নিজে দেখে নিশ্চিত করবেন।"), fontSize = 13.sp)
                Button(
                    onClick = { request("share") },
                    enabled = !busy,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(tr("যেকোনো অ্যাপে পাঠান"), color = Color.White, fontWeight = FontWeight.Bold) }
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text(tr("প্রিয়জনের ফোন নম্বর")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { request("sms") },
                    enabled = !busy && number.isNotBlank(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(tr("এই নম্বরে এসএমএস করুন"), color = Color.White, fontWeight = FontWeight.Bold) }
                status?.let { Text(it, fontSize = 13.sp, color = Color(0xFF555555)) }
            }
        }
    )
}


// ---------- বাংলা সংখ্যা ও সময় ----------
fun bnText(text: String): String = when (appLang) {
    AppLang.BN -> text.map { if (it in '0'..'9') '০' + (it - '0') else it }.joinToString("")
    AppLang.AR -> text.map { if (it in '0'..'9') '٠' + (it - '0') else it }.joinToString("")
    AppLang.EN -> text
}
fun bn(n: Int): String = bnText(n.toString())

fun fmtTime(hours: Double): String {
    val total = ((hours * 60).roundToInt() % 1440 + 1440) % 1440
    val h24 = total / 60
    val m = total % 60
    val ap = if (appLang == AppLang.AR) (if (h24 < 12) "ص" else "م") else (if (h24 < 12) "AM" else "PM")
    val h12 = if (h24 % 12 == 0) 12 else h24 % 12
    return bnText("%d:%02d".format(Locale.US, h12, m)) + " " + ap
}

// ---------- নামাজের সময় (ইন্টারনেট ছাড়াই হিসাব, উম্মুল কুরা পদ্ধতি) ----------
data class City(val bn: String, val lat: Double, val lng: Double)

val saudiCities = listOf(
    City("রিয়াদ", 24.7136, 46.6753), City("জেদ্দা", 21.4858, 39.1925), City("মক্কা", 21.3891, 39.8579),
    City("মদিনা", 24.5247, 39.5692), City("দাম্মাম", 26.4207, 50.0888), City("আল খোবার", 26.2172, 50.1971),
    City("তায়েফ", 21.2703, 40.4158), City("তাবুক", 28.3838, 36.5550), City("বুরাইদা", 26.3260, 43.9750),
    City("আবহা", 18.2164, 42.5053), City("হাইল", 27.5114, 41.7208), City("জাজান", 16.8892, 42.5511),
    City("ইয়ানবু", 24.0895, 38.0618), City("জুবাইল", 27.0046, 49.6460), City("নাজরান", 17.4917, 44.1322),
    City("হফুফ (আল আহসা)", 25.3648, 49.5865)
)

class PrayerTimes(val fajr: Double, val sunrise: Double, val dhuhr: Double, val asr: Double, val maghrib: Double, val isha: Double)

private fun toRad(d: Double) = Math.toRadians(d)
private fun toDeg(r: Double) = Math.toDegrees(r)
private fun fixAngle(a: Double): Double { var x = a % 360.0; if (x < 0) x += 360.0; return x }
private fun fixHour(h: Double): Double { var x = h % 24.0; if (x < 0) x += 24.0; return x }

private fun julianDate(y: Int, m: Int, d: Int): Double {
    var year = y
    var month = m
    if (month <= 2) { year -= 1; month += 12 }
    val a = Math.floor(year / 100.0)
    val b = 2 - a + Math.floor(a / 4.0)
    return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + d + b - 1524.5
}

fun computePrayerTimes(y: Int, m: Int, d: Int, lat: Double, lng: Double, tz: Double = 3.0): PrayerTimes {
    val jd = julianDate(y, m, d) - lng / (15.0 * 24.0)
    val dd = jd + 0.5 - 2451545.0
    val g = fixAngle(357.529 + 0.98560028 * dd)
    val q = fixAngle(280.459 + 0.98564736 * dd)
    val l = fixAngle(q + 1.915 * sin(toRad(g)) + 0.020 * sin(toRad(2 * g)))
    val e = 23.439 - 0.00000036 * dd
    val ra = fixHour(toDeg(atan2(cos(toRad(e)) * sin(toRad(l)), cos(toRad(l)))) / 15.0)
    var eqt = q / 15.0 - ra
    if (eqt > 12) eqt -= 24
    if (eqt < -12) eqt += 24
    val decl = toDeg(asin(sin(toRad(e)) * sin(toRad(l))))
    val noon = fixHour(12.0 - eqt + tz - lng / 15.0)

    fun angleTime(angle: Double, before: Boolean): Double {
        val c = (-sin(toRad(angle)) - sin(toRad(decl)) * sin(toRad(lat))) / (cos(toRad(decl)) * cos(toRad(lat)))
        val t = toDeg(acos(c.coerceIn(-1.0, 1.0))) / 15.0
        return if (before) noon - t else noon + t
    }

    val altAsr = toDeg(atan(1.0 / (1.0 + tan(toRad(abs(lat - decl))))))
    val cAsr = (sin(toRad(altAsr)) - sin(toRad(decl)) * sin(toRad(lat))) / (cos(toRad(decl)) * cos(toRad(lat)))
    val asr = noon + toDeg(acos(cAsr.coerceIn(-1.0, 1.0))) / 15.0

    val maghrib = angleTime(0.833, false)
    return PrayerTimes(
        fajr = angleTime(18.5, true),
        sunrise = angleTime(0.833, true),
        dhuhr = noon,
        asr = asr,
        maghrib = maghrib,
        isha = maghrib + 1.5
    )
}

private const val KEY_CITY = "city"

fun loadCityIndex(context: Context): Int =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_CITY, saudiCities.indexOfFirst { it.bn == "হাইল" }.coerceAtLeast(0)).coerceIn(0, saudiCities.size - 1)

fun saveCityIndex(context: Context, index: Int) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_CITY, index).apply()
}

fun setAlarmFor(context: Context, hours: Double, label: String) {
    val total = ((hours * 60).roundToInt() % 1440 + 1440) % 1440
    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, total / 60)
        putExtra(AlarmClock.EXTRA_MINUTES, total % 60)
        putExtra(AlarmClock.EXTRA_MESSAGE, label)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, tr("অ্যালার্ম অ্যাপ পাওয়া যায়নি"), Toast.LENGTH_SHORT).show()
    }
}

// ---------- আযান ----------
// অডিও: সাধারণ আযান = res/raw/adhan.* অথবা ফোন থেকে বেছে নেওয়া ফাইল।
// ফজরের আযান = আলাদাভাবে বেছে নেওয়া ফাইল; না বাছলে সাধারণ আযানই বাজবে।
private const val KEY_ADHAN_URI = "adhan_uri"
private const val KEY_FAJR_URI = "adhan_fajr_uri"

object AdhanPlayer {
    private var player: MediaPlayer? = null

    fun isPlaying(): Boolean = try { player?.isPlaying == true } catch (e: Exception) { false }

    private fun stop() {
        try { player?.stop() } catch (e: Exception) { }
        try { player?.release() } catch (e: Exception) { }
        player = null
    }

    fun stopPlaying() { stop() }

    private fun rawId(context: Context): Int =
        context.resources.getIdentifier("adhan", "raw", context.packageName)

    private fun keyFor(fajr: Boolean): String = if (fajr) KEY_FAJR_URI else KEY_ADHAN_URI

    private fun savedUri(context: Context, fajr: Boolean): Uri? {
        val str = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(keyFor(fajr), "") ?: ""
        return if (str.isBlank()) null else Uri.parse(str)
    }

    fun saveUri(context: Context, uri: Uri, fajr: Boolean = false) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(keyFor(fajr), uri.toString()).apply()
    }

    private fun clearUri(context: Context, fajr: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().remove(keyFor(fajr)).apply()
    }

    private fun fromUri(app: Context, uri: Uri): MediaPlayer? {
        return try {
            val m = MediaPlayer()
            m.setDataSource(app, uri)
            m.prepare()
            m
        } catch (e: Exception) {
            null
        }
    }

    /** আযান চালানোর মতো কোনো অডিও আছে কি না (ফজর চাইলে ফজরের ফাইল বা সাধারণ আযান) */
    fun hasSource(context: Context, fajr: Boolean = false): Boolean =
        (fajr && savedUri(context, true) != null) || rawId(context) != 0 || savedUri(context, false) != null

    fun toggle(context: Context, fajr: Boolean = false): Boolean {
        if (isPlaying()) {
            stop()
            Toast.makeText(context, tr("আযান বন্ধ করা হয়েছে"), Toast.LENGTH_SHORT).show()
            return false
        }
        stop()
        val app = context.applicationContext
        var mp: MediaPlayer? = null

        if (fajr) {
            val fajrUri = savedUri(context, true)
            if (fajrUri != null) {
                mp = fromUri(app, fajrUri)
                if (mp == null) {
                    clearUri(context, true)
                    Toast.makeText(context, tr("ফজরের ফাইলটি পাওয়া যায়নি, সাধারণ আযান চলছে"), Toast.LENGTH_LONG).show()
                }
            }
        }

        if (mp == null) {
            val resId = rawId(context)
            if (resId != 0) {
                mp = MediaPlayer.create(app, resId)
            } else {
                val uri = savedUri(context, false)
                if (uri == null) {
                    Toast.makeText(context, tr("আযানের অডিও ফাইল বেছে নিন"), Toast.LENGTH_LONG).show()
                    return false
                }
                mp = fromUri(app, uri)
                if (mp == null) {
                    clearUri(context, false)
                    Toast.makeText(context, tr("ফাইলটি পাওয়া যায়নি, আবার বেছে নিন"), Toast.LENGTH_LONG).show()
                    return false
                }
            }
        }

        if (mp == null) {
            Toast.makeText(context, tr("আযান চালানো যায়নি"), Toast.LENGTH_SHORT).show()
            return false
        }
        mp.setOnCompletionListener { stop() }
        player = mp
        mp.start()
        Toast.makeText(context, tr("আযান চলছে 🔊"), Toast.LENGTH_SHORT).show()
        return true
    }
}

/** ফোন থেকে আযানের অডিও বেছে নেওয়ার লঞ্চার। ফাইল বাছাই হলে সেটি মনে রাখে ও সাথে সাথে চালায়। */
@Composable
fun rememberAdhanPicker(fajr: Boolean = false, onResult: (Boolean) -> Unit = {}): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { }
            AdhanPlayer.saveUri(context, uri, fajr)
            AdhanPlayer.stopPlaying()
            onResult(AdhanPlayer.toggle(context, fajr))
        }
    }
    return { launcher.launch(arrayOf("audio/*")) }
}

@Composable
fun PrayerTimesDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var cityIndex by remember { mutableStateOf(loadCityIndex(context)) }
    var menuOpen by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(AdhanPlayer.isPlaying()) }
    val pickAdhan = rememberAdhanPicker(false) { started -> playing = started }
    val pickFajr = rememberAdhanPicker(true) { started -> playing = started }

    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh"))
    val city = saudiCities[cityIndex]
    val t = computePrayerTimes(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), city.lat, city.lng)
    val rows = listOf("ফজর" to t.fajr, "সূর্যোদয়" to t.sunrise, "যোহর" to t.dhuhr, "আসর" to t.asr, "মাগরিব" to t.maghrib, "এশা" to t.isha)
    val nowH = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0
    val prayers = rows.filter { it.first != "সূর্যোদয়" }
    val next = prayers.firstOrNull { it.second > nowH } ?: ("ফজর" to t.fajr + 24)
    val minutesLeft = ((next.second - nowH) * 60).roundToInt().coerceAtLeast(0)
    val leftText = remainingText(minutesLeft)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("বন্ধ করুন"), color = accent, fontWeight = FontWeight.Bold) } },
        title = { Text(tr("🕌  নামাজের সময়"), fontWeight = FontWeight.Bold) },
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
                Text(tr("পরের নামাজ") + ": " + tr(next.first) + " — " + leftText, fontWeight = FontWeight.Bold, color = accent, fontSize = 14.sp)
                rows.forEach { (name, time) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(tr(name), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(fmtTime(time), fontWeight = FontWeight.Bold)
                            Text(tr("বাংলাদেশে") + " " + fmtTime(time + 3.0), fontSize = 11.sp, color = Color(0xFF666666))
                        }
                        if (name != "সূর্যোদয়") {
                            TextButton(onClick = { setAlarmFor(context, time, tr("নামাজ") + ": " + tr(name)) }) { Text("⏰") }
                        } else {
                            Spacer(Modifier.width(48.dp))
                        }
                    }
                }
                Button(
                    onClick = { if (playing || AdhanPlayer.hasSource(context)) playing = AdhanPlayer.toggle(context) else pickAdhan() },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.fillMaxWidth()
                ) { ETxt(if (playing) tr("⏹ আযান বন্ধ করুন") else tr("🔊 আযান শুনুন"), color = Color.White, fontWeight = FontWeight.Bold) }
                OutlinedButton(
                    onClick = { if (playing || AdhanPlayer.hasSource(context, true)) playing = AdhanPlayer.toggle(context, true) else pickFajr() },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) { ETxt(if (playing) tr("⏹ আযান বন্ধ করুন") else tr("🌅 ফজরের আযান শুনুন"), color = accent, fontWeight = FontWeight.Bold) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { pickAdhan() }) { Text(tr("🎵 আযানের ফাইল"), color = accent, fontSize = 12.sp) }
                    TextButton(onClick = { pickFajr() }) { Text(tr("🎵 ফজরের ফাইল"), color = accent, fontSize = 12.sp) }
                }
                Text(
                    tr("⏰ চাপলে ফোনের অ্যালার্ম খুলবে। সময় হিসাব করে বের করা (উম্মুল কুরা পদ্ধতি), রমজানে এশা আরও ৩০ মিনিট পরে হয়। মসজিদের সময়ের সাথে মিলিয়ে নিন।"),
                    fontSize = 11.sp, color = Color(0xFF666666)
                )
            }
        }
    )
}

// ---------- কাগজপত্রের মেয়াদ রিমাইন্ডার ----------
val docTypes = listOf("ইকামা", "ড্রাইভিং লাইসেন্স", "গাড়ির ইসতিমারা", "গাড়ির ইনসুরেন্স", "ফাহাস (গাড়ি পরীক্ষা)", "অন্যান্য")
private const val KEY_DOCS = "docs"

fun loadDocs(context: Context): List<Pair<String, String>> =
    (context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_DOCS, "") ?: "")
        .split("\n").filter { it.contains("|") }
        .map { it.substringBefore("|") to it.substringAfter("|") }

fun saveDocs(context: Context, docs: List<Pair<String, String>>) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        .putString(KEY_DOCS, docs.joinToString("\n") { it.first + "|" + it.second }).apply()
}

fun daysLeft(date: String): Int? = try {
    val p = date.split("-").map { it.toInt() }
    val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    val target = Calendar.getInstance().apply { set(p[0], p[1] - 1, p[2], 0, 0, 0); set(Calendar.MILLISECOND, 0) }
    Math.round((target.timeInMillis - today.timeInMillis) / 86400000.0).toInt()
} catch (e: Exception) { null }

fun addToCalendar(context: Context, name: String, date: String) {
    try {
        val p = date.split("-").map { it.toInt() }
        val start = Calendar.getInstance().apply { set(p[0], p[1] - 1, p[2], 9, 0, 0); add(Calendar.DAY_OF_YEAR, -30) }
        if (start.timeInMillis < System.currentTimeMillis()) start.timeInMillis = System.currentTimeMillis() + 3600000L
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.timeInMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start.timeInMillis + 3600000L)
            putExtra(CalendarContract.Events.TITLE, l3("${tr(name)} এর মেয়াদ শেষ হবে ($date)", "${tr(name)} expires ($date)", "تنتهي صلاحية ${tr(name)} ($date)"))
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, tr("ক্যালেন্ডার অ্যাপ পাওয়া যায়নি"), Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun RemindersDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val docs = remember { mutableStateListOf<Pair<String, String>>().also { it.addAll(loadDocs(context)) } }
    var type by remember { mutableStateOf(docTypes[0]) }
    var menuOpen by remember { mutableStateOf(false) }

    fun pickDate() {
        val now = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            val date = "%04d-%02d-%02d".format(Locale.US, y, m + 1, d)
            if (type != "অন্যান্য") docs.removeAll { it.first == type }
            docs.add(type to date)
            saveDocs(context, docs)
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("বন্ধ করুন"), color = accent, fontWeight = FontWeight.Bold) } },
        title = { Text(tr("⏰  রিমাইন্ডার"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tr("কাগজের মেয়াদ শেষ হওয়ার তারিখ বসান। কত দিন বাকি তা এখানে দেখা যাবে।"), fontSize = 13.sp)
                Box {
                    OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) { Text("${tr(type)}  ▾") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        docTypes.forEach { dt -> DropdownMenuItem(text = { Text(tr(dt)) }, onClick = { type = dt; menuOpen = false }) }
                    }
                }
                Button(
                    onClick = { pickDate() },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(tr("📅 মেয়াদের তারিখ বেছে নিন"), color = Color.White, fontWeight = FontWeight.Bold) }

                if (docs.isEmpty()) {
                    Text(tr("এখনো কিছু যোগ করা হয়নি।"), fontSize = 13.sp, color = Color(0xFF666666))
                }
                docs.sortedBy { it.second }.forEach { doc ->
                    val days = daysLeft(doc.second)
                    val (statusText, statusColor) = when {
                        days == null -> "" to Color(0xFF666666)
                        days < 0 -> tr("মেয়াদ শেষ হয়ে গেছে ⚠️") to Color(0xFFC62828)
                        days <= 30 -> daysLeftText(days, true) to Color(0xFFEF6C00)
                        else -> daysLeftText(days, false) to Color(0xFF2E7D32)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr(doc.first), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${bnText(doc.second)}  •  $statusText", fontSize = 12.sp, color = statusColor)
                        }
                        TextButton(onClick = { addToCalendar(context, doc.first, doc.second) }) { Text("📅") }
                        TextButton(onClick = { docs.remove(doc); saveDocs(context, docs) }) { ETxt("🗑") }
                    }
                }
                Text(tr("📅 চাপলে মেয়াদের ৩০ দিন আগের জন্য ক্যালেন্ডারে রিমাইন্ডার তৈরি হবে।"), fontSize = 11.sp, color = Color(0xFF666666))
            }
        }
    )
}

// ---------- জরুরি নম্বর (সৌদি আরব) ----------
val saudiEmergency = listOf(
    "পুলিশ" to "999",
    "অ্যাম্বুলেন্স (রেড ক্রিসেন্ট)" to "997",
    "ফায়ার সার্ভিস (সিভিল ডিফেন্স)" to "998",
    "ট্রাফিক পুলিশ" to "993"
)

fun dialNumber(context: Context, number: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
    } catch (e: Exception) {
        Toast.makeText(context, tr("ফোন অ্যাপ পাওয়া যায়নি"), Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun EmergencyDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val people = remember { mutableStateListOf<Pair<String, String>>().also { it.addAll(loadPeople(context)) } }
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var code by remember { mutableStateOf(loadLastCode(context)) }

    fun addPerson(n: String, num: String) {
        val cleanNum = applyCountryCode(code, num)
        if (cleanNum.isBlank()) return
        val cleanName = n.replace("|", " ").trim().ifBlank { "নাম নেই" }
        people.add(cleanName to cleanNum)
        savePeople(context, people)
        name = ""
        number = ""
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (uri != null) {
            val picked = readPickedContact(context, uri)
            if (picked != null) addPerson(picked.first, picked.second)
            else Toast.makeText(context, tr("নম্বর পাওয়া যায়নি"), Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("বন্ধ করুন"), color = accent, fontWeight = FontWeight.Bold) } },
        title = { Text(tr("🚨  জরুরি নম্বর"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                saudiEmergency.forEach { (name, number) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr(name), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(bnText(number), fontSize = 13.sp, color = Color(0xFF666666))
                        }
                        Button(onClick = { dialNumber(context, number) }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))) {
                            ETxt(tr("📞 কল"), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(tr("আপন লোকজন (কাছের মানুষ)"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = accent)
                if (people.isEmpty()) {
                    Text(tr("এখনো কারো নম্বর যোগ করা হয়নি।"), fontSize = 13.sp, color = Color(0xFF666666))
                }
                people.toList().forEach { person ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tr(person.first), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text(bnText(person.second), fontSize = 13.sp, color = Color(0xFF666666))
                            }
                            TextButton(onClick = { people.remove(person); savePeople(context, people) }) { ETxt("🗑") }
                        }
                        ContactActions(number = person.second)
                    }
                }
                OutlinedButton(
                    onClick = {
                        try {
                            picker.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
                        } catch (e: Exception) {
                            Toast.makeText(context, tr("কন্টাক্ট অ্যাপ পাওয়া যায়নি"), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(tr("📇 ফোনবুক থেকে বেছে নিন")) }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(tr("নাম (যেমন: বাড়ি, আব্বা, বন্ধু)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CountryCodePicker(selected = code, onSelect = { code = it; saveLastCode(context, it) })
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it },
                        label = { Text(l3("নম্বর (কোড ছাড়া লিখুন)", "Number (without code)", "الرقم (بدون رمز الدولة)")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                }
                Button(
                    onClick = { addPerson(name, number) },
                    enabled = number.isNotBlank(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.fillMaxWidth()
                ) { ETxt(tr("➕ নম্বর যোগ করুন"), color = Color.White, fontWeight = FontWeight.Bold) }
                ETxt(tr("📞 কল চাপলে ফোনের ডায়ালারে নম্বর বসে যাবে, আপনি নিজে কল চাপবেন।"), fontSize = 11.sp, color = Color(0xFF666666))
            }
        }
    )
}

// ---------- আপন লোকজনের নম্বর ----------
private const val KEY_PEOPLE = "people"

fun loadPeople(context: Context): List<Pair<String, String>> =
    (context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_PEOPLE, "") ?: "")
        .split("\n").filter { it.contains("|") }
        .map { it.substringBefore("|") to it.substringAfter("|") }

fun savePeople(context: Context, people: List<Pair<String, String>>) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        .putString(KEY_PEOPLE, people.joinToString("\n") { it.first + "|" + it.second }).apply()
}

fun readPickedContact(context: Context, uri: Uri): Pair<String, String>? = try {
    context.contentResolver.query(
        uri,
        arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
        null, null, null
    )?.use { c -> if (c.moveToFirst()) (c.getString(0) ?: "") to (c.getString(1) ?: "") else null }
} catch (e: Exception) {
    null
}

// ---------- মালিক ----------
private const val KEY_OWNER_NAME = "owner_name"
private const val KEY_OWNER_NUMBER = "owner_number"

@Composable
fun OwnerDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var name by remember { mutableStateOf(prefs.getString(KEY_OWNER_NAME, "") ?: "") }
    var number by remember { mutableStateOf(prefs.getString(KEY_OWNER_NUMBER, "") ?: "") }
    var code by remember { mutableStateOf(loadLastCode(context).let { if (it == "+880") "+966" else it }) }
    val fullNumber = applyCountryCode(code, number)

    fun save() {
        prefs.edit().putString(KEY_OWNER_NAME, name.trim()).putString(KEY_OWNER_NUMBER, fullNumber).apply()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { save(); onDismiss() }) { Text(tr("সেভ করে বন্ধ করুন"), color = accent, fontWeight = FontWeight.Bold) } },
        title = { Text(tr("👔  মালিক"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(tr("মালিকের নাম")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CountryCodePicker(selected = code, onSelect = { code = it })
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it },
                        label = { Text(tr("মালিকের ফোন নম্বর")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (number.isNotBlank()) ContactActions(number = fullNumber, beforeAction = { save() })
                Text(tr("নম্বর একবার সেভ করলে পরের বার সরাসরি কল বা মেসেজ করা যাবে। কল বাটন চাপলে ডায়ালারে নম্বর বসবে, আপনি নিজে কল চাপবেন।"), fontSize = 11.sp, color = Color(0xFF666666))
            }
        }
    )
}

// ---------- কল / হোয়াটসঅ্যাপ / ইমো / মেসেঞ্জার বাটন ----------
private const val PKG_IMO = "com.imo.android.imoim"
private val PKG_MESSENGER = listOf("com.facebook.orca", "com.facebook.mlite")

fun openWhatsApp(context: Context, number: String) {
    var digits = number
    if (digits.startsWith("00")) digits = "+" + digits.removePrefix("00")
    if (!digits.startsWith("+")) {
        Toast.makeText(context, tr("হোয়াটসঅ্যাপের জন্য দেশের কোডসহ নম্বর দিন (যেমন +৮৮০…)"), Toast.LENGTH_LONG).show()
        return
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=" + digits.filter { it.isDigit() })))
    } catch (e: Exception) {
        Toast.makeText(context, tr("হোয়াটসঅ্যাপ ইনস্টল করা নেই"), Toast.LENGTH_SHORT).show()
    }
}

// ইমো ও মেসেঞ্জার নম্বর দিয়ে সরাসরি চ্যাট খোলার লিংক দেয় না, তাই অ্যাপ খুলে নম্বরটা কপি করে দেওয়া হয়।
fun openAppWithNumber(context: Context, packages: List<String>, appName: String, number: String) {
    val intent = packages.firstNotNullOfOrNull { context.packageManager.getLaunchIntentForPackage(it) }
    if (intent == null) {
        Toast.makeText(context, l3("$appName ইনস্টল করা নেই", "$appName is not installed", "$appName غير مثبت"), Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("number", number))
        Toast.makeText(context, l3("নম্বর কপি হয়েছে। $appName-এর সার্চে পেস্ট করুন", "Number copied. Paste it in the $appName search", "تم نسخ الرقم. ألصقه في بحث $appName"), Toast.LENGTH_LONG).show()
    } catch (e: Exception) { }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, l3("$appName খোলা যায়নি", "Could not open $appName", "تعذر فتح $appName"), Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ActionButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
    ) { ETxt(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1) }
}

@Composable
fun ContactActions(number: String, beforeAction: () -> Unit = {}) {
    val context = LocalContext.current
    val clean = number.filter { it.isDigit() || it == '+' }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ActionButton(tr("📞 কল"), Color(0xFFC62828), Modifier.weight(1f)) { beforeAction(); dialNumber(context, clean) }
            ActionButton(tr("💬 হোয়াটসঅ্যাপ"), Color(0xFF1FA855), Modifier.weight(1f)) { beforeAction(); openWhatsApp(context, clean) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ActionButton(tr("🟦 ইমো"), Color(0xFF1E88E5), Modifier.weight(1f)) { beforeAction(); openAppWithNumber(context, listOf(PKG_IMO), tr("ইমো"), clean) }
            ActionButton(tr("🔵 মেসেঞ্জার"), Color(0xFF0084FF), Modifier.weight(1f)) { beforeAction(); openAppWithNumber(context, PKG_MESSENGER, tr("মেসেঞ্জার"), clean) }
        }
    }
}

// ---------- লাইভ লোকেশন (গাড়ি) ----------
// এই পাতা খোলা থাকলে তবেই লোকেশন আপডেট হয়; পাতা বন্ধ করলে আপডেট বন্ধ হয়ে যায়। ব্যাকগ্রাউন্ডে কিছু চলে না।

@SuppressLint("MissingPermission")
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
@Composable
fun LiveLocationDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasLocationPermission(context)) }
    var loc by remember { mutableStateOf<Location?>(null) }
    var updatedAt by remember { mutableStateOf(0L) }
    var status by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        granted = result.values.any { it }
        if (!granted) status = tr("পারমিশন না দিলে লাইভ লোকেশন দেখানো যাবে না।")
    }
    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    DisposableEffect(granted) {
        if (!granted) return@DisposableEffect onDispose { }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val current = loc
                val fresh = System.currentTimeMillis() - updatedAt > 10000L
                if (current == null || location.provider == LocationManager.GPS_PROVIDER || location.accuracy <= current.accuracy || fresh) {
                    loc = location
                    updatedAt = System.currentTimeMillis()
                    status = null
                }
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).filter {
            try { lm.isProviderEnabled(it) } catch (e: Exception) { false }
        }
        if (providers.isEmpty()) {
            status = tr("ফোনের লোকেশন (GPS) চালু করুন।")
        } else {
            status = tr("লোকেশন খোঁজা হচ্ছে… (খোলা জায়গায় দ্রুত পাওয়া যায়)")
            providers.forEach { p ->
                try { lm.requestLocationUpdates(p, 2000L, 0f, listener, Looper.getMainLooper()) } catch (e: Exception) { }
            }
            providers.mapNotNull { try { lm.getLastKnownLocation(it) } catch (e: Exception) { null } }
                .maxByOrNull { it.time }?.let { if (loc == null) { loc = it; updatedAt = System.currentTimeMillis() } }
        }
        onDispose { try { lm.removeUpdates(listener) } catch (e: Exception) { } }
    }

    val current = loc
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("বন্ধ করুন"), color = accent, fontWeight = FontWeight.Bold) } },
        title = { Text(tr("🚗  লাইভ লোকেশন"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (current == null) {
                    Text(status ?: tr("লোকেশন খোঁজা হচ্ছে…"), fontSize = 14.sp)
                } else {
                    val time = SimpleDateFormat("hh:mm:ss a", Locale.US).format(java.util.Date(updatedAt))
                    Text(tr("📍 অক্ষাংশ: ") + bnText("%.5f".format(Locale.US, current.latitude)), fontWeight = FontWeight.Medium)
                    Text(tr("📍 দ্রাঘিমাংশ: ") + bnText("%.5f".format(Locale.US, current.longitude)), fontWeight = FontWeight.Medium)
                    ETxt(tr("🎯 সঠিকতা") + ": ±" + bn(current.accuracy.roundToInt()) + " " + tr("মিটার"), fontSize = 13.sp)
                    if (current.hasSpeed()) Text(tr("🚗 গতি") + ": " + bn((current.speed * 3.6f).roundToInt()) + " " + tr("কিমি/ঘণ্টা"), fontSize = 13.sp)
                    Text(tr("🕒 শেষ আপডেট: ") + bnText(time), fontSize = 13.sp, color = Color(0xFF666666))
                    status?.let { Text(it, fontSize = 12.sp, color = Color(0xFFC62828)) }
                    Button(
                        onClick = {
                            val lat = current.latitude
                            val lng = current.longitude
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng(${tr("আমার গাড়ি")})")))
                            } catch (e: Exception) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$lat,$lng")))
                                } catch (e2: Exception) {
                                    Toast.makeText(context, tr("ম্যাপ অ্যাপ পাওয়া যায়নি"), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        modifier = Modifier.fillMaxWidth()
                    ) { ETxt(tr("🗺️ গুগল ম্যাপে দেখুন"), color = Color.White, fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { shareLocationText(context, locationMessage(current)) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(tr("📤 এই লোকেশন পাঠান"), color = Color.White, fontWeight = FontWeight.Bold) }
                }
                Text(
                    tr("এই পাতা খোলা থাকলে প্রতি কয়েক সেকেন্ডে লোকেশন নিজে আপডেট হয়। পাতা বন্ধ করলে আপডেট বন্ধ হয়ে যায়।"),
                    fontSize = 11.sp, color = Color(0xFF666666)
                )
            }
        }
    )
}

// ---------- অ্যালবাম ----------
// প্রথম ছবি অ্যাপের ভেতরের ছবি (my_photo)। "➕ ছবি" দিয়ে ফোনের গ্যালারি থেকে আরও ছবি যোগ করা যায়; সেগুলো শুধু এই অ্যাপের ভেতরে থাকে।

data class AlbumPhoto(val file: File?, val resId: Int)

fun albumDir(context: Context): File = File(context.filesDir, "album").apply { mkdirs() }

private const val KEY_ALBUM_HIDDEN = "album_hidden"

fun photoKey(context: Context, p: AlbumPhoto): String =
    if (p.file != null) "f:" + p.file.name
    else "r:" + (try { context.resources.getResourceEntryName(p.resId) } catch (e: Exception) { p.resId.toString() })

// এই ছবিগুলো অ্যাপ থেকে কোনোভাবেই মোছা বা লুকানো যায় না (অ্যাপের ভেতরের ছবির নাম)।
val protectedPhotoNames = setOf("album_11")

fun isProtectedPhoto(context: Context, p: AlbumPhoto): Boolean =
    p.file == null && photoKey(context, p).removePrefix("r:") in protectedPhotoNames

fun hiddenPhotos(context: Context): Set<String> =
    HashSet(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getStringSet(KEY_ALBUM_HIDDEN, emptySet()) ?: emptySet())

// ফোন থেকে যোগ করা ছবি চিরতরে মোছে; অ্যাপের ভেতরের ছবি লুকিয়ে রাখে (পরে "ফিরিয়ে আনুন" দিয়ে ফেরানো যায়)।
fun deletePhotos(context: Context, list: List<AlbumPhoto>) {
    val hidden = hiddenPhotos(context).toMutableSet()
    for (p in list.filterNot { isProtectedPhoto(context, it) }) {
        if (p.file != null) p.file.delete() else hidden.add(photoKey(context, p))
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putStringSet(KEY_ALBUM_HIDDEN, hidden).apply()
}

fun restoreHiddenPhotos(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().remove(KEY_ALBUM_HIDDEN).apply()
}

fun loadAlbum(context: Context): List<AlbumPhoto> {
    val list = mutableListOf<AlbumPhoto>()
    val builtIn = context.resources.getIdentifier(SPLASH_PHOTO_NAME, "drawable", context.packageName)
    if (builtIn != 0) list.add(AlbumPhoto(null, builtIn))
    for (i in 1..100) {
        val id = context.resources.getIdentifier("album_$i", "drawable", context.packageName)
        if (id != 0) list.add(AlbumPhoto(null, id))
    }
    albumDir(context).listFiles()?.sortedBy { it.lastModified() }?.forEach { list.add(AlbumPhoto(it, 0)) }
    val hidden = hiddenPhotos(context)
    return list.filter { isProtectedPhoto(context, it) || photoKey(context, it) !in hidden }
}

fun addToAlbum(context: Context, uri: Uri): Boolean = try {
    val target = File(albumDir(context), "img_" + System.currentTimeMillis() + "_" + (1000..9999).random() + ".jpg")
    context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } } != null
} catch (e: Exception) {
    false
}

fun decodeFile(path: String, maxSide: Int): ImageBitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
    var bmp = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    if (bmp != null) {
        val degrees = when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees != 0f) bmp = android.graphics.Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(degrees) }, true)
    }
    bmp?.asImageBitmap()
} catch (e: Exception) {
    null
}

@Composable
fun AlbumImage(photo: AlbumPhoto, maxSide: Int, modifier: Modifier, contentScale: ContentScale) {
    val context = LocalContext.current
    var bmp by remember(photo) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(photo, maxSide) {
        bmp = withContext(Dispatchers.IO) {
            if (photo.file != null) decodeFile(photo.file.path, maxSide) else decodePhoto(context, photo.resId, maxSide)
        }
    }
    val img = bmp
    if (img != null) Image(bitmap = img, contentDescription = null, contentScale = contentScale, modifier = modifier)
    else Box(modifier = modifier.background(Color(0xFFE0E0E0)))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumDialog(accent: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var photos by remember { mutableStateOf(loadAlbum(context)) }
    var hiddenN by remember { mutableStateOf(hiddenPhotos(context).size) }
    var viewing by remember { mutableStateOf<AlbumPhoto?>(null) }
    val selected = remember { mutableStateListOf<String>() }
    var confirm by remember { mutableStateOf<List<AlbumPhoto>?>(null) }
    fun refresh() { photos = loadAlbum(context); hiddenN = hiddenPhotos(context).size }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            val ok = withContext(Dispatchers.IO) { uris.count { addToAlbum(context, it) } }
            refresh()
            Toast.makeText(
                context,
                if (ok > 0) l3("${bn(ok)}টি ছবি যোগ হয়েছে ✅", "$ok photo(s) added ✅", "تمت إضافة $ok صورة ✅") else tr("ছবি যোগ করা যায়নি"),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Dialog(
        onDismissRequest = { if (viewing != null) viewing = null else if (selected.isNotEmpty()) selected.clear() else onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(if (selected.isNotEmpty()) Color(0xFF37474F) else accent).padding(top = 24.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selected.isNotEmpty()) {
                            TextButton(onClick = { selected.clear() }) { Text("✕", color = Color.White, fontWeight = FontWeight.Bold) }
                            Text(l3("${bn(selected.size)}টি বাছা", "${selected.size} selected", "${selected.size} محددة"), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            TextButton(onClick = { selected.clear(); photos.filterNot { isProtectedPhoto(context, it) }.forEach { selected.add(photoKey(context, it)) } }) { Text(l3("সব বাছুন", "Select all", "تحديد الكل"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            TextButton(onClick = { confirm = photos.filter { photoKey(context, it) in selected } }) { Text(tr("🗑 মুছুন"), color = Color.White, fontWeight = FontWeight.Bold) }
                        } else {
                            TextButton(onClick = onDismiss) { Text(tr("← ফিরে যান"), color = Color.White, fontWeight = FontWeight.Bold) }
                            Text(tr("🖼️ অ্যালবাম"), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            TextButton(onClick = { picker.launch("image/*") }) { Text(tr("➕ ছবি"), color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                    }
                    if (selected.isEmpty()) {
                        Text(
                            l3("ছবি চেপে ধরলে একসাথে অনেক ছবি বেছে মুছতে পারবেন।", "Long-press a photo to select and delete several at once.", "اضغط مطولاً على صورة لتحديد عدة صور وحذفها."),
                            fontSize = 11.sp, color = Color(0xFF777777), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                    if (hiddenN > 0) {
                        TextButton(onClick = { restoreHiddenPhotos(context); refresh() }, modifier = Modifier.fillMaxWidth()) {
                            Text(l3("↩ মুছে ফেলা অ্যাপের ছবি ফিরিয়ে আনুন", "↩ Restore deleted app photos", "↩ استعادة صور التطبيق المحذوفة"), color = accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    if (photos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(tr("এখনো কোনো ছবি নেই। ➕ ছবি চেপে যোগ করুন।"), textAlign = TextAlign.Center, color = Color(0xFF555555))
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(photos) { p ->
                                val k = photoKey(context, p)
                                val sel = selected.contains(k)
                                Box(
                                    modifier = Modifier.aspectRatio(1f).combinedClickable(
                                        onClick = { if (selected.isNotEmpty()) { if (isProtectedPhoto(context, p)) { Toast.makeText(context, l3("এই ছবি মোছা যাবে না 🔒", "This photo can't be deleted 🔒", "لا يمكن حذف هذه الصورة 🔒"), Toast.LENGTH_SHORT).show() } else if (sel) selected.remove(k) else selected.add(k) } else viewing = p },
                                        onLongClick = { if (isProtectedPhoto(context, p)) { Toast.makeText(context, l3("এই ছবি মোছা যাবে না 🔒", "This photo can't be deleted 🔒", "لا يمكن حذف هذه الصورة 🔒"), Toast.LENGTH_SHORT).show() } else if (!sel) selected.add(k) }
                                    )
                                ) {
                                    AlbumImage(p, 400, Modifier.fillMaxSize(), ContentScale.Crop)
                                    if (isProtectedPhoto(context, p)) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) { Text("🔒", fontSize = 16.sp, modifier = Modifier.padding(6.dp)) }
                                    }
                                    if (sel) {
                                        Box(modifier = Modifier.fillMaxSize().background(Color(0x663366FF)), contentAlignment = Alignment.TopEnd) {
                                            Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, modifier = Modifier.padding(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                val v = viewing
                if (v != null) {
                    val pager = rememberPagerState(initialPage = photos.indexOf(v).coerceAtLeast(0), pageCount = { photos.size })
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                            AlbumImage(photos[page], 2048, Modifier.fillMaxSize(), ContentScale.Fit)
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 4.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { viewing = null }) { Text(tr("✕ বন্ধ করুন"), color = Color.White, fontWeight = FontWeight.Bold) }
                            Text("${bn(pager.currentPage + 1)} / ${bn(photos.size)}", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            val cur = photos.getOrNull(pager.currentPage)
                            if (cur != null && isProtectedPhoto(context, cur)) {
                                TextButton(onClick = { Toast.makeText(context, l3("এই ছবি মোছা যাবে না 🔒", "This photo can't be deleted 🔒", "لا يمكن حذف هذه الصورة 🔒"), Toast.LENGTH_SHORT).show() }) { Text("🔒", color = Color.White, fontWeight = FontWeight.Bold) }
                            } else {
                                TextButton(onClick = { cur?.let { confirm = listOf(it) } }) { Text(tr("🗑 মুছুন"), color = Color.White, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }
        }
    }

    val toDelete = confirm
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(l3("${bn(toDelete.size)}টি ছবি মুছবেন?", "Delete ${toDelete.size} photo(s)?", "حذف ${toDelete.size} صورة؟"), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    l3(
                        "ফোন থেকে যোগ করা ছবি চিরতরে মুছে যাবে। অ্যাপের ভেতরের ছবি শুধু লুকানো হবে, পরে \"ফিরিয়ে আনুন\" চেপে ফেরানো যাবে।",
                        "Photos you added from your phone are deleted permanently. Photos built into the app are only hidden; you can restore them later.",
                        "الصور المضافة من هاتفك تُحذف نهائياً. صور التطبيق تُخفى فقط ويمكن استعادتها لاحقاً."
                    ), fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    deletePhotos(context, toDelete)
                    selected.clear(); viewing = null; confirm = null; refresh()
                }) { Text(tr("🗑 মুছুন"), color = Color(0xFFC62828), fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text(l3("বাতিল", "Cancel", "إلغاء")) } }
        )
    }
}
