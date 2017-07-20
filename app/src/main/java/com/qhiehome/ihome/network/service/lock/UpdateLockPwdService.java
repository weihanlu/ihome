package com.qhiehome.ihome.network.service.lock;

import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdRequest;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UpdateLockPwdService {

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("lock/pwd")
    Call<UpdateLockPwdResponse> updateLockPwd(@Body UpdateLockPwdRequest updateLockPwdRequest);
}
