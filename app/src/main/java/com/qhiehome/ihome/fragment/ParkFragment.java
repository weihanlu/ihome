package com.qhiehome.ihome.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.NaviGuideActivity;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyResponse;
import com.qhiehome.ihome.network.service.inquiry.ParkingEmptyService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    @BindView(R.id.btn_map_location)
    Button mBtnMapLocation;
    Unbinder unbinder;
    @BindView(R.id.btn_map_navi)
    Button mBtnMapNavi;


    private Context mContext;

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LatLng mCurrentPt;
    private LatLng mMyPt;
    private LatLng mPrePt;
    private Marker mMarker;
    private Marker mClickedMarker;
    private BaiduMap.OnMarkerClickListener mOnMarkerClickListener;
    private boolean mRefreshEstate;
    private LocationClient mLocationClient;
    private BDLocationListener mBDLocationListener;
    private Intent mIntent;

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
    /******百度地图导航******/

    private static final float MAP_ZOOM_LEVEL = 15f;
    private static final String LOCATE_RESULT_TYPE = "bd09ll";
    private static final int LOCATE_INTERVAL = 5000;
    private static final String APP_ID = "9901662";
    private static final int RADIUS = 1000;
  
    private static final double REFRESH_DISTANCE = 1000;

    private List<ParkingResponse.DataBean.EstateBean> mEstateBeanList = new ArrayList<>();
    private List<ParkingResponse.DataBean.EstateBean.ParkingBean> mParkingBeanList = new ArrayList<>();


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
        initMap();
        initLocate();
        unbinder = ButterKnife.bind(this, view);

        if (initDirs()) {
            initNavi();
        }
        return view;
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
        settings.setScrollGesturesEnabled(false);//禁用地图拖拽
        settings.setRotateGesturesEnabled(false);//禁用地图旋转
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
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude()).build();
                LatLng xy = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                // 设置定位数据
                mBaiduMap.setMyLocationData(locData);
                if (mRefreshEstate) {
                    mCurrentPt = xy;
                    MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(xy);
                    mBaiduMap.animateMapStatus(status);
                    updateMapState();
                    mRefreshEstate = false;
                    mPrePt = xy;
                }else {
                    double distance = DistanceUtil.getDistance(xy, mPrePt);
                    if (distance >= REFRESH_DISTANCE){
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

    private void updateMapState(){
        ParkingEmptyService parkingEmptyService = ServiceGenerator.createService(ParkingEmptyService.class);
        ParkingEmptyRequest parkingEmptyRequest = new ParkingEmptyRequest(mCurrentPt.longitude, mCurrentPt.latitude, RADIUS);
        Call<ParkingEmptyResponse> call = parkingEmptyService.parkingEmpty(parkingEmptyRequest);
        call.enqueue(new Callback<ParkingEmptyResponse>() {
            @Override
            public void onResponse(Call<ParkingEmptyResponse> call, Response<ParkingEmptyResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE){
                    mEstateBeanList = response.body().getData().getEstate();
                    Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.img_park);
                    // 获得图片的宽高
                    int width = bm.getWidth();
                    int height = bm.getHeight();
                    // 设置想要的大小
                    int newWidth = 60;
                    int newHeight = 60;
                    // 计算缩放比例
                    float scaleWidth = ((float) newWidth) / width;
                    float scaleHeight = ((float) newHeight) / height;
                    // 取得想要缩放的matrix参数
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    // 得到新的图片
                    Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                            true);
                    BitmapDescriptor arrow = BitmapDescriptorFactory.fromBitmap(newbm);

                    for (int i = 0; i<mEstateBeanList.size(); i++){
                        LatLng newPT = new LatLng(mEstateBeanList.get(i).getY(),mEstateBeanList.get(i).getX());
                        OverlayOptions options;
                        options = new MarkerOptions()
                                .position(newPT)//设置位置
                                .icon(arrow);//设置图标样式
                        Bundle bundle = new Bundle();
                        bundle.putString("name", mEstateBeanList.get(i).getName());
                        mMarker = (Marker) mBaiduMap.addOverlay(options);
                        mMarker.setExtraInfo(bundle);
                    }
                }
            }

            @Override
            public void onFailure(Call<ParkingEmptyResponse> call, Throwable t) {
                ToastUtil.showToast(mContext,"网络连接异常");
            }
        });
    }

/*******归位、点击、搜索:待删除*******/
/*    private void updateMapState() {
        //BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.flag);
        if (!mFirstLocation) {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.img_target);
            // 获得图片的宽高
            int width = bm.getWidth();
            int height = bm.getHeight();
            // 设置想要的大小
            int newWidth = 60;
            int newHeight = 60;
            // 计算缩放比例
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // 取得想要缩放的matrix参数
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 得到新的图片
            Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                    true);
            BitmapDescriptor newbmd = BitmapDescriptorFactory.fromBitmap(newbm);

            // 设置覆盖物
            final Marker marker;
            OverlayOptions options;
            options = new MarkerOptions()
                    .position(mCurrentPt)//设置位置
                    .icon(newbmd)//设置图标样式
                    .zIndex(9) // 设置marker所在层级
                    .draggable(true); // 设置手势拖拽;
            marker = (Marker) mBaiduMap.addOverlay(options);

            mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    mCurrentPt = marker.getPosition();
                    mBaiduMap.clear();
                    updateMapState();
                }

                @Override
                public void onMarkerDragStart(Marker marker) {
                }
            });

        } else {
            mFirstLocation = false;
        }
        // 搜索POI
        PoiSearch ps = PoiSearch.newInstance();
        ps.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult == null) {
                    return;
                }
                int resultNum = poiResult.getTotalPoiNum();
                mBaiduMap.removeMarkerClickListener(mOnMarkerClickListener);

                int count = 0;
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.img_park);
                // 获得图片的宽高
                int width = bm.getWidth();
                int height = bm.getHeight();
                // 设置想要的大小
                int newWidth = 60;
                int newHeight = 60;
                // 计算缩放比例
                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;
                // 取得想要缩放的matrix参数
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                // 得到新的图片
                Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                        true);
                BitmapDescriptor arrow = BitmapDescriptorFactory.fromBitmap(newbm);


                while (count < 10 && count < resultNum) {   //选择附近最多10个停车场
                    //while (count < resultNum){      //选择附近所有停车场
                    final PoiInfo pif = poiResult.getAllPoi().get(count);
                    LatLng newPT = pif.location;

                    OverlayOptions options;
                    options = new MarkerOptions()
                            .position(newPT)//设置位置
                            .icon(arrow);//设置图标样式
                    Bundle bundle = new Bundle();
                    bundle.putString("name", pif.name);
                    mMarker = (Marker) mBaiduMap.addOverlay(options);
                    mMarker.setExtraInfo(bundle);

                    count++;
                }

                // 跳转、传递参数
                mOnMarkerClickListener = new BaiduMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker1) {
//                        mIntent = new Intent(getActivity(), ParkingListActivity.class);
//                        //bundle1 = marker1.getExtraInfo();
//                        String currentStr = marker1.getExtraInfo().getString("name");
//                        mIntent.putExtra("name", currentStr);
//                        startActivity(mIntent);
                        mClickedMarker = marker1;
                        showParkingDialog();
                        return false;
                    }
                };
                mBaiduMap.setOnMarkerClickListener(mOnMarkerClickListener);

            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });

        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
        nearbySearchOption.location(mCurrentPt);
        nearbySearchOption.keyword("停车场");
        nearbySearchOption.radius(1000);// 检索半径，单位是米
        nearbySearchOption.pageNum(10);
        ps.searchNearby(nearbySearchOption);// 发起附近检索请求

    }
    private void initListener() {
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mCurrentPt = latLng;
                mBaiduMap.clear();
                updateMapState();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
    }

    @OnClick(R.id.btn_map_location)
    public void onViewClicked() {
        MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(mMyPt);
        mBaiduMap.animateMapStatus(status);
    }

*/         /*******归位、点击、搜索:待删除*******/

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



    /*********显示小区的停车位***********/
    private void showParkingDialog() {

        String estateName = mClickedMarker.getExtraInfo().getString("name");
        //网络请求
        /**********暂时数据************/
        List<Map<String, String>> parking_data = new ArrayList<>();
        final List<CheckBox> cb_all = new ArrayList<>();
        parking_data.add(new HashMap<String, String>() {{
            put("name", "Jack");
            put("time_start", "8");
            put("time_end", "10");
        }});
        parking_data.add(new HashMap<String, String>() {{
            put("name", "Jerry");
            put("time_start", "9");
            put("time_end", "14");
        }});
        parking_data.add(new HashMap<String, String>() {{
            put("name", "Tom");
            put("time_start", "10");
            put("time_end", "13");
        }});
        /**********暂时数据************/
        final int size = parking_data.size();
        int count = 0;
        if (size != 0) {
            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(this.getContext())
                    .positiveText("确定").negativeText("取消");
            dialogBuilder.title(estateName).customView(R.layout.dialog_parking_list, true);
            MaterialDialog dialog = dialogBuilder.build();
            View customView = dialog.getCustomView();
            while (customView != null && count < size) {
                final LinearLayout container = (LinearLayout) customView.findViewById(R.id.parking_list_container);
                View itemContainer = LayoutInflater.from(mContext).inflate(R.layout.item_parking_list, null);
                String name = parking_data.get(count).get("name");
                String time = parking_data.get(count).get("time_start") + "~" + parking_data.get(count).get("time_end");
                TextView tv_name = (TextView) itemContainer.findViewById(R.id.tv_parking_name);
                TextView tv_time = (TextView) itemContainer.findViewById(R.id.tv_parking_time);
                tv_name.setText(name);
                tv_time.setText(time);
                CheckBox cb = (CheckBox) itemContainer.findViewById(R.id.cb_parking);
                cb_all.add(cb);
                container.addView(itemContainer);
                count++;


            }
            dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    int num_chosen = 0;
                    for (int i = 0; i < size; i++) {
                        if (cb_all.get(i).isChecked()) {
                            num_chosen++;
                        }
                    }
                    //发出网络请求if(成功)
                    ToastUtil.showToast(mContext, "已预约" + num_chosen + "车位");
                    //if(失败) 显示网络访问错误
                }
            }).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    ToastUtil.showToast(mContext, "negative");
                }
            });
            dialog.show();
        } else {
            //没有停车位
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
        // TODO Auto-generated method stub

        PackageManager pm = this.getActivity().getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getActivity().getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCompletePhoneAuth() {
        // TODO Auto-generated method stub

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
            case GCJ02: {
                sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
                break;
            }
            case WGS84: {
                sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
                break;
            }
            case BD09_MC: {
                sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
                break;
            }
            case BD09LL: {
                sNode = new BNRoutePlanNode(mCurrentPt.longitude, mCurrentPt.latitude, "我的位置", null, coType);
                //sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", null, coType);
                //网络请求获得目的地经纬度
                eNode = new BNRoutePlanNode(116.40386525193937, 39.915160800132085, "北京天安门", null, coType);
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
        if (BaiduNaviManager.isNaviInited()){
            routeplanToNavi(CoordinateType.BD09LL);
        }
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
            // TODO Auto-generated method stub
            Toast.makeText(mContext, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
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
