package com.myproject.joy.bartapplication.model;

/**
 * Created by Richard2 on 3/5/2018.
 */

public class User {

    private String displayName;
    private String email;
    private String PhoneNumber;
    private String photoUri;
    private String userId;
    private String authToken;
    private String locationSharing;

    public User(String displayName, String userEmail, String phoneNumber, String photoUri, String userId,String authToken,String locationSharing) {
        this.displayName = displayName;
        this.email = userEmail;
        this.PhoneNumber = phoneNumber;
        this.photoUri = photoUri;
        this.userId=userId;
        this.authToken=authToken;
        this.locationSharing=locationSharing;
    }

    public String getLocationSharing() {
        return locationSharing;
    }

    public void setLocationSharing(String locationSharing) {
        this.locationSharing = locationSharing;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public User() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.PhoneNumber = phoneNumber;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
