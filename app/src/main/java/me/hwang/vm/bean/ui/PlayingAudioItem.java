package me.hwang.vm.bean.ui;

import android.view.View;

public class PlayingAudioItem {
    private View view;
    private int msgFromTo;
    private String audioName;

    public PlayingAudioItem(View view, int msgFromTo,String audioName) {
        this.view = view;
        this.msgFromTo = msgFromTo;
        this.audioName = audioName;
    }

    public View getView() {
        return view;
    }

    public int getMsgFromTo() {
        return msgFromTo;
    }

    public String getAudioName() {
        return audioName;
    }
}
