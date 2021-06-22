package com.minorfish.clinicwaste.module.bagin;

/**
 * Created by tangjd on 2016/8/11.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.minorfish.clinicwaste.BaseActivity;
import com.minorfish.clinicwaste.R;
import com.minorfish.clinicwaste.abs.Constants;
import com.minorfish.clinicwaste.usb.PrinterHelperSerial;
import com.tangjd.common.utils.StringUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ActScanIn extends BaseActivity {
    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.toolbar_left_menu)
    TextView toolbarLeftMenu;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.et_scan)
    EditText etScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_scan_layout);
        ButterKnife.bind(this);
        setToolbarTitle("垃圾袋入库");
        enableBackFinish();

        etScan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if (!StringUtil.isEmpty(content) && content.endsWith("\n")) {
                    String code = "";
                    PrinterHelperSerial.getInstance(ActScanIn.this).qrcode = content;
                    content = content.substring(0, content.length() - 1);
                    code = content.substring(content.lastIndexOf("=")+1);
                    ActCheckBag.start(ActScanIn.this, code, Constants.REQUEST_CODE_ADD_BAG);
                    etScan.setText("");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
        }catch (Exception e){}
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return disableKeycode(keyCode, event);
    }

    private boolean disableKeycode(int keyCode, KeyEvent event) {
        int key = event.getKeyCode();
        switch (key) {
            case KeyEvent.KEYCODE_TAB:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_ADD_BAG && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ActScanIn.class);
        context.startActivity(intent);
    }

    public static void start(BaseActivity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, ActScanIn.class), requestCode);
    }
}