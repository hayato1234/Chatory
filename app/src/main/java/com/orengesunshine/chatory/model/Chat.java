package com.orengesunshine.chatory.model;

/**
 * individual chat entries
 */

public class Chat {

    private String name;
    private String date;
    private String time;
    private String message;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return message;
    }

    public void setText(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", text='" + message + '\'' +
                '}';
    }
}
