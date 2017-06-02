package me.hwang.vm.manager;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import me.hwang.vm.util.FilePathUtil;

/**
 * 音频录制管理
 */
public class AudioRecorderManager {

    // 音频的文件缓存路径
    private static final String AUDIO_CACHE_PATH = FilePathUtil.getAudioCachePath();

    private MediaRecorder mRecorder; // 用于录制语音的MediaRecorder对象
    private String mCurrentAudioFilePath; // 当前录制的音频文件对象的完成路径
    private String mCurrentAudioFileName; // 当前录制的音频文件的文件名

    private boolean mediaRecorderPrepared;  // MediaRecorder是否已成功进入prepared状态

    /**
     * MediaRecorder对象的状态监听
     */
    public interface AudioRecorderStateListener {
        void prepared();
    }

    private AudioRecorderStateListener mStateListener;

    public void setAudioRecorderStateListener(AudioRecorderStateListener Listener) {
        mStateListener = Listener;
    }

    private static AudioRecorderManager instance; // The single instance

    private AudioRecorderManager() {
        ensureAudioCache();
    }

    public static AudioRecorderManager getInstance() {
        synchronized (AudioRecorderManager.class) {
            if (instance == null)
                instance = new AudioRecorderManager();
        }

        return instance;
    }

    /**
     * 确保用于储存音频文件的文件夹已经得到创建
     */

    private void ensureAudioCache() {
        File file = new File(AUDIO_CACHE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * media recorder prepare
     */
    public void prepare() {
        mediaRecorderPrepared = false;

        mCurrentAudioFileName = generateFileName();
        mCurrentAudioFilePath = AUDIO_CACHE_PATH + "/" + mCurrentAudioFileName;

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mCurrentAudioFilePath);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();

        mediaRecorderPrepared = true;

        /*
         * callback activity or fragment the media recorder is start recording now
         */
        if (mStateListener != null)
            mStateListener.prepared();
    }

    /**
     * 随机生成音频文件名
     * @return
     */
    private String generateFileName() {
        return UUID.randomUUID() + ".amr";
    }

    /**
     * 结束录制，并释放资源
     */
    public void release() {
        mediaRecorderPrepared = false;
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    /**
     * 结束录制，并且取消音频文件的输出(delete)
     */
    public void cancel() {
        release();

        File file = new File(mCurrentAudioFilePath);

        if (file.exists())
            file.delete();

    }


    /**
     * 计算音量等级
     * @param maxLevel 最高等级
     * @return
     */
    public int getVoiceLevel(int maxLevel) {
        if(mediaRecorderPrepared){
           // mRecorder.getMaxAmplitude() return 0 ~ 32767 ...
           return maxLevel * mRecorder.getMaxAmplitude() / 32767 +1;
        }

        return 1;
    }

    /**
     * 获取当前录制的音频文件的文件名
     * @return
     */
    public String getCurrentFileName(){
        return mCurrentAudioFileName;
    }
}
