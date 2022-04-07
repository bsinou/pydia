package org.sinou.android.pydia.ui.viewer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.db.nodes.RTreeNode
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.NodeService

/**
 * Holds a list of viewable images for the carousel. It takes care of preloading data in background
 * in advance in order to reduce loading time when the user wants to see a given image.
 */
class CarouselViewModel(
//     accountService: AccountService,
    private val nodeService: NodeService,

// FIXME
//    parentFolder: StateID,
//     startElement: StateID,
) : ViewModel() {

    private val tag = CarouselViewModel::class.simpleName

    lateinit var elements: LiveData<List<RTreeNode>>

    private lateinit var _currActive: StateID
    val currActive: StateID
        get() = _currActive

    fun afterCreate (parentFolder: StateID, startElement: StateID){
        _currActive = startElement
        elements = nodeService.listViewable(parentFolder, "image/")
        Log.i(tag, "afterCreate, startElement: $startElement")

    }

    fun setActive(stateID: StateID){
        _currActive = stateID
    }

//
//    class CarouselViewModelFactory(
//        private val accountService: AccountService,
//        private val nodeService: NodeService,
//        private val parentFolder: StateID,
//        private val startElement: StateID,
//        private val application: Application
//    ) : ViewModelProvider.Factory {
//        @Suppress("unchecked_cast")
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            if (modelClass.isAssignableFrom(CarouselViewModel::class.java)) {
//                return CarouselViewModel(
//                    accountService,
//                    nodeService,
//                    parentFolder,
//                    startElement,
//                    application
//                ) as T
//            }
//            throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }
}
