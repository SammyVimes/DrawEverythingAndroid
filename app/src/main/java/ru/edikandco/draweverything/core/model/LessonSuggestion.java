package ru.edikandco.draweverything.core.model;

/**
 * Created by Semyon on 12.04.2015.
 */
public class LessonSuggestion {

    private String title;

    private String link;

    public LessonSuggestion(final String title, final String link) {
        this.title = title;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(final String link) {
        this.link = link;
    }

}