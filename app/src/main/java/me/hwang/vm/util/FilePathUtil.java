package me.hwang.vm.util;

import android.os.Environment;

import me.hwang.vm.app.MyApplication;

public class FilePathUtil {

    public static String getAudioCachePath() {
        return Environment.getExternalStorageDirectory().getPath() + "/" + MyApplication.getGlobalContext().getPackageName() + "/audio/";
    }
}
