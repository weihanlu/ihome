package com.qhiehome.ihome.network.service.update;

import com.qhiehome.ihome.network.model.SMS.SMSResponse;
import com.qhiehome.ihome.network.model.update.CheckUpdateResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface PgyService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("view")
    Call<CheckUpdateResponse> checkUpdate(@QueryMap Map<String, String> option);
}
