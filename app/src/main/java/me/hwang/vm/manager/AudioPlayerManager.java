package me.hwang.vm.manager;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

import me.hwang.vm.util.FilePathUtil;

/**
 * 音频播放管理
 */
public class AudioPlayerManager {

    // 音频的文件缓存路径
    private static final String AUDIO_CACHE_PATH = FilePathUtil.getAudioCachePath();
    // 用于播放音频的MediaPlayer对象
    private static MediaPlayer player;
    // 当前播放的音频是否处于暂停播放的状态
    private static boolean isPlayerPause;

    /**
     * 播放音频
     * @param fileName 音频文件的文件名
     * @param onCompletionListener 音频播放完成的回调
     */
    public static void play(String fileName, MediaPlayer.OnCompletionListener onCompletionListener) {
        if (player == null) {
            player = new MediaPlayer();
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mp.reset();
                    return false;
                }
            });
        } else {
            player.reset();
        }
        // 设置音频流的类型
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // 设置播放完成的回调监听
        player.setOnCompletionListener(onCompletionListener);
        try {
            // 设置数据源
            player.setDataSource(AUDIO_CACHE_PATH + fileName);
            // prepare the media player
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 开始播放
        player.start();
    }

    /**
     * 暂停音频的播放
     */
    public static void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
            isPlayerPause = true;
        }
    }

    /**
     * 继续音频的播放
     */
    public static void resume() {
        if (player != null && isPlayerPause) {
            player.start();
            isPlayerPause = false;
        }
    }

    /**
     * 释放资源
     */
    public static void release() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }

            player.release();
            player = null;
        }
    }
}
