package com.sm.sdk.demo.etc;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidlv2.etc.ETCSearchTradeOBUListenerV2;

public class ETCTradeActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;
    private TextView result;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etc_trade_layout);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.etc_trade_test);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        result = findViewById(R.id.result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok:
                etcTrade();
                break;
        }
    }

    private void etcTrade() {
        try {
            result.setText(null);
            //模拟交易(复合消费回复正确数据)
            //1.搜索OBU
            //发送F0帧: 0D1003008500F005015A678900DB
            //应答F0帧:44100400950000F03B010499D11101020102061000000000000000000000000000000000071EBDF0D2E7BDF0D2E700000000000000000199D4C141313233343500000000A0
            //UnixTime(Hex，4B，格式yyyyMMdd)
            int unixTime = (int) (System.currentTimeMillis() / 1000);
            //OBUId(Hex，4B)，可传null
            String hexObuId = "88060101";
            MyApplication.app.etcOptV2.searchTradeOBU(unixTime, hexObuId, 200, new ETCSearchTradeOBUListenerV2.Stub() {
                @Override
                public void onSuccess(Bundle bundle) throws RemoteException {
                    //设备编号（Hex，4B）
                    String deviceNo = bundle.getString("deviceNo");
                    //设备状态：bit7：0-卡片存在，1-卡片不存在；
                    //bit1：0-设备正常，1-设备失效（已拆卸）；
                    //bit0：0-电量正常，1-设备低电；
                    int deviceStatus = bundle.getInt("deviceStatus");
                    //系统信息-发行方标识(Hex，8Byte)
                    String sysInfoIssuerId = bundle.getString("sysInfoIssuerId");
                    //系统信息-合同序列号(Hex，8Byte)
                    String sysInfoContractNo = bundle.getString("sysInfoContractNo");
                    //0015File：0015文件内容(Hex,30B)
                    String file0015 = bundle.getString("0015File");
                    //0015文件-发卡方标识(Hex，8Byte)
                    String cardIssuerId0015 = bundle.getString("0015CardIssuerId");
                    // 0015文件-卡类型标志(Hex,1B)
                    String cardTypeId0015 = bundle.getString("0015CardTypeId");
                    // 0015文件-卡片版本号(Hex,1B)
                    String cardVersion0015 = bundle.getString("0015CardVersion");
                    // 0015文件-卡片网络标识(Hex，2Byte)
                    String cardNetId0015 = bundle.getString("0015CardNetId");
                    // 0015文件-卡片内部编号(Hex，8Byte)
                    String carInternalNo0015 = bundle.getString("0015CardInternalNo");
                    // 0015文件-启用时间(Hex，4B，格式YYYYMMDD)
                    String startDate0015 = bundle.getString("0015StartDate");
                    // 0015文件-到期时间(Hex，4B，格式YYYYMMDD)
                    String endDate0015 = bundle.getString("0015EtartDate");
                    //0015文件-车牌号码(Hex，12Byte)
                    String plateNo0015 = bundle.getString("0015PlateNo");
                    // 0015文件-用户类型(Hex，1B)，00-普通车，01-绑定OBU普通车，其他见GB/T20851.4
                    String userType0015 = bundle.getString("0015UserType");
                    // 0015文件-车牌颜色(Hex，1B)，00-蓝色，01-黄色，02-黑色，03-白色，09-蓝白渐变色
                    String plateColor0015 = bundle.getString("0015PlateColor");
                    // 0015文件-车型(Hex，1B)
                    String vehicleType0015 = bundle.getString("0015VehicleType");
                    String msg = "searchTradeOBU out:\n" + Utility.bundle2String(bundle, 2);
                    LogUtil.e(TAG, msg);
                    addText(msg);

                    //搜索OBU之后的处理
                    afterSearchTradeOBU();
                }

                @Override
                public void onError(int code) throws RemoteException {
                    String msg = "search searchTradeOBU failed,code:" + code;
                    LogUtil.e(TAG, msg);
                    addText(msg);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afterSearchTradeOBU() throws RemoteException {
        String msg = null;
        //2.获取安全信息密文
        //发送F1帧:131003018500F10B3B000000000000000000003A
        //应答F1帧:5A100401950000F15100E0E966ECEDEBB904A1025EBD17290202E886C655EA35B068E886C655EA35B068E886C655EA35B068E886C655EA35B068E886C655EA35B068E886C655EA35B06852FEFD3849E59E63000000000000000004
        Bundle bundle = new Bundle();
        // 期望获取到密文数据长度（1B，密文长度固定79字节）
        int expectLen = 0x3B;
        //云端产生的随机数（8B，若无，则传8字节 0）
        String hexRandom = "0000000000000000";
        //Mac密钥版本（1B，默认传0）
        int macKeyVersion = 0;
        //加密版本（1B，默认传0）
        int encryptVersion = 0;
        int code = MyApplication.app.etcOptV2.getTradeVehicleCipherInfo(expectLen, hexRandom, macKeyVersion, encryptVersion, bundle);
        if (code < 0) {
            msg = "getTradeVehicleCipherInfo failed,code:" + code;
            LogUtil.e(TAG, msg);
            addText(msg);
            return;
        }
        //所有返回数据(安全信息密文)
        String cipherInfo = bundle.getString("allRet");
        msg = "getTradeVehicleCipherInfo out:\n" + Utility.bundle2String(bundle, 2);
        LogUtil.e(TAG, msg);
        addText(msg);

        //3.获取卡片消费记录
        //发送F2帧:081003038500F20005
        //应答F2帧:3C100403950000F234090400C072FE082BAA290020170209143231A1A2A3A4A5A6A7A80000000000000000000000000000000000000000000000009E24
        bundle.clear();
        code = MyApplication.app.etcOptV2.getTradeRecord(bundle);
        if (code < 0) {
            msg = "getTradeRecord failed,code:" + code;
            LogUtil.e(TAG, msg);
            addText(msg);
            return;
        }
        //卡类型（1B）
        int cardType = bundle.getInt("cardType");
        //卡余额（4B）
        int balance = bundle.getInt("balance");
        //0019文件内容（交易记录文件，Hex，43B）
        String file0019 = bundle.getString("0019File");
        //所有返回数据(Hex，48B)
        String allRet = bundle.getString("allRet");
        msg = "getTradeRecord out:\n" + Utility.bundle2String(bundle, 2);
        LogUtil.e(TAG, msg);
        addText(msg);

        //4.消费初始化
        //发送F3帧:131003048500F30B01000000010000000000010F
        //应答F3帧:19100404950000F3100000C072FE4E3D00000001003DEA3FC66F
        //终端机编号（Hex，6B，PSAM卡序列号）
        String hexTerminalNo = "0137000371A0";
        bundle.clear();
        code = MyApplication.app.etcOptV2.initTrade(1, 0, hexTerminalNo, bundle);
        if (code < 0) {
            msg = "initTrade failed,code:" + code;
            LogUtil.e(TAG, msg);
            addText(msg);
            return;
        }
        //消费初始化应答数据
        // 电子存折或电子钱包旧余额（Hex，4B）
        String hexBalance = bundle.getString("balance");
        //电子存折或电子钱包脱机交易序号（Hex，2B）
        String offlineTradeNo = bundle.getString("offlineTradeNo");
        //透支限额（Hex，3B）
        String overdrawLimit = bundle.getString("overdrawLimit");
        //密钥版本号（Hex，1B）
        String keyVersion = bundle.getString("keyVersion");
        //算法标志（Hex，1B）
        String algorithmId = bundle.getString("algorithmId");
        //伪随机数（Hex，4B）
        String pseudorandomNum = bundle.getString("pseudorandomNum");
        //所有返回数据(Hex，15B)
        allRet = bundle.getString("allRet");
        msg = "initTrade out:\n" + Utility.bundle2String(bundle, 2);
        LogUtil.e(TAG, msg);
        addText(msg);

        //OBU保活测试
        code = MyApplication.app.etcOptV2.tradeHeartbeat();
        msg = "tradeHeartbeat code:" + code;
        LogUtil.e(TAG, msg);
        addText(msg);

        //5.复合消费
        //发送F4帧:461003048500F43EDC2BAA290020170313113836A1A2A3A4A5A6A7A8000000000000000000000000000000000000000000000000B9540F0000000100000000000000BB922A920C
        //应答F4帧:17100404950000F40EDC01005409004B6168DE7C4684408A
        //缓存数据（43B，0019文件内容）
        byte[] cacheData = ByteUtil.hexStr2Bytes("AA290020170313113836A1A2A3A4A5A6A7A8000000000000000000000000000000000000000000000000B9");
        //终端脱机交易序列号（4B）
        String hexTradeNo = "00000001";
        //交易日期（4B，格式yyyyMMdd）
        String hexTradeDate = "00000000";
        //交易时间（3B，格式 HHmmss）
        String hexTradeTime = "000000";
        //MAC1（4B）
        String hexMac = "BB922A92";
        bundle.clear();
        code = MyApplication.app.etcOptV2.complexTrade(cacheData, hexTradeNo, hexTradeDate, hexTradeTime, hexMac, bundle);
        if (code < 0) {
            msg = "complexTrade failed,code:" + code;
            LogUtil.e(TAG, msg);
            addText(msg);
            return;
        }
        //复合消费应答数据
        // TAC(Hex，4B)
        String tac = bundle.getString("tac");
        //Mac2(Hex，4B)
        String mac2 = bundle.getString("mac2");
        //所有返回数据(Hex，8B)
        allRet = bundle.getString("allRet");
        msg = "complexTrade out:\n" + Utility.bundle2String(bundle, 2);
        LogUtil.e(TAG, msg);
        addText(msg);

        //6.结束消费
        //发送FA帧:0A1003058500FA02000108
        //应答FA帧:0A100405950000FA010075
        code = MyApplication.app.etcOptV2.finishTrade(0);
        msg = "finishTrade code:" + code;
        LogUtil.e(TAG, msg);
        addText(msg);
    }

    private void addText(CharSequence msg) {
        String preMsg = result.getText().toString();
        runOnUiThread(() -> result.setText(TextUtils.concat(preMsg, "\n", msg + "\n")));
    }
}
