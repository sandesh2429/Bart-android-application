package com.myproject.joy.bartapplication.model;

/**
 * Created by Vamshik on 3/5/2018.
 */

public class UserLocation {

    private String displayName;
    private Double longitude;
    private Double latitude;
    private String userId;
    private String photoUri;

    public UserLocation(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public UserLocation() {
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}
