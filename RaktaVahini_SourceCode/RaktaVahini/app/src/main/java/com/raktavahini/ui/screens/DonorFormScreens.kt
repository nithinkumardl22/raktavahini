package com.raktavahini.ui.screens

import androidx.compose.foundation.layout.*
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
import com.raktavahini.data.entities.BloodGroup
import com.raktavahini.data.entities.DonorEntity
import com.raktavahini.ui.theme.*
import com.raktavahini.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDonorScreen(vm: MainViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    DonorFormScreen(
        vm          = vm,
        title       = "Register as Donor",
        existing    = null,
        onBack      = onBack,
        onSaved     = onSaved
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDonorScreen(vm: MainViewModel, donorId: Long, onBack: () -> Unit) {
    val donor by vm.myProfile.observeAsState()
    if (donor != null) {
        DonorFormScreen(vm = vm, title = "Edit Profile", existing = donor, onBack = onBack, onSaved = onBack)
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BloodRed)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorFormScreen(
    vm: MainViewModel,
    title: String,
    existing: DonorEntity?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val saveSuccess by vm.saveSuccess.collectAsState()

    var name          by remember { mutableStateOf(existing?.name ?: "") }
    var phone         by remember { mutableStateOf(existing?.phoneNumber ?: "") }
    var city          by remember { mutableStateOf(existing?.city ?: "") }
    var pincode       by remember { mutableStateOf(existing?.pincode ?: "") }
    var selectedGroup by remember { mutableStateOf(existing?.bloodGroup ?: "") }
    var lastDonation  by remember { mutableStateOf(
        if ((existing?.lastDonationDate ?: 0L) > 0L)
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(existing!!.lastDonationDate))
        else ""
    ) }
    var errors        by remember { mutableStateOf(mapOf<String,String>()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMs by remember { mutableStateOf(existing?.lastDonationDate ?: 0L) }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) { vm.clearSaveSuccess(); onSaved() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BloodRed, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Name
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Full Name *") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = BloodRed) },
                isError = errors["name"] != null,
                supportingText = { errors["name"]?.let { Text(it, color = BloodRed) } },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Phone
            OutlinedTextField(
                value = phone, onValueChange = { if (it.length <= 10) phone = it },
                label = { Text("Phone Number *") },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = BloodRed) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = errors["phone"] != null,
                supportingText = { errors["phone"]?.let { Text(it, color = BloodRed) } },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Blood Group
            Text("Blood Group *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            if (errors["group"] != null) Text(errors["group"]!!, color = BloodRed, fontSize = 12.sp)

            val groups = BloodGroup.values()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groups.toList().chunked(4).forEach { rowGroups ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowGroups.forEach { bg ->
                            val sel = selectedGroup == bg.display
                            Button(
                                onClick = { selectedGroup = bg.display },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sel) BloodRed else LightRed,
                                    contentColor   = if (sel) Color.White else BloodRed
                                )
                            ) { Text(bg.display, fontWeight = FontWeight.ExtraBold) }
                        }
                        repeat(4 - rowGroups.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            // City
            OutlinedTextField(
                value = city, onValueChange = { city = it },
                label = { Text("City *") },
                leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = BloodRed) },
                isError = errors["city"] != null,
                supportingText = { errors["city"]?.let { Text(it, color = BloodRed) } },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Pincode
            OutlinedTextField(
                value = pincode, onValueChange = { if (it.length <= 6) pincode = it },
                label = { Text("Pincode") },
                leadingIcon = { Icon(Icons.Default.PinDrop, null, tint = BloodRed) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Last donation date
            OutlinedTextField(
                value = lastDonation,
                onValueChange = {},
                label = { Text("Last Donation Date (dd/MM/yyyy)") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = BloodRed) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.EditCalendar, null, tint = BloodRed)
                    }
                },
                placeholder = { Text("Leave blank if never donated") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Privacy note
            Card(colors = CardDefaults.cardColors(containerColor = LightRed), shape = RoundedCornerShape(10.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = BloodRed, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Your phone number is only revealed to requestors when you choose to respond. It is not shown on public search screens.",
                        fontSize = 11.sp, color = BloodRed)
                }
            }

            Button(
                onClick = {
                    val e = mutableMapOf<String, String>()
                    if (name.isBlank()) e["name"] = "Name is required"
                    if (phone.length != 10) e["phone"] = "Enter a valid 10-digit number"
                    if (selectedGroup.isBlank()) e["group"] = "Please select your blood group"
                    if (city.isBlank()) e["city"] = "City is required"
                    errors = e
                    if (e.isEmpty()) {
                        if (existing == null) {
                            vm.registerDonor(name.trim(), phone.trim(), selectedGroup, city.trim(), pincode.trim(), selectedDateMs)
                        } else {
                            vm.updateDonor(existing.copy(name = name.trim(), phoneNumber = phone.trim(),
                                bloodGroup = selectedGroup, city = city.trim(), pincode = pincode.trim(),
                                lastDonationDate = selectedDateMs))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text(if (existing == null) "Register as Donor" else "Save Changes",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (selectedDateMs > 0) selectedDateMs else null
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        selectedDateMs = ms
                        lastDonation = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ms))
                    }
                    showDatePicker = false
                }) { Text("OK", color = BloodRed) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}
