package com.sm.sdk.demo.pin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.emv.EmvUtil;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.PinBlockFormat;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadTextConfigV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class PinPadActivity extends BaseAppCompatActivity {
    private EditText txtConfirm;
    private EditText txtInputPin;
    private EditText txtInputOfflinePin;
    private EditText txtReinputOfflinePinFormat;

    private String cardNo;
    private EditText mEditCardNo;
    private EditText mEditTimeout;
    private EditText mEditKeyIndex;

    private TextView mTvInfo;

    private RadioGroup mRGKeyboard;
    private RadioGroup mRGIsOnline;
    private RadioGroup mRGKeyboardStyle;
    private RadioGroup mRGPikKeySystem;
    private RadioGroup mRGPinAlgorithmType;
    private SparseArray<CheckBox> rdoModeList;

    private static final int HANDLER_WHAT_INIT_PIN_PAD = 661;
    private static final int HANDLER_PIN_LENGTH = 662;
    private static final int HANDLER_CONFIRM = 663;
    private static final int HANDLER_WHAT_CANCEL = 664;
    private static final int HANDLER_ERROR = 665;

    private final Handler mHandler = new Handler(msg -> {
        switch (msg.what) {
            case HANDLER_WHAT_INIT_PIN_PAD:
                RadioGroup rdoGroup = findViewById(R.id.rg_pin_style);
                int checkedId = rdoGroup.getCheckedRadioButtonId();
                if (checkedId == R.id.rb_pin_style_normal) {
                    initPinPad();
                } else if (checkedId == R.id.rb_pin_style_normal_extend) {
                    initPinPadEx();
                }
                break;
            case HANDLER_WHAT_CANCEL:
                showToast("user cancel");
                break;
            case HANDLER_PIN_LENGTH:
                showToast("inputting");
                break;
            case HANDLER_CONFIRM:
                mTvInfo.setText("PinBlock:" + msg.obj);
                showToast("click ok");
                break;
            case HANDLER_ERROR:
                showToast("error:" + msg.obj + " -- " + msg.arg1);
                break;
        }
        return true;
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_pad);
        initToolbarBringBack(R.string.pin_pad);
        initView();
        EmvUtil.setTerminalParam(EmvUtil.getConfig(EmvUtil.COUNTRY_CHINA));
    }

    private void initView() {
        RadioGroup rdoGroup = findViewById(R.id.rg_pin_style);
        rdoGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_pin_style_normal) {
                findViewById(R.id.input_step_lay).setVisibility(View.GONE);
                findViewById(R.id.edit_input_step).setVisibility(View.GONE);
                findViewById(R.id.input_diversify_lay).setVisibility(View.GONE);
                findViewById(R.id.edit_diversify).setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_pin_style_normal_extend) {
                findViewById(R.id.input_step_lay).setVisibility(View.VISIBLE);
                findViewById(R.id.edit_input_step).setVisibility(View.VISIBLE);
                findViewById(R.id.input_diversify_lay).setVisibility(View.VISIBLE);
                findViewById(R.id.edit_diversify).setVisibility(View.GONE);
            }
        });
        rdoGroup.check(R.id.rb_pin_style_normal);
        rdoModeList = new SparseArray<>();
        rdoModeList.put(R.id.rdo_mode_normal, findViewById(R.id.rdo_mode_normal));
        rdoModeList.put(R.id.rdo_mode_long_press_to_clear, findViewById(R.id.rdo_mode_long_press_to_clear));
        rdoModeList.put(R.id.rdo_mode_silent, findViewById(R.id.rdo_mode_silent));
        rdoModeList.put(R.id.rdo_mode_green_led, findViewById(R.id.rdo_mode_green_led));
        for (int i = 0, size = rdoModeList.size(); i < size; i++) {
            rdoModeList.valueAt(i).setOnClickListener(this);
        }
        txtConfirm = findViewById(R.id.edit_txt_confirm);
        txtInputPin = findViewById(R.id.edit_txt_input_pin);
        txtInputOfflinePin = findViewById(R.id.edit_txt_input_offline_pin);
        txtReinputOfflinePinFormat = findViewById(R.id.edit_txt_reinput_offline_pin_fmt);
        mEditCardNo = findViewById(R.id.edit_card_no);
        mEditTimeout = findViewById(R.id.edit_timeout);
        mEditKeyIndex = findViewById(R.id.edit_key_index);

        mTvInfo = findViewById(R.id.tv_info);

        mRGKeyboard = findViewById(R.id.rg_keyboard);
        mRGIsOnline = findViewById(R.id.rg_is_online);
        mRGKeyboardStyle = findViewById(R.id.rg_keyboard_style);
        mRGPikKeySystem = findViewById(R.id.key_system);
        mRGPinAlgorithmType = findViewById(R.id.pin_type);

        rdoModeList.get(R.id.rdo_mode_normal).setChecked(true);
        findViewById(R.id.mb_set_mode).setOnClickListener(this);
        findViewById(R.id.mb_set_text).setOnClickListener(this);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        findViewById(R.id.call_custom_keyboard).setOnClickListener(this);
        mEditCardNo.setText("123456789123456");
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.rdo_mode_normal:
            case R.id.rdo_mode_long_press_to_clear:
            case R.id.rdo_mode_silent:
            case R.id.rdo_mode_green_led:
                onModeButtonClick(id);
                break;
            case R.id.mb_set_mode:
                onSetPinPadModeClick();
                break;
            case R.id.mb_set_text:
                initPinPadText();
                break;
            case R.id.mb_ok:
                mHandler.sendEmptyMessage(HANDLER_WHAT_INIT_PIN_PAD);
                break;
            case R.id.call_custom_keyboard:
                initCustomPinPad();
                break;
        }
    }

    private void onModeButtonClick(int id) {
        CheckBox normal = rdoModeList.get(R.id.rdo_mode_normal);
        if (id == R.id.rdo_mode_normal && normal.isChecked()) {
            for (int i = 0, size = rdoModeList.size(); i < size; i++) {
                if (rdoModeList.keyAt(i) != id) {
                    rdoModeList.valueAt(i).setChecked(false);
                }
            }
        } else if (rdoModeList.get(R.id.rdo_mode_normal).isChecked()) {
            rdoModeList.get(R.id.rdo_mode_normal).setChecked(false);
        }
    }

    /**
     * Set PinPad mode
     * the set value just valid for next time inputting PIN, after input PIN finished,
     * the set value is lost effect
     */
    private void onSetPinPadModeClick() {
        try {
            Bundle bundle = new Bundle();
            if (rdoModeList.get(R.id.rdo_mode_normal).isChecked()) {//Normal mode
                bundle.putInt("normal", 1);
            } else {
                if (rdoModeList.get(R.id.rdo_mode_long_press_to_clear).isChecked()) {
                    bundle.putInt("longPressToClear", 1);
                }
                if (rdoModeList.get(R.id.rdo_mode_silent).isChecked()) {
                    bundle.putInt("silent", 1);
                }
                if (rdoModeList.get(R.id.rdo_mode_green_led).isChecked()) {
                    bundle.putInt("greenLed", 1);
                }
            }
            int code = MyApplication.app.pinPadOptV2.setPinPadMode(bundle);
            String msg = "Set PinPad mode " + (code == 0 ? "success" : "failed");
            LogUtil.e(TAG, msg);
            showToast(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Get current PinPad mode */
    private void getPinPadMode() {
        try {
            Bundle bundle = new Bundle();
            int code = MyApplication.app.pinPadOptV2.getPinPadMode(bundle);
            if (code != 0) {
                LogUtil.e(TAG, "get PinPad mode failed.");
                return;
            }
            LogUtil.e(TAG, "get PinPad mode success.");
            int normal = bundle.getInt("normal");
            int longPressToClear = bundle.getInt("longPressToClear");
            int silent = bundle.getInt("silent");
            int greenLed = bundle.getInt("greenLed");
            LogUtil.e(TAG, Utility.formatStr("PinPad mode:\nnormal:%d\nlongPressToClear:%d\nsilent:%d\ngreenLed:%d",
                    normal, longPressToClear, silent, greenLed));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set PinPad text
     * <br/> Mostly, the SDK built-in text is appropriate, client don't need to change it.
     * But if client want to customize the showing text, this method is helpful.
     * the set value just valid for next time inputting PIN, after input PIN finished,
     * the set value lost effect
     */
    private void initPinPadText() {
        try {
            if (!checkInput(txtConfirm) || !checkInput(txtInputPin)
                    || !checkInput(txtInputOfflinePin) || !checkInput(txtReinputOfflinePinFormat)) {
                return;
            }
            String confirm = txtConfirm.getText().toString();
            String inputPin = txtInputPin.getText().toString();
            String inputOfflinePin = txtInputOfflinePin.getText().toString();
            String reInputOfflinePinFormat = txtReinputOfflinePinFormat.getText().toString();
            PinPadTextConfigV2 textConfigV2 = new PinPadTextConfigV2();
            textConfigV2.confirm = confirm;
            textConfigV2.inputPin = inputPin;
            textConfigV2.inputOfflinePin = inputOfflinePin;
            textConfigV2.reinputOfflinePinFormat = reInputOfflinePinFormat;
            MyApplication.app.pinPadOptV2.setPinPadText(textConfigV2);
            showToast(R.string.success);
        } catch (RemoteException e) {
            e.printStackTrace();
            showToast(R.string.fail);
        }
    }

    private boolean checkInput(EditText edt) {
        String text = edt.getText().toString();
        if (TextUtils.isEmpty(text)) {
            showToast("PinPad text can't be empty!");
            edt.requestFocus();
            return false;
        }
        return true;
    }


    /** check whether src is hex format */
    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }

    /** start SDK built-in PinPad */
    private void initPinPad() {
        try {
            PinPadConfigV2 configV2 = initPinPadConfigV2();
            if (configV2 != null) {
                // start input PIN
                MyApplication.app.pinPadOptV2.initPinPad(configV2, mPinPadListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** start SDK built-in PinPad */
    private void initPinPadEx() {
        String pikIndexStr = mEditKeyIndex.getText().toString();
        if (TextUtils.isEmpty(pikIndexStr)) {
            showToast(R.string.pin_pad_key_index_hint);
            return;
        }
        int pinKeyIndex = Integer.parseInt(pikIndexStr);
        boolean isKeyDukpt = mRGPikKeySystem.getCheckedRadioButtonId() == R.id.rb_key_system_dukpt;
        if ((isKeyDukpt && (pinKeyIndex < 1100 || pinKeyIndex > 1199) && (pinKeyIndex < 0 || pinKeyIndex > 19))
                || (!isKeyDukpt && (pinKeyIndex < 0 || pinKeyIndex > 19))) {
            showToast(R.string.pin_pad_key_index_hint);
            mEditKeyIndex.requestFocus();
            return;
        }
        String timeoutStr = mEditTimeout.getText().toString();
        if (TextUtils.isEmpty(timeoutStr)) {
            showToast(R.string.pin_pad_timeout_hint);
            return;
        }
        int timeout = Integer.parseInt(timeoutStr) * 1000;
        if (timeout < 0 || timeout > 60000) {
            showToast(R.string.pin_pad_timeout_hint);
            return;
        }
        EditText edtInputStep = findViewById(R.id.edit_input_step);
        if (TextUtils.isEmpty(edtInputStep.getText())) {
            showToast("Please input correct inputStep");
            return;
        }
        int inputStep = Integer.parseInt(edtInputStep.getText().toString());
        if (inputStep < 0 || inputStep > 12) {
            showToast("Please input correct inputStep");
            return;
        }
        cardNo = mEditCardNo.getText().toString().trim();
        if (cardNo.length() < 13 || cardNo.length() > 19) {
            showToast(R.string.pin_pad_card_no_hint);
            return;
        }
        try {
            Bundle bundle = new Bundle();
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            int pinAlgType = 0;//3DES
            int pinBlockFormat = PinBlockFormat.SEC_PIN_BLK_ISO_FMT0;
            if (mRGPinAlgorithmType.getCheckedRadioButtonId() == R.id.rb_pin_type_sm4) {
                pinAlgType = 1;//SM4
            } else if (mRGPinAlgorithmType.getCheckedRadioButtonId() == R.id.rb_pin_type_aes) {
                pinAlgType = 2;//AES
                pinBlockFormat = PinBlockFormat.SEC_PIN_BLK_ISO_FMT4;
            }
            // PinPadType: 0-SDK built-in PinPad, 1-Client customized PinPad
            bundle.putInt("PinPadType", mRGKeyboardStyle.getCheckedRadioButtonId() == R.id.rb_preset_keyboard ? 0 : 1);
            // PinType: 0-online PIN, 1-offline PIN
            bundle.putInt("PinType", mRGIsOnline.getCheckedRadioButtonId() == R.id.rb_online_pin ? 0 : 1);
            // isOrderNumberKey: true-order number PinPad, false-disorder number PinPad
            bundle.putInt("isOrderNumKey", mRGKeyboard.getCheckedRadioButtonId() == R.id.rb_orderly_keyboard ? 1 : 0);
            // PAN(Person Identify Number) ASCII格式转换成的byte 例如 “123456”.getBytes("us ascii")
            byte[] panBytes = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1).getBytes("US-ASCII");
            bundle.putByteArray("pan", panBytes);
            // PIK(PIN key) index
            bundle.putInt("pinKeyIndex", pinKeyIndex);
            // Minimum input PIN number
            bundle.putInt("minInput", 0);
            // Maximum input number(Max value is 12)
            bundle.putInt("maxInput", 12);
            // The input step if input PIN, default 1
            bundle.putInt("inputStep", inputStep);
            // Input PIN timeout time
            bundle.putInt("timeout", timeout);
            // is support bypass PIN, 0-not support, 1-support
            bundle.putInt("isSupportbypass", 1);
            // PIN block format
            bundle.putInt("pinblockFormat", pinBlockFormat);
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            bundle.putInt("algorithmType", pinAlgType);
            // PIK key system: 0-MKSK, 1-Dukpt
            bundle.putInt("keySystem", mRGPikKeySystem.getCheckedRadioButtonId() == R.id.rb_key_system_mksk ? 0 : 1);
            MyApplication.app.pinPadOptV2.initPinPadEx(bundle, mPinPadListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Initialize customized PinPad */
    private void initCustomPinPad() {
        try {
            PinPadConfigV2 pinPadConfigV2 = initPinPadConfigV2();
            if (pinPadConfigV2 != null) {
                Intent mIntent = new Intent(this, CustomPinPadActivity.class);
                mIntent.putExtra("PinPadConfigV2", (Serializable) pinPadConfigV2);
                mIntent.putExtra("cardNo", cardNo);
                startActivityForResult(mIntent, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 初始化PinPadConfigV2 */
    private PinPadConfigV2 initPinPadConfigV2() {
        String pikIndex = mEditKeyIndex.getText().toString();
        if (TextUtils.isEmpty(pikIndex)) {
            showToast(R.string.pin_pad_key_index_hint);
            return null;
        }
        int pinKeyIndex = Integer.parseInt(pikIndex);
        boolean isKeyDukpt = mRGPikKeySystem.getCheckedRadioButtonId() == R.id.rb_key_system_dukpt;
        if ((isKeyDukpt && (pinKeyIndex < 1100 || pinKeyIndex > 1199) && (pinKeyIndex < 0 || pinKeyIndex > 19))
                || (!isKeyDukpt && (pinKeyIndex < 0 || pinKeyIndex > 19))) {
            showToast(R.string.pin_pad_key_index_hint);
            mEditKeyIndex.requestFocus();
            return null;
        }
        String timeoutStr = mEditTimeout.getText().toString();
        if (TextUtils.isEmpty(timeoutStr)) {
            showToast(R.string.pin_pad_timeout_hint);
            return null;
        }
        int timeout = Integer.parseInt(timeoutStr) * 1000;
        if (timeout < 0 || timeout > 60000) {
            showToast(R.string.pin_pad_timeout_hint);
            return null;
        }
        cardNo = mEditCardNo.getText().toString().trim();
        if (cardNo.length() < 13 || cardNo.length() > 19) {
            showToast(R.string.pin_pad_card_no_hint);
            return null;
        }
        try {
            PinPadConfigV2 pinPadConfig = new PinPadConfigV2();
            // PinPadType: 0-SDK built-in PinPad, 1-Client customized PinPad
            pinPadConfig.setPinPadType(mRGKeyboardStyle.getCheckedRadioButtonId() == R.id.rb_preset_keyboard ? 0 : 1);
            // PinType: 0-online PIN, 1-offline PIN
            pinPadConfig.setPinType(mRGIsOnline.getCheckedRadioButtonId() == R.id.rb_online_pin ? 0 : 1);
            // isOrderNumerKey: true:order number PinPad, false:disorder number PinPad
            pinPadConfig.setOrderNumKey(mRGKeyboard.getCheckedRadioButtonId() == R.id.rb_orderly_keyboard);
            // PinAlgType: 0-3DES, 1-SM4, 2-AES
            int pinAlgType = 0;//3DES
            if (mRGPinAlgorithmType.getCheckedRadioButtonId() == R.id.rb_pin_type_sm4) {
                pinAlgType = 1;//SM4
            } else if (mRGPinAlgorithmType.getCheckedRadioButtonId() == R.id.rb_pin_type_aes) {
                pinAlgType = 2;//AES
                pinPadConfig.setPinblockFormat(PinBlockFormat.SEC_PIN_BLK_ISO_FMT4);
            }
            pinPadConfig.setAlgorithmType(pinAlgType);
            // PIK key system: 0-MKSK, 1-Dukpt
            pinPadConfig.setKeySystem(mRGPikKeySystem.getCheckedRadioButtonId() == R.id.rb_key_system_mksk ? 0 : 1);
            // PAN(Person Identify Number) ASCII格式转换成的byte 例如 “123456”.getBytes("us ascii")
            byte[] panBytes = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1).getBytes(StandardCharsets.US_ASCII);
            pinPadConfig.setPan(panBytes);
            // Input PIN timeout time
            pinPadConfig.setTimeout(timeout);
            // PIK(PIN key) index
            pinPadConfig.setPinKeyIndex(pinKeyIndex);
            // Minimum input PIN number
            pinPadConfig.setMinInput(0);
            // Maximum input number(Max value is 12)
            pinPadConfig.setMaxInput(12);
            return pinPadConfig;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private final PinPadListenerV2 mPinPadListener = new PinPadListenerV2.Stub() {
        @Override
        public void onPinLength(int i) {
            LogUtil.e(Constant.TAG, "onPinLength:" + i);
//            mHandler.obtainMessage(HANDLER_PIN_LENGTH, i, 0).sendToTarget();
        }

        @Override
        public void onConfirm(int pinType, byte[] bytes) {
            String hexStr = ByteUtil.bytes2HexStr(bytes);
            LogUtil.e(Constant.TAG, "onConfirm, pinType:" + pinType + ",PinBlock:" + hexStr);
            mHandler.obtainMessage(HANDLER_CONFIRM, hexStr).sendToTarget();
        }

        @Override
        public void onCancel() {
            LogUtil.e(Constant.TAG, "onCancel");
            mHandler.sendEmptyMessage(HANDLER_WHAT_CANCEL);
        }

        @Override
        public void onError(int code) {
            LogUtil.e(Constant.TAG, "onError:" + code);
            String msg = AidlErrorCodeV2.valueOf(code).getMsg();
            mHandler.obtainMessage(HANDLER_ERROR, code, code, msg).sendToTarget();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String pinCipher = data.getStringExtra("pinCipher");
            if (!TextUtils.isEmpty(pinCipher)) {
                mTvInfo.setText("PinBlock:" + pinCipher);
            }
        }
    }
}
