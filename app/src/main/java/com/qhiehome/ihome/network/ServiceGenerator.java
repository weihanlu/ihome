package com.qhiehome.ihome.network;

import android.app.Service;

import com.qhiehome.ihome.network.service.signin.SigninService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class ServiceGenerator {

    private static final String BASE_URL = "http://39.108.77.50:3389/api/";

    private static Retrofit.Builder retrofitBuilder
            = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create());

    private static OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

    public static <T> T createService(Class<T> serviceClass) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClientBuilder.addInterceptor(logging);
        retrofitBuilder.client(okHttpClientBuilder.build());
        Retrofit retrofit = retrofitBuilder.build();
        return retrofit.create(serviceClass);
    }

}
