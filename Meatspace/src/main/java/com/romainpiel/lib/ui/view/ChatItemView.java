package com.romainpiel.lib.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.romainpiel.lib.gif.GIFUtils;
import com.romainpiel.lib.ui.helper.InflateHelper;
import com.romainpiel.lib.ui.listener.OnViewChangedListener;
import com.romainpiel.lib.utils.Debug;
import com.romainpiel.meatspace.R;
import com.romainpiel.model.Chat;

import java.util.Date;

import butterknife.InjectView;
import butterknife.Views;

/**
 * Meatspace
 * User: romainpiel
 * Date: 02/09/2013
 * Time: 14:40
 */
public class ChatItemView extends LinearLayout implements OnViewChangedListener {

    InflateHelper inflateHelper;
    Drawable foregroundSelector;

    @InjectView(R.id.item_chat_gif) GIFView gif;
    @InjectView(R.id.item_chat_timestamp) TextView timestamp;
    @InjectView(R.id.item_chat_message) TextView message;

    public ChatItemView(Context context) {
        super(context);
        init();
    }

    public ChatItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflateHelper = new InflateHelper(getContext(), this, this, R.layout.item_chat);
        foregroundSelector = getResources().getDrawable(R.drawable.item_fg_ms);

        setOrientation(HORIZONTAL);
        setBackgroundResource(R.drawable.item_chat_bg_ms);
    }

    @Override
    protected void onFinishInflate() {
        inflateHelper.onFinishInflate();
        super.onFinishInflate();
    }

    public static ChatItemView build(Context context) {
        ChatItemView instance = new ChatItemView(context);
        instance.onFinishInflate();
        return instance;
    }

    public void bind(Chat chat) {
        if (chat != null) {

            Chat.Value value = chat.getValue();

            try {
                gif.setImage(GIFUtils.mediaToGIFbytes(value.getMedia()));
            } catch (IllegalArgumentException e) {
                // the gif could not be decoded
                Debug.out(e);
            }

            Date date = new Date(value.getCreated());
            timestamp.setText(com.romainpiel.lib.utils.DateUtils.formatTime(getContext(), date));
            message.setText(value.getMessage());
        }
    }

    @Override
    public void onViewChanged() {
        Views.inject(this);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        foregroundSelector.setState(getDrawableState());

        //redraw
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        super.onSizeChanged(width, height, oldwidth, oldheight);
        foregroundSelector.setBounds(0, 0, width, height);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        foregroundSelector.draw(canvas);
    }
}
