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

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.BindLockActivity;
import com.qhiehome.ihome.activity.LoginActivity;
import com.qhiehome.ihome.adapter.MeAdapter;
import com.qhiehome.ihome.manager.ActivityManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeFragment extends Fragment {

    private Context mContext;
    private String[] mTitles;

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
                        break;
                    case 1:
                        BindLockActivity.start(mContext);
                        break;
                    case 2:
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
