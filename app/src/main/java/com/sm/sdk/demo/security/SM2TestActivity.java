package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;

import java.util.Arrays;
import java.util.regex.Pattern;

public class SM2TestActivity extends BaseAppCompatActivity {
    private final SecurityOptV2 securityOptV2 = MyApplication.app.securityOptV2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sm2_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_sm2_test);
        findViewById(R.id.btn_gen_sm2_key_pair).setOnClickListener(this);
        findViewById(R.id.btn_inject_sm2_key).setOnClickListener(this);
        findViewById(R.id.btn_sm2_sign).setOnClickListener(this);
        findViewById(R.id.btn_sm2_verify).setOnClickListener(this);
        findViewById(R.id.btn_sm2_encrypt).setOnClickListener(this);
        findViewById(R.id.btn_sm2_decrypt).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gen_sm2_key_pair:
                generateSM2KeyPair();
                break;
            case R.id.btn_inject_sm2_key:
                injectSM2Key();
                break;
            case R.id.btn_sm2_sign:
                sm2Sign();
                break;
            case R.id.btn_sm2_verify:
                sm2VerifySignature();
                break;
            case R.id.btn_sm2_encrypt:
                sm2Encrypt();
                break;
            case R.id.btn_sm2_decrypt:
                sm2Decrypt();
                break;
        }
    }

    /** Generate SM2 keypair */
    private void generateSM2KeyPair() {
        try {
            String pvtKeyIndexStr = this.<EditText>findViewById(R.id.edt_gen_pvt_key_index).getText().toString();
            if (TextUtils.isEmpty(pvtKeyIndexStr)) {
                showToast("private key index should not be empty");
                return;
            }
            int pvtKeyIndex = Integer.parseInt(pvtKeyIndexStr);
            if (pvtKeyIndex < 0 || pvtKeyIndex > 9) {
                showToast("private key index should in [0,9]");
                return;
            }
            Bundle bundle = new Bundle();
            int code = securityOptV2.generateSM2Keypair(pvtKeyIndex, bundle);
            LogUtil.e(TAG, "generate SM2 keypair code:" + code);
            if (code == 0) {
                byte[] data = bundle.getByteArray("data");
                byte[] kcv = bundle.getByteArray("kcv");
                byte[] rfu = bundle.getByteArray("rfu");
                LogUtil.e(TAG, "generate SM2 keypair success,publicKey={"
                        + "\ndata:" + ByteUtil.bytes2HexStr(data)
                        + "\nkcv:" + ByteUtil.bytes2HexStr(kcv)
                        + "\nrfu:" + ByteUtil.bytes2HexStr(rfu)
                        + "\n}");
                showToast("generate SM2 keypair success");
            } else {
                showToast("generate SM2 keypair failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inject a SM2 key
     * <br/> SM2 public key 64B
     * <br/> MS2 private key 32B
     */
    private void injectSM2Key() {
        try {
            String keyIndexStr = this.<EditText>findViewById(R.id.edt_inject_key_index).getText().toString();
            if (TextUtils.isEmpty(keyIndexStr)) {
                showToast("key index should not be empty");
                return;
            }
            int keyIndex = Integer.parseInt(keyIndexStr);
            if (keyIndex < 0 || keyIndex > 9) {
                showToast("key index should in [0,9]");
                return;
            }
            String keyDataStr = this.<EditText>findViewById(R.id.edt_inject_key_data).getText().toString();
            if (TextUtils.isEmpty(keyDataStr)) {
                showToast("key data should not be empty");
                return;
            }
            if (!checkHexValue(keyDataStr)) {
                showToast("key data should be hex string");
                return;
            }
            if (keyDataStr.length() != 64 && keyDataStr.length() != 128) {
                showToast("key data length should be 32B(privateKey) or 64B(PublicKey)");
                return;
            }
            String kcvStr = this.<EditText>findViewById(R.id.edt_inject_kcv).getText().toString();
            if (!TextUtils.isEmpty(kcvStr) && kcvStr.length() != 10) {
                showToast("kcv length should be 5B");
                return;
            }
            String rfuStr = this.<EditText>findViewById(R.id.edt_inject_rfu).getText().toString();
            if (!TextUtils.isEmpty(rfuStr) && rfuStr.length() != 20) {
                showToast("kcv length should be 10B");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putByteArray("data", ByteUtil.hexStr2Bytes(keyDataStr));
            bundle.putByteArray("kcv", ByteUtil.hexStr2Bytes(kcvStr));
            bundle.putByteArray("rfu", ByteUtil.hexStr2Bytes(rfuStr));
            int code = securityOptV2.injectSM2Key(keyIndex, bundle);
            LogUtil.e(TAG, "inject SM2 key code:" + code);
            String msg = "inject SM2 key " + (code == 0 ? "success" : "failed");
            LogUtil.e(TAG, msg);
            showToast(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SM2 sign
     * <br/>SM2 signature data length is 64B
     */
    private void sm2Sign() {
        try {
            String pubKeyIndexStr = this.<EditText>findViewById(R.id.edt_sign_pub_key_index).getText().toString();
            String pvtKeyIndexStr = this.<EditText>findViewById(R.id.edt_sign_pvt_key_index).getText().toString();
            if (TextUtils.isEmpty(pubKeyIndexStr) || TextUtils.isEmpty(pvtKeyIndexStr)) {
                showToast("key index should not be empty");
                return;
            }
            int pubKeyIndex = Integer.parseInt(pubKeyIndexStr);
            int pvtKeyIndex = Integer.parseInt(pvtKeyIndexStr);
            if (pubKeyIndex < 0 || pubKeyIndex > 9 || pvtKeyIndex < 0 || pvtKeyIndex > 9) {
                showToast("key index should in [0,9]");
                return;
            }
            String userIdStr = this.<EditText>findViewById(R.id.edt_sign_user_id).getText().toString();
            if (TextUtils.isEmpty(userIdStr) || !checkHexValue(userIdStr)) {
                showToast("userId should not empty and should be hex string");
                return;
            }
            String dataInStr = this.<EditText>findViewById(R.id.edt_sign_data_in).getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !checkHexValue(dataInStr)) {
                showToast("dataIn should not empty and should be hex string");
                return;
            }
            byte[] userId = ByteUtil.hexStr2Bytes(userIdStr);
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] dataOut = new byte[64];
            int len = securityOptV2.sm2Sign(pubKeyIndex, pvtKeyIndex, userId, dataIn, dataOut);
            LogUtil.e(TAG, "sm2 sign code:" + len);
            if (len >= 0) {//success
                String validStr = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
                LogUtil.e(TAG, "sm2 sign success, signature:" + validStr);
                TextView textView = findViewById(R.id.txt_sign_signature);
                textView.setText(validStr);
            } else {//failed
                showToast("sm2 sign failed, code:" + len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SM2 verify signature
     * <br/> SM2 signature data length is 64B
     */
    private void sm2VerifySignature() {
        try {
            String pubKeyIndexStr = this.<EditText>findViewById(R.id.edt_verify_pub_key_index).getText().toString();
            if (TextUtils.isEmpty(pubKeyIndexStr)) {
                showToast("public key index should not be empty");
                return;
            }
            int pubKeyIndex = Integer.parseInt(pubKeyIndexStr);
            if (pubKeyIndex < 0 || pubKeyIndex > 9) {
                showToast("public key index should in [0,9]");
                return;
            }
            String userIdStr = this.<EditText>findViewById(R.id.edt_verify_user_id).getText().toString();
            if (TextUtils.isEmpty(userIdStr) || !checkHexValue(userIdStr)) {
                showToast("userId should not empty and should be hex string");
                return;
            }
            String dataInStr = this.<EditText>findViewById(R.id.edt_verify_data_in).getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !checkHexValue(dataInStr)) {
                showToast("dataIn should not empty and should be hex string");
                return;
            }
            String signatureStr = this.<EditText>findViewById(R.id.edt_verify_signature_data).getText().toString();
            if (TextUtils.isEmpty(signatureStr) || !checkHexValue(signatureStr)) {
                showToast("signature should not empty and should be hex string");
                return;
            }
            byte[] userId = ByteUtil.hexStr2Bytes(userIdStr);
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] signature = ByteUtil.hexStr2Bytes(signatureStr);
            int code = securityOptV2.sm2VerifySign(pubKeyIndex, userId, dataIn, signature);
            LogUtil.e(TAG, "sm2 verify signature code:" + code);
            if (code == 0) {//success
                LogUtil.e(TAG, "sm2 verify signature success");
                showToast("sm2 verify signature success");
            } else {//failed
                LogUtil.e(TAG, "sm2 verify signature failed,code:" + code);
                showToast("sm2 verify signature failed,code:" + code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SM2 encrypt data
     */
    private void sm2Encrypt() {
        try {
            String pubKeyIndexStr = this.<EditText>findViewById(R.id.edt_enc_pub_key_index).getText().toString();
            if (TextUtils.isEmpty(pubKeyIndexStr)) {
                showToast("public key index should not be empty");
                return;
            }
            int pubKeyIndex = Integer.parseInt(pubKeyIndexStr);
            if (pubKeyIndex < 0 || pubKeyIndex > 9) {
                showToast("public key index should in [0,9]");
                return;
            }
            String dataInStr = this.<EditText>findViewById(R.id.edt_enc_data_in).getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !checkHexValue(dataInStr)) {
                showToast("dataIn should not empty and should be hex string");
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] dataOut = new byte[1024];
            int len = securityOptV2.sm2EncryptData(pubKeyIndex, dataIn, dataOut);
            LogUtil.e(TAG, "sm2 encrypt data code:" + len);
            if (len >= 0) {//success
                String validStr = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
                LogUtil.e(TAG, "sm2 encrypt data, out:" + validStr);
                TextView textView = findViewById(R.id.txt_encrypt_result);
                textView.setText(validStr);
            } else {//failed
                showToast("sm2 encrypt data failed,code:" + len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SM2 decrypt data
     */
    private void sm2Decrypt() {
        try {
            String pvtKeyIndexStr = this.<EditText>findViewById(R.id.edt_dec_pvt_key_index).getText().toString();
            if (TextUtils.isEmpty(pvtKeyIndexStr)) {
                showToast("private key index should not be empty");
                return;
            }
            int pvtKeyIndex = Integer.parseInt(pvtKeyIndexStr);
            if (pvtKeyIndex < 0 || pvtKeyIndex > 9) {
                showToast("private key index should in [0,9]");
                return;
            }
            String dataInStr = this.<EditText>findViewById(R.id.edt_dec_data_in).getText().toString();
            if (TextUtils.isEmpty(dataInStr) || !checkHexValue(dataInStr)) {
                showToast("dataIn should not empty and should be hex string");
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataInStr);
            byte[] dataOut = new byte[1024];
            int len = securityOptV2.sm2DecryptData(pvtKeyIndex, dataIn, dataOut);
            LogUtil.e(TAG, "sm2 decrypt data code:" + len);
            if (len >= 0) {//success
                String validStr = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
                LogUtil.e(TAG, "sm2 decrypt data, out:" + validStr);
                TextView textView = findViewById(R.id.txt_decrypt_result);
                textView.setText(validStr);
            } else {//failed
                showToast("sm2 decrypt data failed,code:" + len);
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
