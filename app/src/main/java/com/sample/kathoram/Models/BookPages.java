package com.sample.kathoram.Models;

public class BookPages {
    private String pageNo;
    private boolean isDefault;
    private String uriPath;
    private String pageType;

    public BookPages(String pageNo, boolean isDefault, String uriPath, String pageType) {
        this.pageNo = pageNo;
        this.isDefault = isDefault;
        this.uriPath = uriPath;
        this.pageType = pageType;
    }

    public String getPageNo() {
        return pageNo;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getUriPath() {
        return uriPath;
    }

    public String getPageType() {
        return pageType;
    }
}
