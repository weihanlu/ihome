package com.qhiehome.ihome.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.service.estate.DownloadEstatePassService;
import com.qhiehome.ihome.util.EncryptUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by YueMa on 2017/9/8.
 */

public class EstatePassFragment extends Fragment {

    @BindView(R.id.iv_estate_pass)
    ImageView mIvEstatePass;
    Unbinder unbinder;

    public EstatePassFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_estate_pass, container, false);
        unbinder = ButterKnife.bind(this, view);
        DownloadPass();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void DownloadPass(){
        DownloadEstatePassService downloadEstatePassService = ServiceGenerator.createService(DownloadEstatePassService.class);
        String encryptedAvatarName = EncryptUtil.encrypt("pass", EncryptUtil.ALGO.MD5);
        Call<ResponseBody> call = downloadEstatePassService.downloadPass(encryptedAvatarName + ".jpg");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(response.body().bytes(), 0, response.body().bytes().length, new BitmapFactory.Options());
                        mIvEstatePass.setImageBitmap(bitmap);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    //图片加载错误
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                //图片加载错误
            }
        });
    }
}
