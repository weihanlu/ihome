package com.qhiehome.ihome.util;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {
	
	private static String mAppName = "";//caller APP
	private static String mFileName = "";//Log file path
	private static BufferedWriter mBufferedWriter = null;
	private static BufferedWriter mBufferedWriter2 = null;
	private static String mFileName2 = "";//Log file path
	public static String mDataFolder = "";
	public static String mLogFolder = "";
	public static String mCrashFolder = "";
	
	@SuppressLint("SimpleDateFormat")
	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	@SuppressLint("SimpleDateFormat")
	private static DateFormat formatterDay = new SimpleDateFormat("MM-dd-HH-mm-ss");

	public static enum WRITE_LEVEL{
		VERBOSE("VERBOSE"),
		  DEBUG("DEBUG  "),
		   INFO("INFO   "),
		   WARN("WARN   "),
		  ERROR("ERROR  "),
		 ASSERT("ASSERT ");
		
		private String tag;

		private WRITE_LEVEL(String tag){
			this.tag = tag;
		}

		public String toString(){
			return tag;
		}
	}
 

	public static void init(String appName, boolean isEnable){
		mAppName = appName;
		initFolder();
		if(isEnable)
		     mFileName = createLogFile();
	}
	
	public static void startSaveData(){
		mFileName2 = createDataFile();
	}
	
	public static void stopSaveData(){
		try {
			mFileName2 = "";
			mBufferedWriter2.close();
			mBufferedWriter2 = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    private static void initFolder(){
		File sdcardDir = getSDDir();
		if (sdcardDir != null) {
				
				mLogFolder = sdcardDir.getPath() + "/"+ mAppName +"_LOG/";//
				File path2 = new File(mLogFolder);
				if (!path2.exists()) {
					path2.mkdirs();
				}
				
				mCrashFolder = sdcardDir.getPath() + "/"+ mAppName +"_crash/";//
				File path3 = new File(mCrashFolder);
				if (!path3.exists()) {
					path3.mkdirs();
				}
				
//				startSaveData();
		}
    }
    
	private static String createDataFile() {
		String fileName = "";
		if (mDataFolder != "") {
				String time = formatter.format(new Date());
				fileName = mDataFolder + time  + ".txt";
		        File dir = new File(fileName);  
		        if (!dir.exists()) {  
		              try {  
		                  dir.createNewFile();  
		  				  if (mBufferedWriter2 != null) 
		  					  mBufferedWriter2.close();
		  				  FileWriter fileWritter = new FileWriter(fileName,true);
		  				  mBufferedWriter2 = new BufferedWriter(fileWritter);
		            } catch (Exception e) {  
		            	e.printStackTrace();
		            }  
		        }  
		        
		}
		return fileName;
	}
	
	public static void writeData(String strData) {
		
		try {
			if(mFileName2 != ""){
				
				String time = formatterDay.format(new Date());

				if(mBufferedWriter2 == null)
					return;
				mBufferedWriter2.write(time + ": " + strData + "\n");
				mBufferedWriter2.flush();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String createLogFile() {
		String fileName = "";
		if (mLogFolder!="") {
				String time = formatter.format(new Date());
				fileName = mLogFolder + time  + ".txt";
		        File dir = new File(fileName);  
		        if (!dir.exists()) {  
		              try {  
		                  dir.createNewFile();  
		  				  if (mBufferedWriter != null) 
		  					  mBufferedWriter.close();
		  				  FileWriter fileWritter = new FileWriter(fileName,true);
						  mBufferedWriter = new BufferedWriter(fileWritter);
		            } catch (Exception e) {  
		            	e.printStackTrace();
		            }  
		        }  
		        
		}
		return fileName;
	}
	
	private static File getSDDir() {
		File SDdir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
		        Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			SDdir = Environment.getExternalStorageDirectory();
		}
		if (SDdir != null) {
			return SDdir;
		} else {
			return null;
		}
	}

	public static void write(WRITE_LEVEL level,String TAG, String strLog) {
		
		String time = formatter.format(new Date());
	
		try {
			if(mFileName != ""){

				if(mBufferedWriter == null)
					return;
				mBufferedWriter.write(time + "   " 
							+ level.toString() + "   "
							+ TAG + "   "
							+ strLog + "\n");
				mBufferedWriter.flush();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void write(WRITE_LEVEL level,String TAG, Exception e) {
	
		try {
			
			if(mBufferedWriter == null)
				return;
			
			StackTraceElement[] es = e.getStackTrace();
			
			String time = formatter.format(new Date());

			if(mFileName != ""){
				
				String exception = time + "   "+ e.getClass().getName() + "\n";
				mBufferedWriter.write(exception);
				
				for(StackTraceElement ess:es){
					mBufferedWriter.write(time + "   "  
							+ level.toString() + "   "
							+ TAG + "   " 
							+ ess + "\n");
				}
				mBufferedWriter.flush();

			}


		} catch (IOException e2) {
			e.printStackTrace();
		}
	}

	public static void write(WRITE_LEVEL level, String tAG, Throwable ex) {
		try {
			
			if(mBufferedWriter == null)
				return;
			
			StackTraceElement[] es = ex.getStackTrace();
			
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date(timestamp));

			if(mFileName != ""){
				
				String exception = time + "   "+ ex.getClass().getName() + "\n";
				mBufferedWriter.write(exception);
				
				for(StackTraceElement ess:es){
					mBufferedWriter.write(time + "   "  
							+ level.toString() + "   "
							+ tAG + "   " 
							+ ess + "\n");
				}
				mBufferedWriter.flush();

			}


		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	public static String saveCrashInfo2File(Throwable ex, String info) {
		StringBuffer sb = new StringBuffer();
		sb.append(info);
		
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		try {
			String fileName = "";
			if (mCrashFolder != "") {
				String time = formatter.format(new Date());
				long timestamp = System.currentTimeMillis();
				fileName = "crash-" + time + "-" + timestamp + ".txt";
				FileOutputStream fos = new FileOutputStream(mCrashFolder + fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
				return fileName;
			}
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	


}
