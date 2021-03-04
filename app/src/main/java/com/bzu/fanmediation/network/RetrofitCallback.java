package com.bzu.fanmediation.network;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * author: menglei
 * date: 2019/2/13
 * desc: .
 */
public abstract class RetrofitCallback<T> implements Callback<T> {
    @Override
    public void onResponse(@NotNull Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onSuccess(call, response);
        } else {
            onFailure(call, new Throwable(response.message()));
        }
    }

    public abstract void onSuccess(Call<T> call, Response<T> response);

    public void onLoading(long current, long total) {
    }
}
