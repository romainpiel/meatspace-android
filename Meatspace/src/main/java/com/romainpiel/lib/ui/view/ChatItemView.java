package com.romainpiel.lib.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.romainpiel.lib.gif.GIFUtils;
import com.romainpiel.lib.ui.helper.InflateHelper;
import com.romainpiel.lib.ui.listener.OnMenuClickListener;
import com.romainpiel.lib.ui.listener.OnViewChangedListener;
import com.romainpiel.lib.utils.Debug;
import com.romainpiel.meatspace.R;
import com.romainpiel.model.Chat;

import java.io.ByteArrayInputStream;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Meatspace
 * User: romainpiel
 * Date: 02/09/2013
 * Time: 14:40
 */
public class ChatItemView extends LinearLayout implements OnViewChangedListener {

    @InjectView(R.id.item_chat_gif) ImageView gif;
    @InjectView(R.id.item_chat_timestamp) TextView timestamp;
    @InjectView(R.id.item_chat_message) TextView message;
    @InjectView(R.id.item_chat_menu_button) ImageButton menuButton;

    private InflateHelper inflateHelper;
    private PopupMenu popupMenu;
    private OnMenuClickListener<Chat> onMuteClickListener;

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

    public void bind(final Chat chat) {
        if (chat != null) {

            Chat.Value value = chat.getValue();

            try {
                ByteArrayInputStream in = new ByteArrayInputStream(GIFUtils.mediaToGIFbytes(value.getMedia()));
                GifDrawable gifFromStream = new GifDrawable(in);
                gif.setImageDrawable(gifFromStream);
                gif.setVisibility(VISIBLE);
            } catch (Exception e) {
                Debug.out(e);
                gif.setVisibility(INVISIBLE);
            }

            Date date = new Date(value.getCreated());
            timestamp.setText(com.romainpiel.lib.utils.DateUtils.formatTime(getContext(), date));
            message.setText(value.getMessage());

            if (!chat.getValue().isFromMe()) {
                menuButton.setVisibility(VISIBLE);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_card_mute && onMuteClickListener != null) {
                            onMuteClickListener.onMenuClick(chat);
                        }
                        return true;
                    }
                });
            } else {
                menuButton.setVisibility(INVISIBLE);
            }
        }
    }

    public OnMenuClickListener<Chat> getOnMuteClickListener() {
        return onMuteClickListener;
    }

    public void setOnMuteClickListener(OnMenuClickListener<Chat> onMuteClickListener) {
        this.onMuteClickListener = onMuteClickListener;
    }

    @Override
    public void onViewChanged() {
        ButterKnife.inject(this);

        popupMenu = new PopupMenu(getContext(), menuButton);
        popupMenu.inflate(R.menu.card);
        menuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }
}
