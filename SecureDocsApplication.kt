package com.securedocs.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.securedocs.app.utils.Prefs

class SecureDocsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        MobileAds.initialize(this)
    }
}
