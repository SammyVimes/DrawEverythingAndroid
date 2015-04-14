package ru.edikandco.draweverything.core.util;

/**
 * Created by Semyon Danilov on 27.05.2014.
 */
public interface Pool<T> {

    T obtain();

    void retrieve(T object);

}
