package com.raktavahini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktavahini.ui.theme.*
import com.raktavahini.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorProfileScreen(
    vm: MainViewModel,
    donorId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onLog: () -> Unit,
    onHistory: () -> Unit
) {
    val donor         by vm.myProfile.observeAsState()
    val totalDonations by vm.getTotalDonations(donorId).observeAsState(0)
    val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val isEligible = donor?.let {
        vm.isDonorEligible(it.lastDonationDate) && it.isManuallyEligible
    } ?: false

    val daysLeft = donor?.let { vm.daysUntilEligible(it.lastDonationDate) } ?: 0
    val daysSince = donor?.let { vm.daysSinceDonation(it.lastDonationDate) } ?: -1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Donor Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BloodRed, titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (donor == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BloodRed)
            }
            return@Scaffold
        }

        val d = donor!!

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero header ────────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DeepRed, BloodRed)))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Large blood group circle
                    Box(
                        Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            d.bloodGroup,
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(d.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(d.city, color = Color.White.copy(.8f), fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))

                    // Eligibility badge
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(if (isEligible) Color(0xFF1B5E20) else Color.White.copy(.2f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (isEligible) "✅ Eligible to Donate"
                            else "⏳ Eligible in $daysLeft days",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Stats row ──────────────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Donations",
                    value = "$totalDonations",
                    icon = "🩸"
                )
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Days Since Last",
                    value = if (daysSince < 0) "Never" else "$daysSince",
                    icon = "📅"
                )
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Days Until Eligible",
                    value = if (isEligible) "Ready!" else "$daysLeft",
                    icon = "⏱"
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Info card ──────────────────────────────────────────────────────
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Donor Information", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    HorizontalDivider()

                    InfoRow(Icons.Default.Bloodtype, "Blood Group", d.bloodGroup)
                    InfoRow(Icons.Default.Phone, "Phone", d.phoneNumber)
                    InfoRow(Icons.Default.LocationCity, "City", d.city)
                    if (d.pincode.isNotBlank())
                        InfoRow(Icons.Default.PinDrop, "Pincode", d.pincode)
                    InfoRow(
                        Icons.Default.CalendarToday,
                        "Last Donation",
                        if (d.lastDonationDate > 0L)
                            dateFmt.format(Date(d.lastDonationDate))
                        else "Never donated"
                    )
                    InfoRow(
                        Icons.Default.AppRegistration,
                        "Member Since",
                        dateFmt.format(Date(d.registeredAt))
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Eligibility toggle ─────────────────────────────────────────────
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (d.isManuallyEligible) SafeGreenLight else LightRed
                )
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "I am Available to Donate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            if (d.isManuallyEligible)
                                "You will appear in emergency search results"
                            else
                                "You are hidden from search results",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = d.isManuallyEligible,
                        onCheckedChange = { vm.toggleEligibility(d.donorId, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor  = SafeGreen,
                            checkedTrackColor  = SafeGreenLight
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Action buttons ─────────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onHistory,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("History")
                }
                Button(
                    onClick = onLog,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Log Donation")
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = BloodRed, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
