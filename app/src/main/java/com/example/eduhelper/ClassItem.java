package com.example.eduhelper;

public class ClassItem {
    String className, notesLink, questionPaperLink, videoLink;

    public ClassItem(String className, String notesLink, String questionPaperLink, String videoLink) {
        this.className = className;
        this.notesLink = notesLink;
        this.questionPaperLink = questionPaperLink;
        this.videoLink = videoLink;
    }

    public String getClassName() { return className; }
    public String getNotesLink() { return notesLink; }
    public String getQuestionPaperLink() { return questionPaperLink; }
    public String getVideoLink() { return videoLink; }
}
