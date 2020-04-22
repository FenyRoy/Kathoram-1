package com.sample.kathoram.Models;

public class Books {

    private String bookid;
    private String name;
    private String time;

    public Books(String bookid, String name, String time) {
        this.bookid = bookid;
        this.name = name;
        this.time = time;
    }

    public String getBookid() {
        return bookid;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }
}
