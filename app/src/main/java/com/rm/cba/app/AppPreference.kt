package com.rm.cba.app

import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class AppPreference {

    companion object {
        private const val CERTIFICATE_ALIAS_PREF = "certificate_alias_pref"
        private val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(AppApplication.applicationContext)
        var certificateAlias: String
            get() = sharedPreferences.getString(CERTIFICATE_ALIAS_PREF, AppConstants.DEFAULT_ALIAS).toString()
            set(alias) = sharedPreferences.edit().putString(CERTIFICATE_ALIAS_PREF, alias).apply()
    }
}