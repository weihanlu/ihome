package com.qhiehome.ihome.network.service.lock;

import com.qhiehome.ihome.network.model.lock.unbind.UnbindLockRequest;
import com.qhiehome.ihome.network.model.lock.unbind.UnbindLockResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UnbindLockService {

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("unbind")
    Call<UnbindLockResponse> unbind(@Body UnbindLockRequest unbindLockRequest);
}
