package org.sinou.pydia.client.core.ui.account

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.AppKeys
import org.sinou.pydia.client.core.db.accounts.RSessionView
import org.sinou.pydia.client.core.services.AuthService
import org.sinou.pydia.client.core.ui.Destinations
import org.sinou.pydia.client.core.ui.core.composables.DefaultTopBar
import org.sinou.pydia.client.core.ui.core.composables.dialogs.AskForConfirmation
import org.sinou.pydia.client.core.ui.core.encodeStateForRoute
import org.sinou.pydia.client.core.ui.core.lazyStateID
import org.sinou.pydia.client.core.ui.login.LoginDestinations
import org.sinou.pydia.sdk.transport.StateID

private const val LOG_TAG = "AccountsScreen.kt"
private const val ACCOUNT_LIST = "account-list"
private const val CONFIRM_DELETION_PREFIX = "confirm_deletion"
private const val CONFIRM_DELETION_ROUTE = "$CONFIRM_DELETION_PREFIX/{${AppKeys.STATE_ID}}"

@Composable
fun AccountsScreen(
    isExpandedScreen: Boolean,
    accountListVM: AccountListVM,
    navigateTo: (String) -> Unit,
    openDrawer: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {

    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val accounts = accountListVM.sessions.collectAsState(listOf())
    NavHost(navController, ACCOUNT_LIST) {
        composable(ACCOUNT_LIST) {  // Fills the area provided to the NavHost

            AccountsScreen(
                isExpandedScreen = isExpandedScreen,
                accounts = accounts.value,
                openAccount = {
                    scope.launch {
                        accountListVM.openSession(it)?.let {
                            Log.i(LOG_TAG, "About to open session for: $it")
                            navigateTo(Destinations.browse(it.getStateID()))
                        }
                    }
                },
                openDrawer = openDrawer,
                registerNew = {
                    navigateTo(LoginDestinations.AskUrl.createRoute())
                },
                login = { stateID, skipVerify ->
                    val route = LoginDestinations.LaunchAuthProcessing.createRoute(
                        stateID,
                        skipVerify,
                        AuthService.LOGIN_CONTEXT_ACCOUNTS
                    )

                    navigateTo(route)
                },
                logout = { accountListVM.logoutAccount(it) },
                forget = {
                    navController.navigate("$CONFIRM_DELETION_PREFIX/${encodeStateForRoute(it)}")
                },
                contentPadding = contentPadding,
            )
        }

        dialog(CONFIRM_DELETION_ROUTE) { entry ->
            val stateID = lazyStateID(entry)
            if (stateID == StateID.NONE) {
                Log.e(LOG_TAG, "... cannot navigate with no state ID")
                return@dialog
            }
            AskForConfirmation(
                icon = null,
                title = stringResource(R.string.confirm_move_to_recycle_title),
                desc = stringResource(R.string.account_confirm_deletion_desc, stateID),
                confirm = {
                    accountListVM.forgetAccount(stateID)
                    navController.popBackStack(ACCOUNT_LIST, false)
                    accountListVM.watch()

                },
                dismiss = {
                    navController.popBackStack(ACCOUNT_LIST, false)
                }
            )
        }
    }
}

@Composable
private fun AccountsScreen(
    isExpandedScreen: Boolean,
    accounts: List<RSessionView>,
    openAccount: (stateID: StateID) -> Unit,
    openDrawer: () -> Unit,
    registerNew: () -> Unit,
    login: (stateID: StateID, skipVerify: Boolean) -> Unit,
    logout: (stateID: StateID) -> Unit,
    forget: (stateID: StateID) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {

    val confirmForget: (stateID: StateID) -> Unit = {
        // TODO implement dialog validation
        forget(it)
    }

    Scaffold(
        topBar = {
            DefaultTopBar(
                title = stringResource(R.string.choose_account),
                isExpandedScreen = isExpandedScreen,
                openDrawer = if (isExpandedScreen) null else openDrawer,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { registerNew() }
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.create_account)
                )
            }
        },
        modifier = Modifier.padding(contentPadding),
        content = { innerPadding ->
            AccountList(
                accounts,
                openAccount,
                login,
                logout,
                confirmForget,
                modifier,
                innerPadding,
            )
        }
    )
}
