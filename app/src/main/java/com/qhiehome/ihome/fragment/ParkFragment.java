package com.qhiehome.ihome.fragment;


import android.content.Intent;
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
import com.baidu.mapapi.SDKInitializer;
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


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ParkFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParkFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LatLng mCurrentPt;
    private Marker mMarker;
    private BaiduMap.OnMarkerClickListener mOnMarkerClickListener;
    private boolean mFirstLocation;
    private LocationClient mLocationClient;
    private BDLocationListener mBDLocationListener;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ParkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ParkFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ParkFragment newInstance(String param1, String param2) {
        ParkFragment fragment = new ParkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_park, container, false);

        mMapView = (MapView) view.findViewById(R.id.mv_park);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15f);
        mBaiduMap.setMapStatus(msu);

        mBaiduMap.setMyLocationEnabled(true);
        initListener();
        locate();
        mLocationClient.start();
        /**
         * 在请求定位之前应该确定mCLient已经启动
         */
        if (mLocationClient != null && mLocationClient.isStarted())
            mLocationClient.requestLocation();
            //onReceiveLocation();将得到定位数据
        else{

        }


        return view;
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

    private void locate(){
        // 定位初始化
        mLocationClient = new LocationClient(getActivity().getApplicationContext());
        mFirstLocation = true;
        // 设置定位的相关配置
        LocationClientOption locOption = new LocationClientOption();
        locOption.setOpenGps(true);
        locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        locOption.setCoorType("bd09ll");// 设置定位结果类型
        locOption.setScanSpan(5000);// 设置发起定位请求的间隔时间,ms
        locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向
        mLocationClient.setLocOption(locOption);

//        // 设置自定义图标
//        BitmapDescriptor myMarker = BitmapDescriptorFactory
//                .fromResource(R.drawable.navi_map);
//        MyLocationConfigeration config = new MyLocationConfigeration(
//                MyLocationConfigeration.LocationMode.FOLLOWING, true, myMarker);

        mLocationClient.registerLocationListener(mBDLocationListener = new BDLocationListener() {
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
                if (mFirstLocation)
                {
                    mFirstLocation = false;
                    LatLng xy = new LatLng(bdLocation.getLatitude(),
                            bdLocation.getLongitude());
                    MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(xy);
                    mBaiduMap.animateMapStatus(status);
                }
            }
            @Override
            public void onConnectHotSpotMessage(String s, int i) {

            }
        });
    }

    @Override
    public void onStart(){
        // 如果要显示位置图标,必须先开启图层定位
        super.onStart();
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted())
        {
            mLocationClient.start();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {        //核心方法，避免因Fragment跳转导致地图崩溃
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser == true) {
            // if this view is visible to user, start to request user location
            startRequestLocation();
        } else if (isVisibleToUser == false) {
            // if this view is not visible to user, stop to request user
            // location
            stopRequestLocation();
        }
    }

    private void stopRequestLocation() {
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(mBDLocationListener);
            mLocationClient.stop();
        }
    }

    long startTime;
    long costTime;

    private void startRequestLocation() {
        // this nullpoint check is necessary
        if (mLocationClient != null) {
            mLocationClient.registerLocationListener(mBDLocationListener);
            mLocationClient.start();
            mLocationClient.requestLocation();
            startTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (mLocationClient != null)
            mLocationClient.stop();
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);

    }



    private void updateMapState(){
        //BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.flag);

/********************缩放图片********************/
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
            public void onMarkerDragStart(Marker marker) {

            }
        });

/********************搜索POI********************/
        PoiSearch ps = PoiSearch.newInstance();
        //ps.setOnGetPoiSearchResultListener(getPoiSearchListener);
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


/********************加底部列表********************/
                //BottomView bottomView = new BottomView(this,R.style.BottomViewTheme_Defalut,R.layout.bottom_view);

                while (count<10 && count < resultNum){   //选择附近最多10个停车场
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
