package org.sinou.pydia.client.ui.system.models

import androidx.lifecycle.ViewModel
import org.sinou.pydia.client.core.services.PreferencesService
import kotlinx.coroutines.flow.map

/** Exposes preference state as cold flow for the various views */
class PrefReadOnlyVM(
    prefs: PreferencesService,
) : ViewModel() {

    private val cellsPreferences = prefs.cellsPreferencesFlow

    val showDebugTools = cellsPreferences.map { prefs -> prefs.showDebugTools }
}
