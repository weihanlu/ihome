package com.qhiehome.ihome.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.NaviGuideActivity;
import com.qhiehome.ihome.fragment.ParkFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by YueMa on 2017/8/30.
 */

public class NaviUtil {

    /******百度地图导航******/
    private String mSDCardPath = null;
    private static final String APP_FOLDER_NAME = "ihome";
    private final static String authBaseArr[] =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    private final static String authComArr[] = {Manifest.permission.READ_PHONE_STATE};
    private final static int authBaseRequestCode = 1;
    private final static int authComRequestCode = 2;
    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    public static List<Activity> activityList = new LinkedList<Activity>();
    public static final String RESET_END_NODE = "resetEndNode";
    private BNRoutePlanNode.CoordinateType mCoordinateType;
    private static final String APP_ID = "9901662";

    private Context mContext;
    private Activity mActivity;
    private LatLng sNodeLocation;
    private LatLng eNodeLocation;
    private String sNodeName;
    private String eNodeName;

    private static class NaviUtilHolder {
        private static final NaviUtil INSTANCE = new NaviUtil();
    }

    public static NaviUtil getInstance() {
        return NaviUtilHolder.INSTANCE;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void setsNodeLocation(LatLng sNodeLocation) {
        this.sNodeLocation = sNodeLocation;
    }

    public void seteNodeLocation(LatLng eNodeLocation) {
        this.eNodeLocation = eNodeLocation;
    }

    public void setsNodeName(String sNodeName) {
        this.sNodeName = sNodeName;
    }

    public void seteNodeName(String eNodeName) {
        this.eNodeName = eNodeName;
    }

    /*********导航***********/

    public boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    String authinfo = null;
    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                     showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                     showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
            // showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
            // showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

    public void showToastMsg(final String msg) {

//        mActivity.runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
////                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private boolean hasBasePhoneAuth() {

        PackageManager pm = mActivity.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, mActivity.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCompletePhoneAuth() {

        PackageManager pm = mActivity.getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, mActivity.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void initNavi() {

        BNOuterTTSPlayerCallback ttsCallback = null;

        // 申请权限
        if (Build.VERSION.SDK_INT >= 23) {

            if (!hasBasePhoneAuth()) {

                mActivity.requestPermissions(authBaseArr, authBaseRequestCode);
                return;

            }
        }

        BaiduNaviManager.getInstance().init(mActivity, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + msg;
                }
//                mActivity.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Toast.makeText(mContext, authinfo, Toast.LENGTH_LONG).show();
//                    }
//                });
            }

            public void initSuccess() {
//                Toast.makeText(mContext, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
//                Toast.makeText(mContext, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
//                Toast.makeText(mContext, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
            }

        }, null, ttsHandler, ttsPlayStateListener);

    }

    private void initSetting() {
        // BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        // BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
        BNaviSettingManager.setIsAutoQuitWhenArrived(true);
        Bundle bundle = new Bundle();
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, APP_ID);
        BNaviSettingManager.setNaviSdkParam(bundle);
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }


    public void routeplanToNavi(BNRoutePlanNode.CoordinateType coType) {
        mCoordinateType = coType;
        if (!hasInitSuccess) {
//            Toast.makeText(mContext, "还未初始化!", Toast.LENGTH_SHORT).show();
        }
        // 权限申请
        if (Build.VERSION.SDK_INT >= 23) {
            // 保证导航功能完备
            if (!hasCompletePhoneAuth()) {
                if (!hasRequestComAuth) {
                    hasRequestComAuth = true;
                    mActivity.requestPermissions(authComArr, authComRequestCode);
                    return;
                } else {
//                    Toast.makeText(mContext, "没有完备的权限!", Toast.LENGTH_SHORT).show();
                }
            }

        }
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
        switch (coType) {
//            case GCJ02: {
//                sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
//                break;
//            }
//            case WGS84: {
//                sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
//                break;
//            }
//            case BD09_MC: {
//                sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
//                break;
//            }
            case BD09LL: {
                sNode = new BNRoutePlanNode(sNodeLocation.longitude, sNodeLocation.latitude, sNodeName, null, coType);
                eNode = new BNRoutePlanNode(eNodeLocation.longitude, eNodeLocation.latitude, eNodeName, null, coType);
                //查询数据库得到目的地经纬度
//                mParkingReadDB = mParkingSQLHelper.getReadableDatabase();
//                Cursor cursor = mParkingReadDB.query(ParkingSQLHelper.TABLE_NAME,
//                        new String[]{"startTime", "estateName", "x", "y"},
//                        null, null, null, null, "startTime ASC");
//                while (cursor.moveToNext()) {
//                    long startTime = cursor.getLong(cursor.getColumnIndex("startTime"));
//                    if (startTime >= System.currentTimeMillis()) {
//                        String estateName = cursor.getString(cursor.getColumnIndex("estateName"));
//                        double x = cursor.getDouble(cursor.getColumnIndex("x"));
//                        double y = cursor.getDouble(cursor.getColumnIndex("y"));
//                        eNode = new BNRoutePlanNode(x, y, estateName, null, coType);
//                        break;
//                    }
//                }


//                mParkingReadDB.close();
                break;
            }
            default:
                ;
        }
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);

            // 开发者可以使用旧的算路接口，也可以使用新的算路接口,可以接收诱导信息等
            BaiduNaviManager.getInstance().launchNavigator(mActivity, list, 1, true, new DemoRoutePlanListener(sNode));
            //BaiduNaviManager.getInstance().launchNavigator(this.getActivity(), list, 1, true, new DemoRoutePlanListener(sNode),
            //        eventListerner);
        }
    }

    BaiduNaviManager.NavEventListener eventListerner = new BaiduNaviManager.NavEventListener() {

        @Override
        public void onCommonEventCall(int what, int arg1, int arg2, Bundle bundle) {
            //BNEventHandler.getInstance().handleNaviEvent(what, arg1, arg2, bundle);
        }
    };


    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            /*
             * 设置途径点以及resetEndNode会回调该接口
             */

            for (Activity ac : activityList) {

                if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {

                    return;
                }
            }
            Intent intent = new Intent(mActivity, NaviGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            mContext.startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
//            Toast.makeText(mContext, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

}
