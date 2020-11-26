package com.example.raha_firealert.User.ui.announcement;

public class AnnouncementInfo {
    String title,detailes,image,created_at;

//    public AnnouncementInfo(String title, String detailes, String created_at) {
//        this.title = title;
//        this.detailes = detailes;
//        this.created_at = created_at;
//    }

    public AnnouncementInfo(String title, String detailes, String image, String created_at) {
        this.title = title;
        this.detailes = detailes;
        this.image = image;
        this.created_at = created_at;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetailes() {
        return detailes;
    }

    public void setDetailes(String detailes) {
        this.detailes = detailes;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
