package com.orengesunshine.chatory.model;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * group of chat (chat room)
 */

public class ChatRoom {

    private String[] names;
    private String savedOn;
    private ArrayList<Chat> chats;
    private String lastChatDate;
    private String lastChatMessage;

    public String getLastChatDate() {
        return lastChatDate;
    }

    public void setLastChatDate(String lastChatDate) {
        this.lastChatDate = lastChatDate;
    }

    public String getLastChatMessage() {
        return lastChatMessage;
    }

    public void setLastChatMessage(String lastChatMessage) {
        this.lastChatMessage = lastChatMessage;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public String getSavedOn() {
        return savedOn;
    }

    public void setSavedOn(String savedOn) {
        this.savedOn = savedOn;
    }

    public ArrayList<Chat> getChats() {
        return chats;
    }

    public void setChats(ArrayList<Chat> chats) {
        this.chats = chats;
    }

    @Override
    public String toString() {
        return "ChatHistory{" +
                "names=" + Arrays.toString(names) +
                ", savedOn='" + savedOn + '\'' +
                '}';
    }
}
