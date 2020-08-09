package com.rm.cba.app

import android.app.Application
import android.content.Context
import java.security.cert.X509Certificate

class AppApplication : Application() {

    init {
        INSTANCE = this
    }

    companion object {
        lateinit var INSTANCE: AppApplication
            private set

        val applicationContext: Context
            get() {
                return INSTANCE.applicationContext
            }
    }
}