package ru.edikandco.draweverything.core.model;

/**
 * Created by Semyon on 07.04.2015.
 */
public class Lesson {

    private String coverURI;

    private String title;

    private int complexity;

    private int rating;

    private int views;

    private int id;
    
    private int steps;

    public String getCoverURI() {
        return coverURI;
    }

    public void setCoverURI(final String coverURI) {
        this.coverURI = coverURI;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(final int complexity) {
        this.complexity = complexity;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(final int rating) {
        this.rating = rating;
    }

    public int getViews() {
        return views;
    }

    public void setViews(final int views) {
        this.views = views;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public void setSteps(final int steps) {
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }
}