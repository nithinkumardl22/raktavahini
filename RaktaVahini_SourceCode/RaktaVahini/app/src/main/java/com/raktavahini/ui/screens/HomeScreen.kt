package com.raktavahini.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktavahini.data.entities.BloodGroup
import com.raktavahini.ui.theme.*
import com.raktavahini.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    onSearch: () -> Unit,
    onRegister: () -> Unit,
    onViewAll: () -> Unit,
    onLogDonation: (Long) -> Unit,
    onViewProfile: (Long) -> Unit
) {
    val myProfile      by vm.myProfile.observeAsState()
    val totalDonors    by vm.totalDonorCount.observeAsState(0)
    val bgStats        by vm.bloodGroupStats.observeAsState(emptyMap())
    val allDonors      by vm.allDonors.observeAsState(emptyList())

    val isEligible = myProfile?.let {
        vm.isDonorEligible(it.lastDonationDate) && it.isManuallyEligible
    } ?: false

    val daysLeft = myProfile?.let { vm.daysUntilEligible(it.lastDonationDate) } ?: 0

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = true,  onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = false, onClick = onSearch, icon = { Icon(Icons.Default.Search, null) }, label = { Text("Search") })
                NavigationBarItem(selected = false, onClick = onViewAll, icon = { Icon(Icons.Default.People, null) }, label = { Text("Donors") })
                NavigationBarItem(selected = false, onClick = { myProfile?.let { onViewProfile(it.donorId) } ?: onRegister() },
                    icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Gradient header ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DeepRed, BloodRed)))
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ರಕ್ತ-ವಾಹಿನಿ", color = Color.White.copy(.7f), fontSize = 12.sp)
                            Text("Rakta-Vahini", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Filtered Blood Donor Network", color = Color.White.copy(.8f), fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🩸", fontSize = 24.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // EMERGENCY SEARCH CTA
                    Button(
                        onClick = onSearch,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Search, null, tint = BloodRed)
                        Spacer(Modifier.width(8.dp))
                        Text("EMERGENCY BLOOD SEARCH", color = BloodRed, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── My donor status card ───────────────────────────────────────────
            if (myProfile != null) {
                val donor = myProfile!!
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEligible) SafeGreenLight else LightRed
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        BloodGroupBadge(donor.bloodGroup, size = 52)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(donor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                if (isEligible) "✅ Eligible to donate"
                                else "⏳ Eligible in $daysLeft days",
                                fontSize = 13.sp,
                                color = if (isEligible) SafeGreen else BloodRed,
                                fontWeight = FontWeight.Medium
                            )
                            Text(donor.city, fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Switch(
                                checked = donor.isManuallyEligible,
                                onCheckedChange = { vm.toggleEligibility(donor.donorId, it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SafeGreen, checkedTrackColor = SafeGreenLight)
                            )
                            Text("Available", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    // Action buttons
                    Row(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onViewProfile(donor.donorId) },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, BloodRed)
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = BloodRed)
                            Spacer(Modifier.width(4.dp))
                            Text("Profile", color = BloodRed, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onLogDonation(donor.donorId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                        ) {
                            Icon(Icons.Default.Favorite, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Log Donation", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Not registered — show CTA
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).clickable(onClick = onRegister),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightRed),
                    border = BorderStroke(1.dp, BloodRed)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonAdd, null, tint = BloodRed, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Register as a Donor", fontWeight = FontWeight.Bold, color = BloodRed)
                            Text("Your blood group can save a life today", fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = BloodRed)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Stats row ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMiniCard(modifier = Modifier.weight(1f), label = "Registered Donors", value = "$totalDonors", icon = "👥")
                StatMiniCard(modifier = Modifier.weight(1f), label = "Blood Groups", value = "8", icon = "🩸")
                StatMiniCard(modifier = Modifier.weight(1f), label = "Your City", value = myProfile?.city ?: "—", icon = "📍")
            }

            Spacer(Modifier.height(16.dp))

            // ── Blood group grid ───────────────────────────────────────────────
            Text(
                "Blood Group Directory",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            val groups = BloodGroup.values()
            Column(modifier = Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groups.toList().chunked(4).forEach { rowGroups ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowGroups.forEach { bg ->
                            val count = bgStats[bg.display] ?: 0
                            BloodGroupGridCell(
                                group = bg.display,
                                count = count,
                                modifier = Modifier.weight(1f),
                                onClick = onSearch
                            )
                        }
                        // Fill remaining cells if row is not full
                        repeat(4 - rowGroups.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Recent donors ──────────────────────────────────────────────────
            if (allDonors.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Donors", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    TextButton(onClick = onViewAll) { Text("View all", color = BloodRed) }
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allDonors.take(6)) { donor ->
                        DonorMiniCard(
                            name = donor.name,
                            group = donor.bloodGroup,
                            city = donor.city,
                            isEligible = vm.isDonorEligible(donor.lastDonationDate) && donor.isManuallyEligible,
                            onClick = { onViewProfile(donor.donorId) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun BloodGroupBadge(group: String, size: Int = 44) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(BloodRed),
        contentAlignment = Alignment.Center
    ) {
        Text(group, color = Color.White, fontWeight = FontWeight.ExtraBold,
            fontSize = (size * 0.28f).sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun StatMiniCard(modifier: Modifier, label: String, value: String, icon: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 22.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BloodRed)
            Text(label, fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun BloodGroupGridCell(group: String, count: Int, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (count > 0) LightRed else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(group, fontWeight = FontWeight.ExtraBold, color = BloodRed, fontSize = 16.sp)
            Text("$count donors", fontSize = 9.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DonorMiniCard(name: String, group: String, city: String, isEligible: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(14.dp), modifier = Modifier.width(130.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            BloodGroupBadge(group, size = 44)
            Spacer(Modifier.height(6.dp))
            Text(name.split(" ").first(), fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1)
            Text(city, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(30.dp))
                .background(if (isEligible) SafeGreenLight else LightRed)
                .padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text(if (isEligible) "✅ Ready" else "⏳ Waiting",
                    fontSize = 9.sp, color = if (isEligible) SafeGreen else BloodRed, fontWeight = FontWeight.Medium)
            }
        }
    }
}
