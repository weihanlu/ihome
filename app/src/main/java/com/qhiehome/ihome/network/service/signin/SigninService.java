package com.qhiehome.ihome.network.service.signin;

import com.qhiehome.ihome.network.model.signin.SigninRequest;
import com.qhiehome.ihome.network.model.signin.SigninResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SigninService {

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("signin")
    Call<SigninResponse> signin(@Body SigninRequest signinRequest);
}
