package me.hwang.vm.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Activity基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected final String LOG_TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutID());
        initVariables();
        initViews();
        loadData();
    }

    /**
     * 初始化activity所需的变量
     * (例如活动内的实例变量，从intent获取的变量等)
     */
    protected abstract void initVariables();

    /**
     * 加载布局；初始化控件；设置监听等.
     */
    protected abstract void initViews();

    /**
     * 读取从API获取的数据等操作
     */
    protected abstract void loadData();

    /**
     * 获取界面布局文件id
     */
    protected abstract int getLayoutID();
}
