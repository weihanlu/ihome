package com.qhiehome.ihome.network.service.pay;


import com.qhiehome.ihome.network.model.pay.account.AccountRequest;
import com.qhiehome.ihome.network.model.pay.account.AccountResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by YueMa on 2017/9/19.
 */

public interface AccountService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("account")
    Call<AccountResponse> account(@Body AccountRequest accountRequest);
}
