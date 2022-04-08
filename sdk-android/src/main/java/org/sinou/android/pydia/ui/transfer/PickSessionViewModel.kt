package org.sinou.android.pydia.ui.transfer

import androidx.lifecycle.ViewModel
import org.sinou.android.pydia.services.AccountService

/**
 * Holds a list of connected clients to choose a target destination for uploads and moves.
 */
class PickSessionViewModel(accountService: AccountService) : ViewModel() {
    val sessions = accountService.liveSessions
}
