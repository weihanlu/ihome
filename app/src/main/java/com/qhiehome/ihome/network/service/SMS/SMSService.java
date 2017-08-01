package com.qhiehome.ihome.network.service.SMS;

import com.qhiehome.ihome.network.model.SMS.SMSResponse;

import java.util.Map;


import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by YueMa on 2017/7/31.
 */

public interface SMSService {

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("send")
    Call<SMSResponse> sendSMS(@QueryMap Map<String, Object> option);

}
