package com.example.locateme;

import android.net.Uri;

class CreateUser {
    public CreateUser(){}
    public CreateUser(String username) {
        this.username = username;
    }

    public String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
