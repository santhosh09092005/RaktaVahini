package com.example.raktavahini

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// ─── Design Tokens ───────────────────────────────────────────────────────────
val PrimaryRed        = Color(0xFFCC1E1E)
val PrimaryRedLight   = Color(0xFFE02020)
val RedDark           = Color(0xFFAA1515)
val White             = Color(0xFFFFFFFF)
val OffWhite          = Color(0xFFF5F5F5)
val LightGray         = Color(0xFFEEEEEE)
val MediumGray        = Color(0xFFBBBBBB)
val DarkGray          = Color(0xFF666666)
val TextDark          = Color(0xFF1A1A1A)
val TextMedium        = Color(0xFF444444)
val TextLight         = Color(0xFF888888)
val GreenAccent       = Color(0xFF22AA55)
val GreenLight        = Color(0xFFE8F5EE)
val GreenBorder       = Color(0xFF99DDBB)
val RedTint           = Color(0xFFFFF0F0)
val CardShadow        = Color(0x18000000)

// ─── Data Model ──────────────────────────────────────────────────────────────
data class Donor(
    val id: String = "",
    val name: String = "",
    val bloodGroup: String = "",
    val city: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val lastDonationDate: String = "",
    val phone: String = "",
    val isAvailable: Boolean = true
)

// ─── App Entry ───────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = PrimaryRed)) {
                AppNavigator()
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────
fun getDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun getDaysSince(dateStr: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val diff = System.currentTimeMillis() - (sdf.parse(dateStr)?.time ?: return 0)
        diff / (1000 * 60 * 60 * 24)
    } catch (e: Exception) { 0 }
}

fun initials(name: String) = name.trim().split(" ")
    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
    .take(2).joinToString("")

// ─── Navigation ──────────────────────────────────────────────────────────────
@Composable
fun AppNavigator() {
    var screen by remember { mutableStateOf("home") }
    Box(modifier = Modifier.fillMaxSize().background(White)) {
        when (screen) {
            "home"     -> HomeScreen { screen = it }
            "register" -> RegisterScreen { screen = "home" }
            "search"   -> SearchScreen { screen = "home" }
        }
    }
}

// ─── Home Screen ─────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {

        // ── Red top half ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .background(PrimaryRed)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp, bottom = 28.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {

                // Live badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).background(White, CircleShape))
                    Text(
                        "• NETWORK LIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        letterSpacing = 0.8.sp
                    )
                }

                Column {
                    // Heart icon box
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Rakta",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        lineHeight = 48.sp,
                        letterSpacing = (-1.5).sp
                    )
                    Text(
                        "Vahini",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        lineHeight = 48.sp,
                        letterSpacing = (-1.5).sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "BLOOD MANAGEMENT SYSTEM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = White.copy(alpha = 0.75f),
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        // ── White bottom half ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(White)
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Choose an action below",
                    fontSize = 13.sp,
                    color = TextLight,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Donor Registration button
                Button(
                    onClick = { onNavigate("register") },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "DONOR REGISTRATION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Find Blood Now button
                OutlinedButton(
                    onClick = { onNavigate("search") },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryRed)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "FIND BLOOD NOW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("8" to "Types", "24h" to "Support").forEach { (num, lbl) ->
                    HomeStatCard(num, lbl, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun HomeStatCard(number: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(OffWhite, RoundedCornerShape(12.dp))
            .border(1.dp, LightGray, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(number, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        Text(label.uppercase(), fontSize = 9.sp, color = TextLight, letterSpacing = 1.sp)
    }
}

// ─── Register Screen ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var blood by remember { mutableStateOf("") }
    var placeName by remember { mutableStateOf("") }
    var donationDate by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d -> donationDate = "$y-${(m + 1).toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}" },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(modifier = Modifier.fillMaxSize().background(White)) {

        // ── Red top bar ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryRed)
                .padding(horizontal = 20.dp)
                .padding(top = 44.dp, bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("← Back", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp)
            ) {
                Text(
                    "Donor Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = White
                )
                Text(
                    "Register to save lives nearby",
                    fontSize = 13.sp,
                    color = White.copy(alpha = 0.8f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).background(White),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                WhiteTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "FULL NAME",
                    placeholder = "Arjun Sharma",
                    leadingIcon = Icons.Default.Person
                )
            }
            item {
                Text("BLOOD GROUP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLight, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(8.dp))
                BloodGroupSelector(selected = blood, onSelected = { blood = it })
            }
            item {
                WhiteTextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    label = "LOCATION",
                    placeholder = "Mekhri Circle, Bengaluru",
                    leadingIcon = Icons.Default.LocationOn
                )
            }
            item {
                Column {
                    Text("LAST DONATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLight, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(White, RoundedCornerShape(10.dp))
                            .border(1.dp, if (donationDate.isNotEmpty()) PrimaryRed else LightGray, RoundedCornerShape(10.dp))
                            .clickable { datePicker.show() }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = if (donationDate.isNotEmpty()) PrimaryRed else MediumGray, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (donationDate.isEmpty()) "yyyy-mm-dd" else donationDate,
                            fontSize = 15.sp,
                            color = if (donationDate.isEmpty()) MediumGray else TextDark,
                            modifier = Modifier.weight(1f)
                        )
                        if (donationDate.isNotEmpty()) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            item {
                WhiteTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "PHONE",
                    placeholder = "+91 98765 43210",
                    leadingIcon = Icons.Default.Phone
                )
            }
        }

        // Save button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    if (name.isBlank() || blood.isBlank() || placeName.isBlank() || donationDate.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    val geocoder = Geocoder(context)
                    try {
                        val addresses = geocoder.getFromLocationName(placeName, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val db = FirebaseDatabase.getInstance().getReference("donors")
                            val id = db.push().key ?: return@Button
                            val donor = Donor(id, name, blood, placeName, addresses[0].latitude, addresses[0].longitude, donationDate, phone)
                            db.child(id).setValue(donor)
                                .addOnSuccessListener {
                                    isSaving = false
                                    Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                                .addOnFailureListener { isSaving = false }
                        } else {
                            isSaving = false
                            Toast.makeText(context, "Location not found.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        isSaving = false
                        Toast.makeText(context, "Network error.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = White, strokeWidth = 2.dp)
                } else {
                    Text("SAVE PROFILE", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun WhiteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLight, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MediumGray, fontSize = 14.sp) },
            leadingIcon = {
                Icon(leadingIcon, contentDescription = null, tint = if (value.isNotEmpty()) PrimaryRed else MediumGray, modifier = Modifier.size(18.dp))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = White,
                focusedContainerColor = White,
                unfocusedBorderColor = LightGray,
                focusedBorderColor = PrimaryRed,
                unfocusedTextColor = TextDark,
                focusedTextColor = TextDark,
                cursorColor = PrimaryRed
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
        )
    }
}

@Composable
fun BloodGroupSelector(selected: String, onSelected: (String) -> Unit) {
    val groups = listOf("A+", "B+", "O+", "AB+", "A-", "B-", "O-", "AB-")
    val rows = groups.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { group ->
                    val isSelected = selected == group
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .background(
                                if (isSelected) PrimaryRed else White,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.5.dp,
                                if (isSelected) PrimaryRed else LightGray,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelected(group) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            group,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) White else TextMedium
                        )
                    }
                }
            }
        }
    }
}

// ─── Search Screen ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var bloodType by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf(10) }
    var results by remember { mutableStateOf(listOf<Pair<Donor, Double>>()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    val radiusOptions = listOf(10, 20)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(modifier = Modifier.fillMaxSize().background(OffWhite)) {

        // ── Red top bar ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryRed)
                .padding(horizontal = 20.dp)
                .padding(top = 44.dp, bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("← Back", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
            }
            Column(modifier = Modifier.fillMaxWidth().padding(top = 48.dp)) {
                Text("Find Blood", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = White)
                Text("Search nearby donors", fontSize = 13.sp, color = White.copy(alpha = 0.8f))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Blood group dropdown-style
            item {
                SearchCard {
                    Text("BLOOD GROUP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLight, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(10.dp))
                    // Show selected blood group in a dropdown-style pill, then a grid below
                    val displayText = if (bloodType.isEmpty()) "Select Blood Group" else bloodType
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .background(White, RoundedCornerShape(10.dp))
                            .border(1.dp, if (bloodType.isNotEmpty()) PrimaryRed else LightGray, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                displayText,
                                fontSize = 15.sp,
                                fontWeight = if (bloodType.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                color = if (bloodType.isNotEmpty()) PrimaryRed else MediumGray,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MediumGray, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    BloodGroupSelector(selected = bloodType, onSelected = { bloodType = it })
                }
            }

            // Radius
            item {
                SearchCard {
                    Text("SEARCH RADIUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLight, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        radiusOptions.forEach { km ->
                            val sel = radius == km
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .background(if (sel) PrimaryRed else White, RoundedCornerShape(10.dp))
                                    .border(1.5.dp, if (sel) PrimaryRed else LightGray, RoundedCornerShape(10.dp))
                                    .clickable { radius = km },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${km} km",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sel) White else TextMedium
                                )
                            }
                        }
                    }
                }
            }

            // Search button
            item {
                Button(
                    onClick = {
                        if (bloodType.isBlank()) {
                            Toast.makeText(context, "Please select a blood group", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            return@Button
                        }
                        isSearching = true
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { myLoc ->
                                if (myLoc == null) { isSearching = false; return@addOnSuccessListener }
                                val db = FirebaseDatabase.getInstance().getReference("donors")
                                db.get().addOnSuccessListener { snap ->
                                    val list = mutableListOf<Pair<Donor, Double>>()
                                    snap.children.forEach { child ->
                                        val d = child.getValue(Donor::class.java) ?: return@forEach
                                        if (d.bloodGroup == bloodType) {
                                            val dist = getDistanceInKm(myLoc.latitude, myLoc.longitude, d.lat, d.lng)
                                            if (dist <= radius && getDaysSince(d.lastDonationDate) >= 90) {
                                                list.add(d to dist)
                                            }
                                        }
                                    }
                                    results = list.sortedBy { it.second }
                                    hasSearched = true
                                    isSearching = false
                                }
                            }
                            .addOnFailureListener { isSearching = false }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    enabled = !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("SEARCHING...", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SEARCH NOW", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                    }
                }
            }

            // Results header
            if (hasSearched) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val found = results.isNotEmpty()
                        Icon(
                            if (found) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (found) GreenAccent else PrimaryRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (results.isEmpty()) "No donors found" else "${results.size} donor${if (results.size != 1) "s" else ""} found",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (found) GreenAccent else PrimaryRed
                        )
                        Spacer(Modifier.weight(1f))
                        Text("within ${radius}km", fontSize = 12.sp, color = TextLight)
                    }
                }
            }

            // Donor cards
            items(results) { (donor, dist) ->
                DonorCard(donor = donor, distance = dist)
            }
        }
    }
}

@Composable
fun SearchCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(14.dp))
            .border(1.dp, LightGray, RoundedCornerShape(14.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun DonorCard(donor: Donor, distance: Double) {
    val context = LocalContext.current
    val days = getDaysSince(donor.lastDonationDate)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(14.dp))
            .border(1.dp, LightGray, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        // Header row
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Initials avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(RedTint, CircleShape)
                    .border(1.5.dp, PrimaryRed.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials(donor.name), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryRed)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(donor.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(donor.city, fontSize = 12.sp, color = TextLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            // Blood group badge
            Box(
                modifier = Modifier
                    .background(PrimaryRed, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(donor.bloodGroup, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = White)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Distance + days row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryRed, modifier = Modifier.size(14.dp))
                Text(String.format("%.1f km away", distance), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            }
            Row(
                modifier = Modifier
                    .background(GreenLight, RoundedCornerShape(8.dp))
                    .border(1.dp, GreenBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(12.dp))
                Text("$days days ago", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Call button
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${donor.phone}") }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
        ) {
            Icon(Icons.Default.Call, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("CALL DONOR", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = White, letterSpacing = 1.sp)
        }
    }
}