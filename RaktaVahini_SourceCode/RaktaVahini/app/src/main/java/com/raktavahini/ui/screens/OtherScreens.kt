package com.raktavahini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktavahini.data.entities.DonationLogEntity
import com.raktavahini.data.entities.DonorEntity
import com.raktavahini.ui.theme.*
import com.raktavahini.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

// ══════════════════════════════════════════════════════
// LOG DONATION SCREEN
// ══════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDonationScreen(
    vm: MainViewModel,
    donorId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val logSuccess by vm.logSuccess.collectAsState()
    val donor      by vm.myProfile.observeAsState()

    var hospital      by remember { mutableStateOf("") }
    var city          by remember { mutableStateOf(donor?.city ?: "") }
    var units         by remember { mutableStateOf("1") }
    var recipientNote by remember { mutableStateOf("") }
    var error         by remember { mutableStateOf("") }

    LaunchedEffect(donor) { if (city.isBlank()) city = donor?.city ?: "" }
    LaunchedEffect(logSuccess) {
        if (logSuccess) { vm.clearLogSuccess(); onSaved() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log a Donation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BloodRed, titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header card
            Card(
                colors = CardDefaults.cardColors(containerColor = LightRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🩸", fontSize = 30.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Record a Blood Donation", fontWeight = FontWeight.Bold, color = BloodRed)
                        Text(
                            "After saving, you will be marked ineligible for 90 days " +
                            "and receive a Thank You notification.",
                            fontSize = 12.sp, color = Color.Gray
                        )
                    }
                }
            }

            OutlinedTextField(
                value = hospital,
                onValueChange = { hospital = it },
                label = { Text("Hospital / Blood Bank *") },
                leadingIcon = { Icon(Icons.Default.LocalHospital, null, tint = BloodRed) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City *") },
                leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = BloodRed) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = units,
                onValueChange = { if (it.length <= 1 && it.all { c -> c.isDigit() }) units = it },
                label = { Text("Units Donated") },
                leadingIcon = { Icon(Icons.Default.Bloodtype, null, tint = BloodRed) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = recipientNote,
                onValueChange = { recipientNote = it },
                label = { Text("Note (optional)") },
                placeholder = { Text("e.g. Emergency surgery patient") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (error.isNotBlank()) {
                Text(error, color = BloodRed, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    when {
                        hospital.isBlank() -> error = "Hospital name is required"
                        city.isBlank()     -> error = "City is required"
                        else -> {
                            error = ""
                            vm.logDonation(
                                donorId      = donorId,
                                hospital     = hospital.trim(),
                                city         = city.trim(),
                                units        = units.toIntOrNull() ?: 1,
                                recipientNote = recipientNote.trim(),
                                donorName    = donor?.name ?: "Donor"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Favorite, null)
                Spacer(Modifier.width(8.dp))
                Text("Save Donation Record", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// DONATION HISTORY SCREEN
// ══════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationHistoryScreen(
    vm: MainViewModel,
    donorId: Long,
    onBack: () -> Unit
) {
    val logs   by vm.getDonationLog(donorId).observeAsState(emptyList())
    val total  by vm.getTotalDonations(donorId).observeAsState(0)
    val dateFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donation History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BloodRed, titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Total badge
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(LightRed)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🩸", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "$total Total Donation${if (total != 1) "s" else ""}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = BloodRed
                        )
                        Text("Every donation saves up to 3 lives", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            if (logs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💉", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No donations logged yet.", color = Color.Gray)
                        Text("Tap 'Log Donation' to record your first donation.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(logs, key = { it.logId }) { log ->
                        DonationLogCard(log = log, dateFmt = dateFmt)
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun DonationLogCard(log: DonationLogEntity, dateFmt: SimpleDateFormat) {
    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(LightRed, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🩸", fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(log.hospital, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(log.city, fontSize = 12.sp, color = Color.Gray)
                if (log.recipientNote.isNotBlank())
                    Text(log.recipientNote, fontSize = 11.sp, color = Color.Gray)
                Text(
                    dateFmt.format(Date(log.donationDate)),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${log.unitsGiven} unit${if (log.unitsGiven > 1) "s" else ""}",
                    fontWeight = FontWeight.Bold,
                    color = BloodRed,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// ALL DONORS SCREEN
// ══════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDonorsScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onDonorClick: (Long) -> Unit
) {
    val donors       by vm.allDonors.observeAsState(emptyList())
    var searchQuery  by remember { mutableStateOf("") }
    var filterGroup  by remember { mutableStateOf("All") }

    val groups = listOf("All") + listOf("A+","A-","B+","B-","AB+","AB-","O+","O-")

    val filtered = donors
        .filter { d ->
            (filterGroup == "All" || d.bloodGroup == filterGroup) &&
            (searchQuery.isBlank() || d.name.contains(searchQuery, ignoreCase = true) ||
             d.city.contains(searchQuery, ignoreCase = true))
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Donors (${donors.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BloodRed, titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or city…") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = BloodRed) },
                trailingIcon = {
                    if (searchQuery.isNotBlank())
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(30.dp)
            )

            // Group filter chips
            androidx.compose.foundation.lazy.LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(groups) { g ->
                    FilterChip(
                        selected = filterGroup == g,
                        onClick  = { filterGroup = g },
                        label    = { Text(g, fontSize = 12.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BloodRed,
                            selectedLabelColor     = Color.White
                        )
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No donors found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.donorId }) { donor ->
                        DonorListCard(
                            donor = donor,
                            isEligible = vm.isDonorEligible(donor.lastDonationDate) && donor.isManuallyEligible,
                            onClick = { onDonorClick(donor.donorId) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun DonorListCard(donor: DonorEntity, isEligible: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BloodGroupBadge(donor.bloodGroup, size = 46)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(donor.name, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(donor.city, fontSize = 12.sp, color = Color.Gray)
            }
            Box(
                Modifier
                    .background(
                        if (isEligible) SafeGreenLight else LightRed,
                        RoundedCornerShape(30.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isEligible) "✅ Ready" else "⏳ Waiting",
                    fontSize = 11.sp,
                    color = if (isEligible) SafeGreen else BloodRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
