package com.sample.kathoram.Models;

public class BookPages {

    private String key;
    private String pageNo;
    private String uriPath;
    private String pageDesc;


    public BookPages(String key, String pageNo, String uriPath, String pageDesc) {
        this.key = key;
        this.pageNo = pageNo;
        this.uriPath = uriPath;
        this.pageDesc = pageDesc;
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

    public String getPageDesc() {
        return pageDesc;
    }
}
