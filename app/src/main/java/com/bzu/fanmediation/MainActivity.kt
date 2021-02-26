package com.bzu.fanmediation

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener

/**
 * author: 孟磊
 * date: 2021/2/26
 * desc: 首页.
 */
class MainActivity : AppCompatActivity(), InterstitialAdListener {

    private lateinit var interstitialAd: InterstitialAd
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button).setOnClickListener {
            loadInterstitial()
        }
    }

    private fun loadInterstitial() {
        interstitialAd = InterstitialAd(this, "YOUR_PLACEMENT_ID")
        interstitialAd.loadAd(interstitialAd.buildLoadAdConfig().withAdListener(this).build())
    }

    override fun onDestroy() {
        super.onDestroy()
        interstitialAd.destroy()
    }

    override fun onInterstitialDisplayed(p0: Ad?) {
    }

    override fun onAdClicked(p0: Ad?) {
    }

    override fun onInterstitialDismissed(p0: Ad?) {
    }

    override fun onError(p0: Ad?, p1: AdError?) {
    }

    override fun onAdLoaded(p0: Ad?) {
        interstitialAd.show()
    }

    override fun onLoggingImpression(p0: Ad?) {
    }
}
