package org.sinou.android.pydia.home

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import org.sinou.android.pydia.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
