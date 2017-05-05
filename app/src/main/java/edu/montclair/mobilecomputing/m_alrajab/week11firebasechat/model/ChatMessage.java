package edu.montclair.mobilecomputing.m_alrajab.week11firebasechat.model;

/**
 * Created by m_alrajab on 4/3/17.
 */

public class ChatMessage {
    private String msg;
    private String username;
    private String msgTime;
    private String photoUrl;


    public ChatMessage(){

    }
    public ChatMessage(String msg, String username) {
        this.msg = msg;
        this.username = username;
    }

    public ChatMessage(String msg, String username, String time) {
        this.msg = msg;
        this.username = username;
        this.msgTime=time;
    }

    public ChatMessage(String msg, String username, String time, String photoUri) {
        this.msg = msg;
        this.username = username;
        this.msgTime=time;
        this.photoUrl=photoUri;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUsername() {
        return username;
    }
    public String getUri() {
        return photoUrl;
    }

    public String getMsgTime() {
        return msgTime;
    }


    public void setUsername(String username) {
        this.username = username;
    }
}
