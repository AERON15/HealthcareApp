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

        paint.setAntiAlias(true);
        paint.setStrokeWidth(8f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF000000);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
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
                strokeBuilder = Ink.Stroke.builder();
                strokeBuilder.addPoint(Ink.Point.create(x, y, time));
                return true;

            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                strokeBuilder.addPoint(Ink.Point.create(x, y, time));
                notifyInkChanged();
                break;

            case MotionEvent.ACTION_UP:
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
