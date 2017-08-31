package com.qhiehome.ihome.network.service.crashlog;

import com.qhiehome.ihome.network.model.crashlog.CrashLogRequest;
import com.qhiehome.ihome.network.model.crashlog.CrashLogResponse;
import com.qhiehome.ihome.network.model.feedback.FeedbackRequest;
import com.qhiehome.ihome.network.model.feedback.FeedbackResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface CrashLogService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("crashlog")
    Call<CrashLogResponse> uploadCrashLog(@Body CrashLogRequest crashLogRequest);
}