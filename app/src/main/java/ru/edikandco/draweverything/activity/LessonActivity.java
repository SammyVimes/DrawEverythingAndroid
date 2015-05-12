package ru.edikandco.draweverything.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import ru.edikandco.draweverything.R;
import ru.edikandco.draweverything.core.util.Constants;
import ru.edikandco.draweverything.dialog.ColorPickerDialog;
import ru.edikandco.draweverything.util.Utilities;


public class LessonActivity extends ActionBarActivity implements ViewSwitcher.ViewFactory, SeekBar.OnSeekBarChangeListener, ColorPickerDialog.OnColorChangeListener {

    private int currentStep, totalSteps;
    private ActivityState state;
    private int paintSize;
    private int old_paintSize;
    LessonActivity _this;
    private ImageButton  paint_button, layer_visible_button;
    private ProgressBar progressLoader;
    private ImageView progressScreen;
    private ImageView  size_line;
    private SeekBar seekbar_paint_size;
    private RelativeLayout drawing_layout;
    private LinearLayout layout_paint_size;
    private TableLayout layout_paint_type;
    private int paint_mod, paint_layer;
    private TextView layout_field;
    private ActivityState avtivityData;
    private Paint mPaint, oldPaint;
    private int paintsNum = 7;
    private MaskFilter[] maskFilters = new MaskFilter[paintsNum];
    private int countLayers = 3, step;
    private boolean UnlockDialog = false;
    private int redraw_process = 0;
    private Bitmap floorBitmap ;
    private Handler historyHandler;
    public Canvas drawingCanvas;
    public Canvas[] cashCanvas = new Canvas [countLayers];
    public Canvas[] historyCanvas = new Canvas [countLayers];
    public Bitmap[] cashBitmaps = new Bitmap [countLayers];
    public Bitmap[] historyBitmaps = new Bitmap [countLayers];
    public boolean[] visibleLayers = new boolean [countLayers];
    public LinkedList <PaintAction> actions = new LinkedList<PaintAction>();

    private Path mPath, oldPath;
    private boolean visibleInterface, zooming;
    DisplayMetrics metrics;
    DrawingView drawingView;

    private ViewSwitcher imagePrew;
    private int id_lesson;
    private String lessonTitle;
    private String[] imagePaths;

    private Animation in;
    private Animation out;

    private LinearLayout right_panel1, bottom_panel2;
    private ImageButton saveButton, interfaceButton, zoomButton;
    private FrameLayout selectedColorLayout;
    private int currentLessonStep;
    private int totalLessonSteps;

    /**
     * Called when the activity is first created.
     */

    public static Bitmap rotate(Bitmap src, float degree) {
        if(degree!=0){
            // create new matrix
            Matrix matrix = new Matrix();
            // setup rotation degree
            matrix.postRotate(degree);
            System.out.println("ROTATE");
            // return new bitmap rotated using matrix
            return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        }else{
            return src;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.gc();
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lesson);

        maskFilters[2] = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 3.5f, 6, 0.4f);
        maskFilters[3] = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);

        maskFilters[4] = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
        maskFilters[5] = new BlurMaskFilter(18, BlurMaskFilter.Blur.OUTER);
        maskFilters[6] = new BlurMaskFilter(18, BlurMaskFilter.Blur.SOLID);


        //mBlur = android.graphics.

        paint_button = (ImageButton) findViewById(R.id.paintbutton);

        drawing_layout = (RelativeLayout) findViewById(R.id.drawinglayout);
        layout_paint_size = (LinearLayout) findViewById(R.id.layoutpaintsize);
        layout_paint_type = (TableLayout) findViewById(R.id.layoutpainttype);
        progressLoader = (ProgressBar) findViewById(R.id.progressbar);
        size_line = (ImageView)  findViewById(R.id.sizeLine);

        progressScreen= (ImageView)  findViewById(R.id.progress);
        seekbar_paint_size = (SeekBar) findViewById(R.id.seekbarpaintsize);
        seekbar_paint_size.setOnSeekBarChangeListener(this);
        state = (ActivityState) getLastNonConfigurationInstance();

        historyHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 1){
                    redraw_process= 0;
                    progressScreen.setVisibility(View.GONE);
                    progressLoader.setVisibility(View.GONE);
                    drawingView.invalidate();
                }
            };
        };
        if (state == null) {
            currentLessonStep = 0;
            currentStep = 0;
            totalSteps = 0;
            avtivityData = new ActivityState();
            avtivityData.angle = 0;
            visibleInterface = true;
            drawingView = new DrawingView(this);
            paintSize =12;


            paint_mod = 1;
            paint_layer=2;

            avtivityData.historyBitmaps = new Bitmap[countLayers];
            avtivityData.cashBitmaps = new Bitmap[countLayers];

            redraw_process= 0;

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(0xFF34c924);
            mPaint.setStyle(Paint.Style.STROKE);

            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(paintSize);



            oldPaint = mPaint;
            for(int i = 0; i<countLayers;i++){
                avtivityData.cashBitmaps[i] = Bitmap.createBitmap(metrics.widthPixels,metrics.heightPixels, Bitmap.Config.ARGB_8888);
                avtivityData.historyBitmaps[i] = Bitmap.createBitmap(metrics.widthPixels,metrics.heightPixels, Bitmap.Config.ARGB_8888);

                //avtivityData.cashBitmaps[i] = avtivityData.cashBitmaps[i];
                cashCanvas[i] = new Canvas(avtivityData.cashBitmaps[i]);
                historyCanvas[i] = new Canvas(avtivityData.historyBitmaps[i]);
                visibleLayers[i] = true;
            }



        }else{
            currentLessonStep = state.currentLessonStep;
            avtivityData = state;
            currentStep = state.currentStep;
            totalSteps = state.totalSteps;
            actions = state.actions;
            visibleLayers = state.visibleLayers;
            redraw_process = state.redraw;
            visibleInterface = state.visibleInterface;
            oldPaint = state.oldPaint;
            oldPath = state.oldPath;




            if(state.angle==1){
                state.angle=0;
            }else{
                state.angle=1;
            }


            System.gc();

            for(int i = 0; i<countLayers;i++){
                //avtivityData.cashBitmaps[i] = rotate(avtivityData.cashBitmaps[i], state.angle*90);
                //System.gc();
                cashCanvas[i] = new Canvas(avtivityData.cashBitmaps[i]);
                historyCanvas[i] = new Canvas(avtivityData.historyBitmaps[i]);
            }

            //System.out.println(metrics.widthPixels+" "+ metrics.heightPixels);
            //Bitmap bmOverlay = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888);
            //Bitmap bmOverlay = Bitmap.createBitmap(state.cashBitmaps[0].getWidth(), state.cashBitmaps[0].getHeight(), state.cashBitmaps[0].getConfig());
            //Bitmap bmOverlay = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888);
            //Canvas canvas = new Canvas(bmOverlay); 

            // cashCanvas[i].rotate(90, metrics.widthPixels/2,metrics.heightPixels/2);
            // canvas.drawColor(0xFFFFFFFF);

            System.gc();

            //	Bitmap rotated = Bitmap.createBitmap(bmOverlay, 0, 0, bmOverlay.getWidth(), bmOverlay.getHeight(), matrix, true);

            //canvas.rotate(90, metrics.widthPixels/2,metrics.heightPixels/2);


            //bmOverlay = rotate(bmOverlay, (float) 30.0);
            drawingView = new DrawingView(this);





            paintSize =state.paintSize;

            paint_layer = state.paint_layer;

            mPaint = state.mPaint;

            avtivityData = state;
            paint_mod = state.paint_mod;
            updatePaintMod(paint_mod);


            switch(redraw_process){
                case 1:
                    undo();
                    break;
                case 2:
                    redo();
                    break;
            }
        }
        old_paintSize = paintSize;

        layout_field = (TextView)  findViewById(R.id.layertext);


        layout_field.setText(("("+paint_layer+")"));
        layer_visible_button =(ImageButton) findViewById(R.id.visibleLayer);


        drawing_layout.addView(drawingView);

        right_panel1 = (LinearLayout) findViewById(R.id.right_panel1);
        saveButton = (ImageButton) findViewById(R.id.saveButton);
        interfaceButton = (ImageButton) findViewById(R.id.interfaceVisibleButton);
        zoomButton = (ImageButton) findViewById(R.id.zoomButton);
        bottom_panel2 = (LinearLayout) findViewById(R.id.bottom_panel2);
        selectedColorLayout = (FrameLayout) findViewById(R.id.selectedColor);
        selectedColorLayout.setBackgroundColor(mPaint.getColor());

        updateLayerVisible();
        updateInterfaceVisible();


        id_lesson = getIntent().getIntExtra("lesson_id", 0);

        lessonTitle = getIntent().getStringExtra("title_lesson");

        imagePaths = new String[totalLessonSteps + 1];

        String path = Constants.SDPATH + "/" + id_lesson + "/";
        File file = new File(path);
        imagePaths = file.list(new FilenameFilter() {
            @Override
            public boolean accept(final File file, final String s) {
                return s.endsWith("png");
            }
        });
        Arrays.sort(imagePaths, new Comparator<String>() {

            @Override
            public int compare(final String lhs, final String rhs) {

                String _lhs = lhs;
                String _rhs = rhs;
                int index = _lhs.lastIndexOf('.');
                if (index != -1) {
                    _lhs = _lhs.substring(0, index);
                    index = _lhs.lastIndexOf('/');
                    if (index == -1) {
                        index = _lhs.lastIndexOf('\\');
                    }
                    if (index != -1) {
                        _lhs = _lhs.substring(index + 1);
                    }
                }
                index = _rhs.lastIndexOf('.');
                if (index != -1) {
                    _rhs = _rhs.substring(0, index);
                    index = _rhs.lastIndexOf('/');
                    if (index == -1) {
                        index = _rhs.lastIndexOf('\\');
                    }
                    if (index != -1) {
                        _rhs = _rhs.substring(index + 1);
                    }
                }
                try {
                    Integer left = Integer.valueOf(_lhs);
                    Integer right = Integer.valueOf(_rhs);
                    return left.compareTo(right);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
        for (int i = 0; i < imagePaths.length; i++) {
            String img = imagePaths[i];
            imagePaths[i] = path + img;
        }
        totalLessonSteps = imagePaths.length;

        in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        imagePrew = (ViewSwitcher) findViewById(R.id.stepImage);
        imagePrew.setFactory(this);
        imagePrew.setInAnimation(in);
        imagePrew.setOutAnimation(out);
        updateStep();

        imagePrew.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent e) {
                gd.onTouchEvent(e);
                return true;
            }
        });

        System.gc();

    }

    private final GestureDetector gd = new GestureDetector(
            new GestureListener());

    private static final int DISTANCE = 100;
    private static final int VELOCITY = 200;

    @Override
    public void onDismiss(int color, int alpha) {
        drawingView.stopAction();
        mPaint.setColor(color);
        mPaint.setAlpha(alpha);
        selectedColorLayout.setBackgroundColor(mPaint.getColor());
    }

    @Override
    public void onColorChanged(int color, int alpha) {
        drawingView.stopAction();
        mPaint.setColor(color);
        mPaint.setAlpha(alpha);
        selectedColorLayout.setBackgroundColor(mPaint.getColor());
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (e1.getX() - e2.getX() > DISTANCE
                    && Math.abs(velocityX) > VELOCITY) {
                if (currentLessonStep < totalLessonSteps) {
                    currentLessonStep++;
                    updateStep();
                }
                return false;
            } else if (e2.getX() - e1.getX() > DISTANCE
                    && Math.abs(velocityX) > VELOCITY) {
                if (currentLessonStep > 0) {
                    currentLessonStep--;
                    updateStep();
                }
                return false;
            }
            return false;
        }
    }

    private int lastImg = -1;

    private SubsamplingScaleImageView[] ssivs = new SubsamplingScaleImageView[2];

    public void updateStep() {
        if(ssivs[0]==null){
            ssivs[0] = new SubsamplingScaleImageView(this);
            ssivs[0].setImage(ImageSource.uri(imagePaths[currentLessonStep]));
            ssivs[0].setMaxScale(3);
            ssivs[0].setMinScale(0.2f);

            ssivs[1] = new SubsamplingScaleImageView(this);
            ssivs[1].setImage(ImageSource.uri(imagePaths[currentLessonStep]));
            ssivs[1].setMaxScale(3);
            ssivs[1].setMinScale(0.2f);
            lastImg = currentLessonStep;
            imagePrew.removeAllViews();
            imagePrew.addView(ssivs[0]);
            imagePrew.addView(ssivs[1]);
        }


        if(lastImg != currentLessonStep) {
            SubsamplingScaleImageView nxtView = (SubsamplingScaleImageView) imagePrew.getNextView();
            nxtView.setImage(ImageSource.uri(imagePaths[currentLessonStep]));
            nxtView.setScaleAndCenter(((SubsamplingScaleImageView)imagePrew.getCurrentView()).getScale(), ((SubsamplingScaleImageView)imagePrew.getCurrentView()).getCenter());
            lastImg = currentLessonStep;
            imagePrew.showNext();
        }


        SubsamplingScaleImageView c = new SubsamplingScaleImageView(this);

        if (currentLessonStep < totalLessonSteps) {
            this.setTitle(lessonTitle + " "
                    + getResources().getString(R.string.step) + " "
                    + (currentLessonStep + 1) + "/" + totalLessonSteps);
        } else {
            this.setTitle(getResources().getString(R.string.result));
        }
        System.gc();
    }

    private void updateInterfaceVisible() {
        if(visibleInterface){
            right_panel1.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            bottom_panel2.setVisibility(View.VISIBLE);
            drawingView.setVisibility(View.VISIBLE);
            interfaceButton.setImageResource(R.drawable.ic_hide_draw);
        }else{
            right_panel1.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            bottom_panel2.setVisibility(View.GONE);
            drawingView.setVisibility(View.GONE);
            interfaceButton.setImageResource(R.drawable.ic_show_draw);
        }

        if(zooming){
            drawingView.setVisibility(View.GONE);
            zoomButton.setImageResource(R.drawable.ic_unloop);
        }else{
            //drawingView.setVisibility(View.VISIBLE);
            zoomButton.setImageResource(R.drawable.ic_loop);
        }
    }


    private  void updateSeekBarP(){
        int bottom = (paint_button.getHeight()-(int) ((float) paint_button.getHeight() /100 * ((float) paintSize / ((float) seekbar_paint_size.getMax()/100))));
        if (bottom > 61){
            bottom = 61;
        }
        size_line.setPadding(0, bottom,0,0);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            if (currentLessonStep < totalLessonSteps) {
                currentLessonStep++;
                updateStep();
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            if (currentLessonStep > 0) {
                currentLessonStep--;
                updateStep();
            }
        }else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BACKSLASH ){
            finish();
            return true;
        }
        return false;
    }

    @Override   public void finish(){        System.gc();        super.finish();    }

    @Override   public void onDestroy() { System.gc();  super.onDestroy();  }

    public void updateLayerVisible(){
        drawingView.stopAction();
        if(visibleLayers[paint_layer-1]){
            layer_visible_button.setImageResource(R.drawable.ic_visible);
        }else{
            layer_visible_button.setImageResource(R.drawable.ic_unvisible);
        }

    }

    public int currentStep_cash;
    public void undo(){
        //System.out.println("!!!!! Input in undo " + currentStep);
        if (currentStep>0 ){
            currentStep_cash = currentStep;
            redraw_process= 1;
            progressScreen.setVisibility(View.VISIBLE);
            progressLoader.setVisibility(View.VISIBLE);

            avtivityData.process = new Thread(new Runnable() {
                public void run() {
                    try{
                        if (currentStep>0){
                            currentStep--;
                            for(int i = 0; i<countLayers;i++){
                                cashCanvas[i].drawColor(0x00FFFFFF,PorterDuff.Mode.CLEAR);
                                cashCanvas[i].drawBitmap(avtivityData.historyBitmaps[i], 0, 0, null);
                            }


                            step = 0;
                            for (PaintAction action : actions) {
                                //System.out.println("!!!!! Undo " + step);
                                step++;
                                if(step>currentStep){
                                    break;
                                }
                                if (!action.custom){
                                    cashCanvas[action.getLayer()].drawPath(action.getPath(), action.getPaint());
                                }else{
                                    switch(action.getAction()){
                                        case 0:
                                            cashCanvas[action.getLayer()].drawColor(0x00FFFFFF, PorterDuff.Mode.CLEAR);
                                            break;
                                        case 1:
                                            drawingView.FloodFill(avtivityData.cashBitmaps[action.getLayer()], action.getPoint(),action.getColor());
                                            //System.out.println("!!!!! Load " + action.getPoint() + " "+  action.getPoint().x + ": "   +   action.getPoint().y);


                                            //  avtivityData.cashBitmaps[action.getLayer()] = floorBitmap;
                                            //  cashCanvas[action.getLayer()].drawColor(0x00FFFFFF,PorterDuff.Mode.CLEAR);
                                            // cashCanvas[action.getLayer()].drawBitmap(avtivityData.cashBitmaps[action.getLayer()], 0, 0, null);
                                            avtivityData.cashBitmaps[action.getLayer()] = floorBitmap;
                                            cashCanvas[action.getLayer()].setBitmap(avtivityData.cashBitmaps[action.getLayer()]);

                                            break;
                                    }
                                }
                            }


                        }

                        historyHandler.sendEmptyMessage(1);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

            });
            avtivityData.process.start();
        }
    }

    public void redo(){
        if (currentStep < totalSteps){
            currentStep_cash = currentStep;
            redraw_process= 2;
            progressScreen.setVisibility(View.VISIBLE);
            progressLoader.setVisibility(View.VISIBLE);

            avtivityData.process = new Thread(new Runnable() {
                public void run() {
                    try {
                        if(currentStep < totalSteps ){
                            currentStep ++;

                            for(int i = 0; i<countLayers;i++){
                                cashCanvas[i].drawColor(0x00FFFFFF,PorterDuff.Mode.CLEAR);
                                cashCanvas[i].drawBitmap(avtivityData.historyBitmaps[i], 0, 0, null);
                            }

                            step = 0;
                            for (PaintAction action :actions) {
                                step++;
                                if(step>currentStep){
                                    break;
                                }
                                if (!action.custom){
                                    cashCanvas[action.getLayer()].drawPath(action.getPath(), action.getPaint());
                                }else{
                                    switch(action.getAction()){
                                        case 0:
                                            cashCanvas[action.getLayer()].drawColor(0x00FFFFFF,PorterDuff.Mode.CLEAR);
                                            break;
                                        case 1:
                                            drawingView.FloodFill(avtivityData.cashBitmaps[action.getLayer()], action.getPoint(),action.getColor());

                                            avtivityData.cashBitmaps[action.getLayer()] = floorBitmap;
                                            cashCanvas[action.getLayer()].setBitmap(avtivityData.cashBitmaps[action.getLayer()]);
                                            break;

                                    }
                                }
                            }
                        }

                        historyHandler.sendEmptyMessage(1);
                    }catch(Exception e){
                        e.printStackTrace();
                        historyHandler.sendEmptyMessage(404);
                    }
                }
            });
            avtivityData.process.start();



        }

    }


    @SuppressLint("WrongCall")
    public void butClick(View view) throws IOException {
        switch (view.getId()) {

            case R.id.prewStepButton:
                if (currentLessonStep > 0) {
                    currentLessonStep--;
                    updateStep();
                }
                break;
            case R.id.interfaceVisibleButton:
                visibleInterface = !visibleInterface;
                updateInterfaceVisible();
                break;

            case R.id.zoomButton:
                zooming = !zooming;
                updateInterfaceVisible();
                break;

            case R.id.nextStepButton:
                currentLessonStep++;
                if (currentLessonStep < totalLessonSteps) {
                    updateStep();
                }
                break;

            case R.id.redoButton:
                if(redraw_process == 0){
                    redo();
                }               break;
            case R.id.undoButton:
                if(redraw_process == 0) {
                    //System.out.println("!!!!! St " + redraw_process);
                    undo();
                    //System.out.println("!!!!! Undo comp");
                }
                break;

            case R.id.colorButton:

                mPaint.setXfermode(null);
                //new ColorPickerDialog(this, this, "", mPaint.getColor(), mPaint.getColor()).show();
                //mPaint.setColor(Color.GREEN);
                //mPaint.setColor(0xFF34c924);
                new ColorPickerDialog(this, this,  mPaint.getColor(), mPaint.getAlpha()).show();
                break;
            case R.id.fillButton:
                updatePaintMod(7);
                break;
            case R.id.eraseButton:
                if(paintSize==1){
                    seekbar_paint_size.setProgress(12);
                }
                paintSize = seekbar_paint_size.getProgress();
                mPaint.setStrokeWidth(paintSize);

                mPaint.setXfermode(null);
                mPaint.setAlpha(0xFF);
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                break;

            case R.id.upLayer:
                if(paint_layer<countLayers){
                    paint_layer++;
                    layout_field.setText("("+paint_layer+")");
                }
                updateLayerVisible();
                break;
            case R.id.downLayer:
                if(paint_layer>1){
                    paint_layer--;
                    layout_field.setText("("+paint_layer+")");
                }
                updateLayerVisible();
                break;


            case R.id.visibleLayer:
                if(visibleLayers[paint_layer-1]){
                    visibleLayers[paint_layer-1]=false;
                }else{
                    visibleLayers[paint_layer-1]=true;
                }
                drawingView.invalidate();
                updateLayerVisible();
                break;


            case R.id.cleareLayer:
                drawingView.stopAction();

                cashCanvas[paint_layer-1].drawColor(0x00FFFFFF,PorterDuff.Mode.CLEAR);
                drawingView.addAction(new PaintAction(paint_layer-1, 0));
                drawingView.invalidate();

                break;

            case R.id.sizeButton:
                if(layout_paint_size.getVisibility()==View.VISIBLE){
                    layout_paint_size.setVisibility(View.GONE);
                    paintSize = seekbar_paint_size.getProgress();
                    mPaint.setStrokeWidth(paintSize);
                    layout_paint_size.setVisibility(View.GONE);
                }else{
                    layout_paint_size.setVisibility(View.VISIBLE);
                    old_paintSize =  paintSize;
                }
                break;

            case R.id.saveButton:
                Toast toast;
                Configuration sysConfig = getResources().getConfiguration();
                Locale curLocale = sysConfig.locale;

                Bitmap bmOverlay = Bitmap.createBitmap(drawingView.mBitmap.getWidth(), drawingView.mBitmap.getHeight(), drawingView.mBitmap.getConfig());
                Canvas canvas = new Canvas(bmOverlay);
                canvas.drawColor(0xFFFFFFFF);
                for(int i = 0; i<countLayers;i++){
                    if(visibleLayers[i]){
                        canvas.drawBitmap(avtivityData.cashBitmaps[i], 0, 0, null);
                    }
                }

                Time time = new Time();
                time.setToNow();

                // Create a path where we will place our picture in the user's
                // public pictures directory.  Note that you should be careful about
                // what you place here, since the user often manages these files.  For
                // pictures and other media owned by the application, consider
                // Context.getExternalMediaDir().

                try {
                    Resources baseResources = getResources();
                    Configuration config = new Configuration(baseResources.getConfiguration());
                    config.locale = Locale.ENGLISH;
                    Resources localResources = new Resources(baseResources.getAssets(), baseResources.getDisplayMetrics(), config);

                    String enTitle = localResources.getString(R.string.app_name);

                    File path = new File(Environment.getExternalStorageDirectory()+"/Pictures/"+enTitle+"/");

                    path.mkdirs();

                    File file = new File(path, "Drawing-"+Integer.toString(time.year) + Integer.toString(time.month) + Integer.toString(time.monthDay) + Integer.toString(time.hour) + Integer.toString(time.minute) + Integer.toString(time.second) +".jpg");
                    OutputStream os = new FileOutputStream(file);
                    bmOverlay.compress(Bitmap.CompressFormat.PNG, 100, os);

                    os.flush();
                    os.close();
                    // Tell the media scanner about the new file so that it is
                    // immediately available to the user.
                    MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(),  file.getName()); // регистрация в фотоальбоме

                    if (Utilities.itRuLocale(this)) {
                        toast = Toast.makeText(getApplicationContext(), "Сохранено в галереи:\nКартаПамяти/Pictures/"+enTitle, Toast.LENGTH_LONG);
                    } else {
                        toast = Toast.makeText(getApplicationContext(), "Saved in gallery:\nSD/Pictures/"+enTitle, Toast.LENGTH_LONG);
                    }
                    toast.show();
                } catch (IOException e) {
                    if (Utilities.itRuLocale(this)) {
                        toast = Toast.makeText(getApplicationContext(),
                                "Ошибка при сохранении. Проверьте карту памяти.", Toast.LENGTH_LONG);
                    } else {
                        toast = Toast.makeText(getApplicationContext(),
                                "Error saving. Check the memory card.", Toast.LENGTH_LONG);
                    }
                    toast.show();
                }
                break;

            case R.id.savesizeButton:
                paintSize = seekbar_paint_size.getProgress();
                mPaint.setStrokeWidth(paintSize);
                layout_paint_size.setVisibility(View.GONE);
                break;
            case R.id.closesizeButton:
                paintSize = old_paintSize;
                updateSeekBarP();
                layout_paint_size.setVisibility(View.GONE);
                break;

            case R.id.paintbutton:
                if(layout_paint_type.getVisibility()==View.VISIBLE){
                    layout_paint_type.setVisibility(View.GONE);
                }else{
                    layout_paint_type.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.closepaintButton:
                layout_paint_type.setVisibility(View.GONE);
                break;

            case R.id.sample0:
                updatePaintMod(0);
                break;
            case R.id.sample1:
                updatePaintMod(1);
                break;
            case R.id.sample2:
                updatePaintMod(3);
                break;
            case R.id.sample3:
                updatePaintMod(4);
                break;
            case R.id.sample4:
                updatePaintMod(5);
                break;
            case R.id.sample5:
                updatePaintMod(6);
                break;
        }
    }

    public void updatePaintMod(int id) {
        paint_mod = id;

        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);
        mPaint.setMaskFilter(null);


        switch (id){
            case 0:
                seekbar_paint_size.setProgress(1);
                break;
            case 1:
                if(paintSize==1){
                    seekbar_paint_size.setProgress(12);
                }
                break;
            case 7:

                break;
            default:
                if(paintSize==1){
                    seekbar_paint_size.setProgress(12);
                }
                mPaint.setMaskFilter(maskFilters[paint_mod]);
                break;

        }


        paintSize = seekbar_paint_size.getProgress();
        mPaint.setStrokeWidth(paintSize);
    }


    public View makeView() {
        SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(this);
        //imageView.setBackgroundColor(getResources().getColor(R.color.white));
        //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        return imageView;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        paintSize = seekbar_paint_size.getProgress();

        updateSeekBarP();
        mPaint.setStrokeWidth(paintSize);
    }




    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        avtivityData.currentLessonStep = currentLessonStep;
        avtivityData.totalSteps = totalSteps;
        avtivityData.totalSteps = totalSteps;
        avtivityData.redraw = redraw_process;
        if(redraw_process!=0){
            avtivityData.currentStep = currentStep_cash;
        }else{
            avtivityData.currentStep  = currentStep;
        }
        avtivityData.paint_layer= paint_layer;
        cashCanvas = null;
        avtivityData.actions =  actions;
        avtivityData.paintSize = paintSize;
        avtivityData.paint_mod = paint_mod;
        avtivityData.mPaint =mPaint;
        avtivityData.visibleLayers = visibleLayers;
        avtivityData.oldPaint =oldPaint;
        avtivityData.oldPath = oldPath;
        avtivityData.visibleInterface = visibleInterface;
        avtivityData.zooming = zooming;

        return avtivityData;
    }
    public class PaintAction {
        int layer, action;
        Path path;
        Paint paint;
        boolean custom;

        int color;
        //Point point;
        int x;
        int y;

        PaintAction(int _layer,Path _path,Paint _paint){
            layer=_layer;
            path=_path;
            paint=_paint;
            custom = false;
        }

        PaintAction(int _layer, int _action){
            layer=_layer;
            action = _action;
            custom = true;
        }

        public void setPoint(int _x,int _y){
            x = _x;
            y = _y;
        }
        public Point getPoint(){return new Point(x,y);}
        public int getColor(){
            return color;
        }
        public int getLayer(){
            return layer;
        }
        public Path getPath(){
            return path;
        }
        public Paint getPaint(){
            return paint;
        }
        public int getAction(){
            return action;
        }
    }

    public class DrawingView extends View {

        public Bitmap mBitmap;


        public DrawingView(Context c) {
            super(c);
            mBitmap = Bitmap.createBitmap(metrics.widthPixels,
                    metrics.heightPixels, Bitmap.Config.ARGB_8888);
            drawingCanvas = new Canvas(mBitmap);

            mPath = new Path();
            oldPath = new Path();
        }

        public DrawingView(Context c, Bitmap btmp) {
            super(c);
            mBitmap = btmp;
            drawingCanvas = new Canvas(mBitmap);
            mPath = new Path();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }



        public void onDraw(Canvas canvas) {
            if (redraw_process==0){

                if(!mPath.isEmpty() &&  visibleLayers[paint_layer-1]){
                    if(currentStep < totalSteps ){
                        for(int i = 0; i < (totalSteps - currentStep); i++){
                            actions.removeLast();
                        }
                        totalSteps=currentStep;
                    }

                    if (mPaint==oldPaint){
                        oldPath.addPath(mPath);
                    }else{
                        stopAction();

                        oldPaint = mPaint;
                        oldPath = mPath;
                    }

                    cashCanvas[paint_layer-1].drawPath(mPath, mPaint);
                }

                canvas.drawColor(0x00AAAAAA);
                for(int i = 0; i<countLayers;i++){
                    if(visibleLayers[i]){
                        canvas.drawBitmap(avtivityData.cashBitmaps[i], 0, 0, null);
                    }
                }

            }

        }

        private void addAction(PaintAction pa){
            actions.add(pa);

            if(currentStep < totalSteps ){
                for(int i = 0; i < (totalSteps - currentStep); i++){
                    actions.removeLast();
                }
                totalSteps=currentStep;
            }
            currentStep++;
            totalSteps++;

            if (totalSteps > 100){
                PaintAction action = actions.getFirst();
                if (!action.custom){
                    historyCanvas[action.getLayer()].drawPath(action.getPath(), action.getPaint());
                }else{
                    switch(action.getAction()){
                        case 0:
                            historyCanvas[action.getLayer()].drawColor(0x00FFFFFF,PorterDuff.Mode.CLEAR);
                            break;
                        case 1:
                            drawingView.FloodFill(avtivityData.historyBitmaps[action.getLayer()], action.getPoint(), action.getColor());
                            historyCanvas[action.getLayer()].drawColor(0x00FFFFFF,PorterDuff.Mode.CLEAR);
                            historyCanvas[action.getLayer()].drawBitmap(avtivityData.historyBitmaps[action.getLayer()], 0, 0, null);
                            break;
                    }
                }
                actions.removeFirst();
                currentStep--;
                totalSteps--;
            }

        }


        private void stopAction(){
            if(!oldPath.isEmpty()){
                currentStep++;
                totalSteps ++;
                actions.add(new PaintAction(paint_layer-1, new Path(oldPath), new Paint(oldPaint)));

                oldPath = new Path();

                if (totalSteps > 100){
                    PaintAction action = actions.getFirst();
                    if (!action.custom){
                        historyCanvas[action.getLayer()].drawPath(action.getPath(), action.getPaint());
                    }else{
                        switch(action.getAction()){
                            case 0:
                                historyCanvas[action.getLayer()].drawColor(0x00FFFFFF,PorterDuff.Mode.CLEAR);
                                break;
                            case 1:
                                drawingView.FloodFill(avtivityData.historyBitmaps[action.getLayer()], action.getPoint(),action.getColor());
                                break;
                        }
                    }
                    actions.removeFirst();
                    currentStep--;
                    totalSteps--;
                }
            }
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        // private int saveOperations = (metrics.widthPixels * metrics.heightPixels) / 2;
        public void FloodFill(Bitmap bmp_, Point pt,  int replacementColor) {

            // Bitmap bmp2 = avtivityData.historyBitmaps[paint_layer-1];

            //System.out.println("!!!!! FloodFill 1");
            //int cutOperations = saveOperations;
            System.gc();
            //System.out.println("!!!!! FloodFill 2");
            floorBitmap = null;
            //System.out.println("!!!!! FloodFill 3");
            Bitmap bmp = Bitmap.createBitmap(bmp_);
            //System.out.println("!!!!! FloodFill 4");
            //  for(int i = 0; i < 3; i++){
            //    //System.out.println("!!!!! " + i + ": "   + bmp.getPixel(i,i));
            // }
            //System.out.println("!!!!! " + paint_layer +" " + bmp + " " +bmp2);
            int targetColor = bmp.getPixel(pt.x, pt.y);
            if (targetColor != replacementColor) {
                Queue<Point> q = new LinkedList<Point>();
                q.add(pt);
                //System.out.println("!!!!! FloodFill 5");
                while (q.size() > 0) {
                    Point n = q.poll();
                    if ((bmp.getPixel(n.x, n.y) != targetColor))
                        continue;

                    Point w = n, e = new Point(n.x + 1, n.y);
                    while ((w.x > 0) && ((bmp.getPixel(w.x, w.y) == targetColor))) {
                        ////System.out.println("!!!!! FloodFill 6");
                        bmp.setPixel(w.x, w.y, replacementColor);
                        ////System.out.println("!!!!! FloodFill 7");

                        if ((w.y > 0) && ((bmp.getPixel(w.x, w.y - 1) == targetColor)))
                            q.add(new Point(w.x, w.y - 1));
                        if ((w.y < bmp.getHeight() - 1)
                                && ((bmp.getPixel(w.x, w.y + 1) == targetColor)))
                            q.add(new Point(w.x, w.y + 1));
                        w.x--;

                    }
                    // //System.out.println("!!!!! FloodFill 8");
                    while ((e.x < bmp.getWidth() - 1) && ((bmp.getPixel(e.x, e.y) == targetColor))) {
                        bmp.setPixel(e.x, e.y, replacementColor);


                        if ((e.y > 0) && ((bmp.getPixel(e.x, e.y - 1) == targetColor)))
                            q.add(new Point(e.x, e.y - 1));
                        if ((e.y < bmp.getHeight() - 1) && ((bmp.getPixel(e.x, e.y + 1) == targetColor)))
                            q.add(new Point(e.x, e.y + 1));
                        e.x++;
                    }
                }
                //System.out.println("!!!!! FloodFill 9");
                floorBitmap = bmp;
                //System.out.println("!!!!! FloodFill 10");

            }
        }


        private void touch_start(final float x, final float y) {
            if (redraw_process==0) {
                if (paint_mod == 7) {

                } else {
                    mPath.reset();
                    mPath.moveTo(x, y);
                    mX = x;
                    mY = y;
                }
            }
        }

        private void touch_move(float x, float y) {
            if (redraw_process==0) {
                if (paint_mod == 7) {

                } else {
                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                        mX = x;
                        mY = y;
                    }
                }
            }
        }

        public void touch_up(final float x, final float y) {
            if (redraw_process==0) {
                if (paint_mod == 7) {
                    progressScreen.setVisibility(View.VISIBLE);
                    progressLoader.setVisibility(View.VISIBLE);

                    avtivityData.process = new Thread(new Runnable() {
                        public void run() {
                            PaintAction pa = new PaintAction(paint_layer - 1, 1);
                            //pa.point = new Point((int) x, (int) y);
                            pa.setPoint((int) x, (int) y);
                            pa.color = mPaint.getColor();

                            //System.out.println("!!!!! Save " + pa.getPoint()+ " "  +  pa.getPoint().x + ": "   +   pa.getPoint().y);
                            //drawingView.stopAction();
                            redraw_process = 100;
                            drawingView.FloodFill(avtivityData.cashBitmaps[paint_layer - 1], pa.getPoint(), pa.color);
                            drawingView.addAction(pa);

                            historyHandler.sendEmptyMessage(2);
                        }

                    });
                    avtivityData.process.start();
                } else {
                    mPath.lineTo(mX, mY);
                    // commit the path to our offscreen
                    drawingCanvas.drawPath(mPath, mPaint);
                    stopAction();
                    // kill this so we don't double draw

                    mPath.reset();
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (redraw_process==0) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touch_start(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        touch_move(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        touch_up(x, y);
                        invalidate();
                        break;
                }
            }
            return true;
        }

    }

}