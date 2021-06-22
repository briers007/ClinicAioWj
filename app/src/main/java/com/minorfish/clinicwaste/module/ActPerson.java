package com.minorfish.clinicwaste.module;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.minorfish.clinicwaste.BaseActivity;
import com.minorfish.clinicwaste.R;
import com.minorfish.clinicwaste.abs.App;

/**
 * Created by tangjd on 2016/8/10.
 */
public class ActPerson extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_person_layout);
        initView();
    }

    private void initView() {
        setToolbarTitle("个人中心");
        enableBackFinish();
        if (App.getApp().mUserBean != null) {
            ((TextView) findViewById(R.id.agency_name)).setText(App.getApp().mUserBean.instName + "");
            String address = App.getApp().mUserBean.instAddress;
            if(TextUtils.isEmpty(address)){
                ((TextView) findViewById(R.id.agency_address)).setText("暂无数据");
            }else{
                ((TextView) findViewById(R.id.agency_address)).setText(address);
            }

            ((TextView) findViewById(R.id.operator_person)).setText(App.getApp().mUserBean.name + "");
        }
        findViewById(R.id.btn_modify_pwd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActModifyPwd.start(ActPerson.this);
            }
        });
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, ActPerson.class));
    }
}
