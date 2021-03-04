package com.bzu.fanmediation.bean

import com.google.gson.annotations.SerializedName


/**
 * author: menglei
 * date: 2021/3/3
 * desc: bidding 结果.
 */
data class BidResponse(
    @SerializedName("id")
    var id: String?,
    @SerializedName("seatbid")
    var seatbid: List<SeatBid>?,
    @SerializedName("bidid")
    var bidid: String?,
    @SerializedName("cur")
    var cur: String?
)

data class SeatBid(
    @SerializedName("bid")
    var bid: List<Bid>?
)

data class Bid(
    @SerializedName("id")
    var id: String?,
    @SerializedName("impid")
    var impid: String?,
    @SerializedName("price")
    var price: Double?,
    @SerializedName("adm")
    var adm: String?,
    @SerializedName("nurl")
    var nurl: String?,
    @SerializedName("lurl")
    var lurl: String?,
    @SerializedName("ext")
    var ext: Ext?
)

data class Ext(
    @SerializedName("encrypted_cpm")
    var encryptedCpm: String?
)