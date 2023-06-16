package com.example.dbaproject.api.models;

// Response from backend after registration or authorization
public final class UserAuthResp {
    public String username;
    public Tokens tokens;

    public static final class Tokens{
        public String refresh;
        public String access;
    }
}
