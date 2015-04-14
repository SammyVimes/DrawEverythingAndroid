package ru.edikandco.draweverything.activity;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
/**
 * Created by Эдуард on 10.08.2014.
 */
import java.util.LinkedList;

public class ActivityState {
    public int currentStep, totalSteps;
    public LessonActivity.DrawingView drawingView;
    public Thread process;
    public int paintSize, paint_mod, paint_layer;
    public Bitmap[] cashBitmaps;
    public Bitmap[] historyBitmaps;
    public Bitmap mbtmp;
    public boolean visibleLayers[];
    public int redraw;
    public LinkedList <LessonActivity.PaintAction> actions = new LinkedList<LessonActivity.PaintAction>();
    public int angle;
    public Path oldPath;
    public boolean visibleInterface;

    public Paint mPaint, oldPaint;
    public int currentLessonStep;

    // public AvtivityState(int step, int size, int mode, DrawingView dv, Paint paint){
    public ActivityState(){
        // currentStep =step;
        // drawingView=dv;
        // paintSize=size;
        // paint_mod=mode;

        // mPaint=paint;
    }
}