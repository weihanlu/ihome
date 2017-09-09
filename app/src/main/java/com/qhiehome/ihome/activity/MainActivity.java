package com.qhiehome.ihome.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.mapapi.SDKInitializer;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.fragment.ParkFragment;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.avatar.UploadAvatarResponse;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceRequest;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceResponse;
import com.qhiehome.ihome.network.model.update.CheckUpdateResponse;
import com.qhiehome.ihome.network.service.avatar.DownloadAvatarService;
import com.qhiehome.ihome.network.service.avatar.UploadAvatarService;
import com.qhiehome.ihome.network.service.pay.AccountBalanceService;
import com.qhiehome.ihome.network.service.update.PgyService;
import com.qhiehome.ihome.network.service.update.PgyServiceGenerator;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.FileUtils;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.QhAvatarSelectDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int CODE_CAMERA_REQUEST_SRC = 1;
    private static final int REQUEST_FOR_OPEN_CAMERA_AND_WRITE_EXTERNAL = 2;
    private static final int CODE_PICTURES_REQUEST_SRC = 3;
    private static final int REQUEST_FOR_COPY_LOCAL_FILE = 4;

    @BindView(R.id.iv_avatar)
    CircleImageView mIvAvatar;
    @BindView(R.id.tv_user_balance)
    TextView mTvUserBalance;
    @BindView(R.id.bt_login)
    Button btLogin;
    @BindView(R.id.tv_add_balance)
    TextView mTvAddBalance;
    @BindView(R.id.tv_user_account)
    TextView mTvUserAccount;
    @BindView(R.id.tv_label_balance)
    TextView mTvBalanceLabel;
    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.ll_my_lock)
    LinearLayout mLlMyLock;
    @BindView(R.id.ll_my_publish)
    LinearLayout mLlMyPublish;

    private Context mContext;

    Fragment mParkFragment;
    Fragment mThisFragment;
    FragmentManager mFragmentManager;

    private boolean cancelUpdate;

    MaterialDialog mUpdateInfoDialog;

    MaterialDialog mUpdateProcessDialog;

    private String mSavedPath;

    private boolean isLogin;

    private File mAvatarFile;

    private String mAvatarPath;

    private String mAvatarName;

    private boolean isFirst;

    private String mPhoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;
        initFragments(savedInstanceState);
        checkUpdate();
        isFirst = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserType();
        checkLogin();
    }

    private void checkLogin() {
        mPhoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, "");
        if (!TextUtils.isEmpty(mPhoneNum)) {
            isLogin = true;
        }
        mIvAvatar.setVisibility(isLogin ? View.VISIBLE : View.INVISIBLE);
        mTvUserBalance.setVisibility(isLogin ? View.VISIBLE : View.INVISIBLE);
        btLogin.setVisibility(isLogin ? View.INVISIBLE : View.VISIBLE);
        mTvAddBalance.setVisibility(isLogin ? View.VISIBLE : View.INVISIBLE);
        mTvUserAccount.setVisibility(isLogin ? View.VISIBLE : View.INVISIBLE);
        mTvBalanceLabel.setVisibility(isLogin ? View.VISIBLE : View.INVISIBLE);
        if (isLogin) {
            initBalance();
            if (isFirst) {
                initAvatar();
                mTvUserAccount.setText(mPhoneNum);
                isFirst = false;
            }
        }
    }

    private void initBalance() {
        AccountBalanceService accountBalanceService = ServiceGenerator.createService(AccountBalanceService.class);
        AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(EncryptUtil.encrypt(mPhoneNum, EncryptUtil.ALGO.SHA_256), 0.0);
        Call<AccountBalanceResponse> call = accountBalanceService.account(accountBalanceRequest);
        call.enqueue(new Callback<AccountBalanceResponse>() {
            @Override
            public void onResponse(Call<AccountBalanceResponse> call, Response<AccountBalanceResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    mTvUserBalance.setText(String.format(getString(R.string.format_user_balance), response.body().getData().getAccount()));
                }
            }

            @Override
            public void onFailure(Call<AccountBalanceResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }

    private void initAvatar() {
        mAvatarName = "portrait_" + mPhoneNum;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        mAvatarFile = new File(storageDir, mAvatarName + ".jpg");
        mAvatarPath = mAvatarFile.getAbsolutePath();
        File avatarDir = mAvatarFile.getParentFile();
        if (avatarDir.isDirectory() && avatarDir.listFiles().length != 0) {
            Bitmap avatarBitmap = BitmapFactory.decodeFile(mAvatarPath);
            mIvAvatar.setImageBitmap(avatarBitmap);
        } else {
            LogUtil.d(TAG, "download avatar");
            DownloadAvatarService downloadAvatarService = ServiceGenerator.createService(DownloadAvatarService.class);
            String encryptedAvatarName = EncryptUtil.encrypt(mAvatarName, EncryptUtil.ALGO.MD5);
            Call<ResponseBody> call = downloadAvatarService.downloadAvatar(encryptedAvatarName + ".jpg");
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            mAvatarFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        writtenToAvatarFile(response.body());
                    } else {
                        mIvAvatar.setBackground(ContextCompat.getDrawable(mContext, R.drawable.selector_bg_avatar));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    mIvAvatar.setBackground(ContextCompat.getDrawable(mContext, R.drawable.selector_bg_avatar));
                }
            });
        }
    }

    private void writtenToAvatarFile(ResponseBody body) {
        InputStream is = null;
        OutputStream os = null;
        try {
            byte[] fileReader = new byte[4096];

            is = body.byteStream();
            os = new FileOutputStream(mAvatarFile);
            int read;
            while ((read = is.read(fileReader)) != -1) {
                os.write(fileReader, 0, read);
            }
            os.flush();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAvatarFile != null && mAvatarFile.length() == 0) {
                        mIvAvatar.setBackground(ContextCompat.getDrawable(mContext, R.drawable.selector_bg_avatar));
                    } else {
                        Bitmap avatarBitmap = BitmapFactory.decodeFile(mAvatarPath);
                        mIvAvatar.setImageBitmap(avatarBitmap);
                    }

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkUpdate() {
        // 1. request versionCode on server
        PgyService pgyService = PgyServiceGenerator.createService(PgyService.class);
        Call<CheckUpdateResponse> call = pgyService.getLatestVersion(Constant.APK_UPDATE_UKEY, Constant.APK_UPDATE_API_KEY, Constant.APK_UPDATE_PAGE_NUM);
        call.enqueue(new Callback<CheckUpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<CheckUpdateResponse> call, @NonNull Response<CheckUpdateResponse> response) {
                CheckUpdateResponse body = response.body();
                if (body != null && body.getCode().equals("0")) {
                    List<CheckUpdateResponse.DataBean.ListBean> list = body.getData().getList();
                    int onLineAppVersionNo = Integer.valueOf(list.get(0).getAppVersionNo());
                    if (onLineAppVersionNo > CommonUtil.getVersionCode()) {
                        SharedPreferenceUtil.setBoolean(mContext, Constant.UPDATE_ENABLED, true);
                        final String appKey = list.get(0).getAppKey();
                        final String appUpdateDescription = list.get(0).getAppUpdateDescription();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mUpdateInfoDialog == null)
                                    buildUpdateInfoDialog(appUpdateDescription, appKey);
                                mUpdateInfoDialog.show();
                            }
                        });
                    } else {
                        SharedPreferenceUtil.setBoolean(mContext, Constant.UPDATE_ENABLED, false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CheckUpdateResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void buildUpdateInfoDialog(String updateInfo, final String appKey) {
        String[] updateInfoItems = updateInfo.split(";");
        StringBuilder formatUpdateInfo = new StringBuilder();
        for (int i = 0; i < updateInfoItems.length; i++) {
            formatUpdateInfo.append(i + 1).append(". ").append(updateInfoItems[i]).append("\n");
        }
        formatUpdateInfo.deleteCharAt(formatUpdateInfo.length() - 1);
        mUpdateInfoDialog = new MaterialDialog.Builder(this)
                .title("新特性")
                .content(formatUpdateInfo.toString())
                .positiveText("立即更新")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (mUpdateProcessDialog == null) buildUpdateProcessDialog();
                        mUpdateProcessDialog.show();
                        downloadApk(String.format(Constant.APK_UPDATE_URL_PATTERN, appKey));
                    }
                })
                .canceledOnTouchOutside(true).build();
    }

    private void buildUpdateProcessDialog() {
        mUpdateProcessDialog = new MaterialDialog.Builder(this)
                .title("正在更新")
                .content("下载进度")
                .progress(false, 100, true)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                        cancelUpdate = true;
                    }
                }).build();
    }

    private void downloadApk(String downloadUrl) {
        new DownloadAsyncTask().execute(downloadUrl);
    }

    private void initFragments(Bundle savedInstanceState) {
        mFragmentManager = getSupportFragmentManager();
        // First init load ParkFragment
        if (savedInstanceState == null) {
            mParkFragment = ParkFragment.newInstance();
            mFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, mParkFragment, ParkFragment.TAG).commit();
        } else {
            mParkFragment = mFragmentManager.findFragmentByTag(ParkFragment.TAG);
            mFragmentManager.beginTransaction()
                    .show(mParkFragment)
                    .commit();
        }
        mThisFragment = mParkFragment;
    }

//    private void switchContent(Fragment from, Fragment to) {
//        if (mThisFragment != to) {
//            mThisFragment = to;
//            if (!to.isAdded()) {
//                String tag = (to instanceof MeFragment) ? MeFragment.TAG: ParkFragment.TAG;
//                mFragmentManager.beginTransaction().hide(from).add(R.id.fragment_container, to, tag).commit();
//            } else {
//                mFragmentManager.beginTransaction().hide(from).show(to).commit();
//            }
//        }
//    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mParkFragment.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CODE_CAMERA_REQUEST_SRC) {
                galleryAddPic();
                showOriginalImage();
            } else if (requestCode == CODE_PICTURES_REQUEST_SRC) {
                showLocalImage(data);
            }
        }
    }

    @OnClick({R.id.ll_my_lock, R.id.ll_my_reserve, R.id.ll_my_publish, R.id.ll_setting,
            R.id.iv_avatar, R.id.bt_login, R.id.tv_add_balance})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_my_lock:
                if (isLogin) {
                    UserLockActivity.start(mContext);
                } else {
                    LoginActivity.start(mContext);
                }
                break;
            case R.id.ll_my_reserve:
                if (isLogin) {
                    ReserveActivity.start(mContext);
                } else {
                    LoginActivity.start(mContext);
                }
                break;
            case R.id.ll_my_publish:
                if (isLogin) {
                    PublishParkingActivity.start(mContext);
                } else {
                    LoginActivity.start(mContext);
                }
                break;
            case R.id.ll_setting:
                if (isLogin) {
                    SettingActivity.start(mContext);
                } else {
                    LoginActivity.start(mContext);
                }
                break;
            case R.id.iv_avatar:
                QhAvatarSelectDialog dialog = new QhAvatarSelectDialog(mContext);
                dialog.setOnItemClickListener(new QhAvatarSelectDialog.OnItemClickListener() {
                    @Override
                    public void onTakePhoto(View view) {
                        if (CommonUtil.checkCameraHardware(mContext)) {
                            openCamera();
                        } else {
                            ToastUtil.showToast(mContext, "没有检测到相机");
                        }
                    }

                    @Override
                    public void onGallery(View view) {
                        openLocalFolder();
                    }
                });
                dialog.show();
                break;
            case R.id.bt_login:
                LoginActivity.start(mContext);
                break;
            case R.id.tv_add_balance:
                Intent intent = new Intent(mContext, PayActivity.class);
                intent.putExtra("payState", Constant.PAY_STATE_ADD_ACCOUNT);
                startActivity(intent);
                break;
            default:
                break;
        }
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

    private void showOriginalImage() {
        final Bitmap portraitBitmap = getScaledImage(mAvatarPath, mIvAvatar);
        // 将bitmap写入文件中
        BitmapToFileTask bitmapToFileTask = new BitmapToFileTask();
        bitmapToFileTask.execute(portraitBitmap);
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
        // 将bitmap写入文件中
        BitmapToFileTask bitmapToFileTask = new BitmapToFileTask();
        bitmapToFileTask.execute(bitmap);
    }

    private void uploadAvatar() {
        File avatarDir = mAvatarFile.getParentFile();
        if (avatarDir.isDirectory() && avatarDir.listFiles().length != 0) {
            UploadAvatarService uploadAvatarService = ServiceGenerator.createService(UploadAvatarService.class);
            RequestBody requestPhone = RequestBody.create(MediaType.parse("multipart/form-data"), EncryptUtil.encrypt(mPhoneNum, EncryptUtil.ALGO.SHA_256));
            final RequestBody requestAvatar = RequestBody.create(MediaType.parse("multipart/form-data"), mAvatarFile);
            LogUtil.d(TAG, "file length is " + mAvatarFile.length());
            String encryptedAvatarName = EncryptUtil.encrypt(mAvatarName, EncryptUtil.ALGO.MD5);
            MultipartBody.Part avatarPart = MultipartBody.Part.createFormData("file", encryptedAvatarName + ".jpg", requestAvatar);

            Call<UploadAvatarResponse> call = uploadAvatarService.uploadAvatar(avatarPart, requestPhone);
            call.enqueue(new Callback<UploadAvatarResponse>() {
                @Override
                public void onResponse(Call<UploadAvatarResponse> call, Response<UploadAvatarResponse> response) {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    }
                }

                @Override
                public void onFailure(Call<UploadAvatarResponse> call, Throwable t) {

                }
            });
        }

    }

    // Add the portrait to the galley
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File portraitPhoto = new File(mAvatarPath);
        Uri portraitUri = Uri.fromFile(portraitPhoto);
        mediaScanIntent.setData(portraitUri);
        this.sendBroadcast(mediaScanIntent);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FOR_OPEN_CAMERA_AND_WRITE_EXTERNAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getPhotoByCamera();
            } else {
                ToastUtil.showToast(mContext, "没有相应的权限");
            }
        } else if (requestCode == REQUEST_FOR_COPY_LOCAL_FILE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhotoFromFolder();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void openDrawer() {
        if (drawer != null && !drawer.isDrawerOpen(Gravity.START)) {
            drawer.openDrawer(Gravity.START);
        }
    }

    private class DownloadAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavedPath = sdpath + "Download";
                    URL url = new URL(params[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();
                    File file = new File(mSavedPath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavedPath, "Ihome.apk");
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    byte buf[] = new byte[1024];
                    do {
                        int numRead = is.read(buf);
                        count += numRead;
                        final int progress = (int) (((float) count / length) * 100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUpdateProcessDialog.setProgress(progress);
                            }
                        });
                        if (numRead <= 0) {
                            installApk();
                            break;
                        }
                        fos.write(buf, 0, numRead);
                    } while (!cancelUpdate);
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUpdateProcessDialog.dismiss();
                }
            });
            return null;
        }
    }

    private void installApk() {
        File apkFile = new File(mSavedPath, "Ihome.apk");
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(this, "com.qhiehome.ihome.provider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        startActivity(intent);
        Process.killProcess(Process.myPid());
    }

    private class BitmapToFileTask extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(final Bitmap... bitmap) {
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIvAvatar.setImageBitmap(bitmap[0]);
                }
            });
            uploadAvatar();
            return null;
        }
    }

    private void checkUserType() {
        int userType = SharedPreferenceUtil.getInt(mContext, Constant.USER_TYPE, Constant.USER_TYPE_TEMP);
        mLlMyLock.setVisibility(userType == Constant.USER_TYPE_TEMP? View.GONE: View.VISIBLE);
        mLlMyPublish.setVisibility(userType == Constant.USER_TYPE_TEMP? View.GONE: View.VISIBLE);
    }



}
