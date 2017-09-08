package com.qhiehome.ihome.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qhiehome.ihome.R;

import butterknife.ButterKnife;

/**
 * Created by YueMa on 2017/9/8.
 */

public class EstatePassFragment extends Fragment {

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
        View view =  inflater.inflate(R.layout.fragment_estate_pass, container, false);
        ButterKnife.bind(this, view);
        return view;
    }
}
