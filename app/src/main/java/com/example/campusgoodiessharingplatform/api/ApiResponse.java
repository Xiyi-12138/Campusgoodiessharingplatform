package com.example.campusgoodiessharingplatform.api;

public class ApiResponse<T> {
    public String code;
    public String msg;
    public T data;

    public boolean ok() {
        return "200".equals(code);
    }
}
