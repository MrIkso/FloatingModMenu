package com.mrikso.modmenu;

import android.app.ActivityManager;

class Util {

    static float getConvertetValue(int intVal) {
        float value;
        value = intVal * 0.1f;
        return value;
    }

    static boolean isAppBackground() {
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }
}
