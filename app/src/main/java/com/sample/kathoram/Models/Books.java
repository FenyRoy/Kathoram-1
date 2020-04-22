package com.sample.kathoram.Models;

public class Books {

    private String name;
    private String time;

    public Books(String name, String time) {
        this.name = name;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }
}
