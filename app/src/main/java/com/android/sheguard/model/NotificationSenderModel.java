package com.android.sheguard.model;

@SuppressWarnings("all")
public class NotificationSenderModel {
    private Message message;

    public NotificationSenderModel(String token, NotificationDataModel data) {
        this.message = new Message(token, data);
    }

    public static class Message {
        private String token;
        private Notification notification;
        private Data data;

        public Message(String token, NotificationDataModel data) {
            this.token = token;
            this.notification = new Notification(data.getTitle(), data.getBody());
            this.data = new Data(data.getTitle(), data.getBody());
        }
    }

    public static class Notification {
        private String title;
        private String body;

        public Notification(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }

    public static class Data {
        private String title;
        private String body;

        public Data(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }
}
