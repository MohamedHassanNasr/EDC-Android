package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

public class GetKeyCheckValueActivity extends BaseAppCompatActivity {
    private int keySystem = AidlConstantsV2.Security.SEC_MKSK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_get_kcv);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_get_kcv);
        RadioGroup group = findViewById(R.id.key_system_group);
        group.setOnCheckedChangeListener((group1, checkedId) -> {
            if (checkedId == R.id.rdo_sys_mksk) {
                keySystem = AidlConstantsV2.Security.SEC_MKSK;
            } else if (checkedId == R.id.rdo_sys_dukpt) {
                keySystem = AidlConstantsV2.Security.SEC_DUKPT;
            }
        });
        group.check(R.id.rdo_sys_mksk);
        findViewById(R.id.mb_get_kcv).setOnClickListener((v) -> getKcv());
    }

    private void getKcv() {
        try {
            String keyIndexStr = this.<EditText>findViewById(R.id.key_index).getText().toString();
            int keyIndex = -1;
            if (keySystem == AidlConstantsV2.Security.SEC_MKSK) {
                if (TextUtils.isEmpty(keyIndexStr)) {
                    showToast(R.string.security_key_index_hint);
                    return;
                }
                keyIndex = Integer.parseInt(keyIndexStr);
                if (keyIndex > 19 || keyIndex < 0) {
                    showToast(R.string.security_key_index_hint);
                    return;
                }
            } else if (keySystem == AidlConstantsV2.Security.SEC_DUKPT) {
                if (TextUtils.isEmpty(keyIndexStr)) {
                    showToast(R.string.security_duKpt_key_hint);
                    return;
                }
                keyIndex = Integer.parseInt(keyIndexStr);
                if ((keyIndex < 0 || keyIndex > 19) && (keyIndex < 1100 || keyIndex > 1199)) {
                    showToast(R.string.security_duKpt_key_hint);
                    return;
                }
            }
            byte[] dataOut = new byte[4];
            int code = MyApplication.app.securityOptV2.getKeyCheckValue(keySystem, keyIndex, dataOut);
            if (code < 0) {
                String msg = "Get kcv error:" + code;
                LogUtil.e(TAG, msg);
                showToast(msg);
            } else {
                String hexKcv = ByteUtil.bytes2HexStr(dataOut);
                this.<TextView>findViewById(R.id.tv_info).setText("KCV:" + hexKcv);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("key illegal key index");
        }
    }

}
