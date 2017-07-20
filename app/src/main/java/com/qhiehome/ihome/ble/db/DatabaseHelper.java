package com.qhiehome.ihome.ble.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qhiehome.ihome.application.IhomeApplication;
import com.qhiehome.ihome.bean.LockBean;
import com.qhiehome.ihome.util.LogUtil;

import java.util.ArrayList;

/**
 *
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    public static final String DB_NAME = "CrAM.db";
    public static final int DB_VERSION = 1;

    // table attributes
    public static final String TABLE_LOCK = "lock";
    public static final String FIELD_ID = "_id";
    public static final String FIELD_LOCK_ADDRESS = "address";
    public static final String FIELD_LOCK_NAME = "name";
    public static final String FIELD_LOCK_PASSWORD = "password";
    public static final String FIELD_LOCK_ROLE = "role";
    public static final String FIELD_LOCK_DEFAULT = "isDefault";
    public static final String FIELD_LOCK_AUTO_PARK = "autopark";
    public static final String FIELD_LOCK_PASSWORD_CUSTOMER = "password_customer";
    public static final String FIELD_LOCK_REAL_NAME = "realname";

    private static final int ADDRESS_ID = 0;
    private static final int ADDRESS_INDEX = 1;
    private static final int NAME_INDEX = 2;
    private static final int PASSWORD_INDEX = 3;
    private static final int ROLE_INDEX = 4;
    private static final int DEFAULT_INDEX = 5;
    private static final int AUTO_PARK_INDEX = 6;
    private static final int PASSWORD_CUSTOMER_INDEX = 7;
    private static final int REAL_NAME_INDEX = 8;

    // table untrusted lock
    public static final String TABLE_UNTRUSTED_LOCK = "untrustedLocks";
    public static final String FIELD_LAST_LOGIN_TIME = "last_time";

    private static final int LOGIN_TIME_INDEX = 2;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private static class DatabaseHelperHolder {
        private static final DatabaseHelper INSTANCE = new DatabaseHelper(IhomeApplication.getInstance());
    }

    public static DatabaseHelper getInstance() {
        return DatabaseHelperHolder.INSTANCE;
    }

    public SQLiteDatabase getDatabase(boolean writable) {
        return writable? getWritableDatabase(): getReadableDatabase();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        LogUtil.d(TAG, "--onOpen-- DB Version: " + DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.d(TAG, "--onCreate-- DB Version: " + DB_VERSION);
        createLockTable(db);
        createUntrustedLockTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.d(TAG, "onUpgrade, oldVersion: " + oldVersion + ", " + "newVersion: " + newVersion);
    }

    private void createLockTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCK);
        db.execSQL("CREATE TABLE " + TABLE_LOCK + " ("
                + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FIELD_LOCK_ADDRESS + " TEXT, "
                + FIELD_LOCK_NAME + " TEXT, "
                + FIELD_LOCK_PASSWORD + " TEXT, "
                + FIELD_LOCK_ROLE + " TEXT, "
                + FIELD_LOCK_DEFAULT + " TEXT, "
                + FIELD_LOCK_AUTO_PARK + " TEXT, "
                + FIELD_LOCK_PASSWORD_CUSTOMER + " TEXT, "
                + FIELD_LOCK_REAL_NAME + " TEXT);");
        LogUtil.d(TAG, "LOCK table has been created");
    }

    private void createUntrustedLockTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UNTRUSTED_LOCK);
        db.execSQL("CREATE TABLE " + TABLE_UNTRUSTED_LOCK + " ("
                + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FIELD_LOCK_ADDRESS + " TEXT, "
                + FIELD_LAST_LOGIN_TIME + " TEXT"
                +");");
        LogUtil.d(TAG, "UNTRUST_LOCK table has been created!");
    }

    public void insertLock(LockBean lock) {
        SQLiteDatabase db = getDatabase(true);
        deleteLock(lock.getAddress());
        if(lock.isDefault() == LockBean.LOCK_DEFAULT.DEFAULT.ordinal()){
            ContentValues cv1 = new ContentValues();
            cv1.put(FIELD_LOCK_DEFAULT, LockBean.LOCK_DEFAULT.NOT_DEFAULT.ordinal());
            db.update("lock", cv1, "isDefault=?", new String[]{String.valueOf(LockBean.LOCK_DEFAULT.DEFAULT.ordinal())});// 执行修改
            db = getDatabase(true);
        }

        ContentValues cv = new ContentValues();
        cv.put(FIELD_LOCK_ADDRESS, lock.getAddress());
        cv.put(FIELD_LOCK_NAME, lock.getName());
        cv.put(FIELD_LOCK_REAL_NAME, lock.getRealName());
        cv.put(FIELD_LOCK_ROLE, lock.getRole());
        cv.put(FIELD_LOCK_DEFAULT, lock.isDefault());
        cv.put(FIELD_LOCK_AUTO_PARK, lock.getAutoPark());
        cv.put(FIELD_LOCK_PASSWORD, lock.getPassword());
        cv.put(FIELD_LOCK_PASSWORD_CUSTOMER, lock.getCustomerPassword());
        db.insert("lock", null, cv);// 执行插入操作

        LogUtil.i(TAG, "insert name: " + lock.getName());
        db = null;
        db = getDatabase(true);
        String sql = "delete from lock where _id IN ( select _id from lock ORDER BY _id DESC LIMIT -1 OFFSET 5 )";
        db.execSQL(sql);
    }

    public void updateLock(ContentValues cv, String whereClause,
                           String[] whereArgs) {
        if(cv.containsKey(FIELD_LOCK_DEFAULT) && cv.getAsInteger(FIELD_LOCK_DEFAULT) == LockBean.LOCK_DEFAULT.DEFAULT.ordinal()){
            clearAllDefault();
        }
        SQLiteDatabase db = getDatabase(true);
        db.update("lock", cv, whereClause, whereArgs);// 执行修改
    }

    private void clearAllDefault() {
        SQLiteDatabase db = getDatabase(true);
        ContentValues cv1 = new ContentValues();
        cv1.put(FIELD_LOCK_DEFAULT, LockBean.LOCK_DEFAULT.NOT_DEFAULT.ordinal());
        db.update("lock", cv1, "isDefault=?", new String[]{"1"});// 执行修改
    }

    public void deleteLock(String address) {
        SQLiteDatabase db = getDatabase(true);
        String sql = "delete from lock where address=" + "'" + address + "'";// 删除操作的SQL语句
        db.execSQL(sql);
    }

    public LockBean queryLockBean(String mAddress) {
        Cursor c = null;
        try {
            SQLiteDatabase db = getDatabase(false);
            c = db.rawQuery("select * from lock where address=?",
                    new String[] {mAddress});
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                String address = c.getString(ADDRESS_INDEX);
                String name = c.getString(NAME_INDEX);
                String realName = c.getString(REAL_NAME_INDEX);
                int role = c.getInt(ROLE_INDEX);
                int isDefault = c.getInt(DEFAULT_INDEX);
                String mm = c.getString(PASSWORD_INDEX);
                String mm_customer = c.getString(PASSWORD_CUSTOMER_INDEX);
                int autoPark = c.getInt(AUTO_PARK_INDEX);
                c.close();
                LockBean lock = new LockBean(address, name, role, isDefault, realName);
                if(!mm.equals(""))
                    lock.setPassword(mm);
                if(!(mm_customer.equals("")))
                    lock.setCustomerPassword(mm_customer);
                lock.setAutoPark(autoPark);
                return lock;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null)
                c.close();
        }
        return null;
    }

    public LockBean getDefaultLockBean() {
        Cursor c = null;
        try {
            SQLiteDatabase db = getDatabase(false);
            c = db.rawQuery("select * from lock where isDefault=?",
                    new String[] { String.valueOf(LockBean.LOCK_DEFAULT.DEFAULT.ordinal())});
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                String address = c.getString(ADDRESS_INDEX);
                String name = c.getString(NAME_INDEX);
                int role = c.getInt(ROLE_INDEX);
                int isDefault = c.getInt(DEFAULT_INDEX);
                String mm = c.getString(PASSWORD_INDEX);
                String mm_customer = c.getString(PASSWORD_CUSTOMER_INDEX);
                String realName = c.getString(REAL_NAME_INDEX);
                int autoPark = c.getInt(AUTO_PARK_INDEX);
                c.close();
                LockBean lock = new LockBean(address, name, role, isDefault, realName);
                if(mm != "")
                    lock.setPassword(mm);
                if(mm_customer != "")
                    lock.setPassword(mm_customer);
                lock.setAutoPark(autoPark);
                return lock;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null)
                c.close();
        }
        return null;
    }

    public boolean updateName(String address, String newName) {
        SQLiteDatabase db = getDatabase(true);
        ContentValues cv = new ContentValues();
        cv.put(FIELD_LOCK_NAME, newName);
        int id = db.update("lock", cv, "address=?",new String[]{ address});
        return id>=0;
    }

    public boolean updatePassword(String mm, boolean setOwner, String address) {
        SQLiteDatabase db = getDatabase(true);
        ContentValues cv = new ContentValues();
        int id = -1;
        if(setOwner){
            cv.put(FIELD_LOCK_PASSWORD, mm);
            id = db.update("lock", cv, "address=?", new String[]{ address});
        }else{
            cv.put(FIELD_LOCK_PASSWORD_CUSTOMER, mm);
            id = db.update("lock", cv, "address=?",new String[]{ address});
        }
        return id>=0;
    }

    public ArrayList<LockBean> getAllLocks() {
        ArrayList<LockBean> lockList = new ArrayList<LockBean>();
        Cursor c = null;
        try {
            SQLiteDatabase db = getDatabase(false);
            c = db.rawQuery("select * from lock where address<>?",
                    new String[] {""});
            while (c != null && c.moveToNext()) {

                String address = c.getString(ADDRESS_INDEX);
                String name = c.getString(NAME_INDEX);
                String realName = c.getString(REAL_NAME_INDEX);
                int role = c.getInt(ROLE_INDEX);
                int isDefault = c.getInt(DEFAULT_INDEX);
                String mm = c.getString(PASSWORD_INDEX);
                String mm_customer = c.getString(PASSWORD_CUSTOMER_INDEX);
                int autoPark = c.getInt(AUTO_PARK_INDEX);
                LockBean lock = new LockBean(address, name, role, isDefault,realName);
                if(!mm.equals(""))
                    lock.setPassword(mm);
                if(!mm_customer.equals(""))
                    lock.setCustomerPassword(mm_customer);
                lock.setAutoPark(autoPark);
                lockList.add(lock);
            }
            c.close();
            return lockList;
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null)
                c.close();
        }
        return null;
    }

    public void insertIntoUntrusttedLocks(String address) {
        SQLiteDatabase db = getDatabase(true);
        deleteFromUntrusttedLocks(address);
        ContentValues cv = new ContentValues();
        cv.put(FIELD_LOCK_ADDRESS, address);
        cv.put(FIELD_LAST_LOGIN_TIME,String.valueOf(System.currentTimeMillis()));
        db.insert("untrustedLocks", null, cv);// 执行插入操作
    }

    public void deleteFromUntrusttedLocks(String address) {
        SQLiteDatabase db = getDatabase(true);
        String sql = "delete from untrustedLocks where address=" + "'" + address + "'";
        db.execSQL(sql);
    }

    public int findInUntrusttedLocks(String mAddress) {
        Cursor c = null;
        try {
            SQLiteDatabase db = getDatabase(false);
            c = db.rawQuery("select * from untrustedLocks where address=?",
                    new String[] {mAddress});
            if (c != null && c.getCount() > 0 && c.moveToNext()) {
                long timeToNow = System.currentTimeMillis() - Long.parseLong(c.getString(LOGIN_TIME_INDEX));
                if(timeToNow > 12 * 60 * 60 *1000){
                    deleteFromUntrusttedLocks(mAddress);
                    return -1;
                }
                int timeToHour = (int) (12 - timeToNow /1000/60/60);
                return timeToHour;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null)
                c.close();
        }
        return -1;
    }

}
