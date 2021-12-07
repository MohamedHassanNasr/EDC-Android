package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

import java.util.Arrays;
import java.util.regex.Pattern;

public class CalcHashActivity extends BaseAppCompatActivity {
    private int hashAlgType = AidlConstantsV2.Security.HASH_SHA_TYPE_1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_calc_hash);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_calc_hash);
        RadioGroup rdoGroup = findViewById(R.id.has_alg_type);
        rdoGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_sha_type_1:
                    hashAlgType = AidlConstantsV2.Security.HASH_SHA_TYPE_1;
                    break;
                case R.id.rb_sha_type_224:
                    hashAlgType = AidlConstantsV2.Security.HASH_SHA_TYPE_224;
                    break;
                case R.id.rb_sha_type_256:
                    hashAlgType = AidlConstantsV2.Security.HASH_SHA_TYPE_256;
                    break;
                case R.id.rb_sha_type_384:
                    hashAlgType = AidlConstantsV2.Security.HASH_SHA_TYPE_384;
                    break;
                case R.id.rb_sha_type_512:
                    hashAlgType = AidlConstantsV2.Security.HASH_SHA_TYPE_512;
                    break;
                case R.id.rb_sm3_type:
                    hashAlgType = AidlConstantsV2.Security.HASH_SM3_TYPE;
                    break;
            }
        });
        rdoGroup.check(R.id.rb_sha_type_1);
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok:
                calcHash();
                break;
        }
    }

    /** Calculate hash */
    private void calcHash() {
        try {
            String dataInStr = this.<EditText>findViewById(R.id.edt_data_in).getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !checkHexValue(dataInStr)) {
                showToast("dataIn should not empty and should be hex string");
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] dataOut = new byte[2048];
            int len = MyApplication.app.securityOptV2.calcSecHash(hashAlgType, dataIn, dataOut);
            LogUtil.e(TAG, "calculate hash, code:" + len);
            if (len >= 0) {//success
                String validStr = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
                TextView textView = findViewById(R.id.tv_info);
                textView.setText(validStr);
            } else {//failed
                showToast("calc hash failed,code:" + len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** check whether src is hex format */
    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }
}
