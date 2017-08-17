package com.qhiehome.ihome.network.service.avatar;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadAvatarService {
    @Multipart
    @POST("pic/upload")
    Call<ResponseBody> uploadAvatar(@Part MultipartBody.Part image, @Part("phone") RequestBody phoneNum);
}
