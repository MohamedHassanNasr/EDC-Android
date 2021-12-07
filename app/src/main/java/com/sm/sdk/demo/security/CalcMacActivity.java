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

public class CalcMacActivity extends BaseAppCompatActivity {
    private EditText mEditKeyIndex;
    private EditText mEditData;
    private EditText mEditKeyLen;
    private EditText mEditDiversify;

    private TextView mTvInfo;
    private int mCalcType = AidlConstantsV2.Security.MAC_ALG_ISO_9797_1_MAC_ALG1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_calc_mac);
        initToolbarBringBack(R.string.security_calc_mac);
        initView();
    }

    private void initView() {
        RadioGroup rdoGroup = findViewById(R.id.mac_style);
        rdoGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_mac_style_normal) {
                findViewById(R.id.mac_key_len_lay).setVisibility(View.GONE);
                findViewById(R.id.mac_key_len).setVisibility(View.GONE);
                findViewById(R.id.mac_diversify_lay).setVisibility(View.GONE);
                findViewById(R.id.mac_diversify).setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_mac_style_extend) {
                findViewById(R.id.mac_key_len_lay).setVisibility(View.VISIBLE);
                findViewById(R.id.mac_key_len).setVisibility(View.VISIBLE);
                findViewById(R.id.mac_diversify_lay).setVisibility(View.VISIBLE);
                findViewById(R.id.mac_diversify).setVisibility(View.GONE);
            }
        });
        rdoGroup.check(R.id.rb_mac_style_normal);
        rdoGroup = findViewById(R.id.mac_type);
        rdoGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_mac_type1:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_ISO_9797_1_MAC_ALG1;
                            break;
                        case R.id.rb_mac_type2:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_ISO_9797_1_MAC_ALG3;
                            break;
                        case R.id.rb_mac_type3:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_ISO_16609_MAC_ALG1;
                            break;
                        case R.id.rb_mac_type4:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_FAST_MODE;
                            break;
                        case R.id.rb_mac_type5:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_X9_19;
                            break;
                        case R.id.rb_mac_type6:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_CBC;
                            break;
                        case R.id.rb_mac_type7:
                            mCalcType = AidlConstantsV2.Security.MAC_ALG_CUP_SM4_MAC_ALG1;
                            break;
                    }
                }
        );
        mEditKeyIndex = findViewById(R.id.key_index);
        mEditData = findViewById(R.id.source_data);
        mEditKeyLen = findViewById(R.id.mac_key_len);
        mEditDiversify = findViewById(R.id.mac_diversify);
        mTvInfo = findViewById(R.id.tv_info);
        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                calcMac();
                break;
        }
    }

    private void calcMac() {
        RadioGroup rdoGroup = findViewById(R.id.mac_style);
        int id = rdoGroup.getCheckedRadioButtonId();
        if (id == R.id.rb_mac_style_normal) {
            calcMacNormal();
        } else if (id == R.id.rb_mac_style_extend) {
            calcMacWithDiversify();
        }
    }

    /** Normal style Calculate Mac */
    private void calcMacNormal() {
        try {
            String keyIndexStr = mEditKeyIndex.getText().toString();
            String dataStr = mEditData.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast(R.string.security_key_index_hint);
                return;
            }
            int keyIndex = Integer.parseInt(keyIndexStr);
            if (keyIndex < 0 || keyIndex > 19) {
                showToast(R.string.security_key_index_hint);
                return;
            }
            if (TextUtils.isEmpty(dataStr) || dataStr.length() % 2 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            byte[] dataOut = new byte[8];
            if (mCalcType == AidlConstantsV2.Security.MAC_ALG_CUP_SM4_MAC_ALG1) {
                dataOut = new byte[16];
            }
            byte[] dataBytes = ByteUtil.hexStr2Bytes(dataStr);
            int code = MyApplication.app.securityOptV2.calcMac(keyIndex, mCalcType, dataBytes, dataOut);
            if (code == 0) {
                String hexStr = ByteUtil.bytes2HexStr(dataOut);
                mTvInfo.setText(hexStr);
                LogUtil.e(TAG, "calcMac() result:" + hexStr);
            } else {
                LogUtil.e(TAG, "calcMac() failed code:" + code);
                toastHint(code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Extend style Calculate Mac, MacKey length, diversify should be set */
    private void calcMacWithDiversify() {
        try {
            String keyIndexStr = mEditKeyIndex.getText().toString();
            String keyLenStr = mEditKeyLen.getText().toString();
            String dataStr = mEditData.getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast(R.string.security_key_index_hint);
                return;
            }
            int keyIndex = Integer.parseInt(keyIndexStr);
            if (keyIndex < 0 || keyIndex > 19) {
                showToast(R.string.security_key_index_hint);
                return;
            }
            if (TextUtils.isEmpty(keyLenStr)) {
                showToast("Mac key length should not be empty");
                return;
            }
            int keyLen = Integer.parseInt(keyLenStr);
            if (keyLen < 8 || keyLen > 24 || keyLen % 8 != 0) {
                showToast("Mac key length should be [8,16,24]");
                return;
            }
            if (TextUtils.isEmpty(dataStr) || dataStr.length() % 2 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            byte[] dataOut = new byte[8];
            if (mCalcType == AidlConstantsV2.Security.MAC_ALG_CUP_SM4_MAC_ALG1) {
                dataOut = new byte[16];
            }
            byte[] dataBytes = ByteUtil.hexStr2Bytes(dataStr);
            int code = MyApplication.app.securityOptV2.calcMacEx(keyIndex, keyLen, mCalcType, null, dataBytes, dataOut);
            if (code >= 0) {
                String hexStr = ByteUtil.bytes2HexStr(dataOut);
                mTvInfo.setText(hexStr);
                LogUtil.e(TAG, "calcMacEx() result:" + hexStr);
            } else {
                LogUtil.e(TAG, "calcMacEx() failed code:" + code);
                toastHint(code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
