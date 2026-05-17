package com.hku.toiletguide.model;

public class User {
    public final String id;
    public final String displayName;
    public final String email;
    public final String role;

    public User(String id, String displayName, String email, String role) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
    }
}
