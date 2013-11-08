package com.romainpiel.lib.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.romainpiel.lib.gif.decoder.GIFDecode;

import java.io.ByteArrayInputStream;

public class GIFView extends View {

    private Paint paint;
	private GIFDecode decode;
    private Rect src, dst;

    public GIFView(Context context) {
        super(context);
        init();
    }

    public GIFView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GIFView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        src = new Rect();
        dst = new Rect();
    }

    public void setImage(byte[] bytes) {
        decode = new GIFDecode();
        decode.read(new ByteArrayInputStream(bytes));
    }

	@Override
	protected void onDraw(Canvas canvas) {
        if (decode != null && decode.getFrameCount() > 0) {

            Bitmap bmp = decode.next();

            boolean portrait = bmp.getWidth() < bmp.getHeight();

            int bWidth = bmp.getWidth();
            int bHeight = bmp.getHeight();

            int cWidth = canvas.getWidth();
            int cHeight = canvas.getHeight();

            int width = portrait? bWidth : bHeight * cWidth / cHeight;
            int height = portrait? bWidth * cHeight / cWidth : bHeight;

            src.left = bWidth/2 - width/2;
            src.top = bHeight/2 - height/2;
            src.right = bWidth/2 + width/2;
            src.bottom = bHeight/2 + height/2;

            dst.left = 0;
            dst.top = 0;
            dst.right = canvas.getWidth();
            dst.bottom = canvas.getHeight();

            canvas.drawBitmap(bmp, src, dst, paint);

            postInvalidateDelayed(150);
        }
	}

}