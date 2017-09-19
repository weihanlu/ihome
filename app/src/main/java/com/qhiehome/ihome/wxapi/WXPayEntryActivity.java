package com.qhiehome.ihome.wxapi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.BaseActivity;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.ToastUtil;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends BaseActivity implements IWXAPIEventHandler{

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxpay_entry);
        api = WXAPIFactory.createWXAPI(this, Constant.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }



    @Override
    public void onResp(BaseResp resp) {
        switch (resp.errCode){
            case 0:
                ToastUtil.showToast(mContext, "errorCode 0");
                break;
            case -1:
                ToastUtil.showToast(mContext, "errorCode -1");
                break;
            case -2:
                ToastUtil.showToast(mContext, "errorCode -2");
                break;
            default:
                break;
        }

    }





}
