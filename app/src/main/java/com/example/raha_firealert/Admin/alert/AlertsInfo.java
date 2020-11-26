package com.example.raha_firealert.Admin.alert;

public class AlertsInfo {
    String id,name,address,date;

    public AlertsInfo(String id, String name, String address, String date) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
