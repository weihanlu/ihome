package com.qhiehome.ihome.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.UserLockAdapter;
import com.qhiehome.ihome.application.IhomeApplication;
import com.qhiehome.ihome.bean.UserLockBean;
import com.qhiehome.ihome.bean.UserLockBeanDao;
import com.qhiehome.ihome.lock.ConnectLockService;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedResponse;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdRequest;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdResponse;
import com.qhiehome.ihome.network.service.avatar.UploadAvatarService;
import com.qhiehome.ihome.network.service.inquiry.ParkingOwnedService;
import com.qhiehome.ihome.network.service.lock.UpdateLockPwdService;
import com.qhiehome.ihome.persistence.DaoSession;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.FileUtils;
import com.qhiehome.ihome.util.NetworkUtils;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import org.greenrobot.greendao.query.Query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoActivity extends BaseActivity {

    private static final String TAG = UserInfoActivity.class.getSimpleName();

    private static final int CODE_CAMERA_REQUEST_SRC = 1;
    private static final int REQUEST_FOR_OPEN_CAMERA_AND_WRITE_EXTERNAL = 2;
    private static final int CODE_PICTURES_REQUEST_SRC = 3;
    private static final int REQUEST_FOR_COPY_LOCAL_FILE = 4;

    @BindView(R.id.tb_userinfo)
    Toolbar mTbUserInfo;

    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.tv_phoneNum)
    TextView mTvPhoneNum;

    @BindView(R.id.tv_balance)
    TextView mTvBalance;

    @BindView(R.id.tv_add_balance)
    TextView mTvAddBalance;

    @BindView(R.id.iv_avatar)
    CircleImageView mIvAvatar;

    @BindView(R.id.tv_toolbar_title)
    TextView mTvToolbarTitle;

    @BindView(R.id.vs_user_locks)
    ViewStub mViewStub;

    private Context mContext;

    private ArrayList<UserLockBean> mUserLocks;

    private long mCurrentTime;

    private StringBuilder mParkingIds;

    private ConnectLockReceiver mReceiver;

    EditText mEtOldPwd;

    EditText mEtNewPwd;

    MaterialDialog mProgressDialog;

    MaterialDialog mControlLockDialog;

    private String mAvatarPath;

    private File mAvatarFile;

    private UserLockBeanDao mUserLockBeanDao;

    private Query<UserLockBean> mUserLockBeansQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
        mContext = this;
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new ConnectLockReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectLockService.BROADCAST_CONNECT);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void initData() {
        DaoSession daoSession = ((IhomeApplication)getApplication()).getDaoSession();
        mUserLockBeanDao = daoSession.getUserLockBeanDao();
        mUserLockBeansQuery = mUserLockBeanDao.queryBuilder().orderAsc(UserLockBeanDao.Properties.Id).build();
        String phoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, "");
        String avatarName = "portrait_" + phoneNum;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        mAvatarFile = new File(storageDir, avatarName + ".jpg");
        mAvatarPath = mAvatarFile.getAbsolutePath();

        mUserLocks = new ArrayList<>();
        mParkingIds = new StringBuilder();

        mTvPhoneNum.setText("账号：" + phoneNum);
        inquiryOwnedParkings(phoneNum);
    }

    private void inquiryOwnedParkings(String phoneNum) {
        mCurrentTime = System.currentTimeMillis();
        if (NetworkUtils.isConnected(this)) {
            ParkingOwnedService parkingOwnedService = ServiceGenerator.createService(ParkingOwnedService.class);
            ParkingOwnedRequest parkingOwnedRequest = new ParkingOwnedRequest(phoneNum);
            Call<ParkingOwnedResponse> call = parkingOwnedService.parkingOwned(parkingOwnedRequest);
            call.enqueue(new Callback<ParkingOwnedResponse>() {
                @Override
                public void onResponse(@NonNull Call<ParkingOwnedResponse> call, @NonNull Response<ParkingOwnedResponse> response) {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                        // success and then inflate ViewStub
                        List<ParkingResponse.DataBean.EstateBean> estateList = response.body().getData().getEstate();
                        if (estateList.size() != 0) {
                            mViewStub.inflate();
                            RecyclerView rvUserLocks = (RecyclerView) findViewById(R.id.rv_user_locks);
                            rvUserLocks.setHasFixedSize(true);
                            LinearLayoutManager llm = new LinearLayoutManager(mContext);
                            rvUserLocks.setLayoutManager(llm);
                            initLocks(estateList);
                            UserLockAdapter userLockAdapter = new UserLockAdapter(mContext, mUserLocks);
                            rvUserLocks.setAdapter(userLockAdapter);
                            initListener(userLockAdapter);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ParkingOwnedResponse> call, @NonNull Throwable t) {

                }
            });
        } else {
            List<UserLockBean> list = mUserLockBeansQuery.list();
            if (list != null && list.size() > 0) {
                mViewStub.inflate();
                RecyclerView rvUserLocks = (RecyclerView) findViewById(R.id.rv_user_locks);
                rvUserLocks.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(mContext);
                rvUserLocks.setLayoutManager(llm);
                for (UserLockBean userLockBean: list) {
                    mUserLocks.add(userLockBean);
                }
                UserLockAdapter userLockAdapter = new UserLockAdapter(mContext, mUserLocks);
                rvUserLocks.setAdapter(userLockAdapter);
                initListener(userLockAdapter);
            }
        }
    }

    private void initListener(final UserLockAdapter userLockAdapter) {
        userLockAdapter.setOnItemClickListener(new UserLockAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int i) {
                UserLockBean userLockBean = mUserLocks.get(i);
                final String gatewayId = userLockBean.getGatewayId();
                final String lockMac = userLockBean.getLockMac();
                if (mProgressDialog == null) {
                    mProgressDialog = new MaterialDialog.Builder(mContext)
                            .title("连接中")
                            .content("请等待...")
                            .progress(true, 0)
                            .showListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Intent connectLock = new Intent(mContext, ConnectLockService.class);
                                    if (NetworkUtils.isConnected(mContext)) {
                                        connectLock.setAction(ConnectLockService.ACTION_GATEWAY_CONNECT);
                                        connectLock.putExtra(ConnectLockService.EXTRA_GATEWAY_ID, gatewayId);
                                    } else {
                                        connectLock.setAction(ConnectLockService.ACTION_BLUETOOTH_CONNECT);
                                        connectLock.putExtra(ConnectLockService.EXTRA_LOCK_PWD, Constant.DEFAULT_PASSWORD);
                                    }
                                    connectLock.putExtra(ConnectLockService.EXTRA_LOCK_MAC, lockMac);
                                    startService(connectLock);
                                }
                            }).build();
                }
                mProgressDialog.show();


                View controlLock = LayoutInflater.from(mContext).inflate(R.layout.dialog_control_lock, null);
                ImageView imgUpLock = (ImageView) controlLock.findViewById(R.id.img_up_lock);
                ImageView imgDownLock = (ImageView) controlLock.findViewById(R.id.img_down_Lock);
                imgUpLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent upLock = new Intent(mContext, ConnectLockService.class);
                        upLock.setAction(ConnectLockService.ACTION_UP_LOCK);
                        startService(upLock);
                    }
                });
                imgDownLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent downLock = new Intent(mContext, ConnectLockService.class);
                        downLock.setAction(ConnectLockService.ACTION_DOWN_LOCK);
                        startService(downLock);
                    }
                });
                if (mControlLockDialog == null) {
                    mControlLockDialog = new MaterialDialog.Builder(mContext)
                            .title("已连接").titleGravity(GravityEnum.CENTER)
                            .customView(controlLock, false)
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    Intent disconnect = new Intent(mContext, ConnectLockService.class);
                                    disconnect.setAction(ConnectLockService.ACTION_DISCONNECT);
                                    startService(disconnect);
                                }
                            })
                            .build();
                }
            }

            @Override
            public void onButtonClick(View view, int i) {
                final UserLockBean userLockBean = mUserLocks.get(i);
                MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                        .customView(R.layout.dialog_modify_pwd, false)
                        .positiveText("确定")
                        .negativeText("取消")
                        .build();
                View customView = dialog.getCustomView();
                if (customView != null) {
                    mEtOldPwd = (EditText) customView.findViewById(R.id.et_old_pwd);
                    mEtNewPwd = (EditText) customView.findViewById(R.id.et_new_pwd);
                }
                dialog.getBuilder()
                        .showListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                CommonUtil.showSoftKeyboard(mEtOldPwd, mContext);
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                int parkingId = userLockBean.getParkingId();
                                String oldPwd = mEtOldPwd.getText().toString();
                                String newPwd = mEtNewPwd.getText().toString();
                                if (!(TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd))) {
                                    modifyLockPwd(parkingId, mEtOldPwd.getText().toString(), mEtNewPwd.getText().toString());
                                }
                            }
                        })
                        .canceledOnTouchOutside(false)
                        .show();
            }
        });
    }

    private void modifyLockPwd(int parkingId, String oldPwd, String newPwd) {
        UpdateLockPwdService updateLockPwdService = ServiceGenerator.createService(UpdateLockPwdService.class);
        UpdateLockPwdRequest updateLockPwdRequest = new UpdateLockPwdRequest(parkingId, oldPwd, newPwd);
        Call<UpdateLockPwdResponse> call = updateLockPwdService.updateLockPwd(updateLockPwdRequest);
        call.enqueue(new Callback<UpdateLockPwdResponse>() {
            @Override
            public void onResponse(@NonNull Call<UpdateLockPwdResponse> call, @NonNull Response<UpdateLockPwdResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    ToastUtil.showToast(mContext, "modify password successfully");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdateLockPwdResponse> call, @NonNull Throwable t) {

            }
        });
    }


    private void initLocks(List<ParkingResponse.DataBean.EstateBean> estateList) {
        mUserLocks.clear();
        UserLockBean userLockBean;
        boolean isRented = false;
        for (ParkingResponse.DataBean.EstateBean estate : estateList) {
            for (ParkingResponse.DataBean.EstateBean.ParkingBean parkingBean : estate.getParking()) {
                List<ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean> share = parkingBean.getShare();
                for (int i = 0; i < share.size(); i++) {
                    ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean shareBean = share.get(i);
                    long startTime = shareBean.getStartTime();
                    long endTime = shareBean.getEndTime();
                    if (mCurrentTime >= startTime && mCurrentTime <= endTime) {
                        isRented = true;
                    }
                }
                userLockBean = new UserLockBean(null, estate.getName(), parkingBean.getName(), parkingBean.getId(), parkingBean.getGatewayId(),
                        parkingBean.getLockMac(), isRented);
                mUserLocks.add(userLockBean);
                mUserLockBeanDao.insertOrReplace(userLockBean);
                mParkingIds.append(parkingBean.getId()).append(",");
            }
        }
        SharedPreferenceUtil.setString(this, Constant.OWNED_PARKING_KEY, mParkingIds.deleteCharAt(mParkingIds.length() - 1).toString());
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        initToolbar();
        initAppBarLayout();
        initAvatar();
    }

    private void initAvatar() {
        File avatarDir = mAvatarFile.getParentFile();
        if (avatarDir.isDirectory() && avatarDir.listFiles().length != 0) {
            Bitmap avatarBitmap = BitmapFactory.decodeFile(mAvatarPath);
            mIvAvatar.setImageBitmap(avatarBitmap);
        }
    }

    private void initToolbar() {
        mTbUserInfo.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initAppBarLayout() {
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float total = appBarLayout.getTotalScrollRange() * 1.0f;
                float p = Math.abs(verticalOffset) / total;
                if (p > 0.5) {
                    mTvToolbarTitle.setAlpha(1.0f / 0.5f * (p - 0.5f));
                    mTvPhoneNum.setAlpha(0);
                    mTvBalance.setAlpha(0);
                    mTvAddBalance.setAlpha(0);
                } else {
                    mTvToolbarTitle.setAlpha(0);
                    mTvPhoneNum.setAlpha(1.0f - p / 0.5f);
                    mTvBalance.setAlpha(1.0f - p / 0.5f);
                    mTvAddBalance.setAlpha(1.0f - p / 0.5f);
                }
                mIvAvatar.setVisibility(p == 1 ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    @OnClick(R.id.iv_avatar)
    public void onAvatarClick() {
        new MaterialDialog.Builder(this)
                .title("请选择")
                .items(R.array.avatar_items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        switch (position) {
                            case 0:
                                if (CommonUtil.checkCameraHardware(mContext)) {
                                    openCamera();
                                } else {
                                    ToastUtil.showToast(mContext, "没有检测到相机");
                                }
                                break;
                            case 1:
                                openLocalFolder();
                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            getPhotoByCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_FOR_OPEN_CAMERA_AND_WRITE_EXTERNAL);
        }
    }

    private void getPhotoByCamera() {
        Intent mStartCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (mStartCamera.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the file...
            }
            // Continue only if the file was successfully created
            if (photoFile != null) {
                Uri photoURI;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    mStartCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    photoURI = FileProvider.getUriForFile(this, "com.qhiehome.ihome.provider", photoFile);
                } else {
                    photoURI = Uri.fromFile(photoFile);
                }
                mStartCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(mStartCamera, CODE_CAMERA_REQUEST_SRC);
            }
        }
    }

    private File createImageFile() throws IOException {
        if (mAvatarFile.exists()) {
            mAvatarFile.delete();
        } else {
            mAvatarFile.createNewFile();
        }
        // Save a file: path for use with ACTION_VIEW intents
        return mAvatarFile;
    }

    private void openLocalFolder() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            getPhotoFromFolder();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_FOR_COPY_LOCAL_FILE);
        }
    }

    private void getPhotoFromFolder() {
        Intent getLocalPictures = new Intent();
        getLocalPictures.setType("image/*");
        getLocalPictures.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(getLocalPictures, CODE_PICTURES_REQUEST_SRC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CODE_CAMERA_REQUEST_SRC) {
                galleryAddPic();
                showOriginalImage();
            } else if (requestCode == CODE_PICTURES_REQUEST_SRC) {
                showLocalImage(data);
            }
        }
    }

    private void uploadAvatar() {
        File avatarDir = mAvatarFile.getParentFile();
        if (avatarDir.isDirectory() && avatarDir.listFiles().length != 0) {
            UploadAvatarService uploadAvatarService = ServiceGenerator.createService(UploadAvatarService.class);
            String phoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, "");
            RequestBody requestPhone = RequestBody.create(MediaType.parse("multipart/form-data"), EncryptUtil.encrypt(phoneNum, EncryptUtil.ALGO.SHA_256));
            final RequestBody requestAvatar = RequestBody.create(MediaType.parse("multipart/form-data"), mAvatarFile);
            String encryptedAvatarName = EncryptUtil.encrypt(mAvatarFile.getName(), EncryptUtil.ALGO.MD5);
            MultipartBody.Part avatarPart = MultipartBody.Part.createFormData("file", encryptedAvatarName, requestAvatar);

            Call<ResponseBody> call = uploadAvatarService.uploadAvatar(avatarPart, requestPhone);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(mContext, "头像上传成功");
                            }
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

                }
            });
        }

    }


    private void showOriginalImage() {
        final Bitmap portraitBitmap = getScaledImage(mAvatarPath, mIvAvatar);
        mIvAvatar.setImageBitmap(portraitBitmap);

    }

    private Bitmap getScaledImage(String filePath, ImageView imageView) {
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(filePath, bmOptions);
    }

    // Add the portrait to the galley
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File portraitPhoto = new File(mAvatarPath);
        Uri portraitUri = Uri.fromFile(portraitPhoto);
        mediaScanIntent.setData(portraitUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FOR_OPEN_CAMERA_AND_WRITE_EXTERNAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getPhotoByCamera();
            } else {
                ToastUtil.showToast(mContext, "permission denied");
            }
        } else if (requestCode == REQUEST_FOR_COPY_LOCAL_FILE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhotoFromFolder();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showLocalImage(Intent data) {
        Uri uri = data.getData();
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mIvAvatar.setImageBitmap(bitmap);
        // 将bitmap写入文件中
        BitmapToFileTask bitmapToFileTask = new BitmapToFileTask();
        bitmapToFileTask.execute(bitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        uploadAvatar();
    }

    @OnClick(R.id.tv_add_balance)
    public void onViewClicked() {
        Intent intent = new Intent(this, PayActivity.class);
        intent.putExtra("isPay", false);
        startActivity(intent);
    }

    private class ConnectLockReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                if (mControlLockDialog != null && !mControlLockDialog.isShowing()) {
                    mControlLockDialog.show();
                }
            }
        }
    }

    private class BitmapToFileTask extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... bitmap) {
            try {
                if (mAvatarFile.exists()) {
                    mAvatarFile.delete();
                } else {
                    mAvatarFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mAvatarFile != null) {
                FileUtils.bitmapToJpeg(bitmap[0], mAvatarFile);
            }
            return null;
        }
    }

}
