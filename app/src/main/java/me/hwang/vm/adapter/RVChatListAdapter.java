package me.hwang.vm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import me.hwang.vm.R;
import me.hwang.vm.bean.chat.Message;

public class RVChatListAdapter extends RecyclerView.Adapter<RVChatListAdapter.ViewHolder> {

    // 音频消息的最小显示长度
    private int mMinAudioItemWidth;
    // 音频消息的最大显示长度
    private int mMaxAudioItemWidth;
    // 用于计算长度的增长基数
    private int growthBase;
    // 消息实体集合
    private List<Message> messageList;

    /**
     * 音频消息item-view的点击事件监听接口
     */
    public interface AudioItemOnClickListener {
        void onClick(View view,int position);
    }

    private AudioItemOnClickListener audioItemOnClickListener;

    public void setAudioItemOnClickListener(AudioItemOnClickListener listener) {
        audioItemOnClickListener = listener;
    }

    public RVChatListAdapter(Context context, List<Message> messageList) {
        this.messageList = messageList;

        growthBase = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.35f);
        mMaxAudioItemWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.5f);
        mMinAudioItemWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.15f);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = null;
        // 根据消息类型构造对应的view holder
        if (viewType == MessageTypeHelper.TYPE_TEXT_MSG_FROM) {
           // TODO 文本消息的ViewHolder对象构造(包括可以添加图片消息，视频消息等等)
        } else if (viewType == MessageTypeHelper.TYPE_TEXT_MSG_TO) {
           // TODO
        } else if (viewType == MessageTypeHelper.TYPE_AUDIO_MSG_FROM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_chat_list_from_type_audio, parent, false);
            holder = new ViewHolder(view, viewType);
        } else if (viewType == MessageTypeHelper.TYPE_AUDIO_MSG_TO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_chat_list_to_type_audio, parent, false);
            holder = new ViewHolder(view, viewType);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // 获取position处的消息实体对象
        Message message = messageList.get(position);
        // 获取position处的视图类型
        int viewType = getItemViewType(position);
        // 根据视图类型进行不同的视图赋值等操作
        if (viewType == MessageTypeHelper.TYPE_TEXT_MSG_TO || viewType == MessageTypeHelper.TYPE_TEXT_MSG_TO) { // 消息类型为文本

        } else if (viewType == MessageTypeHelper.TYPE_AUDIO_MSG_FROM || viewType == MessageTypeHelper.TYPE_AUDIO_MSG_TO) { // 消息类型为音频
            // 音频时长
            viewHolder.tvAudioSeconds.setText(message.getSeconds() + "\"");

            // 根据音频时长设置音频item的显示宽度
            ViewGroup.LayoutParams params = viewHolder.vgAudioItem.getLayoutParams();
            // 长度计算(以35秒作为标准，通过视频的长度按比例进行计算)
            int itemWidth = mMinAudioItemWidth + growthBase * message.getSeconds() / 35;
            // 如果计算出的长度超过设定的最大宽度，则使用最大长度作为最终宽度
            if (itemWidth > mMaxAudioItemWidth) {
                params.width = mMaxAudioItemWidth;
            } else {
                params.width = itemWidth;
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return MessageTypeHelper.getMessageType(messageList.get(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserIcon;   // 用户头像
        TextView tvTextMessage; // 文本消息
        ViewGroup vgAudioItem;  // 语音消息
        ImageView ivAudioTag;   // 语音标示
        TextView tvAudioSeconds;// 语音时长

        ViewHolder(View itemView, int viewType) {
            super(itemView);

            ivUserIcon = (ImageView) itemView.findViewById(R.id.iv_chat_user_icon);
            // 根据消息类型，进行对应的视图对象实例化工作
            if (viewType == MessageTypeHelper.TYPE_TEXT_MSG_TO || viewType == MessageTypeHelper.TYPE_TEXT_MSG_TO) { // 如果为文本信息

            } else if (viewType == MessageTypeHelper.TYPE_AUDIO_MSG_FROM || viewType == MessageTypeHelper.TYPE_AUDIO_MSG_TO) { // 如果为语音信息
                ivAudioTag = (ImageView) itemView.findViewById(R.id.iv_audio_tag);
                vgAudioItem = (ViewGroup) itemView.findViewById(R.id.rl_chat_audio_item);

                // 如果音频item的点击监听不为null，则为item-view添加点击事件
                if(audioItemOnClickListener!=null){
                    vgAudioItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            audioItemOnClickListener.onClick(v.findViewById(R.id.iv_audio_tag),getAdapterPosition());
                        }
                    });
                }

                tvAudioSeconds = (TextView) itemView.findViewById(R.id.tv_audio_seconds);
            }
        }
    }

    /**
     * 根据消息类型，计算其对应的item-view的viewType的内部工具类
     */
    private static class MessageTypeHelper {
        static final int TYPE_TEXT_MSG_FROM = 0;
        static final int TYPE_TEXT_MSG_TO = 1;
        static final int TYPE_AUDIO_MSG_FROM = 2;
        static final int TYPE_AUDIO_MSG_TO = 3;

        static int getMessageType(Message msg) {
            int fromTo = msg.getFromTo();
            if (fromTo == Message.MSG_FROM) {
                switch (msg.getType()) {
                    case Message.MSG_TYPE_TEXT:
                        return TYPE_TEXT_MSG_FROM;
                    case Message.MSG_TYPE_AUDIO:
                        return TYPE_AUDIO_MSG_FROM;
                }
            } else {
                switch (msg.getType()) {
                    case Message.MSG_TYPE_TEXT:
                        return TYPE_TEXT_MSG_TO;
                    case Message.MSG_TYPE_AUDIO:
                        return TYPE_AUDIO_MSG_TO;
                }
            }

            return -1;
        }
    }
}
