package com.example.cpastone.models;

import android.util.Log;

public class ModelDoner {

    // variables
    String uid, id, name, condition, latitude, longitude, url, bookId, year, selectedBookTitle;
    long viewsCount;

    public ModelDoner(String uid, String id, String name, String condition, String latitude, String longitude, String url, String bookId, String year, String selectedBookTitle, long viewsCount) {
        this.uid = uid;
        this.id = id;
        this.name = name;
        this.condition = condition;
        this.latitude = latitude;
        this.longitude = longitude;
        this.url = url;
        this.bookId = bookId;
        this.year = year;
        this.selectedBookTitle = selectedBookTitle;
        this.viewsCount = viewsCount;
    }

    // empty constructor
    public ModelDoner() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSelectedBookTitle() {
        return selectedBookTitle;
    }

    public void setSelectedBookTitle(String selectedBookTitle) {
        this.selectedBookTitle = selectedBookTitle;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }
}

