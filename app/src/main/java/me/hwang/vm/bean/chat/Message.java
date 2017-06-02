package me.hwang.vm.bean.chat;

/**
 * 消息实体类
 */
public class Message {
    public static final int MSG_FROM = 0;
    public static final int MSG_TO = 1;

    public static final int MSG_TYPE_TEXT = 0;
    public static final int MSG_TYPE_AUDIO = 1;

    private int fromTo;     // 是接受的/还是发送的消息
    private int type;       // 消息类型(文本/音频/图片...)
    private String time;    // 消息发送的时间
    private String content; // 消息内容
    private int seconds;    // 音频消息的时长

    public Message(int fromTo, int type, String time, String content, int seconds) {
        this.fromTo = fromTo;
        this.type = type;
        this.time = time;
        this.content = content;
        this.seconds = seconds;
    }

    public int getFromTo() {
        return fromTo;
    }

    public void setFromTo(int fromTo) {
        this.fromTo = fromTo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
}
