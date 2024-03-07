package com.bzu.fanmediation

import org.json.JSONArray
import org.json.JSONObject

/**
 * author: menglei
 * date: 2021/3/1
 * desc: bidding相关请求.
 */
class BiddingServer {

    companion object {
        const val APP_NAME = BuildConfig.APPLICATION_ID
        const val VERSION_NAME = BuildConfig.VERSION_NAME
        const val APP_ID = BuildConfig.APP_ID
        const val APP_SECRET = BuildConfig.APP_SECRET
        const val AD_AUCTION_ID = "interstitial_test_bid_req_id"
        const val AD_PLACEMENT_ID = BuildConfig.PLACEMENT_ID_INTERSTITIAL
        const val AD_PLACEMENT_NAME = "fb_interstitial"
        const val BIDDING_PLATFORM_FB = "audience_network"
        const val PLATFORM_ID = APP_ID
        const val PARTNER_FBID = APP_ID
        const val APP_FBID = APP_ID

        fun getClientParams(): JSONObject {
            val requestObject = JSONObject()
            // App ID and placement ID are used to look up settings from
            // server settings
            requestObject.put("app_id", APP_ID)
            requestObject.put("placement_id", AD_PLACEMENT_ID)

            requestObject.put("bundle", APP_NAME)
            requestObject.put("bundle_version", VERSION_NAME)

            // Device specifics
            requestObject.put("ifa", Constants.googleAdID ?: "invalid")
            requestObject.put("coppa", 0)
            requestObject.put("dnt", 0)

            // buyer_tokens are the user tokens required for different networks
            requestObject.put(
                "buyer_tokens", JSONObject()
                    // Token for audience network from BidderTokenProvider.getBidderToken(context)
                    // This can be cached for the same app session
                    .put("audience_network", Constants.bidderToken ?: "invalid")
            )
            return requestObject
        }

        fun getAuctionSettingParams(
            appId: String,
            placementId: String,
            placementName: String,
            adFormat: String,
            auction_id: String
        ): JSONObject {
            val requestObject = JSONObject()
            val platformObject = JSONArray()
            platformObject.put(
                0, JSONObject()
                    .put("platform_name", "audience_network")
                    .put("end_point", "https://an.facebook.com/${PLATFORM_ID}/placementbid.ortb")
                    .put("timeout", 1000)
                    .put(
                        "timeout_notification_url",
                        "https://www.facebook.com/audiencenetwork/nurl/?partner=${PARTNER_FBID}&app=${APP_FBID}&auction=${auction_id}&ortb_loss_code=2"
                    )
            )
            requestObject.put("bidding_source_platforms", platformObject)
            val appsObject = JSONArray()
            val placementObject = JSONObject()
            placementObject.put("app_id", appId)
            placementObject.put("app_name", APP_NAME)
            placementObject.put(
                "placements", JSONArray().put(
                    0, JSONObject()
                        .put("placement_id", placementId)
                        .put("placement_name", placementName)
                        .put("ad_format", adFormat)
                        .put(
                            "bidding_source_placement_ids", JSONArray().put(
                                0, JSONObject()
                                    .put("platform_name", "audience_network")
                                    .put("platform_app_id", appId)
                                    .put("platform_placement_id", placementId)
                            )
                        )
                )
            )
            appsObject.put(0, placementObject)
            requestObject.put("apps", appsObject)
            return requestObject
        }

        fun getORTBRequestParams(): JSONObject {
            val requestObject = JSONObject()
            // Device information from client side
            val deviceObject = JSONObject()
            deviceObject.put("ifa", Constants.googleAdID ?: "invalid")
            deviceObject.put("dnt", 0)
            deviceObject.put("ip", "127.0.0.1")
            // Application information
            val appObject = JSONObject()
            appObject.put("ver", VERSION_NAME)
            appObject.put("bundle", APP_NAME)
            // For server to server bidding integration, this is your application id on Facebook
            appObject.put("publisher", JSONObject().put("id", APP_ID))
            // Placement information we can store on server
            val impObject = JSONArray()
            val placementObject = JSONObject()
            placementObject.put("id", AD_AUCTION_ID)
            // This is the placement id for Audience Network
            placementObject.put("tagid", AD_PLACEMENT_ID)
            placementObject.put("banner", JSONObject().put("w", -1).put("h", 50))
            impObject.put(0, placementObject)
            // Optional regulations object from client side
            val regsObject = JSONObject()
            regsObject.put("coppa", 0)
            // In server to server integration, you can use the Facebook app id as platform id here
            val extObject = JSONObject()
            extObject.put("platformid", APP_ID)
            // buyeruid is the user bidder token generated on client side, using the `getBidderToken` method from the Audience Network SDK.
            // It's constant through out app session so you could cache it on the client side
            val userObject = JSONObject()
            userObject.put("buyeruid", Constants.bidderToken ?: "invalid")

            requestObject.put("device", deviceObject)
            requestObject.put("app", appObject)
            requestObject.put("imp", impObject)
            requestObject.put("regs", regsObject)
            requestObject.put("ext", extObject)
            requestObject.put("user", userObject)
            // Test mode flag
            requestObject.put("test", "1")
            // Time out setting we can store on server
            requestObject.put("tmax", 1000)
            // Request ID we can generate on server
            requestObject.put("id", AD_AUCTION_ID)
            // Auction setting we can store on server
            requestObject.put("at", 1)
            return requestObject
        }
    }
}