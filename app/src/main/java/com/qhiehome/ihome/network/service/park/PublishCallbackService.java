package com.qhiehome.ihome.network.service.park;

import com.qhiehome.ihome.network.model.park.publish.PublishparkRequest;
import com.qhiehome.ihome.network.model.park.publish.PublishparkResponse;
import com.qhiehome.ihome.network.model.park.publishcancel.PublishCancelRequest;
import com.qhiehome.ihome.network.model.park.publishcancel.PublishCancelResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PublishCallbackService {

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("share/callback")
    Call<PublishCancelResponse> callback(@Body PublishCancelRequest publishCancelRequest);
}
