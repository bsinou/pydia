package org.sinou.android.pydia

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.helper.widget.Carousel
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.pydio.cells.transport.StateID
import com.pydio.cells.utils.Str
import org.sinou.android.pydia.databinding.ActivityCarouselBinding
import org.sinou.android.pydia.db.nodes.RTreeNode
import org.sinou.android.pydia.ui.viewer.CarouselViewModel
import java.io.File

class CarouselActivity : AppCompatActivity() {

    private val tag = CarouselActivity::class.simpleName
    private lateinit var binding: ActivityCarouselBinding
    private lateinit var carouselVM: CarouselViewModel

    var numImages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(savedInstanceState)
        setupActivity()
        setupCarousel()
    }

    private fun handleIntent(savedInstanceState: Bundle?) {

        Log.d(tag, "handleIntent, bundle: $savedInstanceState")
        Log.d(tag, "has extra: ${intent.hasExtra(AppNames.EXTRA_STATE)}")

        if (intent.hasExtra(AppNames.EXTRA_STATE)) {
            val stateStr: String = intent.getStringExtra(AppNames.EXTRA_STATE)!!
            val contextType = intent.getStringExtra(AppNames.EXTRA_ACTION_CONTEXT)

            val state = StateID.fromId(stateStr)
            val viewModelFactory = CarouselViewModel.CarouselViewModelFactory(
                CellsApp.instance.accountService,
                CellsApp.instance.nodeService,
                state.parentFolder(),
                state,
                application,
            )
            val tmpVM: CarouselViewModel by viewModels { viewModelFactory }
            carouselVM = tmpVM

            tmpVM.elements.observe(this) {
                numImages = it.size
                if (binding.carousel != null) {
                    Log.i(tag, "got a carousel, refreshing")

                    val currItems = carouselVM.elements.value!!
                    var startItem: RTreeNode? = null
                    var index: Int = -1
                    var i = 0
                    for (currNode in currItems) {
                        if (currNode.encodedState == carouselVM.startElement.id) {
                            startItem = currNode
                            index = i
                        }
                        i++
                    }
                    Log.w(tag, "...Got a carousel, start index: $index")
                    binding.carousel.jumpToIndex(if (index >= 0) index else 0)
                    binding.carousel.refresh()
                } else {
                    Log.w(tag, "...**NO** carousel")
                }
            }
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
    }

    private fun setupCarousel() {
        binding.carousel.setAdapter(object : Carousel.Adapter {
            val fs = CellsApp.instance.fileService

            override fun count(): Int {
                return numImages
            }

            override fun populate(view: View, index: Int) {
                Log.w(tag, "populating view at ${index}")
                if (view is ImageView) {
                    val currItem = carouselVM.elements.value!![index]
                    val lf = fs.getThumbPath(currItem)
                    if (Str.notEmpty(lf) && File(lf!!).exists()) {
                        Glide.with(this@CarouselActivity)
                            .load(File(lf))
                            .transform(
                                MultiTransformation(
                                    CenterCrop(),
                                    // TODO Directly getting  the radius with R fails => image is a circle
                                    // RoundedCorners(R.dimen.glide_thumb_radius)
                                    RoundedCorners(16)
                                )
                            )
                            .into(view)
                    } else {
                        Log.w("SetNodeThumb", "no thumb found for ${index}")
                        // setImageResource(getDrawableFromMime(item.mime, item.sortName))
                    }
                }
            }

            override fun onNewItem(index: Int) {
                Log.i(tag, "onNewItem, index: $index")
            }
        })
    }

    override fun onStart() {
        Log.d(tag, "onStart, intent: $intent")
        super.onStart()
    }

    override fun onResume() {
        Log.i(tag, "onResume, intent: $intent")
        super.onResume()
    }

    override fun onPause() {
        Log.i(tag, "onPause, intent: $intent")
        super.onPause()
    }

    override fun onStop() {
        Log.i(tag, "onStop, intent: $intent")
        super.onStop()
    }


//    add or remove elements dynamically

    //    private void setupCarouselDemo50(Carousel carousel) {
    //        TextView text = findViewById(R.id.text);
    //        Button buttonAdd = findViewById(R.id.add);
    //        if (buttonAdd != null) {
    //            buttonAdd.setOnClickListener(view -> {
    //                numImages++;
    //                if (text != null) {
    //                    text.setText("" + numImages + " images");
    //                }
    //                carousel.refresh();
    //            });
    //        }
    //        Button buttonRemove = findViewById(R.id.remove);
    //        if (buttonRemove != null) {
    //            buttonRemove.setOnClickListener(view -> {
    //                numImages = 0;
    //                if (text != null) {
    //                    text.setText("" + numImages + " images");
    //                }
    //                carousel.refresh();
    //            });
    //        }
    //    }
}