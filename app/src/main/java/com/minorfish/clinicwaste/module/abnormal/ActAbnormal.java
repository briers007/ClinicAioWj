package com.minorfish.clinicwaste.module.abnormal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.minorfish.clinicwaste.BaseActivity;
import com.minorfish.clinicwaste.R;
import com.minorfish.clinicwaste.abs.Api;
import com.minorfish.clinicwaste.abs.Result;
import com.minorfish.clinicwaste.module.boxout.BagBean;
import com.minorfish.clinicwaste.module.boxout.BagItemBean;
import com.minorfish.clinicwaste.module.boxout.RvBags;
import com.tangjd.common.abs.JsonApiBase;
import com.tangjd.common.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author: tangjd
 * Date: 2017/6/14
 */

public class ActAbnormal extends BaseActivity implements BaseQuickAdapter.RequestLoadMoreListener {
    @Bind(R.id.rv_bags)
    RvBagsExp rvBags;
    private int mCurrPage = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_abnormal_layout);
        ButterKnife.bind(this);
        setToolbarTitle("异常记录");
        enableBackFinish();
        getData();
    }

    private void getData() {
        rvBags.showLoadingView();
        mCurrPage = 1;
        Api.getAbnormalData(mCurrPage, new JsonApiBase.OnJsonResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                Result result = Result.parse(response);
                if (result.isSuccess()) {
                    boolean hasMore = ((JSONObject) result.mData).optBoolean("hasNextPage");
                    rvBags.onGetDataSuccess(hasMore, BagItemBean.arrayBagItemBeanFromData(((JSONObject) result.mData).optJSONArray("list").toString()), ActAbnormal.this);
                } else {
                    onError(result.mMsg);
                }
            }

            @Override
            public void onError(String error) {
                rvBags.onGetDataFail(error, true);
            }

            @Override
            public void onFinish(boolean withoutException) {

            }
        });
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, ActAbnormal.class));
    }

    @Override
    public void onLoadMoreRequested() {
        Api.getAbnormalData(++mCurrPage, new JsonApiBase.OnJsonResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                Result result = Result.parse(response);
                if (result.isSuccess()) {
                    boolean hasMore = ((JSONObject) result.mData).optBoolean("hasNextPage");
                    rvBags.onLoadMoreSuccess(hasMore, BagItemBean.arrayBagItemBeanFromData(((JSONObject) result.mData).optJSONArray("list").toString()));
                } else {
                    onError(result.mMsg);
                }
            }

            @Override
            public void onError(String error) {
                rvBags.onLoadMoreFail();
            }

            @Override
            public void onFinish(boolean withoutException) {

            }
        });
    }
}
