package me.hwang.vm.app;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application{

    private static Context globalContext;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = getApplicationContext();
    }

    public static Context getGlobalContext() {
        return globalContext;
    }

    public static void setGlobalContext(Context globalContext) {
        MyApplication.globalContext = globalContext;
    }
}
