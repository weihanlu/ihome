package com.qhiehome.ihome.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ZoomControls;

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
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.CityActivity;
import com.qhiehome.ihome.activity.MainActivity;
import com.qhiehome.ihome.activity.MapSearchActivity;
import com.qhiehome.ihome.activity.ParkingListActivity;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.baiduMap.BaiduMapResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyResponse;
import com.qhiehome.ihome.network.service.baiduMap.BaiduMapService;
import com.qhiehome.ihome.network.service.baiduMap.BaiduMapServiceGenerator;
import com.qhiehome.ihome.network.service.inquiry.ParkingEmptyService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.NaviUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.SharePopupWindow;

import java.text.SimpleDateFormat;
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

    public static final int REQUEST_CODE_SEARCH = 5;

    public static final int REQUEST_CODE_CITY = 6;

    @BindView(R.id.iv_map_location)
    ImageView mIvMapLocation;
    Unbinder unbinder;
    @BindView(R.id.iv_map_refresh)
    ImageView mIvMapRefresh;
    @BindView(R.id.iv_map_marker)
    ImageView mIvMapMarker;
    @BindView((R.id.tv_current_city))
    TextView mTvCurrentCity;
    @BindView(R.id.iv_map_navi)
    ImageView mIvMapNavi;

    private Context mContext;

    private boolean isGetCurrentCity;

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
    private boolean mHasInit = false;

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

    private List<ParkingEmptyResponse.DataBean.EstateBean> mEstateBeanList = new ArrayList<>();
    private List<ParkingEmptyResponse.DataBean.EstateBean.ParkingListBean> mParkingBeanList = new ArrayList<>();

    //private ParkingSQLHelper mParkingSQLHelper;
    //private SQLiteDatabase mParkingReadDB;
    //private SQLiteDatabase mParkingWriteDB;
    private int mChosenCount;

    private boolean mMapStateParkingNum = true;
    private String mCity;
    private String mCurrentCity;

    private NaviUtil mNavi;

    private View mView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_park, container, false);
        unbinder = ButterKnife.bind(this, mView);
        initView(mView);
        initMap();
        initLocate();
//        mParkingSQLHelper = new ParkingSQLHelper(mContext);
        mNavi = NaviUtil.getInstance();
        mNavi.setmActivity(this.getActivity());
        mNavi.setmContext(mContext);
        if (mNavi.initDirs()) {
            mNavi.initNavi();
        }
        //暂时不需定时器
//        AlarmTimer.setRepeatAlarmTime(mContext, System.currentTimeMillis(),
//                10 * 1000, Constant.TIMER_ACTION, AlarmManager.RTC_WAKEUP);
        mIsSearch = false;
        return mView;
    }

    private void initToolbar() {
//        mTbMap.setTitle("Ihome");
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
        if (mHasInit) {
            mIvMapRefresh.performClick();
        }
        if (SharedPreferenceUtil.getInt(mContext, Constant.ORDER_STATE, 0) == 0) {
            mIvMapNavi.setVisibility(View.GONE);
        }else {
            mIvMapNavi.setVisibility(View.VISIBLE);
        }

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
        //隐藏logo
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }
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

        // 设置自定义图标
//        BitmapDescriptor myMarker = BitmapDescriptorFactory
//                .fromResource(R.drawable.btn_nav);
//        MyLocationConfiguration config = new MyLocationConfiguration(
//                MyLocationConfiguration.LocationMode.FOLLOWING, true, myMarker);
//        mBaiduMap.setMyLocationConfiguration(config);
        mBDLocationListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(final BDLocation bdLocation) {
                if (bdLocation == null || mMapView == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isGetCurrentCity) {
                            mCity = bdLocation.getCity();
                            mCurrentCity = bdLocation.getCity();
                            mTvCurrentCity.setText(mCity);
                            isGetCurrentCity = true;
                        }
                    }
                });
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
//                Intent intent = new Intent(getActivity(), ParkingTimelineActivity.class);
                Bundle bundle = marker1.getExtraInfo();
                intent.putExtras(bundle);
                startActivity(intent);
                //showParkingDialog();
                return false;
            }
        };
        mBaiduMap.setOnMarkerClickListener(mOnMarkerClickListener);
        mHasInit = true;
    }


    private void addMarkers() {
        mBaiduMap.clear();
        if (mIsSearch) {
            //添加图标
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.img_pin);
//            int width = bm.getWidth();
//            int height = bm.getHeight();
//            int newWidth = 60;
//            int newHeight = 60;
//            float scaleWidth = ((float) newWidth) / width;
//            float scaleHeight = ((float) newHeight) / height;
//            Matrix matrix = new Matrix();
//            matrix.postScale(scaleWidth, scaleHeight);
//            Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
//                    true);
            BitmapDescriptor flag = BitmapDescriptorFactory.fromBitmap(bm);
            MarkerOptions searchOptions = new MarkerOptions()
                    .position(mMyPt)//设置位置
                    .icon(flag)
                    .zIndex(9);//设置图标样式
            mMarker = (Marker) mBaiduMap.addOverlay(searchOptions);
        }
        for (int i = 0; i < mEstateBeanList.size(); i++) {
            boolean hasShare = false;
            for (int j = 0; j < mEstateBeanList.get(i).getParkingList().size(); j++) {
                if (mEstateBeanList.get(i).getParkingList().get(j).getShareList().size() != 0) {
                    hasShare = true;
                    break;
                }
            }
            if (hasShare) {
                LatLng newPT = new LatLng(mEstateBeanList.get(i).getY(), mEstateBeanList.get(i).getX());
                OverlayOptions options;

                View customMarker = View.inflate(mContext, R.layout.custom_map_marker, null);
                TextView tv_marker = (TextView) customMarker.findViewById(R.id.tv_marker);
                ImageView iv_marker = (ImageView) customMarker.findViewById(R.id.iv_marker);
                if (mMapStateParkingNum) {
                    iv_marker.setImageResource(R.drawable.img_bluemark);
                    int shareNum = 0;
                    for (int j = 0; j < mEstateBeanList.get(i).getParkingList().size(); j++) {
                        shareNum += mEstateBeanList.get(i).getParkingList().get(j).getShareList().size();
                    }
                    tv_marker.setText(String.valueOf(shareNum));
                } else {
                    iv_marker.setImageResource(R.drawable.img_redmark);
                    tv_marker.setText(String.format("%d", mEstateBeanList.get(i).getUnitPrice()));
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
                        .icon(bitmapDescriptor)
                        .zIndex(5);//设置图标样式
                Bundle bundle = new Bundle();
                bundle.putSerializable("estate", mEstateBeanList.get(i));
                //bundle.putString("name", mEstateBeanList.get(i).getName());
                mMarker = (Marker) mBaiduMap.addOverlay(options);
                mMarker.setToTop();
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

    @OnClick(R.id.iv_map_location)
    public void onLocationClicked() {
        mMyPt = mCurrentPt;
        mIsSearch = false;
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(mCurrentPt)
                .zoom(MAP_ZOOM_LEVEL)
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        mTvCurrentCity.setText(mCurrentCity);
        updateMapState(mMyPt);
    }


    @OnClick(R.id.iv_map_refresh)
    public void onRefreshClicked() {
        if (mMyPt == null) {
            mMyPt = mCurrentPt;
        }
        updateMapState(mMyPt);
    }

    @OnClick(R.id.iv_map_marker)
    public void onChangeMarkerClicked() {
        mMapStateParkingNum = !mMapStateParkingNum;
        if (mMapStateParkingNum) {
            mIvMapMarker.setImageResource(R.drawable.ic_number);
        } else {
            mIvMapMarker.setImageResource(R.drawable.ic_price);
        }
        addMarkers();
    }

    @OnClick(R.id.rl_input_location)
    public void onViewClicked() {
        Intent intent = new Intent(getActivity(), MapSearchActivity.class);
        intent.putExtra("city", mCity);
        getActivity().startActivityForResult(intent, REQUEST_CODE_SEARCH);
    }

    @OnClick({R.id.iv_select_city, R.id.tv_current_city})
    public void onSelectCity() {
        Intent intent = new Intent(getActivity(), CityActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("city", mCurrentCity);
        intent.putExtras(bundle);
        getActivity().startActivityForResult(intent, REQUEST_CODE_CITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_CODE_SEARCH) {
                //CommonUtil.hideKeyboard(getActivity());
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
            if (requestCode == REQUEST_CODE_CITY) {
                mCity = data.getExtras().getString("city");
                mTvCurrentCity.setText(mCity);
                String URLString;
                String output = "json";
                String key = "R6nE16pZMKymjr58SMBAPsU3wC8BD9RY";
//                try {
//                    cityName = URLEncoder.encode(data.getExtras().getString("city"), "UTF-8");
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
                if (!TextUtils.isEmpty(mCity)) {
                    BaiduMapService baiduMapService = BaiduMapServiceGenerator.createService(BaiduMapService.class);
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("address", mCity);
                    option.put("output", output);
                    option.put("key", key);
                    Call<BaiduMapResponse> call = baiduMapService.queryLatLnt(option);
                    call.enqueue(new Callback<BaiduMapResponse>() {
                        @Override
                        public void onResponse(Call<BaiduMapResponse> call, Response<BaiduMapResponse> response) {
                            if (response.body().getStatus().equals("OK")) {
                                mMyPt = new LatLng(response.body().getResult().getLocation().getLat(), response.body().getResult().getLocation().getLng());
                                MapStatus mMapStatus = new MapStatus.Builder()
                                        .target(mMyPt)
                                        .zoom(MAP_ZOOM_LEVEL)
                                        .build();
                                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                                mBaiduMap.setMapStatus(mMapStatusUpdate);
                                //刷新附近停车场
                                mIsSearch = true;
                                updateMapState(mMyPt);
                            }
                        }

                        @Override
                        public void onFailure(Call<BaiduMapResponse> call, Throwable t) {

                        }
                    });
                }

            }
        }
    }

    @OnClick(R.id.iv_open_drawer)
    public void onOpenDrawer() {
        ((MainActivity) getActivity()).openDrawer();
    }

    /**
     * remain for future use
     */
    private void showPopFormBottom() {
        SharePopupWindow sharePopupWindow = new SharePopupWindow(mContext, getActivity(), 200);
        //showAtLocation(View parent, int gravity, int x, int y)
        sharePopupWindow.showAtLocation(mView, Gravity.BOTTOM, 0, 0);
    }


    @OnClick(R.id.iv_map_navi)
    public void onNaviClicked() {
        if (BaiduNaviManager.isNaviInited()) {
            mNavi.setsNodeLocation(mCurrentPt);
            mNavi.setsNodeName("我的位置");
            mNavi.seteNodeLocation(new LatLng((double) SharedPreferenceUtil.getFloat(mContext, Constant.ESTATE_LATITUDE, 0), (double) SharedPreferenceUtil.getFloat(mContext, Constant.ESTATE_LONGITUDE, 0)));
            mNavi.seteNodeName(SharedPreferenceUtil.getString(mContext, Constant.ESTATE_NAME, ""));
            mNavi.routeplanToNavi(CoordinateType.BD09LL);
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
//                    Toast.makeText(mContext, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            mNavi.initNavi();
        } else if (requestCode == authComRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                }
            }
            mNavi.routeplanToNavi(mCoordinateType);
        }

    }


}
