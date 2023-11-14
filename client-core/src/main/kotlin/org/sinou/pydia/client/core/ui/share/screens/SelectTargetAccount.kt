package org.sinou.pydia.client.core.ui.share.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.sinou.pydia.client.core.AppNames
import org.sinou.pydia.client.core.ui.account.AccountListVM
import org.sinou.pydia.client.core.ui.share.ShareHelper
import org.sinou.pydia.client.core.ui.share.composables.TargetAccountList
import org.sinou.pydia.sdk.transport.StateID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SelectTargetAccount(
    helper: ShareHelper,
    accountListVM: AccountListVM = koinViewModel()
) {
    val scope = rememberCoroutineScope()

    SelectTargetAccount(
        accountListVM = accountListVM,
        openAccount = { scope.launch { helper.open(it) } },
        cancel = { scope.launch { helper.launchTaskFor(AppNames.ACTION_CANCEL, StateID.NONE) } },
        login = { s, skip, legacy -> scope.launch { helper.login(s, skip, legacy) } },
    )

    DisposableEffect(key1 = true) {
        accountListVM.watch()
        onDispose { accountListVM.pause() }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectTargetAccount(
    accountListVM: AccountListVM,
    openAccount: (StateID) -> Unit,
    cancel: () -> Unit,
    login: (StateID, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {

    val accounts = accountListVM.sessions.collectAsState(listOf())

    val interceptOpen: (stateID: StateID) -> Unit = {
        accountListVM.pause()
        openAccount(it)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Choose an account",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { cancel() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            TargetAccountList(
                accounts.value,
                interceptOpen,
                login,
                modifier,
                innerPadding,
            )
        }
    )
}
