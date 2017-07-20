package com.qhiehome.ihome.network.service.charge;

import com.qhiehome.ihome.network.model.charge.ChargeRequest;
import com.qhiehome.ihome.network.model.charge.ChargeResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ChargeService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("chargeing")
    Call<ChargeResponse> charge(@Body ChargeRequest chargeRequest);
}
