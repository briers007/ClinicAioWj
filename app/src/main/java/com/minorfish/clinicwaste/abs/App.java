package com.minorfish.clinicwaste.abs;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.minorfish.clinicwaste.module.signin.SignInBean;
import com.tangjd.common.manager.SPManager;
import com.tangjd.common.manager.VolleyManager;
import com.tangjd.common.utils.Log;
import com.umeng.commonsdk.UMConfigure;

/**
 * Created by Administrator on 2016/7/2.
 */
public class App extends Application {
    private static App sApp;

    public Handler mBackHandler;
    public HandlerThread mHandlerThread;

    public SignInBean mUserBean;
    public String mToken;

    public synchronized static App getApp() {
        return sApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;

        Log.setLoggable(Configs.LOGABLE);
        SPManager.getInstance().init(this);
        VolleyManager.getInstance().init(this);

        mHandlerThread = new HandlerThread("AppBackHandlerThread");
        mHandlerThread.start();
        mBackHandler = new Handler(mHandlerThread.getLooper());

        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "5b96053b8f4a9d29ab000401");
    }

    public void signOut() {
        SPManager.getInstance().putString(Constants.PREF_KEY_SIGN_IN_BEAN, null);
        mUserBean = null;
        mToken = null;
    }

    public void setSignInBean(String data, SignInBean bean) {
        SPManager.getInstance().putString(Constants.PREF_KEY_SIGN_IN_BEAN, data);
        mUserBean = bean;
    }

    public SignInBean getSignInBean() {
        if (mUserBean == null) {
            String data = SPManager.getInstance().getString(Constants.PREF_KEY_SIGN_IN_BEAN, null);
            mUserBean = SignInBean.objectFromData(data);
            if (mUserBean == null) {
                return null;
            }
            if (TextUtils.isEmpty(mUserBean.token)) {
                return null;
            }
            mToken = mUserBean.token;
        }
        return mUserBean;
    }
}
