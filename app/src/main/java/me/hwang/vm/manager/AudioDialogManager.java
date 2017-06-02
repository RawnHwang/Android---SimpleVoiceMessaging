package me.hwang.vm.manager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.hwang.vm.R;

/**
 * 语音录制时的提示对话框管理
 */
public class AudioDialogManager {
    // 对话框状态
    public static final int SLIDE_TO_CANCEL = 0; // 上滑，取消发送
    public static final int UP_DONE_CANCEL = 1;  // 松开，取消发送
    public static final int SPEAK_TOO_SHORT = 2; // 录制的时间过短
    public static final int UPDATE_VOICE_LEVEL = 3; // 更新实时音量等级

    // 对话框的文本提示内容
    private int[] hints = new int[]
            {R.string.str_dlg_recording_slide_to_cancel,
                    R.string.str_dlg_recording_up_done_cancel,
                    R.string.str_dlg_recording_length_to_short};

    // dialog对象
    private Dialog mDialog;

    private ImageView mIcon;  // 状态图标
    private ImageView mVoiceLevel; // 音量等级图标
    private TextView mHint;   // 文本状态提示

    private Context context;  // 上下文对象

    public AudioDialogManager(Context context) {
        this.context = context;
    }

    /**
     * 构建并显示对话框
     */
    public void showAudioDialog() {
        // 构建dialog对象
        mDialog = new Dialog(context, R.style.Theme_AudioDialog);
        // inflate 对象框的内容视图
        View contentView = LayoutInflater.from(context).inflate(R.layout.dlg_audio_recording, null);
        // 设置内容视图
        mDialog.setContentView(contentView);
        // 实例化对应的view对象
        mIcon = (ImageView) contentView.findViewById(R.id.iv_audio_dlg_icon);
        mVoiceLevel = (ImageView) contentView.findViewById(R.id.iv_audio_dlg_voice_level);
        mHint = (TextView) contentView.findViewById(R.id.tv_audio_dlg_hint);
        // 显示对话框
        mDialog.show();
    }

    /**
     * 更新dialog的信息显示
     *
     * @param behaviour 此次更新的行为
     */
    public void updateDialog(int behaviour) {
        updateDialog(behaviour, -1);
    }

    public void updateDialog(int behaviour, int voiceLevel) {
        if (mDialog != null && mDialog.isShowing()) {
            switch (behaviour) {
                // 上滑可以取消发送
                case SLIDE_TO_CANCEL:
                    mVoiceLevel.setVisibility(View.VISIBLE);
                    mIcon.setBackgroundResource(R.drawable.recorder);
                    mHint.setBackgroundColor(Color.TRANSPARENT);
                    break;
                // 松开手指，结束录音并取消发送
                case UP_DONE_CANCEL:
                    mVoiceLevel.setVisibility(View.GONE);
                    mIcon.setBackgroundResource(R.drawable.cancel);
                    mHint.setBackgroundColor(Color.RED);
                    break;
                // 说话的时间过短
                case SPEAK_TOO_SHORT:
                    mVoiceLevel.setVisibility(View.GONE);
                    mIcon.setBackgroundResource(R.drawable.voice_too_short);
                    break;
                // 更新音量等级
                case UPDATE_VOICE_LEVEL:
                    int resID = context.getResources().getIdentifier("voice_level_v" + voiceLevel, "drawable", context.getPackageName());
                    mVoiceLevel.setBackgroundResource(resID);
                    break;
            }
            // 更新提示信息
            if (behaviour != UPDATE_VOICE_LEVEL)
                mHint.setText(hints[behaviour]);
        }
    }

    /**
     * 隐藏dialog，释放资源
     */
    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

}
