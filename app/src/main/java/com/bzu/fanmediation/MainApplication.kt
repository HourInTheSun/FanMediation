package com.bzu.fanmediation

import android.app.Application
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds

/**
 * author: 孟磊
 * date: 2021/2/26
 * desc: 初始化入口.
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdSettings.setTestMode(true)
        AudienceNetworkAds.initialize(this)
    }
}