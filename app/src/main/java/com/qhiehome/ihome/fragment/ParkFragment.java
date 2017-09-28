package com.qhiehome.ihome.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
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
import com.baidu.mapapi.map.MapPoi;
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
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.CityActivity;
import com.qhiehome.ihome.activity.MainActivity;
import com.qhiehome.ihome.activity.MapSearchActivity;
import com.qhiehome.ihome.activity.ParkingListActivity;
import com.qhiehome.ihome.activity.ReserveActivity_old;
import com.qhiehome.ihome.adapter.EstateInfoAdapter;
import com.qhiehome.ihome.map.DrivingRouteOverlay;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.baiduMap.BaiduMapResponse;
import com.qhiehome.ihome.network.model.configuration.city.CityConfigRequest;
import com.qhiehome.ihome.network.model.configuration.city.CityConfigResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyResponse;
import com.qhiehome.ihome.network.service.baiduMap.BaiduMapService;
import com.qhiehome.ihome.network.service.baiduMap.BaiduMapServiceGenerator;
import com.qhiehome.ihome.network.service.configuration.CityConfigService;
import com.qhiehome.ihome.network.service.inquiry.ParkingEmptyService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.NaviUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.MapInfoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.NOTIFICATION_SERVICE;


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
//    @BindView(R.id.iv_map_marker)
//    ImageView mIvMapMarker;
    @BindView((R.id.tv_current_city))
    TextView mTvCurrentCity;
    @BindView(R.id.iv_map_navi)
    ImageView mIvMapNavi;
    @BindView(R.id.iv_map_price)
    ImageView mPrice;
    @BindView(R.id.iv_map_number)
    ImageView mNumber;
    @BindView(R.id.map_info_view)
    MapInfoView mMapInfoView;

    private Context mContext;

    private boolean isGetCurrentCity;

    private MapView mMapView;
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
    private boolean mHasRemindLeave = false;

    AnimatorSet mRightOutSet;
    AnimatorSet mLeftInSet;

    /******百度地图导航******/

    private final static int authBaseRequestCode = 1;
    private final static int authComRequestCode = 2;
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    private CoordinateType mCoordinateType;

    private static final float MAP_ZOOM_LEVEL = 17f;
    private static final int MAP_ZOOM_IN_DURATION = 600;
    private static final String LOCATE_RESULT_TYPE = "bd09ll";
    private static final int LOCATE_INTERVAL = 5000;
    private static final int RADIUS = 5000;

    private static final double REFRESH_DISTANCE = 1000;
    private static final int REMIND_DISTANCE = 1000;


    private List<ParkingEmptyResponse.DataBean.EstateBean> mEstateBeanList = new ArrayList<>();
    private List<ParkingEmptyResponse.DataBean.EstateBean.ParkingListBean> mParkingBeanList = new ArrayList<>();


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
            if (mMyPt == null) {
                mMyPt = mCurrentPt;
            }
            updateMapState(mMyPt);
        }
        if (SharedPreferenceUtil.getInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_CANCEL) == Constant.ORDER_STATE_RESERVED ||
                SharedPreferenceUtil.getInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_CANCEL) == Constant.ORDER_STATE_PARKED) {
            mIvMapNavi.setVisibility(View.VISIBLE);
        } else {
            mIvMapNavi.setVisibility(View.GONE);
        }

    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
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
        mMapView.showZoomControls(false);
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMapInfoView.getVisibility() == View.VISIBLE){
                    mMapInfoView.setVisibility(View.GONE);
                    updateMapState(mCurrentPt);
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
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
                if (!mHasRemindLeave && SharedPreferenceUtil.getInt(mContext, Constant.ORDER_STATE, 0) == Constant.ORDER_STATE_PARKED) {  //提醒用户确认离开
                    double distance = DistanceUtil.getDistance(xy, new LatLng((double) SharedPreferenceUtil.getFloat(mContext, Constant.ESTATE_LATITUDE, 0), (double) SharedPreferenceUtil.getFloat(mContext, Constant.ESTATE_LONGITUDE, 0)));
                    if (distance >= REMIND_DISTANCE) {
                        mHasRemindLeave = true;
                        sendNotification();
                    }
                } else {
                    double distance = DistanceUtil.getDistance(xy, new LatLng((double) SharedPreferenceUtil.getFloat(mContext, Constant.ESTATE_LATITUDE, 0), (double) SharedPreferenceUtil.getFloat(mContext, Constant.ESTATE_LONGITUDE, 0)));
                    if (distance < REMIND_DISTANCE) {
                        mHasRemindLeave = false;
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
                if (isFastClick(2000)) {
                    return false;
                }
                // TODO: 2017/9/25 更换为显示小区详细信息
                mClickedMarker = marker1;
//                getCityConfig(marker1);
                routeSearch(marker1.getPosition());
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
            BitmapDescriptor flag = BitmapDescriptorFactory.fromBitmap(bm);
            MarkerOptions searchOptions = new MarkerOptions()
                    .position(mMyPt)//设置位置
                    .icon(flag)
                    .zIndex(9);//设置图标样式
            mMarker = (Marker) mBaiduMap.addOverlay(searchOptions);
        }
        for (int i = 0; i < mEstateBeanList.size(); i++) {

            if (mEstateBeanList.get(i).getShareCount() > 0) {
                LatLng newPT = new LatLng(mEstateBeanList.get(i).getY(), mEstateBeanList.get(i).getX());
                OverlayOptions options;

                View customMarker = View.inflate(mContext, R.layout.custom_map_marker, null);
                TextView tv_marker = (TextView) customMarker.findViewById(R.id.tv_marker);
                ImageView iv_marker = (ImageView) customMarker.findViewById(R.id.iv_marker);
                if (mMapStateParkingNum) {
                    iv_marker.setImageResource(R.drawable.img_bluemark);
                    int shareNum = mEstateBeanList.get(i).getShareCount();
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
        refreshAnim();
    }


    @OnClick(R.id.iv_map_refresh)
    public void onRefreshClicked() {
        if (mMyPt == null) {
            mMyPt = mCurrentPt;
        }
        if (mMapInfoView.getVisibility() == View.VISIBLE){
            mMapInfoView.setVisibility(View.GONE);
        }
        updateMapState(mMyPt);
        refreshAnim();
    }

    private void refreshAnim() {
        rotate();
        zoomInAndOut();
    }

    private void zoomInAndOut() {
        MapStatusUpdate zoomIn = MapStatusUpdateFactory.zoomOut();
        mBaiduMap.animateMapStatus(zoomIn, MAP_ZOOM_IN_DURATION);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MapStatusUpdate zoomOut = MapStatusUpdateFactory.zoomTo(MAP_ZOOM_LEVEL);
                mBaiduMap.animateMapStatus(zoomOut);
            }
        }, MAP_ZOOM_IN_DURATION);

    }

    private void rotate() {
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(mIvMapRefresh, "rotation", 0f, 720f);
        rotateAnim.setInterpolator(new AccelerateInterpolator());
        rotateAnim.setDuration(1000);
        rotateAnim.start();
    }

    @OnClick(R.id.iv_map_marker)
    public void onChangeMarkerClicked() {
        mMapStateParkingNum = ! mMapStateParkingNum;
        setAnimators();
        setCameraDistance();
        if(!mMapStateParkingNum) {
            mRightOutSet.setTarget(mNumber);
            mLeftInSet.setTarget(mPrice);
        }else {
            mRightOutSet.setTarget(mPrice);
            mLeftInSet.setTarget(mNumber);
        }
        mRightOutSet.start();
        mLeftInSet.start();
        addMarkers();
    }

    private void setAnimators() {
        mRightOutSet = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.card_flip_anim_out);
        mLeftInSet = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.card_flip_anim_in);
    }

    private void setCameraDistance() {
        int distance = 16000;
        float scale = getResources().getDisplayMetrics().density * distance;
        mNumber.setCameraDistance(scale);
        mPrice.setCameraDistance(scale);
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

    /**
     * 从搜索、城市选择Activity返回时调用
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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
                String output = "json";
                String key = "iLf6t6ZTMfSxZ2T9WFbAalUOfi01GPA8";
                if (!TextUtils.isEmpty(mCity)) {
                    BaiduMapService baiduMapService = BaiduMapServiceGenerator.createService(BaiduMapService.class);
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("address", mCity);
                    option.put("output", output);
                    option.put("ak", key);
                    Call<BaiduMapResponse> call = baiduMapService.queryLatLnt(option);
                    call.enqueue(new Callback<BaiduMapResponse>() {
                        @Override
                        public void onResponse(Call<BaiduMapResponse> call, Response<BaiduMapResponse> response) {
                            if (response.body().getStatus() == 0) {
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

    /**
     * 获取城市配置参数
     *
     * @param marker1 地图上点击的marker
     */
    private void getCityConfig(final Marker marker1) {
        ParkingEmptyResponse.DataBean.EstateBean estateBean = (ParkingEmptyResponse.DataBean.EstateBean) marker1.getExtraInfo().getSerializable("estate");
        CityConfigService cityConfigService = ServiceGenerator.createService(CityConfigService.class);
        CityConfigRequest cityConfigRequest = new CityConfigRequest(estateBean.getId());
        Call<CityConfigResponse> call = cityConfigService.queryCityConfig(cityConfigRequest);
        call.enqueue(new Callback<CityConfigResponse>() {
            @Override
            public void onResponse(Call<CityConfigResponse> call, Response<CityConfigResponse> response) {
                try {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                        Intent intent = new Intent(getActivity(), ParkingListActivity.class);
                        Bundle bundle = marker1.getExtraInfo();
                        bundle.putInt(Constant.MIN_SHARING_PERIOD, response.body().getData().getMinSharingPeriod());
                        bundle.putInt(Constant.MIN_CHARGING_PERIOD, response.body().getData().getMinChargingPeriod());
                        bundle.putInt(Constant.FREE_CANCELLATION_TIME, response.body().getData().getFreeCancellationTime());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        ToastUtil.showToast(mContext, "服务器繁忙，请稍后再试");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToast(mContext, "服务器错误，请稍后再试");
                }

            }

            @Override
            public void onFailure(Call<CityConfigResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        // 设置通知的基本信息：icon、标题、内容
        builder.setSmallIcon(R.mipmap.ic_launcher_logo);
        builder.setContentTitle("爱车位");
        builder.setContentText("您已离开车位1km，是否忘记确认离开");


        // 设置通知的点击行为：这里启动一个 Activity
        Intent intent = new Intent(getActivity(), ReserveActivity_old.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_ALL);

        Notification notification = builder.build();

        int notificationId = 1;

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(notificationId, notification);

    }


    private static long lastClickTime;

    public static boolean isFastClick(long ClickIntervalTime) {
        long ClickingTime = System.currentTimeMillis();
        if (ClickingTime - lastClickTime < ClickIntervalTime) {
            return true;
        }
        lastClickTime = ClickingTime;
        return false;
    }

    private void initPopupWindow(View contentView) {
        TextView tvLocation = (TextView) contentView.findViewById(R.id.tv_estate_location);
        RecyclerView rvEstateDetail = (RecyclerView) contentView.findViewById(R.id.rv_estate_detail);
        Button btnReserve = (Button) contentView.findViewById(R.id.btn_to_reserve);

        EstateInfoAdapter adapter = new EstateInfoAdapter();
        rvEstateDetail.setLayoutManager(new GridLayoutManager(mContext, 3));
        rvEstateDetail.setAdapter(adapter);
        tvLocation.setText("北京邮电大学新科研楼");
    }

    private void routeSearch(LatLng targetPt) {
        RoutePlanSearch routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

                int distance = drivingRouteResult.getRouteLines().get(0).getDistance(); //距离-->单位米
                int duration = drivingRouteResult.getRouteLines().get(0).getDuration(); //时间-->单位秒
                int congestionDistance = drivingRouteResult.getRouteLines().get(0).getCongestionDistance(); //拥堵路段-->单位米
                LogUtil.e("Overlay", "distance:" + distance);
                LogUtil.e("Overlay", "duration:" + duration);
                LogUtil.e("Overlay", "congestionDistance:" + congestionDistance);

                // TODO: 2017/9/26 增加自定义view
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
                LogUtil.e("Overlay", "add overlay");
                overlay.setData(drivingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                mMapInfoView.setOverlay(overlay);

                mMapInfoView.setVisibility(View.VISIBLE);
                View view = mMapInfoView.getmView();
                TextView tvLocation = (TextView) view.findViewById(R.id.tv_estate_location);
                Bundle bundle = mClickedMarker.getExtraInfo();
                ParkingEmptyResponse.DataBean.EstateBean estateBean = (ParkingEmptyResponse.DataBean.EstateBean) bundle.getSerializable("estate");

                try {
                    tvLocation.setText(estateBean.getName());

                    RecyclerView rvEstateDetail = (RecyclerView) view.findViewById(R.id.rv_estate_detail);
                    Button btnReserve = (Button) view.findViewById(R.id.btn_to_reserve);

                    btnReserve.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getCityConfig(mClickedMarker);
                        }
                    });

                    ArrayList<String> data = new ArrayList<String>();
                    data.add(String.format(Locale.CHINA, "%.2f", (float)estateBean.getUnitPrice()) + "元");
                    data.add(distance + "米" + "(" + congestionDistance + " 米)");
                    data.add(duration + "米");

                    String[] title = {"停车费/小时", "距离(拥堵路段)", "预计到达时间"};
                    EstateInfoAdapter adapter = new EstateInfoAdapter(mContext, data, title);
                    rvEstateDetail.setLayoutManager(new GridLayoutManager(mContext, 3));
                    rvEstateDetail.setAdapter(adapter);
                }catch (Exception e){
                    e.printStackTrace();
                }





//                BasePopupWindow popupWindow;
//                View view = LayoutInflater.from(mContext).inflate(R.layout.layout_estate_info,null);//PopupWindow对象
//                popupWindow= new BasePopupWindow(mContext, getActivity(), 1000);//初始化PopupWindow对象
//                popupWindow.setContentView(view);//设置PopupWindow布局文件
//                View rootView =LayoutInflater.from(mContext).inflate(R.layout.fragment_park, null);//父布局
//                popupWindow.showAtLocation(rootView, Gravity.BOTTOM,0,0);
//
//                initPopupWindow(view);
            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        });
        PlanNode stNode = PlanNode.withLocation(mCurrentPt);
        PlanNode enNode = PlanNode.withLocation(targetPt);
        routePlanSearch.drivingSearch((new DrivingRoutePlanOption())
                .from(stNode)//起点
                .to(enNode));//终点


    }


}
