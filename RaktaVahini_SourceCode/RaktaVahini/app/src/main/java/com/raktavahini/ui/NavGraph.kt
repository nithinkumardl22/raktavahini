package com.raktavahini.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.raktavahini.ui.screens.*
import com.raktavahini.viewmodel.MainViewModel

object Routes {
    const val HOME              = "home"
    const val REGISTER_DONOR    = "register_donor"
    const val EDIT_DONOR        = "edit_donor/{donorId}"
    const val EMERGENCY_SEARCH  = "emergency_search"
    const val SEARCH_RESULTS    = "search_results/{bloodGroup}/{location}"
    const val DONOR_PROFILE     = "donor_profile/{donorId}"
    const val LOG_DONATION      = "log_donation/{donorId}"
    const val DONATION_HISTORY  = "donation_history/{donorId}"
    const val ALL_DONORS        = "all_donors"
}

@Composable
fun RaktaVahiniNavGraph(vm: MainViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                vm = vm,
                onSearch        = { navController.navigate(Routes.EMERGENCY_SEARCH) },
                onRegister      = { navController.navigate(Routes.REGISTER_DONOR) },
                onViewAll       = { navController.navigate(Routes.ALL_DONORS) },
                onLogDonation   = { id -> navController.navigate("log_donation/$id") },
                onViewProfile   = { id -> navController.navigate("donor_profile/$id") }
            )
        }

        composable(Routes.REGISTER_DONOR) {
            RegisterDonorScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            Routes.EDIT_DONOR,
            arguments = listOf(navArgument("donorId") { type = NavType.LongType })
        ) { back ->
            val id = back.arguments!!.getLong("donorId")
            EditDonorScreen(vm = vm, donorId = id, onBack = { navController.popBackStack() })
        }

        composable(Routes.EMERGENCY_SEARCH) {
            EmergencySearchScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onResults = { group, location ->
                    val loc = location.ifBlank { "all" }
                    navController.navigate("search_results/$group/$loc")
                }
            )
        }

        composable(
            Routes.SEARCH_RESULTS,
            arguments = listOf(
                navArgument("bloodGroup") { type = NavType.StringType },
                navArgument("location")   { type = NavType.StringType }
            )
        ) { back ->
            val group    = back.arguments!!.getString("bloodGroup") ?: "O+"
            val location = back.arguments!!.getString("location").let {
                if (it == "all") "" else it ?: ""
            }
            SearchResultsScreen(
                vm         = vm,
                bloodGroup = group,
                location   = location,
                onBack     = { navController.popBackStack() }
            )
        }

        composable(
            Routes.DONOR_PROFILE,
            arguments = listOf(navArgument("donorId") { type = NavType.LongType })
        ) { back ->
            val id = back.arguments!!.getLong("donorId")
            DonorProfileScreen(
                vm = vm,
                donorId = id,
                onBack  = { navController.popBackStack() },
                onEdit  = { navController.navigate("edit_donor/$id") },
                onLog   = { navController.navigate("log_donation/$id") },
                onHistory = { navController.navigate("donation_history/$id") }
            )
        }

        composable(
            Routes.LOG_DONATION,
            arguments = listOf(navArgument("donorId") { type = NavType.LongType })
        ) { back ->
            val id = back.arguments!!.getLong("donorId")
            LogDonationScreen(
                vm = vm,
                donorId = id,
                onBack  = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            Routes.DONATION_HISTORY,
            arguments = listOf(navArgument("donorId") { type = NavType.LongType })
        ) { back ->
            val id = back.arguments!!.getLong("donorId")
            DonationHistoryScreen(vm = vm, donorId = id, onBack = { navController.popBackStack() })
        }

        composable(Routes.ALL_DONORS) {
            AllDonorsScreen(vm = vm, onBack = { navController.popBackStack() },
                onDonorClick = { id -> navController.navigate("donor_profile/$id") })
        }
    }
}
