package com.romainpiel.lib.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.romainpiel.lib.ui.helper.InflateHelper;
import com.romainpiel.lib.ui.listener.OnViewChangedListener;
import com.romainpiel.meatspace.R;
import com.romainpiel.model.Chat;

import java.util.Date;

import butterknife.InjectView;
import butterknife.Views;

/**
 * BlaBlaCar
 * User: romainpiel
 * Date: 02/09/2013
 * Time: 14:40
 */
public class ChatItemView extends LinearLayout implements OnViewChangedListener {

    InflateHelper inflateHelper;

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
        setOrientation(HORIZONTAL);
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
            Date date = new Date(chat.getValue().getCreated());
            timestamp.setText(com.romainpiel.lib.utils.DateUtils.formatTime(getContext(), date));
            message.setText(chat.getValue().getMessage());
        }
    }

    @Override
    public void onViewChanged() {
        Views.inject(this);
    }
}
