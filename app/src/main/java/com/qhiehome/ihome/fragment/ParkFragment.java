package com.qhiehome.ihome.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.util.LogUtil;

/**
 * This fragment show the nearest districts around the user
 * according to the current location info.
 */
public class ParkFragment extends Fragment {

    private static final String TAG = "ParkFragment";

    private Context mContext;

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LatLng mCurrentPt;
    private Marker mMarker;
    private BaiduMap.OnMarkerClickListener mOnMarkerClickListener;
    private boolean mFirstLocation;
    private LocationClient mLocationClient;
    private BDLocationListener mBDLocationListener;

    private static final float MAP_ZOOM_LEVEL = 15f;
    private static final String LOCATE_RESULT_TYPE = "bd09ll";
    private static final int LOCATE_INTERVAL = 5000;

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
        return view;
    }

    @Override
    public void onStart(){
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
     * 设置百度地图的缩放级别和点击事件
     */
    private void initMap() {
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(MAP_ZOOM_LEVEL);
        mBaiduMap.setMapStatus(msu);
        mBaiduMap.setMyLocationEnabled(true);
        initListener();
    }

    private void initListener(){
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

    /**
     * 定位相关配置
     */
    private void initLocate(){
        // 定位初始化
        mLocationClient = new LocationClient(mContext.getApplicationContext());
        mFirstLocation = true;
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
                if (bdLocation == null || mMapView == null){
                    return;
                }
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude()).build();
                // 设置定位数据
                mBaiduMap.setMyLocationData(locData);
                if (mFirstLocation) {
                    LatLng xy = new LatLng(bdLocation.getLatitude(),
                            bdLocation.getLongitude());
                    mCurrentPt = xy;
                    MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(xy);
                    mBaiduMap.animateMapStatus(status);
                    updateMapState();
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private void updateMapState(){
        //BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.flag);
        if (!mFirstLocation){
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

/********************设置覆盖物********************/
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
                public void onMarkerDragStart(Marker marker) {}
            });

        }else{
            mFirstLocation = false;
        }
/********************搜索POI********************/
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


                while (count<10 && count < resultNum){   //选择附近最多10个停车场
                //while (count < resultNum){      //选择附近所有停车场
                    final PoiInfo pif =  poiResult.getAllPoi().get(count);
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

                /********************跳转、传递参数********************/
//               mOnMarkerClickListener = new BaiduMap.OnMarkerClickListener() {
//                    @Override
//                    public boolean onMarkerClick(Marker marker1) {
//                        intent = new Intent(MainActivity.this,ListViewActivity.class);
//                        //bundle1 = marker1.getExtraInfo();
//                        String currentStr = marker1.getExtraInfo().getString("name");
//                        intent.putExtra("name",currentStr);
//                        startActivity(intent);
//                        return false;
//                    }
//                };
//                mBaiduMap.setOnMarkerClickListener(mOnMarkerClickListener);

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




}
