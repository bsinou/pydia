package org.sinou.android.pydia

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CallbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        if (Intent.ACTION_VIEW == intent.action) {
            val uri = intent.data
            if (uri == null) {
                finish()
                return
            }
            println("In CellsAuthURLHandler, URI: $uri")
            finish()
        }
   }

}
