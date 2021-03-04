package com.bzu.fanmediation.bean

import com.google.gson.annotations.SerializedName


/**
 * author: menglei
 * date: 2021/3/3
 * desc: bidding 请求参数.
 */
data class BidRequest(
    @SerializedName("id")
    var id: String?,
    @SerializedName("imp")
    var imp: List<Imp>?,
    @SerializedName("app")
    var app: App?,
    @SerializedName("device")
    var device: Device?,
    @SerializedName("regs")
    var regs: Regs?,
    @SerializedName("user")
    var user: User?,
    @SerializedName("ext")
    var ext: RequestExt?,
    @SerializedName("at")
    var at: Int?,
    @SerializedName("tmax")
    var tmax: Int?,
    @SerializedName("test")
    var test: Int?
)

data class Imp(
    @SerializedName("id")
    var id: String?,
    @SerializedName("banner")
    var banner: Banner?,
    @SerializedName("instl")
    var instl: Int?,
    @SerializedName("tagid")
    var tagid: String?
)

data class App(
    @SerializedName("bundle")
    var bundle: String?,
    @SerializedName("ver")
    var ver: String?,
    @SerializedName("publisher")
    var publisher: Publisher?
)

data class Device(
    @SerializedName("ifa")
    var ifa: String?,
    @SerializedName("ua")
    var ua: String?,
    @SerializedName("dnt")
    var dnt: Int?,
    @SerializedName("ip")
    var ip: String?
)

data class Regs(
    @SerializedName("coppa")
    var coppa: Int?
)

data class User(
    @SerializedName("buyeruid")
    var buyeruid: String?
)

data class RequestExt(
    @SerializedName("platformid")
    var platformid: String?
)

data class Banner(
    @SerializedName("w")
    var w: Int?,
    @SerializedName("h")
    var h: Int?
)

data class Publisher(
    @SerializedName("id")
    var id: String?
)