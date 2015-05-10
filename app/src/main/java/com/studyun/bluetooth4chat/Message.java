package com.studyun.bluetooth4chat;

import android.bluetooth.BluetoothDevice;


public class Message {

    private final static int ADD = 0;
    private final static int RECEIVED = 1;
    public final static int SEND = 2;

    private String deviceName;
    private String date;
    private String content;
    private int type;

    public Message(String deviceName, String date, String content, int type) {
        this.deviceName = deviceName;
        this.date = date;
        this.content = content;
        if(type < ADD || type > SEND){
            throw new IllegalArgumentException("");
        }
        this.type = type;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String device) {
        this.deviceName = device;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLayoutId() {
        if(type == ADD){
            return R.layout.list_add_in_time;
        }else if (type == RECEIVED){
            return R.layout.list_say_me_item;
        }else if (type == SEND){
            return R.layout.list_say_he_item;
        }
        return 0;
    }
}
