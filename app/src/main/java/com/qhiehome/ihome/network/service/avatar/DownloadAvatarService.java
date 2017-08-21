package com.qhiehome.ihome.network.service.avatar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DownloadAvatarService {

    @GET("pic/{avatar}")
    Call<ResponseBody> downloadAvatar(@Path("avatar") String avatarPath);
}
