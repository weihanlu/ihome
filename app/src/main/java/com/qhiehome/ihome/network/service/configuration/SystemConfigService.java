package com.qhiehome.ihome.network.service.configuration;

import com.qhiehome.ihome.network.model.configuration.system.SystemConfigResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Created by YueMa on 2017/9/1.
 */

public interface SystemConfigService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })

    @GET("configuration/system")
    Call<SystemConfigResponse> querySystemConfig();
}
