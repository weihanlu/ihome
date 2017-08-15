package com.qhiehome.ihome.network.service.feedback;

import com.qhiehome.ihome.network.model.feedback.FeedbackRequest;
import com.qhiehome.ihome.network.model.feedback.FeedbackResponse;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdRequest;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FeedbackService {
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("feedback")
    Call<FeedbackResponse> sendFeedback(@Body FeedbackRequest feedbackRequest);
}