package org.sinou.android.pydia.ui.home

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.pydio.cells.transport.ClientData
import org.sinou.android.pydia.BuildConfig
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentAboutBinding
import java.util.*


class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val binding: FragmentAboutBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_about, container, false
        )

        binding.aboutVersionName.text =
            resources.getString(R.string.version_name_display, ClientData.getInstance().version)
        binding.aboutVersionCode.text =
            resources.getString(R.string.version_code_display, BuildConfig.VERSION_CODE.toString())

        var dateString = getTimestampAsString(ClientData.getInstance().buildTimestamp)
        binding.aboutVersionDate.text =
            resources.getString(R.string.version_date_display, dateString)

        // See https://developer.android.com/codelabs/basic-android-kotlin-training-navigation-backstack#4
        // to have an example of a complete email sending action

//        val networkStatsManager = getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
//        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        val manager = DataUsageManager(networkStatsManager, telephonyManager.subscriberId)
//        // Monitor single interval
//        manager.getUsage(Interval.today, NetworkType.METERED)
//        // Monitor multiple interval
//        manager.getMultiUsage(listOf(Interval.month, Interval.last30days), NetworkType.WIFI)
//        // Observe realtime usage
//        manager.getRealtimeUsage(NetworkType.UNMETERED).subscribe()


        return binding.root
    }


    private fun getTimestampAsString(timestamp: Long): String {
        // TODO finish to implement this
        var ds = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ds = getTimestampAsStringRecent(timestamp)
        }
        return ds
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getTimestampAsStringRecent(timestamp: Long): String {
        try {
            val netDate = Date(timestamp)
            val sdf = SimpleDateFormat("MM/dd/yyyy")
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }
}
