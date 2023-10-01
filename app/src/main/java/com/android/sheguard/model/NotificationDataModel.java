package com.android.sheguard.model;

@SuppressWarnings("unused")
public class NotificationDataModel {

    private String title;
    private String body;

    public NotificationDataModel(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
