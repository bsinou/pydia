package org.sinou.android.pydia.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.pydio.cells.transport.ClientData
import org.sinou.android.pydia.BuildConfig
import org.sinou.android.pydia.R
import org.sinou.android.pydia.databinding.FragmentAboutBinding
import org.sinou.android.pydia.utils.getOSCurrentVersion
import org.sinou.android.pydia.utils.getTimestampAsENString
import org.sinou.android.pydia.utils.getTimestampAsString

class AboutFragment : Fragment() {

    private val logTag = AboutFragment::class.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val data = ClientData.getInstance()
        setHasOptionsMenu(true)
        val binding: FragmentAboutBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_about, container, false
        )

        binding.aboutVersionName.text =
            resources.getString(R.string.version_name_display, data.version)
        binding.aboutVersionCode.text =
            resources.getString(R.string.version_code_display, BuildConfig.VERSION_CODE.toString())

        val dateString = getTimestampAsString(data.buildTimestamp)
        binding.aboutVersionDate.text =
            resources.getString(R.string.version_date_display, dateString)


        binding.mainWebsiteButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(resources.getString(R.string.main_website))
            startActivity(intent)
        }

        binding.sendSupportMailButton.setOnClickListener {
            // See https://developer.android.com/codelabs/basic-android-kotlin-training-navigation-backstack#4
            // to have an example of a complete email sending action

            val format = resources.getString(R.string.app_info)
            val appInfo = String.format(
                format,
                data.versionCode,
                data.version,
                getTimestampAsENString(data.buildTimestamp),
                getOSCurrentVersion(),
            )
            val summary = "\n\nPlease describe your problem: \n"

            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
                .putExtra(Intent.EXTRA_TEXT, appInfo + summary)
                .putExtra(Intent.EXTRA_EMAIL, arrayOf(resources.getString(R.string.support_email)))
            if (activity?.packageManager?.resolveActivity(intent, 0) != null) {
                startActivity(intent)
            } else {
                Log.e(logTag, "Could not trigger email for: $appInfo")
            }
        }

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
}
