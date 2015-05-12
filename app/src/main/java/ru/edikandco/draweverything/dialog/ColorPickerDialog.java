package ru.edikandco.draweverything.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import ru.edikandco.draweverything.util.Utilities;

/**
 * Created by Эдуард on 17.04.2015.
 */


public class ColorPickerDialog extends Dialog {
    private OnColorChangeListener mListener;
    private int mInitialColor, mInitialAlpha;
    private String mKey;

    public ColorPickerDialog(Context context, OnColorChangeListener listener,  int initialColor, int initialAlpha) {
        super(context);

        mListener = listener;
        mInitialColor = initialColor;
        mInitialAlpha = initialAlpha;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorPickerView cpv =  new ColorPickerView(getContext());


        OnColorChangeListener l = new OnColorChangeListener() {
            @Override
            public void onColorChanged(int color, int alpha) {
                mListener.onColorChanged(color, alpha);
            }

            @Override
            public void onDismiss(int color, int alpha) {
                mListener.onColorChanged(color, alpha);
                dismiss();
            }
        };

        cpv.setOnColorChangeListener(l);


        setContentView(cpv);
        cpv.setUsedColor(mInitialColor, mInitialAlpha);
        //setTitle(R.string.settings_bg_color_dialog);
        if(Utilities.itRuLocale(getContext())) {
            setTitle("Смена Цвета");
        }else{
            setTitle("Change Color");
        }

    }

    public interface OnColorChangeListener {
        public void onDismiss(int color, int alpha);
        public void onColorChanged(int color, int alpha);
    }



    public static class ColorPickerView extends View implements View.OnTouchListener {
        // Константы, определяющие что именно мы устанавливаем в данный момент
        protected static final int SET_COLOR = 0;
        protected static final int SET_SATUR = 1;
        protected static final int SET_ALPHA = 2;
        // и флаг, который будет устанавливаться в одну из этих констант.
        // (как-то непонятно я выразился)
        private int mMode;

        float cx;
        float cy;
        float rad_1; //
        float rad_2; //
        float rad_3; //
        float r_centr; // радиусы наших окружностей

        float r_sel_c; //
        float r_sel_s; //
        float r_sel_a; // границы полей выбора

        private int size;
        private float alpha;

        // всякие краски
        private Paint p_color = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint p_satur = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint p_alpha = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint p_white = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint p_handl = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint p_center = new Paint(Paint.ANTI_ALIAS_FLAG);

        private float deg_col; // углы поворота
        private float deg_sat; // указателей - стрелок
        private float deg_alp; // ********************

        private float lc; //
        private float lm; // отступы и выступы линий
        private float lw; //

        private int mColor;
        private int[] argb = new int[]{255, 0, 0, 0};

        private float[] hsv = new float[]{0, 1f, 1f};

        public ColorPickerView(Context context) {
            this(context, null);
        }

        public ColorPickerView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init(context);
        }

        private void init(Context context) {
            setFocusable(true);


            p_color.setStyle(Paint.Style.STROKE);
            p_satur.setStyle(Paint.Style.STROKE);
            p_alpha.setStyle(Paint.Style.STROKE);
            p_center.setStyle(Paint.Style.FILL_AND_STROKE);
            p_white.setStrokeWidth(2);
            p_white.setColor(Color.WHITE);
            p_white.setStyle(Paint.Style.STROKE);
            p_handl.setStrokeWidth(5);
            p_handl.setColor(Color.WHITE);
            p_handl.setStrokeCap(Paint.Cap.ROUND);

            setOnTouchListener(this);
        }

        private void calcSizes() {
            //
            //
            cx = size * 0.5f;
            cy = cx;
            lm = size * 0.043f;
            lw = size * 0.035f;
            rad_1 = size * 0.44f;
            r_sel_c = size * 0.39f;
            rad_2 = size * 0.34f;
            r_sel_s = size * 0.29f;
            rad_3 = size * 0.24f;
            r_sel_a = size * 0.19f;
            r_centr = size * 0.18f;

            lc = size * 0.08f;
            p_color.setStrokeWidth(lc);
            p_satur.setStrokeWidth(lc);
            p_alpha.setStrokeWidth(lc);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int mWidth = measure(widthMeasureSpec);
            int mHeight = measure(heightMeasureSpec);
            size = Math.min(mWidth, mHeight);
            setMeasuredDimension(size, size);

            // Вычислили размер доступной области, определили что меньше
            // и установили размер нашей View в виде квадрата со стороной в
            // высоту или ширину экрана в зависимости от ориентации.
            // Вместо Math.min как вариант можно использовать getConfiguration,
            // величину size можно умножать на какие-нибудь коэффициенты,
            // задавая размер View относительно размера экрана. Например так:

        /*int orient = getResources().getConfiguration().orientation;

        switch (orient) {
        case Configuration.ORIENTATION_PORTRAIT:
            size = (int) (measureHeight * port);

            break;
        case Configuration.ORIENTATION_LANDSCAPE:
            size = (int) (measureHeight * land);
            break;
        }*/


            calcSizes();
            // И запустили метод для расчетов всяких наших размеров
        }

        private int measure(int measureSpec) {
            int result = 0;
            int specMoge = MeasureSpec.getMode(measureSpec);
            int specSize = MeasureSpec.getSize(measureSpec);
            if (specMoge == MeasureSpec.UNSPECIFIED) result = 200;
            else result = specSize;
            return result;
        }


        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            // Ну а тут будем рисовать
            // Для начала проверим, что все работает – нарисуем просто фон
            //c.drawColor(Color.BLUE);

            //c.drawCircle(cx, cy, rad_1, p_substrate);

            c.drawCircle(cx, cy, r_centr, p_center);
            drawSaturGradient(c);
            drawAlphaGradient(c);
            drawColorGradient(c);
            drawLines(c);

            invalidate();
        }


   public void setUsedColor(int color, int alpha) {
            float a = (float) alpha / 255;
            mColor = color;
            Color.colorToHSV(mColor, hsv);
            setColScale(hsv[0]);
            float deg = 0;
            if (hsv[1] == 1) deg = 90 * hsv[2];
            else if (hsv[2] == 1) deg = 180 - 90 * hsv[1];
            else if (hsv[1] == 0) deg = 360 - 180 * hsv[2];
            setSatScale(deg);
            setAlphaScale(180 - 180 * a);
        }


        private void drawColorGradient(Canvas c) {

            SweepGradient s = null;
            int[] sg = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.RED};
            s = new SweepGradient(cx, cy, sg, null);
            p_color.setShader(s);
            c.drawCircle(cx, cy, rad_1, p_color);
        }

        private void drawSaturGradient(Canvas c) {

            SweepGradient s = null;
            int[] sg = new int[]{
                    Color.HSVToColor(new float[]{deg_col, 1, 0}), Color.HSVToColor(new float[]{deg_col, 1, 1}), Color.HSVToColor(new float[]{hsv[0], 0, 1}), Color.HSVToColor(new float[]{hsv[0], 0, 0.5f}), Color.HSVToColor(new float[]{deg_col, 1, 0})
            };
            s = new SweepGradient(cx, cy, sg, null);
            p_satur.setShader(s);
            c.drawCircle(cx, cy, rad_2, p_satur);
        }

        private void drawAlphaGradient(Canvas c) {
            // три белых линии на черном фоне как бы помогают визуально
            // оценить уровень прозрачности
            c.drawCircle(cx, cy, rad_3 - lw, p_white);
            c.drawCircle(cx, cy, rad_3, p_white);
            c.drawCircle(cx, cy, rad_3 + lw, p_white);
// вытаскиваем компоненты RGB из нашего цвета
            int ir = Color.red(mColor);
            int ig = Color.green(mColor);
            int ib = Color.blue(mColor);
            // массив из двух цветов – наш и он же полностью прозрачный
            int e = Color.argb(0, ir, ig, ib);
            int[] mCol = new int[]{mColor, e, e, mColor};
            // Это мы уже проходили
            Shader sw = new SweepGradient(cx, cy, mCol, null);
            p_alpha.setShader(sw);
            c.drawCircle(cx, cy, rad_3, p_alpha);
        }

        private void drawLines(Canvas c) {
            float d = deg_col;
            c.rotate(d, cx, cy);
            c.drawLine(cx + rad_1 + lm, cy, cx + rad_1 - lm, cy, p_handl);
            c.rotate(-d, cx, cy);
            d = deg_sat;
            c.rotate(d, cx, cy);
            c.drawLine(cx + rad_2 + lm, cy, cx + rad_2 - lm, cy, p_handl);
            c.rotate(-d, cx, cy);
            d = deg_alp;
            c.rotate(d, cx, cy);
            c.drawLine(cx + rad_3 + lm, cy, cx + rad_3 - lm, cy, p_handl);
            c.rotate(-d, cx, cy);
        }

        protected void setColScale(float f) {
            deg_col = f;
            hsv[0] = f;

            mColor = Color.HSVToColor(argb[0], hsv);
            p_center.setColor(mColor);
        }

        protected void setSatScale(float f) {
            deg_sat = f;
            if (f < 90) {
                hsv[1] = 1;
                hsv[2] = f / 90;
            } else if (f >= 90 && f < 180) {
                hsv[1] = 1 - (f - 90) / 90;
                hsv[2] = 1;
            } else {
                hsv[1] = 0;
                hsv[2] = 1 - (f - 180) / 180;
            }
            mColor = Color.HSVToColor(argb[0], hsv);
            p_center.setColor(mColor);
        }


        protected void setAlphaScale(float f) {
            deg_alp = f;

            if(deg_alp >= 180){
                argb[0] = (int) (255 -  (360 - f) / 180 * 255);
            }else{
                argb[0] = (int) (255 -  f / 180 * 255);
            }

            mColor = Color.HSVToColor(argb[0], hsv);
            alpha = (float) Color.alpha(mColor) / 255;
            p_center.setColor(mColor);
        }

        protected float getAngle(float x, float y) {
            float deg = 0;
            if (x != 0) deg = y / x;
            deg = (float) Math.toDegrees(Math.atan(deg));
            if (x < 0) deg += 180;
            else if (x > 0 && y < 0) deg += 360;
            return deg;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float a = Math.abs(motionEvent.getX() - cx);
                    float b = Math.abs(motionEvent.getY() - cy);
                    float c = (float) Math.sqrt(a * a + b * b);
                    if (c > r_sel_c) mMode = SET_COLOR;
                    else if (c < r_sel_c && c > r_sel_s) mMode = SET_SATUR;
                    else if (c < r_sel_s && c > r_sel_a) mMode = SET_ALPHA;
                    else if (c < r_centr) listener.onDismiss(mColor, Color.alpha(mColor));
                    break;

                case MotionEvent.ACTION_MOVE:
                    float x = motionEvent.getX() - cx;
                    float y = motionEvent.getY() - cy;
                    switch (mMode) {
                        case SET_COLOR:
                            setColScale(getAngle(x, y));
                            break;

                        case SET_SATUR:
                            setSatScale(getAngle(x, y));
                            break;

                        case SET_ALPHA:
                            setAlphaScale(getAngle(x, y));
                            break;
                    }
                    invalidate();
                    break;
            }
            return true;
        }

        private OnColorChangeListener listener;

        public void setOnColorChangeListener(OnColorChangeListener l) {
            this.listener = l;
        }
    }
}