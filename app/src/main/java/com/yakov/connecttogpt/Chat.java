package com.yakov.connecttogpt;

public class Chat {
    String title;



    String chatHistory;
    public Chat(String title, String chatHistory){
        this.title = title;
        this.chatHistory = chatHistory;
    }

    public String getTitle() {
        return title;
    }
    public String getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(String chatHistory) {
        this.chatHistory = chatHistory;
    }

}
