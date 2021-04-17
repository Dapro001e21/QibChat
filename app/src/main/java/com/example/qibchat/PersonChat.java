package com.example.qibchat;

public class PersonChat {
    int Image;
    String Name;
    String message;

    public PersonChat(int image, String name, String message) {
        Image = image;
        Name = name;
        this.message = message;
    }

    public int getImage() {
        return Image;
    }

    public void setImage(int image) {
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
