package com.frontend.model;

public class Review {
    private String author;
    private String starsHtml;
    private String text;

    public Review(String author, String starsHtml, String text) {
        this.author = author;
        this.starsHtml = starsHtml;
        this.text = text;
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getStarsHtml() { return starsHtml; }
    public void setStarsHtml(String starsHtml) { this.starsHtml = starsHtml; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}