# Rakta-Vahini — Android Source Code
## ರಕ್ತ-ವಾಹಿನಿ | Filtered Blood Donor Network

---

## ⚡ Setup in Android Studio (3 Steps)

### Step 1 — Open the project
1. Open **Android Studio** (Ladybug 2024 or newer)
2. Click **File → Open** → select the `RaktaVahini` folder
3. Wait for Gradle sync (~2 minutes first time)

### Step 2 — Sync & Build
- Click **"Sync Now"** if prompted
- **Build → Make Project** (Ctrl+F9)

### Step 3 — Run
- Connect Android phone (USB Debugging ON) or start an AVD
- Click ▶ Run

---

## 📁 Project Structure

```
RaktaVahini/
└── app/src/main/java/com/raktavahini/
    ├── MainActivity.kt
    ├── data/
    │   ├── entities/Entities.kt        ← DonorEntity, DonationLogEntity, BloodGroup enum
    │   ├── dao/RaktaVahiniDao.kt       ← All DB queries
    │   ├── database/RaktaVahiniDatabase.kt
    │   └── repository/RaktaVahiniRepository.kt
    ├── viewmodel/MainViewModel.kt      ← 90-day eligibility logic, search, log donation
    ├── utils/
    │   ├── NotificationHelper.kt       ← FR-05 Thank You notification
    │   └── IntentHelper.kt             ← FR-04 Intent.ACTION_DIAL, WhatsApp, Share
    └── ui/
        ├── theme/Theme.kt              ← Deep red / white / gold brand colours
        ├── NavGraph.kt                 ← All navigation routes
        └── screens/
            ├── HomeScreen.kt           ← Dashboard, blood group grid, my status card
            ├── SearchScreens.kt        ← EmergencySearchScreen + SearchResultsScreen
            ├── DonorFormScreens.kt     ← Register + Edit donor
            ├── DonorProfileScreen.kt   ← My profile with stats + eligibility toggle
            └── OtherScreens.kt         ← LogDonation, DonationHistory, AllDonors
```

---

## ✅ All 6 Functional Requirements Implemented

| FR | Feature | Where |
|----|---------|-------|
| FR-01 | Donor Registration | DonorFormScreens.kt |
| FR-02 | Emergency Search with 90-day filter | SearchScreens.kt + MainViewModel |
| FR-03 | Eligibility Toggle switch | HomeScreen.kt + DonorProfileScreen.kt |
| FR-04 | Secure Calling via Intent.ACTION_DIAL | IntentHelper.kt |
| FR-05 | Thank You notification after log | NotificationHelper.kt |
| + | Compatible blood group matching | Entities.kt (COMPATIBLE_DONORS map) |

---

## 🔑 Core Eligibility Logic

```kotlin
// MainViewModel.kt
fun isDonorEligible(lastDonationDateMs: Long): Boolean {
    if (lastDonationDateMs == 0L) return true  // never donated
    val diffDays = TimeUnit.MILLISECONDS.toDays(
        System.currentTimeMillis() - lastDonationDateMs
    )
    return diffDays > 90  // 90-day rule
}
```

---

## 🩸 Features

- **Blood group compatibility** — AB+ recipients see all 8 groups; O- sees only O-
- **90-day automatic hiding** — ineligible donors never appear in search results
- **Manual toggle** — donors can mark themselves unavailable (travel, illness)
- **Privacy-first calling** — phone shown only at dial-time, not in search list
- **Thank You notification** — fires immediately after logging a donation (FR-05)
- **Donation history** — full log with hospital, city, date, units
- **Emergency share** — one tap to broadcast blood request on WhatsApp

---

## 🔧 Troubleshooting

**"Cannot find symbol RaktaVahiniDatabase_Impl"**  
→ Go to **Build → Rebuild Project** (KSP needs to run first)

**Notification not showing**  
→ On Android 13+, grant POST_NOTIFICATIONS permission in device Settings

**Search shows no results**  
→ Register a few donors first using the Register screen, then search
