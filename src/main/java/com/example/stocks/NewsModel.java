package com.example.stocks;

public class NewsModel {
    String newsSource;
    String hoursElapsed;
    String newsTitle;
    String imageSrc;
    String dateStr;
    String content;
    String newsLink;



    public NewsModel(String newsSource, String hoursElapsed, String newsTitle, String imageSrc, String dateStr, String content, String newsLink){
        this.newsSource = newsSource;
        this.newsTitle = newsTitle;
        this.hoursElapsed = hoursElapsed;
        this.imageSrc = imageSrc;
        this.dateStr = dateStr;
        this.content = content;
        this.newsLink = newsLink;
    }


    public String getNewsSource() {
        return newsSource;
    }

    public String getHoursElapsed() {
        return hoursElapsed;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public String getDateStr() {
        return dateStr;
    }

    public String getContent() {
        return content;
    }

    public String getNewsLink() {
        return newsLink;
    }

}
