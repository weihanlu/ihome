package com.qhiehome.ihome.network.service.configuration;

import com.qhiehome.ihome.network.model.configuration.city.CityConfigRequest;
import com.qhiehome.ihome.network.model.configuration.city.CityConfigResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/9/1.
 */

public interface CityConfigService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })

    @POST("configuration/city")
    Call<CityConfigResponse> queryCityConfig(@Body CityConfigRequest cityConfigRequest);
}
