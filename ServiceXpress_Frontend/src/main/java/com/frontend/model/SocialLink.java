package com.frontend.model;

public class SocialLink {
    private String url;
    private String icon;

    public SocialLink(String url, String icon) {
        this.url = url;
        this.icon = icon;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}