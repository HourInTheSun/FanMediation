package com.bzu.fanmediation.network;

import com.bzu.fanmediation.FBResponse;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * author: menglei
 * date: 2021/3/1
 * desc: .
 */
interface ServerService {
    @POST("https://an.facebook.com/{PLATFORM_ID}/placementbid.ortb")
    Call<FBResponse> getBidResponse(@Path("PLATFORM_ID") String appId,
                                    @HeaderMap Map<String, String> headers,
                                    @Body RequestBody params);
}
