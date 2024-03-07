package com.bzu.fanmediation

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bzu.fanmediation.network.BiddingRequestModel
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 * author: 孟磊
 * date: 2021/2/26
 * desc: 首页.
 */
class MainActivity : AppCompatActivity(), InterstitialAdListener {
    private lateinit var textView: TextView
    private lateinit var interstitialAd: InterstitialAd
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        findViewById<Button>(R.id.button).setOnClickListener {
//            request()
//            loadInterstitial()
            thread {
                val startBidRequest: JSONObject = BiddingRequestModel().startBidRequest()
                runOnUiThread {
//                    val code = startBidRequest.getInt("code")
//                    val response = startBidRequest.getJSONObject("data")
//                    textView.text = startBidRequest.toString()
//                    if (response.getString("platform_name") == BiddingServer.BIDDING_PLATFORM_FB) {
//                        val adFormat = response.getString("ad_format")
//                        if (adFormat == "interstitial") {
//                            loadInterstitial(response.getString("placement_id"))
//                        }
//                    }
                }
            }
        }
    }

    private fun loadInterstitial(placementId: String = "YOUR_PLACEMENT_ID") {
        interstitialAd = InterstitialAd(this, placementId)
        interstitialAd.loadAd(interstitialAd.buildLoadAdConfig().withAdListener(this).build())
    }

    @SuppressLint("SetTextI18n")
    private fun request() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
//        val url = "https://www.facebook.com/"
        val url = "http://www.google.com"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                // Display the first 500 characters of the response string.
                textView.text = "Response is: ${response.substring(0, 500)}"
            },
            { textView.text = "That didn't work!" })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    @SuppressLint("SetTextI18n")
    private fun requestAuction() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
//        val url = "https://www.facebook.com/"
//        val url = "http://www.google.com"
        val url = "https://an.facebook.com/1/placementbid.ortb"


        // Request a string response from the provided URL.
        val jsObjRequest = JsonObjectRequest(
            Request.Method.POST, url, getRequestParam(),
            { response: JSONObject ->
                textView.text = "Response is: $response"

                val placementId = response.getString("placement_id")
                val adFormat = response.getString("ad_format")

                val platformName = response.getString("platform_name")
                val platformPlacementId = response.getString("platform_placement_id")
                val payload = response.getString("bid_payload")

//                loadAd(placementId)

            }, { error: VolleyError ->
                textView.text = "That didn't work! ${error.message}"
            }
        )

        // Add the request to the RequestQueue.
        queue.add(jsObjRequest)
    }

    private fun getRequestParam(): JSONObject {
        val requestObject = JSONObject()
        requestObject.put("app_id", packageName)
        requestObject.put("placement_id", "placementId")
        requestObject.put("bundle", BuildConfig.APPLICATION_ID)
        requestObject.put("bundle_version", BuildConfig.VERSION_NAME)
        requestObject.put("ifa", Constants.googleAdID)

        // coppa and do not track flags
        requestObject.put("coppa", 1)
        requestObject.put("dnt", 0)

        requestObject.put(
            "buyer_tokens",
            JSONObject()
                .put("audience_network", Constants.bidderToken)
        )
        requestObject.put("test", if (Constants.isTestMode) 1 else 0)
        return requestObject
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::interstitialAd.isInitialized) interstitialAd.destroy()
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
