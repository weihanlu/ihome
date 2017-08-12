package com.qhiehome.ihome.network.service.update;

import com.qhiehome.ihome.network.model.SMS.SMSResponse;
import com.qhiehome.ihome.network.model.update.CheckUpdateResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface PgyService {
    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "Accept: application/json"
    })
    @POST("listMyPublished")
    Call<CheckUpdateResponse> getLatestVersion(@Field("uKey") String uKey, @Field("_api_key") String apiKey, @Field("page") String page);
}
