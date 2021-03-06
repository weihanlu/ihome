package com.qhiehome.ihome.network.service.baiduMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by YueMa on 2017/8/11.
 */

public class BaiduMapServiceGenerator {
    private static final String BASE_URL = "http://api.map.baidu.com/";

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
