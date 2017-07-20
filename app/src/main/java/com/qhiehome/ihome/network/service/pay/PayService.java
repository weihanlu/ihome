package com.qhiehome.ihome.network.service.pay;

import com.qhiehome.ihome.network.model.pay.PayRequest;
import com.qhiehome.ihome.network.model.pay.PayResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PayService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("pay")
    Call<PayResponse> pay(@Body PayRequest payRequest);
}
