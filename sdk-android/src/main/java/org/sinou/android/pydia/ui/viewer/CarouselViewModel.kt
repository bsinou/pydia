package org.sinou.android.pydia.ui.viewer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pydio.cells.transport.StateID
import org.sinou.android.pydia.services.AccountService
import org.sinou.android.pydia.services.NodeService

/**
 * Holds a list of viewable images for the carousel. It takes care of preloading data in background
 * in advance in order to reduce loading time when the user wants to see a given image.
 */
class CarouselViewModel(
    accountService: AccountService,
    nodeService: NodeService,
    parentFolder: StateID,
    startElement: StateID,
    application: Application
) : AndroidViewModel(application) {

    private val tag = CarouselViewModel::class.simpleName
    val elements = nodeService.listViewable(parentFolder, "image/")

    private var _currActive = startElement
    val currActive: StateID
        get() = _currActive

    fun setActive(stateID: StateID){
        _currActive = stateID
    }

    init {
        Log.i(tag, "init, startElement: $startElement")
    }

    class CarouselViewModelFactory(
        private val accountService: AccountService,
        private val nodeService: NodeService,
        private val parentFolder: StateID,
        private val startElement: StateID,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CarouselViewModel::class.java)) {
                return CarouselViewModel(
                    accountService,
                    nodeService,
                    parentFolder,
                    startElement,
                    application
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
