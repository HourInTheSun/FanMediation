package com.bzu.fanmediation

/**
 * author: menglei
 * date: 2021/3/3
 * desc: .
 */
data class FBResponse(
    val bidid: String,
    val cur: String,
    val id: String,
    val seatbid: List<Bid>
)

data class Bid(
    val bid: List<BidResponse>
)

data class BidResponse(
    val id: String,
    val impid: String,
    val ext: Ext,
    val adm: String,
    val lurl: String,
    val nurl: String,
    val price: Float
)

data class Ext(
    val encrypted_cpm: String
)