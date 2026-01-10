package com.example.flightlab;

public class Question {
    String text;
    String a, b, c, d;
    int correct;
    String imageUrl;
    String audioUrl;

    public Question(String text, String a, String b, String c, String d, int correct, String imageUrl, String audioUrl)
    {
        this.text = text;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.correct = correct;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
    }
}
