package com.qhiehome.ihome.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.BindLockActivity;
import com.qhiehome.ihome.activity.LoginActivity;
import com.qhiehome.ihome.activity.UserInfoActivity;
import com.qhiehome.ihome.adapter.MeAdapter;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.signin.SigninRequest;
import com.qhiehome.ihome.network.model.signin.SigninResponse;
import com.qhiehome.ihome.network.service.SigninService;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.LogUtil;

import java.lang.reflect.Array;

import butterknife.ButterKnife;
import butterknife.BindArray;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeFragment extends Fragment {

    @BindArray(R.array.titles)
    String[] mTitles;

    private static final String TAG = "MeFragment";

    private Context mContext;

    public MeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ParkFragment.
     */
    public static MeFragment newInstance() {
        MeFragment fragment = new MeFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitles = mContext.getResources().getStringArray(R.array.titles);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_me, container, false);
        ButterKnife.bind(this, view);
        initRecyclerView(view);
        return view;
    }

    private void initRecyclerView(View view) {
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv_me);
        rv.setLayoutManager(new LinearLayoutManager(mContext));
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));
        MeAdapter meAdapter = new MeAdapter(mContext, mTitles);
        initListener(meAdapter);
        rv.setAdapter(meAdapter);
    }

    private void initListener(MeAdapter meAdapter) {
        meAdapter.setOnItemClickListener(new MeAdapter.OnClickListener() {
            @Override
            public void onClick(int i) {
                switch (i) {
                    case 0:
                        UserInfoActivity.start(mContext);
                        break;
                    case 1:
                        BindLockActivity.start(mContext);
                        break;
                    case 2:
                        SigninService signinService = ServiceGenerator.createService(SigninService.class);
                        SigninRequest signinRequest = new SigninRequest(EncryptUtil.encrypt("123123", EncryptUtil.ALGO.SHA_256));
                        Call<SigninResponse> call = signinService.signin(signinRequest);
                        call.enqueue(new Callback<SigninResponse>() {
                            @Override
                            public void onResponse(Call<SigninResponse> call, Response<SigninResponse> response) {
                                LogUtil.d(TAG, "response code is " + response.code());
                                if (response.code() == 200) {
                                    LogUtil.d(TAG, "response errmsg is " + response.body().getErrmsg());
                                }
                            }
                            @Override
                            public void onFailure(Call<SigninResponse> call, Throwable t) {

                            }
                        });

                        break;
                    case 3:
                        break;
                    case 4:
                        ActivityManager.finishAll();
                        LoginActivity.start(mContext);
                        break;
                    default:
                        break;
                }
            }
        });
    }

}
