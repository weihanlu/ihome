package com.qhiehome.ihome.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.LoginActivity;
import com.qhiehome.ihome.activity.OrderListActivity;
import com.qhiehome.ihome.activity.PublishParkingActivity;
import com.qhiehome.ihome.activity.ReserveListActivity;
import com.qhiehome.ihome.activity.SettingActivity;
import com.qhiehome.ihome.activity.UserInfoActivity;
import com.qhiehome.ihome.adapter.MeAdapter;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.BindArray;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeFragment extends Fragment {

    @BindArray(R.array.titles)
    String[] mTitles;

    public static final String TAG = "MeFragment";

    private Context mContext;

    public MeFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ParkFragment.
     */
    public static MeFragment newInstance() {
        return new MeFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
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
        initToolbar();
        return view;
    }

    private void initToolbar() {
        mToolbar.setTitle("Ihome");
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
                        //BindLockActivity.start(mContext);
                        ReserveListActivity.start(mContext);
                        break;
                    case 2:
                        PublishParkingActivity.start(mContext);
                        break;
                    case 3:
                        OrderListActivity.start(mContext);
                        break;
                    case 4:
                        SettingActivity.start(mContext);
                        break;
                    case 5:
                    default:
                        break;
                }
            }
        });
    }

}
