package com.qhiehome.ihome.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.MapSearchActivity;
import com.qhiehome.ihome.activity.NaviGuideActivity;
import com.qhiehome.ihome.activity.ParkingListActivity;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyResponse;
import com.qhiehome.ihome.network.service.inquiry.ParkingEmptyService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * This fragment show the nearest districts around the user
 * according to the current location info.
 */
public class ParkFragment extends Fragment {


    public static final String TAG = "ParkFragment";

    public static final int REQUEST_CODE = 1;

    @BindView(R.id.btn_map_location)
    Button mBtnMapLocation;
    Unbinder unbinder;
    @BindView(R.id.btn_map_navi)
    Button mBtnMapNavi;
    @BindView(R.id.btn_map_refresh)
    Button mBtnMapRefresh;
    @BindView(R.id.btn_map_marker)
    Button mBtnMapMarker;
    @BindView(R.id.btn_map_search)
    Button mBtnMapSearch;


    private Context mContext;

    private MapView mMapView;
    private Toolbar mTbMap;
    private BaiduMap mBaiduMap;
    private LatLng mCurrentPt;
    private LatLng mMyPt;
    private LatLng mPrePt;
    private Marker mMarker;
    private Marker mClickedMarker;
    private boolean mIsSearch;
    private BaiduMap.OnMarkerClickListener mOnMarkerClickListener;
    private boolean mRefreshEstate;
    private LocationClient mLocationClient;
    private BDLocationListener mBDLocationListener;


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
    private CoordinateType mCoordinateType;

    private static final float MAP_ZOOM_LEVEL = 15f;
    private static final String LOCATE_RESULT_TYPE = "bd09ll";
    private static final int LOCATE_INTERVAL = 5000;
    private static final String APP_ID = "9901662";
    private static final int RADIUS = 5000;

    private static final double REFRESH_DISTANCE = 1000;
    private static final SimpleDateFormat START_DATE_FORMATE = new SimpleDateFormat("MM-dd HH:mm");
    private static final SimpleDateFormat END_DATE_FORMATE = new SimpleDateFormat("HH:mm");

    private List<ParkingResponse.DataBean.EstateBean> mEstateBeanList = new ArrayList<>();
    private List<ParkingResponse.DataBean.EstateBean.ParkingBean> mParkingBeanList = new ArrayList<>();

    //private ParkingSQLHelper mParkingSQLHelper;
    //private SQLiteDatabase mParkingReadDB;
    //private SQLiteDatabase mParkingWriteDB;
    private int mChosenCount;

    private boolean mMapStateParkingNum = true;
    private String mCity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_park, container, false);
        initView(view);
        mTbMap = (Toolbar) view.findViewById(R.id.tb_map);
        initToolbar();
        initMap();
        initLocate();
        unbinder = ButterKnife.bind(this, view);
//        mParkingSQLHelper = new ParkingSQLHelper(mContext);
//        if (initDirs()) {
//            initNavi();
//        }
        //暂时不需定时器
//        AlarmTimer.setRepeatAlarmTime(mContext, System.currentTimeMillis(),
//                10 * 1000, Constant.TIMER_ACTION, AlarmManager.RTC_WAKEUP);
        mIsSearch = false;
        return view;
    }

    private void initToolbar() {
        mTbMap.setTitle("Ihome");
    }

    @Override
    public void onStart() {
        // 如果要显示位置图标,必须先开启图层定位
        super.onStart();
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }

    }

    @Override
    public void onDestroy() {
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(mBDLocationListener);
        }
        mMapView.onDestroy();
        super.onDestroy();
    }

    public ParkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ParkFragment.
     */
    public static ParkFragment newInstance() {
        return new ParkFragment();
    }

    private void initView(View view) {
        mMapView = (MapView) view.findViewById(R.id.mv_park);
    }

    /**
     * 设置百度地图的缩放级别
     */
    private void initMap() {
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(MAP_ZOOM_LEVEL);
        mBaiduMap.setMapStatus(msu);
        mBaiduMap.setMyLocationEnabled(true);
        UiSettings settings = mBaiduMap.getUiSettings();
        //settings.setScrollGesturesEnabled(false);//禁用地图拖拽
        settings.setRotateGesturesEnabled(false);//禁用地图旋转
        settings.setOverlookingGesturesEnabled(false);
    }

    /**
     * 定位相关配置
     */
    private void initLocate() {
        // 定位初始化
        mLocationClient = new LocationClient(mContext.getApplicationContext());
        mRefreshEstate = true;
        // 设置定位的相关配置
        LocationClientOption locOption = new LocationClientOption();
        locOption.setOpenGps(true);
        locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        locOption.setCoorType(LOCATE_RESULT_TYPE);// 设置定位结果类型
        locOption.setScanSpan(LOCATE_INTERVAL);// 设置发起定位请求的间隔时间,ms
        locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向
        mLocationClient.setLocOption(locOption);

//        // 设置自定义图标
//        BitmapDescriptor myMarker = BitmapDescriptorFactory
//                .fromResource(R.drawable.navi_map);
//        MyLocationConfigeration config = new MyLocationConfigeration(
//                MyLocationConfigeration.LocationMode.FOLLOWING, true, myMarker);

        mBDLocationListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation == null || mMapView == null) {
                    return;
                }
                mCity = bdLocation.getCity();
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude()).build();
                LatLng xy = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                SharedPreferenceUtil.setFloat(mContext, Constant.CURRENT_LATITUDE, (float) bdLocation.getLatitude());
                SharedPreferenceUtil.setFloat(mContext, Constant.CURRENT_LONGITUDE, (float) bdLocation.getLongitude());
                // 设置定位数据
                mBaiduMap.setMyLocationData(locData);
                if (mRefreshEstate) {
                    mCurrentPt = xy;
                    MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(xy);
                    mBaiduMap.animateMapStatus(status);
                    updateMapState(mCurrentPt);
                    mRefreshEstate = false;
                    mPrePt = xy;
                } else {
                    double distance = DistanceUtil.getDistance(xy, mPrePt);
                    if (distance >= REFRESH_DISTANCE) {
                        mRefreshEstate = true;
                    }
                }
            }

            @Override
            public void onConnectHotSpotMessage(String s, int i) {

            }
        };
        mLocationClient.registerLocationListener(mBDLocationListener);
        mLocationClient.start();
        /*
         * 在请求定位之前应该确定mCLient已经启动
         */
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.requestLocation();
            //onReceiveLocation();将得到定位数据
        }
    }

    // if this view is visible to user, start to request user location
    // if this view is not visible to user, stop to request user
    // location
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {        //核心方法，避免因Fragment跳转导致地图崩溃
        super.setUserVisibleHint(isVisibleToUser);
        LogUtil.i(TAG, "isVisibleToUser value is " + isVisibleToUser);
        if (isVisibleToUser) {
            startRequestLocation();
        } else {
            stopRequestLocation();
        }
    }

    private void startRequestLocation() {
        if (mLocationClient != null) {
            mLocationClient.registerLocationListener(mBDLocationListener);
            mLocationClient.start();
            mLocationClient.requestLocation();
        }
    }

    private void stopRequestLocation() {
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(mBDLocationListener);
            mLocationClient.stop();
        }
    }

    private void updateMapState(LatLng pt) {
        mBaiduMap.removeMarkerClickListener(mOnMarkerClickListener);
        ParkingEmptyService parkingEmptyService = ServiceGenerator.createService(ParkingEmptyService.class);
        ParkingEmptyRequest parkingEmptyRequest = new ParkingEmptyRequest(pt.longitude, pt.latitude, RADIUS);
        Call<ParkingEmptyResponse> call = parkingEmptyService.parkingEmpty(parkingEmptyRequest);
        call.enqueue(new Callback<ParkingEmptyResponse>() {
            @Override
            public void onResponse(Call<ParkingEmptyResponse> call, Response<ParkingEmptyResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    mEstateBeanList = response.body().getData().getEstate();
//                    Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.img_park);
//                    // 获得图片的宽高
//                    int width = bm.getWidth();
//                    int height = bm.getHeight();
//                    // 设置想要的大小
//                    int newWidth = 60;
//                    int newHeight = 60;
//                    // 计算缩放比例
//                    float scaleWidth = ((float) newWidth) / width;
//                    float scaleHeight = ((float) newHeight) / height;
//                    // 取得想要缩放的matrix参数
//                    Matrix matrix = new Matrix();
//                    matrix.postScale(scaleWidth, scaleHeight);
//                    // 得到新的图片
//                    Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
//                            true);
//                    BitmapDescriptor arrow = BitmapDescriptorFactory.fromBitmap(newbm);
                    addMarkers();

                }
            }

            @Override
            public void onFailure(Call<ParkingEmptyResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });

        mOnMarkerClickListener = new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker1) {
                mClickedMarker = marker1;
                Intent intent = new Intent(getActivity(), ParkingListActivity.class);
                Bundle bundle = marker1.getExtraInfo();
                intent.putExtras(bundle);
                startActivity(intent);
                //showParkingDialog();
                return false;
            }
        };
        mBaiduMap.setOnMarkerClickListener(mOnMarkerClickListener);
    }


    private void addMarkers() {
        mBaiduMap.clear();
        if (mIsSearch){
            //添加图标
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.img_target);
            int width = bm.getWidth();
            int height = bm.getHeight();
            int newWidth = 60;
            int newHeight = 60;
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                    true);
            BitmapDescriptor flag = BitmapDescriptorFactory.fromBitmap(newbm);
            MarkerOptions searchOptions = new MarkerOptions()
                    .position(mMyPt)//设置位置
                    .icon(flag);//设置图标样式
            mMarker = (Marker) mBaiduMap.addOverlay(searchOptions);
        }
        for (int i = 0; i < mEstateBeanList.size(); i++) {
            boolean hasShare = false;
            for (int j = 0; j < mEstateBeanList.get(i).getParking().size(); j++) {
                if (mEstateBeanList.get(i).getParking().get(j).getShare().size() != 0) {
                    hasShare = true;
                    break;
                }
            }
            if (hasShare) {
                LatLng newPT = new LatLng(mEstateBeanList.get(i).getY(), mEstateBeanList.get(i).getX());
                OverlayOptions options;

                View customMarker = View.inflate(mContext, R.layout.custom_map_marker, null);
                TextView tv_marker = (TextView) customMarker.findViewById(R.id.tv_marker);
                if (mMapStateParkingNum) {
                    tv_marker.setText(String.valueOf(mEstateBeanList.get(i).getParking().size()));
                    tv_marker.setTextColor(Resources.getSystem().getColor(android.R.color.holo_green_light));
                } else {
                    tv_marker.setText(String.format("%d", mEstateBeanList.get(i).getUnitPrice()));
                    tv_marker.setTextColor(Resources.getSystem().getColor(android.R.color.holo_red_light));
                }
                customMarker.setDrawingCacheEnabled(true);
                customMarker.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                customMarker.layout(0, 0,
                        customMarker.getMeasuredWidth(),
                        customMarker.getMeasuredHeight());

                customMarker.buildDrawingCache();
                Bitmap cacheBitmap = customMarker.getDrawingCache();
                Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

                options = new MarkerOptions()
                        .position(newPT)//设置位置
                        .animateType(MarkerOptions.MarkerAnimateType.grow)
                        .icon(bitmapDescriptor);//设置图标样式
                Bundle bundle = new Bundle();
                bundle.putSerializable("estate", mEstateBeanList.get(i));
                //bundle.putString("name", mEstateBeanList.get(i).getName());
                mMarker = (Marker) mBaiduMap.addOverlay(options);
                mMarker.setExtraInfo(bundle);
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_map_location)
    public void onLocationClicked() {
        mMyPt = mCurrentPt;
        mIsSearch = false;
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(mCurrentPt)
                .zoom(MAP_ZOOM_LEVEL)
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        updateMapState(mMyPt);
    }


    @OnClick(R.id.btn_map_refresh)
    public void onRefreshClicked() {
        updateMapState(mMyPt);
    }

    @OnClick(R.id.btn_map_marker)
    public void onChangeMarkerClicked() {
        mMapStateParkingNum = !mMapStateParkingNum;
        if (mMapStateParkingNum) {
            mBtnMapMarker.setText("显示单价");
        } else {
            mBtnMapMarker.setText("显示车位");
        }
        addMarkers();
    }


    @OnClick(R.id.btn_map_search)
    public void onViewClicked() {
        Intent intent = new Intent(getActivity(), MapSearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("city", mCity);
        intent.putExtras(bundle);
        getActivity().startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                //接收数据，改变地图中心
                Bundle bundle = data.getExtras();
                LatLng searchPt = new LatLng(bundle.getDouble("latitude"), bundle.getDouble("longitude"));
                mMyPt = searchPt;
                MapStatus mMapStatus = new MapStatus.Builder()
                        .target(searchPt)
                        .zoom(MAP_ZOOM_LEVEL)
                        .build();
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                //刷新附近停车场
                mIsSearch = true;
                updateMapState(searchPt);
            }
        }
    }




    static class startTimeComparator implements Comparator {
        @Override
        public int compare(Object o, Object t1) {
            ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean shareBean1 = (ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean) o;
            ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean shareBean2 = (ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean) t1;
            if (shareBean1.getStartTime() != shareBean2.getStartTime()) {
                return Long.valueOf(shareBean1.getStartTime()).compareTo(shareBean2.getStartTime());
            } else {
                return Long.valueOf(shareBean1.getEndTime()).compareTo(shareBean2.getEndTime());
            }
        }
    }

    /*********导航***********/

    private boolean initDirs() {
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
                    // showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    // showToastMsg("Handler : TTS play end");
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

        this.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasBasePhoneAuth() {

        PackageManager pm = this.getActivity().getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getActivity().getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCompletePhoneAuth() {

        PackageManager pm = this.getActivity().getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, this.getActivity().getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initNavi() {

        BNOuterTTSPlayerCallback ttsCallback = null;

        // 申请权限
        if (Build.VERSION.SDK_INT >= 23) {

            if (!hasBasePhoneAuth()) {

                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;

            }
        }

        BaiduNaviManager.getInstance().init(this.getActivity(), mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + msg;
                }
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mContext, authinfo, Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void initSuccess() {
                Toast.makeText(mContext, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
                Toast.makeText(mContext, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
                Toast.makeText(mContext, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
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

    private void addDestInfoOverlay(LatLng latLng) {
        mBaiduMap.clear();
        OverlayOptions options = new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_car))
                .zIndex(5);
        mBaiduMap.addOverlay(options);
    }

    private void routeplanToNavi(CoordinateType coType) {
        mCoordinateType = coType;
        if (!hasInitSuccess) {
            Toast.makeText(mContext, "还未初始化!", Toast.LENGTH_SHORT).show();
        }
        // 权限申请
        if (Build.VERSION.SDK_INT >= 23) {
            // 保证导航功能完备
            if (!hasCompletePhoneAuth()) {
                if (!hasRequestComAuth) {
                    hasRequestComAuth = true;
                    this.requestPermissions(authComArr, authComRequestCode);
                    return;
                } else {
                    Toast.makeText(mContext, "没有完备的权限!", Toast.LENGTH_SHORT).show();
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
                sNode = new BNRoutePlanNode(mCurrentPt.longitude, mCurrentPt.latitude, "我的位置", null, coType);
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
            BaiduNaviManager.getInstance().launchNavigator(this.getActivity(), list, 1, true, new DemoRoutePlanListener(sNode));
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

    @OnClick(R.id.btn_map_navi)
    public void onNaviClicked() {
//        if (BaiduNaviManager.isNaviInited()) {
//            routeplanToNavi(CoordinateType.BD09LL);
//        }
    }

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
            Intent intent = new Intent(getActivity(), NaviGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            Toast.makeText(mContext, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                } else {
                    Toast.makeText(mContext, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            initNavi();
        } else if (requestCode == authComRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                }
            }
            routeplanToNavi(mCoordinateType);
        }

    }


}
