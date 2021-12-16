package org.sinou.android.pydia.authktx

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sinou.android.pydia.utils.singleArgViewModelFactory

/**
 * MainViewModel designed to store and manage UI-related data in a lifecycle conscious way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 *
 * @param repository the data source this ViewModel will fetch results from.
 */
class MainViewModel(private val repository: TitleRepository) : ViewModel() {

    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::MainViewModel)
    }

    /**
     * Request a snackbar to display a string.
     *
     * This variable is private because we don't want to expose MutableLiveData
     *
     * MutableLiveData allows anyone to set a value, and MainViewModel is the only
     * class that should be setting values.
     */
    private val _snackBar = MutableLiveData<String?>()

    /**
     * Request a snackbar to display a string.
     */
    val snackbar: LiveData<String?>
        get() = _snackBar

    /**
     * Update title text via this LiveData
     */
    val title = repository.title

    private val _spinner = MutableLiveData<Boolean>(false)

    /**
     * Show a loading spinner if true
     */
    val spinner: LiveData<Boolean>
        get() = _spinner

    /**
     * Count of taps on the screen
     */
    private var tapCount = 0

    /**
     * LiveData with formatted tap count.
     */
    private val _taps = MutableLiveData<String>("$tapCount taps")

    /**
     * Public view of tap live data.
     */
    val taps: LiveData<String>
        get() = _taps

    /**
     * Respond to onClick events by refreshing the title.
     *
     * The loading spinner will display until a result is returned, and errors will trigger
     * a snackbar.
     */
    fun onMainViewClicked() {
        refreshTitle()
        updateTaps()
    }

    /**
     * Wait one second then update the tap count.
     */
//    private fun updateTaps() {
//        // TODO: Convert updateTaps to use coroutines
//        tapCount++
//        //BACKGROUND.submit {
//            Thread.sleep(1_000)
//            _taps.postValue("${tapCount} taps")
//        //}
//    }

    /**
     * Wait one second then display a snackbar.
     */
    fun updateTaps() {
        // launch a coroutine in viewModelScope
        viewModelScope.launch {
            tapCount++
            // suspend this coroutine for one second
            delay(1_000)
            // resume in the main dispatcher
            // _snackbar.value can be called directly from main thread
            _taps.postValue("$tapCount taps")
        }
    }

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackBar.value = null
    }

    /**
     * Refresh the title, showing a loading spinner while it refreshes and errors via snackbar.
     */
    fun refreshTitle() {
        // With CallBacks
//        // TODO: Convert refreshTitle to use coroutines
//        _spinner.value = true
//        repository.refreshTitleWithCallbacks(object : TitleRefreshCallback {
//            override fun onCompleted() {
//                _spinner.postValue(false)
//            }
//
//            override fun onError(cause: Throwable) {
//                _snackBar.postValue(cause.message)
//                _spinner.postValue(false)
//            }
//        })
        viewModelScope.launch {
            try {
                _spinner.value = true
                repository.refreshTitle()
            } catch (error: TitleRefreshError) {
                _snackBar.value = error.message
            } finally {
                _spinner.value = false
            }
        }

    }
}
