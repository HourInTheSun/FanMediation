package com.bzu.fanmediation.network

import android.util.Log
import androidx.annotation.WorkerThread
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bzu.fanmediation.*
import com.bzu.fanmediation.Constants.TAG
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * author: menglei
 * date: 2021/3/1
 * desc: .
 */
class BiddingRequestModel {
    private var bidResponseBean: BidResponse? = null

    @WorkerThread
    fun startBidRequest(): JSONObject {
        Log.d(TAG, "startBidRequest:")
        val bid = getBid()
        Log.d(TAG, "endBidRequest: $bid")
        return bid
    }

    //*********************************app.py***********************************/
    fun getBid(): JSONObject {
        val requestParams = BiddingServer.getClientParams()
        return getBidResponse(
            "192.168.63.63",
            "Dalvik/2.1.0 (Linux; U; Android 8.1.0; A0001 Build/OPM7.181205.001)",
            requestParams
        )
    }
    //*********************************app.py***********************************/


    //*********************************bid_manager.py***********************************/
    /**
     * Return Auction Result - Winner - back to client side
     */
    fun getBidResponse(
        ip: String,
        userAgent: String,
        requestParams: JSONObject
    ): JSONObject {
        try { //Get the winner bid response for the current request
            val appId = requestParams.getString("app_id")
            val placementId = requestParams.getString("placement_id")
            val auctionId = getAuctionId("interstitial")

            // Find placement in the settings
            var placement = ServerSettings.getPlacement(appId, placementId)
            if (placement == null) {
                val auctionSetting = BiddingServer.getAuctionSettingParams(
                    BiddingServer.APP_ID,
                    BiddingServer.AD_PLACEMENT_ID,
                    BiddingServer.AD_PLACEMENT_NAME,
                    "interstitial",
                    BiddingServer.AD_AUCTION_ID
                )
                placement = auctionSetting.getJSONArray("apps").getJSONObject(0)
                    .getJSONArray("placements").getJSONObject(0)
                ServerSettings.savePlacement(auctionSetting)
            }

            val bidRequests = getBidRequests(ip, userAgent, auctionId, placement!!, requestParams)
            val bidResponses = arrayListOf<JSONObject>()
            for (index in 0 until bidRequests.length()) {
                val bidRequest = bidRequests.getJSONObject(index)
                val biResponse = execBidRequest(
                    bidRequest.getString("platform_name"),
                    bidRequest.getString("end_point"),
                    bidRequest.getJSONObject("data"),
                    bidRequest.getString("timeout_notification_url")
                )
                biResponse.put("platform_name", bidRequest.getString("platform_name"))
                bidResponses.add(biResponse)
            }
            return runAuction(bidResponses, placement)
        } catch (e: Exception) {
            throw IllegalStateException("Error in request parameters: {} ${e.message}")
        }
    }

    // Request ID we can generate on server
    fun getAuctionId(adFormat: String): String {
        return "${adFormat}_test_bid_req_id"
    }

    fun runAuction(
        bidResponses: List<JSONObject>,
        placement: JSONObject
    ): JSONObject {
        //Run auction based on raw responses and create the response object
        val other_bid = 1
        var response = JSONObject().put("204", "None")
        bidResponses.forEachIndexed { index, biResponse ->
            if (biResponse.getString("platform_name") == "audience_network") {
                if (biResponse.getInt("code") == 200) {
                    val ortbResponse = biResponse.getJSONObject("response")
                    if (ortbResponse.getDouble("price") > other_bid) {
                        response = createResponse(biResponse, placement)
                        notifyResult(biResponse)
                    } else {
                        notifyResult(biResponse, 102)
                    }
                } else {

                }
            }
        }
        return response
    }

    fun notifyResult(biResponse: JSONObject, code: Int = 200) {
        bidResponseBean ?: return
        val queue = Volley.newRequestQueue(MainApplication.appContext)
        val AUCTION_PRICE = biResponse.getJSONObject("response").getDouble("price")
        val url: String = if (code == 200) {
            bidResponseBean!!.nurl.replace("\${AUCTION_PRICE}", "$AUCTION_PRICE")
        } else {
            bidResponseBean!!.lurl.replace("\${AUCTION_LOSS}", "$code")
                .replace("\${AUCTION_PRICE}", "$AUCTION_PRICE")
        }
        val request = StringRequest(url, {

        }, {

        })
        queue.add(request)
    }

    fun createResponse(
        biResponse: JSONObject,
        placement: JSONObject
    ): JSONObject {
        //Create response object based on the auction result
        val ad_format = placement.getString("ad_format")
        val platform_name = biResponse.getString("platform_name")
        var platform_placement_id: String? = null
        val biddingPlacementIds = placement.getJSONArray("bidding_source_placement_ids")
        for (index in 0 until biddingPlacementIds.length()) {
            val placementId = biddingPlacementIds.getJSONObject(index)
            if (placementId.getString("platform_name") == platform_name) {
                platform_placement_id = placementId.getString("platform_placement_id")
            }
        }
        if (platform_placement_id.isNullOrEmpty()) {
            throw IllegalStateException("Platform placement ID not found!")
        }
        val bid_payload: String?
        if (platform_name == "audience_network") {
            bid_payload = biResponse.getJSONObject("response").getString("adm")
        } else {
            throw IllegalStateException("Invalid platform")
        }
        return JSONObject().put("code", 200)
            .put(
                "data", JSONObject()
                    .put("placement_id", platform_placement_id)
                    .put("ad_format", ad_format)
                    .put("price", biResponse.getJSONObject("response").getDouble("price"))
                    .put("platform_name", platform_name)
                    .put("platform_placement_id", platform_placement_id)
                    .put("bid_payload", bid_payload)
            )
    }

    /**
     * Execute bid request for different platform (network)
     */
    fun execBidRequest(
        platformName: String,
        endPoint: String,
        data: JSONObject,
        timeoutNotificationUrl: String
    ): JSONObject {
        if (platformName == "audience_network") {
            return execBidRequest(endPoint, data, timeoutNotificationUrl)
        } else {
            throw IllegalStateException("Invalid platform: {} $platformName")
        }
    }

    /**
     * Get all bid requests for different platforms
     */
    fun getBidRequests(
        ip: String,
        userAgent: String,
        auctionId: String,
        placement: JSONObject,
        requestParams: JSONObject
    ): JSONArray {
        //Create bid requests based on the internal placement setting
        val results = JSONArray()
        val biddingSource = placement.getJSONArray("bidding_source_placement_ids").getJSONObject(0)
        val bidRequest = getBidRequest(
            ip,
            userAgent,
            auctionId,
            biddingSource.getString("platform_name"),
            biddingSource.getString("platform_app_id"),
            biddingSource.getString("platform_placement_id"),
            placement.getString("ad_format"),
            requestParams
        )
        val endPoint = bidRequest.getString("endPoint")
        val request = bidRequest.getJSONObject("request")
        val timeoutNotificationUrl = bidRequest.getString("timeoutNotificationUrl")
        results.put(
            0, JSONObject()
                .put("platform_name", biddingSource.getString("platform_name"))
                .put("end_point", endPoint)
                .put("data", request)
                .put("timeout_notification_url", timeoutNotificationUrl)
        )

        return results
    }

    /**
     * Get bid request for each platform TODO 目前只有FB
     */
    fun getBidRequest(
        ip: String,
        userAgent: String,
        auctionId: String,
        platformName: String,
        platformAppId: String,
        platformPlacementId: String,
        adFormat: String,
        requestParams: JSONObject
    ): JSONObject {
        return if (platformName == "audience_network") {
            getBidRequest(
                ip,
                userAgent,
                auctionId,
                platformAppId,
                platformPlacementId,
                adFormat,
                requestParams
            )
        } else {
            JSONObject()
        }
    }

    //*********************************bid_manager.py***********************************/

    //*********************************audience_network.py***********************************/
    fun execBidRequest(
        endPoint: String,
        requestParams: JSONObject,
        timeoutNotificationUrl: String
    ): JSONObject {
        val platform = ServerSettings.getPlatform("audience_network")
        val createService = RetrofitHelper.getInstance()
            .buildRetrofit("https://an.facebook.com")
            .createService(ServerService::class.java)
        val headers = HashMap<String, String>()
        headers["Content-Type"] = "application/json; charset=utf-8"
        headers["X-FB-Pool-Routing-Token"] = requestParams
            .getJSONObject("user")
            .getString("buyeruid")
        val requestBody: RequestBody =
            RequestBody.create(MediaType.parse("application/json"), requestParams.toString())

//        val url = "https://an.facebook.com/${BiddingServer.APP_ID}/placementbid.ortb"
//        val queue = Volley.newRequestQueue(MainApplication.appContext)
//        // Request a string response from the provided URL.
//        val jsObjRequest = JsonObjectRequest(
//            Request.Method.POST, url, requestParams,
//            { response: JSONObject ->
//                Log.d(TAG, "volley execBidRequest:$response ")
//            }, { error: VolleyError ->
//                Log.d(TAG, "volley execBidRequest:$error ")
//            }
//        )
//        // Add the request to the RequestQueue.
//        queue.add(jsObjRequest)

        val response = createService.getBidResponse(BiddingServer.APP_ID, headers, requestBody)
            .execute()
        Log.d(TAG, "getBidResponse: $response")
        if (response.isSuccessful) {
            val body: FBResponse? = response.body()
            val raw = response.raw()
            response.errorBody()
            Log.d(TAG, "execBidRequest: $body,  raw = $raw")
            var code = 204
            var ortbResponse: JSONObject? = null
            if (body != null) {
                code = 200
                val bidResponse = body.seatbid[0].bid[0]
                this.bidResponseBean = bidResponse
                ortbResponse = JSONObject()
                    .put("adm", bidResponse.adm)
                    .put("ext", bidResponse.ext)
                    .put("id", bidResponse.id)
                    .put("impid", bidResponse.impid)
                    .put("lurl", bidResponse.lurl)
                    .put("nurl", bidResponse.nurl)
                    .put("price", bidResponse.price)
            }
            return JSONObject()
                .put("code", code)
                .put("response", ortbResponse)
        } else {
            val responseHeaders = response.headers()
            val errorHeader = responseHeaders["x-fb-an-errors"]
            val debugHeader = responseHeaders["x-fb-debug"]
            val bidRequestId = responseHeaders["x-fb-an-request-id"]
            when (response.code()) {
                400 -> {
                }
                204 -> {
                }
                else -> {
                }
            }
            return JSONObject()
                .put("code", response.code())
                .put("response", null)
        }
    }

    fun getBidRequest(
        ip: String,
        userAgent: String,
        auctionId: String,
        platformAppId: String,
        platformPlacementId: String,
        adFormat: String,
        requestParams: JSONObject
    ): JSONObject {
        val platform = ServerSettings.getPlatform("audience_network")
        val endPoint = "https://an.facebook.com/${platformAppId}/placementbid.ortb"
        val timeout = 1000
        val timeoutNotificationUrl = "https://www.facebook.com/audiencenetwork/nurl/?" +
                "partner=${platformAppId}" +
                "&app=${platformAppId}" +
                "&auction=${auctionId}" +
                "&ortb_loss_code=2"

        val request = JSONObject()
        val imp = JSONArray()
        val adFormatImp = JSONObject()
        adFormatImp.put("id", auctionId)
        when (adFormat) {
            "native" -> {
                adFormatImp.put(
                    "native", JSONObject()
                        .put("w", -1)
                        .put("h", -1)
                )
            }
            "banner" -> {
                adFormatImp.put(
                    "banner", JSONObject()
                        .put("w", -1)
                        .put("h", 50)
                )
            }
            "interstitial" -> {
                adFormatImp
                    .put(
                        "banner", JSONObject()
                            .put("w", 0)
                            .put("h", 0)

                    )
                    .put("instl", 1)
            }
            "rewarded_video" -> {
                adFormatImp.put(
                    "video", JSONObject()
                        .put("w", 0)
                        .put("h", 0)
                        .put("linearity", 2)
                )
            }
            "instream_video" -> {
                adFormatImp.put(
                    "video", JSONObject()
                        .put("w", 0)
                        .put("h", 0)
                        .put("linearity", 1)
                )
            }
            else -> throw IllegalStateException("Incorrect ad format")
        }
        adFormatImp.put(
            "tagid",
            platformPlacementId
        ) //holds the Audience Network identifier for the inventory.
        imp.put(0, adFormatImp)
        val device = JSONObject()
        device.put("ifa", requestParams.getString("ifa"))
        device.put("ua", userAgent)
        device.put("dnt", requestParams.getInt("dnt"))
        if (ip.length > 12) { //ipaddress.IPv6Address
            device.put("ip", ip)
//            device.put("ipv6", ip)
        } else {
            device.put("ip", ip)
        }

        request.put("id", auctionId)
        request.put("imp", imp)
        request.put(
            "app", JSONObject()
                .put("bundle", requestParams.getString("bundle"))
                .put("ver", requestParams.getString("bundle_version"))
                .put(
                    "publisher", JSONObject()
                        .put("id", platformAppId)
                )
        )
        request.put("device", device)
        request.put(
            "regs", JSONObject()
                .put("coppa", requestParams.getInt("coppa"))
        )
        request.put(
            "user", JSONObject()
                .put(
                    "buyeruid",
                    requestParams.getJSONObject("buyer_tokens").getString("audience_network")
                )
        )
        request.put(
            "ext", JSONObject()
                .put("platformid", platformAppId)
        )
        request.put("at", 1)
        request.put("tmax", timeout)
        request.put("test", requestParams.optInt("test", 1))
        return JSONObject()
            .put("endPoint", endPoint)
            .put("request", request)
            .put("timeoutNotificationUrl", timeoutNotificationUrl)
    }
    //*********************************audience_network.py***********************************/
}
