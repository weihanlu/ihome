package com.qhiehome.ihome.network.service;

import com.qhiehome.ihome.network.model.estatelist.EstateListResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Created by YueMa on 2017/7/20.
 */

public interface EstateListService {
    @Headers({
            "Accept: application/json"
    })
    @GET("estate/all")
    Call<EstateListResponse> estateList();
}
