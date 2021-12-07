package com.sm.sdk.demo.print;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.LogUtil;

public class PrintConfigActivity extends BaseAppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_config);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.print_set_param);
        findViewById(R.id.btn_set_print_speed).setOnClickListener(this);
        findViewById(R.id.btn_set_print_heat_point).setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_print_speed:
                setPrintSpeed();
                break;
            case R.id.btn_set_print_heat_point:
                setPrintHeatPoint();
                break;
        }
    }

    private void setPrintSpeed() {
        try {
            if (!DeviceUtil.isP2() && !DeviceUtil.isP2Pro()) {
                showToast("Device not support set print speed");
                return;
            }
            String hexSpeed = this.<EditText>findViewById(R.id.edt_print_speed).getText().toString();
            int speed = Integer.parseInt(hexSpeed);
            //print speed should be in [313,4291]
            if (speed < 313 || speed > 4291) {
                showToast("Print speed should in [313,4291]");
                return;
            }
            int code = MyApplication.app.printerOptV2.setPrintSpeed(speed);
            LogUtil.e(TAG, "set print speed code:" + code);
            showToast(code == 0 ? "success" : "failed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPrintHeatPoint() {
        try {
            if (!DeviceUtil.isP2() && !DeviceUtil.isP2Pro()) {
                showToast("Device not support set print heat point");
                return;
            }
            String hexHeatPoint = this.<EditText>findViewById(R.id.edt_print_heat_point).getText().toString();
            int heatPoint = Integer.parseInt(hexHeatPoint);
            //Heat point should be 64/96/128/192
            if (heatPoint != 64 && heatPoint != 96 && heatPoint != 128 && heatPoint != 192) {
                showToast("Print heat point should be 64/96/128/192");
                return;
            }
            int code = MyApplication.app.printerOptV2.setPrintHeatPoint(heatPoint);
            LogUtil.e(TAG, "set heat point code:" + code);
            showToast(code == 0 ? "success" : "failed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
