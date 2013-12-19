package com.romainpiel.lib.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.romainpiel.lib.ui.listener.OnMenuClickListener;
import com.romainpiel.lib.ui.view.ChatItemView;
import com.romainpiel.model.Chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Meatspace
 * User: romainpiel
 * Date: 01/11/2013
 * Time: 18:11
 *
 * adapter for a chat view group
 */
public class ChatAdapter extends BaseAdapter {

    private Context context;
    private List<Chat> items;
    private OnMenuClickListener<Chat> onMuteClickListener;

    public ChatAdapter(Context context) {
        this.context = context;
        items = new ArrayList<Chat>();
    }

    public void setItems(Collection<Chat> items) {
        this.items.clear();
        if (items != null) {
            for (Chat item : items) {
                if (!item.getValue().isMuted()) {
                    this.items.add(item);
                }
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
    public int getCount() {
        return items.size();
    }

    @Override
    public Chat getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatItemView itemView;
        if (convertView == null) {
            itemView = ChatItemView.build(context);
            itemView.setOnMuteClickListener(onMuteClickListener);
        } else {
            itemView = (ChatItemView) convertView;
        }

        itemView.bind(getItem(position));

        return itemView;
    }
}
