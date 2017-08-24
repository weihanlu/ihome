package com.qhiehome.ihome.network.service.pay;

import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceRequest;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/8/24.
 */

public interface AccountBalanceService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("account")
    Call<AccountBalanceResponse> account(@Body AccountBalanceRequest accountBalanceRequest);
}
