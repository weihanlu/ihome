package com.qhiehome.ihome.network.service.inquiry;

import com.qhiehome.ihome.network.model.inquiry.reserveowned.ReserveOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.reserveowned.ReserveOwnedResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ReserveOwnedService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("reservation/all")
    Call<ReserveOwnedResponse> reserveOwned(@Body ReserveOwnedRequest reserveOwnedRequest);
}
