package org.sinou.pydia.client.ui.system.models

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.sinou.pydia.client.core.ListType
import org.sinou.pydia.client.core.services.PreferencesService
import org.sinou.pydia.client.ui.core.ListLayout
import kotlinx.coroutines.launch

/** Expose methods used to perform house keeping on the App */
class SettingsVM(
    private val prefs: PreferencesService,
    // private val nodeService: NodeService
) : ViewModel() {

    // private val logTag = "SettingsVM"

    val cellsPreferences = prefs.cellsPreferencesFlow

    fun setShowRuntimeToolsFlag(show: Boolean) {
        viewModelScope.launch {
            prefs.setShowDebugToolsFlag(show)
        }
    }

    fun setDisablePollFlag(disablePoll: Boolean) {
        viewModelScope.launch {
            prefs.setDisablePollFlag(disablePoll)
        }
    }

    fun setDefaultOrder(order: String) {
        viewModelScope.launch {
            prefs.setOrder(ListType.DEFAULT, order)
        }
    }

    fun setListLayout(layoutStr: String) {
        // Log.e(logTag, "About to set list layout to $layoutStr -- ${ListLayout.GRID.name}")
        val layout: ListLayout = if (ListLayout.GRID.name == layoutStr) {
            ListLayout.GRID
        } else
            ListLayout.LIST
        viewModelScope.launch {
            prefs.setListLayout(layout)
        }
    }

    fun setBooleanFlag(key: Preferences.Key<Boolean>, flag: Boolean) {
        viewModelScope.launch {
            prefs.setBoolean(key, flag)
        }
    }

    fun setStringPref(key: Preferences.Key<String>, strValue: String) {
        viewModelScope.launch {
            prefs.setString(key, strValue)
        }
    }
}
