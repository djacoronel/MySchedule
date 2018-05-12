package com.djacoronel.myschedule

import android.app.Application
import com.google.android.gms.ads.MobileAds



/**
 * Created by djacoronel on 5/12/18.
 */

class MySchedApplication: Application(){
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this, getString(R.string.admob_app_id))
    }
}