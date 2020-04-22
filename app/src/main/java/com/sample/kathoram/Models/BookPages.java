package com.sample.kathoram.Models;

public class BookPages {

    private String key;
    private String pageNo;
    private String uriPath;

    public BookPages(String key, String pageNo, String uriPath) {
        this.key = key;
        this.pageNo = pageNo;
        this.uriPath = uriPath;
    }

    public String getKey() {
        return key;
    }

    public String getPageNo() {
        return pageNo;
    }

    public String getUriPath() {
        return uriPath;
    }
}
