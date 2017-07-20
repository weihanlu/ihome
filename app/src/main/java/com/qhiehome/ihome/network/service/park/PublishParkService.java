package com.qhiehome.ihome.network.service.park;

import com.qhiehome.ihome.network.model.park.publish.PublishparkRequest;
import com.qhiehome.ihome.network.model.park.publish.PublishparkResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PublishParkService {

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("parking/publish")
    Call<PublishparkResponse> publish(@Body PublishparkRequest publishparkRequest);
}
