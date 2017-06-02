package me.hwang.vm.ui;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import me.hwang.vm.R;
import me.hwang.vm.adapter.RVChatListAdapter;
import me.hwang.vm.bean.chat.Message;
import me.hwang.vm.bean.ui.PlayingAudioItem;
import me.hwang.vm.manager.AudioPlayerManager;
import me.hwang.vm.widget.AudioCtrlButton;

public class MainActivity extends BaseActivity {
    // 录音控制按钮
    private AudioCtrlButton btnAudioCtrl;
    // chat list recycler view
    private RecyclerView rvChatList;
    // RecyclerView adapter
    private RVChatListAdapter mRVAdapter;
    // 消息集合
    private List<Message> messageList;
    // 正在播放的音频tag item
    private PlayingAudioItem playingItem;
    // 用于模拟消息fromTo的变量
    private int autoFromTo = 0;

    @Override
    protected void initVariables() {
        messageList = new ArrayList<>();
        mRVAdapter = new RVChatListAdapter(this, messageList);
        // 设置音频item点击事件监听
        mRVAdapter.setAudioItemOnClickListener(new AudioItemOnClickListener());
    }

    @Override
    protected void initViews() {
        btnAudioCtrl = (AudioCtrlButton) findViewById(R.id.btn_audio_control);

        btnAudioCtrl.setOnRecordingCompletedListener(new AudioCtrlButton.OnRecordingCompletionListener() {
            @Override
            public void onCompleted(String fileName, int seconds) {
                Message message = new Message(++autoFromTo % 2, Message.MSG_TYPE_AUDIO, "", fileName, seconds);
                messageList.add(message);
                mRVAdapter.notifyItemInserted(messageList.size()-1);
                rvChatList.smoothScrollToPosition(messageList.size()-1);
            }
        });

        rvChatList = (RecyclerView) findViewById(R.id.rv_chat_list);
        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        rvChatList.setAdapter(mRVAdapter);
    }

    @Override
    protected void loadData() {

    }


    private class AudioItemOnClickListener implements RVChatListAdapter.AudioItemOnClickListener{

        @Override
        public void onClick(View view, int position) {
            // 获取对应position的消息实体
            Message message = messageList.get(position);

            if (playingItem != null) { // 如果有正在播放的音频
                String playingAudioFileName = playingItem.getAudioName();
                /*
                 * 此时分为两种情况，一种情况为：用户再次点击了正在播放的音频消息(试图停止);
                 * 另外一种情况则是用户在某条音频消息还未播放完毕的时候，点击了另一条音频消息(切换播放);
                 * 但是，无论是二者之中任一情况，都需要停止正在播放的音频消息的播放动画和置空对象的工作
                 */
                playingItemReset();

                // 此判断意味用户点击了正在播放的音频
                if(playingAudioFileName.equals(message.getContent())){
                    // 停止播放，并释放资源
                    AudioPlayerManager.release();
                    return; // 直接返回，不再继续其他代码逻辑
                }
            }

            /*
             * 以下代码得以执行，则意味着需要切换播放新的音频；
             * 或者：用户点击了全新的一条音频，即点击该音频消息时，并没有其它正在播放的音频
             */

            // 首先获取此次点击的音频消息的相关信息
            int msgFromTo = message.getFromTo();
            String audioName = message.getContent();
            // 从而构建新的playingItem对象
            playingItem = new PlayingAudioItem(view,msgFromTo,audioName);
            // 设置对应的播放动画
            if (msgFromTo == Message.MSG_FROM) {
                playingItem.getView().setBackgroundResource(R.drawable.anim_chat_from_audio_play);
            } else {
                playingItem.getView().setBackgroundResource(R.drawable.anim_chat_to_audio_play);
            }
            // 开始播放动画
            ((AnimationDrawable) playingItem.getView().getBackground()).start();

            // 播放音频，并设置播放完成的回调处理
            AudioPlayerManager.play(messageList.get(position).getContent(), new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playingItemReset();
                }
            });
        }
    }

    private void playingItemReset(){
        // 结束播放动画，设置回普通背景
        if (playingItem.getMsgFromTo() == Message.MSG_FROM) {
            playingItem.getView().setBackgroundResource(R.drawable.icon_audio_from);
        } else {
            playingItem.getView().setBackgroundResource(R.drawable.icon_audio_to);
        }
        // 置空对象
        playingItem = null;
    }
    @Override
    protected int getLayoutID() {
        return R.layout.activity_main;
    }

    @Override
    protected void onPause() {
        super.onPause();
        AudioPlayerManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AudioPlayerManager.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioPlayerManager.release();
    }
}
