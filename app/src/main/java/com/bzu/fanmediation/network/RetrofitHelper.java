package com.bzu.fanmediation.network;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * author: menglei
 * date: 2018/12/22
 * desc: .
 */
public class RetrofitHelper {
    private static final int CONN_TIMEOUT = 30, READ_TIMEOUT = 30, WRITE_TIMEOUT = 30;

    private static volatile RetrofitHelper mInstance;

    private Retrofit mRetrofit;
    private final OkHttpClient mHttpClient;

    private RetrofitHelper() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        mHttpClient = builder.build();
    }

    public static RetrofitHelper getInstance() {
        if (mInstance == null) {
            synchronized (RetrofitHelper.class) {
                if (mInstance == null) {
                    mInstance = new RetrofitHelper();
                }
            }
        }
        return mInstance;
    }

    public RetrofitHelper buildRetrofit(String baseUrl) {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
//                .client(mHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return this;
    }

    public <T> T createService(Class<T> serviceClass) {
        return mRetrofit.create(serviceClass);
    }

    /**
     * 设置公共参数
     */
    private static Interceptor addQueryParameterInterceptor() {
        return new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request request;
                HttpUrl modifiedUrl = originalRequest.url().newBuilder()
                        .addQueryParameter("c", "" + "-")
                        .build();
                request = originalRequest.newBuilder().url(modifiedUrl).build();
                return chain.proceed(request);
            }
        };
    }

    /**
     * 设置头
     */
    private static Interceptor addHeaderInterceptor() {
        return new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request.Builder requestBuilder = originalRequest.newBuilder()
                        // Provide your custom header here
                        .header("AppType", "TPOS")
                        .header("Accept", "application/json")
                        .header("c", "")
                        .method(originalRequest.method(), originalRequest.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };
    }

    /**
     * 设置缓存
     */
    private static Interceptor addCacheInterceptor() {
        return new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Request request = chain.request();
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                Response response = chain.proceed(request);
                int maxAge = 0;
                response.newBuilder()
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        // 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .removeHeader("Retrofit")
                        .build();
                return response;
            }
        };
    }
}
