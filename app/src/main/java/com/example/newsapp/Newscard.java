package com.example.newsapp;

public class Newscard {
    private String image_url;
    private String news_title;
    private String news_time;
    private String news_section;
    private String news_id;
    private String news_url;

    public Newscard(String image_url, String news_title, String news_time, String news_section, String news_id, String news_url){
        this.image_url = image_url;
        this.news_title = news_title;
        this.news_time = news_time;
        this.news_section = news_section;
        this.news_id = news_id;
        this.news_url = news_url;
    }

    public String getNews_url() {
        return news_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getNews_title() {
        return news_title;
    }

    public String getNews_time() {
        return news_time;
    }

    public String getNews_section() {
        return news_section;
    }

    public String getNews_id() {
        return news_id;
    }
}
