package com.softonetech.learnenglish;

class RssFeedModel {

    public String description;
    public String link;
    public String title;
    public String imgUrl;

    public RssFeedModel(String title, String link, String description, String imgUrl) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.imgUrl = imgUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {

        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

}
