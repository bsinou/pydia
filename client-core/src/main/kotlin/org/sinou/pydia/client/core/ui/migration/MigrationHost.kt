package org.sinou.pydia.client.core.ui.migration

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.sinou.pydia.client.core.ui.system.screens.AfterLegacyMigration
import org.sinou.pydia.client.core.ui.system.screens.MigrateFromV2
import org.sinou.pydia.client.core.ui.system.screens.PrepareMigration
import kotlinx.coroutines.launch

private const val LOG_TAG = "MigrationHost.kt"

private sealed class Destinations(val route: String) {
    object PrepareMigration : Destinations("prepare-migration")
    object MigrateFromV2 : Destinations("migrate-from-v2")
    object AfterLegacyMigration : Destinations("after-legacy-migration")
    object AfterMigrationError : Destinations("after-migration-error")
}

@Composable
fun MigrationHost(
    navController: NavHostController,
    migrationVM: MigrationVM,
    afterMigration: () -> Unit,
) {

    val ctx = LocalContext.current
//    val oldVersion = migrationVM.getOldVersion(ctx)

    val currDestination = migrationVM.currDestination.collectAsState()


    LaunchedEffect(currDestination.value) {
        Log.d(LOG_TAG, "In launch effect for ${currDestination.value}")

        val newDestination = when (currDestination.value) {
            Step.MIGRATING_FROM_V2 -> Destinations.MigrateFromV2.route
            Step.AFTER_LEGACY_MIGRATION -> Destinations.AfterLegacyMigration.route
            Step.AFTER_MIGRATION_ERROR -> Destinations.AfterMigrationError.route
            else -> null
        }

        newDestination?.let {
            Log.i(LOG_TAG, "Got a newDestination: $it, navigating.")
            navController.navigate(it)
        } ?: run {// handle corner cases
            if (currDestination.value == Step.NOT_NEEDED) {
                Log.i(LOG_TAG, "No migration needed, terminating.")
                afterMigration()
            }
        }
    }

    LaunchedEffect(true) {
        migrationVM.migrate(ctx)
    }

    val scope = rememberCoroutineScope()
    fun launchSync() {
        scope.launch {
            migrationVM.launchSync()
            afterMigration()
        }
    }

    /* Configure navigation */
    NavHost(
        navController = navController,
        startDestination = Destinations.PrepareMigration.route, // MigrationDestination.MigrateFromV2.route
    ) {

        composable(Destinations.PrepareMigration.route) {
            PrepareMigration()
        }

        composable(Destinations.MigrateFromV2.route) {
            val currJob = migrationVM.migrationJob.collectAsState(null)
            val oldVersion = migrationVM.versionCode.collectAsState(initial = -1)
            val d = (currJob.value?.progress ?: 0f).toFloat()
            val n = (currJob.value?.total ?: 1f).toFloat()
            MigrateFromV2(oldVersion.value, currJob.value?.progressMessage ?: "-", d, n)
        }

        composable(Destinations.AfterLegacyMigration.route) {
            AfterLegacyMigration(
                offlineRootNb = migrationVM.rootNb,
                browse = { afterMigration() },
                launchSyncAndBrowse = { launchSync() },
            )
        }

        composable(Destinations.AfterMigrationError.route) {
            Text("Unexpected error:\n  Could not start migration process.")
        }
    }
}
