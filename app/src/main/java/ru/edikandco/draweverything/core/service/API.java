package ru.edikandco.draweverything.core.service;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ru.edikandco.draweverything.core.http.HttpBytesReader;
import ru.edikandco.draweverything.core.http.HttpRequestException;
import ru.edikandco.draweverything.core.model.Lesson;
import ru.edikandco.draweverything.core.model.LessonSuggestion;
import ru.edikandco.draweverything.core.util.IoUtils;
import ru.edikandco.draweverything.core.util.ServiceContainer;
import ru.edikandco.draweverything.core.util.Utils;

/**
 * Created by Semyon on 07.04.2015.
 */
public class API {

    private static final String BASE_NEW_LESSONS_URI = "http://howtodraw.azurewebsites.net/HowToDraw/API/lessons/${PAGE}?sort=NEW";
    private static final String SUGGESTIONS_URI = "http://howtodraw.azurewebsites.net/HowToDraw/API/hints/?q=";
    private static final String BASE_LESSONS_COVER_URI = "http://howtodraw.azurewebsites.net/HowToDraw/API/lesson_prev/";

    public List<Lesson> getLessons(final int page) throws Exception {
        List<Lesson> lessons = null;

        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        if (httpBytesReader != null) {
            Exception ex = null;
            try {
                String uri = BASE_NEW_LESSONS_URI;
                uri = uri.replace("${PAGE}", "" + page);
                byte[] response = httpBytesReader.fromUri(uri);
                String responseString = IoUtils.convertBytesToString(response);
                lessons = parseNewLessonsResponse(Utils.toJSON(responseString));
            } catch (HttpRequestException e) {
                ex = e;
            } catch (JSONException e) {
                ex = e;
            }
            if (ex != null) {
                throw new Exception(ex.getMessage());
            }
        }
        return lessons;
    }

    public List<LessonSuggestion> getSuggestions(final String query) throws Exception {
        List<LessonSuggestion> suggestions = null;

        HttpBytesReader httpBytesReader = ServiceContainer.getService(HttpBytesReader.class);
        if (httpBytesReader != null) {
            Exception ex = null;
            try {
                String uri = SUGGESTIONS_URI + URLEncoder.encode(query, Charset.forName(HTTP.UTF_8).name());;
                byte[] response = httpBytesReader.fromUri(uri);
                String responseString = IoUtils.convertBytesToString(response);
                suggestions = parseSuggestionsResponse(Utils.toJSON(responseString));
            } catch (HttpRequestException e) {
                ex = e;
            } catch (JSONException e) {
                ex = e;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (ex != null) {
                throw new Exception(ex.getMessage());
            }
        }
        return suggestions;
    }

    private List<Lesson> parseNewLessonsResponse(final JSONObject jsonObject) throws JSONException {
        int total = jsonObject.getInt("total");
        JSONArray lessonsArray = jsonObject.getJSONArray("lessons");
        List<Lesson> lessons = new ArrayList<>(lessonsArray.length());
        for (int i = 0; i < lessonsArray.length(); i++) {
            JSONObject jsonLesson = lessonsArray.getJSONObject(i);
            Lesson lesson = new Lesson();

            String title = jsonLesson.getString("title");
            int id = jsonLesson.getInt("id");
            String coverURI = BASE_LESSONS_COVER_URI + id;
            int complexity = jsonLesson.getInt("complexity");
            int views = jsonLesson.getInt("views");
            int rating = jsonLesson.getInt("rating");

            lesson.setTitle(title);
            lesson.setCoverURI(coverURI);
            lesson.setComplexity(complexity);
            lesson.setId(id);
            lesson.setViews(views);
            lesson.setRating(rating);
            lesson.setCoverURI(coverURI);
            lessons.add(lesson);
        }
        return lessons;
    }

    private List<LessonSuggestion> parseSuggestionsResponse(final JSONObject jsonObject) throws JSONException {
        JSONArray suggestionsArray = jsonObject.getJSONArray("hints");
        List<LessonSuggestion> suggestions = new ArrayList<>(suggestionsArray.length());
        for (int i = 0; i < suggestionsArray.length(); i++) {
            JSONObject jsonSuggestion = suggestionsArray.getJSONObject(i);
            String title = jsonSuggestion.getString("title");
            LessonSuggestion lessonSuggestion = new LessonSuggestion(title, title);
            suggestions.add(lessonSuggestion);
        }
        return suggestions;
    }

}
