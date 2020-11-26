package com.example.raha_firealert.User.ui.safetytips;

public class TipInfo {
    String subject,content,created_at;

    public TipInfo(String subject, String content, String created_at) {
        this.subject = subject;
        this.content = content;
        this.created_at = created_at;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
