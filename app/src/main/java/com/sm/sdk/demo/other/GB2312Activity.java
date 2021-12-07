package com.sm.sdk.demo.other;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ThreadPoolUtil;
import com.sunmi.peripheral.printer.SunmiPrinterService;

public class GB2312Activity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gb2312);
        initToolbarBringBack("GB2312");
        startPrint();
    }

    private void startPrint() {
        if (MyApplication.app.sunmiPrinterService == null) {//不支持打印
            showToast("Print not supported");
            return;
        }
        EditText editText = this.findViewById(R.id.et_gb2312);
        ThreadPoolUtil.executeInCachePool(() -> {
            showLoadingDialog(getString(R.string.handling) + "...");
            String str = getStr();
//            StringBuilder stringBuilder = new StringBuilder();
//            Random random = new Random();
//            for (int i = 0; i < 10; i++) {
//                int i1 = random.nextInt(str.length());
//                stringBuilder.append(str.substring(i1, i1 + 1));
//            }
            runOnUiThread(() -> editText.setText("叁轷垌眢潴物ㄢ苦⊙垛"));
            SunmiPrinterService sunmiPrinterService = MyApplication.app.sunmiPrinterService;
            try {
                sunmiPrinterService.printTextWithFont("叁轷垌眢潴物ㄢ苦⊙垛", "", 30, null);
                sunmiPrinterService.lineWrap(6, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            dismissLoadingDialog();
        });
    }

    private String getStr() {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0xA0; i < 0xF7; i++) {
                for (int j = 0xA1; j < 0xFF; j++) {
                    byte[] bytes = new byte[2];
                    bytes[0] = (byte) i;
                    bytes[1] = (byte) j;
                    sb.append(new String(bytes, "gb2312"));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
