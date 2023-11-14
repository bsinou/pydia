package org.sinou.pydia.client.core.ui.core.nav

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import org.sinou.pydia.client.R
import org.sinou.pydia.client.core.utils.getOSCurrentVersion
import org.sinou.pydia.client.core.utils.getTimestampAsENString
import org.sinou.pydia.sdk.transport.ClientData

fun openExternalURL(urlStr: String): Intent {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(urlStr)
    return intent
}

fun sendSupportEmail(resources: Resources): Intent {
    val data = ClientData.getInstance()
    val format = resources.getString(R.string.app_info)
    val appInfo = String.format(
        format,
        data.versionCode,
        data.version,
        getTimestampAsENString(data.lastUpdateTime),
        getOSCurrentVersion(),
    )
    val summary = "\n\nPlease describe your problem (in English): \n"

    val intent = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.support_email_subject))
        .putExtra(Intent.EXTRA_TEXT, appInfo + summary)
        .putExtra(Intent.EXTRA_EMAIL, arrayOf(resources.getString(R.string.support_email)))

    return intent
}
