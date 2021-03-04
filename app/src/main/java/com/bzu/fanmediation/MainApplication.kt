package com.bzu.fanmediation

import android.app.Application
import android.content.Context
import android.util.Log
import com.bzu.fanmediation.Constants.TAG
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.BidderTokenProvider
import com.facebook.ads.BuildConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import java.io.IOException

/**
 * author: 孟磊
 * date: 2021/2/26
 * desc: 初始化入口.
 */
class MainApplication : Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        Constants.isTestMode = BuildConfig.DEBUG
        Thread {
            val bidderToken =
                BidderTokenProvider.getBidderToken(this@MainApplication)
            Constants.bidderToken = bidderToken
            Log.d(TAG, "getBidderToken: $bidderToken")
            try {
                val advertisingIdInfo =
                    AdvertisingIdClient.getAdvertisingIdInfo(this@MainApplication)
                if (advertisingIdInfo != null) {
                    val id = advertisingIdInfo.id
                    Constants.googleAdID = id
                    Log.d(TAG, "googleAdID: $id")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: GooglePlayServicesNotAvailableException) {
                e.printStackTrace()
            } catch (e: GooglePlayServicesRepairableException) {
                e.printStackTrace()
            }
        }.start()

        Constants.authenticationId = getAuthenticationId()
        Log.d(TAG, "authenticationId: ${Constants.authenticationId}")
        AdSettings.setTestMode(true)
        AudienceNetworkAds
            .buildInitSettings(this)
            .withInitListener {
                Log.d(TAG, "initialed: ")
            }
            .initialize()
    }

    private fun getAuthenticationId(
        appId: String = BiddingServer.APP_ID,
        appSecret: String = BiddingServer.APP_SECRET
    ): String? {
        return HmacUtils.getHmacSha256(appId, appSecret)
    }
}