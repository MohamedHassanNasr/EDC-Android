package com.sm.sdk.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.window.SplashScreen;

import com.sm.sdk.demo.basic.BasicActivity;
import com.sm.sdk.demo.card.CardActivity;
import com.sm.sdk.demo.emv.EMVActivity;
import com.sm.sdk.demo.etc.ETCActivity;
import com.sm.sdk.demo.other.OtherActivity;
import com.sm.sdk.demo.pin.PinPadActivity;
import com.sm.sdk.demo.print.PrintActivity;
import com.sm.sdk.demo.scan.ScanActivity;
import com.sm.sdk.demo.security.SecurityActivity;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseAppCompatActivity {

    private Object DateFormat;
    private final BasicOptV2 basicOptV2 = MyApplication.app.basicOptV2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String day = dayOfTheWeek + ", " + date;

        //Toast.makeText(getApplicationContext(), day, Toast.LENGTH_LONG).show();
        //setContentView(R.layout.activity_base);

        //showToast(AidlConstantsV2.CardType.FELICA.getValue());

        //basicOptV2.setScreenMode(AidlConstantsV2.SystemUI.SET_SCREEN_MONOPOLY);

        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("SunmiSDKTestDemoVer2");
        //Toast.makeText(getApplicationContext(), "MASUK SINI MAIN ACTIVITY.JAVA", Toast.LENGTH_LONG).show();

        findViewById(R.id.card_view_basic).setOnClickListener(this);
        findViewById(R.id.card_view_card).setOnClickListener(this);
        findViewById(R.id.card_view_pin_pad).setOnClickListener(this);
        findViewById(R.id.card_view_security).setOnClickListener(this);
        findViewById(R.id.card_view_emv).setOnClickListener(this);
        findViewById(R.id.card_view_scan).setOnClickListener(this);
        findViewById(R.id.card_view_print).setOnClickListener(this);
        findViewById(R.id.card_view_other).setOnClickListener(this);
        findViewById(R.id.card_view_etc).setOnClickListener(this);
        //findViewById(R.id.card_view_comm).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        if (!MyApplication.app.isConnectPaySDK()) {
//            MyApplication.app.bindPaySDKService();
//            showToast(R.string.connect_loading);
//            return;
//        }
        final int id = v.getId();
        switch (id) {
            case R.id.card_view_basic:
                openActivity(BasicActivity.class);
                showToast("call basic card__");
                break;
            case R.id.card_view_card:
                openActivity(CardActivity.class);
                Log.d(TAG, "ON TESTED DEBUG---------");
                break;
            case R.id.card_view_pin_pad:
                openActivity(PinPadActivity.class);
                break;
            case R.id.card_view_security:
                openActivity(SecurityActivity.class);
                break;
            case R.id.card_view_emv:
                openActivity(EMVActivity.class);
                break;
            case R.id.card_view_scan:
                openActivity(ScanActivity.class);
                break;
            case R.id.card_view_print:
                openActivity(PrintActivity.class);
                break;
            case R.id.card_view_etc:
                openActivity(ETCActivity.class);
                break;
            case R.id.card_view_other:
                openActivity(OtherActivity.class);
                break;
        }
    }

    public static void reStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


}
