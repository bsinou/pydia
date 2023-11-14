package org.sinou.pydia.client.core.ui.search

import android.content.Context
import android.util.Log
import androidx.navigation.NavHostController
import org.sinou.pydia.client.core.ListContext
import org.sinou.pydia.client.core.ui.browse.BrowseDestinations
import org.sinou.pydia.client.core.ui.browse.BrowseHelper
import org.sinou.pydia.sdk.transport.StateID

class SearchHelper(
    private val navController: NavHostController,
    private val searchVM: SearchVM,
) : BrowseHelper(navController, searchVM) {

    private val logTag = "SearchHelper"

    suspend fun openParentLocation(stateID: StateID) {
        val parent = stateID.parent()
        searchVM.getNode(parent)?.let {
            navController.navigate(
                BrowseDestinations.Open.createRoute(parent)
            )
        } ?: run {
            if (searchVM.retrieveFolder(parent)) {
                navController.navigate(BrowseDestinations.Open.createRoute(parent))
            }
        }
    }

    suspend fun open(context: Context, stateID: StateID) {
        Log.d(logTag, "... Calling open for $stateID")
        searchVM.getNode(stateID)?.let {
            if (it.isFolder()) {
                navController.navigate(
                    BrowseDestinations.Open.createRoute(stateID)
                )
            } else {
                super.open(
                    context = context,
                    stateID = stateID,
                    callingContext = ListContext.SEARCH.id
                )
            }
        }
    }

}
