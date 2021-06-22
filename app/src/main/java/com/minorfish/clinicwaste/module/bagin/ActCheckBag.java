package com.minorfish.clinicwaste.module.bagin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.minorfish.clinicwaste.BaseActivity;
import com.minorfish.clinicwaste.R;
import com.minorfish.clinicwaste.abs.Api;
import com.minorfish.clinicwaste.abs.Constants;
import com.minorfish.clinicwaste.abs.Result;
import com.minorfish.clinicwaste.module.BagInBean;
import com.minorfish.clinicwaste.module.bagin.weight.ActCheckWeight;
import com.tangjd.common.abs.JsonApiBase;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author: tangjd
 * Date: 2017/6/9
 */

public class ActCheckBag extends BaseActivity {
    @Bind(R.id.tv_agency_name)
    TextView tvAgencyName;
    @Bind(R.id.tv_weight_time)
    TextView tvWeightTime;
    @Bind(R.id.tv_batch_num)
    TextView tvBatchNum;
    @Bind(R.id.tv_trah_type)
    TextView tvTrahType;
    @Bind(R.id.btn_no)
    TextView btnNo;
    @Bind(R.id.btn_yes)
    TextView btnYes;

    private BagInBean mBagInBean = new BagInBean();
    private CheckBagBean mCheckBagBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_check_bag_layout);
        ButterKnife.bind(this);
        setToolbarTitle("垃圾袋信息确认");
        enableBackFinish();
        //符合
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActCheckWeight.start(ActCheckBag.this, mBagInBean, Constants.REQUEST_CODE_ADD_BAG);
            }
        });
        //不符合
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActCheckBagFail.start(ActCheckBag.this, mBagInBean, mCheckBagBean, Constants.REQUEST_CODE_ADD_BAG);
            }
        });
        String scanResult = getIntent().getStringExtra(Constants.EXTRA_SCAN_RESULT);
        mBagInBean.bagCode = scanResult;
        getData(scanResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_ADD_BAG && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void getData(String scanResult) {
        showProgressDialog();
        Api.getBagInfoByScan(scanResult, new JsonApiBase.OnJsonResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                Result result = Result.parse(response);
                if (result.isSuccess()) {
                    CheckBagBean bean = CheckBagBean.objectFromData(result.mData + "");
                    if (bean == null) {
                        onError("数据出错");
                        return;
                    }
                    setData(bean);
                } else {
                    onError(result.mMsg);
                }
            }

            @Override
            public void onError(String error) {
                showShortToast(error);
                finish();
            }

            @Override
            public void onFinish(boolean withoutException) {
                dismissProgressDialog();
            }
        });
    }

    private void setData(CheckBagBean bean) {
        mCheckBagBean = bean;
        mBagInBean.clinicName = bean.clinicName;
        mBagInBean.wasteType = bean.type;
        tvAgencyName.setText(bean.clinicName + "");
        tvBatchNum.setText(bean.batchNo);
        tvTrahType.setText(bean.type + "");
        tvWeightTime.setText(bean.outTime);
    }

    public static void start(ActScanIn activity, String scanResult, int requestCode) {
        Intent intent = new Intent(activity, ActCheckBag.class);
        intent.putExtra(Constants.EXTRA_SCAN_RESULT, scanResult);
        activity.startActivityForResult(intent, requestCode);
    }
}
