package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.persistence.DaoMaster;
import com.qhiehome.ihome.persistence.DaoSession;
import com.qhiehome.ihome.persistence.ParkingSQLHelper;
import com.qhiehome.ihome.persistence.SearchDao;
import com.qhiehome.ihome.persistence.SearchDaoDao;
import com.qhiehome.ihome.view.Search_ListView;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MapSearchActivity extends BaseActivity {

//    @BindView(R.id.et_search)
//    EditText mEtSearch;
//    @BindView(R.id.iv_search)
//    ImageView mIvSearch;
    @BindView(R.id.tv_search_tip)
    TextView mTvSearchTip;
    @BindView(R.id.tv_search_clear)
    TextView mTvSearchClear;
    @BindView(R.id.lv_search)
    Search_ListView mLvSearch;
    @BindView(R.id.floating_search_view)
    FloatingSearchView mFloatingSearchView;

    private ParkingSQLHelper mSQLHelper;
    private SQLiteDatabase mDB;
    private Context mContext;
    private BaseAdapter mAdapter;
    private SuggestionSearch mSuggestionSearch = SuggestionSearch.newInstance();
    private SuggestionResult mSuggestionResult;
    private String mCity;
    private boolean isHistory;
    private Handler mHandler;
    private int mPosition;

    private DaoSession mDaoSession;
    private SearchDaoDao mSearchDao;
    private Query<SearchDao> mSearchQuery;

    private static final int BACK_MSG = 1;
    public static final boolean ENCRYPTED = true;

    private static class SearchHandler extends Handler {
        private final WeakReference<MapSearchActivity> mActivity;

        private SearchHandler(MapSearchActivity mapSearchActivity) {
            mActivity = new WeakReference<MapSearchActivity>(mapSearchActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MapSearchActivity mapSearchActivity = mActivity.get();
            if (mapSearchActivity != null) {
                switch (msg.what) {
                    case BACK_MSG:
                        mapSearchActivity.deliverData(mapSearchActivity.mPosition);
                        mapSearchActivity.mFloatingSearchView.setSearchFocused(false);
                        mapSearchActivity.finish();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);
        ButterKnife.bind(this);
        mContext = this;
        mHandler = new SearchHandler(this);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mCity = bundle.getString("city");

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "notes-db-encrypted" : "notes-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        mDaoSession = new DaoMaster(db).newSession();
        mSearchDao = mDaoSession.getSearchDaoDao();
        mSearchQuery = mSearchDao.queryBuilder().orderAsc(SearchDaoDao.Properties.Id).build();

        mSQLHelper = new ParkingSQLHelper(this);
        mFloatingSearchView.setSearchFocused(true);
        queryData("");
        init();
        OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                if (suggestionResult == null || suggestionResult.error == SuggestionResult.ERRORNO.RESULT_NOT_FOUND) {
                    return;
                }
                if (suggestionResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    mSuggestionResult = suggestionResult;
                    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                    for (int i = 0; i < suggestionResult.getAllSuggestions().size(); i++) {
                        Map<String, String> item = new HashMap<String, String>();
                        item.put("result", suggestionResult.getAllSuggestions().get(i).key);
                        list.add(item);
                    }
                    mAdapter = new SimpleAdapter(mContext, list, android.R.layout.simple_list_item_1, new String[]{"result"}, new int[]{android.R.id.text1});
                    mLvSearch.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    if (mPosition >= 0) {
                        mHandler.sendEmptyMessage(BACK_MSG);
                    }
                }
            }
        };
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);
    }

    private void init() {
        mFloatingSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {

                if (newQuery.length() == 0) {
                    mTvSearchTip.setText("搜索历史");
                    isHistory = true;
                } else {
                    mTvSearchTip.setText("搜索结果");
                    isHistory = false;
                    mPosition = -1;
                    suggestionSearch(newQuery);
                }
            }
        });
        mFloatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String currentQuery) {
                if (TextUtils.isEmpty(currentQuery)){
                    return;
                }
                boolean hasData = hasData(mFloatingSearchView.getQuery());
                if (!hasData) {
                    insertData(mFloatingSearchView.getQuery());
                    queryData("");
                }
                mPosition = 0;
                suggestionSearch(mFloatingSearchView.getQuery());
            }
        });


        /*
        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //有内容则显示联想结果，无内容则显示历史记录
                String tempName = mEtSearch.getText().toString();
                if (s.toString().trim().length() == 0) {
                    mTvSearchTip.setText("搜索历史");
                    isHistory = true;
                } else {
                    mTvSearchTip.setText("搜索结果");
                    isHistory = false;
                    mPosition = -1;
                    suggestionSearch(mEtSearch.getText().toString().trim());
                }

            }
        });
        mEtSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    boolean hasData = hasData(mEtSearch.getText().toString().trim());
                    if (!hasData) {
                        insertData(mEtSearch.getText().toString().trim());
                        queryData("");
                    }
                    mPosition = 0;
                    suggestionSearch(mEtSearch.getText().toString().trim());
                }
                return false;
            }
        });*/
        /*********列表点击**********/
        mLvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                String name = textView.getText().toString();
                boolean tmp;
                if (isHistory == true) {
                    tmp = true;
                } else {
                    tmp = false;
                }
                mFloatingSearchView.setSearchText(name);
                //mEtSearch.setText(name);
                isHistory = tmp;
                if (!hasData(name)) {
                    insertData(name);
                }
                if (isHistory) {
                    mPosition = position;
                } else {
                    mPosition = 0;
                }
                //判断历史还是联想
                suggestionSearch(name);
            }
        });
        /*********按钮点击**********/
//        mIvSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!hasData(mEtSearch.getText().toString().trim())) {
//                    insertData(mEtSearch.getText().toString().trim());
//                }
//                mPosition = 0;
//                suggestionSearch(mEtSearch.getText().toString().trim());
//            }
//        });
    }

    @OnClick(R.id.tv_search_clear)
    public void onViewClicked() {
        //清空数据库
        deleteData();
        queryData("");
    }


    /*模糊查询数据 并显示在ListView列表上*/
    private void queryData(String tempName) {

        //模糊搜索
        Cursor cursor = mSQLHelper.getReadableDatabase().rawQuery(
                "select id as _id,name from history where name like '%" + tempName + "%' order by id desc ", null);
        // 创建adapter适配器对象,装入模糊搜索的结果
        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, new String[]{"name"},
                new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        // 设置适配器
        mLvSearch.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void deleteData() {
        mDB = mSQLHelper.getWritableDatabase();
        mDB.execSQL("delete from history");
        mDB.close();
    }

    private boolean hasData(String tempName) {
        //从Record这个表里找到name=tempName的id
        Cursor cursor = mSQLHelper.getReadableDatabase().rawQuery(
                "select id as _id,name from history where name =?", new String[]{tempName});
        //判断是否有下一个
        return cursor.moveToNext();
    }

    /*插入数据*/
    private void insertData(String tempName) {

        mDB = mSQLHelper.getWritableDatabase();
        mDB.execSQL("insert into history(name) values('" + tempName + "')");
        mDB.close();
    }

    private void suggestionSearch(String input) {
        mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                .keyword(input)
                .city(mCity));
    }

    private void deliverData(int position) {
        Bundle data = new Bundle();
        data.putString("name", mSuggestionResult.getAllSuggestions().get(position).key);
        try{
            data.putDouble("latitude", mSuggestionResult.getAllSuggestions().get(position).pt.latitude);
            data.putDouble("longitude", mSuggestionResult.getAllSuggestions().get(position).pt.longitude);
        }catch (Exception e){
            e.printStackTrace();
        }

        Intent backIntent = new Intent();
        backIntent.putExtras(data);
        setResult(RESULT_OK, backIntent);
    }


}
