package org.sinou.android.pydia

import android.app.ListActivity
import android.os.Bundle

class SearchActivity : ListActivity() {

    companion object {
        private const val TAG = "SearchActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

//        handleIntent(intent)
    }

}