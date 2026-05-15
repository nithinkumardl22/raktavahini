package com.raktavahini.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktavahini.data.entities.BloodGroup
import com.raktavahini.data.entities.EligibleDonor
import com.raktavahini.ui.theme.*
import com.raktavahini.utils.IntentHelper
import com.raktavahini.viewmodel.MainViewModel

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN 1 — Emergency Search (blood group + optional city picker)
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencySearchScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onResults: (bloodGroup: String, location: String) -> Unit
) {
    var selectedGroup by remember { mutableStateOf("") }
    var location      by remember { mutableStateOf("") }
    var groupError    by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Search", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BloodRed, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Urgency banner
            Card(colors = CardDefaults.cardColors(containerColor = LightRed), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🚨", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Find Eligible Donors", fontWeight = FontWeight.Bold, color = BloodRed)
                        Text("Only donors who are medically eligible (90-day rule) will appear.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Text("Required Blood Group *", fontWeight = FontWeight.SemiBold)
            if (groupError) Text("Please select a blood group", color = BloodRed, fontSize = 12.sp)

            // Blood group grid selector
            val groups = BloodGroup.values()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groups.toList().chunked(4).forEach { rowGroups ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowGroups.forEach { bg ->
                            val selected = selectedGroup == bg.display
                            Button(
                                onClick = { selectedGroup = bg.display; groupError = false },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) BloodRed else LightRed,
                                    contentColor   = if (selected) Color.White else BloodRed
                                )
                            ) {
                                Text(bg.display, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }
                        repeat(4 - rowGroups.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("City or Pincode (optional)") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = BloodRed) },
                placeholder = { Text("e.g. Mysuru, 570001") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (selectedGroup.isBlank()) { groupError = true; return@Button }
                    onResults(selectedGroup, location.trim())
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(8.dp))
                Text("Find Eligible Donors", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN 2 — Search Results
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    vm: MainViewModel,
    bloodGroup: String,
    location: String,
    onBack: () -> Unit
) {
    val context        = LocalContext.current
    val results        by vm.searchResults.collectAsState()
    val isSearching    by vm.isSearching.collectAsState()
    val searchDone     by vm.searchDone.collectAsState()

    var shareDialogOpen by remember { mutableStateOf(false) }
    var hospital        by remember { mutableStateOf("") }

    // Trigger search on first compose
    LaunchedEffect(bloodGroup, location) {
        vm.searchDonors(bloodGroup, location)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Eligible Donors", fontWeight = FontWeight.Bold)
                        Text("For $bloodGroup ${if (location.isNotBlank()) "· $location" else ""}",
                            fontSize = 12.sp, color = Color.White.copy(.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { vm.clearSearch(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { shareDialogOpen = true }) {
                        Icon(Icons.Default.Share, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BloodRed, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {

            // Result count banner
            if (searchDone) {
                Box(
                    Modifier.fillMaxWidth()
                        .background(if (results.isNotEmpty()) SafeGreenLight else LightRed)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        if (results.isNotEmpty())
                            "✅ ${results.size} eligible donor${if (results.size > 1) "s" else ""} found"
                        else
                            "❌ No eligible donors found for $bloodGroup${if (location.isNotBlank()) " in $location" else ""}",
                        color = if (results.isNotEmpty()) SafeGreen else BloodRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            when {
                isSearching -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = BloodRed)
                            Spacer(Modifier.height(12.dp))
                            Text("Searching eligible donors…", color = Color.Gray)
                        }
                    }
                }
                searchDone && results.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Text("🩸", fontSize = 56.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("No eligible donors found", fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
                            Text("All donors of this type have donated recently or are unavailable.\nTry expanding your search area.",
                                color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                            Spacer(Modifier.height(20.dp))
                            Button(onClick = { shareDialogOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BloodRed)) {
                                Icon(Icons.Default.Share, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Share Emergency Request")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(results, key = { it.donorId }) { donor ->
                            EligibleDonorCard(
                                donor = donor,
                                onCall = { IntentHelper.dialDonor(context, donor.phoneNumber) },
                                onWhatsApp = { IntentHelper.whatsappDonor(context, donor.phoneNumber, bloodGroup) }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    // Share emergency request dialog
    if (shareDialogOpen) {
        AlertDialog(
            onDismissRequest = { shareDialogOpen = false },
            title = { Text("Share Emergency Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter hospital name to include in the request:")
                    OutlinedTextField(
                        value = hospital,
                        onValueChange = { hospital = it },
                        label = { Text("Hospital Name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    IntentHelper.shareEmergencyRequest(context, bloodGroup, location, hospital)
                    shareDialogOpen = false
                }, colors = ButtonDefaults.buttonColors(containerColor = BloodRed)) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { shareDialogOpen = false }) { Text("Cancel") }
            }
        )
    }
}

// ── Eligible donor card ───────────────────────────────────────────────────────
@Composable
fun EligibleDonorCard(
    donor: EligibleDonor,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BloodGroupBadge(donor.bloodGroup, size = 50)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(donor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(donor.city + if (donor.pincode.isNotBlank()) " — ${donor.pincode}" else "",
                        fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    // Eligibility info
                    val daysText = if (donor.daysSinceLastDonation < 0) "Never donated"
                        else "Last donated ${donor.daysSinceLastDonation} days ago"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.clip(RoundedCornerShape(30.dp))
                                .background(SafeGreenLight)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("✅ Eligible", fontSize = 10.sp, color = SafeGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(daysText, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = LightRed)
            Spacer(Modifier.height(10.dp))

            // Action buttons — phone number NOT shown publicly (FR-04)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BloodRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Phone, null, tint = BloodRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Call", color = BloodRed, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onWhatsApp,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
