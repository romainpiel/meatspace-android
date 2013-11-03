package com.romainpiel.lib.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 03/11/2013
 * Time: 20:01
 */
public class GifView extends View {

    private Movie movie;
    private long timeElapsed;

    public GifView(Context context) {
        super(context);
        init();
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GifView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        timeElapsed = 0;
    }

    public void setImage(byte[] bytes) {
        movie = Movie.decodeByteArray(bytes, 0, bytes.length);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = android.os.SystemClock.uptimeMillis();
        if (timeElapsed == 0) {
            timeElapsed = now;
        }
        if (movie != null) {
            int dur = movie.duration();
            if (dur == 0) {
                dur = 1000;
            }
            int relTime = (int)((now - timeElapsed) % dur);
            movie.setTime(relTime);
            movie.draw(canvas, getWidth() - movie.width(), getHeight() - movie.height());
            invalidate();
        }
    }
}
