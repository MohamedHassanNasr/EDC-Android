package com.sm.sdk.demo.basic;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

import java.util.ArrayList;
import java.util.List;

public class GetSysParamActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_get_sys_param);
        initToolbarBringBack(R.string.basic_get_sys_param);
        initView();
    }

    private void initView() {
        TextView tvInfo = findViewById(R.id.tv_info);
        try {
            List<String> keys = new ArrayList<>();
            keys.add(AidlConstantsV2.SysParam.TERM_STATUS);
            keys.add(AidlConstantsV2.SysParam.DEBUG_MODE);
            keys.add(AidlConstantsV2.SysParam.SDK_VERSION);
            keys.add(AidlConstantsV2.SysParam.HARDWARE_VERSION);
            keys.add(AidlConstantsV2.SysParam.FIRMWARE_VERSION);
            keys.add(AidlConstantsV2.SysParam.SM_VERSION);
            keys.add(AidlConstantsV2.SysParam.SUPPORT_ETC);
            keys.add(AidlConstantsV2.SysParam.ETC_FIRM_VERSION);
            keys.add(AidlConstantsV2.SysParam.BootVersion);
            keys.add(AidlConstantsV2.SysParam.CFG_FILE_VERSION);
            keys.add(AidlConstantsV2.SysParam.FW_VERSION);
            keys.add(AidlConstantsV2.SysParam.SN);
            keys.add(AidlConstantsV2.SysParam.PN);
            keys.add(AidlConstantsV2.SysParam.TUSN);
            keys.add(AidlConstantsV2.SysParam.DEVICE_CODE);
            keys.add(AidlConstantsV2.SysParam.DEVICE_MODEL);
            keys.add(AidlConstantsV2.SysParam.RESERVED);
            keys.add(AidlConstantsV2.SysParam.PCD_PARAM_A);
            keys.add(AidlConstantsV2.SysParam.PCD_PARAM_B);
            keys.add(AidlConstantsV2.SysParam.PCD_PARAM_C);
            keys.add(AidlConstantsV2.SysParam.TUSN_KEY_KCV);
            keys.add(AidlConstantsV2.SysParam.PCD_IFM_VERSION);
            keys.add(AidlConstantsV2.SysParam.PUSH_CFG_FILE);
            keys.add(AidlConstantsV2.SysParam.EMV_VERSION);
            keys.add(AidlConstantsV2.SysParam.PAYPASS_VERSION);
            keys.add(AidlConstantsV2.SysParam.PAYWAVE_VERSION);
            keys.add(AidlConstantsV2.SysParam.QPBOC_VERSION);
            keys.add(AidlConstantsV2.SysParam.ENTRY_VERSION);
            keys.add(AidlConstantsV2.SysParam.MIR_VERSION);
            keys.add(AidlConstantsV2.SysParam.JCB_VERSION);
            keys.add(AidlConstantsV2.SysParam.PAGO_VERSION);
            keys.add(AidlConstantsV2.SysParam.PURE_VERSION);
            keys.add(AidlConstantsV2.SysParam.AE_VERSION);
            keys.add(AidlConstantsV2.SysParam.FLASH_VERSION);
            keys.add(AidlConstantsV2.SysParam.DPAS_VERSION);
            keys.add(AidlConstantsV2.SysParam.APEMV_VERSION);
            keys.add(AidlConstantsV2.SysParam.EFTPOS_VERSION);
            keys.add(AidlConstantsV2.SysParam.EMV_KERNEL_CHECKSUM);
            keys.add(AidlConstantsV2.SysParam.PURE_VERSION_FULL);
            keys.add(AidlConstantsV2.SysParam.EFTPOS_VERSION_FULL);
            keys.add(AidlConstantsV2.SysParam.APEMV_VERSION_FULL);

            StringBuilder sb = new StringBuilder();
            appendSecStatus(sb);//获取安装状态
            for (String key : keys) {
                sb.append(key);
                sb.append(":");
                sb.append(MyApplication.app.basicOptV2.getSysParam(key));
                sb.append("\n");
            }
            tvInfo.setText(sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取触发状态 */
    private void appendSecStatus(StringBuilder sb) {
        sb.append("SecStatus:");
        try {
            String model = Build.MODEL.toLowerCase();
            if (model.matches("p.+")) {//P系列设备存在触发上报机制
                int status = MyApplication.app.securityOptV2.getSecStatus();
                sb.append(Utility.formatStr("%08X", status));
            } else {
                sb.append("null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.append("\n");
    }

}
