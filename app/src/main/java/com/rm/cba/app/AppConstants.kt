package com.rm.cba.app

class AppConstants {
    companion object {
        // Replace the  hostname "localhost" with "10.0.2.2". Why? -->  https://developer.android.com/studio/run/emulator-networking
        const val HOST_NAME = "10.0.2.2"

        const val BASE_URL = "https://$HOST_NAME:8443/"
        const val DEFAULT_ALIAS: String = "My Client Certificate"
    }
}