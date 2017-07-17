package com.qhiehome.ihome.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.qhiehome.ihome.IhomeApplication;
import com.qhiehome.ihome.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

@SuppressLint("DefaultLocale")
public class APPUtils {
	
	public static boolean DEBUG_MODE = true;
	private final static String TAG = "APPUtils";
	private static Toast mToast;
	private static String mDeviceAddress = "";
	private static String mDeviceName = "";
	
	public static void init(Context context) {
		LogUtil.i(TAG, "MWatchUtils init---");
		String file[] = context.fileList();
		boolean isConfigFileExist = false;
		if (file != null) {
			for (String fileName : file) {
				if (fileName.equals("config.properties")) {
					isConfigFileExist = true;
					break;
				}
			}
		}
		if (!isConfigFileExist) {
			FileOutputStream outputStream;
			try {
				outputStream = context.openFileOutput("config.properties",
						Context.MODE_PRIVATE);
				outputStream.close();
				LogUtil.d(TAG, "No config.properties file found,create it!!!");
			} catch (FileNotFoundException e) {
		        LogUtil.e(TAG,e.getMessage());
			} catch (IOException e) {
		        LogUtil.e(TAG,e.getMessage());
			}
		} else {
			LogUtil.d(TAG, "Config.properties file found,no need to create!!!");
		}

	}

	public static Properties loadConfig(Context context) {
		if (context == null)
			context = IhomeApplication.getInstance();
		Properties properties = new Properties();
		try {
			FileInputStream fileInputStream = context
					.openFileInput("config.properties");
			properties.load(fileInputStream);
			fileInputStream.close();
		} catch (FileNotFoundException e) {
	        LogUtil.e(TAG,e.getMessage());
		} catch (IOException e) {
	        LogUtil.e(TAG,e.getMessage());
		}
		return properties;
	}

	// http://blog.csdn.net/jacklam200/article/details/7386533
	public static void saveConfig(Context context, String key, String value) {
		Properties localProperties = loadConfig(context);
		LogUtil.d(TAG, "saveConfig: key = " + key + ", value = " + value);
		try {
			FileOutputStream outputStream = context.openFileOutput(
					"config.properties", Context.MODE_PRIVATE);
			Enumeration<?> allEnumeration = localProperties.propertyNames();
			if (allEnumeration.hasMoreElements()) {
				while (allEnumeration.hasMoreElements()) {
					String nextKey = (String) allEnumeration.nextElement();
					if (!nextKey.equals(key)) {
						localProperties.setProperty(nextKey,
								localProperties.getProperty(nextKey));
					}
				}
			}
			localProperties.setProperty(key, value);
			localProperties.store(outputStream, null);
			outputStream.close();
			return;
		} catch (FileNotFoundException e) {
	        LogUtil.e(TAG,e.getMessage());
		} catch (IOException e) {
	        LogUtil.e(TAG,e.getMessage());
		}
	}

	public static boolean isBTEnabled() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			LogUtil.e(TAG, "Device don't surpport bluetooth....");
		}
		return bluetoothAdapter.isEnabled();
	}
	
	public static String getAppName(Context paramContext) {
		return paramContext.getString(R.string.app_name);
	}
	
	public static int getAppVersion(Context paramContext) {
		int version = 1;
		String packageName = paramContext.getPackageName();
		try {
			version = paramContext.getPackageManager().getPackageInfo(
					packageName, 0).versionCode;
			LogUtil.d(TAG, "getAppVersion() return=" + version);
		} catch (PackageManager.NameNotFoundException e) {
			LogUtil.e(TAG, "getAppVersion() Exception="
					+ e.getStackTrace().toString());
		}
		return version;
	}
	
	public static String getAppVersionName(Context paramContext) {
		String versionName = "V";
		String packageName = paramContext.getPackageName();
		try {
			versionName += paramContext.getPackageManager().getPackageInfo(
					packageName, 0).versionName;
			LogUtil.d(TAG, "getAppVersionName() return=" + versionName);
		} catch (PackageManager.NameNotFoundException e) {
			LogUtil.e(TAG, "getAppVersionName() Exception="
					+ e.getStackTrace().toString());
		}
		return versionName;
	}


	public static int getLastVersion(Context context) {
		String version = loadConfig(context).getProperty("KEY_LAST_VERSION");
		if (version == null)
			return -1;
		return Integer.parseInt(version);
	}

	public static void setLastVersion(Context context, String version) {
		saveConfig(context, "KEY_LAST_VERSION", version);
	}
	
	//fw version
	public static String getFwVersion(Context context) {
		String version = loadConfig(context).getProperty("KEY_FW_VERSION");
		return version == null ? "" : version;
	}

	public static void setFwVersion(Context context, String version) {
		saveConfig(context, "KEY_FW_VERSION", version);
	}

	//Name and address need not to save in file
	public static String getWatchName(Context context) {
		return mDeviceName;
	}

	public static void setWatchName(Context context, String name) {
		mDeviceName = name;
	}

	public static String getWatchAddress(Context context) {
		return mDeviceAddress;
	}

	public static void setWatchAddress(Context context, String address) {
		mDeviceAddress = address;
	}

	private static boolean mAuto_park = false;
	private static boolean mRemoteStudy = false;
	private static boolean mAuto_park_status = false;

	//suport feature or not
	public static boolean  getAutoPark(Context context) {
		return mAuto_park;
	}
	
	public static void setAutoPark(Context context, boolean auto_park) {
		mAuto_park = auto_park;
	}

	public static void reverseAutoParkStatus(Context context) {
		mAuto_park = !mAuto_park;
	}
	
	//suport feature or not
	public static boolean  getRemoteStudy(Context context) {
		return mRemoteStudy;
	}
	
	public static void setRemoteStudy(Context context, boolean remote) {
		mRemoteStudy = remote;
	}
	
	//current status
	public static boolean  getAutoParkStatus(Context context) {
		return mAuto_park_status;
	}

	public static void setAutoParkStatus(Context context, boolean auto_park_status) {
		mAuto_park_status = auto_park_status;
	}
	
	/*********************************************LOG BYTES**********************************************************/
	public static void logOutByteArray(String logTag, byte data[]) {
		String dataLog = byteArrayToString(data);
		LogUtil.i(logTag, dataLog);
	}

	public static String byteArrayToString(byte data[]) {

		if (data == null)
			return "";

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < data.length-1; i++) {
			builder.append(String.format("%02x-", (int) (data[i] & 0xFF)));
		}
		builder.append(String.format("%02x", (int) (data[data.length-1] & 0xFF)));
		return builder.toString();
	}
	
	public static String byteArrayToDeciString(int data[]) {

		if (data == null)
			return "";

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < data.length-1; i++) {
			builder.append(String.format("%d ", (int) (data[i] & 0xFF)));
		}
		builder.append(String.format("%d", (int) (data[data.length-1] & 0xFF)));
		return builder.toString();
	}

	public static byte[] hexStringToByte(String[] data_str_array) {
		int index = 0;
		if(data_str_array == null){
			index = -1;
		}else if(data_str_array.length > 10){
			index = 9;
		}else if(data_str_array.length <= 10){
			index = data_str_array.length;
		}
		byte data[] = {(byte)0x00,(byte)0x00,(byte)0x00,
					   (byte)0x00,(byte)0x00,(byte)0x00,
					   (byte)0x00,(byte)0x00,(byte)0x00,
					   (byte)0x00,};
		for(int i=0; i <index; i++){
			String temp = data_str_array[i];
			char ch = temp.length() >= 1 ? temp.charAt(0):'0';
			char ch1 = temp.length() >= 2 ? temp.charAt(1):'0';
			int low = 0;
			int high = 0;
			
			if(ch >= '0' && ch <= '9'){
				low = ch - '0';
			}else if(ch >= 'A' && ch <= 'F'){
				low = 10 + ch - 'A';
			}else if(ch >= 'a' && ch <= 'f'){
				low = 10 + ch - 'a';
			}
			
			if(ch1 >= '0' && ch1 <= '9'){
				high = ch1 - '0';
			}else if(ch1 >= 'A' && ch1 <= 'F'){
				high = 10 + ch1 - 'A';
			}else if(ch1 >= 'a' && ch1 <= 'f'){
				high = 10 + ch1 - 'a';
			}

			data[i] = (byte)(low * 16 + high);
		}
		return data;
	}
	
	public static byte[] deciStringToByte(String[] data_str_array) {
		int size = data_str_array.length;
		byte array[] = new byte[size];
		for(int i = 0; i < size; i ++){
			array[i] = (byte)(Integer.valueOf(data_str_array[i]) & 0xFF);
		}
		return array;
	}

	/*********************************************DATE UTILS**********************************************************/
	public static Date getDate() {
		return Calendar.getInstance().getTime();
	}

	public static byte[] date2bcd() {
		return _int2bcd(getTimeArray(getDate()));
	}
	
	public static String datebcd2string(byte[] data){
		return byteArrayToString(data).replaceAll("-", "");
	}
	
	public static int[] getTimeArray(Date date) {
		int year = date.getYear();// Calendar.getInstance().get(Calendar.YEAR) - 1900; //
		int month = date.getMonth() + 1; //Calendar.getInstance().get(Calendar.MONTH) + 1;//
		int day = date.getDate();//Calendar.getInstance().get(Calendar.DAY_OF_MONTH);//
		int minute =  date.getMinutes();//Calendar.getInstance().get(Calendar.MINUTE);//
		int hour = date.getHours();//Calendar.getInstance().get(Calendar.HOUR_OF_DAY);// 
		int sec = date.getSeconds();//Calendar.getInstance().get(Calendar.SECOND);//
	
//		LogUtils.i(TAG,"getTime--- year:" + (year - (year/100) *100) + ", month: " + month
//				+ ", day: " + day + ", hour" + hour + ", minute:" + minute + ", sec:" + sec);
		return new int[] {20, year - (year/100) *100, month, day, hour,minute, sec };
	}

	/*********************************************BCD UTILS**********************************************************/
	//data has 2 chars, 20 -> 0x20
	public static byte[] _int2bcd(int[] data) {
		if (data.length == 0)
			return null;

		byte b[] = new byte[data.length];
		
		for (int i = 0; i < data.length; i++) {
			b[i] = 0;
		}
		for (int i = 0; i < data.length; i++) {
			if (data[i] >= 100) {
				LogUtil.e(TAG,
				        "data is bigger than 100, so you can not change it to one BCD byte(8bits)!!!!");
				return b;
			}
			b[i] = (byte) (data[i] % 10 + (data[i] / 10) * 16);
		}
		return b;
	}
	
	//data has 2 chars, 20 -> 0x20
	public static byte _int2bcd(int data) {
		if (data >= 100) {
			LogUtil.e(TAG,
			        "data is bigger than 100, so you can not change it to one BCD byte(8bits)!!!!");
			return 0;
		}
		return (byte) (data % 10 + data / 10 << 4);
	}

	//data has 1 chars, 7 -> 0x07
	public static byte[] int2bcd(int[] data) {
		if (data.length == 0)
			return null;
        int size = data.length/2;
		byte b[] = new byte[size];
		
		for (int i = 0; i < size; i++) {
			b[i] = 0;
		}
		for (int i = 0; i < size; i++) {
			if (data[i] >= 10) {
				LogUtil.e(TAG,
				        "data is bigger than 100, so you can not change it to one BCD byte(8bits)!!!!");
				return b;
			}
			b[i] = (byte) (data[i*2+1] | (data[i*2] << 4));
		}
		return b;
	}
	
	public static int[] bcd2int(byte data) {
		int result[] = {0,0};
		result[1] = data & 0x0f;
		result[0] = (data & 0xf0) >> 4;
		return result;
	}
	
	public static int[] bcd2int(byte[] data) {
		int size = data.length;
		int result[] = new int[size*2];
		for(int i = 0; i < size; i++){
			int temp[] = bcd2int(data[i]);
			result[i*2] = temp[0];
			result[i*2+1] = temp[1];
		}
		return result;
	}


	/*******************************************************************************************************/
	/**
	 * Decodes the device name from Complete Local Name or Shortened Local Name field in Advertisement packet. Ususally if should be done by {@link BluetoothDevice#getName()} method but some phones
	 * skips that, f.e. Sony Xperia Z1 (C6903) with Android 4.3 where getName() always returns <code>null</code>. In order to show the device name correctly we have to parse it manually :(
	 */
	private static final int SHORTENED_LOCAL_NAME = 0x08;
	private static final int COMPLETE_LOCAL_NAME = 0x09;

	public static String decodeDeviceName(byte[] data) {
		String name = null;
		int fieldLength, fieldName;
		int packetLength = data.length;
		for (int index = 0; index < packetLength; index++) {
			fieldLength = data[index];
			if (fieldLength == 0)
				break;
			fieldName = data[++index];

			if (fieldName == COMPLETE_LOCAL_NAME || fieldName == SHORTENED_LOCAL_NAME) {
				name = decodeLocalName(data, index + 1, fieldLength - 1);
				break;
			}
			index += fieldLength - 1;
		}
		return name;
	}

	/**
	 * Decodes the local name
	 */
	public static String decodeLocalName(final byte[] data, final int start, final int length) {
		try {
			return new String(data, start, length, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			Log.e(TAG, "Unable to convert the complete local name to UTF-8", e);
			return null;
		} catch (final IndexOutOfBoundsException e) {
			Log.e(TAG, "Error when reading complete local name", e);
			return null;
		}
	}
	
	//network
	public static final int CMNET = 3;
	public static final int CMWAP = 2;
	public static final int WIFI = 1;
	public static final int NONET = -1;

	public static int getAPNType(Context context) {

		int netType = -1;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			LogUtil.d(TAG,
					"networkInfo.getExtraInfo() is "
							+ networkInfo.getExtraInfo());
			if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
				netType = CMNET;
			} else {
				netType = CMWAP;
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = WIFI;
		}
		return netType;
	}

	public static boolean isNetworkConnected(Context paramContext) {
		NetworkInfo networkInfo = ((ConnectivityManager) paramContext
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		return (networkInfo != null) && (networkInfo.isConnected());
	}


}
