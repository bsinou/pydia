package org.sinou.android.pydia

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.helper.widget.Carousel
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sinou.android.pydia.databinding.ActivityCarouselBinding
import org.sinou.android.pydia.services.FileService
import org.sinou.android.pydia.ui.viewer.CarouselViewModel
import java.io.File

class CarouselActivity : AppCompatActivity() {

    private val tag = CarouselActivity::class.simpleName
    private val fileService: FileService by inject()

    private val carouselVM: CarouselViewModel by viewModel()

    private lateinit var binding: ActivityCarouselBinding
    var numImages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag, "onCreate, intent: $intent")
        super.onCreate(savedInstanceState)
        handleIntent(savedInstanceState)
        setupActivity()
    }

    override fun onResume() {
        Log.d(tag, "onResume, intent: $intent")
        super.onResume()
        setupCarousel()
        lifecycleScope.launch {
            // Workaround the NPE on creation
            delay(800)
            observe()
        }
    }

    private fun handleIntent(savedInstanceState: Bundle?) {

        Log.d(tag, "handleIntent, bundle: $savedInstanceState")
        Log.d(tag, "has extra: ${intent.hasExtra(AppNames.EXTRA_STATE)}")

        if (intent.hasExtra(AppNames.EXTRA_STATE)) {
            val stateStr: String = intent.getStringExtra(AppNames.EXTRA_STATE)!!
            val contextType = intent.getStringExtra(AppNames.EXTRA_ACTION_CONTEXT)
            val state = StateID.fromId(stateStr)

//            val viewModelFactory = CarouselViewModel.CarouselViewModelFactory(
//                CellsApp.instance.accountService,
//                CellsApp.instance.nodeService,
//                state.parentFolder(),
//                state,
//                application,
//            )
//            val tmpVM: CarouselViewModel by viewModels { viewModelFactory }
//            carouselVM = tmpVM

            carouselVM.afterCreate(state.parentFolder(), state)
        }
        // TODO handle errors
    }

    private fun setupActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_carousel)
        // TODO rather set a style with no action bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()
        // binding.invalidateAll()
    }

    private fun setupCarousel() {

        val ml = binding.motionLayout
        Log.e(tag, "Motion layout is shown: ${ml.isShown}")

        binding.carousel.setAdapter(object : Carousel.Adapter {

            override fun count(): Int {
                return numImages
            }

            override fun populate(view: View, index: Int) {
                if (view is ImageView) {
                    val currItem = carouselVM.elements.value!![index]
                    val lf = fileService.getThumbPath(currItem)
                    if (Str.notEmpty(lf) && File(lf!!).exists()) {
                        Glide.with(this@CarouselActivity)
                            .load(File(lf))
                            .into(view)
                    } else {
                        Log.w("SetNodeThumb", "no thumb found for ${index}")
                        // setImageResource(getDrawableFromMime(item.mime, item.sortName))
                    }
                }
            }

            override fun onNewItem(index: Int) {
                // Retrieve the encoded state of the current item and store it in the view model
                // to stay at the same index upon restart / configuration change.
                carouselVM.elements.value?.let {
                    val currItem = it[index]
                    carouselVM.setActive(currItem.getStateID())
                }
            }
        })
    }

    private fun observe() {
        carouselVM.elements.observe(this) {
            numImages = it.size
            jumpToIndex()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        Log.i(tag, "onPostCreate")
        super.onPostCreate(savedInstanceState, persistentState)
        jumpToIndex()
    }

    private fun jumpToIndex() {
        val currItems = carouselVM.elements.value!!
        var index: Int = -1
        var i = 0
        for (currNode in currItems) {
            if (currNode.encodedState == carouselVM.currActive.id) {
                index = i
                break
            }
            i++
        }
        Log.w(tag, "... Got a carousel, start index: $index")

        if (index > 0) {
            binding.carousel.jumpToIndex(index)
        } else {
            binding.carousel.refresh()
        }
    }

    override fun onStart() {
        Log.d(tag, "onStart, intent: $intent")
        super.onStart()
    }

    override fun onPause() {
        Log.i(tag, "onPause, intent: $intent")
        super.onPause()
    }

    override fun onStop() {
        Log.i(tag, "onStop, intent: $intent")
        super.onStop()
    }
}

/**
 * Avoid NPE on screen rotation and other configuration changes
 * Thanks to https://stackoverflow.com/questions/65048418/how-to-restore-transition-state-of-motionlayout-without-auto-playing-the-transit
 */
open class SavingMotionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

    private val tag = SavingMotionLayout::class.simpleName

    override fun onSaveInstanceState(): Parcelable {
        Log.d(tag, "onSaveInstanceState()")
        return SaveState(super.onSaveInstanceState(), startState, endState, targetPosition)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        Log.d(tag, "onRestoreInstanceState()")
        (state as? SaveState)?.let {
            super.onRestoreInstanceState(it.superParcel)
            setTransition(it.startState, it.endState)
            progress = it.progress
        }
    }

    @kotlinx.parcelize.Parcelize
    private class SaveState(
        val superParcel: Parcelable?,
        val startState: Int,
        val endState: Int,
        val progress: Float
    ) : Parcelable
}

