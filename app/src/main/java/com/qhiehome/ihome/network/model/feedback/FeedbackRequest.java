package com.qhiehome.ihome.network.model.feedback;

public class FeedbackRequest {


    /**
     * advice : advice
     */

    private String advice;

    public FeedbackRequest(String advice) {
        this.advice = advice;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }
}
