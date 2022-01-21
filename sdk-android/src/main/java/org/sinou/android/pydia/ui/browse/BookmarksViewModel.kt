package org.sinou.android.pydia.ui.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.CellsApp

/**
 * Holds a live list of the cached bookmarks for the current session
 */
class BookmarksViewModel(
    val stateID: StateID,
    application: Application
) : AndroidViewModel(application) {

    val bookmarks = CellsApp.instance.nodeService.listBookmarks(stateID)

    class BookmarksViewModelFactory(
        private val stateID: StateID,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BookmarksViewModel::class.java)) {
                return BookmarksViewModel(stateID, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
