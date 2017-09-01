package com.qhiehome.ihome.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.support.annotation.NonNull;

import com.qhiehome.ihome.activity.FeedbackActivity;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.crashlog.CrashLogRequest;
import com.qhiehome.ihome.network.model.crashlog.CrashLogResponse;
import com.qhiehome.ihome.network.model.feedback.FeedbackRequest;
import com.qhiehome.ihome.network.model.feedback.FeedbackResponse;
import com.qhiehome.ihome.network.service.crashlog.CrashLogService;
import com.qhiehome.ihome.network.service.feedback.FeedbackService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrashHandler implements UncaughtExceptionHandler{

    private static final String TAG = "CrashHandler";

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private Context mContext;// 程序的Context对象
    private Map<String, String> deviceInfo;// 用来存储设备信息和异常信息
    // 用于格式化日期,作为日志文件名的一部分
//    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);

    private CrashHandler() {}

    private static final class CrashHandlerHelper {
        private static final CrashHandler INSTANCE = new CrashHandler();
    }

    public static CrashHandler getInstance() {
        return CrashHandlerHelper.INSTANCE;
    }

    public void init(Context context) {
        deviceInfo = new HashMap<>();
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
        Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!handleException(e) && mDefaultHandler != null) {
            // 如果自定义的没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(t, e);
        } else {
            try {
                Thread.sleep(3000);// 如果处理了，让程序继续运行3秒再退出，保证文件保存并上传到服务器
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     *            异常信息
     * @return true 如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null)
            return false;
        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
        saveCrashLog(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param context context
     */
    private void collectDeviceInfo(Context context) {
        try {
            PackageManager pm = context.getPackageManager();// 获得包管理器
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                deviceInfo.put("versionName", versionName);
                deviceInfo.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            LogUtil.e(TAG, "error occur");
        }

        Field[] fields = Build.class.getDeclaredFields();// 反射机制
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                deviceInfo.put(field.getName(), field.get("").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String saveCrashLog(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------------- log start --------------------------\r\n");
        for (Map.Entry<String, String> entry : deviceInfo.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\r\n");
        }
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        // 循环着把所有的异常信息写入writer中
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();// remember close
        String result = writer.toString();
        sb.append(result);
        sb.append("-------------------------- log end --------------------------");
        // save to file or send to server
        sendToServer(sb.toString());
        return null;
    }

    private void sendToServer(String crashLog) {
        CrashLogService crashLogService = ServiceGenerator.createService(CrashLogService.class);
        CrashLogRequest crashLogRequest = new CrashLogRequest(crashLog);
        Call<CrashLogResponse> call = crashLogService.uploadCrashLog(crashLogRequest);
        call.enqueue(new Callback<CrashLogResponse>() {
            @Override
            public void onResponse(@NonNull Call<CrashLogResponse> call, @NonNull Response<CrashLogResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    LogUtil.i(TAG, "send crash log successfully");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CrashLogResponse> call, @NonNull Throwable t) {

            }
        });
    }

}
