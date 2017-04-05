package com.iyuce.itoefl.Control.Listening.Activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iyuce.itoefl.BaseActivity;
import com.iyuce.itoefl.Common.Constants;
import com.iyuce.itoefl.Control.Listening.Adapter.TopListeneringPageAdapter;
import com.iyuce.itoefl.Model.UserOprate;
import com.iyuce.itoefl.R;
import com.iyuce.itoefl.Utils.DbUtil;
import com.iyuce.itoefl.Utils.HttpUtil;
import com.iyuce.itoefl.Utils.Interface.Http.DownLoadInterface;
import com.iyuce.itoefl.Utils.LogUtil;
import com.iyuce.itoefl.Utils.NetUtil;
import com.iyuce.itoefl.Utils.SDCardUtil;
import com.iyuce.itoefl.Utils.ToastUtil;
import com.iyuce.itoefl.Utils.ZipUtil;

import java.io.File;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Response;

public class TopListeneringPageActivity extends BaseActivity implements TopListeneringPageAdapter.OnPageItemClickListener {

    private RecyclerView mRecyclerView;
    private TopListeneringPageAdapter mAdapter;
    //列表对象，包含两个库中查询的数据
    private ArrayList<String> mModuleList = new ArrayList<>();
    private ArrayList<String> mPaperCodeList = new ArrayList<>();
    private ArrayList<String> mDownTimeList = new ArrayList<>();
    private ArrayList<String> mUrlList = new ArrayList<>();
    private ArrayList<String> mMusicQuestionList = new ArrayList<>();
    private ArrayList<String> mLoadingList = new ArrayList<>();
    private ArrayList<String> mDownloadList = new ArrayList<>();
    private ArrayList<String> mPracticedList = new ArrayList<>();
    private ArrayList<UserOprate> mUserOprateList = new ArrayList<>();

    private TextView mTxtFinish, mTxtTotal;

    //传递来的参数
    private String local_code, local_title, from_where, local_practiced_count;

    //保存根数据库的路径
    private String root_path, local_path;

    //自创的SQL库，路径
    private String downloaded_sql_path;

    @Override
    protected void onRestart() {
        super.onRestart();
        mUserOprateList.clear();
        mLoadingList.clear();
        mDownloadList.clear();
        mDownTimeList.clear();
        mPracticedList.clear();
        mModuleList.clear();
        mPaperCodeList.clear();
        mMusicQuestionList.clear();
        mUrlList.clear();
        initData();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_listenering_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.icon_back);
        }

        initView();
    }

    private void initView() {
        local_code = getIntent().getStringExtra("local_code");
        local_title = getIntent().getStringExtra("local_title");
        from_where = getIntent().getStringExtra("from_where");
        TextView textView = (TextView) findViewById(R.id.txt_activity_top_listenering_title);
        textView.setText(local_title);

        mTxtFinish = (TextView) findViewById(R.id.txt_activity_top_listenering_finish);
        mTxtTotal = (TextView) findViewById(R.id.txt_activity_top_listenering_total);

        //初始化数据列表
        initData();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_activity_top_listenering_page);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TopListeneringPageAdapter(this, mUserOprateList);
        mAdapter.setOnPageItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mTxtFinish.setText(getString(R.string.page_practiced, local_practiced_count));
        mTxtTotal.setText(getString(R.string.page_total, mModuleList.size()));

        //网络已连接
        if (NetUtil.isConnected(this)) {
            //但不是WIFI
            if (!NetUtil.isWifi(this)) {
                ToastUtil.showMessage(this, "当前不在WIFI环境，下载将消耗较多流量");
            }
        } else {
            ToastUtil.showMessage(this, "未连接网络");
        }
    }

    private void initData() {
        //初始化列表数据,从主表中获取到的local_code读取PAPER_RULE表中的RuleName字段
        root_path = SDCardUtil.getExercisePath() + File.separator + Constants.SQLITE_TPO;
        SQLiteDatabase mDatabase = DbUtil.getHelper(this, root_path).getWritableDatabase();
        //TODO 其实可以封装成一个对象数组返回
        if (TextUtils.equals(from_where, Constants.MODULE)) {
            mPaperCodeList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_PAPER_RULE, Constants.PaperCode, Constants.PaperCode + " =? ", local_code);
            mModuleList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_PAPER_RULE, Constants.RuleName, Constants.PaperCode + " =? ", local_code);
            mDownTimeList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_PAPER_RULE, Constants.DownTime, Constants.PaperCode + " =? ", local_code);
            mUrlList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_PAPER_RULE, Constants.DownUrl, Constants.PaperCode + " =? ", local_code);
            mMusicQuestionList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_PAPER_RULE, Constants.MusicQuestion, Constants.PaperCode + " =? ", local_code);
        } else {
            Cursor cursor = mDatabase.query(Constants.TABLE_PAPER_RULE, null, Constants.ClassCode + " =? ", new String[]{local_code}, null, null, Constants.PaperCode + "," + Constants.Sort + " ASC");
            //开启事务批量操作
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    mPaperCodeList.add(cursor.getString(cursor.getColumnIndex(Constants.PaperCode)));
                    mModuleList.add(cursor.getString(cursor.getColumnIndex(Constants.RuleName)));
                    mDownTimeList.add(cursor.getString(cursor.getColumnIndex(Constants.DownTime)));
                    mUrlList.add(cursor.getString(cursor.getColumnIndex(Constants.DownUrl)));
                    mMusicQuestionList.add(cursor.getString(cursor.getColumnIndex(Constants.MusicQuestion)));
                }
                cursor.close();
            }
        }
        mDatabase.close();
        LogUtil.e("mDownTimeList = " + mDownTimeList);
        LogUtil.e("mUrlList = " + mUrlList);

        //初始化用户操作数据库(打开或创建)
        CreateOrOpenDbTable();

        //从两个数据库中查询完成数据拼装，Adapter装载数据
        UserOprate mUserOprate;
        for (int i = 0; i < mModuleList.size(); i++) {
            mUserOprate = new UserOprate();
            mUserOprate.section = mPaperCodeList.get(i);
            mUserOprate.module = mModuleList.get(i);
            mUserOprate.download = mDownloadList.get(i);
            mUserOprate.loading = mLoadingList.get(i);
            mUserOprate.practiced = mPracticedList.get(i);
            mUserOprateList.add(mUserOprate);
        }
        LogUtil.i(mUserOprateList.toString());
        mTxtFinish.setText("已练习 ：" + local_practiced_count + " 篇");
    }

    /**
     * 打开或者新建一个我创建的本地数据库，查询是否有表,无表则新建一张DownLoad清单表
     */
    private void CreateOrOpenDbTable() {
        //打开或者创建我的本地数据库DOWNLOAD
        downloaded_sql_path = SDCardUtil.getExercisePath() + File.separator + Constants.SQLITE_DOWNLOAD;
        SQLiteDatabase mDatabase = DbUtil.getHelper(TopListeneringPageActivity.this, downloaded_sql_path).getWritableDatabase();
        String create = "create table if not exists " + Constants.TABLE_ALREADY_DOWNLOAD + "("
                + Constants.ID + " integer primary key autoincrement,"
                + Constants.UserId + " text,"
                + Constants.SECTION + " text,"
                + Constants.MODULE + " text,"
                + Constants.LOADING + " text,"
                + Constants.DOWNLOAD + " text,"
                + Constants.DownTime + " text,"
                + Constants.Practiced + " text)";
        mDatabase.execSQL(create);
        String query;
        for (int i = 0; i < mModuleList.size(); i++) {
            query = "select " + Constants.DOWNLOAD + " from " + Constants.TABLE_ALREADY_DOWNLOAD + " where " + Constants.SECTION + " = ? and " + Constants.MODULE + " = ?";
            mDownloadList.add(DbUtil.cursorToNotNullString(mDatabase.rawQuery(query, new String[]{mPaperCodeList.get(i), mModuleList.get(i)})));
            query = "select " + Constants.LOADING + " from " + Constants.TABLE_ALREADY_DOWNLOAD + " where " + Constants.SECTION + " = ? and " + Constants.MODULE + " = ?";
            mLoadingList.add(DbUtil.cursorToNotNullString(mDatabase.rawQuery(query, new String[]{mPaperCodeList.get(i), mModuleList.get(i)})));
            query = "select " + Constants.Practiced + " from " + Constants.TABLE_ALREADY_DOWNLOAD + " where " + Constants.SECTION + " = ? and " + Constants.MODULE + " = ?";
            mPracticedList.add(DbUtil.cursorToNotNullString(mDatabase.rawQuery(query, new String[]{mPaperCodeList.get(i), mModuleList.get(i)})));
        }
        //TODO 这里的查询好像有问题
        String sql_practiced_count = "SELECT COUNT(*) FROM " + Constants.TABLE_ALREADY_DOWNLOAD + " WHERE " + Constants.SECTION + " =? and " + Constants.Practiced + " =?";
        local_practiced_count = DbUtil.cursorToString(mDatabase.rawQuery(sql_practiced_count, new String[]{local_code, Constants.TRUE}));
        mDatabase.close();
    }

    /**
     * 给定几个参数，position,Url,path
     */
    private void doDownLoad(final int pos, String url, final String path) {
        HttpUtil.downLoad(url, path, new DownLoadInterface() {
            @Override
            public void onBefore() {
                mUserOprateList.get(pos).loading = Constants.TRUE;
                mRecyclerView.getChildAt(pos).setClickable(false);

                ImageView imgDownload = (ImageView) mRecyclerView.getChildAt(pos).findViewById(R.id.img_recycler_item_top_listenering_page_download);
                ImageView imgReady = (ImageView) mRecyclerView.getChildAt(pos).findViewById(R.id.img_recycler_item_top_listenering_page_download_ready);
                imgDownload.setVisibility(View.VISIBLE);
                imgReady.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAfter() {
                mRecyclerView.getChildAt(pos).setClickable(true);
            }

            @Override
            public void inProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
                TextView textView = (TextView) mRecyclerView.getChildAt(pos).findViewById(R.id.txt_recycler_item_top_listenering_page_download);
                textView.setVisibility(View.VISIBLE);
                textView.setText(((int) (progress * 100) + "%"));
            }

            @Override
            public void doSuccess(File file, Call call, Response response) {
                mUserOprateList.get(pos).loading = Constants.FALSE;
                mUserOprateList.get(pos).download = Constants.TRUE;
                SQLiteDatabase mDatabase = DbUtil.getHelper(TopListeneringPageActivity.this, downloaded_sql_path).getWritableDatabase();
                //先删除，再添加，避免重复
                String sql_delete = "DELETE FROM " + Constants.TABLE_ALREADY_DOWNLOAD + " WHERE " + Constants.SECTION
                        + " = \"" + mPaperCodeList.get(pos) + "\" AND " + Constants.MODULE + " = \"" + mModuleList.get(pos) + "\";";
                LogUtil.e("sql_delete = " + sql_delete);
                mDatabase.execSQL(sql_delete);
                ContentValues mValues = new ContentValues();
                mValues.put(Constants.UserId, "user_default");
                mValues.put(Constants.SECTION, mPaperCodeList.get(pos));
                mValues.put(Constants.MODULE, mModuleList.get(pos));
                mValues.put(Constants.DOWNLOAD, Constants.TRUE);
                mValues.put(Constants.LOADING, Constants.FALSE);
                mValues.put(Constants.DownTime, mDownTimeList.get(pos));
                DbUtil.insert(mDatabase, Constants.TABLE_ALREADY_DOWNLOAD, mValues);
                mDatabase.close();

                //解压文件夹
                unZipFile(file, path);

                //下载箭头、文字设为不可见
                ImageView imgDownload = (ImageView) mRecyclerView.getChildAt(pos).findViewById(R.id.img_recycler_item_top_listenering_page_download);
                TextView textDownload = (TextView) mRecyclerView.getChildAt(pos).findViewById(R.id.txt_recycler_item_top_listenering_page_download);
                TextView textState = (TextView) mRecyclerView.getChildAt(pos).findViewById(R.id.txt_recycler_item_top_listenering_page_state);
                textDownload.setVisibility(View.INVISIBLE);
//                textDownload.setText("未练习");
                imgDownload.setVisibility(View.INVISIBLE);
                textState.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * UnZip解压文件夹
     */
    private void unZipFile(File file, String path) {
        try {
            //解压zip文件到对应路径
            ZipUtil.UnZipFolder(file.getAbsolutePath(), path);
            //删除文件夹,有bool值返回，可以根据做操作
            LogUtil.i("zip delete = " + file.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnPageItemClick(final int pos) {
        //这个路径用来存放下载的文件，或者传递给下一级
        local_path = SDCardUtil.getExercisePath() + File.separator + mPaperCodeList.get(pos) + File.separator + mModuleList.get(pos);

        //查询download库中的下载表,判断是否下载到本地了，是则进入，否则下载
        SQLiteDatabase mDatabase = DbUtil.getHelper(TopListeneringPageActivity.this, downloaded_sql_path).getWritableDatabase();
        String sql_query = "select " + Constants.ID + " from " + Constants.TABLE_ALREADY_DOWNLOAD
                + " where " + Constants.SECTION + " = ? and " + Constants.MODULE + " = ? ";
        String isExist = DbUtil.cursorToString(mDatabase.rawQuery(sql_query, new String[]{mPaperCodeList.get(pos), mModuleList.get(pos)}));
        mDatabase.close();
        LogUtil.i(mPaperCodeList.get(pos) + "_" + mModuleList.get(pos) + " isExist = " + isExist);
        if (isExist.equals(Constants.NONE)) {
            doDownLoad(pos, mUrlList.get(pos), local_path);
            return;
        }

        //如果新表修改了，则提示用户是否下载新表
        String path = SDCardUtil.getExercisePath();
        String filePath = path + File.separator + Constants.SQLITE_DOWNLOAD;
        SQLiteDatabase database = DbUtil.getHelper(TopListeneringPageActivity.this, filePath).getWritableDatabase();
        String sql_down_time_last = "select " + Constants.DownTime + " from " + Constants.TABLE_ALREADY_DOWNLOAD
                + " where " + Constants.SECTION + " = ? and " + Constants.MODULE + " = ? ";
        String down_time_last = DbUtil.cursorToString(database.rawQuery(sql_down_time_last, new String[]{mPaperCodeList.get(pos), mModuleList.get(pos)}));
        database.close();
        LogUtil.e("down_time_last = " + down_time_last);
        if (!down_time_last.equals(mDownTimeList.get(pos))) {
            new AlertDialog.Builder(this).setMessage("有更新的题库，建议下载新题库")
                    .setPositiveButton("使用原题库", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(TopListeneringPageActivity.this, PageReadyActivity.class);
                            intent.putExtra("local_path", local_path);
                            //留给子数据库拼装末位路径
                            intent.putExtra("local_code", mPaperCodeList.get(pos));
                            intent.putExtra("local_module", mModuleList.get(pos));
                            intent.putExtra("local_music_question", mMusicQuestionList.get(pos));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("下载新题库", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //下载新题
                            doDownLoad(pos, mUrlList.get(pos), local_path);
                        }
                    }).show();
        } else {
            Intent intent = new Intent(TopListeneringPageActivity.this, PageReadyActivity.class);
            intent.putExtra("local_path", local_path);
            //留给子数据库拼装末位路径
            intent.putExtra("local_code", mPaperCodeList.get(pos));
            intent.putExtra("local_module", mModuleList.get(pos));
            intent.putExtra("local_music_question", mMusicQuestionList.get(pos));
            startActivity(intent);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.listtening_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.download:
                ToastUtil.showMessage(this, "begin download all");
                String path;
                for (int i = 0; i < mModuleList.size(); i++) {
                    //查对象属性,loading或者downloaded，则不下载
                    if (mUserOprateList.get(i).loading.equals(Constants.TRUE) || mUserOprateList.get(i).download.equals(Constants.TRUE)) {
                        continue;
                    }
                    path = SDCardUtil.getExercisePath() + File.separator + mPaperCodeList.get(i) + File.separator + mModuleList.get(i);
                    doDownLoad(i, mUrlList.get(i), path);
                }
                break;
            default:
                break;
        }
        return true;
    }
}