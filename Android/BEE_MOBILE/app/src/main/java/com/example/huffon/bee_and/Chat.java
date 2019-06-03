package com.example.huffon.bee_and;

/**
 * Created by Huffon on 6/1/2019.
 */

public class Chat {
    public String email;
    public String text;

    public Chat() {
    }

    public Chat(String text) {
        this.text = text;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
