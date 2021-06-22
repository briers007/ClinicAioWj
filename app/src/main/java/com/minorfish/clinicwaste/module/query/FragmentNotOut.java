package com.minorfish.clinicwaste.module.query;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.minorfish.clinicwaste.R;
import com.minorfish.clinicwaste.abs.Api;
import com.minorfish.clinicwaste.abs.App;
import com.minorfish.clinicwaste.abs.Result;
import com.minorfish.clinicwaste.module.boxout.BagBean;
import com.minorfish.clinicwaste.module.boxout.BagItemBean;
import com.minorfish.clinicwaste.module.boxout.BoxBean;
import com.minorfish.clinicwaste.module.boxout.RvBags;
import com.tangjd.common.abs.JsonApiBase;
import com.tangjd.common.utils.DecimalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author: tangjd
 * Date: 2017/6/16
 */

public class FragmentNotOut extends Fragment {

    private static final String TAG = FragmentNotOut.class.getSimpleName();
    @Bind(R.id.tv_agency_name)
    TextView tvAgencyName;
    @Bind(R.id.tv_total)
    TextView tvTotal;
    @Bind(R.id.tv_sunshang)
    TextView tvSunshang;
    @Bind(R.id.tv_ganran)
    TextView tvGanran;
    @Bind(R.id.tv_duty)
    TextView tvDuty;
    @Bind(R.id.rv_bags)
    RvBags rvBags;
    @Bind(R.id.tv_bingli)
    TextView tvBingli;
    @Bind(R.id.tv_yaowu)
    TextView tvYaowu;
    @Bind(R.id.tv_huaxue)
    TextView tvHuaxue;
    @Bind(R.id.ll_ganran)
    LinearLayout llGanran;
    @Bind(R.id.ll_sunshang)
    LinearLayout llSunshang;
    @Bind(R.id.ll_bingli)
    LinearLayout llBingli;
    @Bind(R.id.ll_yaowu)
    LinearLayout llYaowu;
    @Bind(R.id.ll_huaxue)
    LinearLayout llHuaxue;
    @Bind(R.id.tv_suliaoping)
    TextView tvSuliaoping;
    @Bind(R.id.ll_suliaoping)
    LinearLayout llSuliaoping;
    @Bind(R.id.tv_boliping)
    TextView tvBoliping;
    @Bind(R.id.ll_boliping)
    LinearLayout llBoliping;

    private View mLayoutView;
    private ActQuery mActivity;


    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x001:
                    String resultStr = (String) msg.obj;
                    Log.i(TAG, "handleMessage: "+resultStr);
                    BoxBean boxBean = BoxBean.objectFromData(resultStr);
                    setBoxData(boxBean);
                    try {
                        JSONObject jsonObject = new JSONObject(resultStr);
                        JSONArray jsonArray = jsonObject.optJSONArray("garbages");
                        List<BagBean> bagItemBeen = BagBean.arrayBagBeanFromData(jsonArray.toString());
                        setBagListData(bagItemBeen);
                        mActivity.dismissProgressDialog();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 0x002:
                    mActivity.dismissProgressDialog();
                    String reserr = (String) msg.obj;
                    Log.i(TAG, "handleMessage: "+reserr);
                    Toast.makeText(mActivity, ""+reserr, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mLayoutView == null) {
            mLayoutView = inflater.inflate(R.layout.fragment_not_out_layout, container, false);
            mActivity = (ActQuery) getActivity();
            ButterKnife.bind(this, mLayoutView);
            getData();
        } else {
            ViewGroup parent = (ViewGroup) mLayoutView.getParent();
            if (parent != null) {
                parent.removeView(mLayoutView);
            }
        }
        return mLayoutView;
    }

    private void getData() {
        mActivity.showProgressDialog();
        Api.getBoxOutData(new JsonApiBase.OnJsonResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                Result result = Result.parse(response);
                String res = response.toString();
                Log.i(TAG, "onResponse: "+res);
                if (result.isSuccess()) {
                    String str = result.mData+"";
                    Message message = Message.obtain();
                    message.what = 0x001;
                    message.obj = str;
                    mHandler.sendMessage(message);
                } else {
                    String error = response.optString("message");

                    Message message = Message.obtain();
                    message.what = 0x002;
                    message.obj = error;
                    mHandler.sendMessage(message);
//                    onError(result.mMsg);
                }
            }

            @Override
            public void onError(String error) {
//                mActivity.showShortToast(error + "");

                Message message = Message.obtain();
                message.what = 0x002;
                message.obj = error;
                mHandler.sendMessage(message);
            }

            @Override
            public void onFinish(boolean withoutException) {
//                mActivity.dismissProgressDialog();
            }
        });
    }

    private void setBagListData(List<BagBean> bagItemBeen) {
        rvBags.setData(bagItemBeen);
    }

    private void setBoxData(BoxBean bean) {
        if (bean == null) {
            return;
        }
        tvAgencyName.setText(bean.instName + "");
        tvDuty.setText(App.getApp().getSignInBean().name + "");
        tvGanran.setText(bean.type1Num + "袋/" + DecimalUtil.simpleFormat(bean.type1Weight) + "Kg");
        tvSunshang.setText(bean.type2Num + "袋/" + DecimalUtil.simpleFormat(bean.type2Weight) + "Kg");
        tvBingli.setText(bean.type3Num + "袋/" + DecimalUtil.simpleFormat(bean.type3Weight) + "Kg");
        tvYaowu.setText(bean.type4Num + "袋/" + DecimalUtil.simpleFormat(bean.type4Weight) + "Kg");
        tvHuaxue.setText(bean.type5Num + "袋/" + DecimalUtil.simpleFormat(bean.type5Weight) + "Kg");
        tvSuliaoping.setText(bean.type6Num + "袋/" + DecimalUtil.simpleFormat(bean.type6Weight) + "Kg");
        tvBoliping.setText(bean.type7Num + "袋/" + DecimalUtil.simpleFormat(bean.type7Weight) + "Kg");
        tvTotal.setText(bean.totalNum + "袋/" + DecimalUtil.simpleFormat(bean.totalWeight) + "Kg");

        llGanran.setVisibility(bean.type1Show ? View.VISIBLE : View.GONE);
        llSunshang.setVisibility(bean.type2Show ? View.VISIBLE : View.GONE);
        llBingli.setVisibility(bean.type3Show ? View.VISIBLE : View.GONE);
        llYaowu.setVisibility(bean.type4Show ? View.VISIBLE : View.GONE);
        llHuaxue.setVisibility(bean.type5Show ? View.VISIBLE : View.GONE);
        llSuliaoping.setVisibility(bean.type6Show ? View.VISIBLE : View.GONE);
        llBoliping.setVisibility(bean.type7Show ? View.VISIBLE : View.GONE);
    }
}
