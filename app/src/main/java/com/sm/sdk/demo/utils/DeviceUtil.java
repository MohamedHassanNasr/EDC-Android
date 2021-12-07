package com.sm.sdk.demo.utils;

import android.os.Build;
import android.text.TextUtils;

public final class DeviceUtil {
    private DeviceUtil() {
        throw new AssertionError("create instance of DeviceUtil is prohibited");
    }

    /** 是否是P1N */
    public static boolean isP1N() {
        String model = getModel();
        return model.matches("p1n.*");
    }

    /** 是否是P1_4G */
    public static boolean isP14G() {
        String model = getModel();
        return model.matches("p1\\(4g\\).*");
    }

    /** 是否是P2lite */
    public static boolean isP2Lite() {
        String model = getModel();
        return model.matches("p2lite.*");
    }

    /** 是否是P2_PRO */
    public static boolean isP2Pro() {
        String model = getModel();
        return model.matches("p2_pro.*");
    }

    /** 是否是P2 */
    public static boolean isP2() {
        String model = getModel();
        return model.matches("p2.*");
    }

    /** 获取model */
    private static String getModel() {
        String model = SystemPropertiesUtil.get("ro.sunmi.hardware");
        if (TextUtils.isEmpty(model)) {
            model = Build.UNKNOWN;
        }
        return model.toLowerCase();
    }
}
