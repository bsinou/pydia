package org.sinou.android.pydia.utils

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.pydio.cells.api.SDKException
import com.pydio.cells.utils.Log

fun Fragment.hideKeyboard() {
    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
}

@Suppress("DEPRECATION")
fun hasUnMeteredNetwork(context: Context): Boolean {

    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return connMgr.isDefaultNetworkActive && !connMgr.isActiveNetworkMetered
    } else {
        connMgr.allNetworks.forEach { network ->
            connMgr.getNetworkInfo(network)?.let {
                if (it.type == ConnectivityManager.TYPE_WIFI) {
                    return true
                }
            }
        }
        return false
    }
}

@Suppress("DEPRECATION")
fun hasMeteredNetwork(context: Context): Boolean {

    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return connMgr.isDefaultNetworkActive && connMgr.isActiveNetworkMetered
    } else {
        connMgr.allNetworks.forEach { network ->
            connMgr.getNetworkInfo(network)?.let {
                if (it.type == ConnectivityManager.TYPE_MOBILE) {
                    return true
                }
            }
        }
        return false
    }
}


fun hasAtLeastMeteredNetwork(context: Context): Boolean {

    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connMgr.activeNetwork?.let {
            // Log.i("ConnectionCheck", "Active network: ${it.networkHandle}")
            return true
        }
        return false
    } else {
        connMgr.allNetworks.forEach { network ->
            connMgr.getNetworkInfo(network)?.let {
                if (it.type == ConnectivityManager.TYPE_MOBILE || it.type == ConnectivityManager.TYPE_WIFI) {
                    return true
                }
            }
        }
        return false
    }
}

fun logException(caller: String, msg: String, e: Exception) {
    Log.e(caller, "$msg ${if (e is SDKException) "(Code #${e.code} )" else ""}")
    e.printStackTrace()
}