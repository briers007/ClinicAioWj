package com.minorfish.clinicwaste.module.bagin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.minorfish.clinicwaste.BaseActivity;
import com.minorfish.clinicwaste.R;
import com.minorfish.clinicwaste.abs.Api;
import com.minorfish.clinicwaste.abs.Constants;
import com.minorfish.clinicwaste.abs.Result;
import com.minorfish.clinicwaste.module.BagInBean;
import com.minorfish.clinicwaste.module.frame.ActFrame;
import com.tangjd.common.abs.JsonApiBase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 垃圾入库
 */

public class ActBagInList extends BaseActivity {
    @Bind(R.id.rv_bag_in)
    RvBagIn rvBagIn;
    @Bind(R.id.btn_add_bag)
    ImageView btnAddBag;
    @Bind(R.id.btn_commit)
    TextView btnCommit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_bag_in_list_layout);
        ButterKnife.bind(this);
        setToolbarTitle("垃圾入库");
        enableBackFinish();
        btnAddBag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActScanIn.start(ActBagInList.this, Constants.REQUEST_CODE_ADD_BAG);
            }
        });
        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<BagInBean> beans = rvBagIn.mAdapter.getData();
                if (beans == null || beans.size() == 0) {
                    showTipDialog("没有可上传的垃圾袋");
                    return;
                }
                showProgressDialog();
                Api.bagIn(rvBagIn.mAdapter.getData(), new JsonApiBase.OnJsonResponseListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Result result = Result.parse(response);
                        if (result.isSuccess()) {
                            showLongToast("入库成功");
                            ActFrame.startActivity(ActBagInList.this);
                        } else {
                            onError(result.mMsg);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        showTipDialog(error + "");
                    }

                    @Override
                    public void onFinish(boolean withoutException) {
                        dismissProgressDialog();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_ADD_BAG && resultCode == RESULT_OK) {
            BagInBean bean = (BagInBean) data.getSerializableExtra(Constants.EXTRA_BAG_IN_BEAN);
            rvBagIn.addData(bean);
        }
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, ActBagInList.class));
    }
}
