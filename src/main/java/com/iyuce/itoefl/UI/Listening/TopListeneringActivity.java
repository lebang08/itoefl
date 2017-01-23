package com.iyuce.itoefl.UI.Listening;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.iyuce.itoefl.R;
import com.iyuce.itoefl.UI.Main.FragmentLecture;

import java.util.ArrayList;

public class TopListeneringActivity extends AppCompatActivity {

    private TabLayout mTab;
    private ArrayList<String> mTabList = new ArrayList<>();

    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private ArrayList<Fragment> mFragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_listenering);

        initView();
    }

    private void initView() {
        //back button in <include header>
        findViewById(R.id.imgbtn_header_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.viewpager_activity_top_listenering);
        mTab = (TabLayout) findViewById(R.id.tab_activity_top_listenering);
        mTabList.add("顺序");
        mTabList.add("分类");
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTabList.get(position);
            }
        };
        mFragmentList.add(new FragmentOrder());
        mFragmentList.add(new FragmentLecture());
        mViewPager.setAdapter(mAdapter);
        mTab.setTabMode(TabLayout.MODE_FIXED);
        mTab.setupWithViewPager(mViewPager);
    }
}