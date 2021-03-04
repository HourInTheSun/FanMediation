package com.bzu.fanmediation

import com.bzu.fanmediation.util.FileUtil
import com.google.gson.JsonObject
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 * author: menglei
 * date: 2021/3/1
 * desc: .
 */
class ServerSettings {
    companion object {
        private const val FILE_SERVER_SETTING = "server.json"
        var serverSetting = JSONObject()
        fun init() {
            thread {
                val serverJson = FileUtil.readStringFromFile(FILE_SERVER_SETTING)
                if (!serverJson.isNullOrEmpty()) {
                    serverSetting = JSONObject(serverJson)
                }
            }
        }

        fun initialize(bidding_source_platforms: String, apps: String) {

        }

        fun savePlacement(json: JSONObject) {
            FileUtil.writeStringToFile(FILE_SERVER_SETTING, json.toString())
        }

        fun getPlacement(appId: String, placementId: String): JSONObject? {
            val serverJson = FileUtil.readStringFromFile(FILE_SERVER_SETTING)
            if (serverJson.isNullOrEmpty()) return null
            val serverSetting = JSONObject(serverJson)
//            val biddingPlatform = serverSetting.getJSONObject("bidding_source_platforms")
            val biddingApps = serverSetting.getJSONArray("apps")
            val appInfo = biddingApps.getJSONObject(0)
            if (appInfo != null && appInfo.getString("app_id") == appId) {
                val placements = appInfo.getJSONArray("placements")
                for (index in 0 until placements.length()) {
                    val placement = placements.getJSONObject(index)
                    if (placement.getString("placement_id") == placementId) {
                        return placement
                    }
                }
                return null
            }
            return null
        }

        fun getPlatform(key: String): JSONObject {
            val serverJson = FileUtil.readStringFromFile(FILE_SERVER_SETTING)
            if (serverJson.isNullOrEmpty()) return JSONObject()
            val serverSetting = JSONObject(serverJson)
            val biddingPlatform = serverSetting.getJSONArray("bidding_source_platforms")
            for (index in 0 until biddingPlatform.length()){
                val platform = biddingPlatform.getJSONObject(index)
                if(platform.getString("platform_name")==key){
                    return platform
                }
            }
            return JSONObject()
        }

        fun setPlatform(key: HashMap<String, Any>) {

        }
    }
}