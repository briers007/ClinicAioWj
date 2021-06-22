package com.minorfish.clinicwaste.module.boxout;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.minorfish.clinicwaste.BaseActivity;
import com.minorfish.clinicwaste.R;
import com.minorfish.clinicwaste.abs.Api;
import com.minorfish.clinicwaste.abs.App;
import com.minorfish.clinicwaste.abs.Result;
import com.minorfish.clinicwaste.usb.NfcHelper;
import com.minorfish.clinicwaste.usb.UsbService;
import com.minorfish.clinicwaste.util.OkHttpUtil;
import com.tangjd.common.abs.JsonApiBase;
import com.tangjd.common.utils.ByteUtil;
import com.tangjd.common.utils.DecimalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Author: tangjd
 * 垃圾箱出库
 */

public class ActBoxOut extends BaseActivity {
    private static final String TAG = ActBoxOut.class.getSimpleName();
    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.toolbar_left_menu)
    TextView toolbarLeftMenu;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

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
    @Bind(R.id.btn_confirm)
    TextView btnConfirm;
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

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x001:
                    dismissProgressDialog();
                    Toast.makeText(ActBoxOut.this, "出库成功", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case 0x002:
                    dismissProgressDialog();
                    String str = (String) msg.obj;
                    Toast.makeText(ActBoxOut.this, "出库失败"+str, Toast.LENGTH_SHORT).show();
                    break;
                case 0x003:
                    dismissProgressDialog();
                    Toast.makeText(ActBoxOut.this, "请求失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_box_out_layout);
        ButterKnife.bind(this);
        setToolbarTitle("垃圾箱出库");
        enableBackFinish();
        //showProgressDialog();
        getData();
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rvBags.mAdapter.getData() == null || rvBags.mAdapter.getData().size() == 0) {
                    showShortToast("没有可出库垃圾");
                    return;
                }
                showNfcDialog();
                getSanzeData();
                mCanProcessNfc = true;
                //startActForResult(ActBoxOutConfirm.class);
            }
        });

        try {
            NfcHelper.getInstance().setOnUsbReadCallback(new UsbService.OnUsbReadCallback() {
                @Override
                public void onUsbDataReceived(byte[] data) {
                    if (!mCanProcessNfc) {
                        return;
                    }
                    String tagId = ByteUtil.bytesToHexStringWithoutSpace(ByteUtil.subByteArr(data, 0, 4));
                    onNfcResultGet(tagId);
                }

                @Override
                public void onError(String error) {
                }
            });
        }catch (Exception e){}
    }

    private void onNfcResultGet(final String tagId) {
        toBoxOutData(tagId);
    }

    private void toBoxOutData(String tagId) {
        showProgressDialog();
        String url = Api.getDomainName() + "/hw/clinic/pda/bag/out.do";
        String token = App.getApp().getSignInBean().token;

        Log.i(TAG, "run: "+url+"---"+token);
        JSONObject params = new JSONObject();
        try {
            params.put("nfcCode", tagId);
            params.put("typeCodes", checkedTypeCodes);

            String jsonStr = params.toString();
            Log.i(TAG, "run: "+jsonStr);

            OkHttpUtil.getInstance().postJsonAsyn(url, jsonStr, token, new OkHttpUtil.NetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    String res = response.body().string();
                    Log.i(TAG, "success: "+res);
                    try {
                        JSONObject jsonObject = new JSONObject(res);
                        int mCode = jsonObject.optInt("code");
                        if(mCode==200){
                            handler.sendEmptyMessage(0x001);
                        }else{
                            String faild = jsonObject.optString("message");
                            Message message = Message.obtain();
                            message.what = 0x002;
                            message.obj = faild;
                            mHandler.sendMessage(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Call call, IOException e) {
                    handler.sendEmptyMessage(0x003);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setBagListData(List<BagBean> list) {
        rvBags.setData(list);
    }

    private void setBoxData(BoxBean bean) {
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

    private Dialog mDialog;

    private void showNfcDialog() {
        if (mDialog != null) {
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
            return;
        }
        mDialog = new Dialog(this, R.style.CustomDialogTheme);
        View contentView = LayoutInflater.from(ActBoxOut.this).inflate(R.layout.nfc_dialog_layout, null, false);
        mDialog.setContentView(contentView);
        contentView.findViewById(R.id.iv_nfc_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NfcHelper.getInstance().getTagId();
            }
        });

        mDialog.setCancelable(true);

        Point screenOutSize = new Point();

        getWindowManager().getDefaultDisplay().getSize(screenOutSize);

        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
        layoutParams.width = (int) (screenOutSize.x * 0.6);
        layoutParams.height = (int) (screenOutSize.y * 0.4);
        dialogWindow.setAttributes(layoutParams);

        if (!isFinishing()) {
            mDialog.show();
        }
    }

    private void getSanzeData() {
        mHandler.removeCallbacks(getDataRunnable);
        mHandler.postDelayed(getDataRunnable, 600);
    }

    private Handler mHandler = new Handler();
    private Runnable getDataRunnable = new Runnable() {
        @Override
        public void run() {
            NfcHelper.getInstance().getTagId();
            mHandler.postDelayed(this,  600);
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler!=null) {
            mHandler.removeCallbacks(getDataRunnable);
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private boolean mCanProcessNfc = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_COMMON && resultCode == RESULT_OK) {
            finish();
        }
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, ActBoxOut.class));
    }


    JSONArray checkedTypeCodes = new JSONArray();
    private List<WasteTypeBean> typeList = new ArrayList<>();
    private void getData() {
        Api.getWasteType(new JsonApiBase.OnJsonResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                Result result = Result.parse(response);
                if (result.isSuccess()) {
                    typeList = WasteTypeBean.parse(result.mData);
                    if(typeList!=null) {
                        final Boolean[] checkedItems = new Boolean[typeList.size()];
                        showMultiChoiceDialog(false, typeList, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < typeList.size(); i++) {
                                    if (checkedItems[i] != null && checkedItems[i]) {
                                        checkedTypeCodes.put(typeList.get(i).mCode);
                                    }
                                }
                                multiChoiceDone();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < typeList.size(); i++) {
                                    checkedTypeCodes.put(typeList.get(i).mCode);
                                }
                                multiChoiceDone();
                            }
                        }, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                checkedItems[which] = isChecked;
                            }
                        });
                    }
                } else {
                    onError(result.mMsg + " " + result.mCode);
                }
            }

            @Override
            public void onError(String error) {
            }

            @Override
            public void onFinish(boolean withoutException) {
            }
        });
    }

    private void multiChoiceDone(){
        if (checkedTypeCodes.length() == 0) {
            showLongToast("请选择要出库的类型");
            finish();
            return;
        }

        //获取选取的类型对应的数据
        Api.getBoxOutDataOld(checkedTypeCodes, new JsonApiBase.OnJsonResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                Result result = Result.parse(response);
                if (result.isSuccess()) {
                    BoxBean boxBean = BoxBean.objectFromData(result.mData + "");
                    setBoxData(boxBean);
                    List<BagBean> bagItemBeen = BagBean.arrayBagBeanFromData(((JSONObject) result.mData).optJSONArray("garbages").toString());
                    setBagListData(bagItemBeen);
                } else {
                    onError(result.mMsg);
                }
            }

            @Override
            public void onError(String error) {
                showShortToast(error + "");
                finish();
            }

            @Override
            public void onFinish(boolean withoutException) {
                dismissProgressDialog();
            }
        });
    }

}
