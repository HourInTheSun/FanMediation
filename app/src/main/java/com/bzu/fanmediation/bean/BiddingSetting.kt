package com.bzu.fanmediation.bean

import com.google.gson.annotations.SerializedName

/**
 * author: menglei
 * date: 2021/3/3
 * desc: bidding配置信息.
 */
data class ClientParams(
    @SerializedName("app_id")
    var appId: String?,
    @SerializedName("placement_id")
    var placementId: String?,
    @SerializedName("bundle")
    var bundle: String?,
    @SerializedName("bundle_version")
    var bundleVersion: String?,
    @SerializedName("ifa")
    var ifa: String?,
    @SerializedName("coppa")
    var coppa: Int?,
    @SerializedName("dnt")
    var dnt: Int?,
    @SerializedName("buyer_tokens")
    var buyerTokens: BuyerTokens?
)

data class BuyerTokens(
    @SerializedName("audience_network")
    var audienceNetwork: String?
)