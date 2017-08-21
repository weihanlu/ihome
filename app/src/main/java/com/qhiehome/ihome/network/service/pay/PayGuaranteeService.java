package com.qhiehome.ihome.network.service.pay;

import com.qhiehome.ihome.network.model.pay.guarantee.PayGuaranteeRequest;
import com.qhiehome.ihome.network.model.pay.guarantee.PayGuaranteeResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/8/21.
 */

public interface PayGuaranteeService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("pay/guarantee")
    Call<PayGuaranteeResponse> payGuarantee(@Body PayGuaranteeRequest payGuaranteeRequest);
}
