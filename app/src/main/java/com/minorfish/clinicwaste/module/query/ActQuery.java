package com.minorfish.clinicwaste.module.query;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.minorfish.clinicwaste.BaseActivity;
import com.minorfish.clinicwaste.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author: tangjd
 * Date: 2017/6/14
 */

public class ActQuery extends BaseActivity {
    @Bind(R.id.tab_strip)
    NavigationTabStrip tabStrip;
    @Bind(R.id.view_pager)
    ViewPager viewPager;
    private final String[] mFragmentArr = new String[]{FragmentNotOut.class.getName(), FragmentOuted.class.getName()};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_query_layout);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        setToolbarTitle("出入库查询");
        enableBackFinish();
        tabStrip.setTitles("未出库", "已出库");
        tabStrip.setTitleSize(20);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            private SparseArrayCompat<Fragment> mFragments = new SparseArrayCompat<>();

            @Override
            public Fragment getItem(int position) {
                Fragment fragment = mFragments.get(position);
                if (fragment == null) {
                    fragment = Fragment.instantiate(ActQuery.this, mFragmentArr[position]);
                    mFragments.put(position, fragment);
                }
                return fragment;
            }

            @Override
            public int getCount() {
                return mFragmentArr.length;
            }
        });
        tabStrip.setViewPager(viewPager);
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, ActQuery.class));
    }
}
