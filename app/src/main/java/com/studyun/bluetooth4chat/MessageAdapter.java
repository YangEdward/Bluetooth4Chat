package com.studyun.bluetooth4chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends BaseAdapter {

    private static final String TAG = MessageAdapter.class.getSimpleName();

    private List<Message> messages;

    private LayoutInflater inflater = null;

    public MessageAdapter(Context context, List<Message> messages) {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message entity = messages.get(position);
        int itemLayout = entity.getLayoutId();
        View view = convertView;
        ViewHolder holder;
        if(view == null){
            view = inflater.inflate(R.layout.list_item, parent,false);
            holder = new ViewHolder();
            holder.add = view.findViewById(R.id.add);
            holder.send = view.findViewById(R.id.send);
            holder.receive = view.findViewById(R.id.receive);
            holder.addTime = (TextView)view.findViewById(R.id.addTime);
            holder.sendName = (TextView)view.findViewById(R.id.sendBluetoothName);
            holder.sendContent = (TextView)view.findViewById(R.id.sendMessageContent);
            holder.sendTime = (TextView)view.findViewById(R.id.sendMessageTime);
            holder.receiveName = (TextView)view.findViewById(R.id.receiveBluetoothName);
            holder.receiveContent = (TextView)view.findViewById(R.id.receiveMessageContent);
            holder.receiveTime = (TextView)view.findViewById(R.id.receiveMessageTime);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }
        if (itemLayout == R.layout.list_add_in_time){
            holder.send.setVisibility(View.GONE);
            holder.receive.setVisibility(View.GONE);
            holder.addTime.setText(entity.getDeviceName());
        }else if(itemLayout == R.layout.list_say_he_item){
            holder.add.setVisibility(View.GONE);
            holder.receive.setVisibility(View.GONE);
            holder.sendTime.setText(entity.getDate());
            holder.sendContent.setText(entity.getContent());
            holder.sendName.setText(entity.getDeviceName());
        }else if(itemLayout == R.layout.list_say_me_item){
            holder.add.setVisibility(View.GONE);
            holder.send.setVisibility(View.GONE);
            holder.receiveTime.setText(entity.getDate());
            holder.receiveContent.setText(entity.getContent());
            holder.receiveName.setText(entity.getDeviceName());
        }
        return view;
    }

    static class ViewHolder{
        View add;
        View send;
        View receive;
        TextView addTime;
        TextView sendName;
        TextView sendContent;
        TextView sendTime;
        TextView receiveName;
        TextView receiveContent;
        TextView receiveTime;
    }
}
