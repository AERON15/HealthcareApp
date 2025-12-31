package com.example.healthcareapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.mlkit.vision.digitalink.Ink;

public class HandwritingView extends View {

    // ===== ML Kit Ink =====
    private Ink.Builder inkBuilder = Ink.builder();
    private Ink.Stroke.Builder strokeBuilder;

    // ===== Drawing =====
    private Path path = new Path();
    private Paint paint = new Paint();

    private float lastX, lastY;
    private static final float TOUCH_TOLERANCE = 4f;

    // ===== Listener =====
    public interface OnInkChangedListener {
        void onInkChanged();
    }

    private OnInkChangedListener inkChangedListener;

    public void setOnInkChangedListener(OnInkChangedListener listener) {
        this.inkChangedListener = listener;
    }

    public HandwritingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //settings
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setFilterBitmap(true);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f); // thickness of the stroke
        paint.setColor(0xFF000000);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        long time = System.currentTimeMillis();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                lastX = x;
                lastY = y;

                strokeBuilder = Ink.Stroke.builder();
                strokeBuilder.addPoint(Ink.Point.create(x, y, time));
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - lastX);
                float dy = Math.abs(y - lastY);

                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {

                    float velocity = dx + dy;
                    float factor = velocity < 10 ? 0.7f : 0.3f;

                    float cx = lastX + (x - lastX) * factor;
                    float cy = lastY + (y - lastY) * factor;

                    path.quadTo(lastX, lastY, cx, cy);

                    lastX = x;
                    lastY = y;

                    strokeBuilder.addPoint(Ink.Point.create(x, y, time));
                    notifyInkChanged();
                }
                break;

            case MotionEvent.ACTION_UP:
                path.lineTo(x, y);
                strokeBuilder.addPoint(Ink.Point.create(x, y, time));
                inkBuilder.addStroke(strokeBuilder.build());
                notifyInkChanged();
                break;
        }

        invalidate();
        return true;
    }

    private void notifyInkChanged() {
        if (inkChangedListener != null) {
            inkChangedListener.onInkChanged();
        }
    }

    public Ink getInk() {
        return inkBuilder.build();
    }

    public void clear() {
        path.reset();
        inkBuilder = Ink.builder();
        invalidate();
    }
}
