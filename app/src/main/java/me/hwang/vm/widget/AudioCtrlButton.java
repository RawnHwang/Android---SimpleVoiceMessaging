package me.hwang.vm.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

import me.hwang.vm.R;
import me.hwang.vm.manager.AudioDialogManager;
import me.hwang.vm.manager.AudioRecorderManager;

/**
 * 录音控制按钮
 */
public class AudioCtrlButton extends AppCompatButton {

    // 按钮状态
    private static final int STATE_NORMAL = 0;  // 通常状态
    private static final int STATE_RECORDING = 1; // 录音状态
    private static final int STATE_WANT_TO_CANCEL = 2; // 视图取消
    private int mCurrentState = STATE_NORMAL;  // 按钮当前状态

    private boolean isRecording; // 是否已经开始录音
    private int mSpeakTime; // 已录音时长

    private AudioDialogManager mDlgManager; // 录音提示对话框管理
    private AudioRecorderManager mRecorderManager; // 音频录制管理

    // 录制结束的监听回调接口
    public interface OnRecordingCompletionListener {
        void onCompleted(String fileName, int seconds);
    }

    private OnRecordingCompletionListener mCompletionListener;

    public void setOnRecordingCompletedListener(OnRecordingCompletionListener listener) {
        mCompletionListener = listener;
    }

    private Handler mHandler;

    public static final int MSG_UPDATE_VOICE_LEVEL = 1; // 更新音量等级
    public static final int MSG_SPEAK_TOO_SHORT = 2;  // 提示录制时间过短

    private static class AudioDialogHandler extends Handler {
        private WeakReference<AudioCtrlButton> mAudioButton;

        AudioDialogHandler(AudioCtrlButton button) {
            mAudioButton = new WeakReference<>(button);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_VOICE_LEVEL:
                    // 获取音量等级
                    int voiceLevel = mAudioButton.get().mRecorderManager.getVoiceLevel(7);
                    // 更新对话框的音量等级显示
                    mAudioButton.get().mDlgManager.updateDialog(AudioDialogManager.UPDATE_VOICE_LEVEL, voiceLevel);
                    break;
                case MSG_SPEAK_TOO_SHORT:
                    mAudioButton.get().mDlgManager.dismissDialog();
                    break;
            }
        }
    }

    public AudioCtrlButton(Context context) {
        this(context, null);
    }

    public AudioCtrlButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // handler实例化
        mHandler = new AudioDialogHandler(this);
        // 对话匡管理对象实例化
        mDlgManager = new AudioDialogManager(context);
        // 音频录制管理对象实例化
        mRecorderManager = AudioRecorderManager.getInstance();
        // 设置media recorder对象的状态监听
        mRecorderManager.setAudioRecorderStateListener(new AudioRecorderStateListener());
        // 设置长按监听
        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 长按事件触发MediaRecorder对象的构建
                mRecorderManager.prepare();
                return false;
            }
        });
    }

    /**
     * Recorder状态监听
     */
    private class AudioRecorderStateListener implements AudioRecorderManager.AudioRecorderStateListener {

        @Override
        public void prepared() {
            // 更新状态
            updateState(STATE_RECORDING);
            // 展示录音对话框
            mDlgManager.showAudioDialog();
            // 新开线程，定时获取音量变化并更新对话框
            updateVoiceLevelAsync();
        }
    }

    private void updateVoiceLevelAsync() {
        new Thread(new getVoiceLevelTask()).start();
    }

    public class getVoiceLevelTask implements Runnable {

        @Override
        public void run() {
            // 只有处于录制状态下，才有需要获取音量等级
            while (isRecording) {
                // 每隔80ms获取一次
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 通过handler发送消息去获取最新音量等级，并更新对话框
                mHandler.sendEmptyMessage(MSG_UPDATE_VOICE_LEVEL);
                // 累加录制时长
                mSpeakTime += 80;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundResource(R.drawable.bg_btn_audio_ctrl_pressed);
                break;

            case MotionEvent.ACTION_MOVE:
                if (isRecording) { // 只有当处于录制的状态中，才有必要响应move事件
                    int y = (int) event.getY();

                    if (wantToCancel(y)) { // 根据y值判断此次事件是否触发STATE_WANT_TO_CANCEL状态
                        // 如果按钮状态已经为STATE_WANT_TO_CANCEL,那么及时手指继续上滑也没必要再更新状态
                        if (mCurrentState != STATE_WANT_TO_CANCEL)
                            updateState(STATE_WANT_TO_CANCEL);
                    } else {
                        if (mCurrentState != STATE_RECORDING) //道理同上
                            updateState(STATE_RECORDING);
                    }
                }
                break;

            case MotionEvent.ACTION_UP: // 当手指离开按钮
                if (!isRecording) { // 如果还未进入录制状态，则只需重置按钮的状态
                    setBackgroundResource(R.drawable.bg_btn_audio_ctrl_normal);
                    reset();
                } else { // 如果已经进入录制状态（即MediaRecorder已经prepare完毕，开始录制音频）

                    if (mSpeakTime <= 1000) { // 首先判断录制时长是否大于1秒
                        // 结束录制，并取消音频文件的输出
                        mRecorderManager.cancel();
                        // 更新对话框，提示用户说话时间过短
                        mDlgManager.updateDialog(AudioDialogManager.SPEAK_TOO_SHORT);
                        reset();
                        // 提示1800ms后，重置按钮状态以及dismiss对话框
                        mHandler.sendEmptyMessageDelayed(MSG_SPEAK_TOO_SHORT, 1800);
                    } else {// 如果录制时长足够

                        if (mCurrentState == STATE_WANT_TO_CANCEL) { // 判断状态是否为STATE_WANT_TO_CANCEL
                            // 结束录制，并取消发送
                            mRecorderManager.cancel();
                        } else if (mCurrentState == STATE_RECORDING) {
                            // 结束录制，释放资源
                            mRecorderManager.release();
                            if (mCompletionListener != null) {
                                // 通过回调通知activity(fragment)录制完成，并传入此次录制的音频文件名及录制时长
                                mCompletionListener.onCompleted(mRecorderManager.getCurrentFileName(), Math.round(mSpeakTime / 1000));
                            }
                        }

                        mDlgManager.dismissDialog(); // 结束对话框的显示
                        reset(); // 重置按钮状态
                    }
                }

                break;
        }
        return super.onTouchEvent(event);
    }

    // 重置
    private void reset() {
        // 将按钮状态重新初始化至STATE_NORMAL
        updateState(STATE_NORMAL);
        // 重置是否处于录音状态的标识
        isRecording = false;
        // 将录制时长清零
        mSpeakTime = 0;
    }

    // 按钮的状态更新
    private void updateState(int state) {
        if (state != mCurrentState) {
            switch (state) {
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.bg_btn_audio_ctrl_normal);
                    setText(R.string.str_btn_audio_normal);
                    break;

                case STATE_RECORDING:
                    if (mCurrentState == STATE_NORMAL)
                        setBackgroundResource(R.drawable.bg_btn_audio_ctrl_pressed);

                    isRecording = true;
                    setText(R.string.str_btn_audio_recording);
                    mDlgManager.updateDialog(AudioDialogManager.SLIDE_TO_CANCEL);
                    break;

                case STATE_WANT_TO_CANCEL:
                    if (mCurrentState == STATE_NORMAL)
                        setBackgroundResource(R.drawable.bg_btn_audio_ctrl_pressed);

                    setText(R.string.str_btn_audio_want_to_cancel);
                    mDlgManager.updateDialog(AudioDialogManager.UP_DONE_CANCEL);
                    break;
            }

            mCurrentState = state;
        }
    }


    // 根据y值计算是否视图取消发送
    private boolean wantToCancel(int y) {
        return y < -(getBottom() - getHeight());
    }
}
