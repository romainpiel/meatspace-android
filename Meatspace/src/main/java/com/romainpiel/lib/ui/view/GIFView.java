package com.romainpiel.lib.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.romainpiel.lib.gif.decoder.GIFDecode;
import com.romainpiel.lib.utils.Debug;

import java.io.ByteArrayInputStream;

public class GIFView extends View {

    private Paint paint;
	private GIFDecode decode;

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
    }

    public void setImage(byte[] bytes) {
        decode = new GIFDecode();
        decode.read(new ByteArrayInputStream(bytes));
    }

	@Override
	protected void onDraw(Canvas canvas) {
        if (decode != null && decode.getFrameCount() > 0) {
            canvas.drawBitmap(decode.next(), 0, 0, paint);
            postInvalidateDelayed(150);
        }
	}

}