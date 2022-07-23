package com.example.whatsup;

public class Messages {

    private String message, type, from, to, messageID, time, date;
    //private long time;
    private boolean seen;

    public Messages() { }

    public Messages(String message, String type, String from, String to, String messageID, String time,String date, boolean seen) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.to = to;
        this.messageID = messageID;
        this.time = time;
        this.date = date;
        this.seen = seen;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    //    public long getTime() {
//        return time;
//    }
//
//    public void setTime(long time) {
//        this.time = time;
//    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
